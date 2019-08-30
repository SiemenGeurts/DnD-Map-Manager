package comms;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class Client {
	Socket client;
	int port;
	String ip;
	
	BufferedReader in;
	InputStream istream;
	
	private Client(Socket client, String ip, int port) throws IOException {
		this.client = client;
		this.ip = ip;
		this.port = port;
		in = new BufferedReader(new InputStreamReader(istream = client.getInputStream()));
	}
	
	public String read() throws IOException {
		String s = in.readLine();
		System.out.println("[" + System.currentTimeMillis() + "] reading: " + s);
		return s;
	}
	
	public Image readImage() throws IOException {
		byte[] size = new byte[4];
		istream.read(size);
		int len = ByteBuffer.wrap(size).asIntBuffer().get();
		byte[] img = new byte[len];
		istream.read(img);
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(img));
		return SwingFXUtils.toFXImage(image, null);
	}
	
	public static Client create(String ip, int port) throws UnknownHostException, IOException {
		return new Client(new Socket(ip, port), ip, port);
	}
}
