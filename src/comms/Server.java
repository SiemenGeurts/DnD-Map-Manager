package comms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import helpers.Logger;

public class Server {

	static Server instance;
	public ServerSocket server;
	private Socket socket;
	
	ObjectOutputStream ostream;
	ObjectInputStream istream;
	
	private Server(ServerSocket server) {
		instance = this;
		this.server = server;
	}
	
	public void waitForClient() throws IOException {
		socket = server.accept();
		ostream = new ObjectOutputStream(socket.getOutputStream());
		istream = new ObjectInputStream(socket.getInputStream());
	}
	
	public void write(Message<?> msg) throws IOException {
		ostream.writeObject(msg);
		ostream.flush();
	}
	
	public Message<?> read() throws IOException {
		try {
			Message<?> m = (Message<?>) istream.readObject();
			Logger.println("reading: " + m.getMessage());
			return m;
		} catch(ClassNotFoundException | NullPointerException e) {
			throw new IOException("Received object which was not a message...");
		}
	}
	
	public static Server create(int port) throws IOException {
		if(instance == null || instance.server.isClosed())
			instance = new Server(new ServerSocket(port));
		return instance;
	}

}