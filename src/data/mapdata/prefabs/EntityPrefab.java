package data.mapdata.prefabs;

import java.util.ArrayList;

import data.mapdata.Entity;
import data.mapdata.Property;

public class EntityPrefab extends Prefab<Entity> {
	
	public int width=1, height=1;
	ArrayList<Property> properties;
	public boolean bloodied;
	
	public EntityPrefab(int type, int width, int height, ArrayList<Property> properties, boolean bloodied) {
		super(type);
		this.width = width;
		this.height = height;
		this.bloodied = bloodied;
		this.properties = properties;
	}
	
	public ArrayList<Property> getProperties() {
		return properties;
	}
	
	@Override
	public Entity getInstance(int x, int y) {
		Entity entity = new Entity(id, x, y, width, height);
		entity.setBloodied(bloodied);
		entity.setProperties(properties);
		return entity;
	}
}	