package comms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.scene.image.Image;

public class Server {

	public ServerSocket server;
	private Socket socket;
	
	ObjectOutputStream ostream;
	ObjectInputStream istream;
	long startTime;
	
	private Server(ServerSocket server) {
		this.server = server;
		startTime = System.currentTimeMillis();
	}
	
	public void waitForClient() throws IOException {
		socket = server.accept();
		ostream = new ObjectOutputStream(socket.getOutputStream());
		istream = new ObjectInputStream(socket.getInputStream());
	}
	
	public void write(Image image, int id) throws IOException {
		System.out.println(getTimeStamp() + " writing image");
		ostream.writeObject(new Message<SerializableImage>(new SerializableImage(image, id)));
		ostream.flush();
	}
	
	public <T extends Serializable> void write(T obj) throws IOException {
		System.out.println(getTimeStamp() + "writing: " + obj.toString());
		ostream.writeObject(new Message<T>(obj));
		ostream.flush();
	}
	
	public <T extends Object> T read(Class<T> c) throws IOException {
		try {
			Message<?> m = (Message<?>) istream.readObject();
			if(c.isInstance(m.getMessage())) {
				System.out.println(getTimeStamp() + " reading: " + m.getMessage());
				return c.cast(m.getMessage());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		throw new CommsException("Received message was not of type " + c.getSimpleName());
	}
	
	private String getTimeStamp() {
		return "["  + (System.currentTimeMillis() - startTime) + "] ";
	}
	
	public static Server create(int port) throws IOException {
		return new Server(new ServerSocket(port));
	}

}