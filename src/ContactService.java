import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ContactService extends GenericDatabaseService<Contact> {
    private static ContactService instance;
    private static final String TABLE_NAME = "contacts";

    private ContactService() {
        super();
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
        create(TABLE_NAME, columns, values);
    }

    public List<Contact> getAllContacts() throws SQLException {
        return read(TABLE_NAME, null, null, this::mapResultSet);
    }

    public Contact getContactById(int id) throws SQLException {
        List<Contact> contacts = read(TABLE_NAME, "id = ?", new Object[]{id}, this::mapResultSet);
        return contacts.isEmpty() ? null : contacts.get(0);
    }

    public Contact getContactByEmail(String email) throws SQLException {
        List<Contact> contacts = read(TABLE_NAME, "email = ?", new Object[]{email}, this::mapResultSet);
        return contacts.isEmpty() ? null : contacts.get(0);
    }

    public void updateContact(Contact contact) throws SQLException {
        String[] columns = {"name", "email"};
        Object[] values = {contact.getName(), contact.getEmail()};
        update(TABLE_NAME, columns, values, "id = ?", new Object[]{contact.getId()});
    }

    public void deleteContact(int id) throws SQLException {
        delete(TABLE_NAME, "id = ?", new Object[]{id});
    }

    @Override
    protected Contact mapResultSet(ResultSet rs) throws SQLException {
        Contact contact = new Contact(
            rs.getString("name"),
            rs.getString("email")
        );
        contact.setId(rs.getInt("id"));
        return contact;
    }
}