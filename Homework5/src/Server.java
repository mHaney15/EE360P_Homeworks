import java.io.*;
import java.net.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;
import java.nio.file.Files;

public class Server {
    static Hashtable<String, Integer> inventory;
    static Vector<Order> orderHistory;
    static AtomicInteger currentOrderID = new AtomicInteger(1); //Thread-safe.
    static ReentrantLock lock = new ReentrantLock();
    static Condition checkCS = lock.newCondition();
    static Server server = new Server();
    static Executor pool = Executors.newWorkStealingPool();
    static Vector<TimeStamp> requestQueue;
	static Integer myID;
	static Integer numServers;
	static String inventoryPath;
	
	static String[] ipA;
	static int[] port;
	static Integer V[];
	
    public static void main (String[] args) {
	Scanner sc = new Scanner(System.in);
    myID = sc.nextInt() - 1;
    numServers = sc.nextInt();
    inventoryPath = sc.next();
    V = new Integer[numServers];	//direct dependency clock
    ServerSocket myServer = null;
    requestQueue = new Vector<TimeStamp>();
    
    ipA = new String[numServers];
    port = new int[numServers];
    
    for (int i = 0; i < numServers; i++) {
    	// initialize V, and host addresses
    	V[i] = 0;
    	String[] line = sc.next().split(":");
    	ipA[i] = line[0];
    	port[i] = Integer.parseInt(line[1]);
    }

    InetAddress myAddress;
	try {
		myAddress = InetAddress.getByName(ipA[myID]);
		myServer = new ServerSocket(port[myID], 0, myAddress);
		System.out.println("\tServer Socket "+(myID+1)+" created Successfully! IP:"+ipA[myID]+" Port:"+port[myID]+".");
	} catch (UnknownHostException e1) {
		e1.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}

//	System.out.println("\tAttempting to connect to other servers...");
//    boolean serversConnected = false;
//    while(!serversConnected){
//    	int numConnected = 0;
//	    for (int i = 0; i < numServers; i++) {
//	    	if((Processes[i] == null) && (i != myID)){
//		    	try {
//					InetAddress address = InetAddress.getByName(ipA[i]);			    		
//			    	Socket server_i = new Socket(address, port[i]);
//			    	Processes[i] = server.new Connector(server_i);
//			    	System.out.println("\tSocket to Server "+(i+1)+" created Successfully! IP:"+ipA[i]+" Port:"+port[i]+".");			    	
//		    	} catch (UnknownHostException e) {
//				} catch (IOException e) {
//				}
//	    	}
//	    	else {numConnected++;}
//	    }
//	    if(numConnected == numServers)
//	    	serversConnected = true;
//	    else serversConnected = false;
//    }
    
    
    //parse the inventory file
    System.out.println("\tAttempting to parse inventory file...");
    inventory = new Hashtable<String, Integer>();
	orderHistory = new Vector<Order>();
	try {
		List<String> InventoryLines;
		InventoryLines = Files.readAllLines(new File(inventoryPath).toPath());	
		for(String line : InventoryLines){
			String[] elements = line.split(" ");
			if(elements.length == 2)
				inventory.put(elements[0], new Integer(elements[1]));
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
	String list= "";
	for(String key : inventory.keySet()){
			list = list + "\t" + key + "\t\t" + inventory.get(key) + "\n";
	}
	System.out.println("\n\tITEM:\t\tSTOCK:");
	System.out.print(list);
	
    //handle request from client
		
	while(!myServer.isClosed()){
		try {
			System.out.println("\n\tAwaiting socket message...");
			Socket socket = myServer.accept();
			BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter socketOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			String line = socketIn.readLine();			
			//"request clk pid"
			//"ack clk pid"
			//"release clk pid:clk pid of queued :LINE TO EXECUTE"
			if(line.startsWith("request")){
				String[] tokens = line.split(" ");
				TimeStamp ts = server.new TimeStamp(Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]));
				requestQueue.add(ts);
				V[ts.pid] = max(V[ts.pid], ts.clk);
				V[myID] = max(V[myID], ts.clk) + 1;
				TimeStamp myTS = server.new TimeStamp(V[myID], myID);
				System.out.println("\tRequest received from server "+ts.pid+": "+ts);
				try{
					Connector responder = server.new Connector(ipA[ts.pid], port[ts.pid]);
					responder.write("ack "+myTS.toString()+"\n");
					System.out.println("\tSending Acknowledgement to server "+ts.pid+".");
					responder.close();
				}
				catch(IOException e){
					System.out.println("\tServer "+ts.pid+" died. Removing from set...\n");
					V[ts.pid] = Integer.MAX_VALUE;
				}
				finally{socket.close();}
			}
			else if(line.startsWith("ack")){
				String[] tokens = line.split(" ");
				TimeStamp ts = server.new TimeStamp(Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]));
				try{
					lock.lock();
					V[ts.pid] = max(V[ts.pid], ts.clk);
					V[myID] = max(V[myID], ts.clk) + 1;
					socket.close();
					System.out.println("\tAcknowledgement received from server "+ts.pid+": "+ts);
					checkCS.signalAll();}
				finally{lock.unlock();}
			}
			else if(line.startsWith("release")){
				String[] parts = line.split(":");
				String[] ctstokens = parts[0].split(" "); //"release" & message timestamp
				String[] qtstokens = parts[1].split(" "); //queued task completed timestamp
				TimeStamp msgTS = server.new TimeStamp(Integer.parseInt(ctstokens[1]),Integer.parseInt(ctstokens[2]));
				TimeStamp qTS = server.new TimeStamp(Integer.parseInt(qtstokens[0]),Integer.parseInt(qtstokens[1]));
				V[msgTS.pid] = max(V[msgTS.pid], msgTS.clk);
				V[myID] = max(V[myID], msgTS.clk) + 1;
				System.out.println("\tRelease received from server "+qTS.pid+".");
				parseAndExecute(parts[2]);
				requestQueue.remove(qTS);
				socket.close();
				try{lock.lock();
				checkCS.signalAll();}
				finally{lock.unlock();}
				System.out.println("\tExecuted: \""+parts[2]+"\"");
			}
			else{
				
				V[myID]++;
				TimeStamp ts = server.new TimeStamp(V[myID], myID);
				requestQueue.add(ts);
				for(int  id = 0; id < numServers; id++){
					if((V[id] < Integer.MAX_VALUE) && (id != myID)){
						try{
							System.out.println("\tSending Request to server "+id+".");
							Connector responder = server.new Connector(ipA[id], port[id]);
							responder.write("request "+ts.toString()+"\n");
							responder.close();
						}
						catch(IOException e){
							System.out.println("\tServer "+id+" died. Removing from set...\n");
							V[id] = Integer.MAX_VALUE;
							removeProcessTimeStamps(id);
						}
					}
				}
				System.out.println("\tSpawning thread to handle client request...");
				Runnable CTH = server.new ClientTaskHandler(socket, socketOut, socketIn, ts, line);
				pool.execute(CTH);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	sc.close();

	
  }

  public class Connector{
	  Socket socket;
	  BufferedWriter out;
	  BufferedReader in;
	  Connector(String ip, Integer port) throws IOException{
		  InetAddress address= InetAddress.getByName(ip);
		  socket = new Socket(address, port);
		  out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		  in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	  }
	  
	  void write(String message) throws IOException{
		  out.write(message);
		  out.flush();
	  }
	 
	  void close() throws IOException{
		  in.close();
		  out.close();
		  socket.close();
	  }
  }
  
  public class TimeStamp{
	  Integer clk;
	  Integer pid;
	  TimeStamp(Integer c, Integer p){
		  clk = c;
		  pid = p;
	  }
	  public boolean lessThan(TimeStamp other){
		  return (this.clk < other.clk)||((this.clk == other.clk)&&(this.pid < other.pid));
	  }
	  public String toString(){return clk+" "+pid;}
	  
	  @Override
	  public boolean equals(Object obj){
		  if(!(obj instanceof TimeStamp)){return false;}
		  TimeStamp other = (TimeStamp)obj;
		  return ((other.clk == this.clk)&&(other.pid == this.pid));
	  }
  }
  
  public class Order implements Comparable<Order>{
		Integer orderID;
		String username;
		String productName;
		Integer quantity;
		
		Order(Integer orderID, String username, String productName, Integer quantity){
			this.orderID = orderID;
			this.username = username;
			this.productName = productName;
			this.quantity = quantity;
		}

		public Integer getOrderID() {
			return orderID;
		}

		public String getUsername() {
			return username;
		}
		public String getProductName() {
			return productName;
		}

		public Integer getQuantity() {
			return quantity;
		}
		public String toStringNoName(){
			String OString = orderID+", "+productName+", "+quantity;
			return OString;
		}
	
		public String toString(){
			return orderID+" "+username+" "+productName+" "+quantity;
		}
		
		Order copy(){
			return new Order(this.orderID, this.username, this.productName, this.quantity);
		}

		@Override
		public int compareTo(Order o) {
			// TODO Auto-generated method stub
			return this.orderID - o.getOrderID();
		}
	}
  
  public class ClientTaskHandler implements Runnable{
	  Socket client;
	  BufferedWriter out;
	  BufferedReader in;
	  TimeStamp ts;
	  String task;
	  
	  ClientTaskHandler( Socket client, BufferedWriter out, BufferedReader in, TimeStamp ts, String task){
		  this.client = client;
		  this.out = out;
		  this.in = in;
		  this.ts = ts;
		  this.task = task;
	  }
	  
	@Override
	public void run() {
		lock.lock();
		while(!okayToExecute()){
			try {
				checkCS.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String result = parseAndExecute(task);
		requestQueue.remove(ts);
		System.out.println("\tExecuted: "+task);
		try {
			out.write(result);
			out.flush();
			out.close();
			in.close();
			client.close();
			for(int id = 0; id < numServers; id++){
				if((V[id] < Integer.MAX_VALUE)&&(id != myID)){
					TimeStamp tempStamp = new TimeStamp(V[id], id);//timestamp for message
					try{
						System.out.println("\tSending release to server "+id+".");
						Connector responder = server.new Connector(ipA[id], port[id]);
						responder.write("release "+tempStamp.toString()+":"+ts.toString()+":"+task+"\n");
						responder.close();
					}
					catch(IOException e){
						V[id] = Integer.MAX_VALUE;
						removeProcessTimeStamps(id);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			checkCS.signalAll();
			lock.unlock();
		}
	}
	
	private boolean okayToExecute(){
		
		for(int id = 0; id < numServers; id++){
			if(V[id] == Integer.MAX_VALUE)
				removeProcessTimeStamps(id);
		}
		for(TimeStamp stamp: requestQueue){
			if(stamp.lessThan(ts)){return false;}
		}
		for(int id = 0; id < numServers; id++){
			if(id != ts.pid){
				TimeStamp tempStamp = new TimeStamp(V[id], id);
				if (tempStamp.lessThan(ts)){return false;}
			}
		}
		return true;
	}
	  
  }
//executes the intended command and returns a response.
	synchronized static String parseAndExecute(String msg){
		String[] tokens = msg.split(" ");
		String response = "";

		switch(tokens[0]){
		case "purchase": 
			if(!inventory.containsKey(tokens[2]))
				response = "Not Available - We do not sell this product";
			else if(inventory.get(tokens[2]) < Integer.parseInt(tokens[3])){
				response = "Not Available - Not Enough Items";
			}else{
				inventory.replace(tokens[2], inventory.get(tokens[2])-Integer.parseInt(tokens[3]));
				Order order = server.new Order(currentOrderID.getAndIncrement(),tokens[1],tokens[2],Integer.parseInt(tokens[3]));
				orderHistory.addElement(order);
				response = "Your order has been placed, "+order.toString();
			}
			
		break;
		case "cancel":
			Order toCancel = null;
			for(Order order: orderHistory){
				if(order.getOrderID()==Integer.parseInt(tokens[1])){
					toCancel = order;
					break;
				}
			}
			if(toCancel != null){
				orderHistory.remove(toCancel);
				for(String key : inventory.keySet()){
					if(key.equals(toCancel.getProductName())){
					inventory.replace(key, inventory.get(key)+toCancel.getQuantity());
					}
				}	
				response = "Order " + tokens[1] +" is canceled";
			}
			else{
				response = tokens[1] + " not found, no such order";
			}
		break;
		case "search":
			String userName = tokens[1];
			List<Order> orders = new ArrayList<Order>();
			for(Order o : orderHistory){
				if(userName.equals(o.getUsername())){
					orders.add(o.copy());
				}
			}
			if(orders.isEmpty()){
				response = "No order found for "+userName;
			}
			else{
				Collections.sort(orders);
				for(Order o : orders){
					response = response + o.toStringNoName()+"\n";
				}
				response = response.substring(0, response.length()-1);
			}
		break;
		case "list":
			String list= "";
			for(String key : inventory.keySet()){
					list = key + " " + inventory.get(key);
					response = response+list+"\n";
			}
			response = response.substring(0, response.length()-1);
		break;
		default:
			response = "Error: "+tokens[0]+" is not a valid command.\n";
		}
		return response;
	}
	
	static Integer max(Integer x, Integer y){
		if(x >= y)
			return x;
		else 
			return y;
	}
	
	static synchronized void removeProcessTimeStamps(Integer pid){
		requestQueue.removeIf(p -> p.pid == pid);
	}
  }
