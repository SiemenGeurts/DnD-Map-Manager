package comms;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import helpers.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class SerializableImage implements Serializable {
	private static final long serialVersionUID = -5903801998352885750L;
	transient Image image;
	int id;
	
	public SerializableImage(Image image) {
		this.image = image;
	}
	
	public SerializableImage(Image image, int id) {
		this.image = image;
		this.id = id;
	}
	
	public Image getImage() {
		return image;
	}
	
	public int getId() {
		return id;
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		BufferedImage bimage = SwingFXUtils.fromFXImage(image, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bimage, "jpg", baos);
		Logger.println("Image size: " + baos.size() + " bytes");
		stream.writeObject(baos.toByteArray());
	}
	
	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		stream.defaultReadObject();
		byte[] img = (byte[]) stream.readObject();
		BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(img));
		image = SwingFXUtils.toFXImage(bimg, null);
	}
	
}
