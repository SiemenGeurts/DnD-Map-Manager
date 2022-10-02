package comms;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.json.JSONObject;

import actions.ActionDecoder;
import actions.ActionEncoder;
import gui.ErrorHandler;
import helpers.Logger;
import helpers.codecs.JSONEncoder;
import helpers.codecs.JSONKeys;

public class ClientListener {
	
	static boolean isInstanceActive = false;
	static boolean stop = true;
	static boolean disconnect = false;
	static boolean isDisconnectedRun = false;
	private ClientListenerThread listener;
	private ClientSenderThread sender;
	private Runnable runOnDisconnected;
	
	public ClientListener(Client client, Runnable run) {
		if(isInstanceActive)
			throw new IllegalStateException("Cannot start a new client thread while another one is running!");
		runOnDisconnected = run;
		isInstanceActive = true;
		stop = false;
		isDisconnectedRun = false;
		disconnect = false;
		listener = new ClientListenerThread(client);
		sender = new ClientSenderThread(client);
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
	}
	
	public static void stopAll() {
		stop = true;
	}
	
	public void disconnect() {
		sendMessage(ActionEncoder.disconnect());
		disconnect = true;
	}

	public static boolean isActive() {
		return isInstanceActive;
	}
	
	public Message<? extends Serializable> readMessage() throws IllegalStateException {
		if(listener.readingQueue.size()==0) {
			if(isInstanceActive)
				return null;
			throw new IllegalStateException("Client is not active");
		}
		return listener.readingQueue.poll();
	}
	
	public Message<? extends Serializable> waitForMessage() throws InterruptedException, IllegalStateException {
		if(listener.readingQueue.size()==0) {
			if(isInstanceActive)
				return listener.readingQueue.take();
			throw new IllegalStateException("Client is not active");
		}
		return listener.readingQueue.take();
	}
	
	public void sendMessage(JSONObject jobj) throws IllegalStateException {
		Message<SerializableJSON> msg = new Message<SerializableJSON>(new SerializableJSON(jobj));
		Logger.println("Sending message["+msg.getID()+"] of type '" + jobj.getString("type") + "'");
		if(isInstanceActive)
			sender.sendingQueue.add(msg);
		else
			throw new IllegalStateException("Client is not active");
	}
	
	private void onDisconnect() {
		stop = true;
		isInstanceActive = false;
		if(runOnDisconnected != null && !isDisconnectedRun) {
			isDisconnectedRun = true;
			runOnDisconnected.run();
		}
	}
	
	private class ClientListenerThread extends Thread {
		private BlockingQueue<Message<? extends Serializable>> readingQueue;
		private Client client;
		
		public ClientListenerThread(Client client) {
			this.client = client;
			readingQueue = new ArrayBlockingQueue<Message<?>>(50);
			setDaemon(true);
		}
		
		@Override
		public void run() {
			Logger.println("Starting client listener...");
			try {
				while(!stop) {
					try {
						Thread.sleep((long) (1000 / 20));
						Logger.println("listening...");
						Message<? extends Serializable> msg = client.readMessage();
						if(msg.getMessage() != null && msg.getMessage() instanceof SerializableJSON && 
								((SerializableJSON) msg.getMessage()).getJSON().get("type").equals(JSONKeys.KEY_DISCONNECT)) {
							break;
						} else
							readingQueue.put(msg);
					} catch (IOException | InterruptedException e) {
						if(!stop) {
							stop = true;
							ErrorHandler.handle("Communication was lost, please reconnect.", e);
						}
						break;
					}
				}
			} catch(Exception e) {
				ErrorHandler.handle("The client thread has crashed! Try resyncing the game.", e);
			} finally {
				Logger.println("Terminating client listener...");
				sender.interrupt();
				onDisconnect();
			}
		} 
	}
	
	private class ClientSenderThread extends Thread {
		private BlockingQueue<Message<? extends Serializable>> sendingQueue;
		private Client client;
		
		public ClientSenderThread(Client client) {
			this.client = client;
			sendingQueue = new ArrayBlockingQueue<Message<? extends Serializable>>(50);
			setDaemon(true);
		}
		
		@Override
		public void run() {
			Logger.println("Starting client sender...");
			try {
				while(!stop) {
					try {
						Message<? extends Serializable> msg = sendingQueue.take();
						client.write(msg);
						if(sendingQueue.size() == 0 && disconnect == true) {
							stop = false;
							listener.interrupt();
							break; // this'll set stop=true below
						}
					} catch (IOException | InterruptedException e) {
						if(!stop) {
							stop = true;
							ErrorHandler.handle("Communication was lost, please reconnect.", e);
						}
						break;
					}
				}
			} catch(Exception e) {
				ErrorHandler.handle("The client thread has crashed! Try resyncing the game.", e);
			} finally {
				Logger.println("Terminating client sender...");
				listener.interrupt();
				onDisconnect();
			}
		}
	}
}