import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class EmailServer {
    private static EmailServer instance;
    private ServerSocket serverSocket;
    private final int PORT = 12345;
    private boolean running;
    private Map<String, Map<String, Folder>> userFolders; // email -> folder map
    private Map<String, ObjectOutputStream> clientOutputStreams;
    private ExecutorService executorService;
    private boolean isServerMode;
    private Map<Integer, User> users;
    private Map<String, Integer> emailToId;

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

    private void initializeDummyUsers() {
        User admin = new User("Admin", "admin@mihail.ro", "admin");
        User test = new User("Test", "test@mihail.ro", "test");
        
        users.put(admin.getId(), admin);
        users.put(test.getId(), test);
        
        emailToId.put(admin.getEmail(), admin.getId());
        emailToId.put(test.getEmail(), test.getId());

        initializeUserFolders(admin.getEmail());
        initializeUserFolders(test.getEmail());

        Folder adminInbox = userFolders.get(admin.getEmail()).get("inbox");
        adminInbox.addEmail(new Email("test@mihail.ro", "admin@mihail.ro", "Test Email", "This is a test email"));
        adminInbox.addEmail(new Email("test@mihail.ro", "admin@mihail.ro", "Test Email 2", "This is a test email 2"));
        
        users.get(admin.getId()).addContact("Test", "test@mihail.ro");
        users.get(test.getId()).addContact("Admin", "admin@mihail.ro");
    }

    private void initializeUserFolders(String email) {
        Map<String, Folder> folders = new HashMap<>();
        folders.put("inbox", new Folder("inbox", true));
        folders.put("spam", new Folder("spam", true));
        userFolders.put(email, folders);
    }

    public static EmailServer getInstance() {
        return getInstance(true);
    }

    public static EmailServer getInstance(boolean isServerMode) {
        if (instance == null) {
            instance = new EmailServer(isServerMode);
        }
        return instance;
    }

    public void start() {
        if (!isServerMode) return;
        
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Email server started on port " + PORT);

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
                                // just skip as we don't need to send anything back
                            } else {
                                handleCommand(command, out);
                            }
                        } else if (obj instanceof Email) {
                            Email email = (Email) obj;
                            deliverEmail(email);
                        }
                    } catch (EOFException | SocketException e) {
                        break;
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

    private void handleEmailConnection(String email, ObjectOutputStream out) throws IOException {
        Integer userId = emailToId.get(email);
        if (userId != null && users.containsKey(userId)) {
            out.writeObject("CONNECTED");
            out.flush();
            
            clientOutputStreams.put(email, out);
            
            if (!userFolders.containsKey(email)) {
                initializeUserFolders(email);
            }
            
            Map<String, Folder> folders = userFolders.get(email);
            for (Folder folder : folders.values()) {
                for (Email storedEmail : folder.getEmails()) {
                    out.writeObject(storedEmail);
                    out.flush();
                }
            }
        } else {
            out.writeObject("CONNECTION_FAILED:User not found");
            out.flush();
        }
    }

    public void deliverEmail(Email email) {
        if (!isServerMode) return;
        
        String recipientEmail = email.getTo();
        Map<String, Folder> recipientFolders = userFolders.get(recipientEmail);
        
        if (recipientFolders != null) {
            recipientFolders.get("inbox").addEmail(email);

            ObjectOutputStream recipientStream = clientOutputStreams.get(recipientEmail);
            if (recipientStream != null) {
                try {
                    recipientStream.writeObject(email);
                    recipientStream.flush();
                    System.out.println("Email delivered to " + recipientEmail);
                } catch (IOException e) {
                    System.out.println("Failed to deliver email to " + recipientEmail + ": " + e.getMessage());
                    clientOutputStreams.remove(recipientEmail);
                }
            } else {
                System.out.println("Email stored for offline user " + recipientEmail);
            }
            
            // Send success response back to sender
            ObjectOutputStream senderStream = clientOutputStreams.get(email.getFrom());
            if (senderStream != null) {
                try {
                    senderStream.writeObject("SEND_SUCCESS");
                    senderStream.flush();
                } catch (IOException e) {
                    System.out.println("Failed to send success response to " + email.getFrom());
                    clientOutputStreams.remove(email.getFrom());
                }
            }
        } else {
            // Send failure response back to sender
            ObjectOutputStream senderStream = clientOutputStreams.get(email.getFrom());
            if (senderStream != null) {
                try {
                    senderStream.writeObject("SEND_FAILED:Recipient not found");
                    senderStream.flush();
                } catch (IOException e) {
                    System.out.println("Failed to send failure response to " + email.getFrom());
                    clientOutputStreams.remove(email.getFrom());
                }
            }
        }
    }

    public void stop() {
        if (!isServerMode) return;
        
        running = false;
        for (ObjectOutputStream out : clientOutputStreams.values()) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        clientOutputStreams.clear();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        executorService.shutdownNow();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 