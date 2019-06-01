package network;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Scanner;
import java.io.*;


public class Client {
	
	private String notif = " *** ";
	private ObjectInputStream sInput;		// to read from the socket
    private ObjectOutputStream sOutput;		// to write on the socket
    private Socket socket;
    private String server, username;	// server and username
	private int port;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	Client(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}
	public boolean start() {
		// open socket on the  port number
		try {
			socket = new Socket(server, port);
		} 
		// exception handler if it failed
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}
		//now we are connected
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		/* Creating both Data Stream */
		try
		{
			// opening I/O stream with server 
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// run the listener
		new ListenFromServer().start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be Mail objects
		try
		{
			//first thing we make a username on server for this client 
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}
	/*
	 * To send a message to the console
	 */
	private void display(String msg) {

		System.out.println(msg);
		
	}
	/*
	 * To send a message to the server
	 */
	void sendMessage(Mail msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}
	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect
	 */
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {}
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {}
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {}
			
	}
	
	public static void main(String[] args) {
		// default values if not entered
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonymous";
		Scanner scan = new Scanner(System.in);
		
		System.out.print("Enter the username: ");
		userName = scan.nextLine();
 
		switch(args.length) {
			case 3:
				// for > javac Client username portNumber serverAddr
				serverAddress = args[2];
			case 2:
				// for > javac Client username portNumber
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
					return;
				}
			case 1: 
				// for > javac Client username
				userName = args[0];
			case 0:
				// for > java Client
				break;
			// if number of arguments are invalid
			default:
				System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
			return;
		}
		// i will make a new client 
		Client client = new Client(serverAddress, portNumber, userName);
		// try to counect 
		if(!client.start())
			return;
		
		System.out.println("\nHello.! Welcome to the fci email server.");
		System.out.println("Instructions:");
		System.out.println("1. Type 'send' to send mail");
		System.out.println("2. Type 'allmails' to get all mails");
		System.out.println("3. Type 'LOGOUT'  logoff from server");
		System.out.println("4. Type 'BlockUser'");
		System.out.println("5. Type 'deletelastmessage' to delete spam message from your inbox");
		
		while(true) {
			System.out.print("what do you want to do :  ");
			String check = scan.nextLine();
			// logout if message is LOGOUT
			if(check.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new Mail(Mail.LOGOUT, ""));
				break;
			}
			else if(check.equalsIgnoreCase("AllMails")) {
				client.sendMessage(new Mail(Mail.sendAllMails, ""));
				
			}
			else if(check.equalsIgnoreCase("blockUser")) {
				System.out.print("who do you want to block :  ");
				String block = scan.nextLine();
				client.sendMessage(new Mail(Mail.block, block));
				
			}
			else if(check.equalsIgnoreCase("deleteLastMessage")) {
				client.sendMessage(new Mail(Mail.deletelastmessage, ""));
				
			}
			// send mail
			else {
				System.out.print("from: ");
				String from = scan.nextLine();
				System.out.print("to: ");
				String to = scan.nextLine();
				System.out.print("subject: ");
				String subject = scan.nextLine();
				System.out.print("message: ");
				String msg = scan.nextLine();
				client.sendMessage(new Mail(Mail.MESSAGE, from,to,subject,msg));
			}
		}
		// close the connection
		scan.close();
		// close I/O stream 
		client.disconnect();	
	}

	/*
	 * a class that waits for the message from the server
	 */
	class ListenFromServer extends Thread {
		
		public void run() {
			while(true) {
				try {
					// read the message form the input datastream
					Mail msg = (Mail) sInput.readObject();
					//print the message
					System.out.println();
					msg.printMessage();
				}
				catch(IOException e) {
					display(notif + "Server has closed the connection: " + notif);
					break;
				}
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}

