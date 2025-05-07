import java.io.Serializable;

public class Contact extends Person {
    public Contact(String name, String email) {
        super(name, email);
    }

    @Override
    public String toString() {
        return name + " <" + email + ">";
    }
} 