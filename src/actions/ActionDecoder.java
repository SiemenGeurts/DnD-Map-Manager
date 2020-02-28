package actions;

import java.awt.Point;
import java.util.ArrayList;

import app.ClientGameHandler;
import app.GameHandler;
import app.ServerGameHandler;
import data.mapdata.Entity;
import data.mapdata.PresetTile;
import helpers.AssetManager;
import helpers.codecs.Decoder;


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
	private static Decoder decoder;
	
	public static void setVersion(int version) {
		decoder = Decoder.getDecoder(version);
	}
	
	public static void setGameHandler(GameHandler _handler) {
		handler = _handler;
	}
	
	public static Action decode(String s) {
		return decode(s, false);
	}
	
	public static Action decode(String s, boolean isServer) {
		if(s.contains(";")) {
			Action a = Action.empty();
			for(String line : s.split(";")) {
				a.addAction(decodeLine(line, isServer));
			}
			a.setDelay(0); //remove delay of first action
			return a;
		} else {
			Action a = decodeLine(s,isServer);
			a.setDelay(0); //remove delay as there is only one action.
			return a;
		}
	}
	
	private static Action decodeLine(String s, boolean isServer) {
		//every command starts with a single word representing the type of action.
		//extract that word first
		int index = s.indexOf(" ");
		if(index == -1) {
			switch(s) {
			case "declined":
				return new Action(0) {
					@Override
					protected void execute() {
						((ClientGameHandler)handler).onActionDeclined();
					}
				};
			case "accepted":
				return new Action(0) {
					@Override
					protected void execute() {
						((ClientGameHandler)handler).onActionAccepted();
					}
				};
			default:
				throw new IllegalArgumentException("command '" + s + "' could not be parsed.");
			}
		}
		ArrayList<Object> arg = parseArgs(s.substring(index).trim());
		switch(s.substring(0, index)) {
			case "set":
				return new Action(isServer ? 0 : 0.5f) {
					@Override
					protected void execute() {
						Point p = (Point) arg.get(0);
						int type = (Integer) arg.get(1);
						handler.map.getTile(p).setType(type);
						if(!isServer && type>=0 && !AssetManager.textureExists(type))
							ClientGameHandler.instance.requestTexture(type);
					}
				};
			case "move":
				int id = (Integer) arg.get(0);
				Point p = (Point) arg.get(1);
				Point p2 = (Point) arg.get(2);
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
			case "bloodied":
				return new Action(isServer ? 0 : 0.5f) {
					@Override
					protected void execute() {
						Entity entity = handler.map.getEntityById((int)arg.get(0));
						entity.setBloodied(true);
					}
				};
			case "clear":
				return new Action(isServer ? 0 : 0.5f) {
					@Override
					protected void execute() {
						handler.map.getTile((Point) arg.get(0)).setType(PresetTile.EMPTY);
					}
				};
			case "remove":
				return new Action(0f) {
					@Override
					protected void execute() {
						handler.map.removeEntity(handler.map.getEntityById((int) arg.get(0)));
					}
				};
			case "add":
				return new Action(0f) {
					@Override
					protected void execute() {
						Entity e = decoder.decodeEntity((String) arg.get(0));
						handler.map.addEntity(e);
						if(!isServer && e.getType()>=0 && !AssetManager.textureExists(e.getType()))
							ClientGameHandler.instance.requestTexture(e.getType());
					}
				};
			case "addflag":
				return new Action(0f) {
					@Override
					protected void execute() {
						handler.addFlag((short) ((String) arg.get(0)).charAt(0));
					}
				};
			case "remflag":
				return new Action(0f) {
					@Override
					protected void execute() {
						handler.removeFlag((short) ((String) arg.get(0)).charAt(0));
					}
				};
			default:
				return null;
		}
	}
	
	public static Action decodeRequest(String s) {
		int index = s.indexOf(" ");
		ArrayList<Object> arg = parseArgs(s.substring(index).trim());
		switch(s.substring(0, index)) {
		case "texture":
			return new Action(0) {
				@Override
				protected void execute() {
					ServerGameHandler.instance.sendTexture((Integer) arg.get(0));
				}
			};
		case "move":
			return new Action(0) {
				@Override
				protected void execute() {
					ServerGameHandler.instance.preview(s);
				}
			};
		}
		return null;
	}
	
	private static ArrayList<Object> parseArgs(String s) {
		int index = 0;
		ArrayList<Object> arglist = new ArrayList<>(2);
		int end = 0;
		do {
		switch(s.charAt(index++)) {
			case '[':
				end = s.indexOf(']', index);
				String args[] = s.substring(index, end).split(",");
				arglist.add(new Point(Integer.valueOf(args[0]), Integer.valueOf(args[1])));
				index = end+1;
				break;
			case '(':
				end = s.indexOf(')', index);
				arglist.add(Integer.valueOf(s.substring(index, end)));
				index = end;
				break;
			case '<':
				end = s.indexOf('>', index);
				arglist.add(s.substring(index, end));
				index = end;
				break;
		}
		} while(index<s.length());
		return arglist;
	}
}