package controller;

import java.awt.Point;
import java.io.IOException;
import java.util.Optional;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class ServerController extends MapEditorController {
 
    @FXML
    private VBox vbox;
    @FXML
    private Button resync;
    @FXML
    private Button reconnect;
    @FXML
    private CheckBox chkbxBuffer;
    @FXML
    private Button btnPush;
	
	private ServerGameHandler gameHandler;
	private ObjectSelectorController osController;
	
	private Entity selected = null;
	
	@FXML
	@Override
	public void initialize() {
		super.initialize();
		chkbxBuffer.selectedProperty().addListener((obs, oldVal, newVal) -> {
			if(oldVal && gameHandler.getBufferedActions().length()>0) {
				ButtonType cont = new ButtonType("continue", ButtonBar.ButtonData.OK_DONE);
				ButtonType push = new ButtonType("push updates", ButtonBar.ButtonData.APPLY);
				ButtonType cancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

				Alert alert = new Alert(AlertType.WARNING, "If you continue, these changes will be undone. If you click 'push updates' the updates will be send before clearing the buffer.", cont, push, cancel);
				alert.setTitle("Are you sure?");
				alert.setHeaderText("There are one or more changes stored in the buffer.");
				Optional<ButtonType> result = alert.showAndWait();
				if(result.orElse(cancel)==cont) {
					gameHandler.undoBuffer();
				} else if(result.orElse(cancel)==push) {
					gameHandler.pushUpdates();
				} else {
					chkbxBuffer.selectedProperty().set(oldVal);
					return;
				}
			}				
			btnPush.setDisable(!newVal);
			gameHandler.setBufferUpdates(newVal);
		});
		try {
			JSONManager.initialize();
		} catch (IOException e) {
			ErrorHandler.handle("Stored data could not be read.", e);
		}
	}
	
	public void endInit() {
		try {
			FXMLLoader loader = new FXMLLoader(ServerController.class.getResource("/assets/fxml/ObjectSelector.fxml"));
			Parent root = loader.load();
			osController = loader.getController();
			osController.setController(this);
			vbox.getChildren().add(root);
			
			loader = new FXMLLoader(ServerController.class.getResource("/assets/fxml/PropertyEditor.fxml"));
			root = loader.load();
			setPropertyEditor(loader.getController());
			vbox.getChildren().add(root);
			
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
			propeditor.setProperties(entity.getProperties());
		} else {
			if(selected != null) {
				move(selected, getTileOnPosition(event.getX(), event.getY()));
				selected = null;
			} else
				super.handleClick(p, event);
		}
	}
	
	private void move(Entity entity, Point p) {
		gameHandler.sendUpdate(ActionEncoder.movement(entity.getTileX(), entity.getTileY(), p.x, p.y),
				ActionEncoder.movement(p.x, p.y, entity.getTileX(), entity.getTileY()));
		entity.setLocation(p);
		drawMap();
	}
	
	@FXML
	public void pushClicked(ActionEvent e) {
		gameHandler.pushUpdates();
	}

	@FXML
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