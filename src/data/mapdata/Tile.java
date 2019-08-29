package data.mapdata;

import javafx.scene.image.Image;

public class Tile {
	
	private Image texture;
	
	public Tile(Image _texture) {
		texture = _texture;
	}
	
	public Image getTexture() {
		return texture;
	}
}
