package data.mapdata;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import helpers.ScalingBounds;
import javafx.scene.image.Image;

public class Map {
	
	private int width, height;
	private Tile[][] tiles;
	protected ArrayList<Entity> entities;
	protected Image background;
	protected ScalingBounds.ScaleMode mode = ScalingBounds.ScaleMode.FIT;
	
	public Map(int _width, int _height) {
		width = _width;
		height = _height;
		tiles = new Tile[_height][_width];
		entities = new ArrayList<>();
	}
	
	public Map(Tile[][] _tiles) {
		tiles = _tiles;
		width = _tiles[0].length;
		height = _tiles.length;
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
	
	public Entity getEntityById(int id) {
		for(Entity e : entities)
			if(e.getID()==id)
				return e;
		return null;
	}
	
	public Entity getEntity(int x, int y) {
		for (Entity e : entities) {
			if (x >= e.getTileX() && x < e.getTileX() + e.getWidth() && y >= e.getTileY() && y < e.getTileY() + e.getHeight())
				return e;
		}
		return null;
	}
	
	public void addEntity(Entity e) {
		entities.add(e);
	}
	
	public void removeEntity(Entity e) {
		entities.remove(e);
	}
	
	public Entity removeEntity(Point p) {
		Entity e = getEntity(p);
		if(e != null)
			removeEntity(e);
		return e;
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
	
	public void setTiles(Tile[][] tiles) {
		this.tiles = tiles;
		width = tiles[0].length;
		height = tiles.length;
	}
	
	public Image getBackground() {
		return background;
	}

	public void setBackground(Image background) {
		this.background = background;
	}
	
	public void setScaling(ScalingBounds.ScaleMode mode) {
		this.mode = mode;
	}
	
	public ScalingBounds.ScaleMode getScaling() {
		return mode;
	}

	public List<Entity> getEntities() {
		return Collections.unmodifiableList(entities);
	}
	
	protected ArrayList<Entity> getAllEntities() {
		return entities;
	}

	public Map copy() {
		Tile[][] copiedTiles = new Tile[height][width];
		for(int i = 0; i < height; i++)
			for(int j = 0; j < width; j++)
				copiedTiles[i][j] = tiles[i][j].copy();
		Map copy = new Map(copiedTiles);
		copy.entities = new ArrayList<Entity>(entities.stream().map(entity -> entity.copyWidthId(entity.getID())).collect(Collectors.toList()));
		copy.setBackground(background);
		copy.setScaling(mode);
		return copy;
	}
	
	public String encode(boolean includeEntityData) {
		StringBuilder builder = new StringBuilder();
		builder.append(tiles.length).append(':')
				.append(tiles[0].length);
		for(int i = 0; i < tiles.length; i++)
			for(int j = 0; j < tiles[0].length; j++)
				builder.append(':').append(tiles[i][j].getType());

		builder.append(';').append(entities.size()).append(':');
		for(Entity e : entities)
			builder.append(e.encode(includeEntityData)).append(':');
		return builder.toString();
	}
	
	public static Map decode(String string) {
		int index = string.indexOf(";");
		String[] s = string.substring(0, index).split(":");
		Tile[][] tiles = new Tile[Integer.valueOf(s[0])][Integer.valueOf(s[1])];
		int rowlen = tiles[0].length;
		for(int i = 0; i < tiles.length; i++)
			for(int j = 0; j < rowlen; j++)
				tiles[i][j] = new Tile(Integer.valueOf(s[i*rowlen+j+2]));
		Map map = new Map(tiles);
		
		s = string.substring(index+1).split(":");
		ArrayList<Entity> entities = new ArrayList<>(Integer.valueOf(s[0]));
		for(int i = 1; i < s.length; i++) {
			entities.add(Entity.decode(s[i]));
		}
		map.entities = entities;
		return map;
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