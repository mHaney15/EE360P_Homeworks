import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;
import java.nio.file.Files;

public class Server {
	static final int udpBufSize = 1024;
	static Hashtable<String, Integer> inventory;
	static List<Order> orderHistory; 
	static Executor pool = Executors.newWorkStealingPool();
	static AtomicInteger currentOrderID = new AtomicInteger(1);
	static ServerSocket tcpSocket;
    static DatagramSocket udpSocket;
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
    try {
    	Server server = new Server();
    	localHost = InetAddress.getByName("127.0.0.1");
		tcpSocket = new ServerSocket(tcpPort, 0, localHost);
		udpSocket = new DatagramSocket(udpPort);
	    
		// parse the inventory file
		inventory = new Hashtable<String, Integer>();
		List<String> InventoryLines = Files.readAllLines(new File(fileName).toPath());
		for(String line : InventoryLines){
			String[] elements = line.split(" ");
			if(elements.length == 2)
				inventory.put(elements[0], new Integer(elements[1]));
		}
			
		// handle request from clients
		TCPSocketHandler tcpSH = server.new TCPSocketHandler();
		UDPSocketHandler udpSH = server.new UDPSocketHandler();
		pool.execute(tcpSH);
		pool.execute(udpSH);
		while(true){}
    
    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
	public class  Order {
		int orderID;
		String username;
		String productName;
		int quantity;
		
		Order(int orderID, String username, String productName, int quantity){
			this.orderID = orderID;
			this.username = username;
			this.productName = productName;
			this.quantity = quantity;
		}

		public int getOrderID() {
			return orderID;
		}

		public String getUsername() {
			return username;
		}
		public String getProductName() {
			return productName;
		}

		public int getQuantity() {
			return quantity;
		}
	}
	
	public class TCPSocketHandler implements Runnable{
		private class TCPsocketInterpreter implements Runnable{
			Socket socket;
			TCPsocketInterpreter(Socket socket){
				this.socket = socket;
			}
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					BufferedWriter socketOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					String messageOut = parseMessage(socketIn.readLine());
					socketOut.write(messageOut+"\n");
					socketOut.flush();
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void run() { //TCPSocketInterpreter
			// TODO Auto-generated method stub
			try {	
				while(!tcpSocket.isClosed()){
					Socket client = tcpSocket.accept();
					TCPsocketInterpreter sH = new TCPsocketInterpreter(client);
					pool.execute(sH);	
				}
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
	}

	public class UDPSocketHandler implements Runnable{
		private class UDPDatagramInterpreter implements Runnable{
			DatagramPacket dataRecd;
			DatagramPacket dataSend;
			
			UDPDatagramInterpreter(DatagramPacket recd){
				this.dataRecd = recd;
			}
			@Override
			public void run() {
				String messageOut = parseMessage(new String(dataRecd.getData()).trim());
				byte[] messageAsBytes = messageOut.getBytes();
				this.dataSend = new DatagramPacket(messageAsBytes, messageAsBytes.length, dataRecd.getAddress(), dataRecd.getPort());
				try {
					udpSocket.send(dataSend);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!udpSocket.isClosed()){
				byte[] buf = new byte[udpBufSize];
				DatagramPacket recd = new DatagramPacket(buf, buf.length);
				try {
					udpSocket.receive(recd);
					UDPDatagramInterpreter udpDI = new UDPDatagramInterpreter(recd);
					pool.execute(udpDI);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

	// TODO: Implement this.
	String parseMessage(String msg){
		return msg;
	}
}



