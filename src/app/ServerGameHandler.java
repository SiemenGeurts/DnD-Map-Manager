package app;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;

import actions.Action;
import actions.ActionDecoder;
import actions.ActionEncoder;
import comms.Server;
import controller.MainMenuController;
import controller.SceneManager;
import controller.ServerController;
import data.mapdata.Map;
import data.mapdata.ServerMap;
import data.mapdata.Tile;
import data.mapdata.Entity;
import gui.ErrorHandler;
import helpers.IOHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

public class ServerGameHandler extends GameHandler {
	
	Server server;
	
	ServerController controller;
	public static ServerGameHandler instance;
	
	private FileChooser mapChooser;
	private StringBuilder updates;
	private Stack<String> undo;
	private boolean bufferUpdates = false;
	
	public ServerGameHandler(Server _server) {
		server = _server;
		updates = new StringBuilder();
		undo = new Stack<>();
		instance = this;
		mapChooser = new FileChooser();
		mapChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Map files (*.map)", "*.map"));
		try {
			FXMLLoader loader = new FXMLLoader(ServerGameHandler.class.getResource("/assets/fxml/ServerPlayScreen.fxml"));
			Scene scene = new Scene(loader.load());
			scene.getRoot().requestFocus();
			controller = loader.getController();
			controller.setGameHandler(this);
			while(!loadMap());
	        MainMenuController.sceneManager.pushView(scene, loader);
	        controller.endInit();
			//wait for the DM clicks a "begin" button
			//all interaction will be handled by a javafx controller class
			//all communication and gameplay will be handled by this gamehandler.
		} catch (IOException e) {
			ErrorHandler.handle("Well, something went horribly wrong...", e);
		}
	}
	
	public void begin() {
		//after the begin button has been clicked, execute the following sequence.
		acceptClient();
		sendTextures();
		sendMap();
		startGame();
	}
	
	public void resync() {
		try {
			server.write(ActionEncoder.reset());
			sendMap();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void reconnect() throws IOException {
		int port = server.server.getLocalPort();
		server.server.close();
		server = Server.create(port);
		begin();
	}
	
	public boolean loadMap() throws IOException {
		mapChooser.setTitle("Load map");
		File file = mapChooser.showOpenDialog(SceneManager.getPrimaryStage());
		Map m = IOHandler.loadMap(file);
		controller.currentFile = file;
		if (m == null) return false;
		map = new ServerMap(m, this);
		controller.setMap(map);
		return true;
	}
	
	public void startGame() {
		
	}
	
	public void acceptClient() {
		try {
			server.waitForClient();
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Connection established.");
			alert.setHeaderText("A client connected to the game.");
			alert.setContentText("The players are now able to view the visible regions.");
			alert.show();
		} catch(IOException e) {
			ErrorHandler.handle("Something went wrong while waiting for a client connection.", e);
		}
	}
	
	public boolean sendTextures() {
		System.out.println("[SERVER] sending textures.");
		HashMap<Integer, Image> textures = new HashMap<>();
		for(int i = 0; i < map.getWidth(); i++)
			for(int j = 0; j < map.getHeight(); j++) {
				Tile t = map.getTile(i, j);
				if(t.getType()>=0)
					textures.put(t.getType(), t.getTexture());
			}
		for(Entity e : map.getEntities())
			textures.put(e.getType(), e.getTexture());
		try {
			server.write(String.valueOf(textures.size()));
			for(Entry<Integer, Image> pair : textures.entrySet()) {
				server.write(String.valueOf(pair.getKey()));
				server.write(pair.getValue());
			}
		} catch(IOException e) {
			ErrorHandler.handle("Not all textures could be transferred. Please try again.", e);
			return false;
		}
		return true;
	}
	
	public boolean sendMap() {
		System.out.println("[SERVER] sending map.");
		try {
			server.write(map.encode());
		} catch (IOException e) {
			ErrorHandler.handle("Could not send map. Please try again", e);
			return false;
		}
		return true;
	}
	
	public void sendUpdate(String action, String undoAction) {
		if(bufferUpdates)
			updates.append(action).append(';');
		else
			try {		
				server.write(action);
			} catch(IOException e) {
				ErrorHandler.handle("Update [" + action + "] could not be send. Try resyncing the game.", e);
			}
		undo.push(undoAction);
	}
	
	public void pushUpdates() {
		if(bufferUpdates)
			try {
				server.write(updates.toString());
				updates = new StringBuilder();
			} catch (IOException e) {
				ErrorHandler.handle("Update [" + updates.toString() + "] could not be send. Try resyncing the game.", e);
			}
	}
	
	public void undo() {
		String[] s = undo.pop().split(";");
		for(String line : s) {
			Action action = ActionDecoder.decode(line, true);
			action.setDelay(0);
			action.update(0);
			updates.delete(updates.lastIndexOf(";"), updates.length());
		}
	}
	
	public void undoBuffer() {
		long count = updates.chars().filter(ch -> ch == ';').count();
		for(int i = 0; i < count; i++)
			undo();
		updates = new StringBuilder();
	}
	
	public void setBufferUpdates(boolean buffer) {
		bufferUpdates = buffer;
	}
	
	public String getBufferedActions() {
		return updates.toString();
	}
	
	public ServerController getController() {
		return controller;
	}
}