package app;

import java.util.ArrayList;

import actions.Action;
import actions.ActionDecoder;
import controller.MapController;
import data.mapdata.Map;

public abstract class GameHandler {
	public Map map;
	protected static Action currentAction;
	public static ArrayList<Action> actions;
	
	public abstract MapController getController();
	
	public GameHandler() {
		ActionDecoder.setGameHandler(this);
		actions = new ArrayList<>();
		Thread updating = new Thread(new Runnable() {
			@Override
			public void run() {
				long time = System.currentTimeMillis();
				do {
					try {
						Thread.sleep((long) (1000 / 30));
						update((System.currentTimeMillis() - time)/1000f);
						time = System.currentTimeMillis();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} while (true);
			}
		});
		updating.setDaemon(true);
		updating.start();
	}
	
	public void update(float dt) {
		if(actions.size()==0) return;
		for (int i = 0; i < actions.size(); i++) {
			currentAction = actions.get(i);
			currentAction.update(dt);
		}
			getController().redraw();
		currentAction = null;
	}
}
