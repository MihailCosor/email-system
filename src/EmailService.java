import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class EmailService {
    private static EmailService instance;
    private final GenericDatabaseService<Email> dbService;
    private static final String TABLE_NAME = "emails";

    private EmailService() {
        this.dbService = GenericDatabaseService.getInstance();
    }

    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    public void createEmail(Email email) throws SQLException {
        String[] columns = {"sender", "recipient", "subject", "content", "timestamp", "is_read", "folder"};
        Object[] values = {
                email.getFrom(),
                email.getTo(),
                email.getSubject(),
                email.getContent(),
                email.getTimestamp(),
                email.isRead(),
                email.getFolder()
        };
        dbService.create(TABLE_NAME, columns, values);
    }

    public List<Email> getEmailsByRecipient(String recipient) throws SQLException {
        return dbService.read(
                TABLE_NAME,
                "recipient = ?",
                new Object[]{recipient},
                rs -> mapResultSetToEmail(rs)
        );
    }

    public List<Email> getEmailsBySender(String sender) throws SQLException {
        return dbService.read(
                TABLE_NAME,
                "sender = ?",
                new Object[]{sender},
                rs -> mapResultSetToEmail(rs)
        );
    }

    public void updateEmailReadStatus(String sender, String recipient, String subject, boolean isRead) throws SQLException {
        String[] columns = {"is_read"};
        Object[] values = {isRead};
        dbService.update(
                TABLE_NAME,
                columns,
                values,
                "sender = ? AND recipient = ? AND subject = ?",
                new Object[]{sender, recipient, subject}
        );
    }

    public void moveEmailToFolder(String sender, String recipient, String subject, String folder) throws SQLException {
        String[] columns = {"folder"};
        Object[] values = {folder};
        dbService.update(
                TABLE_NAME,
                columns,
                values,
                "sender = ? AND recipient = ? AND subject = ?",
                new Object[]{sender, recipient, subject}
        );
    }

    public void deleteEmail(String sender, String recipient, String subject) throws SQLException {
        dbService.delete(
                TABLE_NAME,
                "sender = ? AND recipient = ? AND subject = ?",
                new Object[]{sender, recipient, subject}
        );
    }

    private Email mapResultSetToEmail(ResultSet rs) throws SQLException {
        Email email = new Email(
                rs.getString("sender"),
                rs.getString("recipient"),
                rs.getString("subject"),
                rs.getString("content")
        );
        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            email.setTimestamp(timestamp.toLocalDateTime());
        }
        email.setRead(rs.getBoolean("is_read"));
        email.setFolder(rs.getString("folder"));
        return email;
    }
}