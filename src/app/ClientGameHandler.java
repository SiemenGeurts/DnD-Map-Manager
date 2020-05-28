package app;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Optional;

import actions.Action;
import actions.ActionDecoder;
import actions.ActionEncoder;
import actions.GuideLine;
import actions.MovementAction;
import comms.Client;
import comms.Message;
import comms.SerializableImage;
import comms.SerializableMap;
import controller.ClientController;
import controller.MainMenuController;
import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.PresetTile;
import data.mapdata.Tile;
import gui.Dialogs;
import gui.ErrorHandler;
import helpers.AssetManager;
import helpers.Logger;
import helpers.codecs.Encoder;
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
		
		try {
			ActionDecoder.setVersion(client.read(Integer.class));
		} catch (IOException e1) {
			ActionDecoder.setVersion(Encoder.VERSION_ID);
			ErrorHandler.handle("Couldn't receive version id, using current.", e1);
		}
		try {
			client.write(Encoder.VERSION_ID);
		} catch(IOException e) {
			ErrorHandler.handle("Couldn't send version id.", e);
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
									if((flags & DISPLAY_IMAGE) == DISPLAY_IMAGE)
										displayImage(si.getImage());
									else {
										AssetManager.putTexture(si.getId(), si.getImage());
										controller.redraw();
									}
								} else if(m.getMessage() instanceof SerializableMap) {
									map = ((SerializableMap) m.getMessage()).getMap();
									requestMissingTextures(map);
									controller.setMap(map);
									controller.redraw();
								} else if(m.getMessage() instanceof String) {
									String message = (String) m.getMessage();
									if(message.length() == 0) {
										Logger.println("Ignored message of length 0");
										continue;
									}
									if(message.startsWith("!"))
										ActionDecoder.decode(message.substring(1)).executeNow();
									else
										ActionDecoder.decode((String) m.getMessage()).attach();
								} else
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
			PresetTile.setupPresetTiles();
			for (int i = 0; i < amount; i++) {
				SerializableImage img = client.read(SerializableImage.class);
				AssetManager.putTexture(img.getId(), img.getImage());
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
			SerializableMap smap = client.read(SerializableMap.class);
			map = smap.getMap();
			controller.setMap(map);
			controller.drawMap();
			resumeClientListener();
		} catch (IOException e) {
			ErrorHandler.handle("The map could not be loaded. Please try again.", e);
			return false;
		}
		return true;
	}
	
	public void move(Entity entity, Point target) {
		if(awaitingResponse) return;
		sendUpdate(ActionEncoder.movement(entity.getTileX(), entity.getTileY(), target.x, target.y, entity.getID()));
		undoAction = new MovementAction(new GuideLine(new Point2D[] {target, entity.getLocation()}), entity, 0).addAction(undoAction);
		new MovementAction(new GuideLine(new Point2D[] {entity.getLocation(), target}), entity, 0).attach();
	}
	
	public void addInitiative(int id, int initiative) {
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
	
	private boolean sendUpdate(String s) {
		if(bufferUpdates) {
			updates.append(s).append(';');
			return true;
		} else {
			try {
				awaitingResponse=true;
				client.write(s);
				return true;
			} catch(IOException e) {
				ErrorHandler.handle("Could not send updates. Please try again.", e);
				return false;
			}
		}
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
				double f= MAX_HEIGHT/height;
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
}