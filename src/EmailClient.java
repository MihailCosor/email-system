import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class EmailClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String userEmail;
    private boolean connected;
    private List<Email> inbox;
    private Thread inboxListener;
    private BlockingQueue<Object> responseQueue;

    public EmailClient() {
        this.inbox = new ArrayList<>();
        this.connected = false;
        this.responseQueue = new LinkedBlockingQueue<>();
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
                    } else if (obj instanceof String) {
                        responseQueue.offer(obj);
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

    private String waitForResponse() throws InterruptedException {
        Object response = responseQueue.poll(5, TimeUnit.SECONDS);
        if (response == null) {
            throw new InterruptedException("Timeout waiting for response");
        }
        return (String) response;
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

    public void deleteEmail(int emailIndex) {
        try {
            out.writeObject("DELETE_EMAIL:" + userEmail + ":" + emailIndex);
            String response = waitForResponse();
            if (!response.equals("DELETE_SUCCESS")) {
                System.out.println("Failed to delete email: " + response);
            }

            // Update local inbox
            inbox.remove(emailIndex);
        } catch (Exception e) {
            System.out.println("Error deleting email: " + e.getMessage());
        }
    }

    public void markEmailAsRead(int emailIndex) {
        try {
            out.writeObject("MARK_READ:" + userEmail + ":" + emailIndex);
            String response = waitForResponse();
            if (!response.equals("MARK_SUCCESS")) {
                System.out.println("Failed to mark email as read: " + response);
            }

            // Update local inbox
            inbox.get(emailIndex).setRead(true);
        } catch (Exception e) {
            System.out.println("Error marking email as read: " + e.getMessage());
        }
    }

    public void markEmailAsUnread(int emailIndex) {
        try {
            out.writeObject("MARK_UNREAD:" + userEmail + ":" + emailIndex);
            String response = waitForResponse();
            if (!response.equals("MARK_SUCCESS")) {
                System.out.println("Failed to mark email as unread: " + response);
            }

            // Update local inbox
            inbox.get(emailIndex).setRead(false);
        } catch (Exception e) {
            System.out.println("Error marking email as unread: " + e.getMessage());
        }
    }
} 