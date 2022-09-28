package app;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import actions.Action;
import actions.ActionDecoder;
import actions.ActionEncoder;
import actions.GuideLine;
import actions.MovementAction;
import comms.Message;
import comms.SerializableImage;
import comms.SerializableJSON;
import comms.SerializableMap;
import comms.Client;
import comms.ClientListener;
import controller.ClientController;
import controller.MainMenuController;
import controller.TextPaneController;
import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.PresetTile;
import data.mapdata.Tile;
import gui.Dialogs;
import gui.ErrorHandler;
import helpers.AssetManager;
import helpers.Logger;
import helpers.codecs.JSONKeys;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ClientGameHandler extends GameHandler {

	public static ClientGameHandler instance;
	ClientListener client;
	
	ClientController controller;
	
	private Action undoAction;
	private LinkedList<JSONObject> updates;
	private boolean bufferUpdates = true, awaitingResponse = false;
	
	public ClientGameHandler(Client c) {
		super();
		client = new ClientListener(c, () -> onDisconnected());
		instance = this;
		undoAction = Action.empty();
		updates = new LinkedList<>();
		AssetManager.initializeManager(false);
		try {
			FXMLLoader loader = new FXMLLoader(
					ClientGameHandler.class.getResource("/assets/fxml/ClientPlayScreen.fxml"));
			Scene scene = new Scene(loader.load());
			scene.getRoot().requestFocus();
			controller = loader.getController();
			controller.setGameHandler(this);
			MainMenuController.sceneManager.pushView(scene, loader);
		} catch (IOException e) {
			ErrorHandler.handle("Well, something went horribly wrong...", e);
		}
		start();
		try {
			PresetTile.setupPresetTiles();
		} catch (IOException e) {
			ErrorHandler.handle("Could not load preset textures.", e);
		}
	}
	
	@Override
	protected void processMessages() {
		Message<?> msg;
		while((msg = client.readMessage())!=null) {
			Logger.println("Processing message of type " + msg.getMessage().getClass().getSimpleName());
			if(msg.getMessage() instanceof SerializableImage) {
				SerializableImage si = (SerializableImage) msg.getMessage();
				if(si.shouldShow())
					displayImage(si.getImage());
				else {
					AssetManager.putTexture(si.getId(), si.getImage());
					controller.redraw();
					if(controller.getInitiativeController().hasEntityWithId(si.getId())) {
						controller.getInitiativeController().redraw();
					}
				}
			} else if(msg.getMessage() instanceof SerializableMap) {
				Map newMap = ((SerializableMap) msg.getMessage()).getMap();
				if(((SerializableMap) msg.getMessage()).hasBackground() && newMap.getBackground()==null)
					newMap.setBackground(map.getBackground());
				requestMissingTextures(newMap);
				map = newMap;
				clearInitiative();
				controller.setMap(newMap);
				controller.redraw();
			} else if(msg.getMessage() instanceof SerializableJSON) {
				parseJSON(((SerializableJSON) msg.getMessage()).getJSON());
			} else
				ErrorHandler.handle("Received message of type " + msg.getMessage().getClass().getSimpleName() + " and didn't know what to do with it...", null);
		}
	}
	
	private void parseJSON(JSONObject json) {
		try {
			Logger.println("JSON type: " + json.getString("type"));
			Action action = ActionDecoder.decode(json, false);
			if(action != null) {
				action.attach();
				return;
			}
			switch(json.getString("type")) {
			case Constants.JSON_TYPE_SHOW_TEXT:
				int n = json.getInt("num_pages");
				String[] pages = new String[n];
				for(int i = 0; i < n; i++)
					pages[i] = json.getString("page"+(i+1));
				displayText(pages);
				break;
			
			default:
				ErrorHandler.handle("Unknown JSON type: " + json.getString("type"), null);
			}
		} catch(JSONException e) {
			ErrorHandler.handle("Could not parse JSON", e);
		}
	}
	
	public void disconnect() {
		client.disconnect();
	}
	
	private void onDisconnected() {
		stop();
		Dialogs.warning("Disconnected from server.", false);
		//otherwise it some error is thrown when trying to reconnect...
		Platform.exit();
	}
	
	public void requestResync() {
		client.sendMessage(ActionEncoder.requestResync());
	}
	
	public void checkTexture(int id) {
		if(id>=0 && !AssetManager.textureExists(id))
			requestTexture(id);
	}

	public void move(Entity entity, Point target) {
		if(awaitingResponse) return;
		sendUpdate(ActionEncoder.movement(entity.getTileX(), entity.getTileY(), target.x, target.y, entity.getID()));
		undoAction = new MovementAction(new GuideLine(new Point2D[] {target, entity.getLocation()}), entity, 0).addAction(undoAction);
		new MovementAction(new GuideLine(new Point2D[] {entity.getLocation(), target}), entity, 0).attach();
	}
	
	public void addInitiative(int id, double initiative) {
		controller.getInitiativeController().addEntity(map.getEntityById(id), initiative);
	}
	
	@Override
	public void selectInitiative(int id) {
		controller.getInitiativeController().select(id);
	}

	@Override
	public void removeInitiative(int id) {
		controller.getInitiativeController().remove(id);
	}
	
	@Override
	public void clearInitiative() {
		controller.getInitiativeController().clear();
	}
	
	public void requestMissingTextures(Map map) {
		for(Tile[] row : map.getTiles())
			for(Tile t : row) {
				if(t.getType()>=0 && !AssetManager.textureExists(t.getType()))
					requestTexture(t.getType());
			}
		for(Entity e : map.getEntities())
			if(e.getType()>=0 && !AssetManager.textureExists(e.getType()))
				requestTexture(e.getType());
	}

	public void requestTexture(int id) {
		try {
			client.sendMessage(ActionEncoder.requestTexture(id));
		} catch (IllegalStateException e) {
			ErrorHandler.handle("Could not send texture request or receive an answer.", e);
		}
	}
	
	public ClientController getController() {
		return controller;
	}

	public Action insertAction(Action action) {
		if (currentAction == null) {
			action.attach();
			return action;
		} else
			return currentAction.insertAction(action);
	}
	
	private boolean sendUpdate(JSONObject jobj) {
		//These are only movement updates
		//The undo action is appended by the calling method
		//see move()
		if(bufferUpdates) {
			updates.add(jobj);
			return true;
		} else {
			try {
				awaitingResponse=true;
				client.sendMessage(jobj);
				return true;
			} catch(IllegalStateException e) {
				ErrorHandler.handle("Could not send updates. Please try again.", e);
				return false;
			}
		}
	}
	
	public boolean pushUpdates() {
		try {
			awaitingResponse = true;
			JSONObject json = new JSONObject();
			json.put("type", JSONKeys.KEY_UPDATE_LIST);
			json.put("size", updates.size());
			JSONArray array = new JSONArray();
			while(updates.size()>0)
				array.put(updates.poll());
			json.put("array", array);
			client.sendMessage(json);
			undoAction = null;
			return true;
		} catch(IllegalStateException e) {
			ErrorHandler.handle("Could not send updates. Please try again.", e);
			return false;
		}
	}
	
	public boolean requestDisableUpdateBuffer() {
		if(!bufferUpdates)
			return true;
		if(updates.size()==0) {
			bufferUpdates = false;
			return true;
		}
		ButtonType cont = new ButtonType("continue", ButtonBar.ButtonData.OK_DONE);
		ButtonType push = new ButtonType("push updates", ButtonBar.ButtonData.APPLY);
		ButtonType cancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

		Alert alert = new Alert(AlertType.WARNING, "If you continue, these changes will be undone. If you click 'push updates' the updates will be send before clearing the buffer.", cont, push, cancel);
		alert.setTitle("Are you sure?");
		alert.setHeaderText("There are one or more changes stored in the buffer.");
		Optional<ButtonType> result = alert.showAndWait();
		
		if(result.orElse(cancel) == cancel) {
			return false;
		} else if(result.orElse(cancel)==cont) {
			bufferUpdates = false;
			undoBuffer();
			return true;
		} else if(result.orElse(cancel)==push) {
			bufferUpdates = false;
			return pushUpdates();
		}
		return false;
	}
	
	public void enableUpdateBuffer() {
		bufferUpdates = true;
	}
	
	public void onActionDeclined() {
		undoBuffer();
		awaitingResponse=false;
		Dialogs.info("The DM declined your movements!", false);
	}
	
	public void onActionAccepted() {
		clearBuffer();
		awaitingResponse=false;
		//Dialogs.info("The DM accepted your movements!", false);
	}
	
	public void clearBuffer() {
		updates.clear();
		undoAction = null;
	}
	
	public void undoBuffer() {
		if(undoAction != null)
			undoAction.attach();
		updates.clear();
		undoAction = null;
	}
	
	private static final int MAX_WIDTH = 1000;
	private static final int MAX_HEIGHT = 1000;
	private static Stage imgStage;
	private static ImageView view;
	private void displayImage(Image img) {
		Runnable run = () -> {
			if(imgStage == null) {
				imgStage = new Stage();
				BorderPane pane = new BorderPane();
				view = new ImageView(img);
				pane.setCenter(view);
				imgStage.setScene(new Scene(pane));
			} else
				view.setImage(img);
			double width = img.getWidth();
			double height = img.getHeight();
			if(width>MAX_WIDTH) {
				double f = MAX_WIDTH/width;
				height*=f;
				width = MAX_WIDTH;
			}
			if(height>MAX_HEIGHT) {
				double f = MAX_HEIGHT/height;
				width *= f;
				height = MAX_HEIGHT;
			}
			view.maxWidth(width);
			view.maxHeight(height);
			imgStage.showAndWait();
		};
		if(Platform.isFxApplicationThread())
			run.run();
		else
			Platform.runLater(run);
	}
	
	private static Stage textStage;
	private void displayText(String[] pages) {
		Runnable run = () -> {
			TextPaneController cont;
			try {
				FXMLLoader loader = new FXMLLoader(ClientGameHandler.class.getResource("/assets/fxml/TextPane.fxml"));
				if(textStage == null)
					textStage = new Stage();
				textStage.setScene(new Scene(loader.load()));
				cont = loader.getController();
				cont.showText(pages);
				textStage.showAndWait();
			} catch(IOException e) {
				ErrorHandler.handle("Could not open text pane", e);
				return;				
			}
		};
		if(Platform.isFxApplicationThread())
			run.run();
		else
			Platform.runLater(run);
		
	}
}