import java.util.ArrayList;

public class Auth {
    private static Auth instance;
    private User user;
    private boolean isLoggedIn;
    private static ArrayList<User> users = new ArrayList<>();

    private Auth() {
        // Private constructor to prevent instantiation
        this.user = null;
        this.isLoggedIn = false;
    }

    public static Auth getInstance() {
        if (instance == null) {
            instance = new Auth();
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public User getCurrentUser() {
        return user;
    }

    public void setCurrentUser(User user) {
        this.user = user;
        this.isLoggedIn = true;
    }

    public void logout() {
        this.user = null;
        this.isLoggedIn = false;
    }

    public boolean login(String email, String password) {
        for (User u : users) {
            if (u.getEmail().equals(email) && u.getPassword().equals(password)) {
                this.user = u;
                this.isLoggedIn = true;
                System.out.println("Login successful!");
                return true;
            }
        }
        System.out.println("Invalid email or password.");
        return false;
    }

    public void register(String name, String email, String password) {
        User newUser = new User(name, email, password);
        if(!emailExists(email)) {
            users.add(newUser);
            System.out.println("Registration successful!");
        } else {
            System.out.println("Email already exists.");
        }
    }

    public boolean emailExists(String email) {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }
}
