package network;
import java.io.*;
public class Mail implements Serializable {

	// The different types of message sent by the Client
	static final int sendAllMails = 0, MESSAGE = 1, LOGOUT = 2, block = 3,deletelastmessage=4;
	
	private int type;
	private String from;
	private String to;
	private String subject;
	private String message;
	
	// constructor
	Mail(int type,String from ,String to,String subject,String message) {
		this.type = type;
		this.from=from;
		this.to=to;
		this.subject=subject ;
		this.message = message;
	}
	Mail(Mail m2) {
		this.type = m2.getType();
		this.from=m2.getFrom();
		this.to=m2.getTO();
		this.subject=m2.getSubject() ;
		this.message = m2.getMessage();
	}
	Mail(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	int getType() {
		return type;
	}

	String getFrom() {
		return from;
	}
	String getTO() {
		return to;
	}
	String getSubject() {
		return subject;
	}
	String getMessage() {
		return message;
	}
	void printMessage() {
		System.out.println("from: "+from);
		System.out.println("to: "+to);
		System.out.println("subject: "+subject);
		System.out.println("message: "+message);
	}
}
