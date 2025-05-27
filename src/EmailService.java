import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class EmailService extends GenericDatabaseService<Email> {
    private static EmailService instance;
    private static final String TABLE_NAME = "emails";

    private EmailService() {
        super();
    }

    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    public int createEmail(Email email, int folderId) throws SQLException {
        System.out.println("Creating email in database:");
        System.out.println("From: " + email.getFrom());
        System.out.println("To: " + email.getTo());
        System.out.println("Subject: " + email.getSubject());
        System.out.println("Folder ID: " + folderId);
        
        String[] columns = {"sender", "recipient", "subject", "content", "folder_id"};
        Object[] values = {
            email.getFrom(),
            email.getTo(),
            email.getSubject(),
            email.getContent(),
            folderId
        };
        int id = create(TABLE_NAME, columns, values);
        System.out.println("Created email with ID: " + id);
        return id;
    }

    public Email getEmailById(int emailId) throws SQLException {
        List<Email> emails = read(TABLE_NAME, "id = ?", new Object[]{emailId}, this::mapResultSet);
        return emails.isEmpty() ? null : emails.get(0);
    }

    public List<Email> getEmailsByFolder(int folderId) throws SQLException {
        return read(TABLE_NAME, "folder_id = ?", new Object[]{folderId}, this::mapResultSet);
    }

    public List<Email> getEmailsByRecipient(String recipient) throws SQLException {
        return read(TABLE_NAME, "recipient = ?", new Object[]{recipient}, this::mapResultSet);
    }

    public List<Email> getEmailsBySender(String sender) throws SQLException {
        return read(TABLE_NAME, "sender = ?", new Object[]{sender}, this::mapResultSet);
    }

    public void updateEmailReadStatus(int emailId, boolean isRead) throws SQLException {
        String[] columns = {"is_read"};
        Object[] values = {isRead};
        update(TABLE_NAME, columns, values, "id = ?", new Object[]{emailId});
    }

    public void moveEmailToFolder(int emailId, int folderId) throws SQLException {
        String[] columns = {"folder_id"};
        Object[] values = {folderId};
        update(TABLE_NAME, columns, values, "id = ?", new Object[]{emailId});
    }

    public void deleteEmail(int emailId) throws SQLException {
        delete(TABLE_NAME, "id = ?", new Object[]{emailId});
    }

    @Override
    protected Email mapResultSet(ResultSet rs) throws SQLException {
        Email email = new Email(
            rs.getString("sender"),
            rs.getString("recipient"),
            rs.getString("subject"),
            rs.getString("content")
        );
        email.setId(rs.getInt("id"));
        email.setRead(rs.getBoolean("is_read"));
        email.setFolderId(rs.getInt("folder_id"));
        
        // Handle SQLite timestamp format
        String timestampStr = rs.getString("timestamp");
        if (timestampStr != null) {
            try {
                // Parse SQLite's timestamp format (YYYY-MM-DD HH:MM:SS)
                LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"));
                email.setTimestamp(timestamp);
            } catch (Exception e) {
                System.err.println("Error parsing timestamp: " + timestampStr);
                email.setTimestamp(LocalDateTime.now());  // Fallback to current time
            }
        }
        return email;
    }
}