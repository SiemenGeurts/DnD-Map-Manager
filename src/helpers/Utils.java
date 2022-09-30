package helpers;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Base64;

import javax.imageio.ImageIO;

import app.MapManagerApp;
import comms.SerializableMapV4;
import comms.SerializableMap;
import controller.SceneManager;
import data.mapdata.Map;
import gui.Dialogs;
import gui.ErrorHandler;
import helpers.ScalingBounds.ScaleMode;
import helpers.codecs.Decoder;
import helpers.codecs.JSONEncoder;
import javafx.application.Application.Parameters;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.FileChooser;

public class Utils {

	private static Parameters params;
	private static FileChooser fileChooser = new FileChooser();
	
	private static final FileChooser.ExtensionFilter mapFilter = new FileChooser.ExtensionFilter("Map files (*.map)", "*.map");
	private static final FileChooser.ExtensionFilter imgFilter = new FileChooser.ExtensionFilter("Image Files (*.png, *.jpg)", "*.png", "*.jpg");
	private static final FileChooser.ExtensionFilter libFilter = new FileChooser.ExtensionFilter("Library files (*.dlib)", "*.dlib");
	
	public static Map loadMap(File mapFile) throws IOException {
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(mapFile));
			int encodingVersion = ois.readInt();
			if(encodingVersion==1 || encodingVersion==JSONEncoder.VERSION) {
				SerializableMapV4 smap;
				Object obj = ois.readObject();
				File libFile;
				boolean changesMade = false;
				if(obj instanceof String) {
					libFile = new File((String) obj);
					if(!libFile.exists()) {
						Dialogs.warning("Can't load the library associated with this map ("  + libFile.getName() + "). You'll have to select it manually.", true);
						libFile = AssetManager.loadLibrary();
						changesMade = true;
					} else
						AssetManager.setLibrary(Library.load(libFile));
					smap = (SerializableMapV4) ois.readObject();
				} else {
					Dialogs.warning("No library was provided by the map, you'll have to select it manually.", true);
					libFile = AssetManager.loadLibrary();
					smap = (SerializableMapV4) obj;
					changesMade = true;
				}
				Map map = smap.getMap();
				map.setLibraryFile(libFile);
				if(!changesMade) map.setSaved();
				ois.close();
				return map;
			} else if(encodingVersion==2) {
				SerializableMap smap;
				Object obj = ois.readObject();
				File libFile;
				boolean changesMade = false;
				if(obj instanceof String) {
					libFile = new File((String) obj);
					if(!libFile.exists()) {
						Dialogs.warning("Can't load the library associated with this map ("  + libFile.getName() + "). You'll have to select it manually.", true);
						libFile = AssetManager.loadLibrary();
						changesMade = true;
					} else
						AssetManager.setLibrary(Library.load(libFile));
					smap = (SerializableMap) ois.readObject();
				} else {
					Dialogs.warning("No library was provided by the map, you'll have to select it manually.", true);
					libFile = AssetManager.loadLibrary();
					smap = (SerializableMap) obj;
					changesMade = true;
				}
				Map map = smap.getMap();
				map.setLibraryFile(libFile);
				if(!changesMade) map.setSaved();
				ois.close();
				return map;
			} else {
				ErrorHandler.handle("Could not load map with version " + encodingVersion, null);
			}
			ois.close();
		} catch(ClassNotFoundException e) {
			ErrorHandler.handle("Could not read map.", e);
			return Map.emptyMap(20, 14);
		} catch(StreamCorruptedException e) {
			ErrorHandler.handle("Could not read map, trying older version...", e);
		}
		//old way of reading maps, before versions were introduced.
		BufferedReader br = new BufferedReader(new FileReader(mapFile));
		ArrayList<String> lines = new ArrayList<String>(5);
		String line;
		while ((line=br.readLine())!= null) {
			lines.add(line);
		}
		br.close();
		int encodingVersion = 1;
		int lineIndex = 0;
		try {
			encodingVersion = Integer.valueOf(lines.get(lineIndex));
			lineIndex += 1;
		} catch(NumberFormatException e) {
			encodingVersion = 1;
			lineIndex = 0;
		}
		File libFile = new File(lines.get(lineIndex++));
		if(!libFile.exists()) {
			Dialogs.warning("Can't load the library associated with this map ("  + libFile.getName() + "). You'll have to select it manually.", true);
			libFile = AssetManager.loadLibrary();
		} else
			AssetManager.setLibrary(Library.load(libFile));	
		
		Decoder decoder = Decoder.getDecoder(encodingVersion);
		Map m = decoder.decodeMap(lines.get(lineIndex++));
		m.setLibraryFile(libFile);
		if(lines.size()>lineIndex) {
			byte[] imgbytes = Base64.getDecoder().decode(lines.get(lineIndex++).getBytes());
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(imgbytes));
			m.getLevel(0).setBackground(SwingFXUtils.toFXImage(image, null));
			m.getLevel(0).setScaling(lines.get(lineIndex).equals("fit") ? ScaleMode.FIT : (lines.get(lineIndex).equals("extend") ? ScaleMode.EXTEND : ScaleMode.STRETCH));
			lineIndex++;
		}
		m.setSaved();
		br.close();
		return m;
	}
	
	public static boolean saveLibrary(Library library, Map map) {
		File file = map.getLibraryFile();
		if(file == null) {
			fileChooser.setTitle("Save Library");
			fileChooser.getExtensionFilters().clear();
			fileChooser.getExtensionFilters().add(libFilter);
			fileChooser.setInitialDirectory(MapManagerApp.getLastMapDirectory());
			file = fileChooser.showSaveDialog(null);
			if(file == null)
				return false;
			MapManagerApp.updateLastMapDirectory(file);
			if(!file.getName().endsWith(".dlib"))
				file = new File(file.getAbsolutePath()+ ".dlib");
		}
		if(library.save(file)) {
			map.setLibraryFile(file);
			Logger.println("Saved library!");
			Dialogs.info("Library was successfully saved!", false);
			return true;
		} else
			return false;
	}
	
	public static void saveMap(File mapFile, Map map) throws IOException {
		saveLibrary(AssetManager.getLibrary(), map);
		FileOutputStream fos = new FileOutputStream(mapFile);
		ObjectOutputStream ous = new ObjectOutputStream(fos);
		ous.writeInt(JSONEncoder.VERSION);
		if(map.getLibraryFile() != null)
			ous.writeObject(map.getLibraryFile().getAbsolutePath());
		ous.writeObject(new SerializableMapV4(map, true, true));
		map.setSaved();
		ous.flush();
		ous.close();
		fos.close();
		Dialogs.info("Map was successfully saved!", false);
	}
	
	
	public static String toBinaryString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for(byte b : bytes) {
			sb.append(String.format("%8s", Integer.toBinaryString(b&0xFF)).replace(' ', '0')).append(' ');
		}
		return sb.toString();
	}
	
	public static boolean isValidIP(String ip) {
		if(ip == null) return false;
		if(!ip.matches("\\A(?:(?:[0-9]{1,3}\\.){3}([0-9]{1,3})(:[0-9]*)?|localhost(:[0-9]*)?)\\z"))
			return false;
		int index = ip.indexOf(':');
		if(index!=-1) {
			if(isValidPort(ip.substring(index+1)))
				ip = ip.substring(0, index);
			else
				return false;
		}
		if(ip.equals("localhost"))
			return true;
		if(ip.indexOf('.')==-1) return false;
		String[] sections = ip.split("\\.");
		for(String s : sections) {
			try {
				if(s.length()>3 || s.length()==0) return false;
				int i = Integer.parseInt(s);
				if(i > 255 || i<0)
					return false;
			} catch(NumberFormatException e) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isValidPort(String s) {
		try {
			int port = Integer.parseInt(s);
			if(!(port>0 && port < 65535))
				return false;
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	public static void setParameters(Parameters _params) {
		params = _params;
	}
	
	public static String getNamedParameter(String key) {
		return params.getNamed().get(key);
	}
	
	public static boolean isUnnamedParameterGiven(String key) {
		return params.getUnnamed().contains(key);
	}
	
	public static boolean safeRun(Runnable run) {
		if(Platform.isFxApplicationThread()) {
			run.run();
			return true;
		} else {
			Platform.runLater(run);
			return false;
		}
	}
	
	public static File openMapDialog() {
		Logger.println("Opening file dialog");
		fileChooser.setTitle("Open map");
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(mapFilter);
		fileChooser.setInitialDirectory(MapManagerApp.getLastMapDirectory());
		File file = fileChooser.showOpenDialog(SceneManager.getPrimaryStage());
		if(file != null) {
			MapManagerApp.updateLastMapDirectory(file);
			try {
				Logger.println("Selected file " + file.getCanonicalPath());
			} catch(IOException e) {}
		}
		return file;
	}
	
	public static File saveMapDialog() {
		Logger.println("Opening save file dialog");
		fileChooser.setTitle("Save map");
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(mapFilter);
		fileChooser.setInitialDirectory(MapManagerApp.getLastMapDirectory());
		File file = fileChooser.showSaveDialog(SceneManager.getPrimaryStage());
		if(file != null)
			MapManagerApp.updateLastMapDirectory(file);
		try {
			Logger.println("Selected file " + file.getCanonicalPath());
		} catch(IOException e) {}
		return file;
	}
	
	public static File openImageDialog() {
		Logger.println("Opening image file dialog");
		fileChooser.setTitle("Open image");
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(imgFilter);
		fileChooser.setInitialDirectory(MapManagerApp.getLastResourceDirectory());
		File file = fileChooser.showOpenDialog(SceneManager.getPrimaryStage());
		if(file != null)
			MapManagerApp.updateLastResourceDirectory(file);
		return file;
	}
	
	public static byte[] flatten(byte[][] arr) {
		byte[] flat = new byte[arr.length*arr[0].length];
		int rowlength = arr[0].length;
		for(int i = 0, k = 0; i < arr.length; i++)
			for(int j = 0; j < rowlength; j++, k++)
				flat[k] = arr[i][j];
		return flat;
	}
	
	public static byte[][] unflatten(byte[] flat, int w, int h) throws Exception {
		if(flat.length!=w*h)
			throw new Exception("Cannot unflatten array of length " + flat.length + " into " + h + "x" + w +" array");
		byte[][] arr = new byte[h][w];
		for(int i = 0, k = 0; i < h; i++)
			for(int j = 0; j < w; j++, k++)
				arr[i][j] = flat[k];
		return arr;
	}
	
	public static String type(Object obj) {
		String type = obj.getClass().getSimpleName();
		if(type.length()!=0)
			return type;
		else {
			type = obj.getClass().getName();
			return type.substring(type.lastIndexOf(".")+1);
		}
	}
}