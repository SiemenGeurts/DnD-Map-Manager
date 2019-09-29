package comms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javafx.scene.image.Image;

public class Client {
	Socket client;
	int port;
	String ip;
	
	ObjectInputStream istream;
	long startTime;
	private Client(Socket client, String ip, int port) throws IOException {
		this.client = client;
		this.ip = ip;
		this.port = port;
		startTime = System.currentTimeMillis();
		istream = new ObjectInputStream(client.getInputStream());
	}
	
	public Image readImage() throws IOException {
		System.out.println("[" + (System.currentTimeMillis()-startTime) + "] reading image");
		try {
		Message<?> m = (Message<?>) istream.readObject();
		if(m.getMessage() instanceof SerializableImage)
			return ((SerializableImage) m.getMessage()).getImage();
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		throw new CommsException("Message did not contain an image.");
	}
	
	public <T extends Object> T read(Class<T> c) throws IOException {
		if(c == Image.class)
			return c.cast(readImage());
		try {
			Message<?> m = (Message<?>) istream.readObject();
			if(c.isInstance(m.getMessage()))
				return c.cast(m.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		throw new CommsException("Received message was not of type " + c.getSimpleName());
	}
	
	public static Client create(String ip, int port) throws UnknownHostException, IOException {
		return new Client(new Socket(ip, port), ip, port);
	}
}
