import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class EmailServer {
    private static EmailServer instance;
    private ServerSocket serverSocket;
    private final int PORT = 12345;
    private boolean running;
    private Map<String, List<Email>> mailboxes;
    private Map<String, ObjectOutputStream> clientOutputStreams;
    private ExecutorService executorService;
    private boolean isServerMode;
    private Map<Integer, User> users;
    private Map<String, Integer> emailToId;

    private EmailServer(boolean isServerMode) {
        this.isServerMode = isServerMode;
        this.mailboxes = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        this.emailToId = new ConcurrentHashMap<>();     
        if (isServerMode) {
            this.clientOutputStreams = new ConcurrentHashMap<>();
            this.executorService = Executors.newCachedThreadPool();
            initializeDummyUsers();
        }
    }

    private void initializeDummyUsers() {
        // Add some dummy users for mails admin@mihail.ro and test@mihail.ro
        User admin = new User("Admin", "admin@mihail.ro", "admin");
        User test = new User("Test", "test@mihail.ro", "test");
        
        // Store users by ID
        users.put(admin.getId(), admin);
        users.put(test.getId(), test);
        
        // Create email to ID mapping
        emailToId.put(admin.getEmail(), admin.getId());
        emailToId.put(test.getEmail(), test.getId());
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

            // Accept client connections in a separate thread
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
                            } else {
                                // Handle email connection
                                String email = command;
                                Integer userId = emailToId.get(email);
                                if (userId != null && users.containsKey(userId)) {
                                    // First send connection confirmation
                                    out.writeObject("CONNECTED");
                                    out.flush();
                                    
                                    // Then store the client's output stream
                                    clientOutputStreams.put(email, out);
                                    
                                    // Finally send any stored emails
                                    List<Email> storedEmails = mailboxes.getOrDefault(email, new ArrayList<>());
                                    for (Email storedEmail : storedEmails) {
                                        out.writeObject(storedEmail);
                                        out.flush();
                                    }
                                } else {
                                    out.writeObject("CONNECTION_FAILED:User not found");
                                    out.flush();
                                }
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

    private void handleLogin(String credentials, ObjectInputStream in, ObjectOutputStream out) throws IOException {
        String[] parts = credentials.split(":");
        String email = parts[0];
        String password = parts[1];

        Integer userId = emailToId.get(email);
        if (userId == null) {
            out.writeObject("LOGIN_FAILED:Invalid email or password");
            out.flush();
            return;
        }

        User user = users.get(userId);
        if (user != null && user.getPassword().equals(password)) {
            out.writeObject("LOGIN_SUCCESS:" + user.getName());
            out.flush();
        } else {
            out.writeObject("LOGIN_FAILED:Invalid email or password");
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
        out.writeObject("REGISTER_SUCCESS");
        out.flush();
    }

    public void deliverEmail(Email email) {
        if (!isServerMode) return;
        
        // Store email in recipient's mailbox
        mailboxes.computeIfAbsent(email.getTo(), k -> new ArrayList<>()).add(email);

        // If recipient is connected, notify them
        ObjectOutputStream recipientStream = clientOutputStreams.get(email.getTo());
        if (recipientStream != null) {
            try {
                recipientStream.writeObject(email);
                recipientStream.flush();
                System.out.println("Email delivered to " + email.getTo());
            } catch (IOException e) {
                System.out.println("Failed to deliver email to " + email.getTo() + ": " + e.getMessage());
                // Remove disconnected user
                clientOutputStreams.remove(email.getTo());
            }
        } else {
            System.out.println("Email stored for offline user " + email.getTo());
        }
    }

    public List<Email> getInbox(String userEmail) {
        if (!isServerMode) {
            return new ArrayList<>();
        }
        return mailboxes.getOrDefault(userEmail, new ArrayList<>());
    }

    public void stop() {
        if (!isServerMode) return;
        
        running = false;
        // Close all client streams
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