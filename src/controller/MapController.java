package controller;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;

import controller.InitClassStructure.SceneController;
import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.Tile;
import helpers.Calculator;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

public class MapController extends SceneController {

	protected int TILE_SIZE = 50;
	protected double SCALE = 1;
	//private int OFFSET_SPEED = TILE_SIZE + 10;
	private double SCALING_FACTOR = 1.3;
	private double offsetX, offsetY;
	public Map currentMap;
	
	private Point2D lastDragCoords;
	protected Point2D mousePressedCoords;
	private double oldZoom = 1;
	private boolean zooming = false;
	private GraphicsContext gc;
	
    @FXML
    protected Canvas canvas;
    
    @Override
	public void initialize() {
		gc = canvas.getGraphicsContext2D();
		canvas.widthProperty().bind(((AnchorPane) canvas.getParent()).widthProperty());
		canvas.heightProperty().bind(((AnchorPane) canvas.getParent()).heightProperty());
    }
    
    public void setMap(Map map) {
    	currentMap = map;
		canvas.widthProperty().addListener(event -> {
			drawBackground(); drawMap();
		});
		canvas.heightProperty().addListener(event -> {
			drawBackground(); drawMap();
		});
    }
    
    /**
	 * Sets the transform for the GraphicsContext to rotate around a pivot point.
	 *
	 * @param gc
	 *            the graphics context the transform to applied to.
	 * @param angle
	 *            the angle of rotation.
	 * @param px
	 *            the x pivot co-ordinate for the rotation (in canvas co-ordinates).
	 * @param py
	 *            the y pivot co-ordinate for the rotation (in canvas co-ordinates).
	 */
	private void rotate(GraphicsContext gc, double angle, double px, double py) {
		Rotate r = new Rotate(angle, px, py);
		gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
	}
    
    /**
	 * Draws an image on a graphics context.
	 *
	 * The image is drawn at (tlpx, tlpy) rotated by angle pivoted around the point:
	 * (tlpx + image.getWidth() / 2, tlpy + image.getHeight() / 2)
	 *
	 * @param gc
	 *            the graphics context the image is to be drawn on.
	 * @param angle
	 *            the angle of rotation.
	 * @param tlpx
	 *            the top left x co-ordinate where the image will be plotted (in
	 *            canvas co-ordinates).
	 * @param tlpy
	 *            the top left y co-ordinate where the image will be plotted (in
	 *            canvas co-ordinates).
	 */
	private void drawRotatedImage(GraphicsContext gc, Image image, double angle, double tlpx, double tlpy, double width,
			double height) {
		gc.save(); // saves the current state on stack, including the current transform
		rotate(gc, angle, tlpx + width / 2, tlpy + height / 2);
		gc.drawImage(image, tlpx, tlpy, width, height);
		gc.restore(); // back to original state (before rotation)
	}
	
	private void drawImage(Image texture, double tileX, double tileY, int... rotation) {
		double x = tileX * TILE_SIZE * SCALE - offsetX;
		double y = tileY * TILE_SIZE * SCALE - offsetY;
		if (rotation.length > 0 && rotation[0] != 0) {
			drawRotatedImage(gc, texture, rotation[0], x, y, TILE_SIZE * SCALE, TILE_SIZE * SCALE);
		} else
			gc.drawImage(texture, x, y, TILE_SIZE * SCALE, TILE_SIZE * SCALE);
	}
	
	void moveScreen(double dx, double dy) {
		if(dx<0)
			offsetX = Math.max(-canvas.getWidth() + TILE_SIZE*SCALE, offsetX+dx*TILE_SIZE*SCALE);
		else
			offsetX = Math.min((currentMap.getWidth()-1)*TILE_SIZE*SCALE, offsetX+dx*TILE_SIZE*SCALE);
		if(dy<0)
			offsetY = Math.max(-canvas.getHeight() + TILE_SIZE * SCALE, offsetY + dy*TILE_SIZE*SCALE);
		else
			offsetY = Math.min(currentMap.getHeight() * TILE_SIZE * SCALE - TILE_SIZE * SCALE, offsetY + dy*TILE_SIZE*SCALE);
		drawBackground();
		drawMap();
	}

	/**
	 * Zooms in (or out) on the map. 
	 * @param zoom the zoom factor. For zooming in. Values greater than 1 for zooming in, values between 0 and 1 for zooming out.
	 * @param x the x coordinate on which the zooming should focus.
	 * @param y the y coordinate on which the zooming should focus.
	 */
	private void zoom(double zoom, double x, double y) {
		double oldScale = SCALE;
		double factor=zoom;//(zoom>0 ? SCALING_FACTOR*zoom : 1/(SCALING_FACTOR*-zoom));
		SCALE = Calculator.clamp(SCALE*factor, 0.5, 10);
		double mapWidth = TILE_SIZE * SCALE * currentMap.getWidth();
		double mapHeight = TILE_SIZE * SCALE * currentMap.getHeight();
		offsetX = Math.max(-canvas.getWidth() + TILE_SIZE * SCALE, Math.min(offsetX - (x + offsetX) * (oldScale / SCALE - 1) * factor,
				mapWidth - TILE_SIZE * SCALE));
		offsetY = Math.max(-canvas.getHeight() + TILE_SIZE * SCALE, Math.min(offsetY - (y + offsetY) * (oldScale / SCALE - 1) * factor,
				mapHeight - TILE_SIZE * SCALE));
		drawBackground();
		drawMap();
	}
	
	public void drawMap(int minX, int minY, int maxX, int maxY) {
		Tile[][] tiles = currentMap.getTiles();
		for (int i = Math.max(0, minY); i <= Math.min(tiles.length - 1, maxY); i++) {
			for (int j = Math.max(0, minX); j <= Math.min(tiles[0].length - 1, maxX); j++) {
				drawImage(tiles[i][j].getTexture(), j, i);
			}
		}
		for(Entity e : currentMap.getEntities())
			drawImage(e.getTexture(), e.getX(), e.getY());
	}
	
	public void drawMap(Rectangle rect) {
		drawMap(rect.x, rect.y, (int) rect.getMaxX(), (int) rect.getMaxY());
	}
	
	public void drawMap() {
		drawMap(0, 0, currentMap.getWidth(), currentMap.getHeight());
	}
	
	void drawBackground() {
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	public Point getTileOnPosition(double x, double y) {
		return new Point((int) ((x + offsetX)/(TILE_SIZE*SCALE)), (int) ((y+offsetY)/(TILE_SIZE*SCALE)));
	}
	
	//The zoom events are only called for touchscreen/-pad zooming
	@FXML
	void onZoomStarted(ZoomEvent event) {
		zooming = true;
		oldZoom = 1;
	}
	
	@FXML
	void onZoomFinished(ZoomEvent event) {
		zooming = false;
	}
	
	@FXML
	void onZoom(ZoomEvent event) {
		zoom(event.getTotalZoomFactor()/oldZoom, event.getX(), event.getY());
		oldZoom = event.getTotalZoomFactor();
	}
	
	@FXML
	void onScroll(ScrollEvent event) {
		if(zooming) return;
		if(!event.isDirect()) //event triggered by mouse
			zoom(Math.pow(SCALING_FACTOR, event.getDeltaY()/event.getMultiplierY()), event.getX(), event.getY()); //the multiplier is for device and settings dependent scaling
		else { //event triggered by touchscreen
			if(lastDragCoords != null) {
		    	double dx = (lastDragCoords.getX()-event.getX())/(TILE_SIZE*SCALE*SCALING_FACTOR);
		    	double dy = (lastDragCoords.getY()-event.getY())/(TILE_SIZE*SCALE*SCALING_FACTOR);
		    	moveScreen(dx, dy);
	    	}
	    	lastDragCoords = new Point2D(event.getX(), event.getY());
		}
	}
	
	@FXML
    public void onDragHandler(MouseEvent e) {
		if(!e.isSynthesized()) { // event triggered by mouse
	    	if(lastDragCoords != null) {
		    	double dx = (lastDragCoords.getX()-e.getX())/(TILE_SIZE*SCALE*SCALING_FACTOR);
		    	double dy = (lastDragCoords.getY()-e.getY())/(TILE_SIZE*SCALE*SCALING_FACTOR);
		    	moveScreen(dx, dy);
	    	}
	    	lastDragCoords = new Point2D(e.getX(), e.getY());
		}
    }
    
    @FXML
    public void onMousePressed(MouseEvent e) {
    	System.out.println("Mouse Pressed");
    	mousePressedCoords = lastDragCoords = new Point2D(e.getX(), e.getY());
    	System.out.println(mousePressedCoords);
    }
    
    //Only called if the event is triggered by a touchscreen.
    @FXML
    public void onScrollStarted(ScrollEvent e) {
    	lastDragCoords = new Point2D(e.getX(), e.getY());
    }
	
	@FXML
	void keyDown(KeyEvent keyEvent) throws IOException {
		//note that we move the camera, not the map!
		switch (keyEvent.getCode()) {
		case UP:
			moveScreen(0, -1.1);
			break;
		case RIGHT:
			moveScreen(1.1,0);
			break;
		case DOWN:
			moveScreen(0,1.1);
			break;
		case LEFT:
			moveScreen(-1.1, 0);
			break;
		default:
			break;
		}
	}
}