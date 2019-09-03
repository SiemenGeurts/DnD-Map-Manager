package controller;

import java.io.IOException;

import app.MapManagerApp;
import app.ServerGameHandler;
import gui.ErrorHandler;
import helpers.JSONManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class ServerController extends MapEditorController {

    @FXML
    private AnchorPane selectorPane;
    @FXML
    private Button resync;
    @FXML
    private Button reconnect;
	
	private ServerGameHandler gameHandler;
	private ObjectSelectorController osController;
	
	@FXML
	@Override
	public void initialize() {
		super.initialize();		
		try {
			JSONManager.initialize();
		} catch (IOException e) {
			ErrorHandler.handle("Stored data could not be read.", e);
		}
	}
	
	public void endInit() {
		try {
			FXMLLoader loader = new FXMLLoader(MainMenuController.class.getResource("../assets/fxml/ObjectSelector.fxml"));
			Parent root = loader.load();
			osController = loader.getController();
			osController.setController(this);
			selectorPane.getChildren().add(root);
			AnchorPane.setTopAnchor(root, 0d);
			AnchorPane.setBottomAnchor(root, 0d);
			AnchorPane.setLeftAnchor(root, 0d);
			AnchorPane.setRightAnchor(root, 0d);
			
			MapManagerApp.stage.setResizable(true);
			MapManagerApp.stage.setMaximized(true);
			drawBackground();
			drawMap();
		} catch(IOException e) {
			ErrorHandler.handle("Selection pane could not be loaded.", e);
		}
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