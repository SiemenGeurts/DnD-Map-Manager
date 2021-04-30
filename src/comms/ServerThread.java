package comms;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import gui.ErrorHandler;
import helpers.Logger;

public class ServerThread extends Thread {
	
	private static boolean isInstanceActive = false;
	private static boolean stop = false;
	private static BlockingQueue<Message<? extends Serializable>> readingQueue;
	private Server server;
	
	public ServerThread(Server server) {
		if(isInstanceActive)
			throw new IllegalStateException("Cannot start a new serverthread while another one is running!");
		this.server = server;
		readingQueue = new ArrayBlockingQueue<Message<? extends Serializable>>(50);
		isInstanceActive = true;
	}
	
	@Override
	public void run() {
		Logger.println("Starting server...");
		while(!stop) {
			try {
				try {
					Thread.sleep((long) (1000 / 20));
					Logger.println("listening...");
					Message<? extends Serializable> msg = server.read();
					readingQueue.put(msg);
				} catch (IOException | InterruptedException e) {
					ErrorHandler.handle("Communication was lost, please reconnect.", e);
					break;
				}
			} catch(Exception e) {
				ErrorHandler.handle("The server thread has crashed! Try resyncing the game.", e);
			}
		}
		Logger.println("Terminating server...");
		isInstanceActive = false;
		stop = false;
	}
	
	public static void stopAll() {
		stop = true;
	}
	
	public static void startListening(int port) {
		new Thread(() -> {
			try {
				Server server = Server.create(port);
				server.waitForClient();
				new ServerThread(server).start();
				new ServerListener(server).start();
			} catch (IOException e) {
				ErrorHandler.handle("Could not create server.", e);
			}
		}).start();
	}
}
