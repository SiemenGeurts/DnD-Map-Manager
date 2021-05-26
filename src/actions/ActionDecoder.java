package actions;

import java.awt.Point;

import org.json.JSONArray;
import org.json.JSONObject;

import app.ClientGameHandler;
import app.GameHandler;
import app.ServerGameHandler;
import data.mapdata.Entity;
import data.mapdata.PresetTile;
import helpers.codecs.JSONDecoder;
import static helpers.codecs.JSONKeys.*;

/**
 * Possible actions:
 * <ul>
 * <li>set [x,y] to (id): set tile on [x,y] to sprite (id)</li>
 * <li>move (id) from [x1,y1] to [x2,y2]: move the entity (id) on [x1,y1] to [x2,y2]</li>
 * <li>bloodied (id): add a 'bloodied' tag entity (id)</li>
 * <li>clear [x,y]: clears tile [x,y] (removes sprites)</li>
 * <li>remove (id): removes entity (id)</li>
 * <li>add &lt;encodedEntity&gt;: decodes the entity and adds it to the map</li>
 * <li>texture (id): requests the texture with (id)</li>
 * </ul>
 * @author Joep Geuskens
 *
 */
public class ActionDecoder {
	
	static GameHandler handler;
	private static JSONDecoder decoder;
	
	public static void setVersion(int version) {
		decoder = JSONDecoder.get(version);
	}
	
	public static void setGameHandler(GameHandler _handler) {
		handler = _handler;
	}
	
	public static Action decode(JSONObject json) {
		return decode(json, false);
	}
	
	public static Action decode(JSONObject json, boolean isServer) {
		String type = json.getString("type");
		if(json.getString("type").equals(KEY_UPDATE_LIST)) {
			Action a = Action.empty();
			JSONArray array = json.getJSONArray("array");
			for(int i = 0; i < array.length(); i++)
				a.addAction(decode(array.getJSONObject(i), isServer));
			a.setDelay(0);
			return a;
		}
		switch(type) {
		case KEY_SET_TILE:
			return new Action(isServer ? 0 : 0.15f) {
				@Override
				protected void execute() {
					int id = json.getInt("id");
					handler.map.getTile(json.getInt("x"), json.getInt("y")).setType(id);
					if(!isServer) ClientGameHandler.instance.checkTexture(id);
				}
			};
		case KEY_MOVEMENT:
			int id = json.getInt("id");
			Point p = new Point(json.getInt("x1"), json.getInt("y1"));
			Point p2 = new Point(json.getInt("x2"), json.getInt("y2"));
			if(isServer) {
				return new Action(0) {
					@Override
					protected void execute() {
						handler.map.getEntityById(id).setLocation(p2);
						handler.getController().redraw();
					}
				};
			} else
				return new MovementAction(new GuideLine(new Point[] {p, p2}), handler.map.getEntityById(id), 0.5f);
		case KEY_SET_BLOODIED:
			return new Action(isServer ? 0 : 0.5f) {
				@Override
				protected void execute() {
					Entity entity = handler.map.getEntityById(json.getInt("id"));
					entity.setBloodied(json.getBoolean("value"));
				}
			};
		case KEY_CLEAR_TILE:
			return new Action(isServer ? 0 : 0.5f) {
				@Override
				protected void execute() {
					handler.map.getTile(json.getInt("x"), json.getInt("y")).setType(PresetTile.EMPTY);
				}
			};
		case KEY_ADD_ENTITY:
			return new Action(0f) {
				@Override
				protected void execute() {
					Entity e = decoder.decodeEntity(json.getJSONObject("entity"));
					handler.map.addEntity(e);
					if(!isServer)
						ClientGameHandler.instance.checkTexture(e.getType());
				}
			};
		case KEY_ADD_INITIATIVE:
			return new Action(0f) {
				@Override
				protected void execute() {
					handler.addInitiative(json.getInt("id"), json.getDouble("initiative"));
				}
			};
		case KEY_CLEAR_INITIATIVE:
			return new Action(0f) {
				@Override
				public void execute() {
					handler.clearInitiative();
				}
			};
		case KEY_INITIATIVELIST:
			return new Action(0f) {
				@Override
				public void execute() {
					JSONArray array = json.getJSONArray("initiatives");
					for(int i = 0; i < array.length(); i++) {
						JSONObject obj = array.getJSONObject(i);
						handler.addInitiative(obj.getInt("entity_id"), obj.getDouble("initiative"));
					}
				}
			};
		case KEY_FOW_MASK:
			return new Action(0f) {
				@Override
				public void execute() {
					handler.map.setMask(decoder.decodeMask(json));
				}
			};
		case KEY_EMPTY:
			return Action.empty();
		default:
			if(type.equals(BoolKey.KEY_PREVIEW_CONFIRMATION.get())) {
				if(json.getBoolean("accepted")) {
					return new Action(0) {
						@Override
						protected void execute() {
							((ClientGameHandler)handler).onActionAccepted();
						}
					};
				} else {
					return new Action(0) {
						@Override
						protected void execute() {
							((ClientGameHandler)handler).onActionDeclined();
						}
					};
				}
			} else if(type.equals(IntKey.KEY_REMOVE_ENTITY.get())) {
				return new Action(0f) {
					@Override
					protected void execute() {
						handler.map.removeEntity(handler.map.getEntityById(json.getInt("value")));
					}
				};
			} else if(type.equals(IntKey.KEY_SELECT_INITIATIVE.get())) {
				return new Action(0f) {
					@Override
					public void execute() {
						handler.selectInitiative(json.getInt("value"));
					}
				};
			} else if(type.equals(IntKey.KEY_REMOVE_INITIATIVE.get())) {
				return new Action(0f) {
					@Override
					public void execute() {
						handler.removeInitiative(json.getInt("value"));
					}
				};
			}
			return null;
		}
	}
	
	public static Action decodeRequest(JSONObject json) {
		String type = json.getString("type");
		if(type.equals(IntKey.KEY_REQUEST_TEXTURE.get())) {
			return new Action(0) {
				@Override
				protected void execute() {
					ServerGameHandler.instance.sendTexture(json.getInt("value"));
				}
			};
		} else if(type.equals(KEY_MOVEMENT) || type.equals(KEY_UPDATE_LIST)) {
			return new Action(0) {
				@Override
				protected void execute() {
					ServerGameHandler.instance.preview(json);
				}
			};
		}
		return null;
	}
}