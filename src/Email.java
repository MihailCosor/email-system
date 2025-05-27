import java.io.Serializable;
import java.time.LocalDateTime;

// represents an email message with read status and folder organization
public class Email extends Message implements Serializable {
    // unique identifier for the email
    private int id;
    // tracks whether the email has been read by the recipient
    private boolean read;
    // current folder id containing this email
    private int folderId;
    // current folder name containing this email (for backward compatibility)
    private String folder;

    // default constructor
    public Email() {
        super();
        this.id = -1;
        this.read = false;
        this.folderId = -1;
        this.folder = "inbox";
    }

    // creates a new email with default unread status and inbox folder
    public Email(String from, String to, String subject, String content) {
        super(from, to, subject, content);
        this.id = -1;
        this.read = false;
        this.folderId = -1;
        this.folder = "inbox";  // default folder for new emails
    }

    // retrieves the email id
    public int getId() {
        return id;
    }

    // sets the email id
    public void setId(int id) {
        this.id = id;
    }

    // retrieves the read status of the email
    public boolean isRead() { return read; }
    
    // gets the current folder id containing this email
    public int getFolderId() { return folderId; }
    
    // gets the current folder name containing this email
    public String getFolder() { return folder; }

    // updates the read status of the email
    public void setRead(boolean read) { this.read = read; }
    
    // sets the folder id for this email
    public void setFolderId(int folderId) { this.folderId = folderId; }
    
    // moves the email to a different folder (for backward compatibility)
    public void setFolder(String folder) { 
        this.folder = folder;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
} 