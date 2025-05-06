import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;

public class EmailClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final String userEmail;
    private List<Email> inbox;
    private boolean connected;
    private Thread listenerThread;

    public EmailClient(String userEmail) {
        this.userEmail = userEmail;
        this.inbox = new ArrayList<>();
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 12345);
            
            // First create output stream
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush(); // Important: flush the header
            
            // Then create input stream
            in = new ObjectInputStream(socket.getInputStream());
            
            // Send user email to server for identification
            out.writeObject(userEmail);
            out.flush();

            // Wait for connection confirmation
            Object response = in.readObject();
            if ("CONNECTED".equals(response)) {
                connected = true;
                System.out.println("Connected to email server successfully!");
                
                // Start listening for incoming emails
                listenerThread = new Thread(this::listenForEmails);
                listenerThread.start();
            } else {
                System.out.println("Failed to connect to server");
                disconnect();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
            disconnect();
        }
    }

    private void listenForEmails() {
        while (connected && !socket.isClosed()) {
            try {
                Object obj = in.readObject();
                if (obj instanceof Email) {
                    Email email = (Email) obj;
                    synchronized(inbox) {
                        inbox.add(email);
                    }
                }
            } catch (EOFException | SocketException e) {
                // Server disconnected
                System.out.println("Lost connection to server");
                break;
            } catch (Exception e) {
                if (connected) {
                    System.out.println("Error receiving email: " + e.getMessage());
                }
                break;
            }
        }
        disconnect();
    }

    public void sendEmail(Email email) {
        if (!connected || socket.isClosed()) {
            System.out.println("Not connected to server");
            return;
        }
        
        try {
            out.writeObject(email);
            out.flush();
            System.out.println("Email sent successfully!");
        } catch (IOException e) {
            System.out.println("Error sending email: " + e.getMessage());
            disconnect();
        }
    }

    public List<Email> getInbox() {
        synchronized(inbox) {
            return new ArrayList<>(inbox);
        }
    }

    public void markAsRead(Email email) {
        email.setRead(true);
    }

    public void disconnect() {
        connected = false;
        
        // Stop the listener thread
        if (listenerThread != null) {
            listenerThread.interrupt();
            try {
                listenerThread.join(1000); // Wait up to 1 second for thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Close streams
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error during disconnect: " + e.getMessage());
        }
        
        out = null;
        in = null;
        socket = null;
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
} 