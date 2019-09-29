package comms;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.scene.image.Image;

public class Server {

	public ServerSocket server;
	private Socket socket;
	
	ObjectOutputStream ostream;
	
	long startTime;
	
	private Server(ServerSocket server) {
		this.server = server;
		startTime = System.currentTimeMillis();
	}
	
	public void waitForClient() throws IOException {
		socket = server.accept();
		ostream = new ObjectOutputStream(socket.getOutputStream());
	}
	
	public void write(String s) throws IOException {
		System.out.println("[" + (System.currentTimeMillis()-startTime) + "] writing: " + s);
		ostream.writeObject(new Message<String>(s));
		ostream.flush();
	}
	
	public void write(Image image) throws IOException {
		ostream.writeObject(new Message<SerializableImage>(new SerializableImage(image)));
		ostream.flush();
	}
	
	public <T extends Serializable> void write(T obj) throws IOException {
		ostream.writeObject(new Message<T>(obj));
		ostream.flush();
	}
	
	public static Server create(int port) throws IOException {
		return new Server(new ServerSocket(port));
	}

}