import java.util.*;
import java.net.*;
import java.io.*;

public class Client {
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int udpBufSize = 1024;

    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <hostAddress>: the address of the server");
      System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(3) <udpPort>: the port number for UDP connection");
      System.exit(-1);
    }
    
    hostAddress = args[0];
    tcpPort = Integer.parseInt(args[1]);
    udpPort = Integer.parseInt(args[2]);

    InetAddress host;
    Socket clientSocket;
    DatagramSocket udpSocket;
    
    BufferedReader tcpIn;
    BufferedWriter tcpOut;
    DatagramPacket packetIn, packetOut;
    byte[] buf = new byte[udpBufSize];
    try {
    	host = InetAddress.getByName(hostAddress);
        clientSocket = new Socket(host, tcpPort);
        udpSocket = new DatagramSocket(udpPort, host);
        tcpIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        tcpOut = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    
    Scanner sc = new Scanner(System.in);
    while(sc.hasNextLine()) {
      String cmd = sc.nextLine();
      String[] tokens = cmd.split(" ");

      if (tokens[0].equals("purchase")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
    	String username, productName, protocol;
    	int quantity;
    	username = tokens[1];
    	productName = tokens[2];
    	quantity = Integer.parseInt(tokens[3]);
    	protocol = tokens[4];
    	
      } else if (tokens[0].equals("cancel")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
      } else if (tokens[0].equals("search")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
      } else if (tokens[0].equals("list")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
      } else {
        System.out.println("ERROR: No such command");
      }
    }
  }
}
