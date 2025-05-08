import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// handles date and time operations with multiple formatting options
public class DateTime implements Serializable, Comparable<DateTime> {
    // internal representation of date and time
    private LocalDateTime dateTime;
    // format pattern for full date time display (dd/MM/yyyy HH:mm:ss)
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    // format pattern for abbreviated date time display (dd/MM/yy HH:mm)
    private static final DateTimeFormatter SHORT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    // creates a new datetime object with current system time
    public DateTime() {
        this.dateTime = LocalDateTime.now();
    }

    // creates a new datetime object with specified LocalDateTime
    public DateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    // factory method to create a datetime object with current time
    public static DateTime now() {
        return new DateTime();
    }

    // formats the datetime in full format (dd/MM/yyyy HH:mm:ss)
    public String getFormattedDateTime() {
        return dateTime.format(DISPLAY_FORMAT);
    }

    // formats the datetime in short format (dd/MM/yy HH:mm)
    public String getShortDateTime() {
        return dateTime.format(SHORT_FORMAT);
    }

    // retrieves the underlying LocalDateTime object
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    // implements comparison for sorting datetime objects
    @Override
    public int compareTo(DateTime other) {
        return this.dateTime.compareTo(other.dateTime);
    }

    // converts datetime to string using ISO format
    @Override
    public String toString() {
        return dateTime.toString();
    }
} 