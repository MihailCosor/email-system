import java.util.ArrayList;
import java.util.List;

public class Folder {
    private String name;
    private List<Email> emails;

    public Folder(String name) {
        this.name = name;
        this.emails = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void addEmail(Email email) {
        emails.add(email);
    }

    public void removeEmail(Email email) {
        emails.remove(email);
    }
} 