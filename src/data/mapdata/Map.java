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
}
