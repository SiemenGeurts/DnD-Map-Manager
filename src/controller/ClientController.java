package controller;

import app.ClientGameHandler;
import app.MapManagerApp;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class ClientController extends MapController {
	ClientGameHandler gameHandler;
	
	public void setGameHandler(ClientGameHandler _gameHandler) {
		gameHandler = _gameHandler;
	}
	
	@FXML
	@Override
	public void initialize() {
		MapManagerApp.stage.setResizable(true);
		MapManagerApp.stage.setMaximized(true);
		drawBackground();
		drawMap();
	}
	
	@FXML
	void onMouseClicked(MouseEvent event) {
    	if(mousePressedCoords.distance(event.getX(), event.getY())>TILE_SIZE*SCALE/2) return;
    	System.out.println("mouse clicked [touch=" + event.isSynthesized() + "; x=" + event.getX() + ", y=" + event.getY() + "]");
    	if(event.isPrimaryButtonDown() || event.isSynthesized()) {
    		
    	}
	}

}