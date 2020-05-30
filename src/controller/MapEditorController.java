package controller;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.PresetTile;
import data.mapdata.Tile;
import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.Prefab;
import data.mapdata.prefabs.TilePrefab;
import gui.Dialogs;
import gui.ErrorHandler;
import helpers.Logger;
import helpers.Utils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class MapEditorController extends MapController {
	private Prefab<?> toBePlaced;
	
	@FXML
	protected RadioMenuItem rbFowShow1, rbFowShow2, rbFowShow3, rbFowHide;
	protected ToggleGroup FowGroup = new ToggleGroup();
	
	public File currentFile = null;
	protected FileChooser mapChooser;
	
	protected PropertyEditorController propeditor;
	protected PaintPaneController paintController;
	private Entity currentlyEdited;
	protected EntityMenu entityMenu;
	public boolean isSaved;
	Runnable onPropertySave = () -> getMap().setUnsaved();
	
	@Override
	public void initialize() {
		super.initialize();
		entityMenu = new EntityMenu();
		rbFowShow1.setToggleGroup(FowGroup);
		rbFowShow2.setToggleGroup(FowGroup);
		rbFowShow3.setToggleGroup(FowGroup);
		rbFowHide.setToggleGroup(FowGroup);
		rbFowShow1.selectedProperty().addListener((obs, oldVal, newVal) -> {if(newVal) setFoWOpacity(1);});
		rbFowShow2.selectedProperty().addListener((obs, oldVal, newVal) -> {if(newVal) setFoWOpacity(0.5);});
		rbFowShow3.selectedProperty().addListener((obs, oldVal, newVal) -> {if(newVal) setFoWOpacity(0.25);});
		rbFowHide.selectedProperty().addListener((obs, oldVal, newVal) -> {if(newVal) setFoWOpacity(0);});
	}
	
	public MapEditorController() {
		mapChooser = new FileChooser();
		mapChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Map files (*.map)", "*.map"));
	}
	
	public void setPropertyEditor(PropertyEditorController editor) {
		propeditor = editor;
	}
    
	/**
	 * shows the properties of the given Entity.
	 * The method will first check if the user is currently editing another entity and if so,
	 * if the user wants to cancel editing.
	 * @param entity the entities whose properties are to be shown
	 * @return true if the properties are shown, false is that was not possible because the user was still editing another entity.
	 */
    public boolean showProperties(Entity entity) {
    	if(entity != null) {
    		if(currentlyEdited == entity)
    			return true;
    		if(propeditor.requestCancelEditing()) {
	    		propeditor.setEntity(entity);
	    		currentlyEdited = entity;
	    		redraw();
	    		return true;
    		}
    	}
    	return false;
    }
    
    private void editProperties(Entity entity) {
    	editProperties(entity, onPropertySave);
    }
    
    public void editProperties(Entity entity, Runnable onSave) {
    	if(showProperties(entity)) {
    		propeditor.setOnSavedAction(onSave);
    		propeditor.setEditing(true);
    	}
    }
    
    /**
     * Hides the properties currently displayed.
     * If the user is currently editing those properties, he/she will be asked if he/she wants to cancel editing.
     * @return true if the properties were hidden, false if the user canceled the operation
     */
    public boolean hideProperties() {
    	if(currentlyEdited != null) {
    		if(propeditor.requestClear()) {
    			currentlyEdited = null;
    			return true;
    		} else
    			return false;
    	}
    	return true;
    }
    
    protected Entity getEditedEntity() {
    	return currentlyEdited;
    }

    @Override
    protected void handleClick(Point p, MouseEvent event) {
    	Logger.println(" mouse clicked [touch=" + event.isSynthesized() + "; x=" + event.getX() + ", y=" + event.getY() + "]" + event.isControlDown());
    	if(p.x < 0 || p.y < 0 || p.x >= getMap().getWidth() || p.y >= getMap().getHeight())
    		return;
    	if(entityMenu.isShowing())
    		entityMenu.hide();
    	if(event.getButton() == MouseButton.PRIMARY) {
    		if(event.isControlDown()) {
    			getMap().setTile(p.x, p.y, new Tile(PresetTile.EMPTY));
    		} else {
    			//check if the clicked tile contains an entity
    			//if it does: set the editorpane to the entity
    			//if it doesn't: place an instance of toBePlaced on this tile.
    			Entity e;
    			if((e=getMap().getEntity(p))!=null)
    				showProperties(e);
    			else if(toBePlaced != null) {
		    		if(toBePlaced instanceof TilePrefab) {
		    			getMap().setTile(p.x, p.y, ((TilePrefab) toBePlaced).getInstance(p.x, p.y));
		    		} else if(toBePlaced instanceof EntityPrefab) {
		    			Entity entity;
		    			getMap().addEntity(entity=((EntityPrefab)toBePlaced).getInstance(p.x, p.y));
		    			showProperties(entity);
		    			if(this instanceof ServerController)
		    				toBePlaced = null;
		    		}
	    		}
    		}
    		redraw();
    	} else {
    		entityMenu.show(event.getX(), event.getY(), event.getScreenX(), event.getScreenY());
    	}
    }
    
    Point2D lastDragPos = null;
    @Override
    public void handleDrag(Point2D old, Point2D cur, MouseEvent e) {
    	if(lastDragPos != null && cur.distance(lastDragPos)<0.5) return;
    	lastDragPos = cur;
    	TilePrefab prefab = null;
    	boolean erasing = e.isControlDown();
    	boolean editingMask = paintController.isEditingMask();
    	if(erasing)
    		prefab = new TilePrefab(PresetTile.EMPTY);
    	else if(toBePlaced instanceof TilePrefab)
    		prefab = (TilePrefab) toBePlaced;
    	else if(!erasing && !editingMask)
    		return;
    	int width = paintController.getSize();
    	byte opacity = erasing ? 0 : (byte)(Math.round(paintController.getOpacity()*64));
    	Point center = new Point((int)cur.getX(), (int)cur.getY());
    	if(width==1) {
    		if(editingMask)
    			getMap().setMask(center.x, center.y, opacity);
    		else
    			getMap().setTile(center.x, center.y, prefab.getInstance(0, 0));
    	}
    	//float size = width/2f;
    	//float top = center.y+size;
    	//float bottom = center.y-size;
    	//for(int i = (int)top; i < bottom; i--) {
    		
    	//}
    	redraw();
    }
    
    @Override
    public void drawMap(int minX, int minY, int maxX, int maxY) {
    	super.drawMap(minX, minY, maxX, maxY);
    	if(currentlyEdited!=null) {
    		gc.setStroke(Color.LIGHTBLUE);
    		gc.setLineWidth(5);
    		Rectangle rect = worldToScreen(new Rectangle2D(currentlyEdited.getX()-.05, currentlyEdited.getY()-.05, currentlyEdited.getWidth()+.1, currentlyEdited.getHeight()+.1));
    		gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());    		
    	}
    }

	public void setToBePlaced(TilePrefab t) {
		toBePlaced = t;
	}

	public void setToBePlaced(EntityPrefab e) {
		toBePlaced = e;
	}
	
	@FXML
	void onSave(){
		if(currentFile != null)
			try {
				Utils.saveMap(currentFile, getMap());
			} catch(IOException e) {
				ErrorHandler.handle("Map could not be saved!", e);				
			}
		else
			onSaveAs();
	}

	@FXML
	void onSaveAs() {
		mapChooser.setTitle("Save map");
		currentFile = mapChooser.showSaveDialog(SceneManager.getPrimaryStage());
		if(currentFile == null) {
			Dialogs.warning("Map was not saved.", true);
		if(!currentFile.getName().endsWith(".map"))
			currentFile = new File(currentFile.getAbsolutePath() + ".map");
		} else
			onSave();
	}

	@FXML
	void onOpen() throws IOException {
		if(!checkSaved()) return;
		mapChooser.setTitle("Load map");
		currentFile = mapChooser.showOpenDialog(SceneManager.getPrimaryStage());
		if(currentFile == null) return;
		Map map = Utils.loadMap(currentFile);
		if(map != null)
			setMap(map);
		redraw();
	}
	
	@FXML
	void onQuit() {
		if(checkSaved())
			MainMenuController.sceneManager.popScene();
	}
	
	/**
	 * Checks if the current library and map are saved.
	 * @return true if they are both saved, or the user chose not to save them, false if the user canceled the save (meaning the closing should be canceled as well).
	 */
	protected boolean checkSaved() {
		if(getMap() != null && !getMap().isSaved()) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText("It appears that the current map is not saved. Do you want to save?");
			alert.setContentText("If not saved, all changes will be lost.");
			ButtonType btnYes = new ButtonType("Yes", ButtonData.YES);
			ButtonType btnNo = new ButtonType("No", ButtonData.NO);
			ButtonType btnCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
			alert.getButtonTypes().setAll(btnYes, btnNo, btnCancel);
			Optional<ButtonType> result = alert.showAndWait();
			if(result.orElse(btnCancel) == btnCancel)
				return false;
			else if(result.get() == btnYes)
				onSave();
		}
		return true;
	}
	
	class EntityMenu extends ContextMenu {
		Entity selected;
		Point selectedTile;
		Entity copied;
		
		MenuItem edit, cut, copy, paste, delete, save;
		
		ArrayList<MenuItem> entityMenuItems = new ArrayList<>();
		
		public EntityMenu() {
			edit = new MenuItem("Edit");
			cut = new MenuItem("Cut");
			copy = new MenuItem("Copy");
			paste = new MenuItem("Paste");
			delete = new MenuItem("Delete");
			save = new MenuItem("Save");
			edit.setOnAction(event -> editProperties(selected));
			cut.setOnAction(event -> {
				if(hideProperties()) {
					copied = selected;
					getMap().removeEntity(selected);
				}
			});
			copy.setOnAction(event -> copied = selected);
			paste.setOnAction(event -> {
				Entity e = copied.copy();
				e.setLocation(selectedTile);
				getMap().addEntity(e);
				showProperties(e);
			});
			delete.setOnAction(event -> {
				if(hideProperties())
					getMap().removeEntity(selected);
			});
			save.setOnAction(event -> {});
			setOnHidden(event -> redraw());
			entityMenuItems.add(edit);
			entityMenuItems.add(cut);
			entityMenuItems.add(copy);
			entityMenuItems.add(delete);
			entityMenuItems.add(save);
		}
		
		public void addEntityMenuItem(MenuItem item) {
			entityMenuItems.add(item);
		}
		
		public void show(Entity e, double screenX, double screenY) {
			selected = e;
			showProperties(selected);
			getItems().clear();
			getItems().addAll(entityMenuItems);
			show(canvas, screenX, screenY);
		}
		
		public void show(double canvasX, double canvasY, double screenX, double screenY) {
			selectedTile = getTileOnPosition(canvasX, canvasY);
			Entity e = getMap().getEntity(selectedTile);
			if(e!= null) {
				show(e, screenX, screenY);
			} else {
				getItems().clear();
				if(copied != null) {
					getItems().add(paste);
					show(canvas, screenX, screenY);
				}
			}
		}		
	}
}