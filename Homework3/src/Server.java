import java.util.*;
import java.net.*;
import java.nio.file.Files;
import java.io.*;


public class Server {
  public static void main (String[] args) {
    int tcpPort;
    int udpPort;
    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(2) <udpPort>: the port number for UDP connection");
      System.out.println("\t(3) <file>: the file of inventory");

      System.exit(-1);
    }
    InetAddress localHost;
    tcpPort = Integer.parseInt(args[0]);
    udpPort = Integer.parseInt(args[1]);
    String fileName = args[2];
    ServerSocket tcpSocket;
    DatagramSocket udpSocket;
    try {
    	localHost = InetAddress.getLocalHost();
		tcpSocket = new ServerSocket(tcpPort, 0, localHost);
		udpSocket = new DatagramSocket(udpPort, localHost);
	    
		// parse the inventory file
		Hashtable<String, Integer> inventory = new Hashtable<String, Integer>();
		List<String> InventoryLines = Files.readAllLines(new File(fileName).toPath());
		for(String line : InventoryLines){
			String[] elements = line.split(" ");
			inventory.put(elements[0], new Integer(elements[1]));
		}
			
		
		// TODO: handle request from clients
    
    	
    	
    
		tcpSocket.close();
		udpSocket.close();
    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
}
