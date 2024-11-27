
You can run multiple client instances to test the chat functionality.

## Usage Guide

1. Server Setup:
   - Start the server first
   - Server runs on port 5001 by default
   - Server logs are stored in `logs` directory

2. Client Connection:
   - Launch the client application
   - Enter your username
   - Use "localhost" as server host for local testing
   - Use port 5001 to connect
   - Click "Connect"

3. Chat Features:
   - View active users in the "Active Users" tab
   - Click on a user and "Start Chat" to begin conversation
   - Use the message input field to type messages
   - Press Enter or click "Send" to send messages
   - Use "File" button to send files
   - View connected users in the "Connected Users" tab
   - Use refresh buttons to update user lists

4. File Sharing:
   - Click "File" button in chat
   - Select file from your system
   - Files are automatically saved in `downloads` directory
   - Click on received file links to open them

## Directory Structure Details

### Server Components
- `ChatServer.java`: Main server class handling client connections
- `ClientHandler.java`: Manages individual client connections
- `model/*.java`: Data model classes

### Client Components
- `Main.java`: JavaFX application entry point
- `ChatClient.java`: Network communication handler
- `ConnectWindow.java`: Initial connection window
- `MainWindow.java`: Main chat interface with tabs

## Common Issues and Solutions

1. Connection Refused:
   - Ensure server is running
   - Verify correct port number (5001)
   - Check firewall settings

2. File Transfer Issues:
   - Ensure sufficient disk space
   - Check write permissions in downloads directory

3. UI Not Responding:
   - Check server connection
   - Restart client application

## Development Notes

1. Adding New Features:
   - UI changes go in `chat-client/src/main/java/com/chatapp/ui/`
   - Server logic goes in `chat-server/src/main/java/com/chatapp/server/`
   - Network protocol changes need updates in both ChatClient and ClientHandler

2. Building:
   - Each module (client/server) can be built independently
   - Use `mvn clean package` to create executable JARs
   - Use `mvn javafx:run` for client development

3. Testing:
   - Run server first
   - Launch multiple client instances
   - Test with different usernames
   - Verify file transfers work