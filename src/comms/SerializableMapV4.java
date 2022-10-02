package comms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import data.mapdata.Map;
import helpers.Logger;
import helpers.ScalingBounds.ScaleMode;
import helpers.codecs.Decoder;
import helpers.codecs.JSONEncoder;

public class SerializableMapV4 implements Serializable {

	private static final long serialVersionUID = -8164093898060950403L;
	transient Map map;
	private boolean hasBackground;
	private int encodingVersion;
	private LevelBackgroundData[] backgrounds;
	private boolean includeEntityProps;
	private int activeLevel;
	
	public SerializableMapV4(Map map, boolean includeBackground) {
		this(map, includeBackground, false);
	}
	
	public SerializableMapV4(Map map, boolean includeBackground, boolean includeEntityProps) {
		this.map = map;
		this.includeEntityProps = includeEntityProps;
		backgrounds = new LevelBackgroundData[map.getNumberOfLevels()];
		activeLevel = map.getActiveLevelIndex();
		hasBackground = false; //init
		for(int i = 0; i < map.getNumberOfLevels(); i++) {
			LevelBackgroundData data = new LevelBackgroundData(i);
			Map.Level level = map.getLevel(i);
			if(level.getBackground()!=null) {
				data.hasBackground = true;
				hasBackground = true;
				if(includeBackground) {
					data.background = new SerializableImage(level.getBackground());
					data.background.setFormat(SerializableImage.Format.JPG);
				}
				data.scaling = level.getScaling();
			}
			backgrounds[i] = data;
		}
		encodingVersion = JSONEncoder.VERSION;
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
		String s = JSONEncoder.encode(map, includeEntityProps).toString();
		stream.writeObject(s.getBytes());
	}
	
	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		stream.defaultReadObject();
		String s = new String((byte[])stream.readObject());
		map = Decoder.getDecoder(encodingVersion).decodeMap(s);
		Logger.println("Decoded map");
		if(backgrounds.length != map.getNumberOfLevels())
			Logger.println("Could not load backgrounds: inconsistent number of levels!");
		if(hasBackground) {
			for(int i = 0; i < map.getNumberOfLevels(); i++) {
				if(backgrounds[i] != null && backgrounds[i].hasBackground
						&& backgrounds[i].background.getImage() != null) {
					map.getLevel(i).setBackground(backgrounds[i].background.getImage());
					map.getLevel(i).setScaling(backgrounds[i].scaling);
				}
			}
		}
		map.setActiveLevel(activeLevel);
	}
	
	public class LevelBackgroundData implements Serializable {
		private static final long serialVersionUID = 134821918815644445L;
		int level;
		boolean hasBackground;
		SerializableImage background;
		private ScaleMode scaling;
		private LevelBackgroundData(int level) {
			this(level, false, null, ScaleMode.FIT);
		}
		
		protected LevelBackgroundData(int level, boolean hasBackground, SerializableImage img, ScaleMode scaling) {
			this.level = level;
			this.hasBackground = hasBackground;
			this.background = img;
			this.scaling = scaling;
		}
	}
}
