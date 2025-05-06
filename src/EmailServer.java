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

    private EmailServer(boolean isServerMode) {
        this.isServerMode = isServerMode;
        this.mailboxes = new ConcurrentHashMap<>();
        if (isServerMode) {
            this.clientOutputStreams = new ConcurrentHashMap<>();
            this.executorService = Executors.newCachedThreadPool();
        }
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
                // First create output stream
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush(); // Important: flush the header
                // Then create input stream
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                while (running) {
                    try {
                        Object obj = in.readObject();
                        if (obj instanceof String) {
                            String email = (String) obj;
                            clientOutputStreams.put(email, out);
                            // Send confirmation
                            out.writeObject("CONNECTED");
                            out.flush();
                        } else if (obj instanceof Email) {
                            Email email = (Email) obj;
                            deliverEmail(email);
                        }
                    } catch (EOFException | SocketException e) {
                        // Client disconnected
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
            System.out.println("Recipient " + email.getTo() + " is not currently connected");
        }
    }

    public List<Email> getInbox(String userEmail) {
        // For client mode, return an empty list since emails are handled by EmailClient
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