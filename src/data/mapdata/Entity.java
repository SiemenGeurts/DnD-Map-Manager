package data.mapdata;

import java.awt.Point;
import java.awt.geom.Point2D;

import javafx.scene.image.Image;

public class Entity {

	private int type;
	private double x, y;
	private int width, height;
	private boolean bloodied = false;
	
	public Entity(Integer _type, int _x, int _y, int _width, int _height) {
		type = _type;
		x = _x;
		y = _y;
		width = _width;
		height = _height;
	}
	
	public Integer getType() {
		return type;
	}
	
	public Image getTexture() {
		return AssetManager.textures.get(type);
	}
	
	public boolean isBloodied() {
		return bloodied;
	}

	public void setBloodied(boolean bloodied) {
		this.bloodied = bloodied;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	public int getTileX() {
		return (int) x;
	}
	
	public int getTileY() {
		return (int) y;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Point2D getLocation() {
		return new Point2D.Double(x, y);
	}
	
	public void setLocation(Point2D p) {
		x = p.getX();
		y = p.getY();
	}
	
	public Point getTileLocation() {
		return new Point((int) x, (int) y);
	}
	
	public String encode() {
		StringBuilder builder = new StringBuilder();
		builder.append(type).append(',').append((int) x).append(',').append((int) y).append(',').append(width).append(',').append(height);
		return builder.toString();
	}
	
	public static Entity decode(String s) {
		return null;
	}
}
