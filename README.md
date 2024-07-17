# COMPUTATIONAL SYSTEMS AND NETWORKS

## PROJECT DESCRIPTION

This project is an implementation of a client-server application that allows a user to send a message and a keyword to a
server and receive an anonymized response.

## ARCHITECTURE

A client-server application was developed using the TCP and UDP protocol. In both protocols, the server is
responsible for receiving the messages from the client and processing them. The client is responsible for sending the
messages and the keywords to the server and receiving the anonymized response.

- ### TCP Protocol

In the TCP protocol, the server creates a socket and binds it to a specific port. Then, it listens for incoming
connections from clients. When a client connects to the server, the server creates a new thread to handle the client's
request. The client asks the user for a message and a keyword and sends them to the server. The server receives the
message and the keyword from the client and processes them. Then, it sends the anonymized response to the client, the
number of times the keyword was replaced and the message "Socket programming" as many times as the keyword was replaced.
Finally, the server closes the connection with the client.

- ### UDP Protocol

This application uses the User Datagram Protocol (UDP) for communication between the client and server. UDP is a
connectionless protocol, which means it does not establish a connection before sending data and does not guarantee the
delivery of packets. Message Chunking: To handle messages larger than the maximum packet size, the application breaks
down large messages into smaller chunks. Each chunk is sent in a separate UDP packet. Acknowledgements (ACKs): After
receiving each chunk or message, the receiver sends an ACK packet back to the sender to confirm receipt. Timeout and
Retransmission: The sender waits for an ACK for a certain period. If the ACK is not received (indicating packet loss or
delay), the sender retransmits the packet. Keyword Anonymization: Upon receiving a message, the server searches for
instances of the specified keyword and replaces them with an X for each character before sending the message back to the
client.

## INSTRUCTIONS

To run the application, you need to open two terminals, one for the server and another for the client.

- ### Server

To run the server, you need to execute the following command:
```java server_java_tcp.java``` (for the TCP protocol) or ```java server_java_udp.java``` (for the UDP protocol). In
either case you will need to type a valid port number to start the server at the end of the command.
Example: ```java server_java_tcp.java 4555``` or ```java server_java_udp.java 4555```

- ### Client

To run the client, you need to execute the following command:
```java client_java_tcp.java``` (for the TCP protocol) or ```java client_java_udp.java``` (for the UDP protocol) and
follow the instructions that appear on the terminal (Enter the server's IP address, the server's port number, the
message and the keyword).

## REFERENCES

Nogueira, Luís (2021). "T4 - Introdução às redes de computadores (Parte 1)". ISEP. Porto.
Nogueira, Luís (2021). "T6 - Introdução às redes de computadores (Parte 2)". ISEP. Porto.
Nogueira, Luís (2021). "T7 - Introdução às redes de computadores (Parte 3)". ISEP. Porto.
This project was based on the initial code provided by the teacher and changes were made to implement the required
functionalities.