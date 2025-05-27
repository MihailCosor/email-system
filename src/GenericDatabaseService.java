import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class GenericDatabaseService<T> {
    private final DatabaseConnection dbConnection;

    protected GenericDatabaseService() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public void create(String tableName, String[] columns, Object[] values) throws SQLException {
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

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
            System.out.println("Executing query: " + query);
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            pstmt.executeUpdate();
        }
    }

    public List<T> read(String tableName, String whereClause, Object[] params, ResultSetMapper<T> mapper) throws SQLException {
        List<T> results = new ArrayList<>();
        String query = "SELECT * FROM " + tableName;
        if (whereClause != null && !whereClause.isEmpty()) {
            query += " WHERE " + whereClause;
        }

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