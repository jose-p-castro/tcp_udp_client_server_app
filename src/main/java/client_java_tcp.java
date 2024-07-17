import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A simple TCP/IP client that connects to a TCP server
 * The server's IP and Port must be passed as command-line arguments
 */
public class client_java_tcp {

    private Socket socket;

    /**
     * Connects to a TCP server living at host:port
     *
     * @param hostname The name of the TCP server to which the client is going to connect
     * @param port     The port binded to the server living at hostname
     */
    public client_java_tcp(String hostname, int port) {

        socket = null;
        if (port < 1024 || port > 49151) {
            System.err.println("Invalid port number. Terminating!");
            System.exit(1);
        }
        try {
            socket = new Socket(hostname, port);
        } catch (UnknownHostException e) {
            System.err.println("Could not connect to server. Terminating!");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Could not connect to server. Terminating!");
            System.exit(1);
        }
    }

    /**
     * Repeatedly reads a line from terminal, sends it to the server, and waits for a reply
     */
    public void sendData(String userInput, String keyword) {

        try {

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(userInput);
            out.println(keyword);
            System.out.println(in.readLine());
            System.out.println(in.readLine());
            String serverOutput;
            while ((serverOutput = in.readLine()) != null) {
                System.out.println(serverOutput);
            }

            System.out.println("Closing client...");
            in.close();
            out.close();
            socket.close();

        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    /**
     * Creates a TCP client and connects to a TCP server
     *
     * @param args The server's name and port should be passed here
     * @throws IOException If an I/O error occurs
     **/
    public static void main(String[] args) throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter server name or IP address:");
        String serverIP = stdIn.readLine();
        System.out.println("Enter port:");
        int serverPort = Integer.parseInt(stdIn.readLine());
        System.out.println("Enter string");
        String userInput = stdIn.readLine();
        System.out.println("Enter keyword:");
        String keyword = stdIn.readLine();

        client_java_tcp echoClient = new client_java_tcp(serverIP, serverPort);

        echoClient.sendData(userInput, keyword);
    }
}