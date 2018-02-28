import java.net.*;
import java.io.*;
import tcpclient.TCPClient;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {
    public final Socket clientSocket;
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        handle();
    }

    public void handle(){
        String reader = "";
        boolean error400 = false;
        boolean error404 = false;
        String[] info = new String[3];
        String errorMsg = "Error Message: ";
        try{
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            reader = inFromClient.readLine();
            System.out.println(reader);
            info = getParameters(reader, error404, error400, errorMsg);
        } catch(IOException ioe){
            ioe.printStackTrace();
        }

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
                errorMsg += "400 Bad Request!";
                error400 = true;
            }
        } else {
            if (error400) {
                httpResponse = "HTTP/1.1 400 bad request\r\n\r\n" + errorMsg;
            } else {
                httpResponse = "HTTP/1.1 404 not found\r\n\r\n" + errorMsg;
            }
        }
        try {
            PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream(), true);
            outToClient.println(httpResponse);
            System.out.println("Closing Connection...");
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("unable to close connection...");
            System.exit(1);
        }
    }

    public static String[] getParameters (String req, boolean error404, boolean error400, String errorMsg) throws IOException {
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