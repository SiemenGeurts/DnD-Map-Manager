package data.mapdata;

import java.awt.Point;

public class Map {

	private int width, height;
	private Tile[][] tiles;
	private Entity[][] entities;
	
	public Map(int _width, int _height) {
		width = _width;
		height = _height;
	}
	
	private Map(Tile[][] _tiles) {
		tiles = _tiles;
	}
	
	public void setTile(int x, int y, Tile tile) {
		tiles[y][x] = tile;
	}
	
	public void addEntity(int x, int y, Entity entity) {
		entities[y][x] = entity;
	}
	
	public Tile getTile(Point p) {
		return tiles[p.y][p.x];
	}
	
	public Tile getTile(int x, int y) {
		return tiles[y][x];
	}
	
	public Entity getEntity(Point p) {
		return entities[p.y][p.x];
	}
	
	public Entity getEntity(int x, int y) {
		return entities[y][x];
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
}
