/*
 * EE360P Homework 3 Question 3: Client Code
 * Authors: Matthew Haney and Kelvin Pang
 * UT eids: mah4687 and kkp452
 */

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

    try {
    	
    	InetAddress host;
        Socket clientSocket;
        DatagramSocket udpSocket;
        BufferedReader tcpIn;
	    BufferedWriter tcpOut;
	    DatagramPacket packetIn, packetOut;
    	host = InetAddress.getByName(hostAddress);
	    Scanner sc = new Scanner(System.in);
	    while(sc.hasNextLine()) {
	      String cmd = sc.nextLine();
	      String[] tokens = cmd.split(" ");
	      String username, product, protocol, message, response,buffer;
	      message = username = product = protocol = null;
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
	    	  protocol = tokens[4];
	    	  message = tokens[0] + " " + username + " " + product + " " + quantity;
	    	  
	      } else if (tokens[0].equals("cancel")) {
	        // TODO: send appropriate command to the server and display the
	        // appropriate responses form the server
	    	  orderID = Integer.parseInt(tokens[1]);
	    	  protocol = tokens[2];
	    	  message = tokens[0] + " " + Integer.toString(orderID);
	      
	      } else if (tokens[0].equals("search")) {
	        // TODO: send appropriate command to the server and display the
	        // appropriate responses form the server
	    	  username = tokens[1];
	    	  protocol = tokens[2];
	    	  message = tokens[0] + " " + username;
	      
	      } else if (tokens[0].equals("list")) {
	        // TODO: send appropriate command to the server and display the
	        // appropriate responses form the server
	    	  protocol = tokens[1];
	    	  message = tokens[0];
	      
	      } else {
	        System.out.println("ERROR: No such command");
	      }
	      
	      if(message != null){
	    	  if(protocol.toUpperCase().equals("T")){
	    		  clientSocket = new Socket(host, tcpPort);
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
	    	  }else{
	    		  byte[] data = message.getBytes();
	    		  packetOut = new DatagramPacket(data, data.length, host, udpPort);
	    		  udpSocket = new DatagramSocket();
	    		  udpSocket.send(packetOut);
	    		  byte[] buf = new byte[udpBufSize];
	    		  packetIn = new DatagramPacket(buf, buf.length);
	    		  udpSocket.receive(packetIn);
	    		  response = new String(packetIn.getData()).trim();
	    		  udpSocket.close();
	    	  }
	    	  System.out.println(response);
	      }
	      
	      
	      
	    }
	    sc.close();
    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}   
  }
}
