package controller;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;

import controller.InitClassStructure.SceneController;
import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.Tile;
import helpers.Calculator;
import helpers.Logger;
import helpers.ScalingBounds;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.RotateEvent;
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
	private Map currentMap;
	
	private Point2D lastDragCoords;
	protected Point2D mousePressedCoords;
	private double oldZoom = 1;
	private boolean zooming = false;
	protected GraphicsContext gc;
	protected ScalingBounds imagebounds;
	
    @FXML
    protected Canvas canvas;
    
    @Override
	public void initialize() {
		gc = canvas.getGraphicsContext2D();
		((AnchorPane)canvas.getParent()).widthProperty().addListener((obs, oldVal, newVal) -> {
			canvas.widthProperty().setValue(newVal);
		});
		((AnchorPane)canvas.getParent()).heightProperty().addListener((obs, oldVal, newVal) -> {
			canvas.heightProperty().setValue(newVal);
		});
		canvas.widthProperty().addListener(event -> {
			onResize();
		});
		canvas.heightProperty().addListener(event -> {
			onResize();
		});
    }
    
    public Map getMap() {
    	return currentMap;
    }
    
    public void setMap(Map map) {
    	currentMap = map;
    	calculateBackgroundBounds();
		redraw();
    }
    
    public void calculateBackgroundBounds() {    	
    	if(currentMap.getBackground() != null)
    		imagebounds = ScalingBounds.getBounds(currentMap.getWidth()*TILE_SIZE*SCALE, currentMap.getHeight()*TILE_SIZE*SCALE, currentMap.getBackground(), currentMap.getScaling());
    }
    
    private void onResize() {
    	redraw();
    }
    /**
	 * Sets the transform for the GraphicsContext to rotate around a pivot point.
	 *
	 * @param angle
	 *            the angle of rotation.
	 * @param px
	 *            the x pivot co-ordinate for the rotation (in canvas co-ordinates).
	 * @param py
	 *            the y pivot co-ordinate for the rotation (in canvas co-ordinates).
	 */
	protected void rotate(double angle, double px, double py) {
		Rotate r = new Rotate(angle, px, py);
		gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
	}
	
	private void drawImage(Image texture, double tileX,double tileY) {
		drawImage(texture, tileX, tileY, 1, 1);
	}
	
	private void drawImage(Image texture, double tileX, double tileY, double width, double height) {
		double factor = TILE_SIZE*SCALE;
		gc.drawImage(texture, tileX*factor-offsetX, tileY*factor-offsetY, width*factor, height*factor);
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
		redraw();
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
		redraw();
	}
	
	public void drawMap(int minX, int minY, int maxX, int maxY) {
		Tile[][] tiles = currentMap.getTiles();
		for (int i = Math.max(0, minY); i <= Math.min(tiles.length - 1, maxY); i++) {
			for (int j = Math.max(0, minX); j <= Math.min(tiles[0].length - 1, maxX); j++) {
				drawImage(tiles[i][j].getTexture(), j, i);
			}
		}
		for(Entity e : currentMap.getEntities())
			if(e.getX()>=minX && e.getY()>=minY && e.getX()+e.getWidth()<=maxX && e.getY()+e.getHeight()<=maxY)
				drawImage(e.getTexture(), e.getX(), e.getY(), e.getWidth(), e.getHeight());
		
		double xBegin = Math.max(0, minX)*TILE_SIZE*SCALE-offsetX;
		double xEnd = Math.min(tiles[0].length+1, maxX)*TILE_SIZE*SCALE-offsetX;
		double yBegin = Math.max(0, minY)*TILE_SIZE*SCALE-offsetY;
		double yEnd = Math.min(tiles.length+1, maxY)*TILE_SIZE*SCALE-offsetY;
		
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1);
		for (double x = xBegin; x<=xEnd+0.1*TILE_SIZE; x+=TILE_SIZE*SCALE)
			gc.strokeLine(x, yBegin, x, yEnd);
			
		for (double y = yBegin; y <= yEnd+0.1*TILE_SIZE; y+=TILE_SIZE*SCALE)
			gc.strokeLine(xBegin, y, xEnd, y);
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
		if(currentMap.getBackground() != null)
			gc.drawImage(currentMap.getBackground(),imagebounds.getSourceX(), imagebounds.getSourceY(), imagebounds.getSourceWidth(), imagebounds.getSourceHeight(),
					imagebounds.getDestX()-offsetX, imagebounds.getDestY()-offsetY, imagebounds.getDestWidth(), imagebounds.getDestHeight());
	}
	
	public void redraw() {
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		drawBackground();
		drawMap();
	}
	
	public Point getTileOnPosition(double x, double y) {
		return new Point((int) ((x + offsetX)/(TILE_SIZE*SCALE)), (int) ((y+offsetY)/(TILE_SIZE*SCALE)));
	}
	
	public Point2D screenToWorld(Point p) {
		return new Point2D((p.x + offsetX)/(TILE_SIZE*SCALE), (p.y+offsetY)/(TILE_SIZE*SCALE));
	}
		
	public Point worldToScreen(Point2D p) {
		return new Point((int)(p.getX()*TILE_SIZE*SCALE-offsetX),(int) (p.getY()*TILE_SIZE*SCALE-offsetY));
	}
	
	public Rectangle2D screenToWorld(Rectangle rect) {
		return new Rectangle2D((rect.x + offsetX)/(TILE_SIZE*SCALE), (rect.y+offsetY)/(TILE_SIZE*SCALE), rect.getWidth()*TILE_SIZE*SCALE, rect.getHeight()*TILE_SIZE*SCALE);
	}
	
	public Rectangle worldToScreen(Rectangle2D rect) {
		return new Rectangle((int)(rect.getMinX()*TILE_SIZE*SCALE-offsetX),(int) (rect.getMinY()*TILE_SIZE*SCALE-offsetY), (int) (rect.getWidth()*TILE_SIZE*SCALE), (int) (rect.getHeight()*TILE_SIZE*SCALE));
	}
	
    @FXML
    void onMouseClicked(MouseEvent event) {
    	if(mousePressedCoords != null && mousePressedCoords.distance(event.getX(), event.getY())>TILE_SIZE*SCALE/2) return;
    	handleClick(getTileOnPosition(event.getX(), event.getY()), event);
    }
    
    protected void handleClick(Point position, MouseEvent event) {
    	
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
    	Logger.println("Mouse Pressed");
    	mousePressedCoords = lastDragCoords = new Point2D(e.getX(), e.getY());
    	Logger.println(mousePressedCoords);
    }
    
    //Only called if the event is triggered by a touchscreen.
    @FXML
    public void onScrollStarted(ScrollEvent e) {
    	lastDragCoords = new Point2D(e.getX(), e.getY());
    }
    
	@FXML
	void onRotate(RotateEvent e) {
		//Works perfectly, but is very annoying.
		//canvas.setRotate(e.getAngle()+canvas.getRotate());
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