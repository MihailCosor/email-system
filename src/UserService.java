import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserService extends GenericDatabaseService<User> {
    private static UserService instance;
    private static final String TABLE_NAME = "users";

    private UserService() {
        super();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public void createUser(User user) throws SQLException {
        if (!isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format. Email must end with '@mihail.ro' or '@example.com'");
        }

        String[] columns = {"email", "password", "name"};
        Object[] values = {
            user.getEmail(),
            user.getPassword(),
            user.getName()
        };
        create(TABLE_NAME, columns, values);
    }

    public User getUserByEmail(String email) throws SQLException {
        List<User> users = read(TABLE_NAME, "email = ?", new Object[]{email}, this::mapResultSet);
        return users.isEmpty() ? null : users.get(0);
    }

    public List<User> getAllUsers() throws SQLException {
        return read(TABLE_NAME, null, null, this::mapResultSet);
    }

    public void updateUser(User user) throws SQLException {
        String[] columns = {"password", "name"};
        Object[] values = {user.getPassword(), user.getName()};
        update(TABLE_NAME, columns, values, "email = ?", new Object[]{user.getEmail()});
    }

    public void deleteUser(String email) throws SQLException {
        delete(TABLE_NAME, "email = ?", new Object[]{email});
    }

    private boolean isValidEmail(String email) {
        return email != null && (email.endsWith("@mihail.ro") || email.endsWith("@example.com"));
    }

    @Override
    protected User mapResultSet(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password")
        );
        return user;
    }
}