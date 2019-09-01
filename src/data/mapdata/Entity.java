package data.mapdata;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import helpers.AssetManager;
import javafx.scene.image.Image;

public class Entity {

	private int type;
	private double x, y;
	private int width, height;
	private boolean bloodied = false;
	private ArrayList<Property> properties;
	
	public Entity(Integer _type, int _x, int _y, int _width, int _height) {
		type = _type;
		x = _x;
		y = _y;
		width = _width;
		height = _height;
		properties = new ArrayList<>();
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
	
	public void setProperties(ArrayList<Property> properties) {
		this.properties = properties;
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
	
	public ArrayList<Property> getProperties() {
		return properties;
	}
	
	
	public String encode() {
		StringBuilder builder = new StringBuilder();
		builder.append(type).append(',').append((int) x).append(',').append((int) y).append(',').append(width).append(',').append(height);
		return builder.toString();
	}
	
	public static Entity decode(String s) {
		return null;
	}
	
	public static ArrayList<Property> getDefaultProperties() {
		ArrayList<Property> list = new ArrayList<>(8);
		list.add(new Property("Strength", "12"));
		list.add(new Property("Dexterity", "9"));
		list.add(new Property("Constitution", "15"));
		list.add(new Property("Intelligence", "13"));
		list.add(new Property("Wisdom", "15"));
		list.add(new Property("Charisma", "8"));
		list.add(new Property("Saving Throws", ""));
		list.add(new Property("Skills", ""));
		list.add(new Property("Senses", ""));
		list.add(new Property("Languages", ""));
		list.add(new Property("Challenge", ""));
		return list;
	}
}
