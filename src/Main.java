import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Choose mode:");
        System.out.println("1. Start Email Server");
        System.out.println("2. Start Email Client");
        
        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            // Start server only
            EmailServer emailServer = EmailServer.getInstance(true);
            emailServer.start();
            System.out.println("Server is running. Press Ctrl+C to stop.");
            // Keep server running
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } else if (choice.equals("2")) {
            // Initialize client-only mode
            EmailServer emailServer = EmailServer.getInstance(false);
            Menu.setEmailServer(emailServer);
            
            // Initialize authentication
            Auth auth = Auth.getInstance();
            Menu.setAuthInstance(auth);

            // add some dummy users
            auth.register("Mihail", "admin@mihail.ro", "123");
            auth.register("John", "john@mihail.ro", "123");

            Menu.welcomeScreen();
        } else {
            System.out.println("Invalid choice!");
        }
    }
}
