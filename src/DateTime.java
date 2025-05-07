import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTime implements Serializable {
    private LocalDateTime dateTime;
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter SHORT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    public DateTime() {
        this.dateTime = LocalDateTime.now();
    }

    public DateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public static DateTime now() {
        return new DateTime(LocalDateTime.now());
    }

    public String getFormattedDateTime() {
        return dateTime.format(DISPLAY_FORMAT);
    }

    public String getShortDateTime() {
        return dateTime.format(SHORT_FORMAT);
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return getFormattedDateTime();
    }
} 