package com.chatapp.ui;

import com.chatapp.client.ChatClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javafx.scene.shape.SVGPath;

public class MainWindow {
    private ChatClient client;
    private TabPane tabPane;
    private Map<String, Tab> chatTabs;
    private ListView<String> activeUsersList;
    private ListView<String> connectedUsersList;
    private TextArea currentChatArea;
    private static final Color SENT_COLOR = Color.rgb(220, 248, 198);
    private static final Color RECEIVED_COLOR = Color.rgb(229, 229, 229);

    public MainWindow(ChatClient client) {
        this.client = client;
        this.chatTabs = new HashMap<>();
        client.setMainWindow(this);
    }

    public void setClient(ChatClient client) {
        this.client = client;
    }

    public void show(Stage stage) {
        tabPane = new TabPane();
        
        // Create Active Users Tab
        Tab activeUsersTab = createActiveUsersTab();
        
        // Create Connected Users Tab
        Tab connectedUsersTab = createConnectedUsersTab();
        
        // Add tabs to TabPane
        tabPane.getTabs().addAll(activeUsersTab, connectedUsersTab);

        Scene scene = new Scene(tabPane, 800, 600);
        stage.setTitle("Chat Application - " + client.getUsername());
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> client.disconnect());
        stage.show();
    }

    private Tab createActiveUsersTab() {
        Tab tab = new Tab("Active Users");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Create HBox for label and refresh button
        HBox headerBox = new HBox(10);
        Label titleLabel = new Label("Online Users:");
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> client.sendMessage("REFRESH_USERS"));
        headerBox.getChildren().addAll(titleLabel, refreshButton);

        activeUsersList = new ListView<>();
        Button chatButton = new Button("Start Chat");
        chatButton.setOnAction(e -> startChat());

        content.getChildren().addAll(
            headerBox,
            activeUsersList,
            chatButton
        );

        tab.setContent(content);
        return tab;
    }

    private Tab createConnectedUsersTab() {
        Tab tab = new Tab("Connected Users");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Create HBox for label and refresh button
        HBox headerBox = new HBox(10);
        Label titleLabel = new Label("Currently Connected:");
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshConnectedUsers());
        headerBox.getChildren().addAll(titleLabel, refreshButton);

        connectedUsersList = new ListView<>();

        content.getChildren().addAll(
            headerBox,
            connectedUsersList
        );

        tab.setContent(content);
        return tab;
    }

    private void refreshConnectedUsers() {
        Platform.runLater(() -> {
            connectedUsersList.getItems().clear();
            // Add all chat tabs to connected users list
            for (String username : chatTabs.keySet()) {
                connectedUsersList.getItems().add(username);
            }
        });
    }

    private void startChat() {
        String selectedUser = activeUsersList.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Please select a user to chat with");
            return;
        }

        if (!chatTabs.containsKey(selectedUser)) {
            Tab chatTab = createChatTab(selectedUser);
            chatTabs.put(selectedUser, chatTab);
            tabPane.getTabs().add(chatTab);
            refreshConnectedUsers(); // Update connected users list
        }
        
        tabPane.getSelectionModel().select(chatTabs.get(selectedUser));
    }

    private Tab createChatTab(String username) {
        Tab tab = new Tab("Chat with " + username);
        
        BorderPane content = new BorderPane();
        content.setPadding(new Insets(10));

        // Chat area using VBox
        VBox chatArea = new VBox(10);
        chatArea.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(chatArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setVvalue(1.0);
        chatArea.heightProperty().addListener((obs, old, val) -> 
            scrollPane.setVvalue(1.0));

        // Message input area with better layout
        VBox bottomContainer = new VBox(0); // Container for input area
        bottomContainer.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");

        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputBox.setAlignment(Pos.CENTER_LEFT);
        inputBox.setMaxWidth(Double.MAX_VALUE); // Make HBox take full width

        // Text field with better styling
        TextField messageField = new TextField();
        messageField.setPromptText("Type a message...");
        messageField.setStyle(
            "-fx-padding: 8 12;" +
            "-fx-font-size: 14px;" +
            "-fx-background-color: white;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;"
        );
        HBox.setHgrow(messageField, Priority.ALWAYS);

        // Buttons container
        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMinWidth(120); // Fixed width for button container

        // Simple text buttons
        Button sendButton = new Button("Send");
        sendButton.setStyle(
            "-fx-background-color: #2196F3;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8 15;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        sendButton.setOnMouseEntered(e -> 
            sendButton.setStyle(
                "-fx-background-color: #1976D2;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 8 15;" +
                "-fx-background-radius: 4;" +
                "-fx-cursor: hand;"
            )
        );
        sendButton.setOnMouseExited(e -> 
            sendButton.setStyle(
                "-fx-background-color: #2196F3;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 8 15;" +
                "-fx-background-radius: 4;" +
                "-fx-cursor: hand;"
            )
        );

        Button fileButton = new Button("File");
        fileButton.setStyle(
            "-fx-background-color: #757575;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8 15;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        fileButton.setOnMouseEntered(e -> 
            fileButton.setStyle(
                "-fx-background-color: #616161;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 8 15;" +
                "-fx-background-radius: 4;" +
                "-fx-cursor: hand;"
            )
        );
        fileButton.setOnMouseExited(e -> 
            fileButton.setStyle(
                "-fx-background-color: #757575;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 8 15;" +
                "-fx-background-radius: 4;" +
                "-fx-cursor: hand;"
            )
        );

        // Handle message sending
        Runnable sendMessage = () -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                client.sendMessage(username + ":" + message);
                addMessage(chatArea, "You", message, true);
                messageField.clear();
                messageField.requestFocus();
            }
        };

        messageField.setOnAction(e -> sendMessage.run());
        sendButton.setOnAction(e -> sendMessage.run());

        fileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose File to Send");
            File file = fileChooser.showOpenDialog(tab.getTabPane().getScene().getWindow());
            
            if (file != null) {
                client.sendFile(file, username);
                addMessage(chatArea, "You", "Sending file: " + file.getName(), true);
            }
            messageField.requestFocus();
        });

        // Add buttons to button container
        buttonBox.getChildren().addAll(sendButton, fileButton);

        // Add all elements to input box
        inputBox.getChildren().addAll(messageField, buttonBox);
        bottomContainer.getChildren().add(inputBox);

        content.setCenter(scrollPane);
        content.setBottom(bottomContainer);

        // Set initial focus to message field
        Platform.runLater(() -> messageField.requestFocus());

        tab.setContent(content);
        
        tab.setOnClosed(e -> {
            chatTabs.remove(username);
            refreshConnectedUsers();
        });

        return tab;
    }

    private void addMessage(VBox chatArea, String sender, String message, boolean isSent) {
        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(5));
        messageBox.setMaxWidth(Double.MAX_VALUE);

        VBox textBox = new VBox(2);
        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10pt;");
        
        Text messageText = new Text(message);
        messageText.setWrappingWidth(400); // Increased width for messages

        textBox.getChildren().addAll(senderLabel, messageText);
        textBox.setStyle(String.format(
            "-fx-background-color: #%s;" +
            "-fx-padding: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-max-width: 450px;" + // Maximum width for the message box
            "-fx-min-width: 100px",  // Minimum width for the message box
            isSent ? "DCF8C6" : "E5E5E5"
        ));

        if (isSent) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            textBox.setStyle(textBox.getStyle() + ";" +
                "-fx-background-color: #DCF8C6;" +
                "-fx-border-radius: 10 10 0 10;" +
                "-fx-background-radius: 10 10 0 10;");
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            textBox.setStyle(textBox.getStyle() + ";" +
                "-fx-background-color: #E5E5E5;" +
                "-fx-border-radius: 10 10 10 0;" +
                "-fx-background-radius: 10 10 10 0;");
        }

        messageBox.getChildren().add(textBox);
        Platform.runLater(() -> chatArea.getChildren().add(messageBox));
    }

    public void updateUserList(String[] users) {
        Platform.runLater(() -> {
            activeUsersList.getItems().clear();
            for (String user : users) {
                if (!user.equals(client.getUsername())) {
                    activeUsersList.getItems().add(user);
                }
            }
        });
    }

    public void displayMessage(String sender, String message) {
        Platform.runLater(() -> {
            Tab chatTab = chatTabs.get(sender);
            if (chatTab == null) {
                chatTab = createChatTab(sender);
                chatTabs.put(sender, chatTab);
                tabPane.getTabs().add(chatTab);
                refreshConnectedUsers();
            }
            
            BorderPane content = (BorderPane) chatTab.getContent();
            ScrollPane scrollPane = (ScrollPane) content.getCenter();
            VBox chatArea = (VBox) scrollPane.getContent();
            
            // Check if it's a file message
            if (message.startsWith("File sent: ")) {
                String fileName = message.substring("File sent: ".length());
                
                // Create file message with download link
                VBox fileMessage = new VBox(5);
                Hyperlink fileLink = new Hyperlink("Download " + fileName);
                fileLink.setOnAction(e -> {
                    File downloadDir = new File("downloads");
                    File file = new File(downloadDir, fileName);
                    if (file.exists()) {
                        try {
                            java.awt.Desktop.getDesktop().open(file);
                        } catch (IOException ex) {
                            showAlert("Error opening file: " + ex.getMessage());
                        }
                    } else {
                        showAlert("File not found: " + fileName);
                    }
                });
                
                addMessage(chatArea, sender, "Sent a file: " + fileName + "\nClick to download", false);
                fileMessage.getChildren().add(fileLink);
                
                HBox wrapper = new HBox();
                wrapper.getChildren().add(fileMessage);
                wrapper.setAlignment(Pos.CENTER_LEFT);
                chatArea.getChildren().add(wrapper);
            } else {
                addMessage(chatArea, sender, message, false);
            }
            
            tabPane.getSelectionModel().select(chatTab);
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }
} 