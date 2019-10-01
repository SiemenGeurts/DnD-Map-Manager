package controller;

import java.awt.Point;

import app.ClientGameHandler;
import app.MapManagerApp;
import data.mapdata.Entity;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class ClientController extends MapController {
	ClientGameHandler gameHandler;
	Entity selected = null;
	
	public void setGameHandler(ClientGameHandler _gameHandler) {
		gameHandler = _gameHandler;
	}
	
	@FXML
	@Override
	public void initialize() {
		super.initialize();
		MapManagerApp.stage.setResizable(true);
		MapManagerApp.stage.setMaximized(true);
	}
	
	@Override
	protected void handleClick(Point p, MouseEvent event) {
    	if(event.isPrimaryButtonDown() || event.isSynthesized()) {
    		Entity e = currentMap.getEntity(p);
    		if(e == null && selected != null) {
    			gameHandler.move(selected, p);
    			selected = null;
    		} else if(selected == null) {
    			selected = e;
    		}
    	}
	}
}