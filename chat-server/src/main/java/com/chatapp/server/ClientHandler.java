package com.chatapp.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // First message from client should be their username
            username = in.readLine();
            server.addClient(username, this);
            server.log("User connected: " + username);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equals("REFRESH_USERS")) {
                    // Just broadcast the client list without sending a chat message
                    server.broadcastClientList();
                } else if (message.startsWith("FILE:")) {
                    handleFileTransfer(message);
                } else if (message.contains(":")) {
                    // Message format: "receiver:message"
                    String[] parts = message.split(":", 2);
                    String receiver = parts[0];
                    String content = parts[1];
                    
                    ClientHandler receiverHandler = server.getClient(receiver);
                    if (receiverHandler != null) {
                        receiverHandler.sendMessage(username + ":" + content);
                        server.log("Message from " + username + " to " + receiver + ": " + content);
                    }
                }
            }
        } catch (IOException e) {
            server.log("Error handling client " + username + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void disconnect() {
        try {
            server.removeClient(username);
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            server.log("Error disconnecting client " + username + ": " + e.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }

    private void handleFileTransfer(String message) {
        try {
            String[] parts = message.split(":", 4);
            if (parts.length == 4) {
                String receiver = parts[1];
                String fileName = parts[2];
                long fileSize = Long.parseLong(parts[3]);

                server.log("File transfer started: " + fileName + " from " + username + " to " + receiver);

                // Read file data
                byte[] buffer = new byte[4096];
                InputStream is = socket.getInputStream();
                long totalRead = 0;
                
                // Forward file header to receiver
                ClientHandler receiverHandler = server.getClient(receiver);
                if (receiverHandler != null) {
                    receiverHandler.sendMessage("FILE:" + username + ":" + fileName + ":" + fileSize);
                    
                    // Forward file data
                    while (totalRead < fileSize) {
                        int read = is.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead));
                        if (read == -1) break;
                        receiverHandler.socket.getOutputStream().write(buffer, 0, read);
                        totalRead += read;
                    }
                    receiverHandler.socket.getOutputStream().flush();
                    server.log("File transfer completed: " + fileName);
                } else {
                    server.log("File transfer failed: receiver " + receiver + " not found");
                }
            }
        } catch (IOException e) {
            server.log("Error in file transfer: " + e.getMessage());
        }
    }
} 