import java.util.Scanner;
import java.util.List;

public class Menu {
    private static Menu instance;
    private static Auth authInstance;
    private static EmailServer emailServer;
    private static Scanner scanner = new Scanner(System.in);

    public static Menu getInstance() {
        if (instance == null) {
            instance = new Menu();
        }
        return instance;
    }

    public static void setAuthInstance(Auth auth) {
        authInstance = auth;
    }

    public static void setEmailServer(EmailServer server) {
        emailServer = server;
    }

    public static void authScreen() {
        System.out.println("Enter your credentials:");

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (authInstance.login(email, password)) {
            System.out.println("Welcome " + authInstance.getCurrentUser().getName() + "!");
            authInstance.getCurrentUser().connectToEmailServer();
            dashboardScreen();
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

    public static void welcomeScreen() {
        System.out.println("Welcome to the Mail System");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");

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
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                welcomeScreen();
        }
    }

    public static void dashboardScreen() {
        while (true) {
            System.out.println("\n=== Email Dashboard ===");
            System.out.println("1. Compose Email");
            System.out.println("2. View Inbox");
            System.out.println("3. Logout");
            System.out.print("> ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    composeEmailScreen();
                    break;
                case "2":
                    viewInboxScreen();
                    break;
                case "3":
                    authInstance.getCurrentUser().disconnectFromEmailServer();
                    authInstance.logout();
                    welcomeScreen();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void composeEmailScreen() {
        System.out.println("\n=== Compose Email ===");
        
        System.out.print("To: ");
        String to = scanner.nextLine();
        
        System.out.print("Subject: ");
        String subject = scanner.nextLine();
        
        System.out.println("Content (type 'END' on a new line to finish):");
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            content.append(line).append("\n");
        }

        try {
            authInstance.getCurrentUser().sendEmail(to, subject, content.toString());
            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
        }
    }

    private static void viewInboxScreen() {
        System.out.println("\n=== Inbox ===");
        List<Email> inbox = authInstance.getCurrentUser().getInbox();
        
        if (inbox.isEmpty()) {
            System.out.println("No emails in inbox.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        // Display emails
        for (int i = 0; i < inbox.size(); i++) {
            Email email = inbox.get(i);
            System.out.printf("%d. From: %s | Subject: %s %s%n",
                i + 1,
                email.getFrom(),
                email.getSubject(),
                email.isRead() ? "" : "(Unread)");
        }

        System.out.println("\nEnter email number to read (or 0 to go back):");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice > 0 && choice <= inbox.size()) {
                Email email = inbox.get(choice - 1);
                authInstance.getCurrentUser().markEmailAsRead(email);
                
                System.out.println("\n=== Email ===");
                System.out.println("From: " + email.getFrom());
                System.out.println("Subject: " + email.getSubject());
                System.out.println("\nContent:");
                System.out.println(email.getContent());
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    public static void registerScreen() {
        System.out.println("Enter your details:");

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (authInstance.register(name, email, password)) {
            System.out.println("Registration successful!");
            authInstance.login(email, password);
            authInstance.getCurrentUser().connectToEmailServer();
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
