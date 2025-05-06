import java.util.Scanner;

public class Menu {
    private static Menu instance;
    private static Auth authInstance;

    public static Menu getInstance() {
        if (instance == null) {
            instance = new Menu();
        }
        return instance;
    }

    public static void setAuthInstance(Auth auth) {
        authInstance = auth;
    }

    public static void authScreen() {
        System.out.println("Enter your credentials:");
        Scanner scanner = new Scanner(System.in);

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (authInstance.login(email, password)) {
            System.out.println("Welcome " + authInstance.getCurrentUser().getName() + "!");
            // Proceed to the main menu or dashboard
        } else {
            System.out.println("Login failed");
            System.out.println("1. Try again");
            System.out.println("2. Back to main menu");
            System.out.print("> ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    authScreen();
                    break;
                case "2":
                    welcomeScreen();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void welcomeScreen(){
        System.out.println("Welcome to the Mail System");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");

        Scanner scanner = new Scanner(System.in);
        System.out.print("> ");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                authScreen();
                break;
            case "2":
                registerScreen();
                break;
            case "3":
                System.out.println("Exiting...");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    public static void dashboardScreen() {
        System.out.println("Welcome to the Dashboard!");

    }

    public static void registerScreen() {
        System.out.println("Enter your details:");
        Scanner scanner = new Scanner(System.in);

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if(authInstance.register(name, email, password)) {
            System.out.println("Registration successful!");
            authInstance.login(email, password);
            dashboardScreen();
        } else {
            System.out.println("Registration failed");
            System.out.println("1. Try again");
            System.out.println("2. Back to main menu");
            System.out.print("> ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    registerScreen();
                    break;
                case "2":
                    welcomeScreen();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

}
