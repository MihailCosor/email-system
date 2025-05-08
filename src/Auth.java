import java.io.*;
import java.net.*;

// singleton class handling user authentication and server communication
public class Auth {
    // singleton instance
    private static Auth instance;
    // currently authenticated user
    private User currentUser;
    // socket connection to authentication server
    private Socket serverSocket;
    // output stream for sending requests to server
    private ObjectOutputStream out;
    // input stream for receiving responses from server
    private ObjectInputStream in;

    // private constructor for singleton pattern
    private Auth() {
        this.currentUser = null;
        connectToServer();
    }

    // establishes connection to authentication server
    private void connectToServer() {
        try {
            serverSocket = new Socket("localhost", 12345);
            out = new ObjectOutputStream(serverSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(serverSocket.getInputStream());
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }

    // returns singleton instance, creating it if necessary
    public static Auth getInstance() {
        if (instance == null) {
            instance = new Auth();
        }
        return instance;
    }

    // checks if a user is currently logged in
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // returns the currently authenticated user
    public User getCurrentUser() {
        return currentUser;
    }

    // logs out current user and cleans up resources
    public void logout() {
        if (currentUser != null) {
            currentUser.getEmailClient().disconnect();
            currentUser = null;
        }
    }

    // authenticates user with email and password
    public boolean login(String email, String password) {
        try {
            out.writeObject("LOGIN:" + email + ":" + password);
            out.flush();
            
            String response = (String) in.readObject();
            if (response.equals("LOGIN_SUCCESS")) {
                currentUser = (User) in.readObject();
                currentUser.updateLastLogin();
                
                // connect to email server after successful authentication
                if (currentUser.getEmailClient().connect(email)) {
                    System.out.println("Login successful!");
                    return true;
                } else {
                    System.out.println("Failed to connect to email server");
                    currentUser = null;
                    return false;
                }
            } else {
                System.out.println(response.substring("LOGIN_FAILED:".length()));
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
            return false;
        }
    }

    // registers a new user with the system
    public boolean register(String name, String email, String password) {
        try {
            out.writeObject("REGISTER:" + name + ":" + email + ":" + password);
            out.flush();
            
            String response = (String) in.readObject();
            if (response.equals("REGISTER_SUCCESS")) {
                System.out.println("Registration successful!");
                return true;
            } else {
                System.out.println(response.substring("REGISTER_FAILED:".length()));
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error during registration: " + e.getMessage());
            return false;
        }
    }

    // closes all resources when shutting down
    public void close() {
        try {
            logout();
            if (out != null) out.close();
            if (in != null) in.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
    }
}
