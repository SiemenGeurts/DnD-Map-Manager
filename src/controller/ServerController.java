package controller;

import java.awt.Point;
import java.io.IOException;

import actions.ActionEncoder;
import app.MapManagerApp;
import app.ServerGameHandler;
import data.mapdata.Entity;
import data.mapdata.Map;
import gui.ErrorHandler;
import helpers.Logger;
import helpers.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ServerController extends MapEditorController {
 
    @FXML
    private VBox vbox;
    @FXML
    private Circle iconConnected;
    @FXML
    private Button resync;
    @FXML
    private CheckBox chkbxBuffer;
    @FXML
    private Button btnPush;
    @FXML
    private Button btnSendImage;
    @FXML
    private Button btnSendText;
	
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
    
    //Level management
    @FXML
    private ChoiceBox<String> cbServerLevel;
    @FXML
    private ChoiceBox<String> cbClientLevel;
    @FXML
    private Button btnLock;
    @FXML
    private HBox hboxLevels;
    static final Image IMG_LINKED = new Image("assets/images/icons/linked.png");
	static final Image IMG_UNLINKED = new Image("assets/images/icons/unlinked.png");
	private boolean locked = true;
    
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
	
	@FXML
	@Override
	void onQuit() {
		if(checkSaved()) {
			gameHandler.disconnect();
			MainMenuController.sceneManager.popScene();
		}
	}
	
	@FXML
	void onDisconnect(ActionEvent event) {
		gameHandler.disconnect();
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
			
			ImageView imgView = new ImageView(IMG_LINKED);
			imgView.setFitHeight(20);
			imgView.setFitWidth(31);
			btnLock.setGraphic(imgView);
			btnLock.setDisable(true);
			cbClientLevel.setDisable(true);
			
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
		if(event.getButton() == MouseButton.PRIMARY) {
			Entity e;
			if((e=getMap().getActiveLevel().getEntity(p)) != null) {
				selected = e;
			} else if(selected != null){
				move(selected, p);
				selected = null;
			} else
				super.handleClick(p, event);
		} else {
			super.handleClick(p, event);
		}
	}
	
	@Override
	public void handleDrag(Point2D old, Point2D cur, MouseEvent e) {
		if(inPreview || !gameHandler.isBufferEnabled()) return;
		super.handleDrag(old, cur, e);
	}
	
	private void move(Entity entity, Point p) {
		gameHandler.sendUpdate(ActionEncoder.movement(getMap().getActiveLevelIndex(), entity.getTileX(), entity.getTileY(), p.x, p.y, entity.getID()),
				ActionEncoder.movement(getMap().getActiveLevelIndex(), p.x, p.y, entity.getTileX(), entity.getTileY(), entity.getID()));
		entity.setLocation(p);
		redraw();
	}
	
	@FXML
	public void pushClicked(ActionEvent e) {
		gameHandler.pushUpdates();
	}

	public void setConnected(final boolean connected) {
		Utils.safeRun(() -> {
			btnSendImage.setDisable(!connected);
			btnSendText.setDisable(!connected);
			//TODO btnLock.setDisable(!connected);
			if(connected == true) {
				resync.setDisable(false);
				resync.setText("Resync");
				iconConnected.fillProperty().set(Color.rgb(33,255,95));
			} else {
				resync.setText("Reconnect");
				iconConnected.fillProperty().set(Color.rgb(255,31,31));
			}
		});
	}
	
	@FXML
	public void onResyncClicked(ActionEvent e) {
		if(resync.getText().equals("Resync"))
			gameHandler.resync(false);
		else {
			gameHandler.reconnect();
			resync.setText("Reconnecting...");
			resync.setDisable(true);
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
	
	@FXML
	public void sendTextClicked(ActionEvent e) {
		gameHandler.sendText();
	}
	
	@FXML
	public void onLockClicked(ActionEvent e) {
		locked = !locked;
		if(locked) {
			((ImageView)btnLock.getGraphic()).setImage(IMG_LINKED);
			cbClientLevel.setDisable(true);
			cbClientLevel.getSelectionModel().select(cbServerLevel.getSelectionModel().getSelectedIndex());
		} else {
			((ImageView)btnLock.getGraphic()).setImage(IMG_UNLINKED);
			cbClientLevel.setDisable(false);
		}
	}
	
	@Override
	public void setMap(Map map) {
		super.setMap(map);
		cbServerLevel.getItems().clear();
		cbClientLevel.getItems().clear();
		for(Map.Level level : map.getLevels()) {
			cbServerLevel.getItems().add(level.getName());
			cbClientLevel.getItems().add(level.getName());
		}
		cbServerLevel.getSelectionModel().select(map.getActiveLevelIndex());
		cbClientLevel.getSelectionModel().select(map.getActiveLevelIndex());
		
		cbServerLevel.setDisable(false);
		map.addActiveLevelChangedListener(activeLevelChangedListener);
		
		cbServerLevel.setOnAction(event -> {
			int index = cbServerLevel.getSelectionModel().getSelectedIndex();
			if(index != getMap().getActiveLevelIndex() && gameHandler.maskChanged) {
				ErrorHandler.handle("Cannot change level while fog of war has been edited", null);
				return;
			}
			getMap().setActiveLevel(index);
			if(locked)
				cbClientLevel.getSelectionModel().select(index);
		});
		
		cbClientLevel.setOnAction(event -> {
			Logger.println("Client level: " + cbClientLevel.getSelectionModel().getSelectedIndex());
		});
		hboxLevels.setVisible(true);
	}
	
	@Override
	@FXML
	void onOpen() throws IOException {
		super.onOpen();
		gameHandler.setMap(getMap(), key); //to ensure that this map becomes a ServerMap
		ilController.clear();
		gameHandler.sendForcedUpdate(ActionEncoder.clearInitiative());
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
	
	public InitiativeListController getILController() {
		return ilController;
	}
	
	public void setGameHandler(ServerGameHandler _gameHandler) {
		gameHandler = _gameHandler;
	}
	
	private Map.LevelChangedListener activeLevelChangedListener = new Map.LevelChangedListener() {
		
		@Override
		public void onActiveLevelChanged(int oldLevel, int newLevel) {
			if(cbServerLevel != null) {
				if(cbServerLevel.getSelectionModel().getSelectedIndex() != newLevel)
					cbServerLevel.getSelectionModel().select(newLevel);
			}
		}
	};
	
}