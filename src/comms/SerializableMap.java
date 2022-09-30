package comms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import data.mapdata.Map;
import helpers.Logger;
import helpers.ScalingBounds.ScaleMode;
import helpers.codecs.Decoder;

/**
 * This is a deprecated class. It's only purpose is reading old maps.
 * @author Joep Geuskens
 */
public class SerializableMap implements Serializable {
	private static final long serialVersionUID = 7313703191129654465L;
	transient Map map;
	private boolean hasBackground;
	private int encodingVersion;
	private ScaleMode scaling;
	private SerializableImage background;
	@SuppressWarnings("unused")
	private boolean includeEntityProps; //needed for backwards compatibility
	
	private SerializableMap() {}
	
	public boolean hasBackground() {
		return hasBackground;
	}
	
	public Map getMap() {
		return map;
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		throw new IllegalArgumentException("Cannot write map of ouddated version");
	}
	
	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		stream.defaultReadObject();
		String s = new String((byte[])stream.readObject());
		Logger.println("Decoded map");
		map = Decoder.getDecoder(encodingVersion).decodeMap(s);
		if(hasBackground)  {
			if(background != null && background.getImage()!=null) {
				map.getActiveLevel().setBackground(background.getImage());
			}
			map.getActiveLevel().setScaling(scaling);
		}
	}
}
