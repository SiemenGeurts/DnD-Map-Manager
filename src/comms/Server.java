package comms;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public ServerSocket server;
	private Socket socket;
	
	BufferedWriter out;
	
	private Server(ServerSocket server) {
		this.server = server;
	}
	
	public void waitForClient() throws IOException {
		socket = server.accept();
		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	}
	
	public void write(String s) throws IOException {
		out.write(s);
		out.flush();
	}
	
	public static Server createServer(int port) throws IOException {
		return new Server(new ServerSocket(port));
	}
}