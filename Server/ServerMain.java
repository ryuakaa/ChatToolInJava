package Server;

class ServerMain {

    // because \n doesn't work properly dnw
    // public static String nl = System.getProperty("line.separator");

    public static void main(String[] args) throws InterruptedException { // different!!

        int port = 4222;
        Server server = new Server(port);
        server.start();

    }

}