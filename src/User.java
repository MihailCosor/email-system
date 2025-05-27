import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.sql.SQLException;

// represents a user of the email system with authentication and contact management
public class User extends Person implements Serializable {
    // hashed password for authentication
    private String password;
    // timestamp of the user's most recent login
    private LocalDateTime lastLogin;
    // non-serializable email client instance for server communication
    private transient EmailClient emailClient;
    private transient ContactService contactService;
    // set of user's contacts for quick lookup
    private Set<Contact> contacts;

    // default constructor
    public User() {
        super();
        this.lastLogin = LocalDateTime.now();
        this.contacts = new HashSet<>();
        initializeServices();
    }

    // creates a new user with basic information and validates email format
    public User(String name, String email, String password) {
        super(name, email);
        this.password = password;
        this.lastLogin = LocalDateTime.now();
        this.contacts = new HashSet<>();
        initializeServices();

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format. Email must end with '@mihail.ro' or '@example.com'");
        }
    }

    // initializes services
    private void initializeServices() {
        this.emailClient = new EmailClient();
        this.contactService = ContactService.getInstance();
    }

    // custom deserialization to reinitialize transient fields
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        initializeServices();
    }

    // basic getters
    public String getPassword() { return password; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public EmailClient getEmailClient() { return emailClient; }

    // basic setters
    public void setPassword(String password) { this.password = password; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    // updates the last login time to current time
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    // validates email domain against allowed list
    private boolean isValidEmail(String email) {
        return email != null && (email.endsWith("@mihail.ro") || email.endsWith("@example.com"));
    }

    // email server connection management
    public void connectToEmailServer() {
        emailClient.connect(email);
    }

    public void disconnectFromEmailServer() {
        emailClient.disconnect();
    }

    // email operations
    public void sendEmail(String to, String subject, String content) {
        emailClient.sendEmail(to, subject, content);
    }

    public List<Email> getInbox() {
        return emailClient.getInbox();
    }

    // contact management operations
    public boolean addContact(String name, String email) {
        try {
            Contact contact = new Contact(name, email);
            contactService.createContact(contact);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to add contact: " + e.getMessage());
            return false;
        }
    }

    public boolean removeContact(String email) {
        try {
            Contact contact = contactService.getContactByEmail(email);
            if (contact != null) {
                contactService.deleteContact(contact.getId());
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Failed to remove contact: " + e.getMessage());
            return false;
        }
    }

    public boolean removeContact(Contact contact) {
        try {
            contactService.deleteContact(contact.getId());
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to remove contact: " + e.getMessage());
            return false;
        }
    }

    // returns a defensive copy of contacts set
    public Set<Contact> getContacts() {
        return new HashSet<>(contacts);
    }

    // returns contacts as a list for ordered display
    public List<Contact> getContactsList() {
        try {
            return contactService.getAllContacts();
        } catch (SQLException e) {
            System.err.println("Failed to get contacts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // finds a contact by email address
    public Contact findContact(String email) {
        try {
            return contactService.getContactByEmail(email);
        } catch (SQLException e) {
            System.err.println("Failed to find contact: " + e.getMessage());
            return null;
        }
    }
}
