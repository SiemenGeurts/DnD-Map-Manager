package comms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;

import helpers.Logger;
import javafx.scene.image.Image;

public class Client {
	Socket client;
	int port;
	String ip;
	
	ObjectInputStream istream;
	ObjectOutputStream ostream;
	
	long startTime;
	private Client(String ip, int port) throws IOException {
		client = new Socket(ip, port);
		this.ip = ip;
		this.port = port;
		startTime = System.currentTimeMillis();
		istream = new ObjectInputStream(client.getInputStream());
		ostream = new ObjectOutputStream(client.getOutputStream());
	}
	
	public Image readImage() throws IOException {
		Logger.println(getTimeStamp() + "reading image");
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
			if(c.isInstance(m.getMessage())) {
				Logger.println(getTimeStamp() + "reading: " + m.getMessage());
				return c.cast(m.getMessage());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		throw new CommsException("Received message was not of type " + c.getSimpleName());
	}
	
	public Message<?> readMessage() throws IOException {
		try {
			Message<?> m = (Message<?>) istream.readObject();
			Logger.println(getTimeStamp() + "reading: " + m.toString());
			return m;
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		throw new CommsException("Could not receive message.");
	}
	
	public <T extends Serializable> void write(T obj) throws IOException {
		Logger.println(getTimeStamp() +"writing: " + obj.toString());
		ostream.writeObject(new Message<T>(obj));
		ostream.flush();
	}
	
	private String getTimeStamp() {
		return "["  + (System.currentTimeMillis() - startTime) + "] ";
	}
}
