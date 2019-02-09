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
            System.out.println("Waiting for clients...");
            
            // server runs until killed
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);

                // create new thread for every connection
                ServerWorker worker = new ServerWorker(clientSocket);
                worker.start();
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    
}