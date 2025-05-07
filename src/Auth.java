import java.io.*;
import java.net.*;

public class Auth {
    private static Auth instance;
    private User currentUser;
    private Socket serverSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private Auth() {
        this.currentUser = null;
        connectToServer();
    }

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

    public static Auth getInstance() {
        if (instance == null) {
            instance = new Auth();
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
        try {
            // if (out != null) out.close();
            // if (in != null) in.close();
            // if (serverSocket != null) serverSocket.close();
        // } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean login(String email, String password) {
        try {
            out.writeObject("LOGIN:" + email + ":" + password);
            out.flush();
            
            String response = (String) in.readObject();
            if (response.equals("LOGIN_SUCCESS")) {
                currentUser = (User) in.readObject();
                currentUser.updateLastLogin();
                
                // Connect to email server
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

    public void close() {
        try {
            // if (out != null) out.close();
            // if (in != null) in.close();
            // if (serverSocket != null) serverSocket.close();
        // } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
