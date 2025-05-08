import java.io.*;
import java.net.*;
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
    // maps user ids to user objects
    private Map<Integer, User> users;
    // maps email addresses to user ids
    private Map<String, Integer> emailToId;

    // private constructor for singleton pattern
    private EmailServer(boolean isServerMode) {
        this.isServerMode = isServerMode;
        this.userFolders = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        this.emailToId = new ConcurrentHashMap<>();     
        if (isServerMode) {
            this.clientOutputStreams = new ConcurrentHashMap<>();
            this.executorService = Executors.newCachedThreadPool();
            initializeDummyUsers();
        }
    }

    // creates test users for development/testing
    private void initializeDummyUsers() {
        User admin = new User("Admin", "admin@mihail.ro", "admin");
        User test = new User("Test", "test@mihail.ro", "test");
        
        users.put(admin.getId(), admin);
        users.put(test.getId(), test);
        
        emailToId.put(admin.getEmail(), admin.getId());
        emailToId.put(test.getEmail(), test.getId());

        initializeUserFolders(admin.getEmail());
        initializeUserFolders(test.getEmail());

        // add test emails to admin's inbox
        Folder adminInbox = userFolders.get(admin.getEmail()).get("inbox");
        adminInbox.addEmail(new Email("test@mihail.ro", "admin@mihail.ro", "Test Email", "This is a test email"));
        adminInbox.addEmail(new Email("test@mihail.ro", "admin@mihail.ro", "Test Email 2", "This is a test email 2"));
        
        // set up test contacts
        users.get(admin.getId()).addContact("Test", "test@mihail.ro");
        users.get(test.getId()).addContact("Admin", "admin@mihail.ro");
    }

    // creates default folders for a new user
    private void initializeUserFolders(String email) {
        Map<String, Folder> folders = new HashMap<>();
        folders.put("inbox", new Folder("inbox", true));
        folders.put("spam", new Folder("spam", true));
        userFolders.put(email, folders);
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
                                // skip acknowledgment for email sending
                            } else {
                                handleCommand(command, out);
                            }
                        } else if (obj instanceof Email) {
                            Email email = (Email) obj;
                            deliverEmail(email);
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
        String email = parts[0];
        String password = parts[1];

        Integer userId = emailToId.get(email);
        if (userId != null) {
            User user = users.get(userId);
            if (user != null && user.getPassword().equals(password)) {
                out.writeObject("LOGIN_SUCCESS");
                out.writeObject(user);
                out.flush();
            } else {
                out.writeObject("LOGIN_FAILED:Invalid email or password");
                out.flush();
            }
        } else {
            out.writeObject("LOGIN_FAILED:User not found");
            out.flush();
        }
    }

    // registers a new user
    private void handleRegister(String userData, ObjectInputStream in, ObjectOutputStream out) throws IOException {
        String[] parts = userData.split(":");
        String name = parts[0];
        String email = parts[1];
        String password = parts[2];

        if (emailToId.containsKey(email)) {
            out.writeObject("REGISTER_FAILED:Email already exists");
            out.flush();
            return;
        }

        User newUser = new User(name, email, password);
        users.put(newUser.getId(), newUser);
        emailToId.put(email, newUser.getId());
        initializeUserFolders(email);
        out.writeObject("REGISTER_SUCCESS");
        out.flush();
    }

    // moves an email between folders
    private void handleMoveEmail(String emailData, ObjectOutputStream out) throws IOException {
        String[] parts = emailData.split(":");
        String userEmail = parts[1];
        int emailIndex = Integer.parseInt(parts[2]);
        String targetFolder = parts[3];

        Map<String, Folder> folders = userFolders.get(userEmail);
        if (folders != null) {
            Email emailToMove = null;
            Folder sourceFolder = null;
            for (Folder folder : folders.values()) {
                List<Email> emails = folder.getEmails();
                if (emailIndex < emails.size()) {
                    emailToMove = emails.get(emailIndex);
                    sourceFolder = folder;
                    break;
                }
            }

            if (emailToMove != null && sourceFolder != null && folders.containsKey(targetFolder)) {
                sourceFolder.removeEmail(emailToMove);
                folders.get(targetFolder).addEmail(emailToMove);
                out.writeObject("MOVE_SUCCESS");
            } else {
                out.writeObject("MOVE_FAILED:Invalid email or folder");
            }
        } else {
            out.writeObject("MOVE_FAILED:User not found");
        }
        out.flush();
    }

    // deletes an email from a folder
    private void handleDeleteEmail(String emailData, ObjectOutputStream out) throws IOException {
        String[] parts = emailData.split(":");
        String userEmail = parts[1];
        int emailIndex = Integer.parseInt(parts[2]);

        Map<String, Folder> folders = userFolders.get(userEmail);
        if (folders != null) {
            for (Folder folder : folders.values()) {
                List<Email> emails = folder.getEmails();
                if (emailIndex < emails.size()) {
                    folder.removeEmail(emails.get(emailIndex));
                    out.writeObject("DELETE_SUCCESS");
                    out.flush();
                    return;
                }
            }
        }
        out.writeObject("DELETE_FAILED:Invalid email index");
        out.flush();
    }

    // marks an email as read or unread
    private void handleMarkEmail(String emailData, boolean markAsRead, ObjectOutputStream out) throws IOException {
        String[] parts = emailData.split(":");
        String userEmail = parts[1];
        int emailIndex = Integer.parseInt(parts[2]);

        Map<String, Folder> folders = userFolders.get(userEmail);
        if (folders != null) {
            for (Folder folder : folders.values()) {
                List<Email> emails = folder.getEmails();
                if (emailIndex < emails.size()) {
                    emails.get(emailIndex).setRead(markAsRead);
                    out.writeObject("MARK_SUCCESS");
                    out.flush();
                    return;
                }
            }
        }
        out.writeObject("MARK_FAILED:Invalid email index");
        out.flush();
    }

    // handles client connection to email server
    private void handleEmailConnection(String email, ObjectOutputStream out) throws IOException {
        if (userFolders.containsKey(email)) {
            clientOutputStreams.put(email, out);
            out.writeObject("CONNECT_SUCCESS");
        } else {
            out.writeObject("CONNECT_FAILED:User not found");
        }
        out.flush();
    }

    // delivers an email to recipient's inbox
    public void deliverEmail(Email email) {
        String recipient = email.getTo();
        Map<String, Folder> recipientFolders = userFolders.get(recipient);
        
        if (recipientFolders != null) {
            Folder inbox = recipientFolders.get("inbox");
            if (inbox != null) {
                inbox.addEmail(email);
                
                // notify recipient if they are connected
                ObjectOutputStream recipientOut = clientOutputStreams.get(recipient);
                if (recipientOut != null) {
                    try {
                        recipientOut.writeObject("NEW_EMAIL");
                        recipientOut.writeObject(email);
                        recipientOut.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        clientOutputStreams.remove(recipient);
                    }
                }
            }
        }
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