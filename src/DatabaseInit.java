import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInit {
    // calc the path to db_structure.sql relative to the src folder
    private static final String DB_STRUCTURE_FILE = "files/db.sql";

    public static void initDB() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        Statement stmt = conn.createStatement();

        // Load SQL commands from the file
        String[] sqlCommands = new DatabaseInit().loadSQLFile();

        try {
            // Execute each SQL command
            for (String sql : sqlCommands) {
                if (!sql.trim().isEmpty()) {
                    System.out.println("Executing SQL command: " + sql);
                    stmt.executeUpdate(sql);
                }
            }

            System.out.println("Database initialized successfully!");
        } finally {
            // Close the statement
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Error closing statement: " + e.getMessage());
                }
            }
        }
    }

    public String[] loadSQLFile() {
        // open the file and append each line to a StringBuilder
        StringBuilder sqlBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(DB_STRUCTURE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sqlBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading SQL file: " + e.getMessage());
        }

        // split the SQL commands by semicolon and return as an array
        String sqlContent = sqlBuilder.toString();
        String[] sqlCommands = sqlContent.split(";");
        for (int i = 0; i < sqlCommands.length; i++) {
            sqlCommands[i] = sqlCommands[i].trim();
            // append semicolon back to each command
            if (!sqlCommands[i].isEmpty()) {
                sqlCommands[i] += ";";
            }
        }
        return sqlCommands;
    }
}