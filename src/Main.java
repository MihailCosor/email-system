
public class Main {
    public static void main(String[] args) {
        Menu menu = Menu.getInstance();
        Auth auth = Auth.getInstance();
        Menu.setAuthInstance(auth);

        // add some dummy users
        auth.register("Mihail", "admin@mihail.ro", "123");
        auth.register("John", "john@mihail.ro", "123");

        Menu.welcomeScreen();
    }
}
