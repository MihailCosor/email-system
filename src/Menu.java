import java.util.Scanner;
import java.util.List;

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
            System.out.println("3. View Profile");
            System.out.println("4. Logout");
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
                    handleViewProfile();
                    break;
                case 4:
                    handleLogout();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void handleSendEmail() {
        System.out.print("Enter recipient's email: ");
        String to = scanner.nextLine();
        
        System.out.print("Enter subject: ");
        String subject = scanner.nextLine();
        
        System.out.println("Enter content (type 'END' on a new line to finish):");
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            content.append(line).append("\n");
        }
        
        emailClient.sendEmail(to, subject, content.toString());
        System.out.println("Email sent successfully.");
    }

    private void handleViewInbox() {
        displayInboxMenu();
    }

    private void displayInboxMenu() {
        while (true) {
            System.out.println("\n=== Inbox Menu ===");
            System.out.println("1. View all emails");
            System.out.println("2. View unread emails");
            System.out.println("3. View full email");
            System.out.println("4. Send new email");
            System.out.println("5. Delete email");
            System.out.println("6. Mark email as read");
            System.out.println("7. Mark email as unread");
            System.out.println("8. Back to main menu");
            System.out.print("Choose an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    viewAllEmails();
                    break;
                case 2:
                    viewUnreadEmails();
                    break;
                case 3:
                    viewFullEmail();
                    break;
                case 4:
                    handleSendEmail();
                    break;
                case 5:
                    deleteEmail();
                    break;
                case 6:
                    markEmailAsRead();
                    break;
                case 7:
                    markEmailAsUnread();
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void viewAllEmails() {
        List<Email> emails = emailClient.getInbox();
        if (emails.isEmpty()) {
            System.out.println("No emails in inbox.");
            return;
        }

        for (int i = 0; i < emails.size(); i++) {
            Email email = emails.get(i);
            System.out.printf("%d. [%s] From: %s, Subject: %s, Time: %s%n",
                i + 1,
                email.isRead() ? "READ" : "UNREAD",
                email.getFrom(),
                email.getSubject(),
                email.getTimestamp());
        }
    }

    private void viewUnreadEmails() {
        List<Email> emails = emailClient.getInbox();
        if (emails.isEmpty()) {
            System.out.println("No emails in inbox.");
            return;
        }

        boolean hasUnread = false;
        for (int i = 0; i < emails.size(); i++) {
            Email email = emails.get(i);
            if (!email.isRead()) {
                hasUnread = true;
                System.out.printf("%d. From: %s, Subject: %s, Time: %s%n",
                    i + 1,
                    email.getFrom(),
                    email.getSubject(),
                    email.getTimestamp());
            }
        }

        if (!hasUnread) {
            System.out.println("No unread emails.");
        }
    }

    private void viewFullEmail() {
        viewAllEmails();
        System.out.print("\nEnter the number of the email to view (0 to cancel): ");
        int choice = getIntInput();

        if (choice > 0) {
            List<Email> emails = emailClient.getInbox();
            if (choice <= emails.size()) {
                Email email = emails.get(choice - 1);
                System.out.println("\n=== Email Details ===");
                System.out.println("From: " + email.getFrom());
                System.out.println("To: " + email.getTo());
                System.out.println("Subject: " + email.getSubject());
                System.out.println("Time: " + email.getTimestamp());
                System.out.println("Status: " + (email.isRead() ? "READ" : "UNREAD"));
                System.out.println("\nContent:");
                System.out.println("----------------------------------------");
                System.out.println(email.getContent());
                System.out.println("----------------------------------------");
                
                // Mark as read when viewing
                if (!email.isRead()) {
                    emailClient.markEmailAsRead(choice - 1);
                }
            } else {
                System.out.println("Invalid email number.");
            }
        }
    }

    private void deleteEmail() {
        viewAllEmails();
        System.out.print("Enter the number of the email to delete (0 to cancel): ");
        int choice = getIntInput();

        if (choice > 0) {
            emailClient.deleteEmail(choice - 1);
            System.out.println("Email deleted successfully.");
        }
    }

    private void markEmailAsRead() {
        viewAllEmails();
        System.out.print("Enter the number of the email to mark as read (0 to cancel): ");
        int choice = getIntInput();

        if (choice > 0) {
            emailClient.markEmailAsRead(choice - 1);
            System.out.println("Email marked as read.");
        }
    }

    private void markEmailAsUnread() {
        viewAllEmails();
        System.out.print("Enter the number of the email to mark as unread (0 to cancel): ");
        int choice = getIntInput();

        if (choice > 0) {
            emailClient.markEmailAsUnread(choice - 1);
            System.out.println("Email marked as unread.");
        }
    }

    private void handleLogout() {
        emailClient.disconnect();
        auth.logout();
        System.out.println("Logged out successfully.");
    }

    private void handleViewProfile() {
        User currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            System.out.println("\n=== User Profile ===");
            System.out.println("Name: " + currentUser.getName());
            System.out.println("Email: " + currentUser.getEmail());
            System.out.println("User ID: " + currentUser.getId());
            System.out.println("Account Status: Active");
            System.out.println("Last Login: " + java.time.LocalDateTime.now());
            System.out.println("----------------------------------------");
        } else {
            System.out.println("Error retrieving profile information.");
        }
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
