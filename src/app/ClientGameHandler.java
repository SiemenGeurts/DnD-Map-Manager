package app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import actions.Action;
import actions.ActionDecoder;
import actions.ActionEncoder;
import comms.Client;
import comms.Message;
import comms.SerializableImage;
import controller.ClientController;
import controller.MainMenuController;
import data.mapdata.Map;
import gui.ErrorHandler;
import helpers.AssetManager;
import helpers.ScalingBounds.ScaleMode;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;

public class ClientGameHandler extends GameHandler {

	private Client client;
	public ArrayList<Action> actions;
	private Action currentAction;
	public static ClientGameHandler instance;

	ClientController controller;

	// clientListener stuff
	Thread clientListener;
	private boolean running = false, paused = false;
	private Object pauseLock = new Object();

	public ClientGameHandler(Client client) {
		this.client = client;
		instance = this;
		actions = new ArrayList<>();
		try {
			FXMLLoader loader = new FXMLLoader(
					ClientGameHandler.class.getResource("/assets/fxml/ClientPlayScreen.fxml"));
			Scene scene = new Scene(loader.load());
			scene.getRoot().requestFocus();
			controller = loader.getController();
			controller.setGameHandler(this);
			MainMenuController.sceneManager.pushView(scene, loader);
		} catch (IOException e) {
			ErrorHandler.handle("Well, something went horribly wrong...", e);
		}
		loadTextures();
		loadMap();

		clientListener = new Thread(new Runnable() {
			@Override
			public void run() {
				while (running) {
					synchronized (pauseLock) {
						if (paused) {
							System.out.println("Listener paused");
							try {
								synchronized (pauseLock) {
									pauseLock.wait();
								}
							} catch (InterruptedException ex) {
								break;
							}
						}
						if (!running)
							break;
						try {
							while (true) {
								Thread.sleep((long) (1000 / 20));
								Message<?> m = client.readMessage();
								if(m.getMessage() instanceof SerializableImage) {
									SerializableImage si = (SerializableImage) m.getMessage();
									AssetManager.textures.put(si.getId(), si.getImage());
								} else if(m.getMessage() instanceof String)
									ActionDecoder.decode((String) m.getMessage()).attach();
								else
									ErrorHandler.handle("Received message of type " + m.getMessage().getClass().getSimpleName() + " and didn't know what to do with it...", null);
							}
						} catch (IOException | InterruptedException e) {
							Platform.runLater(new Runnable() {
								public void run() {
									ErrorHandler.handle("Communication was lost, please restart the session.", e);
								}
							});
							break;
						}
					}
				}
			}
		});
		clientListener.setDaemon(true);
		running = true;
		clientListener.start();
		Thread updating = new Thread(new Runnable() {
			@Override
			public void run() {
				long time = System.currentTimeMillis();
				do {
					try {
						Thread.sleep((long) (1000 / 20));
						update(System.currentTimeMillis() - time);
						time = System.currentTimeMillis();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} while (true);
			}
		});
		updating.setDaemon(true);
		updating.start();
	}
	
	public void pauseClientListener() {
		paused = true;
	}
	
	public void resumeClientListener() {
		synchronized(pauseLock) {
			paused = false;
			pauseLock.notifyAll();
		}
	}

	public void update(float dt) {
		for (int i = 0; i < actions.size(); i++) {
			currentAction = actions.get(i);
			currentAction.update(dt);
		}
		currentAction = null;
	}

	public boolean loadTextures() {
		System.out.println("[CLIENT] Loading textures.");
		try {
			int amount = client.read(Integer.class);
			HashMap<Integer, Image> textures = AssetManager.textures;
			for (int i = 0; i < amount; i++) {
				SerializableImage img = client.read(SerializableImage.class);
				textures.put(img.getId(), img.getImage());
			}
		} catch (NumberFormatException | IOException e) {
			ErrorHandler.handle("Did not recieve all textures. Please try again.", e);
			return false;
		}
		return true;
	}

	public boolean loadMap() {
		System.out.println("[CLIENT] Loading map. ");
		try {
			boolean hasBackground = client.read(Boolean.class);
			map = Map.decode(client.read(String.class));
			if (hasBackground) {
				String scaling = client.read(String.class);
				map.setScaling(scaling.equals("fit") ? ScaleMode.FIT
						: (scaling.equals("stretch") ? ScaleMode.STRETCH : ScaleMode.EXTEND));
				map.setBackground(client.readImage());
			}
			controller.setMap(map);
			controller.drawMap();
		} catch (IOException e) {
			ErrorHandler.handle("The map could not be loaded. Please try again.", e);
			return false;
		}
		return true;
	}

	public void requestTexture(int id) {
		try {
			client.write(ActionEncoder.requestTexture(id));
		} catch (IOException e) {
			ErrorHandler.handle("Could not send texture request or receive an answer.", e);
		}
	}

	public ClientController getController() {
		return controller;
	}

	public Action insertAction(Action action) {
		if (currentAction == null) {
			action.attach();
			return action;
		} else
			return currentAction.insertAction(action);
	}
}