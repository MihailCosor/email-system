import java.io.*;
import java.net.*;
import java.util.*;

public class EmailClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String userEmail;
    private boolean connected;
    private List<Email> inbox;
    private Thread inboxListener;

    public EmailClient() {
        this.inbox = new ArrayList<>();
        this.connected = false;
    }

    public boolean connect(String email) {
        try {
            socket = new Socket("localhost", 12345);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Clear inbox when connecting
            inbox.clear();
            this.userEmail = email;

            // Send email to identify this client
            out.writeObject(email);
            out.flush();

            // Wait for connection confirmation
            Object response = in.readObject();
            if (response instanceof String) {
                String responseStr = (String) response;
                if (responseStr.equals("CONNECTED")) {
                    this.connected = true;
                    startInboxListener();
                    return true;
                } else if (responseStr.startsWith("CONNECTION_FAILED:")) {
                    System.out.println(responseStr.substring("CONNECTION_FAILED:".length()));
                    disconnect();
                    return false;
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to connect: " + e.getMessage());
            disconnect();
        }
        return false;
    }

    private void startInboxListener() {
        inboxListener = new Thread(() -> {
            while (connected) {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof Email) {
                        Email email = (Email) obj;
                        inbox.add(email);
                        // System.out.println("\nNew email received from " + email.getFrom());
                    }
                } catch (EOFException | SocketException e) {
                    // Server disconnected
                    break;
                } catch (Exception e) {
                    if (connected) {
                        e.printStackTrace();
                    }
                }
            }
        });
        inboxListener.start();
    }

    public boolean sendEmail(String to, String subject, String content) {
        if (!connected) return false;

        try {
            Email email = new Email(userEmail, to, subject, content);
            out.writeObject(email);
            out.flush();
            return true;
        } catch (IOException e) {
            System.out.println("Failed to send email: " + e.getMessage());
            return false;
        }
    }

    public List<Email> getInbox() {
        return new ArrayList<>(inbox);
    }

    public void disconnect() {
        connected = false;
        try {
            if (inboxListener != null) {
                inboxListener.interrupt();
            }
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 