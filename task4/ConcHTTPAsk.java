import java.net.*;
import java.io.*;
import tcpclient.TCPClient;

public class ConcHTTPAsk {
    private static int PORT = 0;
    private static ServerSocket serverSocket = null;

    public static void main( String[] args) {
        PORT = Integer.parseInt(args[0]);
        try{
            serverSocket = new ServerSocket(PORT);
            System.out.println("waiting for client...");
        }catch (IOException e) {
            System.out.println("unable to connect");
            System.exit(1);
        }
        while(true){
            try {
               Socket link = serverSocket.accept();
               System.out.println("connection established");
               startConnection(link);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void startConnection(Socket link) {
        ClientHandler handler = new ClientHandler(link);
        Thread threadHandler = new Thread(handler);
        threadHandler.start();
    }

}


