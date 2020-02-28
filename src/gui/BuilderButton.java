package gui;

import data.mapdata.prefabs.Prefab;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BuilderButton<T>  extends Button {
	
	private Prefab<T> prefab;
	ImageView imgView;
	
	public BuilderButton(Prefab<T> _prefab, Image image) {
		prefab = _prefab;
		imgView = new ImageView(image);
		imgView.setFitHeight(40);
		imgView.setFitWidth(40);
		setGraphic(imgView);
	}
	
	public Prefab<T> getPrefab() {
		return prefab;
	}
	
	public void setPrefab(Prefab<T> prefab) {
		this.prefab = prefab;
	}
} 
