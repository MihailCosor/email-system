import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class User extends Person {
    private String password;
    private DateTime lastLogin;
    private transient EmailClient emailClient;
    private Set<Contact> contacts;

    public User(String name, String email, String password) {
        super(name, email);
        this.password = password;
        this.lastLogin = DateTime.now();
        this.contacts = new HashSet<>();
        initializeEmailClient();

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format. Email must end with '@mihail.ro' or '@example.com'");
        }
    }

    private void initializeEmailClient() {
        this.emailClient = new EmailClient();
    }

    // Called after deserialization
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        initializeEmailClient();
    }

    public String getPassword() { return password; }
    public DateTime getLastLogin() { return lastLogin; }
    public EmailClient getEmailClient() { return emailClient; }

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

    // Contacts management
    public boolean addContact(String name, String email) {
        return contacts.add(new Contact(name, email));
    }

    public boolean removeContact(String email) {
        return contacts.removeIf(contact -> contact.getEmail().equals(email));
    }

    public boolean removeContact(Contact contact) {
        return contacts.remove(contact);
    }

    public Set<Contact> getContacts() {
        return new HashSet<>(contacts);
    }

    public List<Contact> getContactsList() {
        return new ArrayList<>(contacts);
    }

    public Contact findContact(String email) {
        return contacts.stream()
                .filter(contact -> contact.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
}
