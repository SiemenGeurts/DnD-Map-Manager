package controller;

import java.io.IOException;

import app.ErrorHandler;
import app.ServerGameHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class ServerController extends MapController {

    @FXML
    private ListView<?> lv;
    @FXML
    private Button resync;
    @FXML
    private Button reconnect;
	
	private ServerGameHandler gameHandler;
	
	@FXML
	@Override
	public void initialize() {
		super.initialize();
	}
	
	public void beginClicked(ActionEvent e) {
		if(resync.getText().equals("begin")) {
			reconnect.setDisable(false);
			resync.setText("resync");
			gameHandler.begin();
		} else {
			gameHandler.resync();
		}
	}
	
	public void reconnectClicked(ActionEvent e) {
		try {
			gameHandler.reconnect();
		} catch(IOException ex) {
			ErrorHandler.handle("Could not reopen server.", ex);
		}
	}
	
	public void setGameHandler(ServerGameHandler _gameHandler) {
		gameHandler = _gameHandler;
	}
}
