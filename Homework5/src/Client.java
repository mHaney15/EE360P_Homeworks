import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
  @SuppressWarnings("null")
public static void main (String[] args) {
    Scanner sc = new Scanner(System.in);
    int numServer = sc.nextInt();
    String[] serverID = null;
    InetAddress[] ipAddress = null;
    int[] portNumber = null;
    for (int i = 0; i < numServer; i++) {
      // TODO: parse inputs to get the ips and ports of servers
    	serverID[i] = sc.nextLine();
    	String[] split = serverID[i].split(":");
    	try {
			ipAddress[i] = InetAddress.getByName(split[0]);
	    	portNumber[i] = Integer.parseInt(split[1]);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    try{
	    while(sc.hasNextLine()) {
	      String cmd = sc.nextLine();
	      String[] tokens = cmd.split(" ");
	      Socket clientSocket;
	      BufferedReader tcpIn;
	      BufferedWriter tcpOut;
	      clientSocket = null;
	      String username, product, message, response,buffer;
	      message = username = product = null;
	      response = "";
	      buffer = "";
	      int quantity, orderID;
	      quantity = orderID = -1;
	
	      if (tokens[0].equals("purchase")) {
	        // TODO: send appropriate command to the server and display the
	        // appropriate responses form the server
	    	  username = tokens[1];
	    	  product = tokens[2];
	    	  quantity = Integer.parseInt(tokens[3]);
	    	  message = tokens[0] + " " + username + " " + product + " " + quantity;
	      } else if (tokens[0].equals("cancel")) {
	        // TODO: send appropriate command to the server and display the
	        // appropriate responses form the server
	    	  orderID = Integer.parseInt(tokens[1]);
	    	  message = tokens[0] + " " + Integer.toString(orderID);
	    	  
	      } else if (tokens[0].equals("search")) {
	        // TODO: send appropriate command to the server and display the
	        // appropriate responses form the server
	    	  username = tokens[1];
	    	  message = tokens[0] + " " + username;
	    	  
	      } else if (tokens[0].equals("list")) {
	        // TODO: send appropriate command to the server and display the
	        // appropriate responses form the server
	    	  message = tokens[0];
	    	  
	      } else {
	        System.out.println("ERROR: No such command");
	      }
	      for(int x = 0; x < ipAddress.length; x++){
	    	  try{
	    		  clientSocket = new Socket(ipAddress[x], portNumber[x]);
	    		  break;
	    	  }catch(IOException e){
	    		  e.printStackTrace();
	    	  }
	      }
			  tcpIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		      tcpOut = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		      tcpOut.write(message+"\n");
		      tcpOut.flush();
		      buffer = tcpIn.readLine();
		      while(buffer != null){
		    	  response = response+buffer+"\n";
		    	  buffer = tcpIn.readLine();
		      }
		      response = response.substring(0, response.length() - 1);
		      clientSocket.close();
	         
	    }
	    sc.close();
    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
  }
}
