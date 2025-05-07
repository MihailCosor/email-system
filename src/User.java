import java.util.List;

public class User {
    private String name;
    private final String email;
    private String password;
    private EmailClient emailClient;

    public User(String name, String email, String password) {
        this.name = name;
        this.password = password;

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format. Email must end with '@mihail.ro' or '@example.com'");
        }
        this.email = email;
        this.emailClient = new EmailClient();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private boolean isValidEmail(String email) {
        return email.endsWith("@mihail.ro") || email.endsWith("@example.com");
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
