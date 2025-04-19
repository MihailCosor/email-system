import java.util.List;
import java.util.ArrayList;

public class EmailService {
    private List<Email> emails;

    public EmailService() {
        this.emails = new ArrayList<>();
    }

    public void sendEmail(Email email) {
        emails.add(email);
        System.out.println("Email sent to " + email.getRecipient());
    }

    public void deleteEmail(Email email) {
        emails.remove(email);
        System.out.println("Email deleted: " + email.getSubject());
    }

    public List<Email> listEmails() {
        return emails;
    }
} 