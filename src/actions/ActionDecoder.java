package actions;

import java.awt.Point;
import java.util.ArrayList;

import app.ClientGameHandler;
import app.GameHandler;

public class ActionDecoder {
	
	public static Action decode(String s) {
		if(s.contains(";")) {
			Action a = Action.empty();
			for(String line : s.split(";")) {
				a.addAction(decodeLine(line));
			}
			return a;
		} else
			return decodeLine(s);
	}
	
	private static Action decodeLine(String s) {
		//every command starts with a single word representing the type of action.
		//extract that word first
		int index = s.indexOf(" ");
		if(index == -1) {
			if(s.equals("reset")) {
				return new Action(0) {
				@Override
				protected void execute() {
					ClientGameHandler.instance.loadMap();
				}
			};
			} else
				throw new IllegalArgumentException("command " + s + " could not be parsed.");
		}
		ArrayList<Object> arg = parseArgs(s.substring(index).trim());
		switch(s.substring(0, s.indexOf(" "))) {
			case "set":
				return new Action(0.5f) {
					@Override
					protected void execute() {
						GameHandler.map.getTile((Point) arg.get(0)).setType((Integer) arg.get(1));
					}
				};
			case "move":
				return new MovementAction(new GuideLine(new Point[] {(Point) arg.get(0), (Point) arg.get(1)}), GameHandler.map.getEntity((Point)arg.get(0)), 0.5f);
			case "bloodied":
				return new Action(0.5f) {
					@Override
					protected void execute() {
						GameHandler.map.getEntity((Point) arg.get(0)).setBloodied(true);
					}
				};
			case "clear":
				return new Action(0.5f) {
					@Override
					protected void execute() {
						//GameHandler.map.getTile((Point) arg.get(0)).setType(PresetTile.EMPTY);
					}
				};
			default:
				return null;
		}
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