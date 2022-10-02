package data.mapdata;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import controller.MapBuilderController;
import helpers.ScalingBounds;
import javafx.scene.image.Image;

public class Map {
	
	static {
		MapBuilderController.setEditingKey(new EditingKey());
	}
	
	private File libraryFile = null;
	private boolean isSaved = false;
	ArrayList<Level> levels;
	UpdateHandler updateHandler = null;
	private int activeLevel = 0;
	Set<LevelChangedListener> levelListeners = null;
	
	private static final AtomicInteger idGen = new AtomicInteger();
	
	
	public Map() {
		levels = new ArrayList<>();
	}
	
	public Map(List<Level> levels) {
		 this.levels = new ArrayList<>(levels.size());
		 this.levels.addAll(levels);
		 activeLevel = 0;
	}
	
	public Map(Level... levels) {
		this.levels = new ArrayList<>();
		for(Level level : levels)
			this.levels.add(level);
		activeLevel = 0;
	}
	
	public Map copy() {
		List<Level> copy = levels.stream().map(l -> l.copy()).collect(Collectors.toList());
		Map map = new Map(copy);
		map.setActiveLevel(activeLevel);
		map.setLibraryFile(libraryFile); //will set unsaved
		if(isSaved())
			map.setSaved();
		return map;
	}
	
	public boolean addActiveLevelChangedListener(LevelChangedListener l) {
		if(levelListeners == null)
			levelListeners = new HashSet<>(4);
		return levelListeners.add(l);
	}
	
	public void setUpdateHandler(UpdateHandler handler) {
		this.updateHandler = handler;
	}
	
	public int getNumberOfLevels() {
		return levels.size();
	}
	
	public List<Level> getLevels() {
		return Collections.unmodifiableList(levels);
	}
		
	public Level getActiveLevel() {
		return levels.get(activeLevel);
	}
	
	public int getActiveLevelIndex() {
		return activeLevel;
	}
	
	public Level getLevel(int index) {
		if(index < 0 || index >= levels.size())
			throw new ArrayIndexOutOfBoundsException("That level does not exist");
		return levels.get(index);
	}
	
	public Level getLevelByID(int id) {
		for(Level level : levels)
			if(level.getID() == id)
				return level;
		return null;
	}
	
	public Level getLevelByName(String name) {
		for(Level level : levels)
			if(level.getName().equals(name))
				return level;
		return null;
	}
	
	public Entity getEntityById(int id) {
		Entity e = getActiveLevel().getEntityById(id);
		if(e != null) return e;
		for(int i = 0; i < levels.size(); i++) {
			if(i == activeLevel) continue;
			e = levels.get(i).getEntityById(id);
			if(e != null) return e;
		}
		return null;
	}
	
	public void setLibraryFile(File file) {
		libraryFile = file;
		isSaved = false;
	}
	
	public File getLibraryFile() {
		return libraryFile;
	}
	
	public boolean isSaved() {
		boolean saved = isSaved;
		int i = 0;
		while(saved && i < levels.size()) {
			saved = (saved && levels.get(i).isSaved());
			i++;
		}
		return saved;
	}
	
	public void addLevel(Level level) {
		levels.add(level);
		if(level.getName().trim().equals(""))
			level.setName("Level " + (levels.size()));
		setActiveLevel(levels.size()-1);
		isSaved = false;
	}
	
	public void addLevel(int index, Level level) {
		levels.add(index, level);
		if(level.getName().trim().equals(""))
			level.setName("Level " + (index+1));
		setActiveLevel(index);
		isSaved = false;
	}
	
	public Level addLevel(Tile[][] tiles, ArrayList<Entity> entities, byte[][] mask) {
		Level level = new Level(tiles, mask, entities);
		addLevel(level);
		return level;
	}
	
	public Level addEmptyLevel(int width, int height) {
		Level level = new Level(width, height);
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				level.setTile(x, y, new Tile(PresetTile.EMPTY));
		addLevel(level);
		return level;
	}
	
	public void setLevels(ArrayList<Level> levels, EditingKey key) {
		Objects.requireNonNull(key);
		int activeLevelID = getActiveLevel().getID();
		this.levels = levels;
		//try to keep the active level the same
		for(int i = 0; i < levels.size(); i++) {
			if(levels.get(i).getID()==activeLevelID) {
				setActiveLevel(i);
				return;
			}
		}
		setActiveLevel(0);
	}
	
	public void setActiveLevel(int i) {
		if(i < 0 || i >= levels.size())
			return;
		if(levelListeners != null && levelListeners.size()>0)
			levelListeners.stream().forEach(l -> l.onActiveLevelChanged(activeLevel, i));
		activeLevel = i;
	}
	
	public void setActiveLevelWithID(int id) {
		for(int i = 0; i < levels.size(); i++) {
			if(levels.get(i).getID() == id) {
				setActiveLevel(i);
				return;
			}
		}
	}
	
	public void setTile(int level, int x, int y, Tile tile) {
		if(updateHandler != null)
			updateHandler.onSetTile(level, x, y, levels.get(level).getTile(x,y).getType(), tile.getType());
		levels.get(level).setTile(x,y,tile);
	}
	
	public void setTile(int x, int y, Tile tile) {
		setTile(activeLevel, x,y,tile);
	}
	
	public void addEntity(int level, Entity e) {
		if(updateHandler != null)
			updateHandler.onAddEntity(level, e);
		levels.get(level).addEntity(e);
	}
	
	public void addEntity(Entity e) {
		addEntity(activeLevel,e);
	}

	public void removeEntity(int level, Entity e) {
		if(updateHandler != null)
			updateHandler.onRemoveEntity(level, e);
		levels.get(level).removeEntity(e);
	}
	
	public void removeEntity(Entity e) {
		removeEntity(activeLevel,e);
	}
	
	public void setWholeMask(int level, byte[][] mask) {
		if(updateHandler != null)
			updateHandler.onSetMask(level,levels.get(level).getMask(), mask);
		levels.get(level).setMask(mask);
	}
	
	public void setWholeMask(byte[][] mask) {
		setWholeMask(activeLevel, mask);
	}
	
	public void setMask(int level, int x, int y, byte value) {
		if(updateHandler != null)
			updateHandler.onUpdateMask(level, x, y, levels.get(level).getMask(x,y), value);
		levels.get(level).setMask(x,y,value);
	}
	
	public void setMask(int x, int y, byte value) {
		setMask(activeLevel, x, y, value);
	}
	
	public void setSaved() {
		for(Level level : levels)
			level.setSaved();
		isSaved = true;
	}
	
	public boolean isValidLevelName(String name) {
		if(name == null || name.trim().equals(""))
			return false;
		if(name.contains("'") || name.contains("\"")) 
			return false;
		for(Level level : levels)
			if(level.getName().equals(name))
				return false;
		return true;
	}
	
	//Needed for backwards compatibility
	public static Map MapFromTilesAndEntities(Tile[][] tiles, ArrayList<Entity> entities) {
		Map map = new Map();
		map.addLevel(tiles, entities, null);
		return map;
	}

	public static Map emptyMap(int width, int height) {
		Map map = new Map();
		map.addEmptyLevel(width, height);
		return map;
	}
	
	public static class EditingKey {}
	
	public class Level {
		
		private String name;
		private int width, height;
		private Tile[][] tiles;
		protected ArrayList<Entity> entities;
		protected Image background;
		protected ScalingBounds.ScaleMode mode = ScalingBounds.ScaleMode.FIT;
		private byte[][] mask;
		boolean isSaved = false;
		private int id;
		
		public Level(int _width, int _height) {
			width = _width;
			height = _height;
			tiles = new Tile[_height][_width];
			mask = new byte[_height][_width];
			entities = new ArrayList<>();
			name = "";
			id = idGen.getAndIncrement();
		}
		
		public Level(Tile[][] _tiles) {
			this(_tiles, new byte[_tiles.length][_tiles[0].length], null);
		}
		
		public Level(Tile[][] _tiles, ArrayList<Entity> entities) {
			this(_tiles, new byte[_tiles.length][_tiles[0].length], entities);
			
		}
		
		public Level(Tile[][] _tiles, byte[][] _mask, ArrayList<Entity> entities) {
			tiles = _tiles;
			width = _tiles[0].length;
			height = _tiles.length;
			id = idGen.getAndIncrement();
			name = "";
			if(entities != null)
				this.entities = entities;
			else
				this.entities = new ArrayList<>();
			if(_mask != null) {
				if(_mask.length != tiles.length || _mask[0].length != _tiles[0].length)
					throw new IllegalArgumentException("Mask size does not match map size");
				mask = _mask;
			}else
				mask = new byte[height][width];
		}
		
		public int getID() {
			return id;
		}
		
		public void setName(String name) {
			this.name = name;
			isSaved = false;
		}
		
		public String getName() {
			return name;
		}
		
		protected void setTile(int x, int y, Tile tile) {
			isSaved = (tiles[y][x] == tile);
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
		
		public byte getMask(int x, int y) {
			return mask[y][x];
		}
		
		protected void setMask(int x, int y, byte m) {
			mask[y][x] = m;
			isSaved = false;
		}
		
		public byte[][] getMask() {
			return mask;
		}
		
		protected void setMask(byte[][] m) {
			if(m.length==tiles.length && m[0].length==tiles[0].length) {
				mask = m;
				isSaved = false;
			} else
				throw new IllegalArgumentException("Mask of size " + m.length + "x" + m[0].length + " does not match with level size " + tiles.length + "x" + tiles[0].length);
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
		
		protected void addEntity(Entity e) {
			entities.add(e);
			isSaved = false;
		}
		
		protected void removeEntity(Entity e) {
			entities.remove(e);
			isSaved = false;
		}
		
		protected Entity removeEntity(Point p) {
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
		
		/*
		 * The array of this map should not be used to change the tiles!
		 */
		public Tile[][] getTiles() {
			return tiles;
		}
		
		private void setTiles(Tile[][] tiles) {
			this.tiles = tiles;
			width = tiles[0].length;
			height = tiles.length;
			isSaved = false;
			if(mask.length != tiles.length || mask[0].length != tiles[0].length)
				mask = new byte[height][width];
		}
		
		public void setTiles(Tile[][] tiles, Map.EditingKey key) {
			Objects.requireNonNull(key);
			setTiles(tiles);
		}
		
		public void setMask(byte[][] mask, Map.EditingKey key) {
			Objects.requireNonNull(key);
			setMask(mask);
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
		
		public ArrayList<Entity> getAllEntities(EditingKey key) {
			Objects.requireNonNull(key);
			return entities;
		}
		
		protected void setEntities(ArrayList<Entity> entities) {
			this.entities = entities;
			isSaved = false;
		}

		public Level copy() {
			Tile[][] copiedTiles = new Tile[height][width];
			byte[][] copiedMask = new byte[mask.length][mask[0].length];
			for(int i = 0; i < height; i++)
				for(int j = 0; j < width; j++) {
					copiedTiles[i][j] = tiles[i][j].copy();
					copiedMask[i][j] = mask[i][j];
				}
			Level copy = new Level(copiedTiles);
			copy.entities = new ArrayList<Entity>(entities.stream().map(entity -> entity.copyWidthId(entity.getID())).collect(Collectors.toList()));
			copy.setBackground(background);
			copy.setScaling(mode);
			copy.setMask(copiedMask);
			copy.setUnsaved();
			return copy;
		}
		
		public void setUnsaved() {
			isSaved = false;
		}
		
		private void setSaved() {
			isSaved = true;
		}
		
		public boolean isSaved() {
			return isSaved;
		}
	}
	
	public interface UpdateHandler {
		 void onSetTile(int level, int x, int y, int oldType, int newType);
		 void onAddEntity(int level, Entity e);
		 void onRemoveEntity(int level, Entity e);
		 //void onUpdateEntity(int level, int entityID, Entity oldValue, Entity newValue);
		 void onSetMask(int level, byte[][] oldMask, byte[][] newMask);
		 void onUpdateMask(int level, int x, int y, int oldValue, int newValue);
	}
	
	public interface LevelChangedListener {
		void onActiveLevelChanged(int oldLevel, int newLevel);
	}
}