package controller;

import java.awt.Point;
import java.io.IOException;

import actions.ActionEncoder;
import app.MapManagerApp;
import app.ServerGameHandler;
import data.mapdata.Entity;
import gui.ErrorHandler;
import helpers.JSONManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
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
	
	private Entity selected = null;
	
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
			FXMLLoader loader = new FXMLLoader(MainMenuController.class.getResource("/assets/fxml/ObjectSelector.fxml"));
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
	
	@Override
	public void handleClick(Point p, MouseEvent event) {
		Entity entity = null;
		if((entity = currentMap.getEntity(getTileOnPosition(event.getX(), event.getY()))) != null) {
			selected = entity;
			//TODO: enable editing of properties
		} else {
			if(selected != null)
				move(selected, getTileOnPosition(event.getX(), event.getY()));
			else
				super.handleClick(p, event);
		}
	}
	
	private void move(Entity entity, Point p) {
		try {
			gameHandler.sendUpdate(ActionEncoder.movement(entity.getTileX(), entity.getTileY(), p.x, p.y));
			entity.setLocation(p);
			drawMap();
		} catch (IOException e) {
			ErrorHandler.handle("could not transmit action.", e);
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