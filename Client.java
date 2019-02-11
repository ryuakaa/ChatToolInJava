import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;

    public Client(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 4222);

        if (!client.connect()) {
            System.err.println("Connection failed");
        } else {
            System.out.println("Connection successful");
            if (client.login("tim", "tim")) {
                // successfully logged in
                System.out.println("Login successful");
            } else {
                System.err.println("Login failed");
            }
        }
    }

    private boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + nl;
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        // System.out.println("Response: " + response);

        if ("valid login".equalsIgnoreCase(response)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean connect() throws IOException {
        try {
            // connect to server
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // because \n doesn't work properly dnw
    public static String nl = System.getProperty("line.separator");

}