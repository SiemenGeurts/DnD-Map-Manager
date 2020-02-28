package helpers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class SpriteMap implements Serializable {
	private static final long serialVersionUID = -6453531178594157929L;
	transient HashMap<Integer, Image> sprites;
	
	public SpriteMap() {
		sprites = new HashMap<Integer, Image>();
	}
	
	public Image getImage(int id) {
		return sprites.get(id);
	}
	
	public void putImage(Image image, int id) {
		sprites.put(id, image);
	}
	
	public int addImage(Image img) {
		int newId = 0;
		for(int i : sprites.keySet())
			if(i>=newId)
				newId = i+1;
		putImage(img, newId);
		return newId;
	}
	
	public boolean contains(int id) {
		return sprites.containsKey(id);
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		ArrayList<Image> textures = new ArrayList<>(sprites.size());
		ArrayList<Integer> ids = new ArrayList<>(sprites.size());
		for(Entry<Integer, Image> entry : sprites.entrySet()) {
			ids.add(entry.getKey());
			textures.add(entry.getValue());
		}
		stream.writeObject(ids);
		for(Image img : textures) {
			BufferedImage bimage = SwingFXUtils.fromFXImage(img, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bimage, "png", baos);
			stream.writeObject(baos.toByteArray());
		}
	}
	
	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		stream.defaultReadObject();
		@SuppressWarnings("unchecked")
		ArrayList<Integer> ids = (ArrayList<Integer>) stream.readObject();
		sprites = new HashMap<Integer, Image>(ids.size());
		for(int i = 0; i < ids.size(); i++) {
			byte[] img = (byte[]) stream.readObject();
			BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(img));
			sprites.put(ids.get(i), SwingFXUtils.toFXImage(bimg, null));
		}
	}
}
