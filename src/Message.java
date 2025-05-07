import java.io.Serializable;

public abstract class Message implements Serializable {
    protected String from;
    protected String to;
    protected String subject;
    protected String content;
    protected DateTime timestamp;

    public Message(String from, String to, String subject, String content) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.timestamp = DateTime.now();
    }

    // Getters
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public DateTime getTimestamp() { return timestamp; }

    // Setters
    public void setContent(String content) { this.content = content; }
} 