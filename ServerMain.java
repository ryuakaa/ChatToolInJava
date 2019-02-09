import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

class ServerMain {

    // because \n doesn't work properly dnw
    public static String nl = System.getProperty("line.separator");

    public static void main(String[] args) throws InterruptedException { // different!!
        int port = 4222;
        try {
            // initialize Server
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                System.out.println("Waiting for clients...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);

                // create new thread everytime there is another connection
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try{
                            handleClientSocket(clientSocket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }; t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void handleClientSocket(Socket clientSocket) throws IOException, InterruptedException {
        OutputStream outputStream = clientSocket.getOutputStream();

        for (int i = 0; i < 10; i++) {
            outputStream.write(("Time now is: " + new Date() + nl).getBytes());
            Thread.sleep(1000);
        }

        clientSocket.close();
    }
}