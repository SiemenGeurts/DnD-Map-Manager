package data.mapdata;

import java.awt.Point;
import java.util.ArrayList;

public class Map {

	private int width, height;
	private Tile[][] tiles;
	private ArrayList<Entity> entities;
	
	public Map(int _width, int _height) {
		width = _width;
		height = _height;
		tiles = new Tile[_height][_width];
		entities = new ArrayList<>();
	}
	
	private Map(Tile[][] _tiles) {
		tiles = _tiles;
		entities = new ArrayList<>();
	}
	
	public void setTile(int x, int y, Tile tile) {
		tiles[y][x] = tile;
	}
	
	public Tile getTile(Point p) {
		return tiles[p.y][p.x];
	}
	
	public Tile getTile(int x, int y) {
		return tiles[y][x];
	}
	
	public Entity getEntity(Point p) {
		return getEntity(p.x, p.y);
	}
	
	public Entity getEntity(int x, int y) {
		for (Entity e : entities) {
			if (x >= e.getTileX() && x < e.getTileX() + e.getWidth() && y >= e.getTileY() && y < e.getTileY() + e.getHeight())
				return e;
		}
		return null;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Tile[][] getTiles() {
		return tiles;
	}
	
	public String encode() {
		StringBuilder builder = new StringBuilder();
		builder.append(tiles.length);
		builder.append(":");
		builder.append(tiles[0].length);
		builder.append(":");
		for(int i = 0; i < tiles.length; i++)
			for(int j = 0; j < tiles[0].length; j++) {
				builder.append(tiles[i][j].getType());
				builder.append(":");
			}
		return builder.toString();
	}
	
	public static Map decode(String string) {
		String[] s = string.split(":");
		Tile[][] tiles = new Tile[Integer.valueOf(s[0])][Integer.valueOf(s[1])];
		int rowlen = tiles[0].length;
		for(int i = 0; i < tiles.length; i++)
			for(int j = 0; j < rowlen; j++)
				tiles[i][j] = new Tile(Integer.valueOf(s[i*rowlen+j]));
		return new Map(tiles);
	}
	
	public static Map emptyMap(int width, int height) {
		Map map = new Map(width, height);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				map.setTile(x, y, new Tile(PresetTile.EMPTY));
			}
		}
		return map;
	}
	
}
