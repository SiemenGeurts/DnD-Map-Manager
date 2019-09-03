package controller;

import java.util.ArrayList;

import data.mapdata.Entity;
import data.mapdata.PresetTile;
import data.mapdata.Tile;
import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.TilePrefab;
import gui.BuilderButton;
import gui.GridSelectionPane;
import helpers.AssetManager;
import helpers.JSONManager;
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
	GridSelectionPane tilePane, entityPane, playerPane;
	
	MapEditorController editor;
	
	@FXML
	void initialize() {
		tilePane = new GridSelectionPane(5);
		tilePane.add(new BuilderButton<Tile>(new TilePrefab(PresetTile.EMPTY), AssetManager.textures.get(PresetTile.EMPTY)));
		tilePane.add(new BuilderButton<Tile>(new TilePrefab(PresetTile.FLOOR), AssetManager.textures.get(PresetTile.FLOOR)));
		tilePane.add(new BuilderButton<Tile>(new TilePrefab(PresetTile.WALL), AssetManager.textures.get(PresetTile.WALL)));
		tilePane.add(new BuilderButton<Tile>(new TilePrefab(PresetTile.BUSHES), AssetManager.textures.get(PresetTile.BUSHES)));
		tileScrollPane.setContent(tilePane);
		entityPane = new GridSelectionPane(5);
		entityScrollPane.setContent(entityPane);
		playerPane = new GridSelectionPane(5);
		playerScrollPane.setContent(playerPane);
		ArrayList<TilePrefab> tiles = JSONManager.getTiles();
		if(tiles != null)
			for(TilePrefab tp : tiles)
				tilePane.add(new BuilderButton<Tile>(tp, AssetManager.textures.get(tp.getID())));
		ArrayList<EntityPrefab> entities = JSONManager.getEntities();
		if(entities != null)
			for(EntityPrefab ep : entities)
				entityPane.add(new BuilderButton<Entity>(ep, AssetManager.textures.get(ep.getID())));
		entities = JSONManager.getPlayers();
		if(entities != null)
			for(EntityPrefab ep : entities)
				playerPane.add(new BuilderButton<Entity>(ep, AssetManager.textures.get(ep.getID())));
	}
	
	public void setController(MapEditorController controller) {
		editor = controller;
	}
	
	public void addEntity(EntityPrefab prefab, Image image) {
		BuilderButton<Entity> ebtn = new BuilderButton<>((EntityPrefab) prefab, image);
		entityPane.add(ebtn);
	}
	
	public void addPlayer(EntityPrefab prefab, Image image) {
		BuilderButton<Entity> pbtn = new BuilderButton<>((EntityPrefab) prefab, image);
		playerPane.add(pbtn);
	}
	
	public void addTile(TilePrefab prefab, Image image) {
		BuilderButton<Tile> tbtn = new BuilderButton<>((TilePrefab) prefab, image);
		tilePane.add(tbtn);
	}
	
}
