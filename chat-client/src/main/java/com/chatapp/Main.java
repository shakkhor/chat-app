package com.chatapp;

import com.chatapp.ui.ConnectWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        ConnectWindow connectWindow = new ConnectWindow();
        connectWindow.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
} 