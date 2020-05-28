package helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gui.ErrorHandler;
import javafx.scene.image.Image;

public class Library {
	
	public static final int VERSION_ID = 1;
	private JSONObject entities, players;
	private JSONArray tiles;
	private SpriteMap spriteMap;
	private boolean isSaved = true;
	
	private Library(JSONObject entities, JSONObject players, JSONArray tiles, SpriteMap map) {
		this.entities = entities;
		this.players = players;
		this.tiles = tiles;
		this.spriteMap = map;
	}
	
	private Library() {
		entities = new JSONObject();
		players = new JSONObject();
		tiles = new JSONArray();
		spriteMap = new SpriteMap();
		isSaved = false;
	}
	
	public JSONArray getTiles() {
		return tiles;
	}
	
	public JSONObject getEntities() {
		return entities;
	}
	
	public JSONObject getPlayers() {
		return players;
	}
	
	public Image getTexture(int id) {
		return spriteMap.getImage(id);
	}
	
	public int addTexture(Image img) {
		isSaved = false;
		return spriteMap.addImage(img);
	}
	
	public void putTexture(int id, Image img) {
		isSaved = false;
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
			if(version != VERSION_ID)
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
			objOut.writeObject(VERSION_ID);
			objOut.writeObject(lib.entities.toString());
			objOut.writeObject(lib.players.toString());
			objOut.writeObject(lib.tiles.toString());
			objOut.writeObject(lib.spriteMap);
			return true;
		} catch(JSONException | IOException e) {
			ErrorHandler.handle("Couldn't save library", e);
			return false;
		}
	}
}