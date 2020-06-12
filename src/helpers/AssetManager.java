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
				fs.setInitialDirectory(MapManagerApp.getLastMapDirectory());
				File file = fs.showOpenDialog(SceneManager.getPrimaryStage());
				if(file == null)
					return null;
				MapManagerApp.updateLastMapDirectory(file);
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
}