package comms;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class Server {

	public ServerSocket server;
	private Socket socket;
	
	BufferedWriter out;
	OutputStream ostream;
	
	private Server(ServerSocket server) {
		this.server = server;
	}
	
	public void waitForClient() throws IOException {
		socket = server.accept();
		out = new BufferedWriter(new OutputStreamWriter(ostream = socket.getOutputStream()));
	}
	
	public void write(String s) throws IOException {
		out.write(s);
		out.flush();
	}
	
	public void write(Image image) throws IOException {
		BufferedImage bimage = SwingFXUtils.fromFXImage(image, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bimage, "png", baos);
		byte[] size = ByteBuffer.allocate(4).putInt(baos.size()).array();
		ostream.write(size);
		ostream.write(baos.toByteArray());
		ostream.flush();
	}
	
	public static Server create(int port) throws IOException {
		return new Server(new ServerSocket(port));
	}

}