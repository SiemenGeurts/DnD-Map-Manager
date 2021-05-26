package comms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import helpers.Logger;

public class Client {
	Socket client;
	int port;
	String ip;
	
	ObjectInputStream istream;
	ObjectOutputStream ostream;
	
	long startTime;
	public Client(String ip, int port) throws IOException {
		client = new Socket(ip, port);
		this.ip = ip;
		this.port = port;
		startTime = System.currentTimeMillis();
		istream = new ObjectInputStream(client.getInputStream());
		ostream = new ObjectOutputStream(client.getOutputStream());
	}
	
	public Message<?> readMessage() throws IOException {
		try {
			Message<?> m = (Message<?>) istream.readObject();
			Logger.println("reading: " + m.getMessage().toString());
			return m;
		} catch(ClassNotFoundException | NullPointerException e) {
			throw new IOException("Received object which was not a message...");
		}
	}
	
	public void write(Message<?> m) throws IOException {
		ostream.writeObject(m);
		ostream.flush();
	}
}
