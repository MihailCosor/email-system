import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Logger {
    private static Logger instance;
    private static String FILENAME;
    private static DateTimeFormatter FORMAT;
    private static boolean canWrite;

    private Logger(){
        // init vars
        FILENAME = "audit.csv";
        FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        canWrite = createFileIfNotExists();
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public synchronized void log(String action) {
        if (!canWrite) {
            return;
        }

        String timestamp = LocalDateTime.now().format(FORMAT);

        // sanitize the action string
        String escapedAction = action.replace("\"", "\"\"");
        String safeAction = "\"" + escapedAction + "\"";

        // declare resources
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;

        try {
            fw = new FileWriter(FILENAME, true);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);

            pw.println(safeAction + ", " + timestamp);
        } catch (Exception e) {
            System.err.println("Error writing to log file: " + e.getMessage());
            canWrite = false; // disable further writes if an error occurs
        } finally {
            try {
                // close the resources
                if (pw != null) pw.close();
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (Exception e) {
                System.err.println("Error closing log file resources: " + e.getMessage());
            }
        }
    }

    private boolean createFileIfNotExists() {
        Path filePath = Paths.get(FILENAME);
        if(Files.notExists(filePath)){
            try {
                Files.createFile(filePath);

                // add the header
                String header = "nume_actiune, timestamp";
                Files.write(filePath, List.of(header), StandardOpenOption.APPEND);
                return true;
            } catch (Exception e) {
                System.err.println("Error creating log file: " + e.getMessage());
                return false;
            }
        }
        return true;
    }
}
