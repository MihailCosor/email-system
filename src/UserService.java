import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    private static UserService instance;
    private final GenericDatabaseService<User> dbService;
    private static final String TABLE_NAME = "users";

    private UserService() {
        this.dbService = GenericDatabaseService.getInstance();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public void createUser(User user) throws SQLException {
        System.out.println("Creating user in database: " + user.getEmail());
        String[] columns = {"email", "password", "name"};
        Object[] values = {user.getEmail(), user.getPassword(), user.getName()};
        System.out.println("Creating user: " + user.getEmail());
        dbService.create(TABLE_NAME, columns, values);
    }

    public User getUserByEmail(String email) throws SQLException {
        List<User> users = dbService.read(
                TABLE_NAME,
                "email = ?",
                new Object[]{email},
                rs -> mapResultSetToUser(rs)
        );
        return users.isEmpty() ? null : users.get(0);
    }

    public List<User> getAllUsers() throws SQLException {
        return dbService.read(
                TABLE_NAME,
                null,
                null,
                rs -> mapResultSetToUser(rs)
        );
    }

    public void updateUser(User user) throws SQLException {
        String[] columns = {"password", "name"};
        Object[] values = {user.getPassword(), user.getName()};
        dbService.update(
                TABLE_NAME,
                columns,
                values,
                "email = ?",
                new Object[]{user.getEmail()}
        );
    }

    public void deleteUser(String email) throws SQLException {
        dbService.delete(
                TABLE_NAME,
                "email = ?",
                new Object[]{email}
        );
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password")
        );
    }
}