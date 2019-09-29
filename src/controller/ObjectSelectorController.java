package controller;

import java.util.ArrayList;

import data.mapdata.PresetTile;
import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.Prefab;
import data.mapdata.prefabs.TilePrefab;
import gui.BuilderButton;
import gui.GridSelectionPane;
import helpers.AssetManager;
import helpers.JSONManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;

public class ObjectSelectorController {

	@FXML
	ScrollPane entityScrollPane;
	@FXML
	ScrollPane tileScrollPane;
	@FXML
	ScrollPane playerScrollPane;
	@FXML
	private GridSelectionPane tilePane, entityPane, playerPane;
	
	MapEditorController editor;
	
	@FXML
	void initialize() {
		tilePane = new GridSelectionPane(5);
		//tilePane.add(createButton(new TilePrefab(PresetTile.EMPTY), AssetManager.textures.get(PresetTile.EMPTY)));
		tilePane.add(createButton(new TilePrefab(PresetTile.FLOOR), AssetManager.textures.get(PresetTile.FLOOR)));
		tilePane.add(createButton(new TilePrefab(PresetTile.WALL), AssetManager.textures.get(PresetTile.WALL)));
		tilePane.add(createButton(new TilePrefab(PresetTile.BUSHES), AssetManager.textures.get(PresetTile.BUSHES)));
		tileScrollPane.setContent(tilePane);
		entityPane = new GridSelectionPane(5);
		entityScrollPane.setContent(entityPane);
		playerPane = new GridSelectionPane(5);
		playerScrollPane.setContent(playerPane);
		ArrayList<TilePrefab> tiles = JSONManager.getTiles();
		if(tiles != null)
			for(TilePrefab tp : tiles)
				tilePane.add(createButton(tp, AssetManager.textures.get(tp.getID())));
		ArrayList<EntityPrefab> entities = JSONManager.getEntities();
		if(entities != null)
			for(EntityPrefab ep : entities)
				entityPane.add(createButton(ep, AssetManager.textures.get(ep.getID())));
		entities = JSONManager.getPlayers();
		if(entities != null)
			for(EntityPrefab ep : entities)
				createButton(ep, AssetManager.textures.get(ep.getID()));
	}
	
	private <T> BuilderButton<T> createButton(Prefab<T> prefab, Image image) {
		BuilderButton<T> btn = new BuilderButton<>(prefab, image);
		btn.setOnAction(new ClickListener(prefab));
		return btn;
	}
	
	public void setController(MapEditorController controller) {
		editor = controller;
	}
	
	public void addEntity(EntityPrefab prefab, Image image) {
		entityPane.add(createButton(prefab, image));
	}
	
	public void addPlayer(EntityPrefab prefab, Image image) {
		playerPane.add(createButton(prefab, image));
	}
	
	public void addTile(TilePrefab prefab, Image image) {
		tilePane.add(createButton(prefab, image));
	}
	
	class ClickListener implements EventHandler<ActionEvent> {

		Prefab<?> prefab;
		
		public ClickListener(Prefab<?> prefab) {
			this.prefab = prefab;
		}
		
		@Override
		public void handle(ActionEvent event) {
			if(prefab instanceof TilePrefab)
				editor.setToBePlaced((TilePrefab)prefab);
			else
				editor.setToBePlaced((EntityPrefab)prefab);		
		}
	}
}