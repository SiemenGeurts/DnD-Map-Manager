package helpers.codecs.versions;

import java.util.ArrayList;
import java.util.Base64;

import org.json.JSONObject;

import data.mapdata.Entity;
import data.mapdata.Property;
import data.mapdata.prefabs.EntityPrefab;
import gui.ErrorHandler;
import helpers.Logger;
import helpers.Utils;
import helpers.codecs.JSONDecoder;
import helpers.codecs.JSONKeys;

public class JSONDecoderV3 implements JSONDecoder {

	@Override
	public Entity decodeEntity(JSONObject json) {
		if(!json.getString("type").equals(JSONKeys.KEY_ENTITY))
			throw new IllegalArgumentException("JSON type is not entity");
		Entity entity = new Entity(json.getInt("entity_type"), json.getInt("x"), json.getInt("y"),
				json.getInt("width"), json.getInt("height"), json.getBoolean("isNPC"), json.getString("name"));
		entity.setDescription(json.getString("description"));
		entity.setID(json.getInt("id"));
		if(json.has("properties")) {
			JSONObject properties = json.getJSONObject("properties");
			ArrayList<Property> propertylist = new ArrayList<>(properties.length());
			for (String pkey : properties.keySet())
				propertylist.add(new Property(pkey, properties.getString(pkey)));
			entity.setProperties(propertylist);
		}
		return entity;
	}

	@Override
	public EntityPrefab decodeEntityPrefab(JSONObject json) {
		if(!json.getString("type").equals(JSONKeys.KEY_PREFAB_ENTITY))
			throw new IllegalArgumentException("JSON type is not entity prefab");
		JSONObject properties = json.getJSONObject("properties");
		ArrayList<Property> propertylist = new ArrayList<>(properties.length());
		for (String pkey : properties.keySet())
			propertylist.add(new Property(pkey, properties.getString(pkey)));
		EntityPrefab ep = new EntityPrefab(json.getInt("entity_type"), json.getInt("width"), json.getInt("height"),
				propertylist, json.getBoolean("bloodied"), false,
				(json.has("description") ? json.getString("description") : ""), json.getString("name"));
		Logger.println("Loaded entity: " + ep);
		return ep;
	}
	
	public byte[][] decodeMask(JSONObject json) {
		if(!json.getString("type").equals(JSONKeys.KEY_FOW_MASK))
			throw new IllegalArgumentException("JSON type is not FoW mask");
		int width = json.getInt("width");
		int height = json.getInt("height");
		String base64mask = json.getString("mask");
		try {
			return Utils.unflatten(Base64.getDecoder().decode(base64mask.getBytes()), width, height);
		} catch (Exception e) {
			ErrorHandler.handle("Could not decode mask.", e);
			byte[][] mask = new byte[height][width];
			for(int y = 0; y < height; y++)
				for(int x = 0; x < width; x++)
					mask[y][x] = 64;
			return mask;
		}
	}
}
