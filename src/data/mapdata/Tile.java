package data.mapdata;

import helpers.AssetManager;
import javafx.scene.image.Image;

public class Tile {
	
	private int type;
	
	public Tile(Integer _type) {
		type = _type;
	}
	
	public Integer getType() {
		return type;
	}
	
	public void setType(int _type) {
		type = _type;
	}
	
	public Image getTexture() {
		return AssetManager.textures.get(type);
	}
	
	public Tile copy() {
		return new Tile(type);
	}
	
	@Override
	public String toString() {
		return String.valueOf(type);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Tile)
			return ((Tile) obj).getType() == type;
		else if(obj instanceof Integer)
			return ((Integer)obj).intValue() == type;
		else return false;
	}
}
