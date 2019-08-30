package app;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import comms.Server;
import controller.ServerController;
import data.mapdata.AssetManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;

public class ServerGameHandler extends GameHandler {
	
	Server server;
	
	ServerController controller;
	
	public ServerGameHandler(Server _server) {
		server = _server;
		//load a map
		//wait for the DM clicks a "begin" button
		//all interaction will be handled by a javafx controller class
		//all communication and gameplay will be handled by this gamehandler.
		
		//after the begin button has been clicked, execute the following sequence.
		acceptClient();
		sendTextures();
		sendMap();
		startGame();
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
			alert.showAndWait();
		} catch(IOException e) {
			ErrorHandler.handle("Something went wrong while waiting for a client connection.", e);
		}
	}
	
	public boolean sendTextures() {
		HashMap<Integer, Image> textures = AssetManager.textures;
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
		try {
			server.write(map.toString());
		} catch (IOException e) {
			ErrorHandler.handle("Could not send map. Please try again", e);
			return false;
		}
		return true;
	}
}
