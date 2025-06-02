import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public abstract class GenericDatabaseService<T> {
    private final DatabaseConnection dbConnection;

    protected GenericDatabaseService() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public int create(String tableName, String[] columns, Object[] values) throws SQLException {
        System.out.println("Creating record in table: " + tableName);
        StringBuilder query = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder placeholders = new StringBuilder(") VALUES (");

        for (int i = 0; i < columns.length; i++) {
            query.append(columns[i]);
            placeholders.append("?");
            if (i < columns.length - 1) {
                query.append(", ");
                placeholders.append(", ");
            }
        }
        placeholders.append(")");
        query.append(placeholders);

        System.out.println("Full SQL query: " + query);
        System.out.println("Values: " + Arrays.toString(values));

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            // log the full query
            Logger.getInstance().log("INSERT_" + tableName + "_VALUES:" + Arrays.toString(values));
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    System.out.println("Generated ID: " + id);
                    return id;
                }
            }
        }
        System.out.println("No ID was generated");
        return -1;  // Return -1 if no ID was generated
    }

    public List<T> read(String tableName, String whereClause, Object[] params, ResultSetMapper<T> mapper) throws SQLException {
        List<T> results = new ArrayList<>();
        String query = "SELECT * FROM " + tableName;
        if (whereClause != null && !whereClause.isEmpty()) {
            query += " WHERE " + whereClause;
        }
        Logger.getInstance().log(query + "_PARAMS:" + (params != null ? Arrays.toString(params) : "[]"));

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        }
        return results;
    }

    public void update(String tableName, String[] columns, Object[] values, String whereClause, Object[] whereParams) throws SQLException {
        StringBuilder query = new StringBuilder("UPDATE " + tableName + " SET ");

        for (int i = 0; i < columns.length; i++) {
            query.append(columns[i]).append(" = ?");
            if (i < columns.length - 1) {
                query.append(", ");
            }
        }

        if (whereClause != null && !whereClause.isEmpty()) {
            query.append(" WHERE ").append(whereClause);
        }

        Logger.getInstance().log(query + "_VALUES:" + Arrays.toString(values) +
                (whereParams != null ? "_WHERE_PARAMS:" + Arrays.toString(whereParams) : ""));

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
            int paramIndex = 1;

            // Set values for SET clause
            for (Object value : values) {
                pstmt.setObject(paramIndex++, value);
            }

            // Set values for WHERE clause
            if (whereParams != null) {
                for (Object param : whereParams) {
                    pstmt.setObject(paramIndex++, param);
                }
            }

            pstmt.executeUpdate();
        }
    }

    public void delete(String tableName, String whereClause, Object[] params) throws SQLException {
        String query = "DELETE FROM " + tableName;
        if (whereClause != null && !whereClause.isEmpty()) {
            query += " WHERE " + whereClause;
        }

        Logger.getInstance().log(query + "_PARAMS:" + (params != null ? Arrays.toString(params) : "[]"));

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
            }
            pstmt.executeUpdate();
        }
    }

    // Interface for mapping ResultSet to entity
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected abstract T mapResultSet(ResultSet rs) throws SQLException;
}