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
			FXMLLoader loader = new FXMLLoader(ServerGameHandler.class.getResource("../assets/fxml/ClientPlayScreen.fxml"));
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
					ActionDecoder.decode(client.read()).attach();
				} catch (IOException e) {
					ErrorHandler.handle("Could not recieve message", e);
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
						Thread.sleep((long)(1000/20));
						update(System.currentTimeMillis()-time);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} while(true);
			}
		});
		updating.setDaemon(true);
		updating.start();
	}
	
	public void update(float dt) {
		for(Action a : actions) {
			currentAction = a;
			a.update(dt);
		}
		currentAction = null;
	}
	
	public boolean loadTextures() {
		System.out.println("[CLIENT] Loading textures.");
		try {
			int amount = Integer.valueOf(client.read());
			HashMap<Integer, Image> textures = new HashMap<>(amount);
			for(int i = 0; i < amount; i++) {
				int id = Integer.valueOf(client.read());
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
		System.out.println("[CLIENT] Loading map.");
		try {
			map = Map.decode(client.read());
			controller.currentMap = map;
			controller.drawMap();
		} catch (IOException e) {
			ErrorHandler.handle("The map could not be loaded. Please try again.", e);
			return false;
		}
		return true;
	}
	
    public Action insertAction(Action action) {
        if(currentAction == null) {
            action.attach();
            return action;
        } else
            return currentAction.insertAction(action);
    }
}
