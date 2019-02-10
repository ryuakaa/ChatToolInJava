import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private String login = null;
    private final Server server;
    private InputStream inputStream;
    private OutputStream outputStream;

    private String welcomeMsg = nl + "<////> Hello Client <////>\n" + nl + "> Commands are:" + nl
            + "--> login <username> <password>" + nl + "--> logoff" + nl + "--> quit" + nl + nl;

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
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

    public String getLogin() {
        return login;
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        // send and receive data
        this.inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

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

                if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
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

    private void handleLogoff() throws IOException {
        // logoff user
        List<ServerWorker> workerList = server.getWorkerList();
        // send other online users current status
        String onlineMsg = "> -" + login + " left" + nl;
        for (ServerWorker worker : workerList) {
            if (!login.equals(worker.getLogin())) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        // do login
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];
            // check acc
            if ((login.equalsIgnoreCase("tom") && password.equals("tom"))
                    || (login.equalsIgnoreCase("tim") && password.equals("tim"))) {

                // valid login
                String msg = "> Login successful!" + nl;
                outputStream.write(msg.getBytes());
                this.login = login;
                // Server output
                System.out.println("Client: " + this.clientSocket.getRemoteSocketAddress() + " logged in as " + login);

                List<ServerWorker> workerList = server.getWorkerList();

                // send current user all other online logins
                for (ServerWorker worker : workerList) {
                    // no msgs of yourself being online
                    if (worker.getLogin() != null) {
                        if (!login.equals(worker.getLogin())) {
                            String msg2 = "> " + worker.getLogin() + " is online" + nl;
                            send(msg2);
                        }
                    }
                }
                // send other online users current status
                String onlineMsg = "> +" + login + " joined" + nl;
                for (ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }

            } else {
                String msg = "> Login failed!" + nl;
                outputStream.write(msg.getBytes());
            }
        }
    }

    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }
}