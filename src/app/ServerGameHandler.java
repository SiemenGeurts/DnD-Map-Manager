package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import actions.ActionEncoder;
import comms.Server;
import controller.MainMenuController;
import controller.SceneManager;
import controller.ServerController;
import data.mapdata.Map;
import data.mapdata.Tile;
import gui.ErrorHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

public class ServerGameHandler extends GameHandler {
	
	Server server;
	
	ServerController controller;
	
	private FileChooser mapChooser;
	
	public ServerGameHandler(Server _server) {
		server = _server;
		mapChooser = new FileChooser();
		List<FileChooser.ExtensionFilter> extensionFilters = mapChooser.getExtensionFilters();
		extensionFilters.add(new FileChooser.ExtensionFilter("Map files (*.map)", "*.map"));
		try {
			FXMLLoader loader = new FXMLLoader(ServerGameHandler.class.getResource("../assets/fxml/ServerPlayScreen.fxml"));
			Scene scene = new Scene(loader.load());
			scene.getRoot().requestFocus();
			controller = loader.getController();
			controller.setGameHandler(this);
			loadMap();
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
	
	public void loadMap() throws IOException {
		mapChooser.setTitle("Load map");
		File file = mapChooser.showOpenDialog(SceneManager.getPrimaryStage());
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();
		while (line != null) {
			sb.append(line);
			line = br.readLine();
		}
		br.close();
		Map m = Map.decode(new String(sb));
		if (m != null)
			map = m;
		controller.currentMap = map;
		controller.drawMap(0,0, map.getWidth(), map.getHeight());
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
				textures.put(t.getType(), t.getTexture());
			}
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
	
	public void sendUpdate(String str) throws IOException {
		server.write(str);
	}
}