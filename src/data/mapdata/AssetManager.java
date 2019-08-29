package data.mapdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.scene.image.Image;

public class AssetManager {

	private static AtomicInteger generator;
	private static ArrayList<Integer> openSlots;
	public static HashMap<Integer, Image> textures;
	
	public static void initializeManager() throws IOException {
		textures = new HashMap<>();
		readTexturesFromDisk();
		ArrayList<Integer> takenSlots = new ArrayList<>();
		for (Entry<Integer, Image> entry : textures.entrySet()) {
			takenSlots.add(entry.getKey());
		}
		openSlots = new ArrayList<>();
		int max = -1;
		if (!takenSlots.isEmpty()) {
			max = Collections.max(takenSlots);
			for (int i = 0; i < max; i++) {
				if (!takenSlots.contains(i))
					openSlots.add(i);
			}			
		}
		generator = new AtomicInteger(max + 1);
		
		addTexture(new Image("assets/images/tiles/dirt.png"));
		addTexture(new Image("assets/images/tiles/floor.png"));
		addTexture(new Image("assets/images/tiles/guard.png"));
		addTexture(new Image("assets/images/tiles/intruder.png"));
	}
	
	public static void addTexture(Image texture) {
		if (!openSlots.isEmpty()) {
			 textures.put(openSlots.remove(0), texture);
		} else
			textures.put(generator.getAndIncrement(), texture);
	}
	
	public static void removeTexture(Integer id) {
		textures.remove(id);
		openSlots.add(id);
	}
	
	private static void writeTexturesToDisk() throws IOException {
		
	}
	
	private static void readTexturesFromDisk() throws IOException {
		
	}
	
}
