package app;

import controller.MapController;
import data.mapdata.Map;

public abstract class GameHandler {
	public static Map map;
	
	public abstract MapController getController();
}
