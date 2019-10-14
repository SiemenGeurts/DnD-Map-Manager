package app;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import actions.Action;
import actions.ActionDecoder;
import actions.ActionEncoder;
import actions.GuideLine;
import actions.MovementAction;
import comms.Client;
import comms.Message;
import comms.SerializableImage;
import controller.ClientController;
import controller.MainMenuController;
import data.mapdata.Entity;
import data.mapdata.Map;
import gui.Dialogs;
import gui.ErrorHandler;
import helpers.AssetManager;
import helpers.Logger;
import helpers.ScalingBounds.ScaleMode;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;

public class ClientGameHandler extends GameHandler {

	private Client client;
	public static ClientGameHandler instance;

	ClientController controller;

	// clientListener stuff
	Thread clientListener;
	private boolean running = false, paused = false;
	private Object pauseLock = new Object();
	
	private Action undoAction;
	private StringBuilder updates;
	private boolean bufferUpdates = true, awaitingResponse = false;
	
	public ClientGameHandler(Client client) {
		super();
		this.client = client;
		instance = this;
		undoAction = Action.empty();
		updates = new StringBuilder();
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
		
		loadTextures();
		loadMap();

		clientListener = new Thread(new Runnable() {
			@Override
			public void run() {
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
							while (true) {
								Thread.sleep((long) (1000 / 20));
								Message<?> m = client.readMessage();
								if(m.getMessage() instanceof SerializableImage) {
									SerializableImage si = (SerializableImage) m.getMessage();
									AssetManager.textures.put(si.getId(), si.getImage());
									controller.redraw();
								} else if(m.getMessage() instanceof String)
									ActionDecoder.decode((String) m.getMessage()).attach();
								else
									ErrorHandler.handle("Received message of type " + m.getMessage().getClass().getSimpleName() + " and didn't know what to do with it...", null);
							}
						} catch (IOException | InterruptedException e) {
							Platform.runLater(new Runnable() {
								public void run() {
									ErrorHandler.handle("Communication was lost, please restart the session.", e);
								}
							});
							break;
						}
					}
				}
			}
		});
		clientListener.setDaemon(true);
		running = true;
		clientListener.start();
	}
	
	public void pauseClientListener() {
		paused = true;
	}
	
	public void resumeClientListener() {
		synchronized(pauseLock) {
			paused = false;
			pauseLock.notifyAll();
		}
	}

	public boolean loadTextures() {
		Logger.println("[CLIENT] Loading textures.");
		try {
			int amount = client.read(Integer.class);
			HashMap<Integer, Image> textures = AssetManager.textures;
			for (int i = 0; i < amount; i++) {
				SerializableImage img = client.read(SerializableImage.class);
				textures.put(img.getId(), img.getImage());
			}
		} catch (NumberFormatException | IOException e) {
			ErrorHandler.handle("Did not recieve all textures. Please try again.", e);
			return false;
		}
		return true;
	}

	public boolean loadMap() {
		Logger.println("[CLIENT] Loading map.");
		try {
			boolean hasBackground = client.read(Boolean.class);
			map = Map.decode(client.read(String.class));
			if (hasBackground) {
				String scaling = client.read(String.class);
				map.setScaling(scaling.equals("fit") ? ScaleMode.FIT
						: (scaling.equals("stretch") ? ScaleMode.STRETCH : ScaleMode.EXTEND));
				map.setBackground(client.readImage());
			}
			controller.setMap(map);
			controller.drawMap();
		} catch (IOException e) {
			ErrorHandler.handle("The map could not be loaded. Please try again.", e);
			return false;
		}
		return true;
	}
	
	public void move(Entity entity, Point target) {
		if(awaitingResponse) return;
		updates.append(ActionEncoder.movement(entity.getTileX(), entity.getTileY(), target.x, target.y, entity.getID())).append(';');
		undoAction = new MovementAction(new GuideLine(new Point2D[] {target, entity.getLocation()}), entity, 0).addAction(undoAction);
		new MovementAction(new GuideLine(new Point2D[] {entity.getLocation(), target}), entity, 0).attach();
	}

	public void requestTexture(int id) {
		try {
			client.write(ActionEncoder.requestTexture(id));
		} catch (IOException e) {
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
	
	public boolean pushUpdates() {
		try {
			awaitingResponse=true;
			client.write(updates.toString());
			return true;
		} catch(IOException e) {
			ErrorHandler.handle("Could not send updates. Please try again.", e);
			return false;
		}
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
		Dialogs.info("The DM accepted your movements!", false);
	}
	
	public void clearBuffer() {
		updates = new StringBuilder();
		undoAction = Action.empty();
	}
	
	public void undoBuffer() {
		undoAction.attach();
		updates = new StringBuilder();
		undoAction = Action.empty();
	}
}