import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInit {
    // calc the path to db_structure.sql relative to the src folder
    private static final String DB_STRUCTURE_FILE = "src/files/db_structure.sql";

    private static final String CREATE_USERS_TABLE = """
        CREATE TABLE IF NOT EXISTS users (
            email VARCHAR(255) PRIMARY KEY,
            password VARCHAR(255) NOT NULL,
            name VARCHAR(255) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """;

    private static final String CREATE_EMAILS_TABLE = """
        CREATE TABLE IF NOT EXISTS emails (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            sender VARCHAR(255) NOT NULL,
            recipient VARCHAR(255) NOT NULL,
            subject VARCHAR(255) NOT NULL,
            content TEXT NOT NULL,
            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            is_read BOOLEAN DEFAULT FALSE,
            folder VARCHAR(50) DEFAULT 'inbox',
            FOREIGN KEY (sender) REFERENCES users(email),
            FOREIGN KEY (recipient) REFERENCES users(email)
        )
    """;

    private static final String CREATE_CONTACTS_TABLE = """
        CREATE TABLE IF NOT EXISTS contacts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name VARCHAR(255) NOT NULL,
            email VARCHAR(255) NOT NULL,
            FOREIGN KEY (user_email) REFERENCES users(email),
            UNIQUE(email, user_email)
        )
    """;

    public static void initializeDatabase() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        Statement stmt = conn.createStatement();

        try {
            // Create tables
            stmt.execute(CREATE_USERS_TABLE);
            stmt.execute(CREATE_EMAILS_TABLE);
            stmt.execute(CREATE_CONTACTS_TABLE);

            System.out.println("Database initialized successfully!");
        } finally {
            stmt.close();
        }
    }
}