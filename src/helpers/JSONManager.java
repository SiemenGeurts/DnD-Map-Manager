package helpers;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.TilePrefab;
import gui.ErrorHandler;
import helpers.codecs.JSONDecoder;
import helpers.codecs.JSONEncoder;

public class JSONManager {

	public static final int VERSION_ID = 1;
	private static JSONEncoder encoder = new JSONEncoder();
	private static JSONDecoder decoder = JSONDecoder.get(VERSION_ID);
	
	public static void setVersion(int version) {
		decoder = JSONDecoder.get(version);
	}

	public static ArrayList<TilePrefab> getTiles() {
		JSONArray tiles = AssetManager.getLibrary().getTiles();
		ArrayList<TilePrefab> tileList = new ArrayList<>(tiles.length());
		for (int i = 0; i < tiles.length(); i++)
			try {
				tileList.add(new TilePrefab(tiles.getInt(i)));
			} catch (JSONException e) {
				ErrorHandler.handle("Could not load tiles.", e);
			}
		return tileList.size() > 0 ? tileList : null;
	}

	public static ArrayList<EntityPrefab> getEntities() {
		JSONObject entities = AssetManager.getLibrary().getEntities();
		ArrayList<EntityPrefab> entityList = new ArrayList<>(entities.length());
		for (String key : entities.keySet())
			try {
				entityList.add(decoder.decodeEntity(entities.getJSONObject(key)));
			} catch (JSONException e) {
				ErrorHandler.handle("Could not load enemy " + key + ".", e);
				// return null;
			}
		return entityList.size() > 0 ? entityList : null;
	}

	public static ArrayList<EntityPrefab> getPlayers() {
		JSONObject players = AssetManager.getLibrary().getPlayers();
		ArrayList<EntityPrefab> playerList = new ArrayList<>(players.length());
		for (String key : players.keySet()) {
			try {
				EntityPrefab p = decoder.decodeEntity(players.getJSONObject(key));
				p.isPlayer = true;
				playerList.add(p);
			} catch (JSONException e) {
				ErrorHandler.handle("Could not load players.", e);
			}
		}
		return playerList.size() > 0 ? playerList : null;
	}

	public static void addEntity(EntityPrefab prefab) {
		JSONObject entity = encoder.encode(prefab);
		JSONObject entities = AssetManager.getLibrary().getEntities();
		entities.put(String.valueOf(prefab.getType()), entity);
		AssetManager.getLibrary().setUnsaved();
	}

	public static void addPlayer(EntityPrefab prefab) {
		JSONObject player = encoder.encode(prefab);
		JSONObject players = AssetManager.getLibrary().getPlayers();
		players.put(String.valueOf(prefab.getType()), player);
		AssetManager.getLibrary().setUnsaved();
	}

	public static void addTile(TilePrefab prefab) {
		JSONArray tiles = AssetManager.getLibrary().getTiles();
		tiles.put(prefab.getType());
		AssetManager.getLibrary().setUnsaved();
	}
	
	public static void removeTile(int id) {
		JSONArray tiles = AssetManager.getLibrary().getTiles();
		for(int i = 0; i < tiles.length(); i++)
			if(tiles.getInt(i)==id) {
				tiles.remove(i);
				return;
			}
	}
	
	public static void removeEntity(int id) {
		AssetManager.getLibrary().getEntities().remove(String.valueOf(id));
	}

	public static void removePlayer(int id) {
		AssetManager.getLibrary().getPlayers().remove(String.valueOf(id));
	}
	/*public static void save() {
		try {
			json.put("version_id", VERSION_ID);
			FileWriter writer = new FileWriter(new File(MapManagerApp.defaultDirectory + "/data.json"),
					false);
			writer.write(json.toString());
			writer.close();
		} catch (IOException e) {
			ErrorHandler.handle("Data could not be saved.", e);
		}
	}

	private static JSONObject parse(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuffer buf = new StringBuffer();
		String s;
		if (in != null) {
			while ((s = reader.readLine()) != null) {
				buf.append(s + "\n");
			}
		}
		in.close();
		reader.close();
		return new JSONObject(buf.toString());
	}*/
}
