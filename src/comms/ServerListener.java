package comms;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.json.JSONObject;

import actions.ActionDecoder;
import actions.ActionEncoder;
import data.mapdata.Map;
import gui.ErrorHandler;
import helpers.Logger;
import helpers.codecs.JSONEncoder;
import helpers.codecs.JSONKeys;
import javafx.scene.image.Image;

public class ServerListener extends Thread {
	
	static boolean isInstanceActive = false;
	static boolean stop = true;
	private static boolean disconnect = false;
	private static boolean isDisconnectedRun = false;
	private int port;
	private Runnable runOnConnect;
	private Runnable runOnDisconnect;
	private ServerListenerThread listener;
	private ServerSenderThread sender;
	
	public ServerListener(int port, Runnable run, Runnable disconnect) {
		this.port = port;
		this.runOnConnect = run;
		this.runOnDisconnect = disconnect;
		if(isInstanceActive)
			throw new IllegalStateException("Cannot start a new server thread while another one is running!");
		isInstanceActive = true;
		setDaemon(true);
	}
	
	public static void stopAll() {
		stop = true;
	}
	
	public void disconnect() {
		try {
			sendMessage(ActionEncoder.disconnect());
			if(listener.isAlive())
				listener.interrupt();
		}catch(InterruptedException | IllegalStateException e) {}
		disconnect = true;
	}
	
	@Override
	public void run() {
		try {
			stop = false;
			disconnect = false;
			isDisconnectedRun = false;
			Server server = Server.create(port);
			server.waitForClient();
			listener = new ServerListenerThread(server);
			sender = new ServerSenderThread(server);
			listener.start();
			sender.start();
			int version = JSONEncoder.VERSION;
			try {
				sendMessage(JSONEncoder.version());
			} catch (IllegalStateException e1) {
				ErrorHandler.handle("Could not send encoder version.", e1);
			}
			try {
				Message<?> msg = waitForMessage();
				if(msg.getMessage() instanceof SerializableJSON) {
					JSONObject obj = ((SerializableJSON) msg.getMessage()).getJSON();
					if(obj.getString("type").equals(JSONKeys.IntKey.KEY_JSON_VERSION.get()))
						version = obj.getInt("version");
					else {
						ErrorHandler.handle("First message did not contain encoding version, using " + version,  null);
						listener.readingQueue.put(msg); //put the message back into the queue
					}
				} else {
					ErrorHandler.handle("First message did not contain encoding version, using "+ version,  null);
					listener.readingQueue.put(msg); //put the message back into the queue
				}
			} catch(InterruptedException e) {
				ErrorHandler.handle("Didn't receive encoding version, using " + version, e);
			}
			ActionDecoder.setVersion(version);
			if(runOnConnect != null)
				runOnConnect.run();
		} catch (IOException e) {
			ErrorHandler.handle("Could not create server.", e);
		} catch(IllegalStateException | InterruptedException e) {
			ErrorHandler.handle("Could not transmit encoder version.", e);
		}
	}
	
	public static boolean isActive() {
		return isInstanceActive;
	}
	
	public Message<? extends Serializable> readMessage() throws IllegalStateException {
		if(listener.readingQueue.size()==0) {
			if(isInstanceActive)
				return null;
			throw new IllegalStateException("Server is not active");
		}
		return listener.readingQueue.poll();
	}
	
	public Message<? extends Serializable> waitForMessage() throws InterruptedException, IllegalStateException {
		if(listener.readingQueue.size()==0) {
			if(isInstanceActive)
				return listener.readingQueue.take();
			throw new IllegalStateException("Server is not active");
		}
		return listener.readingQueue.take();
	}
	
	public void sendMessage(Map map, boolean includeBackground) throws IllegalStateException, InterruptedException {
		Logger.println("Sending map");
		sendMessage(new SerializableMapV4(map, includeBackground));
	}
	
	public void sendMessage(Image image, int id, boolean show) throws IllegalStateException, InterruptedException {
		Logger.println("Sending image " + (show ? "to show " : "") + "with id " + id);
		SerializableImage simg = new SerializableImage(image, id);
		simg.setShow(show);
		sendMessage(simg);
	}
	
	public void sendMessage(Image image, int id) throws IllegalStateException, InterruptedException {
		Logger.println("Sending image with id " + id);
		sendMessage(new SerializableImage(image, id));
	}
	
	public void sendMessage(JSONObject obj) throws IllegalStateException, InterruptedException {
		Logger.println("Sending json of type '" + obj.getString("type") + "'");
		sendMessage(new SerializableJSON(obj));
	}
	
	private <T extends Serializable> void sendMessage(T obj) throws IllegalStateException, InterruptedException {
		Message<T> msg = new Message<T>(obj);
		Logger.println("Sending message["+msg.getID() +"] of type " + obj.getClass().getSimpleName());
		if(isInstanceActive)
			sender.sendingQueue.put(msg);
		else
			throw new IllegalStateException("Server is not active");
	}
	
	private void onDisconnect() {
		isInstanceActive = false;
		stop = true;
		if(runOnDisconnect != null && !isDisconnectedRun) {
			runOnDisconnect.run();
			isDisconnectedRun = true;
		}
	}
	
	private class ServerListenerThread extends Thread {
		private BlockingQueue<Message<? extends Serializable>> readingQueue;
		private Server server;
		
		public ServerListenerThread(Server server) {
			this.server = server;
			readingQueue = new ArrayBlockingQueue<Message<?>>(50);
			setDaemon(true);
		}
		
		@Override
		public void run() {
			Logger.println("Starting server listener...");
			try {
				while(!stop) {
					try {
						Thread.sleep((long) (1000 / 20));
						Logger.println("listening...");
						Message<? extends Serializable> msg = server.read();
						if(msg.getMessage() != null && msg.getMessage() instanceof SerializableJSON
								&& ((SerializableJSON) msg.getMessage()).getJSON().get("type").equals(JSONKeys.KEY_DISCONNECT)) {
							break;
						} else
							readingQueue.put(msg);
					} catch (IOException | InterruptedException e) {
						if(!stop)
							ErrorHandler.handle("Communication was lost, please reconnect.", e);
						break;
					}
				}
			} catch (Exception e) {
				ErrorHandler.handle("The server thread has crashed! Try resyncing the game.", e);
			} finally {				
				Logger.println("Terminating server listener...");
				sender.interrupt();
				onDisconnect();
			}
		} 
	}
	
	private class ServerSenderThread extends Thread {
		private BlockingQueue<Message<? extends Serializable>> sendingQueue;
		private Server server;
		
		public ServerSenderThread(Server server) {
			this.server = server;
			sendingQueue = new ArrayBlockingQueue<Message<? extends Serializable>>(50);
			setDaemon(true);
		}
		
		@Override
		public void run() {
			Logger.println("Starting server sender...");
			try {
				while(!stop) {
					try {
						Message<? extends Serializable> msg = sendingQueue.take();
						server.write(msg);
						if(sendingQueue.size() == 0 && disconnect == true) {
							stop = true;
							break;
						}
					} catch (IOException | InterruptedException e) {
						if(!stop)
							ErrorHandler.handle("Communication was lost, please reconnect.", e);
						break;
					}
				}
			} catch(Exception e) {
				ErrorHandler.handle("The server thread has crashed! Try resyncing the game.", e);
			} finally {				
				Logger.println("Terminating server sender...");
				listener.interrupt();
				onDisconnect();
			}
		}
	}
}