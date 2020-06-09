package helpers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import app.MapManagerApp;
import controller.SceneManager;
import gui.ErrorHandler;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

public class AssetManager {
	
	private static Library library;
	private final static FileChooser fs = new FileChooser();
	private static HashMap<Integer, Image> presetTextures = new HashMap<Integer, Image>();
	private static Image unknownTexture;

	static {
		fs.setTitle("Choose a library");
		fs.getExtensionFilters().add(new FileChooser.ExtensionFilter("Library Files (*.dlib)", "*.dlib"));
		fs.setInitialDirectory(new File(MapManagerApp.defaultDirectory));
	}
	
	public static File initializeManager(boolean allowLibrarySelection) {
		unknownTexture = new Image("assets/images/unknown.png");
		if(allowLibrarySelection) {
			File libraryFile = loadLibrary();
			if(libraryFile == null)
				library = Library.emptyLibrary();
			else
				return libraryFile;
		} else
			library = Library.emptyLibrary();
		return null;
	}
	
	public static File loadLibrary() {
		while(true) {
			try {
				File file = fs.showOpenDialog(SceneManager.getPrimaryStage());
				if(file == null)
					return null;
				setLibrary(Library.load(file));
				return file;
			} catch(IOException e) {
				ErrorHandler.handle("Could not load library.", e);
			}
		}
	}
	
	public static void setLibrary(Library lib) {
		library = lib;
	}
	
	public static Library getLibrary() {
		return library;
	}
	
	public static void addPresetTextures(HashMap<Integer, Image> map) {
		presetTextures.putAll(map);
	}
	
	public static Image getTexture(int id) {
		if(id<0)
			return presetTextures.get(id);
		Image tex = library.getTexture(id);
		if(tex == null)
			return unknownTexture;
		return tex;
	}
	
	public static void putTexture(int id, Image img) {
		if(id < 0)
			presetTextures.put(id, img);
		library.putTexture(id, img);
	}
	
	public static int addTexture(Image img) {
		return library.addTexture(img);
	}
	
	public static boolean textureExists(int id) {
		return library.textureExists(id) || presetTextures.containsKey(id);
	}

	/*
	public static int addTexture(Image texture) throws IOException {
		library.
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
		new File(MapManagerApp.defaultDirectory + "/Textures/" + id + ".png").delete();
		try {
			writeTexturesToDisk();			
		} catch(IOException e) {
			return false;
		}
		return true;
	}

	private static void saveToFile(int id, Image image) throws IOException {
		File outputFile = new File(MapManagerApp.defaultDirectory + "/Textures/" + id + ".png");
		outputFile.mkdirs();
		BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
		ImageIO.write(bImage, "png", outputFile);
		writeTexturesToDisk();
	}
	
	private static void writeTexturesToDisk() throws IOException {
		FileWriter writer = new FileWriter(new File(MapManagerApp.defaultDirectory + "/Textures/textureInfo.txt"), false);
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
		File f = new File(defaultDir + "/Textures/textureInfo.txt");
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
				Logger.println("file:/" + defaultDir + "/Textures/" + id + ".png");
				textures.put(id, new Image("file:/" + defaultDir + "/Textures/" + id + ".png"));
				Logger.println("Loading image: " + id + ", success: " + !textures.get(id).isError());
			}
			line = reader.readLine();
		}
		reader.close();
	}
	private static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}
	 */
}