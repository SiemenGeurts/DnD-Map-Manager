package helpers;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import javax.imageio.ImageIO;

import data.mapdata.Map;
import helpers.ScalingBounds.ScaleMode;
import javafx.embed.swing.SwingFXUtils;

public class Utils {

	public static Map loadMap(File mapFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(mapFile));
		ArrayList<String> lines = new ArrayList<String>(4);
		String line;
		while ((line=br.readLine())!= null) {
			lines.add(line);
		}
		br.close();
		Map m = Map.decode(lines.get(0));
		if(lines.size()>1) {
			byte[] imgbytes = Base64.getDecoder().decode(lines.get(1).getBytes());
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(imgbytes));
			m.setBackground(SwingFXUtils.toFXImage(image, null));
			m.setScaling(lines.get(2).equals("fit") ? ScaleMode.FIT : (lines.get(2).equals("extend") ? ScaleMode.EXTEND : ScaleMode.STRETCH));
		}
		return m;
	}
	
	public static void saveMap(File mapFile, Map map) throws IOException {
		FileWriter writer = new FileWriter(mapFile, false);
		writer.write(map.encode()+System.lineSeparator());
		if(map.getBackground() != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(SwingFXUtils.fromFXImage(map.getBackground(), null), "png", baos);
			writer.write(Base64.getEncoder().encodeToString(baos.toByteArray()));
			writer.write(System.lineSeparator() + map.getScaling().name().toLowerCase());
		}
		writer.close();
	}
	
	
	public static String toBinaryString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for(byte b : bytes) {
			sb.append(String.format("%8s", Integer.toBinaryString(b&0xFF)).replace(' ', '0')).append(' ');
		}
		return sb.toString();
	}
}
