import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class EmailClient {
    private Socket socket;

    public EmailClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
    }

    public void connect() {
        System.out.println("Connected to server...");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            // Example of sending a message to the server
            out.println("Hello Server");

            // Read response from server
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 