package controller;

import java.awt.Point;
import java.io.IOException;

import actions.ActionEncoder;
import app.MapManagerApp;
import app.ServerGameHandler;
import data.mapdata.Entity;
import data.mapdata.Map;
import gui.ErrorHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    @FXML
    private Button btnSendImage;
	
    //Preview stuff
    @FXML
    private Button btnTogglePreview;
    @FXML
    private Button btnDeclinePreview;
    @FXML
    private Button btnAcceptPreview;
    @FXML
    private HBox hboxPreviewTools;
    @FXML
    private CheckMenuItem chkboxViewGrid;
    
    //Stats pane stuff
    @FXML
    private AnchorPane statsPane;
    @FXML
    private AnchorPane initiativePane;
    
    public Map previewMap, oldMap;
    public boolean inPreview = false;
    
	private ServerGameHandler gameHandler;
	private InitiativeListController ilController;
	
	private Entity selected = null;
	

	public static final class Key { private Key() {}}
	private static Key key = new Key();
	
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
		
		chkboxViewGrid.selectedProperty().addListener((obs, oldVal, newVal) -> setViewGrid(newVal));
	}
	
	public void endInit() {
		try {
			FXMLLoader loader = new FXMLLoader(ServerController.class.getResource("/assets/fxml/PaintPane.fxml"));
			Parent root = loader.load();
			paintController = loader.getController();
			vbox.getChildren().add(root);
			VBox.setVgrow(root, Priority.NEVER);
			
			loader = new FXMLLoader(ServerController.class.getResource("/assets/fxml/ObjectSelector.fxml"));
			root = loader.load();
			osController = loader.getController();
			osController.setController(this);
			vbox.getChildren().add(root);
			
			loader = new FXMLLoader(ServerController.class.getResource("/assets/fxml/PropertyEditor.fxml"));
			root = loader.load();
			setPropertyEditor(loader.getController());
			vbox.getChildren().add(root);
			
			loader = new FXMLLoader(ServerController.class.getResource("/assets/fxml/ProbabilityCalculator.fxml"));
			root = loader.load();
			statsPane.getChildren().add(root);
			AnchorPane.setBottomAnchor(root, 0d);
			AnchorPane.setLeftAnchor(root, 0d);
			AnchorPane.setRightAnchor(root, 0d);
			AnchorPane.setTopAnchor(root, 0d);
			
			loader = new FXMLLoader(ServerController.class.getResource("/assets/fxml/InitiativeList.fxml"));
			root = loader.load();
			initiativePane.getChildren().add(root);
			AnchorPane.setBottomAnchor(root, 0d);
			AnchorPane.setLeftAnchor(root, 0d);
			AnchorPane.setRightAnchor(root, 0d);
			AnchorPane.setTopAnchor(root, 0d);
			ilController = loader.getController();
			ilController.setGameHandler(gameHandler);
			ilController.setMapController(this);
			MenuItem item = new MenuItem("Initiative");
			item.setOnAction(event -> {
				ilController.addEntity(entityMenu.selected);
			});
			entityMenu.addEntityMenuItem(item);
			
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
		if(inPreview || !gameHandler.isPlaying) return;
		super.handleClick(p, event);
		if(event.getButton() == MouseButton.PRIMARY) {
			Entity e;
			if((e=getMap().getEntity(p)) != null) {
				selected = e;
			} else if(selected != null){
				move(selected, p);
				selected = null;
			}
		}
	}
	
	@Override
	public void handleDrag(Point2D old, Point2D cur, MouseEvent e) {
		if(inPreview || !gameHandler.isPlaying || !gameHandler.isBufferEnabled()) return;
		super.handleDrag(old, cur, e);
	}
	
	private void move(Entity entity, Point p) {
		gameHandler.sendUpdate(ActionEncoder.movement(entity.getTileX(), entity.getTileY(), p.x, p.y, entity.getID()),
				ActionEncoder.movement(p.x, p.y, entity.getTileX(), entity.getTileY(), entity.getID()));
		entity.setLocation(p);
		redraw();
	}
	
	@FXML
	public void pushClicked(ActionEvent e) {
		gameHandler.pushUpdates();
	}

	@FXML
	public void beginClicked(ActionEvent e) {
		if(resync.getText().equals("begin")) {
			gameHandler.begin();
			reconnect.setDisable(false);
			resync.setText("resync");
			btnSendImage.setDisable(false);
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
	
	@FXML
	public void sendImageClicked(ActionEvent e) {
		gameHandler.sendImage();
	}
	
	@Override
	@FXML
	void onOpen() throws IOException {
		super.onOpen();
		gameHandler.setMap(getMap(), key);
	}
	
	public void togglePreview() {
		inPreview = !inPreview;
		if(inPreview) {
			btnTogglePreview.setText("Hide Preview");
			setMap(previewMap);
		} else {
			setMap(oldMap);
			btnTogglePreview.setText("Show Preview");
		}
	}
	
	public void showPreview(Map previewMap) {
		if(!inPreview) {
			oldMap = getMap();
			this.previewMap = previewMap;
			togglePreview();
			hboxPreviewTools.setVisible(true);
		}
	}
	
	public void disablePreview() {
		inPreview = false;
		setMap(oldMap);
		hboxPreviewTools.setVisible(false);
	}
	
	public void reconnectClicked(ActionEvent e) {
		try {
			gameHandler.reconnect();
		} catch(IOException ex) {
			ErrorHandler.handle("Could not reopen server.", ex);
		}
	}
	
	public InitiativeListController getILController() {
		return ilController;
	}
	
	public void setGameHandler(ServerGameHandler _gameHandler) {
		gameHandler = _gameHandler;
	}
	
}