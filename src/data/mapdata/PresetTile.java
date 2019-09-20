package data.mapdata;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import helpers.AssetManager;
import javafx.scene.image.Image;

public class PresetTile {

	public static final int EMPTY = -5, FLOOR = -4, WALL = -3, BUSHES = -2;
	
	private static HashMap<Integer, Image> presetTextures = new HashMap<>();
	
	static {
		presetTextures.put(EMPTY, new Image("assets/images/tiles/floor.png"));
		presetTextures.put(FLOOR, new Image("assets/images/tiles/dirt.png"));
		presetTextures.put(WALL, new Image("assets/images/tiles/wall.png"));
		presetTextures.put(BUSHES, new Image("assets/images/tiles/bushes.png"));		
	}
	
	public static void setupPresetTiles() throws IOException {
		for (Entry<Integer, Image> entry : presetTextures.entrySet())
			AssetManager.forceAddTexture(entry.getKey(), entry.getValue());
	}
	
}
