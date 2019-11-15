package actions;

import data.mapdata.Entity;
import helpers.codecs.Encoder;

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
	
	public static String set(int x, int y, int id) {
		return "set [" + x + "," + y + "] to (" + id + ")";
	}
	
	public static String movement(int x1, int y1, int x2, int y2, int id) {
		return "move (" + id + ") from [" + x1 + "," + y1 + "] to [" + x2 + "," + y2 + "]";
	}
	
	public static String setBloodied(int id) {
		return "bloodied (" + id + ")";
	}
	
	public static String clearTile(int x, int y) {
		return "clear [" + x + "," + y + "]";
	}

	public static String removeEntity(int id) {
		return "remove (" + id + ")";
	}
	
	public static String addEntity(Entity e, boolean includeProperties) {
		return "add <" + Encoder.encode(e,includeProperties) + ">";
	}
	
	public static String requestTexture(int id) {
		return "texture (" + id + ")";
	}
	
	public static String addFlag(short flag) {
		return "!addflag <" + (char) flag + ">";
	}
	public static String remFlag(short flag) {
		return "!remflag <" + (char) flag + ">";
	}
}
