package data.mapdata.prefabs;

import java.util.ArrayList;
import java.util.stream.Collectors;

import data.mapdata.Entity;
import data.mapdata.Property;
import helpers.Logger;

public class EntityPrefab extends Prefab<Entity> {
	
	public int width=1, height=1;
	ArrayList<Property> properties;
	public boolean bloodied, isPlayer;
	public String description = "";
	public String name = "";
	
	public EntityPrefab(int type, int width, int height, ArrayList<Property> properties, boolean bloodied, boolean isPlayer, String description, String name) {
		this(type, width, height, properties, bloodied, isPlayer, description);
		setName(name);
	}
	
	public EntityPrefab(int type, int width, int height, ArrayList<Property> properties, boolean bloodied, boolean isPlayer, String description) {
		super(type);
		this.width = width;
		this.height = height;
		this.bloodied = bloodied;
		this.properties = properties;
		this.isPlayer = isPlayer;
		this.description = description;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Property> getProperties() {
		return properties;
	}
	
	@Override
	public String toString() {
		return "EntityPrefab[type=" + type + ", name="+name + ", isPlayer="+isPlayer+"]";
	}
	
	@Override
	public Entity getInstance(int x, int y) {
		Entity entity = new Entity(type, x, y, width, height, !isPlayer);
		entity.setBloodied(bloodied);
		entity.setDescription(description);
		entity.setName(name);
		if(properties != null)
			entity.setProperties(new ArrayList<>(properties.stream().map(prop -> prop.copy()).collect(Collectors.toList())));
		Logger.println("Created instance " + entity);
		return entity;
	}
	
	public static EntityPrefab fromEntity(Entity e) {
		return new EntityPrefab(e.getType(), e.getWidth(), e.getHeight(),
				e.getProperties(), e.isBloodied(), !e.isNPC(), e.getDescription(), e.getName());
	}
}	