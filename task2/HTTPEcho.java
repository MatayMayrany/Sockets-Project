import java.net.*;
import java.io.*;

public class HTTPEcho {
    private static int PORT = 0;
    private static ServerSocket serverSocket = null;
    private static Socket connectionSocket = null;
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
                clientHandler();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void clientHandler() throws IOException{
        connectionSocket = serverSocket.accept();
        System.out.println("connection established");
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        String reader = inFromClient.readLine();
        StringBuilder builder = new StringBuilder();
        while(reader!=null && !reader.isEmpty()){
            builder.append(reader);
            builder.append("\r\n");
            reader = inFromClient.readLine();
        }
        PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);
        outToClient.print("HTTP/1.1 200 OK");
        outToClient.print("Content-Type: text/html");
        outToClient.println("\r\n");
        outToClient.println(builder.toString());
        try{
            System.out.println("Closing Connection...");
            connectionSocket.close();
        }
        catch (IOException e) {
            System.out.println("unable to close connection...");
            System.exit(1);
        }
    }
}

