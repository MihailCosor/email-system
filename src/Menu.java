import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

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
            System.out.println("4. Manage Contacts");
            System.out.println("5. Logout");
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
                    handleContactsMenu();
                    break;
                case 5:
                    handleLogout();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void handleContactsMenu() {
        while (true) {
            System.out.println("\n=== Contacts Menu ===");
            System.out.println("1. List Contacts");
            System.out.println("2. Add Contact");
            System.out.println("3. Remove Contact");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1:
                    listContacts();
                    break;
                case 2:
                    addContact();
                    break;
                case 3:
                    removeContact();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void listContacts() {
        List<Contact> contacts = auth.getCurrentUser().getContactsList();
        if (contacts.isEmpty()) {
            System.out.println("No contacts found.");
            return;
        }

        System.out.println("\n=== Contact List ===");
        for (int i = 0; i < contacts.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, contacts.get(i));
        }
        System.out.println("----------------------------------------");
    }

    private void addContact() {
        System.out.print("Enter contact name: ");
        String name = scanner.nextLine();
        System.out.print("Enter contact email: ");
        String email = scanner.nextLine();

        if (auth.getCurrentUser().addContact(name, email)) {
            System.out.println("Contact added successfully!");
        } else {
            System.out.println("Contact already exists.");
        }
    }

    private void removeContact() {
        List<Contact> contacts = auth.getCurrentUser().getContactsList();
        if (contacts.isEmpty()) {
            System.out.println("No contacts to remove.");
            return;
        }

        System.out.println("\n=== Contact List ===");
        for (int i = 0; i < contacts.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, contacts.get(i));
        }
        System.out.println("----------------------------------------");
        
        System.out.print("Enter contact number: ");
        int contactNumber = getIntInput();
        
        if (contactNumber < 1 || contactNumber > contacts.size()) {
            System.out.println("Invalid contact number.");
            return;
        }

        Contact contactToRemove = contacts.get(contactNumber - 1);
        if (auth.getCurrentUser().removeContact(contactToRemove)) {
            System.out.println("Contact removed successfully!");
        } else {
            System.out.println("Failed to remove contact.");
        }
    }

    private void handleSendEmail() {
        System.out.println("\n1. Do you want to select from contacts?: ");
        int choice = getIntInput();
        String to = null;

        if (choice == 1) {
            List<Contact> contacts = auth.getCurrentUser().getContactsList();
            if (contacts.isEmpty()) {
                System.out.println("No contacts found. Please enter email manually.");
                System.out.print("Enter recipient's email: ");
                to = scanner.nextLine();
            } else {
                System.out.println("\n=== Contact List ===");
                for (int i = 0; i < contacts.size(); i++) {
                    System.out.printf("%d. %s%n", i + 1, contacts.get(i));
                }
                System.out.println("----------------------------------------");
                
                while (true) {
                    System.out.print("Enter contact number: ");
                    int contactNum = getIntInput();
                    if (contactNum >= 1 && contactNum <= contacts.size()) {
                        to = contacts.get(contactNum - 1).getEmail();
                        break;
                    }
                    System.out.println("Invalid contact number. Please try again.");
                }
            }
        }
        if (to == null) {
            System.out.print("Enter recipient's email: ");
            to = scanner.nextLine();
        }
        
        System.out.print("Enter subject: ");
        String subject = scanner.nextLine();
        
        System.out.println("Enter content (type 'END' on a new line to finish):");
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            content.append(line).append("\n");
        }
        
        try {
            boolean success = emailClient.sendEmail(to, subject, content.toString());
            if (success) {
                System.out.println("Email sent successfully.");
            }
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
        }
    }

    private void handleViewInbox() {
        while (true) {
            System.out.println("\n=== Folders ===");
            System.out.println("1. Inbox");
            System.out.println("2. Spam");
            System.out.println("3. Back to Main Menu");
            System.out.print("Choose a folder: ");

            int choice = getIntInput();
            switch (choice) {
                case 1:
                    handleFolderView("inbox");
                    break;
                case 2:
                    handleFolderView("spam");
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void handleFolderView(String folderName) {
        while (true) {
            List<Email> emails = folderName.equals("inbox") ? 
                emailClient.getInbox() : emailClient.getSpam();
            
            System.out.println("\n=== " + folderName.toUpperCase() + " ===");
            if (!emails.isEmpty()) {
                for (int i = 0; i < emails.size(); i++) {
                    Email email = emails.get(i);
                    System.out.printf("%d. [%s] From: %s, Subject: %s, Time: %s%n",
                        i + 1,
                        email.isRead() ? "READ" : "UNREAD",
                        email.getFrom(),
                        email.getSubject(),
                        email.getTimestamp().getShortDateTime());
                }
            } else {
                System.out.println("No emails in this folder.");
            }
            System.out.println("----------------------------------------");

            System.out.println("\n=== " + folderName.toUpperCase() + " Options ===");
            System.out.println("1. View Full Email");
            System.out.println("2. Mark as Read");
            System.out.println("3. Mark as Unread");
            System.out.println("4. Delete Email");
            System.out.println("5. Move to " + (folderName.equals("inbox") ? "Spam" : "Inbox"));
            System.out.println("6. Back to Folders");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            if (emails.isEmpty() && choice != 6) {
                System.out.println("No emails to perform operations on.");
                continue;
            }

            switch (choice) {
                case 1:
                    viewFullEmailInFolder(emails);
                    break;
                case 2:
                    markEmailInFolder(emails, true);
                    break;
                case 3:
                    markEmailInFolder(emails, false);
                    break;
                case 4:
                    deleteEmailInFolder(emails);
                    break;
                case 5:
                    moveEmailBetweenFolders(emails, folderName);
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void viewFullEmailInFolder(List<Email> emails) {
        System.out.print("\nEnter the number of the email to view (0 to cancel): ");
        int choice = getIntInput();

        if (choice > 0 && choice <= emails.size()) {
            Email email = emails.get(choice - 1);
            System.out.println("\n=== Email Details ===");
            System.out.println("From: " + email.getFrom());
            System.out.println("To: " + email.getTo());
            System.out.println("Subject: " + email.getSubject());
            System.out.println("Time: " + email.getTimestamp().getFormattedDateTime());
            System.out.println("Status: " + (email.isRead() ? "READ" : "UNREAD"));
            System.out.println("\nContent:");
            System.out.println("----------------------------------------");
            System.out.println(email.getContent());
            System.out.println("----------------------------------------");
            
            if (!email.isRead()) {
                emailClient.markEmailAsRead(choice - 1);
            }
        } else if (choice != 0) {
            System.out.println("Invalid email number.");
        }
    }

    private void markEmailInFolder(List<Email> emails, boolean markAsRead) {
        System.out.printf("\nEnter the number of the email to mark as %s (0 to cancel): ", 
            markAsRead ? "read" : "unread");
        int choice = getIntInput();

        if (choice > 0 && choice <= emails.size()) {
            if (markAsRead) {
                emailClient.markEmailAsRead(choice - 1);
                System.out.println("Email marked as read.");
            } else {
                emailClient.markEmailAsUnread(choice - 1);
                System.out.println("Email marked as unread.");
            }
        } else if (choice != 0) {
            System.out.println("Invalid email number.");
        }
    }

    private void deleteEmailInFolder(List<Email> emails) {
        System.out.print("\nEnter the number of the email to delete (0 to cancel): ");
        int choice = getIntInput();

        if (choice > 0 && choice <= emails.size()) {
            emailClient.deleteEmail(choice - 1);
            System.out.println("Email deleted successfully.");
        } else if (choice != 0) {
            System.out.println("Invalid email number.");
        }
    }

    private void moveEmailBetweenFolders(List<Email> emails, String currentFolder) {
        System.out.print("\nEnter the number of the email to move (0 to cancel): ");
        int choice = getIntInput();

        if (choice > 0 && choice <= emails.size()) {
            String targetFolder = currentFolder.equals("inbox") ? "spam" : "inbox";
            emailClient.moveEmailToFolder(choice - 1, targetFolder);
            System.out.println("Email moved to " + targetFolder + ".");
        } else if (choice != 0) {
            System.out.println("Invalid email number.");
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
            System.out.println("Last Login: " + currentUser.getLastLogin().getFormattedDateTime());
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
