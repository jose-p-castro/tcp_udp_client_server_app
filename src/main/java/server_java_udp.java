import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.regex.Pattern;

/**
 * A simple UDP/IP server that sends back lines as they are typed
 * The server port must be passed as a command-line argument
 */
public class server_java_udp {

    private static final int MAX_PACKET_SIZE = 100;
    private DatagramSocket udpSocket;
    private byte[] receiveData = new byte[MAX_PACKET_SIZE];
    private byte[] sendData = new byte[MAX_PACKET_SIZE];


    /**
     * Starts the Server, bindind it to the specified port
     *
     * @param port UDP port binded by the server
     */
    server_java_udp(int port) {
        udpSocket = null;

        try {
            udpSocket = new DatagramSocket(port);
            System.out.println("Created UDP socket at " + udpSocket.getLocalPort());
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Receives a string divided in chunks from a server living at hostname:port and rebuilds it
     *
     * @param socket          The socket to receive the data
     * @param remoteAddr      The address of the client
     * @param remotePort      The port of the client
     * @param lengthOfMessage The length of the message to be received
     * @return The received string
     * @throws IOException If an I/O error occurs
     */
    private String receiveStringInChunks(DatagramSocket socket, InetAddress remoteAddr, int remotePort, int lengthOfMessage) throws IOException {
        int bufferSize = 100;
        int bytesRead;
        byte[] chunkData = new byte[bufferSize];
        StringBuilder receivedData = new StringBuilder();

        while (lengthOfMessage > 0) {
            DatagramPacket chunkPacket = new DatagramPacket(chunkData, chunkData.length, remoteAddr, remotePort);
            socket.receive(chunkPacket);
            bytesRead = chunkPacket.getLength();
            receivedData.append(new String(chunkPacket.getData(), 0, bytesRead));
            lengthOfMessage -= bytesRead;
        }

        return receivedData.toString();
    }

    /**
     * Sends a string divided in chunks to a client living at hostname:port
     *
     * @param socket  The socket to send the data
     * @param address The address of the client
     * @param port    The port of the client
     * @param data    The string to be sent
     * @throws IOException If an I/O error occurs
     */
    private void sendStringInChunks(DatagramSocket socket, InetAddress address, int port, String data) throws IOException {
        int bufferSize = 100;
        int totalLength = data.length();
        int bytesSent = 0;
        int chunkSize = 3;
        byte[] sendData = new byte[bufferSize];
        byte[] chunkSizeMessage = new byte[0];
        chunkSizeMessage = Integer.toString(chunkSize).getBytes(); //send chunk size message to client
        DatagramPacket firstChunkPacket = new DatagramPacket(chunkSizeMessage, chunkSizeMessage.length, address, port);
        socket.send(firstChunkPacket);

        while (bytesSent < totalLength) {
            int endIndex = Math.min(bytesSent + bufferSize, totalLength);
            String chunk = data.substring(bytesSent, endIndex);
            sendData = chunk.getBytes();
            DatagramPacket chunkPacket = new DatagramPacket(sendData, sendData.length, address, port);
            socket.send(chunkPacket);
            bytesSent += chunk.length();
        }
    }

    /**
     * Waits for packets from clients and processes them accordingly, while simultaneously handling errors.
     */
    public void waitPackets() {
        DatagramPacket packetStringSize = null;
        DatagramPacket packetString = null;
        DatagramPacket packetKeyword = null;
        DatagramPacket packetReplyMessage = null;
        DatagramPacket packetSocketProgramming = null;
        DatagramPacket packetReceiveAck = null;
        DatagramPacket ackResponse = null;
        InetAddress remoteAddr = null;
        int remotePort = 0;

        String sizeString = null;
        String receivedString = null;
        String receivedKeyword = null;
        String replyMessage = null;
        String socketProgramming = null;
        String ackLengthResponse = "ACK";

        while (true) {
            try {

                // packet for receiving length message

                packetStringSize = new DatagramPacket(receiveData, receiveData.length);
                udpSocket.receive(packetStringSize);
                remoteAddr = packetStringSize.getAddress();
                remotePort = packetStringSize.getPort();
                sizeString = new String(packetStringSize.getData(), 0, packetStringSize.getLength());
                int lengthOfMessage = Integer.parseInt(sizeString);
                System.out.println("Received " + lengthOfMessage + " bytes from (" + remoteAddr.getHostAddress() + "," + remotePort + ")");

                try {

                    // packet for receiving userInput

                    udpSocket.setSoTimeout(500);
                    if (lengthOfMessage > MAX_PACKET_SIZE) {
                        receivedString = receiveStringInChunks(udpSocket, remoteAddr, remotePort, lengthOfMessage);
                    } else {
                        receiveData = new byte[lengthOfMessage];
                        packetString = new DatagramPacket(receiveData, receiveData.length);
                        udpSocket.receive(packetString);
                        receivedString = new String(packetString.getData(), 0, packetString.getLength());
                    }

                    // packet for server response if first length message size matches userInput size


                    if (lengthOfMessage == receivedString.length()) {
                        sendData = ackLengthResponse.getBytes();
                        ackResponse = new DatagramPacket(sendData, sendData.length, remoteAddr, remotePort);
                        udpSocket.send(ackResponse);
                    }

                    // if the first length message size does not match userInput size, the server will terminate and print an error message

                } catch (SocketTimeoutException e) {
                    System.out.println("Did not receive valid string from client. Terminating!");
                    System.exit(1);
                }

                try {
                    // packet for receiving keyword
                    packetKeyword = new DatagramPacket(receiveData, receiveData.length);
                    udpSocket.receive(packetKeyword);
                    remoteAddr = packetKeyword.getAddress();
                    remotePort = packetKeyword.getPort();
                    receivedKeyword = new String(packetKeyword.getData(), 0, packetKeyword.getLength());
                    sendData = ackLengthResponse.getBytes();
                    ackResponse = new DatagramPacket(sendData, sendData.length, remoteAddr, remotePort);
                    udpSocket.send(ackResponse);
                } catch (SocketTimeoutException e) {
                    System.out.println("Did not receive valid keyword from client. Terminating!");
                    System.exit(1);
                }

                // packet for sending anonymized text

                String index = "\\b" + Pattern.quote(receivedKeyword) + "\\b";
                String modifiedText = receivedString.replaceAll("(?i)" + index, "X".repeat(receivedKeyword.length()));
                sendData = modifiedText.getBytes();
                if (modifiedText.length() > MAX_PACKET_SIZE) {
                    sendStringInChunks(udpSocket, remoteAddr, remotePort, modifiedText);
                } else {
                    packetString = new DatagramPacket(sendData, sendData.length, remoteAddr, remotePort);
                    udpSocket.send(packetString);
                }

                int timesKeywordIsRepeated = Tools.numberOfTimesKeywordIsRepeated(receivedString, receivedKeyword);

                // packet for sending message with number of instances replaced

                replyMessage = "Server has replaced " + timesKeywordIsRepeated + " instances of the keyword '" + receivedKeyword + "'.";
                sendData = replyMessage.getBytes();
                packetReplyMessage = new DatagramPacket(sendData, sendData.length, remoteAddr, remotePort);
                udpSocket.send(packetReplyMessage);

                // packet with number of times keyword is replaced

                String instances = String.valueOf(timesKeywordIsRepeated);
                sendData = instances.getBytes();
                DatagramPacket keywordInstances = new DatagramPacket(sendData, sendData.length, remoteAddr, remotePort);
                udpSocket.send(keywordInstances);

                int numberOfAttempts = 0;

                // loop to print "Socket Programming" for the amount of times the keyword is replaced
                // if the server does not receive an ACK message from the client, it will try to send the message 3 times
                // if the server does not receive an ACK message from the client after 3 attempts, it will terminate

                while (timesKeywordIsRepeated != 0) {
                    try {
                        socketProgramming = "Socket Programming!";
                        sendData = socketProgramming.getBytes();
                        packetSocketProgramming = new DatagramPacket(sendData, sendData.length, remoteAddr, remotePort);
                        udpSocket.send(packetSocketProgramming);
                        udpSocket.setSoTimeout(1000);
                        packetReceiveAck = new DatagramPacket(receiveData, receiveData.length);
                        udpSocket.receive(packetReceiveAck);
                        String ack = new String(packetReceiveAck.getData(), 0, packetReceiveAck.getLength());
                        System.out.println(ack);
                        timesKeywordIsRepeated--;
                        if (timesKeywordIsRepeated == 0) {
                            System.exit(0);
                        }
                    } catch (SocketTimeoutException e) {
                        numberOfAttempts++;
                        if (numberOfAttempts == 3) {
                            System.out.println("Result transmission failed. Terminating!");
                            System.exit(1);
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("I/O error: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    /**
     * Creates a UDP server and waits for client packets
     *
     * @param args The server's port should be passed here
     **/
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java UDPServer <port number>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        server_java_udp echoServer = new server_java_udp(port);

        echoServer.waitPackets();
    }
}