package app;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import actions.Action;
import actions.ActionDecoder;
import actions.ActionEncoder;
import comms.Message;
import comms.SerializableJSON;
import comms.ServerListener;
import controller.CreateTextPaneController;
import controller.InitiativeListController.InitiativeEntry;
import controller.MainMenuController;
import controller.ServerController;
import controller.ToolkitController;
import data.mapdata.Map;
import data.mapdata.PresetTile;
import data.mapdata.ServerMap;
import gui.ErrorHandler;
import helpers.AssetManager;
import helpers.Logger;
import helpers.Utils;
import helpers.codecs.JSONEncoder;
import helpers.codecs.JSONKeys;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ServerGameHandler extends GameHandler {

	ServerListener server;
	public int serverport;
	
	ServerController controller;
	public static ServerGameHandler instance;

	private LinkedList<JSONObject> updates;
	private Stack<JSONObject> undo;
	private byte[][] oldMask;
	
	private boolean bufferUpdates = false;
	public boolean isPlaying = false;

	private boolean blockUpdates = false;
	
	public ServerGameHandler(int serverport) {
		super();
		instance = this;
		
		try {
			FXMLLoader loader = new FXMLLoader(
					ServerGameHandler.class.getResource("/assets/fxml/ServerPlayScreen.fxml"));
			Scene scene = new Scene(loader.load());
			scene.getRoot().requestFocus();
			controller = loader.getController();
			controller.setGameHandler(this);
			
			boolean loadedMap = false;
			if(Utils.getNamedParameter("map")!=null) {
				File f = new File(Utils.getNamedParameter("map"));
				if(f.exists()&&loadMap(f)) {
					loadedMap = true;
				} else 
					ErrorHandler.handle("Could not load map " + f.getAbsolutePath(),null);
			}
			if(!loadedMap)
				do {
					controller.currentFile = Utils.openMapDialog();
				} while(!loadMap(controller.currentFile));
			try {
				PresetTile.setupPresetTiles();
			} catch (IOException e) {
				ErrorHandler.handle("Could not load preset tiles.", e);
			}
			
			MainMenuController.sceneManager.pushView(scene, loader);
			controller.endInit();
			
			this.serverport = serverport;
			server = new ServerListener(serverport, () -> onConnected(), () -> onDisconnected());
			server.start();
			updates = new LinkedList<>();
			undo = new Stack<>();
			// wait for the DM clicks a "begin" button
			// all interaction will be handled by a javafx controller class
			// all communication and gameplay will be handled by this gamehandler.
		} catch (IOException e) {
			ErrorHandler.handle("Well, something went horribly wrong...", e);
		}
	}
	
	@Override
	protected void processMessages() {
		Message<?> msg;
		while((msg = server.readMessage())!=null) {
			Logger.println("Processing message of type " + msg.getMessage().getClass().getSimpleName());
			if(msg.getMessage() instanceof SerializableJSON) {
				parseJSON(((SerializableJSON) msg.getMessage()).getJSON());
			} else
				ErrorHandler.handle("Received message of type " + msg.getMessage().getClass().getSimpleName() + " and didn't know what to do with it...", null);
		}
	}
	private void parseJSON(JSONObject json) {
		try {
			Action action = ActionDecoder.decodeRequest(json);
			if(action != null) {
				action.attach();
				return;
			} else
				ErrorHandler.handle("Unknown JSON type: " + json.getString("type"), null);
		} catch(JSONException e) {
			ErrorHandler.handle("Could not parse JSON", e);
		}
	}
	
	public void preview(JSONObject action) {
		int count = 1;
		if(action.getString("type").equals(JSONKeys.KEY_UPDATE_LIST))
			count = action.getInt("size");
		final int count2 = count;
		Platform.runLater(new Runnable() {
			public void run() {
				ButtonType accept = new ButtonType("Accept", ButtonData.YES);
				ButtonType decline = new ButtonType("Decline", ButtonData.NO);
				ButtonType preview = new ButtonType("Preview", ButtonData.OTHER);
				Alert alert = new Alert(Alert.AlertType.INFORMATION, "The party has made " + count2 + " moves. Do you want to accept or decline?", accept, decline, preview);
				alert.setTitle("Update");
				Optional<ButtonType> result = alert.showAndWait();
				if(result.orElse(decline)==preview) {
					final Map old = map;
					map = map.copy();
					ActionDecoder.decode(action, false).executeNow();
					controller.showPreview(map);
					map = old;
				} else if(result.orElse(decline)==accept) {
					ActionDecoder.decode(action, false).attach();
					try {
						server.sendMessage(JSONEncoder.accepted);
					} catch(IllegalStateException | InterruptedException e) {
						ErrorHandler.handle("Couldn't send confirmation of acceptance. You should probably resync." , e);
					}
				} else {
					previewDeclined();
				}	
			}
		});
	}
	
	public void previewAccepted() {
		map = controller.previewMap;
		try {
			server.sendMessage(JSONEncoder.accepted);
		} catch(IllegalStateException | InterruptedException e) {
			ErrorHandler.handle("Couldn't send confirmation of acceptance. You should probably resync." , e);
		}
	}
	
	public void previewDeclined() {
		try {
			server.sendMessage(JSONEncoder.declined);
		} catch(IllegalStateException | InterruptedException e) {
			ErrorHandler.handle("Couldn't send decline command. You should probably resync.", e);
		}
	}

	private void onConnected() {
		isPlaying = true;
		start();
		controller.setConnected(true);
		resync(true);
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Connection established.");
			alert.setHeaderText("A client connected to the game.");
			alert.setContentText("The players are now able to view the map.");
			alert.show();
		});
	}
	
	public void disconnect() {
		server.disconnect();
	}
	
	public void onDisconnected() {
		isPlaying = false;
		stop();
		controller.setConnected(false);
	}
	
	private void onReconnected () {
		isPlaying = true;
		start();
		controller.setConnected(true);
		resync(true);
	}
	
	public void reconnect() {
		if(ServerListener.isActive()) {
			stop();
			ServerListener.stopAll();
		}
		 //start new server listener
		server = new ServerListener(serverport, () -> onReconnected(), () -> onDisconnected());
		server.start();
	}
	
	static boolean resyncOpen = false;
	public void requestResync() {
		Utils.safeRun(() -> {
			if(resyncOpen) return;
			resyncOpen = true;
			ButtonType resync = new ButtonType("Resync", ButtonBar.ButtonData.YES);
			ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
	
			Alert alert = new Alert(AlertType.WARNING, "", resync, cancel);
			alert.setTitle("Request for resync");
			alert.setHeaderText("The connected client has requested a resync of the game. Do you want to continue? If you resync, all updates will automatically be pushed.");
			Optional<ButtonType> result = alert.showAndWait();
			resyncOpen = false;
			if(result.orElse(cancel)==cancel)
				return;
			else if(result.orElse(cancel)==resync)
				resync(false);
		});
	}
	
	public void resync(boolean includeBackground) {
		try {
			server.sendMessage(map, includeBackground);
		} catch(IllegalStateException | InterruptedException e) {
			ErrorHandler.handle("Couldn't send map and/or initiative list. You should probably resync." , e);
		}
		try {
			List<InitiativeEntry> il = controller.getILController().getAll();
			if(il.size()==0)
				server.sendMessage(ActionEncoder.clearInitiative());
			else
				server.sendMessage(JSONEncoder.encodeInitiatives(il));
		} catch (IllegalStateException | InterruptedException e) {
			ErrorHandler.handle("Couldn't send map and/or initiative list. You should probably resync." , e);
		}
		//reset the buffer
		updates.clear();
	}
	
	public boolean loadMap(File f) throws IOException {
		Map m = Utils.loadMap(f);
		controller.currentFile = f;
		if (m == null)
			return false;
		map = new ServerMap(m, this);
		controller.setMap(map);
		return true;
	}
	
	public boolean sendTexture(int id) {
		Logger.println("[SERVER] sending texture " + id);
		try {
			server.sendMessage(AssetManager.getTexture(id), id);
		} catch (IllegalStateException | InterruptedException e) {
			ErrorHandler.handle("Texture could not be send.", e);
			return false;
		}
		return true;
	}

	public void sendForcedUpdate(JSONObject action) {
		if(blockUpdates || !isPlaying)return;
		//this bypasses the buffer
		try{
			server.sendMessage(action);
		} catch (IllegalStateException | InterruptedException e) {
			ErrorHandler.handle("Update of type '" + action.getString("type") + "' could not be send. Try resyncing the game.", e);
		}
	}
	
	public void sendUpdate(JSONObject action, JSONObject undoAction) {
		if(blockUpdates || !isPlaying) return; //if this is true, we're undoing the buffer, and we don't want to send the undo actions
		if (bufferUpdates)
			updates.add(action);
		else
			try {
				server.sendMessage(action);
			} catch (IllegalStateException | InterruptedException e) {
				ErrorHandler.handle("Update of type '" + action.getString("type") + "' could not be send. Try resyncing the game.", e);
			}
		undo.push(undoAction);
	}

	public void pushUpdates() {
		if(!isPlaying) return;
		if (bufferUpdates) {
			try {
				server.sendMessage(JSONEncoder.encodeMask(map.getMask()));
			} catch(IllegalStateException | InterruptedException e) {
				ErrorHandler.handle("Could not send the new Fog of War, try resyncing the game.", e);
			}
			try {
				JSONObject json = new JSONObject();
				json.put("type", JSONKeys.KEY_UPDATE_LIST);
				json.put("size", updates.size());
				JSONArray array = new JSONArray();
				while(updates.size()>0)
					array.put(updates.poll());
				json.put("array", array);
				server.sendMessage(json);
			} catch (IllegalStateException | InterruptedException e) {
				ErrorHandler.handle("Update could not be send. Try resyncing the game.",e);
			}
		} else
			ErrorHandler.handle("Buffer is not enabled", null);
	}
	
	public void undo() {
		if(undo.isEmpty()) return;
		blockUpdates = true;
		JSONObject json = undo.pop();
		Action action = ActionDecoder.decode(json, true);
		action.executeNow();
		updates.removeLast();
		blockUpdates = false;
	}

	private void undoBuffer() {
		map.setMask(oldMask);
		controller.redraw();
		oldMask = null;
		while(!updates.isEmpty()) {
			ActionDecoder.decode(undo.pop(), true).executeNow();
			updates.removeLast();
		}
	}

	public boolean requestDisableUpdateBuffer() {
		if(!bufferUpdates)
			return true;
		if(updates.isEmpty()) {
			//check if fow map changed
			boolean maskchanged = false;
			for(int i = 0; i < oldMask.length && !maskchanged; i++)
				for(int j = 0; j < oldMask[0].length && !maskchanged; j++)
					if(oldMask[i][j] == map.getMask(j, i))
						maskchanged = true;
			if(!maskchanged) {
				bufferUpdates = false;
				return true;
			}
		}
		ButtonType cont = new ButtonType("continue", ButtonBar.ButtonData.OK_DONE);
		ButtonType push = new ButtonType("push updates", ButtonBar.ButtonData.APPLY);
		ButtonType cancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

		Alert alert = new Alert(AlertType.WARNING, "If you continue, these changes will be undone. If you click 'push updates' the updates will be send before clearing the buffer.", cont, push, cancel);
		alert.setTitle("Are you sure?");
		alert.setHeaderText("There are one or more changes stored in the buffer.");
		Optional<ButtonType> result = alert.showAndWait();
		
		if(result.orElse(cancel)==cancel)
			return false;
		else if(result.orElse(cancel)==cont)
			undoBuffer();
		else if(result.orElse(cancel)==push) {
			pushUpdates();
		}
		bufferUpdates = false;
		return true;		
	}
	
	public void enableUpdateBuffer() {
		if(!bufferUpdates) {
			byte[][] mask = map.getMask();
			oldMask = new byte[mask.length][mask[0].length];
			for(int i = 0; i < oldMask.length; i++)
				for(int j = 0; j < oldMask[0].length; j++)
					oldMask[i][j] = mask[i][j];
		}
		bufferUpdates = true;
	}
	
	public boolean isBufferEnabled() {
		return bufferUpdates;
	}

	public String getBufferedActions() {
		return updates.toString();
	}

	public ServerController getController() {
		return controller;
	}
	
	public ServerMap setMap(Map map, ServerController.Key key) {
		Objects.requireNonNull(key);
		this.map = new ServerMap(map, this);
		if(isPlaying) {
			try {
				server.sendMessage(map, true);
			} catch (IllegalStateException | InterruptedException e) {
				ErrorHandler.handle("Could not send map. Try resyncing.", e);
			}
		} else {
			Logger.println("No client connected, so no need to send the map");
		}
		return (ServerMap) this.map;
	}
	
	@Override
	public void selectInitiative(int id) {
		controller.getILController().select(id);
	}

	@Override
	public void removeInitiative(int id) {
		controller.getILController().remove(id);		
	}

	@Override
	public void addInitiative(int id, double initiative) {
		controller.getILController().addEntity(map.getEntityById(id), initiative);
	}
	
	@Override
	public void clearInitiative() {
		//this method cannot be called by the server
	}
	
	
	private Stage sendImgStage;
	private ImageView imgView;
	public void sendImage() {
		try {
			Image tex = ToolkitController.getTexture();
			if(sendImgStage == null) {
				sendImgStage = new Stage();
				BorderPane pane = new BorderPane();
				imgView = new ImageView(tex);
				ButtonBar bar = new ButtonBar();
				Button btnSend = new Button("Send");
				Button btnCancel = new Button("Cancel");
				btnSend.setDefaultButton(true);
				btnCancel.setCancelButton(true);
				btnCancel.setOnAction(event -> sendImgStage.close());
				btnSend.setOnAction(event -> {
					try {
						server.sendMessage(tex, -6, true);
					} catch (IllegalStateException | InterruptedException e) {
						ErrorHandler.handle("Could not send image.", e);
					}
					sendImgStage.close();
				});
				bar.getButtons().addAll(btnSend, btnCancel);
				pane.setCenter(imgView);
				pane.setTop(bar);
				pane.setMaxWidth(600);
				pane.setMaxHeight(600);
				imgView.maxHeight(530);
				imgView.maxWidth(600);
				Scene scene = new Scene(pane);
				sendImgStage.setScene(scene);
				sendImgStage.setTitle("Send image?");
			} else
				imgView.setImage(tex);
			sendImgStage.showAndWait();
		} catch (IOException e) {
			ErrorHandler.handle("Could not load image.", e);
		}
	}
	
	private Stage sendTextStage;
	public void sendText() {
        try {
        	sendTextStage = new Stage();
        	FXMLLoader loader = new FXMLLoader(MapManagerApp.class.getResource("/assets/fxml/TextCreatePane.fxml"));
			Scene scene = new Scene(loader.load());
			CreateTextPaneController cont = loader.getController();
			cont.setMode(CreateTextPaneController.SEND);
			sendTextStage.setScene(scene);
			sendTextStage.setResizable(true);
			sendTextStage.sizeToScene();
			sendTextStage.showAndWait();
			if(cont.isCanceled()) return;
			Logger.println("Page count: " +cont.getPageCount());
			server.sendMessage(cont.getJSON());
		} catch (IOException e) {
			ErrorHandler.handle("Could not start stage", e);
		} catch (IllegalStateException | InterruptedException e) {
			ErrorHandler.handle("Could not send text", e);
		}
        
	}
}