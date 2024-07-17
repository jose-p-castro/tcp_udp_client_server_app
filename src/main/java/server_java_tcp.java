import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;

/**
 * A simple TCP/IP server that sends back lines as they are typed
 * The server port must be passed as a command-line argument
 */
public class server_java_tcp {

    private ServerSocket serverSocket;

    /**
     * Starts the Server, bindind it to the specified port
     *
     * @param port The port binded to the server at localhost
     */
    public server_java_tcp(int port) {
        serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port: " + port);

        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Waits for a client connection and sends back lines as they are typed
     */
    public void waitConnections() {
        Socket clientSocket = null;

        while (true) {
            try {
                clientSocket = serverSocket.accept();

                System.out.println("Connected to " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String userInput = in.readLine();
                String keyword = in.readLine();
                String ouptutString = "Socket Programming";
                if ((userInput != null && keyword != null)) {
                    int timesKeywordIsRepeated = Tools.numberOfTimesKeywordIsRepeated(userInput, keyword);
                    String index = "\\b" + Pattern.quote(keyword) + "\\b";
                    String modifiedText = userInput.replaceAll("(?i)" + index, "X".repeat(keyword.length()));
                    out.println(modifiedText);
                    out.println("Server has replaced " + timesKeywordIsRepeated + " instances of the keyword '" + keyword + "'.");
                    while (timesKeywordIsRepeated != 0) {
                        out.println(ouptutString);
                        timesKeywordIsRepeated--;
                    }
                }
                System.out.println("Client exiting...");
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Server exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a TCP server and waits for client connections
     *
     * @param args The server's port should be passed here
     **/
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        server_java_tcp echoServer = new server_java_tcp(port);

        echoServer.waitConnections();
    }
}