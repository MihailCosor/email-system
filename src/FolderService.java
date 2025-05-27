import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FolderService extends GenericDatabaseService<Folder> {
    private static FolderService instance;
    private static final String TABLE_NAME = "folders";

    private FolderService() {
        super();
    }

    public static synchronized FolderService getInstance() {
        if (instance == null) {
            instance = new FolderService();
        }
        return instance;
    }

    public void createFolder(String folderName, String userEmail) throws SQLException {
        // Check if folder already exists for this user
        if (getFolderByNameAndUser(folderName, userEmail) != null) {
            // simply return if it exists
            return;
        }

        String[] columns = {"folder_name", "user_email"};
        Object[] values = {folderName, userEmail};
        create(TABLE_NAME, columns, values);
    }

    public Folder getFolderByNameAndUser(String folderName, String userEmail) throws SQLException {
        List<Folder> folders = read(TABLE_NAME, 
            "folder_name = ? AND user_email = ?", 
            new Object[]{folderName, userEmail}, 
            this::mapResultSet);
        return folders.isEmpty() ? null : folders.get(0);
    }

    public List<Folder> getFoldersByUser(String userEmail) throws SQLException {
        return read(TABLE_NAME, "user_email = ?", new Object[]{userEmail}, this::mapResultSet);
    }

    public Folder getFolderById(int folderId) throws SQLException {
        List<Folder> folders = read(TABLE_NAME, "id = ?", new Object[]{folderId}, this::mapResultSet);
        return folders.isEmpty() ? null : folders.get(0);
    }

    public void deleteFolder(int folderId) throws SQLException {
        delete(TABLE_NAME, "id = ?", new Object[]{folderId});
    }

    public void createDefaultFolders(String userEmail) throws SQLException {
        createFolder("inbox", userEmail);
        createFolder("spam", userEmail);
    }

    @Override
    protected Folder mapResultSet(ResultSet rs) throws SQLException {
        Folder folder = new Folder(
            rs.getString("folder_name"),
            true  // isSystem is true for all folders in the database
        );
        folder.setId(rs.getInt("id"));  // Set the folder ID from the database
        return folder;
    }
}
