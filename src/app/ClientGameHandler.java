package app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import actions.Action;
import actions.ActionDecoder;
import comms.Client;
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

		Thread reading = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep((long) (1000 / 20));
						ActionDecoder.decode(client.read(String.class)).attach();
					}
				} catch (IOException | InterruptedException e) {
					Platform.runLater(new Runnable() {
						public void run() {
							ErrorHandler.handle("Could not recieve message.", e);
						}
					});
				}
			}
		});
		reading.setDaemon(true);
		reading.start();
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
				int id = client.read(Integer.class);
				Image image = client.readImage();
				textures.put(id, image);
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
			if(hasBackground) {
				String scaling = client.read(String.class);
				map.setScaling(scaling.equals("fit") ? ScaleMode.FIT : (scaling.equals("stretch") ? ScaleMode.STRETCH : ScaleMode.EXTEND));
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