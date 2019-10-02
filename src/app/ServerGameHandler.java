package app;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Stack;

import actions.Action;
import actions.ActionDecoder;
import actions.ActionEncoder;
import comms.Server;
import controller.MainMenuController;
import controller.SceneManager;
import controller.ServerController;
import data.mapdata.Map;
import data.mapdata.ServerMap;
import data.mapdata.Tile;
import data.mapdata.Entity;
import gui.ErrorHandler;
import helpers.AssetManager;
import helpers.Utils;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

public class ServerGameHandler extends GameHandler {

	Server server;

	ServerController controller;
	public static ServerGameHandler instance;

	private FileChooser mapChooser;
	private StringBuilder updates;
	private Stack<String> undo;
	private boolean bufferUpdates = false;

	//for serverlistener
	Thread serverListener;
	private Object pauseLock = new Object();
	private boolean running = false, paused = false;
	
	public ServerGameHandler(Server _server) {
		super();
		server = _server;
		updates = new StringBuilder();
		undo = new Stack<>();
		instance = this;
		mapChooser = new FileChooser();
		mapChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Map files (*.map)", "*.map"));

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
					mapChooser.setTitle("Load map");
					controller.currentFile = mapChooser.showOpenDialog(SceneManager.getPrimaryStage());
				} while(!loadMap(controller.currentFile));
			
			MainMenuController.sceneManager.pushView(scene, loader);
			controller.endInit();
			// wait for the DM clicks a "begin" button
			// all interaction will be handled by a javafx controller class
			// all communication and gameplay will be handled by this gamehandler.
		} catch (IOException e) {
			ErrorHandler.handle("Well, something went horribly wrong...", e);
		}

		serverListener = new Thread(new Runnable() {
			@Override
			public void run() {
				while (running) {
					synchronized (pauseLock) {
						if (paused) {
							System.out.println("Listener paused");
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
							System.out.println("listening...");
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
			}
		});
		serverListener.setDaemon(true);
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
					controller.inPreview = true;
					Map old = map;
					map = map.copy();
					
					controller.previewMap = map;
					map = old;
				} else if(result.orElse(decline)==accept) {
					ActionDecoder.decode(action).attach();
					try {
						server.write("accepted");
					} catch(IOException e) {
						ErrorHandler.handle("Couldn't send confirmation of acceptance. You should probably resync." , e);
					}
				} else {
					try {
						server.write("declined");
					} catch(IOException e) {
						ErrorHandler.handle("Couldn't send decline command. You should probably resync.", e);
					}
				}	
			}
		});
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
			serverListener.start();
		} else
			resumeServerListener();
	}

	public void resync() {
		try {
			server.write(ActionEncoder.reset());
			sendMap();
		} catch (IOException e) {
			e.printStackTrace();
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
		System.out.println("[SERVER] sending textures.");
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
		System.out.println("[SERVER] sending texture " + id);
		try {
			server.write(AssetManager.textures.get(id), id);
		} catch (IOException e) {
			ErrorHandler.handle("Texture could not be send.", e);
			return false;
		}
		return true;
	}

	public boolean sendMap() {
		System.out.println("[SERVER] sending map.");
		try {
			server.write(map.getBackground() != null);
			server.write(map.encode(false));
			if (map.getBackground() != null) {
				server.write(map.getScaling().name().toLowerCase());
				server.write(map.getBackground(),-6);
			}
		} catch (IOException e) {
			ErrorHandler.handle("Could not send map. Please try again", e);
			return false;
		}
		return true;
	}

	public void sendUpdate(String action, String undoAction) {
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
		if (bufferUpdates)
			try {
				server.write(updates.toString());
				updates = new StringBuilder();
			} catch (IOException e) {
				ErrorHandler.handle("Update [" + updates.toString() + "] could not be send. Try resyncing the game.",
						e);
			}
	}

	public void undo() {
		String[] s = undo.pop().split(";");
		for (String line : s) {
			Action action = ActionDecoder.decode(line, true);
			action.setDelay(0);
			action.update(0);
			updates.delete(updates.lastIndexOf(";"), updates.length());
		}
	}

	private void undoBuffer() {
		long count = updates.chars().filter(ch -> ch == ';').count();
		for (int i = 0; i < count; i++)
			undo();
		updates = new StringBuilder();
	}

	public boolean requestDisableUpdateBuffer() {
		if(!bufferUpdates)
			return true;
		if(updates.length()==0) {
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
		bufferUpdates = true;
	}

	public String getBufferedActions() {
		return updates.toString();
	}

	public ServerController getController() {
		return controller;
	}
}