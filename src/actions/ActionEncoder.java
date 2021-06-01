package actions;

import org.json.JSONObject;

import data.mapdata.Entity;
import helpers.codecs.JSONEncoder;

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
public class ActionEncoder {
	
	public static JSONObject set(int x, int y, int id) {
		JSONObject json = new JSONObject();
		json.put("type", KEY_SET_TILE).put("x", x).put("y", y).put("id", id);
		return json;
	}
	
	public static JSONObject movement(int x1, int y1, int x2, int y2, int id) {
		JSONObject json = new JSONObject();
		json.put("type", KEY_MOVEMENT).put("x1", x1).put("y1", y1).put("x2", x2).put("y2", y2);
		json.put("id", id);
		return json;
	}
	
	public static JSONObject setBloodied(int id, boolean value) {
		JSONObject json = new JSONObject();
		json.put("type", KEY_SET_BLOODIED).put("id", id).put("value", value);
		return json;
	}
	
	public static JSONObject clearTile(int x, int y) {
		JSONObject json = new JSONObject();
		json.put("type", KEY_CLEAR_TILE).put("x", x).put("y", y);
		return json;
	}

	public static JSONObject removeEntity(int id) {
		return JSONEncoder.encode(IntKey.KEY_REMOVE_ENTITY, id);
	}
	
	public static JSONObject addEntity(Entity e, boolean includeProperties) {
		JSONObject json = new JSONObject();
		json.put("type", KEY_ADD_ENTITY); //change the json type
		json.put("entity", JSONEncoder.encode(e, includeProperties));
		return json;
	}
	
	public static JSONObject requestTexture(int id) {
		return JSONEncoder.encode(IntKey.KEY_REQUEST_TEXTURE, id);
	}
	
	public static JSONObject addInitiative(int id, double initiative) {
		JSONObject json = new JSONObject();
		json.put("type", KEY_ADD_INITIATIVE);
		json.put("id", id).put("initiative", initiative);
		return json;
	}
	
	public static JSONObject selectInitiative(int id) {
		return JSONEncoder.encode(IntKey.KEY_SELECT_INITIATIVE, id);
	}
	
	public static JSONObject removeInitiative(int id) {
		return JSONEncoder.encode(IntKey.KEY_REMOVE_INITIATIVE, id);
	}
	
	public static JSONObject clearInitiative() {
		return new JSONObject().put("type", KEY_CLEAR_INITIATIVE);
	}
	
	public static JSONObject disconnect() {
		return new JSONObject().put("type", KEY_DISCONNECT);
	}
	
	public static JSONObject requestResync() {
		return new JSONObject().put("type", KEY_REQUEST_RESYNC);
	}
	
	public static JSONObject empty() {
		return new JSONObject().put("type", KEY_EMPTY);
	}
}