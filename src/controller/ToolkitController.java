package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import app.ServerGameHandler;
import data.mapdata.Entity;
import data.mapdata.PresetTile;
import data.mapdata.Tile;
import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.Prefab;
import data.mapdata.prefabs.TilePrefab;
import gui.BuilderButton;
import gui.ErrorHandler;
import gui.GridSelectionPane;
import helpers.AssetManager;
import helpers.JSONManager;
import javafx.css.PseudoClass;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class ToolkitController {
	
	enum TextureType {
		TILE, PLAYER, ENTITY;
	}
	@FXML
	ImageView imgView;
	@FXML
	Button btnAdd;
	@FXML
	VBox vbox;
	@FXML
	AnchorPane propertyEditor;
	@FXML
	TextField nameField;
	@FXML
	ScrollPane entityScrollPane;
	@FXML
	ScrollPane tileScrollPane;
	@FXML
	ScrollPane playerScrollPane;
	@FXML
	GridSelectionPane tilePane, entityPane, playerPane;
	
	PropertyEditorController propertyEditorController;
	
	private static final PseudoClass ERROR_BORDER = PseudoClass.getPseudoClass("invalid");
	
	TextureType current;
	static FileChooser fc = new FileChooser();
	
	public void initialize() {
		List<FileChooser.ExtensionFilter> extensionFilters = fc.getExtensionFilters();
		extensionFilters.add(new FileChooser.ExtensionFilter("Image files (*.png)", "*.png"));
		try {
			FXMLLoader loader = new FXMLLoader(ServerGameHandler.class.getResource("../assets/fxml/PropertyEditor.fxml"));
			propertyEditor = (AnchorPane) new Scene(loader.load()).getRoot();
			propertyEditor.setVisible(false);
			propertyEditorController = loader.getController();
			vbox.getChildren().add(3, propertyEditor);		
			JSONManager.initialize();
		} catch (IOException e) {
			ErrorHandler.handle("Could not load PropertyEditor", e);
		}
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
	
	@FXML
	public void addBtnClicked(ActionEvent e) {
		if(nameField.getText().equals("")) {
			nameField.pseudoClassStateChanged(ERROR_BORDER, true);
			return;
		}
		nameField.pseudoClassStateChanged(ERROR_BORDER, false);
		try {
			//1. save the image to the images directory.
			int id = AssetManager.addTexture(imgView.getImage());
			//2. define a new tile/entity/player prefab.
			//3. add it to the accordion selector.
			Prefab<?> prefab;
			switch(current) {
				case ENTITY:
					prefab = new EntityPrefab(id, propertyEditorController.getWidth(), propertyEditorController.getHeight(), propertyEditorController.getPropertyList(), propertyEditorController.getBloodied());
					BuilderButton<Entity> ebtn = new BuilderButton<>((EntityPrefab) prefab, imgView.getImage());
					JSONManager.addEntity((EntityPrefab) prefab);
					entityPane.add(ebtn);
					break;
				case PLAYER:
					prefab = new EntityPrefab(id, 1, 1, null, false);
					BuilderButton<Entity> pbtn = new BuilderButton<>((EntityPrefab) prefab, imgView.getImage());
					JSONManager.addPlayer((EntityPrefab) prefab);
					playerPane.add(pbtn);
					break;
				case TILE:
					prefab = new TilePrefab(id);
					BuilderButton<Tile> tbtn = new BuilderButton<>((TilePrefab) prefab, imgView.getImage());
					JSONManager.addTile((TilePrefab) prefab);
					tilePane.add(tbtn);
					break;
			}
			
		} catch (IOException e1) {
			ErrorHandler.handle("Could not add texture.", e1);
		}
	}
	
	public Image getTexture() throws IOException {
		File file = fc.showOpenDialog(SceneManager.getPrimaryStage());
		if(file != null) {
			fc.setInitialDirectory(new File(file.getParent()));
			return SwingFXUtils.toFXImage(ImageIO.read(file), null);
		} else
			return null;
	}
	
	@FXML
	public void addTile(ActionEvent e) {
		try {
			Image image = getTexture();
			if(image != null) {
				imgView.setImage(image);
				current = TextureType.TILE;
				btnAdd.setDisable(false);
				propertyEditor.setVisible(false);
			}
		} catch (IOException ex) {
			ErrorHandler.handle("Could not read image file", ex);
		}
	}
	
	@FXML
	public void addEntity(ActionEvent e) {
		try {
			Image image = getTexture();
			if(image != null) {
				imgView.setImage(image);
				current = TextureType.ENTITY;
				btnAdd.setDisable(false);
				propertyEditorController.setProperties(Entity.getDefaultProperties());
				propertyEditor.setVisible(true);
			}
		} catch (IOException ex) {
			ErrorHandler.handle("Could not read image file", ex);
		}
	}
	
	@FXML
	public void addPlayer(ActionEvent e) {
		try {
			Image image = getTexture(); 
			if(image != null) {
				imgView.setImage(image);
				current = TextureType.PLAYER;
				btnAdd.setDisable(false);
				propertyEditor.setVisible(false);
			}
		} catch (IOException ex) {
			ErrorHandler.handle("Could not read image file", ex);
		}
	}

}
