package app;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

import actions.Action;
import actions.ActionDecoder;
import actions.ActionEncoder;
import comms.SerializableJSON;
import comms.Server;
import controller.CreateTextPaneController;
import controller.InitiativeListController.InitiativeEntry;
import controller.MainMenuController;
import controller.ServerController;
import controller.ToolkitController;
import data.mapdata.Map;
import data.mapdata.PresetTile;
import data.mapdata.ServerMap;
import data.mapdata.Tile;
import data.mapdata.Entity;
import gui.ErrorHandler;
import helpers.AssetManager;
import helpers.Logger;
import helpers.Utils;
import helpers.codecs.Encoder;
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

	Server server;

	ServerController controller;
	public static ServerGameHandler instance;

	private StringBuilder updates;
	private Stack<String> undo;
	private byte[][] oldMask;
	
	private boolean bufferUpdates = false;
	public boolean isPlaying = false;
	//for serverlistener
	Runnable serverListener;
	Thread listenerThread;
	private Object pauseLock = new Object();
	private boolean running = false, paused = false;
	private boolean blockUpdates = false;
	
	public ServerGameHandler(Server _server) {
		super();
		server = _server;
		updates = new StringBuilder();
		undo = new Stack<>();
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
			// wait for the DM clicks a "begin" button
			// all interaction will be handled by a javafx controller class
			// all communication and gameplay will be handled by this gamehandler.
		} catch (IOException e) {
			ErrorHandler.handle("Well, something went horribly wrong...", e);
		}

		serverListener = new Runnable() {
			@Override
			public void run() {
				try {
					while (running) {
						synchronized (pauseLock) {
							if (paused) {
								Logger.println("Listener paused");
								try {
									synchronized (pauseLock) {
										pauseLock.wait();
									}
								} catch (InterruptedException ex) {
									break;
								}
							}
							if (!running)
								break;
							try {
								Thread.sleep((long) (1000 / 20));
								Logger.println("listening...");
								ActionDecoder.decodeRequest(server.read(String.class)).attach();
							} catch (IOException | InterruptedException e) {
								Platform.runLater(new Runnable() {
									public void run() {
										ErrorHandler.handle("Communication was lost, please reconnect.", e);
									}
								});
								break;
							}
						}
					}
				} catch(Exception e) {
					ErrorHandler.handle("The server thread has crashed! Try resyncing the game.", e);
				}
			}
		};
	}
	
	public void preview(String action) {
		int count = 0;
		for(int i = 0; i < action.length(); i++) if(action.charAt(i)==';') count++;
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
						server.write("accepted");
					} catch(IOException e) {
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
			server.write("accepted");
		} catch(IOException e) {
			ErrorHandler.handle("Couldn't send confirmation of acceptance. You should probably resync." , e);
		}
	}
	
	public void previewDeclined() {
		try {
			server.write("declined");
		} catch(IOException e) {
			ErrorHandler.handle("Couldn't send decline command. You should probably resync.", e);
		}
	}
	
	public void pauseServerListener() {
		paused = true;
	}
	
	public void resumeServerListener() {
		synchronized(pauseLock) {
			paused = false;
			pauseLock.notifyAll();
		}
	}

	public void begin() {
		// after the begin button has been clicked, execute the following sequence.
		acceptClient();
		sendTextures();
		sendMap();
		if(!running) {
			running = true;
			listenerThread = new Thread(serverListener);
			listenerThread.setDaemon(true);
			listenerThread.start();
		} else
			resumeServerListener();
	}

	public void resync() {
		try {
			if(!listenerThread.isAlive()) {
				//restart the thread
				running = true;
				listenerThread = new Thread(serverListener);
				listenerThread.setDaemon(true);
				listenerThread.start();
			}
			server.write(map, false);
			for(InitiativeEntry entry : controller.getILController().getAll())
				server.write(ActionEncoder.addInitiative(entry.getEntity().getID(), entry.getInitiative()));
		} catch (IOException e) {
			ErrorHandler.handle("Could not resync...", e);
		}
	}

	public void reconnect() throws IOException {
		pauseServerListener();
		int port = server.server.getLocalPort();
		server.server.close();
		server = Server.create(port);
		begin();
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

	public void acceptClient() {
		try {
			server.waitForClient();
			server.write(Encoder.VERSION_ID);
			isPlaying = true;
			ActionDecoder.setVersion(server.read(Integer.class));
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Connection established.");
			alert.setHeaderText("A client connected to the game.");
			alert.setContentText("The players are now able to view the visible regions.");
			alert.show();
		} catch (IOException e) {
			ErrorHandler.handle("Something went wrong while waiting for a client connection.", e);
		}
	}

	public boolean sendTextures() {
		Logger.println("[SERVER] sending textures.");
		HashMap<Integer, Image> textures = new HashMap<>();
		for (int i = 0; i < map.getWidth(); i++)
			for (int j = 0; j < map.getHeight(); j++) {
				Tile t = map.getTile(i, j);
				if (t.getType() >= 0)
					textures.put(t.getType(), t.getTexture());
			}
		for (Entity e : map.getEntities())
			textures.put(e.getType(), e.getTexture());
		try {
			server.write(textures.size());
			for (Entry<Integer, Image> pair : textures.entrySet()) {
				server.write(pair.getValue(), pair.getKey());
			}
		} catch (IOException e) {
			ErrorHandler.handle("Not all textures could be transferred. Please try again.", e);
			return false;
		}
		return true;
	}
	
	public boolean sendTexture(int id) {
		Logger.println("[SERVER] sending texture " + id);
		try {
			server.write(AssetManager.getTexture(id), id);
		} catch (IOException e) {
			ErrorHandler.handle("Texture could not be send.", e);
			return false;
		}
		return true;
	}

	public boolean sendMap() {
		Logger.println("[SERVER] sending map.");
		try {
			server.write(map);
		} catch (IOException e) {
			ErrorHandler.handle("Could not send map. Please try again", e);
			return false;
		}
		return true;
	}

	public void sendForcedUpdate(String action) {
		if(blockUpdates)return;
		//this bypasses the buffer
		try{
			server.write(action);
		} catch (IOException e) {
			ErrorHandler.handle("Update [" + action + "] could not be send. Try resyncing the game.", e);
		}
	}
	
	public void sendUpdate(String action, String undoAction) {
		if(blockUpdates) return; //if this is true, we're undoing the buffer, and we don't want to send the undo actions
		if (bufferUpdates)
			updates.append(action).append(';');
		else
			try {
				server.write(action);
			} catch (IOException e) {
				ErrorHandler.handle("Update [" + action + "] could not be send. Try resyncing the game.", e);
			}
		undo.push(undoAction);
	}

	public void pushUpdates() {
		if (bufferUpdates) {
			try {
				server.write(map.getMask());
			} catch(IOException e) {
				ErrorHandler.handle("Could not send the new Fog of War, try resyncing the game.", e);
			}
			if(updates.length()>0) {
				try {
					server.write(updates.toString());
					updates = new StringBuilder();
				} catch (IOException e) {
					ErrorHandler.handle("Update [" + updates.toString() + "] could not be send. Try resyncing the game.",
							e);
				}
			}
		} else
			ErrorHandler.handle("Buffer is not enabled", null);
	}
	
	public void undo() {
		blockUpdates = true;
		String[] s = undo.pop().split(";");
		for (String line : s) {
			Action action = ActionDecoder.decode(line, true);
			action.setDelay(0);
			action.update(0);
			updates.delete(updates.lastIndexOf(";"), updates.length());
		}
		blockUpdates = false;
	}

	private void undoBuffer() {
		map.setMask(oldMask);
		controller.redraw();
		oldMask = null;
		long count = updates.chars().filter(ch -> ch == ';').count();
		for (int i = 0; i < count; i++)
			undo();
		updates = new StringBuilder();
	}

	public boolean requestDisableUpdateBuffer() {
		if(!bufferUpdates)
			return true;
		if(updates.length()==0) {
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
		try {
			server.write(map);
		} catch (IOException e) {
			ErrorHandler.handle("Could not send map. Try resyncing.", e);
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
	public void addInitiative(int id, int initiative) {
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
						server.write(ActionEncoder.addFlag(DISPLAY_IMAGE));
						server.write(tex, -6);
					} catch (IOException e) {
						ErrorHandler.handle("Could not send image.", e);
					} finally {
						try {
							server.write(ActionEncoder.remFlag(DISPLAY_IMAGE));
						} catch (IOException e) {}
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
			sendTextStage.showAndWait();
			if(cont.isCanceled()) return;
			System.out.println(cont.getPageCount());
			server.write(new SerializableJSON(cont.getJSON()));
		} catch (IOException e) {
			ErrorHandler.handle("Could not start stage", e);
		}
        
	}
}