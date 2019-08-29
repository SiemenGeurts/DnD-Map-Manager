package controller;

import app.ClientGameHandler;

public class ClientMapController extends MapController {
	ClientGameHandler gameHandler;
	
	public ClientMapController(ClientGameHandler _gameHandler) {
		gameHandler = _gameHandler;
	}

}
