package controller;

import app.ClientGameHandler;

public class ClientController extends MapController {
	ClientGameHandler gameHandler;
	
	public void setGameHandler(ClientGameHandler _gameHandler) {
		gameHandler = _gameHandler;
	}

}