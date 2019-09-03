package controller;

import java.awt.Point;

import data.mapdata.Entity;
import data.mapdata.Tile;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class MapEditorController extends MapController {
	private Object toBePlaced; //can be tile/entity/player
	private enum Type { TILE, ENTITY}
	private Type currentType = null;
	
    private void placeTile(Point loc) {
    	
    }
    
    private void editProperties(Point loc) {
    	Entity entity = currentMap.getEntity(loc);
    	if(entity == null) return;
    	else {
    		
    	}    	
    }

    @FXML
    void onMouseClicked(MouseEvent event) {
    	if(mousePressedCoords.distance(event.getX(), event.getY())>TILE_SIZE*SCALE/2) return;
    	System.out.println("mouse clicked [touch=" + event.isSynthesized() + "; x=" + event.getX() + ", y=" + event.getY() + "]");
    	if(event.isPrimaryButtonDown())
    		placeTile(getTileOnPosition(event.getX(), event.getY()));
    	else
    		editProperties(getTileOnPosition(event.getX(), event.getY()));
    }
	
	public void setToBePlaced(Tile t) {
		toBePlaced = t;
		currentType = Type.TILE;
	}
	
	public void setToBePlaced(Entity e) {
		toBePlaced = e;
		currentType = Type.ENTITY;
	}
}
