package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private InputStream inputStream;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();

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
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    // split only first two
                    String[] tokensMsg = line.split(" ", 3);
                    handleMessage(tokensMsg);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    handleLeave(tokens);
                } else {
                    String msg = "> Unknown: " + cmd + nl;
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    private void handleLeave(String[] tokens) {
        // leave a topic
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    private boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        // join a topic
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    // format: msg login body
    // format: msg topic body
    private void handleMessage(String[] tokens) throws IOException {
        // send messages
        String sendTo = tokens[1];
        String body = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';

        // send to worker which matches the sendTo
        List<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList) {
            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)) {
                    String outMsg = "> msg " + sendTo + ":" + login + "> " + body + nl;
                    worker.send(outMsg);
                }
            } else if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                String outMsg = login + "> " + body + nl;
                worker.send(outMsg);
            }
        }
    }

    private void handleLogoff() throws IOException {
        // logoff user
        server.removeWorker(this);
        List<ServerWorker> workerList = server.getWorkerList();
        // send other online users current status
        String onlineMsg = "offline " + login + nl;
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
                String msg = "valid login" + nl;
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
                            String msg2 = "online " + worker.getLogin() + nl;
                            send(msg2);
                        }
                    }
                }
                // send other online users current status
                String onlineMsg = "online " + login + nl;
                for (ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }

            } else {
                String msg = "bad login" + nl;
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for " + login);
            }
        }
    }

    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }
}