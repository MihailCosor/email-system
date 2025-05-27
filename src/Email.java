import java.io.Serializable;
import java.time.LocalDateTime;

// represents an email message with read status and folder organization
public class Email extends Message implements Serializable {
    // tracks whether the email has been read by the recipient
    private boolean read;
    // current folder location of the email (inbox, sent, trash, etc.)
    private String folder;

    // default constructor
    public Email() {
        super();
        this.read = false;
        this.folder = "inbox";
    }

    // creates a new email with default unread status and inbox folder
    public Email(String from, String to, String subject, String content) {
        super(from, to, subject, content);
        this.read = false;
        this.folder = "inbox";  // default folder for new emails
    }

    // retrieves the read status of the email
    public boolean isRead() { return read; }
    // gets the current folder containing this email
    public String getFolder() { return folder; }

    // updates the read status of the email
    public void setRead(boolean read) { this.read = read; }
    // moves the email to a different folder
    public void setFolder(String folder) { this.folder = folder; }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp.setDateTime(timestamp);
    }
} 