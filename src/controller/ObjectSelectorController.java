package controller;

import java.util.ArrayList;

import data.mapdata.Entity;
import data.mapdata.PresetTile;
import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.Prefab;
import data.mapdata.prefabs.TilePrefab;
import gui.BuilderButton;
import gui.GridSelectionPane;
import helpers.AssetManager;
import helpers.JSONManager;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

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
	
	ButtonMenu menu = new ButtonMenu();
	
	@FXML
	void initialize() {
		tilePane = new GridSelectionPane(5);
		tilePane.setNames(false);
		//tilePane.add(createButton(new TilePrefab(PresetTile.EMPTY), AssetManager.textures.get(PresetTile.EMPTY)));
		tilePane.add(createButton(new TilePrefab(PresetTile.FLOOR), AssetManager.getTexture(PresetTile.FLOOR)));
		tilePane.add(createButton(new TilePrefab(PresetTile.WALL), AssetManager.getTexture(PresetTile.WALL)));
		tilePane.add(createButton(new TilePrefab(PresetTile.BUSHES), AssetManager.getTexture(PresetTile.BUSHES)));
		tilePane.add(createButton(new TilePrefab(PresetTile.FIRE), AssetManager.getTexture(PresetTile.FIRE)));
		tileScrollPane.setContent(tilePane);
		entityPane = new GridSelectionPane(5);
		entityScrollPane.setContent(entityPane);
		playerPane = new GridSelectionPane(5);
		playerScrollPane.setContent(playerPane);
		ArrayList<TilePrefab> tiles = JSONManager.getTiles();
		if(tiles != null)
			for(TilePrefab tp : tiles)
				tilePane.add(createButton(tp, AssetManager.getTexture(tp.getType())));
		ArrayList<EntityPrefab> entities = JSONManager.getEntities();
		if(entities != null)
			for(EntityPrefab ep : entities)
				entityPane.add(createButton(ep, AssetManager.getTexture(ep.getType())), ep.getName());
		entities = JSONManager.getPlayers();
		if(entities != null)
			for(EntityPrefab ep : entities)
				playerPane.add(createButton(ep, AssetManager.getTexture(ep.getType())), ep.getName());
	}
	
	private <T> BuilderButton<T> createButton(Prefab<T> prefab, Image image) {
		BuilderButton<T> btn = new BuilderButton<>(prefab, image);
		btn.setOnMouseClicked(new ClickListener());
		return btn;
	}
	
	public void setController(MapEditorController controller) {
		editor = controller;
	}
	
	public void addEntity(EntityPrefab prefab, Image image) {
		entityPane.add(createButton(prefab, image), prefab.getName());
	}
	
	public void addPlayer(EntityPrefab prefab, Image image) {
		playerPane.add(createButton(prefab, image), prefab.getName());
	}
	
	public void addTile(TilePrefab prefab, Image image) {
		tilePane.add(createButton(prefab, image));
	}
	
	class ClickListener implements EventHandler<MouseEvent> {	
		@Override
		public void handle(MouseEvent event) {
			if(menu.isShowing())
				menu.hide();
			if(event.getButton() == MouseButton.SECONDARY) {
				BuilderButton<?> btn = (BuilderButton<?>) event.getSource();
				menu.show(btn.getPrefab(), btn, event.getX(), event.getY());
			} else {
				Prefab<?> prefab = ((BuilderButton<?>) event.getSource()).getPrefab();
				if(prefab instanceof TilePrefab)
					editor.setToBePlaced((TilePrefab)prefab);
				else
					editor.setToBePlaced((EntityPrefab)prefab);
			}
		}
	}
	
	class ButtonMenu extends ContextMenu {
		EntityPrefab selectedEntity;
		TilePrefab selectedTile;
		MenuItem edit, copy,delete;
		Entity prefabInstance;
		final Runnable onSave = () -> save();
		BuilderButton<Entity> btnClicked;
		
		public ButtonMenu() {
			edit = new MenuItem("Edit");
			copy = new MenuItem("Copy");
			delete = new MenuItem("Delete");
			
			edit.setOnAction(event -> editor.editProperties(prefabInstance, onSave));
			delete.setOnAction(event -> {
				if(editor.hideProperties()) {
					if(prefabInstance == null) {
						JSONManager.removeTile(selectedTile.getType());
						tilePane.remove(btnClicked);
					} else if(prefabInstance.isNPC()) {
						JSONManager.removeEntity(prefabInstance.getType());
						entityPane.remove(btnClicked);
					} else {
						JSONManager.removePlayer(prefabInstance.getType());
						playerPane.remove(btnClicked);
					}
					AssetManager.getLibrary().removeTexture(prefabInstance.getType());
				}
			});
			
			//copy.setOnAction(value);
			
		}
		
		public void save() {
			EntityPrefab prefab = EntityPrefab.fromEntity(prefabInstance);
			btnClicked.setPrefab(prefab);
			if(prefab.isPlayer) {
				JSONManager.addPlayer(prefab);
				playerPane.updateName(btnClicked, prefab.getName());
			} else {
				JSONManager.addEntity(prefab);
				entityPane.updateName(btnClicked, prefab.getName());
			}
		}
		
		@SuppressWarnings("unchecked")
		public void show(Prefab<?> e, BuilderButton<?> btn, double x, double y) {
			if(e instanceof EntityPrefab) {
				selectedEntity = (EntityPrefab) e;
				selectedTile = null;				
				btnClicked = (BuilderButton<Entity>) btn;
				getItems().clear();
				getItems().addAll(edit, copy, delete);
				if(!editor.showProperties(prefabInstance = selectedEntity.getInstance(0, 0))) return;
			} else {
				selectedEntity = null;
				selectedTile = (TilePrefab) e;
				btnClicked = null;
				getItems().clear();
				getItems().addAll(delete);
			}
			Point2D coords = btn.localToScreen(x, y);
			show(btn, coords.getX(), coords.getY());
		}
	}
}