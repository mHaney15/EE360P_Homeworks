import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
	
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
    	localHost = InetAddress.getLocalHost();
		tcpSocket = new ServerSocket(tcpPort, 0, localHost);
		udpSocket = new DatagramSocket(udpPort, localHost);
	    
		// parse the inventory file
		inventory = new Hashtable<String, Integer>();
		List<String> InventoryLines = Files.readAllLines(new File(fileName).toPath());
		for(String line : InventoryLines){
			String[] elements = line.split(" ");
			inventory.put(elements[0], new Integer(elements[1]));
		}
			
		// handle request from clients
		TCPSocketInterpreter tcpSI = server.new TCPSocketInterpreter();
		pool.execute(tcpSI);
		while(true){}
    	
    
		//tcpSocket.close();
		//udpSocket.close();
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
	
	public class TCPSocketInterpreter implements Runnable{
		private class socketHandler implements Runnable{
			Socket socket;
			socketHandler(Socket socket){
				this.socket = socket;
			}
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					BufferedWriter socketOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					String message = socketIn.readLine();
					//Do something with message....
					socketOut.write(message);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {	
				while(!tcpSocket.isClosed()){
					Socket client = tcpSocket.accept();
					socketHandler sH = new socketHandler(client);
					pool.execute(sH);	
				}
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
	}

}



