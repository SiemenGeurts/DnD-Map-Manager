package data.mapdata;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import helpers.AssetManager;
import javafx.scene.image.Image;

public class Entity {
	private static final AtomicInteger idGenerator = new AtomicInteger();
	
	private int type;
	private int id;
	private double x, y;
	private int width, height;
	private boolean bloodied = false;
	private boolean isNPC = false;
	private ArrayList<Property> properties;
	private String description = "", name;
	
	public Entity(Integer _type, int _x, int _y, int _width, int _height, boolean _isNPC, String name) {
		this(_type, _x, _y, _width, _height, _isNPC);
		setName(name);
	}
	
	public Entity(Integer _type, int _x, int _y, int _width, int _height, boolean _isNPC) {
		type = _type;
		x = _x;
		y = _y;
		width = _width;
		height = _height;
		properties = new ArrayList<>();
		isNPC = _isNPC;
		id = idGenerator.getAndIncrement();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
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
	
	public int getID() {
		return id;
	}
	
	public void setID(int i) {
		id = i;
	}
	
	public boolean isNPC() {
		return isNPC;
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
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setHeight(int height) {
		this.height = height;
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
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String s) {
		description = s;
	}
	
	public Entity copy() {
		Entity copy = new Entity(type, getTileX(), getTileY(), width, height, isNPC);
		copy.setBloodied(bloodied);
		copy.setDescription(description);
		copy.setLocation(getLocation());
		copy.setProperties(new ArrayList<Property>(properties.stream().map(prop -> prop.copy()).collect(Collectors.toList())));
		return copy;
	}
	
	public Entity copyWidthId(int id) {
		Entity copy = copy();
		copy.setID(id);
		return copy;
	}
	
	@Override
	public String toString() {
		return "Entity[name=" +  name + ", x=" + x + ", y=" + y + ", isNPC="+ isNPC + "]";
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
