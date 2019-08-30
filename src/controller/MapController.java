package controller;
import java.io.IOException;

import controller.InitClassStructure.SceneController;
import data.mapdata.Map;
import data.mapdata.Tile;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

public class MapController extends SceneController {

	private int TILE_SIZE = 50;
	private double SCALE = 1;
	private int OFFSET_SPEED = TILE_SIZE + 10;
	private double SCALING_FACTOR = 1.3;
	private double offsetX, offsetY;
	public Map currentMap;
	
	private GraphicsContext gc;
	
	
    @FXML
    private Canvas canvas;
    

    @Override
	public void initialize() {
		gc = canvas.getGraphicsContext2D();
    }
    
    
    @FXML
    void hoverTile(MouseEvent event) {

    }

    @FXML
    void placeTile(MouseEvent event) {

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
	
	private void drawImage(Image texture, int tileX, int tileY, int... rotation) {
		double x = tileX * TILE_SIZE * SCALE - offsetX;
		double y = tileY * TILE_SIZE * SCALE - offsetY;
		if (rotation.length > 0 && rotation[0] != 0) {
			drawRotatedImage(gc, texture, rotation[0], x, y, TILE_SIZE * SCALE, TILE_SIZE * SCALE);
		} else
			gc.drawImage(texture, x, y, TILE_SIZE * SCALE, TILE_SIZE * SCALE);
	}
	
	void moveScreen(int orientation, int direction) {
		if (orientation == 0) {
			if (direction == -1) {
				offsetX = Math.max(-canvas.getWidth() + TILE_SIZE * SCALE, offsetX - OFFSET_SPEED);
			} else {
				offsetX = Math.min(currentMap.getWidth() * TILE_SIZE * SCALE - TILE_SIZE * SCALE, offsetX + OFFSET_SPEED);
			}
		} else {
			if (direction == -1) {
				offsetY = Math.max(-canvas.getHeight() + TILE_SIZE * SCALE, offsetY - OFFSET_SPEED);
			} else {
				offsetY = Math.min(currentMap.getHeight() * TILE_SIZE * SCALE - TILE_SIZE * SCALE, offsetY + OFFSET_SPEED);
			}
		}
		drawBackground();
		drawMap(0, 0, currentMap.getWidth(), currentMap.getHeight());
	}
	
	@FXML
	void zoomMap(ScrollEvent event) {
		zoom(event.getDeltaY(), event.getX(), event.getY());
	}

	private void zoom(double zoom, double x, double y) {
		double oldScale = SCALE;
		if (zoom > 0) {
			SCALE = Math.min(10, SCALE * SCALING_FACTOR);
			double mapWidth = TILE_SIZE * SCALE * currentMap.getWidth();
			double mapHeight = TILE_SIZE * SCALE * currentMap.getHeight();
			offsetX = Math.max(-canvas.getWidth() + TILE_SIZE * SCALE, Math.min(offsetX - (x + offsetX) * (oldScale / SCALE - 1) * SCALING_FACTOR,
					mapWidth - TILE_SIZE * SCALE));
			offsetY = Math.max(-canvas.getHeight() + TILE_SIZE * SCALE, Math.min(offsetY - (y + offsetY) * (oldScale / SCALE - 1) * SCALING_FACTOR,
					mapHeight - TILE_SIZE * SCALE));
		} else {
			SCALE = Math.max(0.5, SCALE / SCALING_FACTOR);
			double mapWidth = TILE_SIZE * SCALE * currentMap.getWidth();
			double mapHeight = TILE_SIZE * SCALE * currentMap.getHeight();
			offsetX = Math.max(-canvas.getWidth() + TILE_SIZE * SCALE, Math.min(offsetX - (x + offsetX) * (oldScale / SCALE - 1) / SCALING_FACTOR,
					mapWidth - TILE_SIZE * SCALE));
			offsetY = Math.max(-canvas.getHeight() + TILE_SIZE * SCALE, Math.min(offsetY - (y + offsetY) * (oldScale / SCALE - 1) / SCALING_FACTOR,
					mapHeight - TILE_SIZE * SCALE));
		}
		drawBackground();
		drawMap(0, 0, currentMap.getWidth(), currentMap.getHeight());
	}
	
	public void drawMap(int minX, int minY, int maxX, int maxY) {
		Tile[][] tiles = currentMap.getTiles();
		for (int i = Math.max(0, minY); i <= Math.min(tiles.length - 1, maxY); i++) {
			for (int j = Math.max(0, minX); j <= Math.min(tiles[0].length - 1, maxX); j++) {
				drawImage(tiles[i][j].getTexture(), j, i);
			}
		}
	}
	
	public void drawMap() {
		drawMap(0, 0, currentMap.getWidth(), currentMap.getHeight());
	}
	
	void drawBackground() {
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	@FXML
	void keyDown(KeyEvent keyEvent) throws IOException {
		switch (keyEvent.getCode()) {
		case UP:
			moveScreen(1, -1);
			break;
		case RIGHT:
			moveScreen(0, 1);
			break;
		case DOWN:
			moveScreen(1, 1);
			break;
		case LEFT:
			moveScreen(0, -1);
			break;
		default:
			break;
		}
	}
	
	

}
