import java.util.Scanner;

public class Menu {
    private Scanner scanner;
    private Auth auth;
    private EmailClient emailClient;

    public Menu() {
        this.scanner = new Scanner(System.in);
        this.auth = Auth.getInstance();
        this.emailClient = new EmailClient();
    }

    public void showMainMenu() {
        while (true) {
            System.out.println("\n=== Email System ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    handleRegister();
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    auth.close();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void handleLogin() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (auth.login(email, password)) {
            if (emailClient.connect(email)) {
                showUserMenu();
            } else {
                System.out.println("Failed to connect to email server");
                auth.logout();
            }
        }
    }

    private void handleRegister() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (auth.register(name, email, password)) {
            System.out.println("Registration successful! Please login.");
        }
    }

    private void showUserMenu() {
        while (auth.isLoggedIn()) {
            System.out.println("\n=== User Menu ===");
            System.out.println("1. Send Email");
            System.out.println("2. View Inbox");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1:
                    handleSendEmail();
                    break;
                case 2:
                    handleViewInbox();
                    break;
                case 3:
                    handleLogout();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void handleSendEmail() {
        System.out.print("Enter recipient email: ");
        String to = scanner.nextLine();
        System.out.print("Enter subject: ");
        String subject = scanner.nextLine();
        System.out.print("Enter message: ");
        String content = scanner.nextLine();

        if (emailClient.sendEmail(to, subject, content)) {
            System.out.println("Email sent successfully!");
        } else {
            System.out.println("Failed to send email.");
        }
    }

    private void handleViewInbox() {
        System.out.println("\n=== Inbox ===");
        var inbox = emailClient.getInbox();
        if (inbox.isEmpty()) {
            System.out.println("No emails in inbox.");
            return;
        }

        for (int i = 0; i < inbox.size(); i++) {
            Email email = inbox.get(i);
            System.out.printf("%d. From: %s | Subject: %s\n", 
                i + 1, email.getFrom(), email.getSubject());
        }

        System.out.print("\nEnter email number to view (0 to return): ");
        int choice = getIntInput();
        if (choice > 0 && choice <= inbox.size()) {
            Email email = inbox.get(choice - 1);
            System.out.println("\nFrom: " + email.getFrom());
            System.out.println("Subject: " + email.getSubject());
            System.out.println("Content: " + email.getContent());
        }
    }

    private void handleLogout() {
        emailClient.disconnect();
        auth.logout();
        System.out.println("Logged out successfully.");
    }

    private int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}
