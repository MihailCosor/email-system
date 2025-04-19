import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Menu menu = Menu.getInstance();
        menu.display();

        // Initialize EmailServer and EmailClient
        try {
            EmailServer server = new EmailServer(12345);
            new Thread(() -> server.start()).start();

            EmailClient client = new EmailClient("localhost", 12345);
            client.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}