package data.mapdata;

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
	
	@Override
	public String toString() {
		return String.valueOf(type);
	}
}
