package helpers;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import app.MapManagerApp;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class AssetManager {

	private static AtomicInteger generator;
	private static ArrayList<Integer> openSlots;
	public static HashMap<Integer, Image> textures = new HashMap<>();
	
	public static void initializeManager() throws IOException, NullPointerException {
		readTexturesFromDisk();
		ArrayList<Integer> takenSlots = new ArrayList<>();
		for (Entry<Integer, Image> entry : textures.entrySet())
			takenSlots.add(entry.getKey());
		openSlots = new ArrayList<>();
		int max = -1;
		if (!takenSlots.isEmpty()) {
			max = Collections.max(takenSlots);
			for (int i = 0; i < max; i++) {
				if (!takenSlots.contains(i))
					openSlots.add(i);
			}
		}
		generator = new AtomicInteger(Math.max(0, max + 1));
	}

	public static int addTexture(Image texture) throws IOException {
		int id = 0;
		Object result;
		if (!openSlots.isEmpty()) {
			result = textures.put(id = openSlots.remove(0), texture);
		} else
			result = textures.put(id = generator.getAndIncrement(), texture);
		if(result==null)
			saveToFile(id, texture);
		return id;
	}
	
	public static int forceAddTexture(int id, Image texture) throws IOException {
		if(textures.put(id, texture)==null && texture!=null)
			saveToFile(id, texture);
		return id;
	}

	public static boolean removeTexture(Integer id) throws IOException {
		if (id < 0)
			return false;
		textures.remove(id);
		openSlots.add(id);
		return removeFile(id);
	}
	
	private static boolean removeFile(int id) {
		new File(MapManagerApp.defaultDirectory + "/DnD Map Manager/Textures/" + id + ".png").delete();
		try {
			writeTexturesToDisk();			
		} catch(IOException e) {
			return false;
		}
		return true;
	}

	private static void saveToFile(int id, Image image) throws IOException {
		File outputFile = new File(MapManagerApp.defaultDirectory + "/DnD Map Manager/Textures/" + id + ".png");
		outputFile.mkdirs();
		BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
		ImageIO.write(bImage, "png", outputFile);
		writeTexturesToDisk();
	}
	
	private static void writeTexturesToDisk() throws IOException {
		FileWriter writer = new FileWriter(new File(MapManagerApp.defaultDirectory + "/DnD Map Manager/Textures/textureInfo.txt"), false);
		StringBuilder s = new StringBuilder();
		for (Integer i : textures.keySet()) {
			if(i>=0)
				s.append(i + System.lineSeparator());
		}
		writer.write(new String(s));
		writer.close();
	}

	private static void readTexturesFromDisk() throws IOException {
		String defaultDir = MapManagerApp.defaultDirectory;
		File f = new File(defaultDir + "/DnD Map Manager/Textures/textureInfo.txt");
		if (!f.exists())
			return;
		InputStream is = new FileInputStream(f);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = reader.readLine();
		if(!isWindows() && defaultDir.charAt(0)=='/')
			defaultDir=defaultDir.substring(1);
		while (line != null) {
			int id = Integer.valueOf(line);
			if(id>=0) {
				Logger.println("file:/" + defaultDir + "/DnD Map Manager/Textures/" + id + ".png");
				textures.put(id, new Image("file:/" + defaultDir + "/DnD Map Manager/Textures/" + id + ".png"));
				Logger.println("Loading image: " + id + ", success: " + !textures.get(id).isError());
			}
			line = reader.readLine();
		}
		reader.close();
	}
	
	private static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}
}