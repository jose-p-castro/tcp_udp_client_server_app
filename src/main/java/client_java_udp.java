import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * A simple UDP/IP client that connects to a UDP server
 * The server's IP and Port must be passed as command-line arguments
 */
public class client_java_udp {

    private static final int MAX_PACKET_SIZE = 100;
    private DatagramSocket udpSocket;
    private byte[] receiveData = new byte[MAX_PACKET_SIZE];
    private byte[] sendData = new byte[MAX_PACKET_SIZE];

    /**
     * Creates a DatagramSocket and sets its reception timeout
     *
     * @param timeout Timeout set for packet reception
     * @throws SocketException If an I/O error occurs
     */
    public client_java_udp(int timeout) {
        try {
            udpSocket = new DatagramSocket();
            udpSocket.setSoTimeout(timeout);
        } catch (SocketException e) {
            System.err.println("Socket error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends a string divided in chunks to a server living at hostname:port
     *
     * @param socket  DatagramSocket used to send the chunks
     * @param address Server's address
     * @param port    Server's port
     * @param data    String to be sent
     * @throws IOException If an I/O error occurs
     */
    private void sendStringInChunks(DatagramSocket socket, InetAddress address, int port, String data) throws IOException {
        int bufferSize = 100;
        int totalLength = data.length();
        int bytesSent = 0;

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
     * Receives a string divided in chunks from a server living at hostname:port and rebuilds it
     *
     * @param socket     DatagramSocket used to receive the chunks
     * @param remoteAddr Server's address
     * @param remotePort Server's port
     * @return The received string
     * @throws IOException If an I/O error occurs
     */
    private String receiveStringInChunks(DatagramSocket socket, InetAddress remoteAddr, int remotePort) throws IOException {
        int bufferSize = 100;
        int bytesRead;
        byte[] chunkData = new byte[bufferSize];
        StringBuilder receivedData = new StringBuilder();

        DatagramPacket chunkPacket;

        do {
            chunkPacket = new DatagramPacket(chunkData, chunkData.length, remoteAddr, remotePort);
            socket.receive(chunkPacket);
            bytesRead = chunkPacket.getLength();
            receivedData.append(new String(chunkPacket.getData()).substring(0, bytesRead));
        } while (chunkPacket.getLength() >= bufferSize);

        return receivedData.toString();
    }


    /**
     * Repeatedly reads a line from terminal, sends it to a server living at hostname:port, and waits for a reply
     *
     * @param hostname  Name of the UDP server
     * @param port      Port binded to the UDP server living at hostname
     * @param userInput String to be sent to the server
     * @param keyword   Keyword to be sent to the server
     */
    public void sendData(String hostname, int port, String userInput, String keyword) {

        try {
            InetAddress address = InetAddress.getByName(hostname);

            for (int attempt = 1; attempt <= 3; attempt++) {
                try {

//            packet for length message

                    int lengthOfMessage = userInput.length();
                    String lengthMessage = String.valueOf(lengthOfMessage);
                    sendData = lengthMessage.getBytes();
                    DatagramPacket packetStringSize = new DatagramPacket(sendData, sendData.length, address, port);
                    udpSocket.send(packetStringSize);
//                    sleep(5000);

//            packet for userInput

                    if (lengthOfMessage > MAX_PACKET_SIZE) {
                        sendStringInChunks(udpSocket, address, port, userInput);
                    } else {
                        sendData = userInput.getBytes();
                        DatagramPacket packetString = new DatagramPacket(sendData, sendData.length, address, port);
                        udpSocket.send(packetString);
                    }

//            packet for server response if first length message size matches userInput size

                    udpSocket.setSoTimeout(1000);
                    DatagramPacket ackLengthResponse = new DatagramPacket(receiveData, receiveData.length);
                    udpSocket.receive(ackLengthResponse);
                    String ackResponse = new String(ackLengthResponse.getData(), 0, ackLengthResponse.getLength());
                    System.out.println(ackResponse);
                    break;
                } catch (SocketTimeoutException e) {
                    if (attempt == 3) {
                        System.out.println("Failed to send string. Terminating!");
                        System.exit(1);
                    }
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
                }

            }

//           packet for keyword, trying 3 times

            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
//                    sleep(5000);
                    sendData = keyword.getBytes();
                    DatagramPacket packetKeyword = new DatagramPacket(sendData, sendData.length, address, port);
                    udpSocket.send(packetKeyword);
                    udpSocket.setSoTimeout(1000);
                    DatagramPacket ackLengthResponse = new DatagramPacket(receiveData, receiveData.length);
                    udpSocket.receive(ackLengthResponse);
                    String ackResponse = new String(ackLengthResponse.getData(), 0, ackLengthResponse.getLength());
                    System.out.println(ackResponse);
                    break;
                } catch (SocketTimeoutException e) {
                    if (attempt == 3) {
                        System.out.println("Failed to send keyword. Terminating!");
                        System.exit(1);
                    }
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
                }
            }

//            packet for userInput with anonymized text

            DatagramPacket packetString = new DatagramPacket(receiveData, receiveData.length);
            udpSocket.receive(packetString);
            if (userInput.length() > MAX_PACKET_SIZE) {
                String received = receiveStringInChunks(udpSocket, address, port);
                System.out.println(received);
            } else {
                String received = new String(packetString.getData(), 0, packetString.getLength());
                System.out.println(received);
            }

//            packet for message with number of instances replaced

            DatagramPacket packetReplyMessage = new DatagramPacket(receiveData, receiveData.length);
            udpSocket.receive(packetReplyMessage);
            String replyMessage = new String(packetReplyMessage.getData(), 0, packetReplyMessage.getLength());
            System.out.println(replyMessage);

//            packet with number of times keyword is replaced

            DatagramPacket packetKeywordInstances = new DatagramPacket(receiveData, receiveData.length);
            udpSocket.receive(packetKeywordInstances);
            String socketReply = new String(packetKeywordInstances.getData(), 0, packetKeywordInstances.getLength());
            int socketNumberOfTimes = Integer.parseInt(socketReply);

//            loop to print "Socket Programming" for set amount of times

            while (socketNumberOfTimes != 0) {
                DatagramPacket packetSocketProgramming = new DatagramPacket(receiveData, receiveData.length);
                udpSocket.receive(packetSocketProgramming);
                String ackMessage = "ACK";
                sendData = ackMessage.getBytes();
                DatagramPacket ack = new DatagramPacket(sendData, sendData.length, address, port);
                udpSocket.send(ack);
                String messageOfSocketProgramming = new String(packetSocketProgramming.getData(), 0, packetSocketProgramming.getLength());
                System.out.println(messageOfSocketProgramming);
                socketNumberOfTimes--;

            }
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout reached: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    /**
     * Closes the DatagramSocket
     */
    public void close() {
        udpSocket.close();
    }

    /**
     * Creates a UDP client and connects to a UDP server
     *
     * @param args The server's name and port should be passed here
     * @throws IOException If an I/O error occurs
     */
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

        client_java_udp echoClient = new client_java_udp(1000);

        echoClient.sendData(serverIP, serverPort, userInput, keyword);

        echoClient.close();
    }
}