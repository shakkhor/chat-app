package com.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatServer {
    private static final int PORT = 5001;
    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private volatile boolean running = true;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private PrintWriter logWriter;

    public ChatServer() {
        try {
            // Create logs directory if it doesn't exist
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdir();
            }

            // Create log file with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File logFile = new File(logsDir, "server_" + timestamp + ".log");
            logWriter = new PrintWriter(new FileWriter(logFile, true));
        } catch (IOException e) {
            System.err.println("Error creating log file: " + e.getMessage());
        }
    }

    public void log(String message) {
        String logMessage = String.format("[%s] %s", LocalDateTime.now().format(formatter), message);
        System.out.println(logMessage);
        if (logWriter != null) {
            logWriter.println(logMessage);
            logWriter.flush();
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            log("Server started on port " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                log("New client connected from: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            log("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        try {
            log("Shutting down server...");
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            // Close all client connections
            for (ClientHandler client : clients.values()) {
                client.disconnect();
            }
            clients.clear();
            
            if (logWriter != null) {
                logWriter.close();
            }
        } catch (IOException e) {
            System.err.println("Error shutting down server: " + e.getMessage());
        }
    }

    public void broadcast(String message, String sender) {
        log("Broadcasting from " + sender + ": " + message);
        for (ClientHandler client : clients.values()) {
            if (!client.getUsername().equals(sender)) {
                client.sendMessage(message);
            }
        }
    }

    public void addClient(String username, ClientHandler handler) {
        clients.put(username, handler);
        log("Client connected: " + username);
        broadcastClientList();
    }

    public void removeClient(String username) {
        clients.remove(username);
        log("Client disconnected: " + username);
        broadcastClientList();
    }

    public void broadcastClientList() {
        String[] usernames = clients.keySet().toArray(new String[0]);
        String clientList = "CLIENTS:" + String.join(",", usernames);
        log("Active clients: " + String.join(", ", usernames));
        
        // Send updated client list to all connected clients
        for (ClientHandler client : clients.values()) {
            try {
                client.sendMessage(clientList);
            } catch (Exception e) {
                log("Error sending client list to " + client.getUsername() + ": " + e.getMessage());
            }
        }
    }

    public ClientHandler getClient(String username) {
        return clients.get(username);
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
} 