package actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import app.ClientGameHandler;
import app.GameHandler;
import data.mapdata.Entity;
import helpers.Calculator;

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
						Point p = (Point) arg.get(0);
						GameHandler.map.getTile(p).setType((Integer) arg.get(1));
						ClientGameHandler.instance.getController().drawMap(p.x, p.y, 1+p.x, 1+p.x);
					}
				};
			case "move":
				Point p = (Point) arg.get(0);
				Point p2 = (Point) arg.get(1);
				return new MovementAction(new GuideLine(new Point[] {p, p2}), GameHandler.map.getEntity((Point)arg.get(0)), 0.5f) {
					@Override
					public void execute() {
						Rectangle rect = Calculator.getRectangle(p, p2);
						ClientGameHandler.instance.getController().drawMap(rect);
					}
				};
			case "bloodied":
				return new Action(0.5f) {
					@Override
					protected void execute() {
						Entity entity = GameHandler.map.getEntity((Point) arg.get(0));
						entity.setBloodied(true);
						ClientGameHandler.instance.getController().drawMap(entity.getTileX(), entity.getTileY(), entity.getWidth()+entity.getTileX(), entity.getHeight()+entity.getTileY());
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