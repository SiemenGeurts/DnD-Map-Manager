package comms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	
	Socket client;
	int port;
	String ip;
	
	BufferedReader in;
	
	
	private Client(Socket client, String ip, int port) throws IOException {
		this.client = client;
		this.ip = ip;
		this.port = port;
		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	}
	
	public String read() throws IOException {
		return in.readLine();
	}
	
	public static Client create(String ip, int port) throws UnknownHostException, IOException {
		return new Client(new Socket(ip, port), ip, port);
	}
}
