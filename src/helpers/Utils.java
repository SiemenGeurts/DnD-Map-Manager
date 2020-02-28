package helpers;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import javax.imageio.ImageIO;

import data.mapdata.Map;
import gui.Dialogs;
import helpers.ScalingBounds.ScaleMode;
import helpers.codecs.Decoder;
import helpers.codecs.Encoder;
import javafx.application.Application.Parameters;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.FileChooser;

public class Utils {

	private static Parameters params;
	
	public static Map loadMap(File mapFile) throws IOException {
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
			AssetManager.setLibrary(Library.load(new FileInputStream(libFile)));	
		
		Decoder decoder = Decoder.getDecoder(encodingVersion);
		Map m = decoder.decodeMap(lines.get(lineIndex++));
		m.setLibraryFile(libFile);
		if(lines.size()>lineIndex) {
			byte[] imgbytes = Base64.getDecoder().decode(lines.get(lineIndex++).getBytes());
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(imgbytes));
			m.setBackground(SwingFXUtils.toFXImage(image, null));
			m.setScaling(lines.get(lineIndex).equals("fit") ? ScaleMode.FIT : (lines.get(lineIndex).equals("extend") ? ScaleMode.EXTEND : ScaleMode.STRETCH));
			lineIndex++;
		}
		m.setSaved();
		return m;
	}
	
	public static boolean saveLibrary(Library library, Map map) {
		File file = map.getLibraryFile();
		if(file == null) {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("DnD library files (*.dlib)", "*.dlib"));
			file = fc.showSaveDialog(null);
			if(file == null)
				return false;
			if(!file.getName().endsWith(".dlib"))
				file = new File(file.getAbsolutePath()+ ".dlib");
		}
		if(library.save(file)) {
			map.setLibraryFile(file);
			return true;
		} else
			return false;
	}
	
	public static void saveMap(File mapFile, Map map) throws IOException {
		saveLibrary(AssetManager.getLibrary(), map);
		FileWriter writer = new FileWriter(mapFile, false);
		writer.write(String.valueOf(Encoder.VERSION_ID)+System.lineSeparator());
		writer.write(map.getLibraryFile().getAbsolutePath()+System.lineSeparator());
		writer.write(Encoder.encode(map, true)+System.lineSeparator());
		if(map.getBackground() != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(SwingFXUtils.fromFXImage(map.getBackground(), null), "png", baos);
			writer.write(Base64.getEncoder().encodeToString(baos.toByteArray()));
			writer.write(System.lineSeparator() + map.getScaling().name().toLowerCase());
		}
		writer.close();
		map.setSaved();
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
	
	public static boolean saveRun(Runnable run) {
		if(Platform.isFxApplicationThread()) {
			run.run();
			return true;
		} else {
			Platform.runLater(run);
			return false;
		}
	}
}