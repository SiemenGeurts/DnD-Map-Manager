package data.mapdata;

import actions.ActionEncoder;
import app.ServerGameHandler;

public class ServerMap extends Map {
	
	ServerGameHandler handler;
	
	public ServerMap(int x, int y, ServerGameHandler handler) {
		super(x, y);
		this.handler = handler;
	}
	
	public ServerMap(Map map, ServerGameHandler handler) {
		super(map.getTiles(), map.getMask());
		entities = map.getAllEntities();
		background = map.getBackground();
		mode = map.getScaling();
		this.handler = handler;
	}

	@Override
	public void setTile(int x, int y, Tile tile) {
		int prevType = getTile(x, y).getType();
		super.setTile(x, y, tile);
		handler.sendUpdate(ActionEncoder.set(x, y, tile.getType()), ActionEncoder.set(x, y, prevType));
	}
	
	@Override
	public void addEntity(Entity e) {
		super.addEntity(e);
		handler.sendUpdate(ActionEncoder.addEntity(e, false), ActionEncoder.removeEntity(e.getID()));
	}
	
	@Override
	public void removeEntity(Entity e) {
		super.removeEntity(e);
		handler.sendUpdate(ActionEncoder.removeEntity(e.getID()), ActionEncoder.addEntity(e, true));
	}
}