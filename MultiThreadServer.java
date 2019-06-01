package network;

import java.io.BufferedReader;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadServer  {
	// a unique ID for each connection
		private static int uniqueId;
		// an ArrayList to keep the list of the Client
		private ArrayList<ClientThread> al;
		// the port number to listen for connection
		private int port;
		// to check if server is running
		private boolean keepGoing;
		// notification
		private String notif = " *** ";
		//  map to save all messages for every user
		Map<String, List<Mail>> allmails = new HashMap<>();
		//map to save blocked users for every user
		Map<String, List<String>> blocklist = new HashMap<>();
	

  //constructor that receive the port to listen to for connection as parameter
	
  	public MultiThreadServer(int port) {
  		// the port
  		this.port = port;
  		// an ArrayList to keep the list of the Client
  		al = new ArrayList<ClientThread>();
  	}
  	

	public void start() {
		keepGoing = true;
		//open server socket
		try 
		{
			// open server socket on the port 
			ServerSocket serverSocket = new ServerSocket(port);

			// waitg for any connection and if any client want to counnect open a new thread for him and save it on the array
			//give him ID and username
			while(keepGoing) 
			{
				display("Server waiting for Clients on port " + port + ".");
				
				// waiting clients
				Socket socket = serverSocket.accept();
				// break if server stoped
				if(!keepGoing)
					break;
				//open new thread
				ClientThread t = new ClientThread(socket);
				//now i have a connection for the client and it have id and username on new theard  
				// add to the array 
				al.add(t);
				//start the thread
				t.start();
			}
			// try to stop the server
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					// close all data streams and socket
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
            String msg = " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}
	// to stop the server
		protected void stop() {
			keepGoing = false;
			try {
				new Socket("localhost", port);
			}
			catch(Exception e) {
			}
		}
		// Display an event to the console
		private void display(String msg) {
						System.out.println(msg);
		}
    
	private synchronized boolean broadcast(Mail message) {
		
		// i will check if the message goes to one person or more
		String w[];
		w=message.getTO().split(" ");
		
		// if there is one name
		if(w.length==1)
		{
			String tocheck=w[0];
			System.out.println("message to "+w[0]);
			System.out.println("chaking if user "+ message.getFrom()+" is blocked....");
			List<String> block = blocklist.get(w[0]);
			boolean isblocked=false;
			if(!(block==null)) {
				for(int i=0;i<block.size();i++) {
					if(message.getFrom().equalsIgnoreCase(block.get(i))) {
						isblocked=true;
						break;
					}
				}
			}
			
			if(!isblocked) {
				List<Mail> inbox = allmails.get(w[0]);
				if (inbox == null) {//if it's the first message for this client i will make for him a inbox
					inbox = new ArrayList<Mail>();
					inbox.add(message);
					allmails.put(w[0], inbox);
				}else {
					// if this client all ready have on i will put the message on it
					inbox.add(message);
				}
			
			
				boolean found=false;
				// i will search by the name 
				for(int y=al.size(); --y>=0;)
				{
	
					ClientThread ct1=al.get(y);
					String check=ct1.getUsername();
					if(check.equals(tocheck))
					{
						// if i cant send the message for him i will delete this client
						if(!ct1.sendmail(message)) {
							al.remove(y);
							System.out.println("Disconnected Client " + ct1.username + " removed from list.");
						}
						// username found and delivered the message
						found=true;
						break;
					}
				}
				// mentioned user not found, return false
				if(found!=true)
				{
					return false; 
				}
			}
		}
		// there are more than one receiver 
		else
		{
		
			// display message
			//System.out.println("there is a message for more than one receiver");
			//message.printMessage();
			
			// we loop in reverse order in case we would have to remove a Client
			// because it has disconnected
			for(int j=0;j<w.length;j++) {
				for(int i = al.size(); --i >= 0;) {
					ClientThread ct = al.get(i);
					
					if(ct.getUsername().equals(w[j]) ) {
					// try to write to the Client if it fails remove it from the list
						if(!ct.sendmail(message)) {
							al.remove(i);
							System.out.println("Disconnected Client " + ct.username + " removed from list.");				
						}
						else {
							System.out.println("message send to "+al.get(i).username);
							List<Mail> current = allmails.get(w[j]);
							if (current == null) {
							    current = new ArrayList<Mail>();
							    current.add(message);
							    allmails.put(w[j], current);
							}else {
								
								current.add(message);
							}
						}
						
						
					}
				}
			}
		}
		return true;
	}
synchronized void remove(int id) {
	
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// if found remove it
			if(ct.id == id) {
				System.out.println(ct.getUsername()+" is disconnected");
				al.remove(i);
				break;
			}
		}
	}
/*
 *  To run as a console application
 * > java Server
 * > java Server portNumber
 * If the port number is not specified 1500 is used
 */ 
public static void main(String[] args) {
	// start server on port 1500 unless a PortNumber is specified 
	int portNumber = 1500;
	switch(args.length) {
		case 1:
			try {
				portNumber = Integer.parseInt(args[0]);
			}
			catch(Exception e) {
				System.out.println("Invalid port number.");
				System.out.println("Usage is: > java Server [portNumber]");
				return;
			}
		case 0:
			break;
		default:
			System.out.println("Usage is: > java Server [portNumber]");
			return;
			
	}
	// create a server object and start it
	MultiThreadServer server = new MultiThreadServer(portNumber);
	server.start();
}

// One instance of this thread will run for each client
class ClientThread extends Thread {
	// the socket to get messages from client
	Socket socket;
	ObjectInputStream sInput;
	ObjectOutputStream sOutput;
	// every client have unique id
	int id;
	// the Username of the Client
	public String username;
	// message object to recieve message and its type
	Mail cm;
	// Constructor
	ClientThread(Socket socket) {
		// unique id
		id = ++uniqueId;
		this.socket = socket;
		//Creating both Data Stream
		System.out.println("Thread trying to create Object Input/Output Streams");
		try
		{
			// open I/O objectstream
			sOutput = new ObjectOutputStream(socket.getOutputStream());
			sInput  = new ObjectInputStream(socket.getInputStream());
			// get the username from client 
			username = (String) sInput.readObject();
		}
		catch (IOException e) {
			System.out.println("Exception creating new Input/output Streams: " + e);
			return;
		}
		catch (ClassNotFoundException e) {
		}
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	// infinite loop to read and forward message
	public void run() {
		// to loop until LOGOUT
		boolean keepGoing = true;
		while(keepGoing) {
			try {
				cm = (Mail) sInput.readObject();
			}
			catch (IOException e) {
				System.out.println(username + " Exception reading Streams: " + e);
				break;				
			}
			catch(ClassNotFoundException e2) {
				break;
			}

			// check the type of the message
			switch(cm.getType()) {

			case Mail.MESSAGE:
				boolean confirmation =  broadcast( cm);
				if(confirmation==false){
					sendmail(cm);
				}
				break;
			case Mail.LOGOUT:
				System.out.println(username + " disconnected with a LOGOUT message.");
				keepGoing = false;
				break;
			case Mail.sendAllMails:
				// send all maills
				List<Mail> current = allmails.get(username);
				
				for(int i = 0; i <current.size() ; ++i) {
					sendmail(current.get(i));
					
				}
				break;
			case Mail.deletelastmessage:
				List<Mail> temp= allmails.get(username);
				allmails.get(username).remove(temp.size()-1); 
				break;
			case Mail.block:
				System.out.println(username + " want block "+cm.getMessage());
				List<String> block = blocklist.get(username);
				if (block == null) {
				    block = new ArrayList<String>();
				    block.add(cm.getMessage());
				    System.out.println(cm.getMessage()+" is blocked");
				    blocklist.put(username, block);
				}else {
			
					block.add(cm.getMessage());
					System.out.println(cm.getMessage()+" is blocked");
				}
				 
				break;
			}
		}
		//to close the connection for this client
		remove(id);
		close();
	}
	
	// close all I/O streams
	private void close() {
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {}
		try {
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {};
		try {
			if(socket != null) socket.close();
		}
		catch (Exception e) {}
	}

	// write a mail to the Client output stream
	
	private boolean sendmail(Mail msg) {
		// 
		if(!socket.isConnected()) {
			close();
			return false;
		}
		// write the message to the stream
		try {
			sOutput.writeObject(msg);
		}
		// if an error occurs, do not abort just inform the user
		catch(IOException e) {
			System.out.println(notif + "Error sending message to " + username + notif);
			System.out.println(e.toString());
		}
		return true;
	}
}
}

