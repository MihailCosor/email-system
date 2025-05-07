import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Auth {
    private static Auth instance;
    private User user;
    private boolean isLoggedIn;
    private Socket serverSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private Auth() {
        this.user = null;
        this.isLoggedIn = false;
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
        return isLoggedIn;
    }

    public User getCurrentUser() {
        return user;
    }

    public void setCurrentUser(User user) {
        this.user = user;
        this.isLoggedIn = true;
    }

    public void logout() {
        this.user = null;
        this.isLoggedIn = false;
    }

    public boolean login(String email, String password) {
        try {
            out.writeObject("LOGIN:" + email + ":" + password);
            out.flush();
            
            String response = (String) in.readObject();
            if (response.startsWith("LOGIN_SUCCESS:")) {
                String name = response.substring("LOGIN_SUCCESS:".length());
                this.user = new User(name, email, password);
                this.isLoggedIn = true;
                System.out.println("Login successful!");
                return true;
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
            if (out != null) out.close();
            if (in != null) in.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
