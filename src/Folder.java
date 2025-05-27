import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// represents an email folder that can contain multiple email messages
public class Folder implements Serializable {
    // name of the folder (inbox, sent, trash, etc.)
    private String name;
    // list of emails contained in this folder
    private List<Email> emails;
    // indicates if this is a system folder (inbox, spam) that cannot be deleted
    private boolean isSystem;

    // default constructor
    public Folder() {
        this.name = "";
        this.emails = new ArrayList<>();
        this.isSystem = true;
    }

    // creates a new folder with specified name and system status
    public Folder(String name, boolean isSystem) {
        this.name = name.toLowerCase();  // normalize folder names to lowercase
        this.emails = new ArrayList<>();
        this.isSystem = isSystem;
    }

    // retrieves the folder name
    public String getName() {
        return name;
    }

    // checks if this is a system folder
    public boolean isSystem() {
        return isSystem;
    }

    // returns a defensive copy of the emails list
    public List<Email> getEmails() {
        return new ArrayList<>(emails);
    }

    // adds an email to this folder and updates the email's folder reference
    public void addEmail(Email email) {
        emails.add(email);
        email.setFolder(name);
    }

    // removes an email from this folder
    public void removeEmail(Email email) {
        emails.remove(email);
    }

    // retrieves an email by its index in the folder
    public Email getEmail(int index) {
        if (index >= 0 && index < emails.size()) {
            return emails.get(index);
        }
        return null;
    }

    // counts the number of unread emails in the folder
    public int getUnreadCount() {
        return (int) emails.stream().filter(e -> !e.isRead()).count();
    }

    // checks if the folder contains no emails
    public boolean isEmpty() {
        return emails.isEmpty();
    }

    // returns the total number of emails in the folder
    public int size() {
        return emails.size();
    }

    // formats folder information for display
    @Override
    public String toString() {
        return name + " (" + emails.size() + " emails, " + getUnreadCount() + " unread)";
    }
} 