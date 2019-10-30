package helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.MapManagerApp;
import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.TilePrefab;
import gui.ErrorHandler;
import helpers.codecs.JSONDecoder;
import helpers.codecs.JSONEncoder;

public class JSONManager {

	public static JSONObject json;
	public static final int VERSION_ID = 1;
	private static JSONEncoder encoder = new JSONEncoder();
	private static JSONDecoder decoder = JSONDecoder.get(VERSION_ID);
	
	
	public static void initialize() throws IOException {
		File f = new File(MapManagerApp.defaultDirectory + "/DnD Map Manager/data.json");
		if (!f.exists())
			json = new JSONObject();
		else
			try {
				json = parse(f);
			} catch (JSONException e) {
				ErrorHandler.handle("Couldn't read data file.", e);
				json = new JSONObject();
			}
	}

	public static ArrayList<TilePrefab> getTiles() {
		JSONArray tiles = null;
		try {
			tiles = json.getJSONArray("tiles");
		} catch(JSONException e) {
			Logger.println("No tiles found in data file.");
			return null;
		}
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
		JSONObject entities = null;
		try {
			entities = json.getJSONObject("entities");
		} catch(JSONException e) {
			Logger.println("No entities found in data file.");
			return null;
		}
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
		JSONObject players = null;
		try {
			players = json.getJSONObject("players");
		} catch(JSONException e) {
			Logger.println("No players found in data file.");
			return null;
		}
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
		JSONObject entities;
		try {
			entities = json.getJSONObject("entities");
			entities.put(String.valueOf(prefab.getID()), entity);
		} catch (JSONException e) {
			entities = new JSONObject();
			entities.put(String.valueOf(prefab.getID()), entity);
			json.put("entities", entities);
		}
		save();
	}

	public static void addPlayer(EntityPrefab prefab) {
		JSONObject player = encoder.encode(prefab);
		JSONObject players;
		try {
			players = json.getJSONObject("players");
			players.put(String.valueOf(prefab.getID()), player);
		} catch (JSONException e) {
			players = new JSONObject();
			players.put(String.valueOf(prefab.getID()), player);
			json.put("players", players);
		}
		save();
	}

	public static void addTile(TilePrefab prefab) {
		JSONArray tiles;
		try {
			tiles = json.getJSONArray("tiles");
			tiles.put(prefab.getID());
		} catch (JSONException e) {
			tiles = new JSONArray();
			tiles.put(prefab.getID());
			json.put("tiles", tiles);
		}
		save();
	}

	public static void save() {
		try {
			json.put("version_id", VERSION_ID);
			FileWriter writer = new FileWriter(new File(MapManagerApp.defaultDirectory + "/DnD Map Manager/data.json"),
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
	}
}
