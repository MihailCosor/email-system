import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Folder implements Serializable {
    private String name;
    private List<Email> emails;
    private boolean isSystem; // true for inbox and spam

    public Folder(String name, boolean isSystem) {
        this.name = name.toLowerCase();
        this.emails = new ArrayList<>();
        this.isSystem = isSystem;
    }

    public String getName() {
        return name;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public List<Email> getEmails() {
        return new ArrayList<>(emails);
    }

    public void addEmail(Email email) {
        emails.add(email);
        email.setFolder(name);
    }

    public void removeEmail(Email email) {
        emails.remove(email);
    }

    public Email getEmail(int index) {
        if (index >= 0 && index < emails.size()) {
            return emails.get(index);
        }
        return null;
    }

    public int getUnreadCount() {
        return (int) emails.stream().filter(e -> !e.isRead()).count();
    }

    public boolean isEmpty() {
        return emails.isEmpty();
    }

    public int size() {
        return emails.size();
    }

    @Override
    public String toString() {
        return name + " (" + emails.size() + " emails, " + getUnreadCount() + " unread)";
    }
} 