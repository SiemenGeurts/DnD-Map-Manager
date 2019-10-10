package helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.filechooser.FileSystemView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import data.mapdata.Property;
import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.TilePrefab;
import gui.ErrorHandler;

public class JSONManager {
	
	public static JSONObject json;
	public static String defaultDirectory;
	
	public static void initialize() throws IOException {
		defaultDirectory = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
		File f = new File(defaultDirectory + "/DnD Map Manager/data.json");
		if(!f.exists())
			json = new JSONObject();
		else
			try {
				json = parse(f);
			} catch(JSONException e) {
				ErrorHandler.handle("Couldn't read data file.", e);
				json = new JSONObject();
			}
	}
	
	public static ArrayList<TilePrefab> getTiles() {
		try {
			JSONArray tiles = json.getJSONArray("tiles");
			ArrayList<TilePrefab> tileList = new ArrayList<>(tiles.length());
			for(int i = 0; i < tiles.length(); i++)
				tileList.add(new TilePrefab(tiles.getInt(i)));
			return tileList;
		} catch(JSONException e) {
			ErrorHandler.handle("Could not load tiles.", e);
			return null;
		}
	}
	
	public static ArrayList<EntityPrefab> getEntities() {
		try {
			JSONObject entities = json.getJSONObject("entities");
			ArrayList<EntityPrefab> entityList = new ArrayList<>(entities.length());
			for(String key : entities.keySet())
				entityList.add(getEntity(entities.getJSONObject(key)));
			return entityList;
		} catch(JSONException e) {
			ErrorHandler.handle("Could not load enemies.", e);
			return null;
		}
	}
	
	public static ArrayList<EntityPrefab> getPlayers() {
		try {
			JSONObject players = json.getJSONObject("players");
			ArrayList<EntityPrefab> playerList = new ArrayList<>(players.length());
			for(String key : players.keySet())
				playerList.add(getEntity(players.getJSONObject(key)));
			return playerList;
		} catch(JSONException e) {
			ErrorHandler.handle("Could not load players.", e);
			return null;
		}
	}
	
	private static EntityPrefab getEntity(JSONObject json) {
		JSONObject properties = json.getJSONObject("properties");
		ArrayList<Property> propertylist = new ArrayList<>(properties.length());
		for(String pkey : properties.keySet())
			propertylist.add(new Property(pkey, properties.getString(pkey)));
		EntityPrefab ep = new EntityPrefab(json.getInt("type"), json.getInt("width"), json.getInt("height"), propertylist, json.getBoolean("bloodied"), false, (json.has("description") ? json.getString("description") : ""), json.getString("name"));
		System.out.println("Loaded entity: " + ep);
		return ep;
	}
	
	private static JSONObject createEntity(EntityPrefab prefab) {
		JSONObject entity = new JSONObject();
		entity.put("width", prefab.width);
		entity.put("height", prefab.height);
		entity.put("bloodied", prefab.bloodied);
		entity.put("type", prefab.getID());
		entity.put("description", prefab.description);
		entity.put("name", prefab.name);
		JSONObject properties = new JSONObject();
		for(Property p : prefab.getProperties())
			properties.put(p.getKey(), p.getValue());
		entity.put("properties", properties);
		return entity;
	}
	
	public static void addEntity(EntityPrefab prefab) {
		JSONObject entity = createEntity(prefab);
		JSONObject entities;
		try {
			entities = json.getJSONObject("entities");
			entities.put(String.valueOf(prefab.getID()), entity);
		} catch(JSONException e) {
			entities = new JSONObject();
			entities.put(String.valueOf(prefab.getID()), entity);
			json.put("entities", entities);
		}
		save();
	}
	
	public static void addPlayer(EntityPrefab prefab) {
		JSONObject player = createEntity(prefab);
		JSONObject players;
		try {
			players = json.getJSONObject("players");
			players.put(String.valueOf(prefab.getID()), player);
		} catch(JSONException e) {
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
		} catch(JSONException e) {
			tiles = new JSONArray();
			tiles.put(prefab.getID());
			json.put("tiles", tiles);
		}
		save();
	}
	
	public static void save() {
		try {
			FileWriter writer = new FileWriter(new File(defaultDirectory + "/DnD Map Manager/data.json"), false);
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
		if(in != null) {
			while((s = reader.readLine()) != null) {
				buf.append(s + "\n");
			}
		}
		in.close();
		reader.close();
		return new JSONObject(buf.toString());
	}
}
