package data.mapdata;

public class Map {

	private int width, height;
	private Tile[][] tiles;
	
	
	public Map(int _width, int _height) {
		width = _width;
		height = _height;
	}
	
	public void setTile(int x, int y, Tile tile) {
		tiles[y][x] = tile;
	}
	
	public Tile getTile(int x, int y) {
		return tiles[y][x];
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
