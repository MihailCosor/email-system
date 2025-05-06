import java.io.Serializable;
import java.time.LocalDateTime;

public class Email implements Serializable {
    private String from;
    private String to;
    private String subject;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;

    public Email(String from, String to, String subject, String content) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    // Getters
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }

    // Setters
    public void setRead(boolean read) { isRead = read; }
} 