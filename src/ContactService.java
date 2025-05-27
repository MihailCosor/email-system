import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ContactService {
    private static ContactService instance;
    private final GenericDatabaseService<Contact> dbService;
    private static final String TABLE_NAME = "contacts";

    private ContactService() {
        this.dbService = GenericDatabaseService.getInstance();
    }

    public static synchronized ContactService getInstance() {
        if (instance == null) {
            instance = new ContactService();
        }
        return instance;
    }

    public void createContact(Contact contact) throws SQLException {
        String[] columns = {"name", "email"};
        Object[] values = {contact.getName(), contact.getEmail()};
        dbService.create(TABLE_NAME, columns, values);
    }

    public List<Contact> getContactsByUser(String userEmail) throws SQLException {
        return dbService.read(
                TABLE_NAME,
                "user_email = ?",
                new Object[]{userEmail},
                rs -> mapResultSetToContact(rs)
        );
    }

    public Contact getContactByEmail(String email, String userEmail) throws SQLException {
        List<Contact> contacts = dbService.read(
                TABLE_NAME,
                "email = ? AND user_email = ?",
                new Object[]{email, userEmail},
                rs -> mapResultSetToContact(rs)
        );
        return contacts.isEmpty() ? null : contacts.get(0);
    }

    public void updateContact(Contact contact) throws SQLException {
        String[] columns = {"name"};
        Object[] values = {contact.getName()};
        dbService.update(
                TABLE_NAME,
                columns,
                values,
                "email = ?",
                new Object[]{contact.getEmail()}
        );
    }

    public void deleteContact(String email, String userEmail) throws SQLException {
        dbService.delete(
                TABLE_NAME,
                "email = ? AND user_email = ?",
                new Object[]{email, userEmail}
        );
    }

    private Contact mapResultSetToContact(ResultSet rs) throws SQLException {
        Contact contact = new Contact(
                rs.getString("name"),
                rs.getString("email")
        );
        return contact;
    }
}