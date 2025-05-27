import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

// singleton server class handling email operations and user management
public class EmailServer {
    // singleton instance
    private static EmailServer instance;
    // server socket for accepting client connections
    private ServerSocket serverSocket;
    // port number for server socket
    private final int PORT = 12345;
    // flag indicating if server is running
    private boolean running;
    // maps user emails to their folder structure
    private Map<String, Map<String, Folder>> userFolders;
    // maps connected clients to their output streams
    private Map<String, ObjectOutputStream> clientOutputStreams;
    // thread pool for handling client connections
    private ExecutorService executorService;
    // indicates if running in server or client mode
    private boolean isServerMode;
    // database services
    private final UserService userService;
    private final FolderService folderService;
    private final EmailService emailService;

    // private constructor for singleton pattern
    private EmailServer(boolean isServerMode) {
        this.isServerMode = isServerMode;
        this.userFolders = new ConcurrentHashMap<>();
        this.userService = UserService.getInstance();
        this.folderService = FolderService.getInstance();
        this.emailService = EmailService.getInstance();
        
        if (isServerMode) {
            this.clientOutputStreams = new ConcurrentHashMap<>();
            this.executorService = Executors.newCachedThreadPool();
            // init database
            try {
                DatabaseInit.initDB();
            } catch (SQLException e) {
                System.out.println("Database initialization failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // creates default folders for a new user
    private void initializeUserFolders(String email) throws SQLException {
        // Create default folders in database
        folderService.createDefaultFolders(email);
        
        // Initialize in-memory folder structure
        Map<String, Folder> folders = new HashMap<>();
        folders.put("inbox", new Folder("inbox", true));
        folders.put("spam", new Folder("spam", true));
        userFolders.put(email, folders);
        
        // Load existing emails from database into memory
        loadUserEmails(email);
    }

    // loads user's emails from database into memory
    private void loadUserEmails(String email) throws SQLException {
        Map<String, Folder> folders = userFolders.get(email);
        if (folders == null) return;

        System.out.println("\n=== Loading Emails for " + email + " ===");
        // Get all folders for the user
        List<Folder> dbFolders = folderService.getFoldersByUser(email);
        System.out.println("Found " + dbFolders.size() + " folders in database");
        
        for (Folder dbFolder : dbFolders) {
            System.out.println("Processing folder: " + dbFolder.getName() + " (ID: " + dbFolder.getId() + ")");
            // Get emails for each folder
            List<Email> emails = emailService.getEmailsByFolder(dbFolder.getId());
            System.out.println("Found " + emails.size() + " emails in folder");
            
            // Add emails to in-memory folder
            Folder memFolder = folders.get(dbFolder.getName());
            if (memFolder != null) {
                memFolder.setId(dbFolder.getId());  // Set the folder ID
                System.out.println("Setting memory folder ID to: " + dbFolder.getId());
                for (Email emailObj : emails) {
                    memFolder.addEmail(emailObj);
                    System.out.println("Added email: " + emailObj.getSubject() + " (ID: " + emailObj.getId() + ")");
                }
            } else {
                System.err.println("Memory folder not found for: " + dbFolder.getName());
            }
        }
        System.out.println("=== Email Loading Complete ===\n");
    }

    // returns singleton instance in server mode
    public static EmailServer getInstance() {
        return getInstance(true);
    }

    // returns singleton instance with specified mode
    public static EmailServer getInstance(boolean isServerMode) {
        if (instance == null) {
            instance = new EmailServer(isServerMode);
        }
        return instance;
    }

    // starts the email server
    public void start() {
        if (!isServerMode) return;
        
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Email server started on port " + PORT);

            // accept client connections in separate thread
            new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleNewConnection(clientSocket);
                    } catch (IOException e) {
                        if (running) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        } catch (IOException e) {
            System.out.println("Could not start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // handles new client connection in separate thread
    private void handleNewConnection(Socket clientSocket) {
        if (!isServerMode) return;
        
        executorService.submit(() -> {
            try {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                while (running) {
                    try {
                        Object obj = in.readObject();
                        if (obj instanceof String) {
                            String command = (String) obj;
                            if (command.startsWith("LOGIN:")) {
                                handleLogin(command.substring(6), in, out);
                            } else if (command.startsWith("REGISTER:")) {
                                handleRegister(command.substring(9), in, out);
                            } else if (command.startsWith("SEND_EMAIL:")) {
                                handleSendEmail(command.substring(11), in, out);
                            } else {
                                handleCommand(command, out);
                            }
                        } else if (obj instanceof Email) {
                            Email email = (Email) obj;
                            System.out.println("Received email directly:");
                            System.out.println("From: " + email.getFrom());
                            System.out.println("To: " + email.getTo());
                            System.out.println("Subject: " + email.getSubject());
                            deliverEmail(email);
                            // Send acknowledgment
                            out.writeObject("SEND_SUCCESS");
                            out.flush();
                        }
                    } catch (EOFException | SocketException e) {
                        break;  // client disconnected
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // handles email sending command
    private void handleSendEmail(String emailData, ObjectInputStream in, ObjectOutputStream out) throws IOException {
        try {
            // Wait for the Email object
            Object obj = in.readObject();
            if (obj instanceof Email) {
                Email email = (Email) obj;
                deliverEmail(email);
                out.writeObject("SEND_SUCCESS");
            } else {
                out.writeObject("SEND_FAILED:Invalid email data");
            }
            out.flush();
        } catch (Exception e) {
            out.writeObject("SEND_FAILED:" + e.getMessage());
            out.flush();
            e.printStackTrace();
        }
    }

    // routes commands to appropriate handlers
    private void handleCommand(String command, ObjectOutputStream out) throws IOException {
        if (command.startsWith("MOVE_EMAIL:")) {
            handleMoveEmail(command, out);
        } else if (command.startsWith("DELETE_EMAIL:")) {
            handleDeleteEmail(command, out);
        } else if (command.startsWith("MARK_READ:")) {
            handleMarkEmail(command, true, out);
        } else if (command.startsWith("MARK_UNREAD:")) {
            handleMarkEmail(command, false, out);
        } else {
            handleEmailConnection(command, out);
        }
    }

    // authenticates user login attempt
    private void handleLogin(String credentials, ObjectInputStream in, ObjectOutputStream out) throws IOException {
        String[] parts = credentials.split(":");
        if (parts.length != 2) {
            out.writeObject("LOGIN_FAILED:Invalid credentials");
            out.flush();
            return;
        }
        String email = parts[0];
        String password = parts[1];

        try {
            User user = userService.getUserByEmail(email);
            if (user != null && user.getPassword().equals(password)) {
                // Initialize user folders if not already done
                if (!userFolders.containsKey(email)) {
                    initializeUserFolders(email);
                }
                // Update last login time
                user.updateLastLogin();
                userService.updateLastLogin(user);
                // Add to connected clients
                clientOutputStreams.put(email, out);
                
                out.writeObject("LOGIN_SUCCESS");
                out.writeObject(user);
                out.flush();
            } else {
                out.writeObject("LOGIN_FAILED:Invalid email or password");
                out.flush();
            }
        } catch (SQLException e) {
            out.writeObject("LOGIN_FAILED:Database error");
            out.flush();
            e.printStackTrace();
        }
    }

    // registers a new user
    private void handleRegister(String userData, ObjectInputStream in, ObjectOutputStream out) throws IOException {
        String[] parts = userData.split(":");
        String name = parts[0];
        String email = parts[1];
        String password = parts[2];

        try {
            // Check if user already exists
            if (userService.getUserByEmail(email) != null) {
                out.writeObject("REGISTER_FAILED:Email already exists");
                out.flush();
                return;
            }

            // Create new user
            User newUser = new User(name, email, password);
            userService.createUser(newUser);
            
            // Initialize user folders
            initializeUserFolders(email);
            
            out.writeObject("REGISTER_SUCCESS");
            out.flush();
        } catch (SQLException e) {
            out.writeObject("REGISTER_FAILED:Database error");
            out.flush();
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            out.writeObject("REGISTER_FAILED:" + e.getMessage());
            out.flush();
        }
    }

    // moves an email between folders
    private void handleMoveEmail(String emailData, ObjectOutputStream out) throws IOException {
        String[] parts = emailData.split(":");
        String userEmail = parts[1];
        int emailIndex = Integer.parseInt(parts[2]);
        String targetFolder = parts[3];

        try {
            Map<String, Folder> folders = userFolders.get(userEmail);
            if (folders != null) {
                Email emailToMove = null;
                Folder sourceFolder = null;
                
                // Find email and source folder
                for (Folder folder : folders.values()) {
                    List<Email> emails = folder.getEmails();
                    if (emailIndex < emails.size()) {
                        emailToMove = emails.get(emailIndex);
                        sourceFolder = folder;
                        break;
                    }
                }

                if (emailToMove != null && sourceFolder != null && folders.containsKey(targetFolder)) {
                    // Get target folder from database
                    Folder dbTargetFolder = folderService.getFolderByNameAndUser(targetFolder, userEmail);
                    if (dbTargetFolder != null) {
                        // Update in database
                        emailService.moveEmailToFolder(emailToMove.getId(), dbTargetFolder.getId());
                        
                        // Update in memory
                        sourceFolder.removeEmail(emailToMove);
                        emailToMove.setFolderId(dbTargetFolder.getId());
                        emailToMove.setFolder(targetFolder);
                        folders.get(targetFolder).addEmail(emailToMove);
                        
                        out.writeObject("MOVE_SUCCESS");
                    } else {
                        out.writeObject("MOVE_FAILED:Target folder not found in database");
                    }
                } else {
                    out.writeObject("MOVE_FAILED:Invalid email or folder");
                }
            } else {
                out.writeObject("MOVE_FAILED:User not found");
            }
        } catch (SQLException e) {
            out.writeObject("MOVE_FAILED:Database error");
            e.printStackTrace();
        }
        out.flush();
    }

    // deletes an email from a folder
    private void handleDeleteEmail(String emailData, ObjectOutputStream out) throws IOException {
        String[] parts = emailData.split(":");
        String userEmail = parts[1];
        int emailIndex = Integer.parseInt(parts[2]);

        try {
            Map<String, Folder> folders = userFolders.get(userEmail);
            if (folders != null) {
                Email emailToDelete = null;
                Folder sourceFolder = null;
                
                // Find email and its folder
                for (Folder folder : folders.values()) {
                    List<Email> emails = folder.getEmails();
                    if (emailIndex < emails.size()) {
                        emailToDelete = emails.get(emailIndex);
                        sourceFolder = folder;
                        break;
                    }
                }

                if (emailToDelete != null && sourceFolder != null) {
                    // Delete from database
                    emailService.deleteEmail(emailToDelete.getId());
                    
                    // Delete from memory
                    sourceFolder.removeEmail(emailToDelete);
                    
                    out.writeObject("DELETE_SUCCESS");
                } else {
                    out.writeObject("DELETE_FAILED:Invalid email index");
                }
            } else {
                out.writeObject("DELETE_FAILED:User not found");
            }
        } catch (SQLException e) {
            out.writeObject("DELETE_FAILED:Database error");
            e.printStackTrace();
        }
        out.flush();
    }

    // marks an email as read or unread
    private void handleMarkEmail(String emailData, boolean markAsRead, ObjectOutputStream out) throws IOException {
        String[] parts = emailData.split(":");
        String userEmail = parts[1];
        int emailIndex = Integer.parseInt(parts[2]);

        try {
            Map<String, Folder> folders = userFolders.get(userEmail);
            if (folders != null) {
                Email emailToMark = null;
                
                // Find email in any folder
                for (Folder folder : folders.values()) {
                    List<Email> emails = folder.getEmails();
                    if (emailIndex < emails.size()) {
                        emailToMark = emails.get(emailIndex);
                        break;
                    }
                }

                if (emailToMark != null) {
                    // Update in database
                    emailService.updateEmailReadStatus(emailToMark.getId(), markAsRead);
                    
                    // Update in memory
                    emailToMark.setRead(markAsRead);
                    
                    out.writeObject("MARK_SUCCESS");
                } else {
                    out.writeObject("MARK_FAILED:Invalid email index");
                }
            } else {
                out.writeObject("MARK_FAILED:User not found");
            }
        } catch (SQLException e) {
            out.writeObject("MARK_FAILED:Database error");
            e.printStackTrace();
        }
        out.flush();
    }

    // handles client connection to email server
    private void handleEmailConnection(String email, ObjectOutputStream out) throws IOException {
        try {
            if (userFolders.containsKey(email)) {
                clientOutputStreams.put(email, out);
                out.writeObject("CONNECT_SUCCESS");
                
                // Send existing emails to the client
                Map<String, Folder> folders = userFolders.get(email);
                for (Folder folder : folders.values()) {
                    for (Email emailObj : folder.getEmails()) {
                        out.writeObject(emailObj);
                    }
                }
                out.flush();
            } else {
                out.writeObject("CONNECT_FAILED:User not found");
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Error handling email connection: " + e.getMessage());
            e.printStackTrace();
            out.writeObject("CONNECT_FAILED:Server error");
            out.flush();
        }
    }

    // delivers an email to recipient's inbox
    public void deliverEmail(Email email) {
        System.out.println("\n=== Delivering Email ===");
        System.out.println("From: " + email.getFrom());
        System.out.println("To: " + email.getTo());
        System.out.println("Subject: " + email.getSubject());
        
        String recipient = email.getTo();
        Map<String, Folder> recipientFolders = userFolders.get(recipient);
        
        try {
            // Initialize recipient folders if they don't exist
            if (recipientFolders == null) {
                System.out.println("Initializing recipient folders");
                initializeUserFolders(recipient);
                recipientFolders = userFolders.get(recipient);
            }
            
            if (recipientFolders != null) {
                System.out.println("Found recipient folders");
                // Get inbox folder from database
                Folder inboxFolder = folderService.getFolderByNameAndUser("inbox", recipient);
                if (inboxFolder != null && inboxFolder.getId() > 0) {
                    System.out.println("Found inbox folder with ID: " + inboxFolder.getId());
                    // Save email to database
                    email.setFolderId(inboxFolder.getId());
                    int emailId = emailService.createEmail(email, inboxFolder.getId());
                    email.setId(emailId);  // Set the generated ID
                    System.out.println("Email saved to database with ID: " + emailId);
                    
                    // Add to in-memory inbox
                    Folder inbox = recipientFolders.get("inbox");
                    if (inbox != null) {
                        inbox.setId(inboxFolder.getId());  // Ensure memory folder has correct ID
                        inbox.addEmail(email);
                        System.out.println("Email added to in-memory inbox");
                        
                        // notify recipient if they are connected
                        ObjectOutputStream recipientOut = clientOutputStreams.get(recipient);
                        if (recipientOut != null) {
                            try {
                                recipientOut.writeObject("NEW_EMAIL");
                                recipientOut.writeObject(email);
                                recipientOut.flush();
                                System.out.println("Recipient notified of new email");
                            } catch (IOException e) {
                                System.err.println("Failed to notify recipient: " + e.getMessage());
                                e.printStackTrace();
                                clientOutputStreams.remove(recipient);
                            }
                        } else {
                            System.out.println("Recipient is not connected");
                        }
                    } else {
                        System.err.println("Inbox folder not found in memory");
                    }
                } else {
                    System.err.println("Inbox folder not found in database or has invalid ID");
                }
            } else {
                System.err.println("Failed to initialize recipient folders");
            }
        } catch (SQLException e) {
            System.err.println("Database error while delivering email: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== Email Delivery Complete ===\n");
    }

    // stops the server and cleans up resources
    public void stop() {
        if (!isServerMode) return;
        
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (executorService != null) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                }
            }
            for (ObjectOutputStream out : clientOutputStreams.values()) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            clientOutputStreams.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 