import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // display initial mode selection prompt
        System.out.println("Choose mode:");
        System.out.println("1. Start Email Server");
        System.out.println("2. Start Email Client");
        
        // get user input for mode selection
        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            Logger.getInstance().log("Server mode");

            // initialize and start the email server in singleton mode
            EmailServer server = EmailServer.getInstance();
            server.start();
            System.out.println("Server is running. Press Ctrl+C to stop.");
            
            // keep server running until interrupted
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

            // cleanup server resources on exit
            server.stop();
        } else if (choice.equals("2")) {
            Logger.getInstance().log("Client mode");
            // launch the email client interface
            Menu menu = new Menu();
            menu.showMainMenu();
        } else {
            System.out.println("Invalid choice!");
        }

        // cleanup scanner resource
        scanner.close();
    }
}
