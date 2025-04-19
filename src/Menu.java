public class Menu {
    private static Menu instance;

    private Menu() {
        // Private constructor to prevent instantiation
    }

    public static Menu getInstance() {
        if (instance == null) {
            instance = new Menu();
        }
        return instance;
    }

    public void display() {
        System.out.println("1. Send Email");
        System.out.println("2. Receive Email");
        System.out.println("3. Delete Email");
        System.out.println("4. List Emails");
        // Add more menu options as needed
    }
} 