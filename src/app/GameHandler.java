package app;

import java.util.ArrayList;

import actions.Action;
import actions.ActionDecoder;
import controller.MapController;
import data.mapdata.Map;
import gui.ErrorHandler;
import helpers.Logger;

public abstract class GameHandler {
	public Map map;
	protected static Action currentAction;
	public static ArrayList<Action> actions;
	public static short flags = 0;
	
	public abstract MapController getController();
	private Thread updating;
	private static boolean isRunning = false;
	public GameHandler() {
		ActionDecoder.setGameHandler(this);
		actions = new ArrayList<>();
	
	}
	
	protected void start() {
		if(isRunning)
			throw new IllegalStateException("Gamehandler thread is already running");
		updating = new Thread(() -> {
			long time = System.currentTimeMillis();
			do {
				try {
					Thread.sleep((long) (1000 / 30));
					processMessages(); //this loads new incoming messages and parses the actions
					update((System.currentTimeMillis() - time)/1000f);
					time = System.currentTimeMillis();
				} catch (Exception e) {
					if(isRunning)
						ErrorHandler.handle("An exception occured!", e);
					else
						Logger.error(e);
				}
			} while (isRunning);
			
		});
		updating.setDaemon(true);
		updating.start();
		isRunning = true;
	}
	
	protected void stop() {
		isRunning = false;
		updating.interrupt();
	}
	
	
	public void update(float dt) {
		if(actions.size()==0) return;
		for (int i = 0; i < actions.size(); i++) {
			currentAction = actions.get(i);
			currentAction.update(dt);
		}
		currentAction = null;
		getController().redraw();
	}
	
	public void addFlag(short flag) {
		flags |= flag;
	}
	
	public void removeFlag(short flag) {
		if((flags & flag) == flag)
			flags &= ~flag;
	}
	
	public abstract void selectInitiative(int id);
	public abstract void removeInitiative(int id);
	public abstract void clearInitiative();
	public abstract void addInitiative(int id, double initiative);
	
	abstract void processMessages();
	
	public static final short DISPLAY_IMAGE = 1;
}
