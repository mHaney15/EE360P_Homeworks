import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.InetAddress;

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
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    // parse the inventory file

    // TODO: handle request from clients
  }
}
