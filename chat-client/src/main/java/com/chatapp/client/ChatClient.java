package com.chatapp.client;

import com.chatapp.ui.MainWindow;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatClient {
    private String username;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MainWindow mainWindow;
    private volatile boolean running = true;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private PrintWriter logWriter;

    public ChatClient(String username, String host, int port) throws IOException {
        this.username = username;
        
        // Create logs directory if it doesn't exist
        File logsDir = new File("logs");
        if (!logsDir.exists()) {
            logsDir.mkdir();
        }

        // Create log file with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File logFile = new File(logsDir, "client_" + username + "_" + timestamp + ".log");
        logWriter = new PrintWriter(new FileWriter(logFile, true));

        log("Connecting to server " + host + ":" + port);
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        // Send username to server
        out.println(username);
        log("Connected successfully");
        
        // Start listening for messages
        new Thread(this::receiveMessages).start();
    }

    private void log(String message) {
        String logMessage = String.format("[%s] %s", LocalDateTime.now().format(formatter), message);
        if (logWriter != null) {
            logWriter.println(logMessage);
            logWriter.flush();
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                if (message.startsWith("CLIENTS:")) {
                    handleClientList(message.substring(8));
                } else if (message.startsWith("FILE:")) {
                    handleFileTransfer(message.substring(5));
                } else {
                    handleMessage(message);
                }
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }

    private void handleClientList(String clientList) {
        if (mainWindow != null) {
            mainWindow.updateUserList(clientList.split(","));
        }
    }

    private void handleFileTransfer(String message) {
        try {
            String[] parts = message.split(":", 4);
            if (parts.length == 4) {
                String sender = parts[0];
                String fileName = parts[1];
                long fileSize = Long.parseLong(parts[2]);

                // Create downloads directory if it doesn't exist
                File downloadDir = new File("downloads");
                if (!downloadDir.exists()) {
                    downloadDir.mkdir();
                }

                // Create file to save
                File file = new File(downloadDir, fileName);
                log("Receiving file: " + fileName + " (" + fileSize + " bytes)");
                
                // Read file data
                FileOutputStream fos = new FileOutputStream(file);
                InputStream is = socket.getInputStream();
                byte[] buffer = new byte[4096];
                long totalRead = 0;
                
                while (totalRead < fileSize) {
                    int read = is.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead));
                    if (read == -1) break;
                    fos.write(buffer, 0, read);
                    totalRead += read;
                }
                
                fos.close();
                log("File received successfully: " + fileName);
                
                // Display message in chat window
                Platform.runLater(() -> {
                    mainWindow.displayMessage(sender, "File sent: " + fileName);
                });
            }
        } catch (IOException e) {
            log("Error receiving file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleMessage(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String sender = parts[0];
            String content = parts[1];
            
            Platform.runLater(() -> {
                if (mainWindow != null) {
                    mainWindow.displayMessage(sender, content);
                }
            });
        }
    }

    public void sendFile(File file, String receiver) {
        try {
            log("Sending file to " + receiver + ": " + file.getName());
            out.println("FILE:" + receiver + ":" + file.getName() + ":" + file.length());
            
            byte[] buffer = new byte[4096];
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = socket.getOutputStream();
            
            int count;
            long totalSent = 0;
            while ((count = fis.read(buffer)) > 0) {
                os.write(buffer, 0, count);
                totalSent += count;
            }
            os.flush();
            fis.close();
            log("File sent successfully: " + file.getName() + " (" + totalSent + " bytes)");
        } catch (IOException e) {
            log("Error sending file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
        log("Sent message: " + message);
    }

    public void disconnect() {
        running = false;
        try {
            log("Disconnecting from server");
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (logWriter != null) {
                logWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public void setMainWindow(MainWindow window) {
        this.mainWindow = window;
        window.setClient(this);
    }
} 