import java.net.*;
import java.io.*;
import tcpclient.TCPClient;


public class HTTPAsk {
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
                clientHandler();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void clientHandler() throws IOException {
        boolean error400 = false;
        boolean error404 = false;
        String errorMsg = "Error Message: ";
        Socket connectionSocket = serverSocket.accept();
        System.out.println("connection established");
        //Set timer
        connectionSocket.setSoTimeout(5000);
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        String reader = inFromClient.readLine();
        System.out.println(reader);

        String[] info = getParameters(reader, error404, error400, errorMsg);
        String hostname = "";
        int port2;
        String msg = "";
        String httpResponse = "";

        if (!error404 && !error400) {
            try {
                TCPClient tcpClient = new TCPClient();
                if (info != null) {
                    String response = tcpClient.askServer(info[0], Integer.parseInt(info[1]), info[2]);
                    httpResponse = "HTTP/1.1 200 OK request\r\n\r\n" + response;
                } else {
                    errorMsg += "Wrong Paramaters!";
                    httpResponse = "HTTP/1.1 404 not found\r\n\r\n" + errorMsg;
                }
            } catch (IOException e) {
                errorMsg += "Wrong Parameters!";
                error400 = true;
            }
        } else {
            if (error400) {
                httpResponse = "HTTP/1.1 400 bad request\r\n\r\n" + errorMsg;
            } else {
                httpResponse = "HTTP/1.1 404 not found\r\n\r\n" + errorMsg;
            }
        }
        PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);
        outToClient.println(httpResponse);

        try {
            System.out.println("Closing Connection...");
            connectionSocket.close();
        } catch (SocketTimeoutException e) {
            System.out.println("Closing Connection...");
            connectionSocket.close();
        } catch (IOException e) {
            System.out.println("unable to close connection...");
            System.exit(1);
        }
    }

    public static String[] getParameters(String req, boolean error404, boolean error400, String errorMsg) throws IOException {
       try {
           String[] mainComponents = req.split(" ");
           String[] divider1 = new String[2];
           String[] divider2 = new String[3];
           String[] parameters = new String[3];
           // check if parameters and syntax is correct otherwise change boolean for errors.
           if (req.contains("/ask?") && req.contains("hostname") && req.contains("port")) {
               divider1 = mainComponents[1].split("/ask?");
               divider2 = divider1[1].split("&");

               // save hostname in parameters[0], port in [1] and msg in [2] if there
               parameters[0] = divider2[0].split("=")[1];
               parameters[1] = divider2[1].split("=")[1];
               if (divider2.length > 2) {
                   parameters[2] = divider2[2].split("=")[1];
               }

               System.out.println("paramaters: " + parameters[0] + " , " + parameters[1] + " , " + parameters[2]);

               return parameters;
           } else {
               errorMsg += "404 Missing Parameters!";
               error404 = true;
               return null;
           }
       } catch(RuntimeException e)
       {
           errorMsg += "400 Wrong Format!";
           error400 = true;
           return null;
       }
    }
}


