package com.chatapp.ui;

import com.chatapp.client.ChatClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class ChatWindow {
    private ChatClient client;
    private TextArea chatArea;
    private TextField messageField;
    private String receiver;

    public ChatWindow(ChatClient client, String receiver) {
        this.client = client;
        this.receiver = receiver;
    }

    public void show(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Chat area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        // Message input area
        VBox bottomBox = new VBox(10);
        messageField = new TextField();
        Button sendButton = new Button("Send");
        Button fileButton = new Button("Send File");
        Button closeButton = new Button("Close Chat");

        bottomBox.getChildren().addAll(messageField, sendButton, fileButton, closeButton);

        root.setCenter(chatArea);
        root.setBottom(bottomBox);

        // Event handlers
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());
        fileButton.setOnAction(e -> sendFile());
        closeButton.setOnAction(e -> stage.close());

        Scene scene = new Scene(root, 400, 500);
        stage.setTitle("Chat with " + receiver);
        stage.setScene(scene);
        stage.show();
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(receiver + ":" + message);
            displayMessage("You: " + message);
            messageField.clear();
        }
    }

    private void sendFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            client.sendFile(file, receiver);
            displayMessage("You sent a file: " + file.getName());
        }
    }

    public void displayMessage(String message) {
        Platform.runLater(() -> {
            chatArea.appendText(message + "\n");
        });
    }
} 