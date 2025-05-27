// base class for all message types in the system, providing common messaging functionality
public abstract class Message {
    // sender's email address or identifier
    protected String from;
    // recipient's email address or identifier
    protected String to;
    // brief description or title of the message
    protected String subject;
    // main body text of the message
    protected String content;
    // timestamp when the message was created
    protected DateTime timestamp;

    // default constructor
    public Message() {
        this.from = "";
        this.to = "";
        this.subject = "";
        this.content = "";
    }

    // creates a new message with sender, recipient, subject, and content
    public Message(String from, String to, String subject, String content) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.timestamp = DateTime.now();
    }

    // retrieves the sender's address
    public String getFrom() { return from; }
    // retrieves the recipient's address
    public String getTo() { return to; }
    // retrieves the message subject
    public String getSubject() { return subject; }
    // retrieves the message content
    public String getContent() { return content; }
    // retrieves the message creation timestamp
    public DateTime getTimestamp() { return timestamp; }

    // updates the message content
    public void setContent(String content) { this.content = content; }
} 