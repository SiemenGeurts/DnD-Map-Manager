package controller;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import app.ServerGameHandler;
import data.mapdata.Entity;
import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.Prefab;
import data.mapdata.prefabs.TilePrefab;
import gui.ErrorHandler;
import helpers.AssetManager;
import helpers.JSONManager;
import helpers.Utils;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
	
	ObjectSelectorController osController;
	PropertyEditorController propertyEditorController;
	
	TextureType current;
	
	public void initialize() {
		try {
			FXMLLoader loader = new FXMLLoader(ServerGameHandler.class.getResource("/assets/fxml/PropertyEditor.fxml"));
			propertyEditor = (AnchorPane) new Scene(loader.load()).getRoot();
			propertyEditor.setVisible(false);
			propertyEditorController = loader.getController();
			vbox.getChildren().add(3, propertyEditor);
			VBox.setVgrow(propertyEditor, Priority.ALWAYS);
		} catch (IOException e) {
			ErrorHandler.handle("Could not load PropertyEditor", e);
		}
	}
	
	@FXML
	public void addBtnClicked(ActionEvent e) {
		//1. save the image to the images directory.
		int id = AssetManager.addTexture(imgView.getImage());
		//2. define a new tile/entity/player prefab.
		//3. add it to the accordion selector.
		Prefab<?> prefab;
		switch(current) {
			case ENTITY:
				prefab = new EntityPrefab(id, propertyEditorController.getWidth(), propertyEditorController.getHeight(), propertyEditorController.getPropertyList(), propertyEditorController.getBloodied(), false, propertyEditorController.getDescription());
				((EntityPrefab)prefab).setName(propertyEditorController.getName());
				osController.addEntity((EntityPrefab) prefab, imgView.getImage());
				JSONManager.addEntity((EntityPrefab) prefab);
				break;
			case PLAYER:
				prefab = new EntityPrefab(id, propertyEditorController.getWidth(), propertyEditorController.getHeight(), propertyEditorController.getPropertyList(), propertyEditorController.getBloodied(), true, propertyEditorController.getDescription());
				((EntityPrefab)prefab).setName(propertyEditorController.getName());
				osController.addPlayer((EntityPrefab) prefab, imgView.getImage());
				JSONManager.addPlayer((EntityPrefab) prefab);
				break;
			case TILE:
				prefab = new TilePrefab(id);
				osController.addTile((TilePrefab) prefab, imgView.getImage());
				JSONManager.addTile((TilePrefab) prefab);
				break;
		}
	}
	
	public void setSelector(ObjectSelectorController controller) {
		osController = controller;
	}
	
	public static Image getTexture() throws IOException {
		File file = Utils.openImageDialog();
		if(file != null) {
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
				Entity temp = new Entity(0, 0,0,1,1, false);
				temp.setProperties(Entity.getDefaultProperties());
				propertyEditorController.setEntity(temp);
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
				Entity temp = new Entity(0, 0,0,1,1, false);
				temp.setProperties(Entity.getDefaultProperties());
				propertyEditorController.setEntity(temp);
				propertyEditor.setVisible(true);
			}
		} catch (IOException ex) {
			ErrorHandler.handle("Could not read image file", ex);
		}
	}

}
