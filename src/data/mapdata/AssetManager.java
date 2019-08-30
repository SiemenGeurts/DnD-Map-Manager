package data.mapdata;

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
import javax.swing.filechooser.FileSystemView;

import javafx.embed.swing.SwingFXUtils;
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
	}

	public static void addTexture(Image texture) throws IOException {
		int id = 0;
		if (!openSlots.isEmpty()) {
			textures.put(id = openSlots.remove(0), texture);
		} else
			textures.put(id = generator.getAndIncrement(), texture);
		saveToFile(id, texture);
	}

	public static void removeTexture(Integer id) throws IOException {
		textures.remove(id);
		openSlots.add(id);
		removeFile(id);
	}
	
	private static void removeFile(int id) throws IOException {
		String defaultDirectory = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
		new File(defaultDirectory + "/DnD Map Manager/Textures/" + id + ".png").delete();
		writeTexturesToDisk();
	}

	private static void saveToFile(int id, Image image) throws IOException {
		String defaultDirectory = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
		File outputFile = new File(defaultDirectory + "/DnD Map Manager/Textures/" + id + ".png");
		outputFile.mkdirs();
		BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
		ImageIO.write(bImage, "png", outputFile);
		writeTexturesToDisk();
	}
	
	private static void writeTexturesToDisk() throws IOException {
		String defaultDirectory = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
		FileWriter writer = new FileWriter(new File(defaultDirectory + "/DnD Map Manager/Textures/textureInfo.txt"), false);
		StringBuilder s = new StringBuilder();
		for (Integer i : textures.keySet()) {
			s.append(i + System.lineSeparator());
		}
		writer.write(new String(s));
		writer.close();
	}

	private static void readTexturesFromDisk() throws IOException {
		String defaultDirectory = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
		File f = new File(defaultDirectory + "/DnD Map Manager/Textures/textureInfo.txt");
		if (!f.exists())
			return;
		InputStream is = new FileInputStream(f);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = reader.readLine();
		while (line != null) {
			int id = Integer.valueOf(line);
			System.out.println(defaultDirectory + "\\DnD Map Manager\\Textures\\" + id + ".png");
			textures.put(id, new Image("file://" + defaultDirectory + "/DnD Map Manager/Textures/" + id + ".png"));
			line = reader.readLine();
		}
		reader.close();
	}

}
