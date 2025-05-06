public class User {
    private String name;
    private final String email;
    private String password;

    public User(String name, String email, String password) {
        this.name = name;
        this.password = password;

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format. Email must end with '@mihail.ro'");
        }
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    private boolean isValidEmail(String email) {
        // check if it ends with "@mihail.ro"
        return email.endsWith("@mihail.ro");
    }
}
