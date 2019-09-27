package controller;

import java.awt.Point;
import java.awt.Rectangle;

import data.mapdata.Entity;
import data.mapdata.PresetTile;
import data.mapdata.Tile;
import data.mapdata.prefabs.EntityPrefab;
import data.mapdata.prefabs.Prefab;
import data.mapdata.prefabs.TilePrefab;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class MapEditorController extends MapController {
	private Prefab<?> toBePlaced;
	
	protected PropertyEditorController propeditor;
	private Entity currentlyEdited;
	
	
	public void setPropertyEditor(PropertyEditorController editor) {
		propeditor = editor;
	}
    
    private void editProperties(Point loc) {
    	Entity entity = currentMap.getEntity(loc);
    	if(entity != null && currentlyEdited != entity) {
    		if(propeditor.requestCancelEditing()) {
	    		propeditor.setProperties(entity.getProperties());
	    		currentlyEdited = entity;
    		}
    		redraw();
    	}
    }

    @FXML
    void onMouseClicked(MouseEvent event) {
    	if(mousePressedCoords != null && mousePressedCoords.distance(event.getX(), event.getY())>TILE_SIZE*SCALE/2) return;
    	handleClick(getTileOnPosition(event.getX(), event.getY()), event);
    }

    public void handleClick(Point p, MouseEvent event) {
    	System.out.println(" mouse clicked [touch=" + event.isSynthesized() + "; x=" + event.getX() + ", y=" + event.getY() + "]" + event.isControlDown());
    	if(event.getButton() == MouseButton.PRIMARY) {
    		if(event.isControlDown()) {
    			Entity e;
    			if((e=currentMap.getEntity(p))!=null) {
    				currentMap.removeEntity(e);
    			} else {
    				currentMap.setTile(p.x, p.y, new Tile(PresetTile.EMPTY));
    			}
    		} else
	    		if(toBePlaced != null) {
		    		if(toBePlaced instanceof TilePrefab) {
		    			currentMap.setTile(p.x, p.y, ((TilePrefab) toBePlaced).getInstance(p.x, p.y));
		    		} else if(toBePlaced instanceof EntityPrefab) {
		    			Entity e;
		    			if((e=currentMap.getEntity(p))!=null) {
		    				currentMap.removeEntity(e);
		    			}
		    			currentMap.addEntity(((EntityPrefab)toBePlaced).getInstance(p.x, p.y));
		    			if(this instanceof ServerController)
		    				toBePlaced = null;
		    		}
	    		}
    		redraw();
    	} else {
    		editProperties(getTileOnPosition(event.getX(), event.getY()));
    		
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
}