import java.io.Serializable;

// represents a contact in a user's address book
public class Contact extends Person implements Serializable {
    private int id;

    // creates a new contact with name and email
    public Contact(String name, String email) {
        super(name, email);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // formats contact as "name <email>" for display
    @Override
    public String toString() {
        return name + " <" + email + ">";
    }
} 