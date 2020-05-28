package data.mapdata;

import java.awt.Point;
import java.io.File;
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
	private File libraryFile = null;
	private boolean isSaved = false;
	private byte[][] mask;
	
	public Map(int _width, int _height) {
		width = _width;
		height = _height;
		tiles = new Tile[_height][_width];
		mask = new byte[_height][_width];
		entities = new ArrayList<>();
	}
	
	public Map(Tile[][] _tiles) {
		this(_tiles, new byte[_tiles.length][_tiles[0].length]);
	}
	
	public Map(Tile[][] _tiles, byte[][] _mask) {
		tiles = _tiles;
		width = _tiles[0].length;
		height = _tiles.length;
		entities = new ArrayList<>();
		mask = _mask;
	}
	
	public void setLibraryFile(File file) {
		libraryFile = file;
		isSaved = false;
	}
	
	public File getLibraryFile() {
		return libraryFile;
	}
	
	public void setTile(int x, int y, Tile tile) {
		isSaved = (tiles[x][y] == tile);
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
	
	public byte getMask(int i, int j) {
		return mask[i][j];
	}
	
	public void setMask(int i, int j, byte m) {
		mask[i][j] = m;
		isSaved = false;
	}
	
	public byte[][] getMask() {
		return mask;
	}
	
	public void setMask(byte[][] m) {
		mask = m;
		isSaved = false;
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
		isSaved = false;
	}
	
	public void removeEntity(Entity e) {
		entities.remove(e);
		isSaved = false;
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
		isSaved = false;
		if(mask.length != tiles.length || mask[0].length != tiles[0].length)
			mask = new byte[height][width];
	}
	
	public Image getBackground() {
		return background;
	}

	public void setBackground(Image background) {
		this.background = background;
		isSaved = false;
	}
	
	public void setScaling(ScalingBounds.ScaleMode mode) {
		this.mode = mode;
		isSaved = false;
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
	
	public void setEntities(ArrayList<Entity> entities) {
		this.entities = entities;
		isSaved = false;
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
		copy.setUnsaved();
		return copy;
	}
	
	public void setUnsaved() {
		isSaved = false;
	}
	
	public void setSaved() {
		isSaved = true;
	}
	
	public boolean isSaved() {
		return isSaved;
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