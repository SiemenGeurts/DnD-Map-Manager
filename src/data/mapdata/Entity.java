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
	
	private void setID(int i) {
		id = i;
	}
	
	public boolean isNPC() {
		return true;
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
	
	public Entity copy() {
		Entity copy = new Entity(type, getTileX(), getTileY(), width, height, isNPC);
		copy.setBloodied(bloodied);
		copy.setLocation(getLocation());
		copy.setProperties(new ArrayList<Property>(properties.stream().map(prop -> prop.copy()).collect(Collectors.toList())));
		return copy;
	}
	
	public Entity copyWidthId(int id) {
		Entity copy = copy();
		copy.setID(id);
		return copy;
	}
	
	public String encode(boolean includeProperties) {
		StringBuilder builder = new StringBuilder();
		builder.append(type).append(',').append((int) x).append(',').append((int) y).append(',').append(width).append(',').append(height).append(',').append(id).append(',').append(isNPC);
		if(includeProperties)
			for(Property p : properties)
				builder.append(',').append(p.getKey()).append('/').append(p.getValue());
		return builder.toString();
	}
	
	public static Entity decode(String s) {
		String[] arr = s.split(",");
		int i = 0;
		Entity entity = new Entity(Integer.valueOf(arr[i++]), Integer.valueOf(arr[i++]), Integer.valueOf(arr[i++]), Integer.valueOf(arr[i++]), Integer.valueOf(arr[i++]), Boolean.valueOf(arr[i++]));
		entity.setID(Integer.valueOf(arr[i++]));
		for(;i < arr.length; i++) {
			int index = arr[i].indexOf("/");
			entity.properties.add(new Property(arr[i].substring(0, index), arr[i].substring(index+1)));
		}
		return entity;
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
