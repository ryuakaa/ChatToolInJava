import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private String login = null;
    private String welcomeMsg = "<////> Hello Client <////>\n" + nl + "> Commands are:" + nl
            + "--> login <username> <password>" + nl + "--> logoff" + nl + "--> quit" + nl + nl;

    public ServerWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // because \n doesn't work properly dnw
    public static String nl = System.getProperty("line.separator");

    private void handleClientSocket() throws IOException, InterruptedException {
        // send and receive data
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        // Standard message from server
        outputStream.write(welcomeMsg.getBytes());

        // connection as long as client wants
        while ((line = reader.readLine()) != null) {

            // split input from client
            String[] tokens = line.split(" ");
            if (tokens != null && tokens.length > 0) {
                // which command?
                String cmd = tokens[0];

                if ("quit".equalsIgnoreCase(cmd)) {
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    // try to login
                    handleLogin(outputStream, tokens);
                } else {
                    String msg = "> Unknown: " + cmd + nl;
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        // do login
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            if (login.equalsIgnoreCase("guest") && password.equals("guest")) {
                String msg = "> Login successful!" + nl;
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("Client: " + this.clientSocket.getRemoteSocketAddress() + " logged in as " + login);
            } else {
                String msg = "> Login failed!" + nl;
                outputStream.write(msg.getBytes());
            }
        }
    }

}