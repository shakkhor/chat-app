package com.chatapp.ui;

import com.chatapp.client.ChatClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConnectWindow {
    private TextField usernameField;
    private TextField hostField;
    private TextField portField;

    public void show(Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // Username input
        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        usernameField.setPromptText("Enter username");

        // Server host input
        Label hostLabel = new Label("Server Host:");
        hostField = new TextField("localhost");

        // Server port input
        Label portLabel = new Label("Server Port:");
        portField = new TextField("5001");

        // Connect button
        Button connectButton = new Button("Connect");
        connectButton.setOnAction(e -> connect(stage));

        root.getChildren().addAll(
            usernameLabel, usernameField,
            hostLabel, hostField,
            portLabel, portField,
            connectButton
        );

        Scene scene = new Scene(root, 300, 400);
        stage.setTitle("Connect to Chat Server");
        stage.setScene(scene);
        stage.show();
    }

    private void connect(Stage stage) {
        try {
            String username = usernameField.getText().trim();
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());

            if (username.isEmpty()) {
                showError("Username cannot be empty");
                return;
            }

            ChatClient client = new ChatClient(username, host, port);
            MainWindow mainWindow = new MainWindow(client);
            mainWindow.show(new Stage());
            stage.close();
        } catch (NumberFormatException e) {
            showError("Invalid port number");
        } catch (Exception e) {
            showError("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
} 