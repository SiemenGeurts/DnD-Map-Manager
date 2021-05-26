package comms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import data.mapdata.Map;
import helpers.Logger;
import helpers.ScalingBounds.ScaleMode;
import helpers.codecs.Decoder;
import helpers.codecs.Encoder;

public class SerializableMap implements Serializable {
	private static final long serialVersionUID = 7313703191129654465L;
	transient Map map;
	private boolean hasBackground;
	private int encodingVersion;
	private ScaleMode scaling;
	private SerializableImage background;
	private boolean includeEntityProps;
	
	public SerializableMap(Map map, boolean includeBackground) {
		this(map, includeBackground, false);
	}
	
	public SerializableMap(Map map, boolean includeBackground, boolean includeEntityProps) {
		this.map = map;
		this.includeEntityProps = includeEntityProps;
		if(map.getBackground()!=null) {
			hasBackground = true;
			if(includeBackground)
				background = new SerializableImage(map.getBackground());
			else
				background = null;
			scaling = map.getScaling();
		} else
			hasBackground = false;
		encodingVersion = Encoder.VERSION_ID;
	}
	
	public void setIncludeEntityProperties(boolean includeEntityProps) {
		this.includeEntityProps = includeEntityProps;
	}
	
	public boolean hasBackground() {
		return hasBackground;
	}
	
	public Map getMap() {
		return map;
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		String s = Encoder.encode(map, includeEntityProps);
		//Logger.println("encoded map: " + s);
		stream.writeObject(s.getBytes());
	}
	
	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		stream.defaultReadObject();
		String s = new String((byte[])stream.readObject());
		Logger.println("Decoded map");
		map = Decoder.getDecoder(encodingVersion).decodeMap(s);
		if(hasBackground)  {
			if(background != null && background.getImage()!=null) {
				map.setBackground(background.getImage());
			}
			map.setScaling(scaling);
		}
	}
}
