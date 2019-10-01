package controller;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.PresetTile;
import data.mapdata.Tile;
import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.Prefab;
import data.mapdata.prefabs.TilePrefab;
import gui.ErrorHandler;
import helpers.Utils;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class MapEditorController extends MapController {
	private Prefab<?> toBePlaced;
	
	public File currentFile = null;
	private FileChooser mapChooser;
	
	protected PropertyEditorController propeditor;
	private Entity currentlyEdited;
	private EntityMenu entityMenu;
	@Override
	public void initialize() {
		super.initialize();
		entityMenu = new EntityMenu();
	}
	
	public MapEditorController() {
		mapChooser = new FileChooser();
		mapChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Map files (*.map)", "*.map"));
	}
	
	
	public void setPropertyEditor(PropertyEditorController editor) {
		propeditor = editor;
		propeditor.setController(this);
	}
    
    private boolean showProperties(Entity entity) {
    	if(entity != null && currentlyEdited != entity) {
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
    	if(showProperties(entity))
    		propeditor.setEditing(true);
    }
    
    private boolean hideProperties() {
    	if(currentlyEdited != null) {
    		if(propeditor.requestClear()) {
    			currentlyEdited = null;
    			return true;
    		} else
    			return false;
    	}
    	return true;
    }

    @Override
    protected void handleClick(Point p, MouseEvent event) {
    	System.out.println(" mouse clicked [touch=" + event.isSynthesized() + "; x=" + event.getX() + ", y=" + event.getY() + "]" + event.isControlDown());
    	if(entityMenu.isShowing())
    		entityMenu.hide();
    	if(event.getButton() == MouseButton.PRIMARY) {
    		if(event.isControlDown()) {
    			currentMap.setTile(p.x, p.y, new Tile(PresetTile.EMPTY));
    		} else {
    			//check if the clicked tile contains an entity
    			//if it does: set the editorpane to the entity
    			//if it doesn't: place an instance of toBePlaced on this tile.
    			Entity e;
    			if((e=currentMap.getEntity(p))!=null)
    				showProperties(e);
    			else if(toBePlaced != null) {
		    		if(toBePlaced instanceof TilePrefab) {
		    			currentMap.setTile(p.x, p.y, ((TilePrefab) toBePlaced).getInstance(p.x, p.y));
		    		} else if(toBePlaced instanceof EntityPrefab) {
		    			Entity entity;
		    			currentMap.addEntity(entity=((EntityPrefab)toBePlaced).getInstance(p.x, p.y));
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
	void saveMap(){
		if(currentFile != null)
			try {
				Utils.saveMap(currentFile, currentMap);
			} catch(IOException e) {
				ErrorHandler.handle("Map could not be saved!", e);				
			}
		else
			saveAsMap();
	}

	@FXML
	void saveAsMap() {
		mapChooser.setTitle("Save map");
		currentFile = mapChooser.showSaveDialog(SceneManager.getPrimaryStage());
		try {
			Utils.saveMap(currentFile, currentMap);
		} catch(IOException e) {
			ErrorHandler.handle("Map could not be saved!", e);
		}
	}

	@FXML
	void loadMap() throws IOException {
		mapChooser.setTitle("Load map");
		currentFile = mapChooser.showOpenDialog(SceneManager.getPrimaryStage());
		Map map = Utils.loadMap(currentFile);
		if(map != null)
			setMap(map);
		redraw();
	}
	
	class EntityMenu extends ContextMenu {
		Entity selected;
		Point selectedTile;
		Entity copied;
		
		MenuItem edit, cut, copy, paste, delete, save;
		
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
					currentMap.removeEntity(selected);
				}
			});
			copy.setOnAction(event -> copied = selected);
			paste.setOnAction(event -> {
				Entity e = copied.copy();
				e.setLocation(selectedTile);
				currentMap.addEntity(e);
				showProperties(e);
			});
			delete.setOnAction(event -> {
				if(hideProperties())
					currentMap.removeEntity(selected);
			});
			save.setOnAction(event -> {});
			setOnHidden(event -> redraw());
		}
		
		public void show(Entity e, double screenX, double screenY) {
			selected = e;
			showProperties(selected);
			getItems().clear();
			getItems().addAll(edit, cut, copy, delete, save);
			show(canvas, screenX, screenY);
		}
		
		public void show(double canvasX, double canvasY, double screenX, double screenY) {
			selectedTile = getTileOnPosition(canvasX, canvasY);
			Entity e = currentMap.getEntity(selectedTile);
			if(e!= null) {
				show(e, screenX, screenY);
			} else {
				getItems().clear();
				getItems().add(paste);
				show(canvas, screenX, screenY);
			}
		}		
	}
}