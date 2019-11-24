package comms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import data.mapdata.Map;
import helpers.ScalingBounds.ScaleMode;
import helpers.codecs.Decoder;
import helpers.codecs.Encoder;

public class SerializableMap implements Serializable {
	private static final long serialVersionUID = 7313703191129654465L;
	transient Map map;
	boolean hasBackground;
	int encodingVersion;
	ScaleMode scaling;
	SerializableImage background;
	
	public SerializableMap(Map map) {
		this.map = map;
		if(map.getBackground()!=null) {
			hasBackground = true;
			background = new SerializableImage(map.getBackground());
			scaling = map.getScaling();
		} else
			hasBackground = false;
		encodingVersion = Encoder.VERSION_ID;
	}
	
	public Map getMap() {
		return map;
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		String s = Encoder.encode(map, false);
		System.out.println("encoded map: " + s);
		stream.writeObject(s.getBytes());
	}
	
	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		stream.defaultReadObject();
		String s = new String((byte[])stream.readObject());
		System.out.println("map: " + s);
		map = Decoder.getDecoder(encodingVersion).decodeMap(s);
		if(hasBackground)  {
			map.setBackground(background.getImage());
			map.setScaling(scaling);
		}
	}
}
