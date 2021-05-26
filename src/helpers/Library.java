package helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.TilePrefab;
import gui.ErrorHandler;
import helpers.codecs.JSONDecoder;
import helpers.codecs.JSONEncoder;
import javafx.scene.image.Image;

public class Library {
	
	private HashMap<Integer, EntityPrefab> entities, players;
	private HashMap<Integer, TilePrefab> tiles;
	private SpriteMap spriteMap;
	private boolean isSaved = true;
	
	private Library(JSONObject entities, JSONObject players, JSONArray tiles, SpriteMap map) {
		this.entities = JSONManager.parseEntities(entities);
		this.players = JSONManager.parsePlayers(players);
		this.tiles = JSONManager.parseTiles(tiles);
		this.spriteMap = map;
	}
	
	private Library() {
		entities = new HashMap<>();
		players = new HashMap<>();
		tiles = new HashMap<>();
		spriteMap = new SpriteMap();
		isSaved = false;
	}
	
	public HashMap<Integer, TilePrefab> getTiles() {
		return tiles;
	}
	
	public HashMap<Integer, EntityPrefab> getEntities() {
		return entities;
	}
	
	public HashMap<Integer, EntityPrefab> getPlayers() {
		return players;
	}
	
	public void addEntity(EntityPrefab prefab) {
		entities.put(prefab.getType(), prefab);
		setUnsaved();

	}

	public void addPlayer(EntityPrefab prefab) {
		players.put(prefab.getType(), prefab);
		setUnsaved();
	}

	public void addTile(TilePrefab prefab) {
		tiles.put(prefab.getType(), prefab);
		setUnsaved();
	}
	
	public void removeTile(int id) {
		tiles.remove(id);
		setUnsaved();
	}
	
	public void removeEntity(int id) {
		entities.remove(id);
		setUnsaved();
	}

	public void removePlayer(int id) {
		players.remove(id);
		setUnsaved();
	}
	
	public Image getTexture(int id) {
		return spriteMap.getImage(id);
	}
	
	public int addTexture(Image img) {
		isSaved = false;
		setUnsaved();
		return spriteMap.addImage(img);
	}
	
	public void putTexture(int id, Image img) {
		isSaved = false;
		setUnsaved();
		spriteMap.putImage(img, id);
	}
	
	public boolean textureExists(int id) {
		return spriteMap.contains(id);
	}

	public boolean isSaved() {
		return isSaved;
	}
	
	public void setUnsaved() {
		isSaved = false;
	}
	
	public boolean save(File fout) {
		try {
			isSaved = write(this, new FileOutputStream(fout));
			return isSaved;
		} catch(IOException e) {
			ErrorHandler.handle("Could not save library.", e);
			isSaved = false;
			return false;
		}
	}
	
	public boolean removeTexture(int id) {
		return spriteMap.sprites.remove(id) != null;
	}
	
	public static Library emptyLibrary() {
		return new Library();
	}
	
	public static Library load(File file) throws IOException {
		try {
			FileInputStream in = new FileInputStream(file);
			ObjectInputStream objIn = new ObjectInputStream(in);
			int version = (Integer) objIn.readObject();
			JSONManager.setVersion(version);
			JSONObject entities = new JSONObject((String) objIn.readObject());
			JSONObject players = new JSONObject((String) objIn.readObject());
			JSONArray tiles = new JSONArray((String)objIn.readObject());
			SpriteMap sm = (SpriteMap) objIn.readObject();
			in.close();
			return new Library(entities, players, tiles, sm);
		} catch (ClassNotFoundException e) {
			ErrorHandler.handle("Something went really wrong, notify the idiot who designed this.", e);
		}
		return new Library();
	}
	
	private static boolean write(Library lib, OutputStream out) {
		try {
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			objOut.writeObject(JSONEncoder.VERSION);
			objOut.writeObject(JSONManager.encodeEntities(lib.entities).toString());
			objOut.writeObject(JSONManager.encodePlayers(lib.players).toString());
			objOut.writeObject(JSONManager.encodeTiles(lib.tiles).toString());
			objOut.writeObject(lib.spriteMap);
			return true;
		} catch(JSONException | IOException e) {
			ErrorHandler.handle("Couldn't save library", e);
			return false;
		}
	}
	private static class JSONManager {

		private static JSONDecoder decoder = JSONDecoder.get(JSONEncoder.VERSION);
		
		public static void setVersion(int version) {
			decoder = JSONDecoder.get(version);
		}

		public static HashMap<Integer, TilePrefab> parseTiles(JSONArray tiles) {
			HashMap<Integer, TilePrefab> tileList = new HashMap<>(tiles.length());
			for (int i = 0; i < tiles.length(); i++)
				try {
					TilePrefab tp = new TilePrefab(tiles.getInt(i));
					tileList.put(tp.getType(), tp);
				} catch (JSONException e) {
					ErrorHandler.handle("Could not load tiles.", e);
				}
			return tileList;
		}

		public static HashMap<Integer, EntityPrefab> parseEntities(JSONObject entities) {
			HashMap<Integer, EntityPrefab> entityList = new HashMap<>(entities.length());
			for (String key : entities.keySet())
				try {
					EntityPrefab ep = decoder.decodeEntityPrefab(entities.getJSONObject(key));
					entityList.put(ep.getType(), ep);
				} catch (JSONException e) {
					ErrorHandler.handle("Could not load enemy " + key + ".", e);
				}
			return entityList;
		}

		public static HashMap<Integer, EntityPrefab> parsePlayers(JSONObject players) {
			HashMap<Integer, EntityPrefab> playerList = new HashMap<>(players.length());
			for (String key : players.keySet()) {
				try {
					EntityPrefab p = decoder.decodeEntityPrefab(players.getJSONObject(key));
					p.isPlayer = true;
					playerList.put(p.getType(), p);
				} catch (JSONException e) {
					ErrorHandler.handle("Could not load players.", e);
				}
			}
			return playerList;
		}
		
		public static JSONObject encodeEntities(HashMap<Integer, EntityPrefab> entities) {
			JSONObject json = new JSONObject();
			for(Entry<Integer, EntityPrefab> entry : entities.entrySet()) {
				JSONObject entity = JSONEncoder.encode(entry.getValue());
				json.put(String.valueOf(entry.getValue().getType()), entity);
			}
			return json;
		}
		
		public static JSONObject encodePlayers(HashMap<Integer, EntityPrefab> players) {
			JSONObject json = new JSONObject();
			for(Entry<Integer, EntityPrefab> entry : players.entrySet()) {
				JSONObject entity = JSONEncoder.encode(entry.getValue());
				json.put(String.valueOf(entry.getValue().getType()), entity);
			}
			return json;
		}
		
		public static JSONArray encodeTiles(HashMap<Integer, TilePrefab> tiles) {
			JSONArray json = new JSONArray();
			for(Entry<Integer, TilePrefab> entry : tiles.entrySet()) {
				json.put(entry.getValue().getType());
			}
			return json;
		}
		
	}
}