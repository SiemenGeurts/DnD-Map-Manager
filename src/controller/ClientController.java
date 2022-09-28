package controller;

import java.awt.Point;
import java.io.IOException;
import java.util.Optional;

import app.ClientGameHandler;
import app.Constants;
import app.MapManagerApp;
import data.mapdata.Entity;
import gui.ErrorHandler;
import helpers.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class ClientController extends MapController {
	ClientGameHandler gameHandler;
	Entity selected = null;
	
	@FXML
	private CheckBox chkboxBuffer;
	@FXML
	private Button btnPush;
	@FXML
	private CheckBox chkboxViewGrid;
	@FXML
	private VBox vbox;
	@FXML
	private AnchorPane initiativePane;
	
	InitiativeListController ilController;
	
	public void setGameHandler(ClientGameHandler _gameHandler) {
		gameHandler = _gameHandler;
	}
	
	@FXML
	@Override
	public void initialize() {
		super.initialize();
		MapManagerApp.stage.setResizable(true);
		MapManagerApp.stage.setMaximized(true);
		chkboxBuffer.selectedProperty().addListener((obs, oldVal, newVal) -> {
			if(oldVal == newVal) return;
			if(!newVal) { //buffer to be disabled
				if(gameHandler.requestDisableUpdateBuffer()) {
					btnPush.setDisable(true);
				} else {
					chkboxBuffer.setSelected(true);
					btnPush.setDisable(false);
				}
			} else { // buffer enabled
				btnPush.setDisable(false);
				gameHandler.enableUpdateBuffer();
			}
		});
		chkboxViewGrid.selectedProperty().addListener((obs, oldVal, newVal) -> setViewGrid(newVal));
		
		try {	
			FXMLLoader loader = new FXMLLoader(ServerController.class.getResource("/assets/fxml/InitiativeList.fxml"));
			Node root = loader.load();
			initiativePane.getChildren().add(root);
			AnchorPane.setBottomAnchor(root, 0d);
			AnchorPane.setTopAnchor(root, 0d);
			AnchorPane.setLeftAnchor(root, 0d);
			AnchorPane.setRightAnchor(root, 0d);
			ilController = loader.getController();
			ilController.setMode(Constants.CLIENTMODE);
			ilController.setMapController(this);
		} catch (IOException e) {
			ErrorHandler.handle("Couldn't load initiative bar.",e);
		}
	}
	
	public InitiativeListController getInitiativeController() {
		return ilController;
	}
	
	@FXML
	void onQuit() {
		gameHandler.disconnect();
	}
	
	@FXML
	void onBtnPushClicked(ActionEvent event) {
		gameHandler.pushUpdates();
	}
	
	@FXML
	void onDisconnect(ActionEvent event) {
		ButtonType disconnect = new ButtonType("Disconnect", ButtonBar.ButtonData.OK_DONE);
		ButtonType resync = new ButtonType("Resync", ButtonBar.ButtonData.APPLY);
		ButtonType cancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

		Alert alert = new Alert(AlertType.WARNING, "Do you really want to disconnect or first try resyncing?", disconnect, resync, cancel);
		alert.setTitle("Are you sure?");
		alert.setHeaderText("Disconnect");
		Optional<ButtonType> result = alert.showAndWait();
		
		if(result.orElse(cancel) == cancel) {
			return;
		} else if(result.orElse(cancel)==resync) {
			gameHandler.requestResync();
		} else if(result.orElse(cancel)==disconnect) {
			onQuit();
		}
	}
	
	@Override
	protected void handleClick(Point p, MouseEvent event) {
		Logger.println("Mouse clicked: " + p + " : " + event.isPrimaryButtonDown());
    	if(event.getButton() == MouseButton.PRIMARY || event.isSynthesized()) {
    		Entity e = getMap().getEntity(p);
    		if(e != null)
    			Logger.println("Clicked on entity: " + e.getName() + " NPC: " + e.isNPC());
    		if(e == null && selected != null) {
    			gameHandler.move(selected, p);
    			selected = null;
    		} else if(selected == null && (e != null && !e.isNPC())) {
    			selected = e;
    		}
    	}
	}
}