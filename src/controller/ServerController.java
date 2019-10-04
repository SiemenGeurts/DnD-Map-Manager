package controller;

import java.awt.Point;
import java.io.IOException;

import actions.ActionEncoder;
import app.MapManagerApp;
import app.ServerGameHandler;
import data.mapdata.Entity;
import data.mapdata.Map;
import gui.ErrorHandler;
import helpers.JSONManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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
	
    //Preview stuff
    @FXML
    private Button btnTogglePreview;
    @FXML
    private Button btnDeclinePreview;
    @FXML
    private Button btnAcceptPreview;
    @FXML
    private HBox hboxPreviewTools;
    
    public Map previewMap, oldMap;
    public boolean inPreview = false;
    
	private ServerGameHandler gameHandler;
	private ObjectSelectorController osController;
	
	private Entity selected = null;
	
	@FXML
	@Override
	public void initialize() {
		super.initialize();
		chkbxBuffer.selectedProperty().addListener((obs, oldVal, newVal) -> {
			if(oldVal) {
				if(!gameHandler.requestDisableUpdateBuffer()) {
					chkbxBuffer.setSelected(true);
					btnPush.setDisable(true);
				}
			} else {
				btnPush.setDisable(!newVal);
				gameHandler.enableUpdateBuffer();
			}
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
		if(inPreview) return;
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
		gameHandler.sendUpdate(ActionEncoder.movement(entity.getTileX(), entity.getTileY(), p.x, p.y, entity.getID()),
				ActionEncoder.movement(p.x, p.y, entity.getTileX(), entity.getTileY(), entity.getID()));
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

	@FXML
	public void onBtnTogglePreviewClicked(ActionEvent e) {
		togglePreview();
	}
	@FXML
	public void onBtnAcceptPreviewClicked(ActionEvent e) {
		gameHandler.previewAccepted();
		disablePreview();
	}
	@FXML
	public void onBtnDeclinePreviewClicked(ActionEvent e) {
		gameHandler.previewDeclined();
		disablePreview();
	}
	
	public void togglePreview() {
		inPreview = !inPreview;
		if(inPreview) {
			btnTogglePreview.setText("Hide Preview");
			currentMap = previewMap;
		} else {
			currentMap = oldMap;
			btnTogglePreview.setText("Show Preview");
		}
		redraw();
	}
	
	public void showPreview(Map previewMap) {
		if(!inPreview) {
			oldMap = currentMap;
			this.previewMap = previewMap;
			togglePreview();
			hboxPreviewTools.setVisible(true);
		}
	}
	
	public void disablePreview() {
		inPreview = false;
		currentMap = oldMap;
		hboxPreviewTools.setVisible(false);
		redraw();
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