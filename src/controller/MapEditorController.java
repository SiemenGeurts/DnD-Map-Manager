package controller;

import java.awt.Point;

import data.mapdata.Entity;
import data.mapdata.PresetTile;
import data.mapdata.Tile;
import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.Prefab;
import data.mapdata.prefabs.TilePrefab;
import javafx.fxml.FXML;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class MapEditorController extends MapController {
	private Prefab<?> toBePlaced;
	
	protected PropertyEditorController propeditor;
	
	public void setPropertyEditor(PropertyEditorController editor) {
		propeditor = editor;
	}
    
    private void editProperties(Point loc) {
    	Entity entity = currentMap.getEntity(loc);
    	if(entity != null) {
    		propeditor.setProperties(entity.getProperties());
    	}
    }

    @FXML
    void onMouseClicked(MouseEvent event) {
    	System.out.println("Mouse clicked " +mousePressedCoords);
    	if(mousePressedCoords != null && mousePressedCoords.distance(event.getX(), event.getY())>TILE_SIZE*SCALE/2) return;
    	handleClick(getTileOnPosition(event.getX(), event.getY()), event);
    }

    public void handleClick(Point p, MouseEvent event) {
    	System.out.println(" mouse clicked [touch=" + event.isSynthesized() + "; x=" + event.getX() + ", y=" + event.getY() + "]");
    	if(event.getButton() == MouseButton.PRIMARY) {
    		if(event.isControlDown()) {
    			Entity e;
    			if((e=currentMap.getEntity(p))!=null)
    				currentMap.getEntities().remove(e);
    			else
    				currentMap.setTile(p.x, p.y, new Tile(PresetTile.EMPTY));
    		}
    		if(toBePlaced instanceof TilePrefab) {
    			currentMap.setTile(p.x, p.y, ((TilePrefab) toBePlaced).getInstance(p.x, p.y));
    		} else if(toBePlaced instanceof EntityPrefab) {
    			Entity e;
    			if((e=currentMap.getEntity(p))!=null)
    				currentMap.getEntities().remove(e);
    			currentMap.getEntities().add(((EntityPrefab)toBePlaced).getInstance(p.x, p.y));
    		}
    		drawBackground();
    		drawMap();
    	} else
    		editProperties(getTileOnPosition(event.getX(), event.getY()));
    }

	public void setToBePlaced(TilePrefab t) {
		toBePlaced = t;
	}

	public void setToBePlaced(EntityPrefab e) {
		toBePlaced = e;
	}
}