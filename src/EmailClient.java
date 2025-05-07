import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class EmailClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String userEmail;
    private boolean connected;
    private Map<String, Folder> folders;
    private Thread inboxListener;
    private BlockingQueue<Object> responseQueue;

    public EmailClient() {
        this.connected = false;
        this.responseQueue = new LinkedBlockingQueue<>();
        this.folders = new HashMap<>();
        initializeFolders();
    }

    private void initializeFolders() {
        folders.put("inbox", new Folder("inbox", true));
        folders.put("spam", new Folder("spam", true));
    }

    public boolean connect(String email) {
        try {
            socket = new Socket("localhost", 12345);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Clear folders when connecting
            initializeFolders();
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
                        // Add email to appropriate folder
                        Folder folder = folders.get(email.getFolder());
                        if (folder != null) {
                            folder.addEmail(email);
                        } else {
                            // If folder doesn't exist, add to inbox by default
                            folders.get("inbox").addEmail(email);
                        }
                    } else if (obj instanceof String) {
                        responseQueue.offer(obj);
                    }
                } catch (EOFException | SocketException e) {
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

    public List<Email> getInbox() {
        return folders.get("inbox").getEmails();
    }

    public List<Email> getSpam() {
        return folders.get("spam").getEmails();
    }

    public List<Email> getEmailsByFolder(String folderName) {
        Folder folder = folders.get(folderName);
        return folder != null ? folder.getEmails() : new ArrayList<>();
    }

    public Set<String> getFolders() {
        return new HashSet<>(folders.keySet());
    }

    public void moveEmailToFolder(int emailIndex, String targetFolder) {
        if (!folders.containsKey(targetFolder)) {
            System.out.println("Target folder does not exist.");
            return;
        }

        try {
            out.writeObject("MOVE_EMAIL:" + userEmail + ":" + emailIndex + ":" + targetFolder);
            String response = waitForResponse();
            if (response.equals("MOVE_SUCCESS")) {
                // Find email in current folder and move it
                for (Folder folder : folders.values()) {
                    List<Email> emails = folder.getEmails();
                    if (emailIndex < emails.size()) {
                        Email email = emails.get(emailIndex);
                        folder.removeEmail(email);
                        folders.get(targetFolder).addEmail(email);
                        break;
                    }
                }
            } else {
                System.out.println("Failed to move email: " + response);
            }
        } catch (Exception e) {
            System.out.println("Error moving email: " + e.getMessage());
        }
    }

    public void deleteEmail(int emailIndex) {
        try {
            out.writeObject("DELETE_EMAIL:" + userEmail + ":" + emailIndex);
            String response = waitForResponse();
            if (response.equals("DELETE_SUCCESS")) {
                // Remove email from its folder
                for (Folder folder : folders.values()) {
                    List<Email> emails = folder.getEmails();
                    if (emailIndex < emails.size()) {
                        folder.removeEmail(emails.get(emailIndex));
                        break;
                    }
                }
            } else {
                System.out.println("Failed to delete email: " + response);
            }
        } catch (Exception e) {
            System.out.println("Error deleting email: " + e.getMessage());
        }
    }

    public void markEmailAsRead(int emailIndex) {
        try {
            out.writeObject("MARK_READ:" + userEmail + ":" + emailIndex);
            String response = waitForResponse();
            if (response.equals("MARK_SUCCESS")) {
                updateEmailReadStatus(emailIndex, true);
            } else {
                System.out.println("Failed to mark email as read: " + response);
            }
        } catch (Exception e) {
            System.out.println("Error marking email as read: " + e.getMessage());
        }
    }

    public void markEmailAsUnread(int emailIndex) {
        try {
            out.writeObject("MARK_UNREAD:" + userEmail + ":" + emailIndex);
            String response = waitForResponse();
            if (response.equals("MARK_SUCCESS")) {
                updateEmailReadStatus(emailIndex, false);
            } else {
                System.out.println("Failed to mark email as unread: " + response);
            }
        } catch (Exception e) {
            System.out.println("Error marking email as unread: " + e.getMessage());
        }
    }

    private void updateEmailReadStatus(int emailIndex, boolean isRead) {
        for (Folder folder : folders.values()) {
            List<Email> emails = folder.getEmails();
            if (emailIndex < emails.size()) {
                emails.get(emailIndex).setRead(isRead);
                break;
            }
        }
    }

    private String waitForResponse() throws InterruptedException {
        Object response = responseQueue.poll(5, TimeUnit.SECONDS);
        if (response == null) {
            throw new InterruptedException("Timeout waiting for response");
        }
        return (String) response;
    }

    public boolean sendEmail(String to, String subject, String content) {
        try {
            Email email = new Email(userEmail, to, subject, content);
            out.writeObject("SEND_EMAIL:" + userEmail);
            out.writeObject(email);
            out.flush();
            
            String response = waitForResponse();
            if (!response.equals("SEND_SUCCESS")) {
                System.out.println("Failed to send email: " + response);
                return false;
            }
            return true;
        } catch (Exception e) {
            System.out.println("Error sending email: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (inboxListener != null) {
                inboxListener.interrupt();
            }
            // if (out != null) out.close();
            // if (in != null) in.close();
            // if (socket != null) socket.close();
        // } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 