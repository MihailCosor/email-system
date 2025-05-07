import java.io.Serializable;
import java.util.List;
public class User implements Serializable {
    private static int nextId = 1;
    private int id;
    private String name;
    private String email;
    private String password;
    private DateTime lastLogin;
    private EmailClient emailClient;

    public User(String name, String email, String password) {
        this.id = nextId++;
        this.name = name;
        this.password = password;
        this.lastLogin = DateTime.now();
        this.emailClient = new EmailClient();

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format. Email must end with '@mihail.ro' or '@example.com'");
        }
        this.email = email;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public DateTime getLastLogin() { return lastLogin; }
    public EmailClient getEmailClient() { return emailClient; }

    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
    public void setLastLogin(DateTime lastLogin) { this.lastLogin = lastLogin; }

    public void updateLastLogin() {
        this.lastLogin = DateTime.now();
    }

    private boolean isValidEmail(String email) {
        return email != null && (email.endsWith("@mihail.ro") || email.endsWith("@example.com"));
    }

    public void connectToEmailServer() {
        emailClient.connect(email);
    }

    public void disconnectFromEmailServer() {
        emailClient.disconnect();
    }

    public void sendEmail(String to, String subject, String content) {
        emailClient.sendEmail(to, subject, content);
    }

    public List<Email> getInbox() {
        return emailClient.getInbox();
    }
}
