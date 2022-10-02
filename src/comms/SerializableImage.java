package comms;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import gui.ErrorHandler;
import helpers.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class SerializableImage implements Serializable {
	private static final long serialVersionUID = -5903801998352885750L;

	public static enum Format {
		PNG("png"),
		JPG("jpg");
		
		public String name;
		Format(String name) {
			this.name = name;
		}
		public String asString() {
			return name;
		}
	}
	
	transient Image image;
	int id;
	boolean show = false;
	transient Format format;
	
	public SerializableImage(Image image) {
		this.image = image;
	}
	
	public SerializableImage(Image image, int id) {
		this.image = image;
		this.id = id;
	}
	
	public void setFormat(Format format) {
		this.format = format;
	}
	
	public void setShow(boolean show) {
		this.show = true;
	}
	
	public boolean getShow() {
		return show;
	}
	
	public boolean shouldShow() {
		return show;
	}
	
	public Image getImage() {
		return image;
	}
	
	public int getId() {
		return id;
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		/*BufferedImage bimage = SwingFXUtils.fromFXImage(image, null);
		Logger.println("Image dims: " + bimage.getWidth() +  "x" + bimage.getHeight());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		boolean result = ImageIO.write(bimage, "png", baos);
		Logger.println("Image size: " + baos.size() + " bytes " + result);
		stream.writeObject(baos.toByteArray());*/
		try {
			boolean success = false;
			if(format == Format.JPG) {
				BufferedImage img = SwingFXUtils.fromFXImage(image, null);
				BufferedImage newBufferedImage = new BufferedImage(img.getWidth(),
						  img.getHeight(), BufferedImage.TYPE_INT_RGB);
						newBufferedImage.getGraphics().drawImage(img, 0, 0, Color.WHITE, null);
				success = ImageIO.write(newBufferedImage, (format == null ? Format.PNG : format).asString(), stream);
			} else
				success = ImageIO.write(SwingFXUtils.fromFXImage(image, null), (format == null ? Format.PNG : format).asString(), stream);
			if(!success) {
				Logger.error("Could not save image!");
			}
		} catch(IOException e) {
			Logger.error(e);
		}
	}
	
	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		stream.defaultReadObject();
		try {
			BufferedImage bimg = ImageIO.read(stream);
			image = SwingFXUtils.toFXImage(bimg, null);
		} catch(IOException | NullPointerException e1) {
			//Old method
			byte[] img = (byte[]) stream.readObject();
			try {
				BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(img));
				image = SwingFXUtils.toFXImage(bimg, null);
			} catch(IIOException | NullPointerException e) {
				ErrorHandler.handle("Couldn't load image", e);
			}
		}
	}
	
}
