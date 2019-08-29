package data.mapdata;

import javafx.scene.image.Image;

public class Entity {

private int type;
	
	public Entity(Integer _type) {
		type = _type;
	}
	
	public Integer getType() {
		return type;
	}
	
	public Image getTexture() {
		return AssetManager.textures.get(type);
	}
	
}
