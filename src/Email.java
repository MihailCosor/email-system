import java.io.Serializable;

public class Email extends Message {
    private boolean read;
    private String folder;

    public Email(String from, String to, String subject, String content) {
        super(from, to, subject, content);
        this.read = false;
        this.folder = "inbox"; // Default folder
    }

    // Getters
    public boolean isRead() { return read; }
    public String getFolder() { return folder; }

    // Setters
    public void setRead(boolean read) { this.read = read; }
    public void setFolder(String folder) { this.folder = folder; }
} 