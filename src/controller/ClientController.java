package controller;

import app.ClientGameHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class ClientController extends MapController {
	ClientGameHandler gameHandler;
	
	public void setGameHandler(ClientGameHandler _gameHandler) {
		gameHandler = _gameHandler;
	}

}