package data.mapdata;

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
	
	public static void initializeManager() {
		ArrayList<Integer> takenSlots = new ArrayList<>();
		for (Entry<Integer, Image> entry : textures.entrySet()) {
			takenSlots.add(entry.getKey());
		}
		
		int max = Collections.max(takenSlots);
		generator = new AtomicInteger(max + 1);
		openSlots = new ArrayList<>();
		for (int i = 0; i < max; i++) {
			if (!takenSlots.contains(i))
				openSlots.add(i);
		}
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
	
}
