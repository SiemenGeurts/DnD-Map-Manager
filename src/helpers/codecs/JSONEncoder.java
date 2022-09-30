package helpers.codecs;

import java.util.Base64;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import controller.InitiativeListController.InitiativeEntry;
import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.Property;
import data.mapdata.prefabs.EntityPrefab;
import helpers.Utils;

import static helpers.codecs.JSONKeys.*;

public class JSONEncoder {
	
	public static final JSONObject accepted = new JSONObject();
	public static final JSONObject declined = new JSONObject();
	public static final int VERSION = 4;
	static {
		accepted.put("type", BoolKey.KEY_PREVIEW_CONFIRMATION.get());
		accepted.put("accepted", true);
		declined.put("type", BoolKey.KEY_PREVIEW_CONFIRMATION.get());
		declined.put("accepted", false);
	}
	
	public static JSONObject version() {
		return new JSONObject().put("type", IntKey.KEY_JSON_VERSION.get()).put("version", VERSION);
	}
	
	public static JSONObject encode(StringKey key, String value) {
		JSONObject json = new JSONObject();
		json.put("type", key.get()).put("value", value);
		return json;
	}
	
	public static JSONObject encode(IntKey key, int value) {
		JSONObject json = new JSONObject();
		json.put("type", key.get()).put("value", value);
		return json;
	}
	
	public static JSONObject encode(BoolKey key, boolean value) {
		JSONObject json = new JSONObject();
		json.put("type", key.get()).put("value", value);
		return json;
	}
	
	public static JSONObject encode(EntityPrefab prefab) {
		JSONObject entity = new JSONObject();
		entity.put("type", KEY_PREFAB_ENTITY);
		entity.put("width", prefab.width);
		entity.put("height", prefab.height);
		entity.put("bloodied", prefab.bloodied);
		entity.put("entity_type", prefab.getType());
		entity.put("description", prefab.description);
		entity.put("name", prefab.name);
		JSONObject properties = new JSONObject();
		for (Property p : prefab.getProperties())
			properties.put(p.getKey(), p.getValue());
		entity.put("properties", properties);
		return entity;
	}
	
	public static JSONObject encode(Entity entity, boolean includeProperties) {
		JSONObject json = new JSONObject();
		json.put("type", KEY_ENTITY);
		json.put("entity_type", entity.getType());
		json.put("width", entity.getWidth());
		json.put("height", entity.getHeight());
		json.put("x", entity.getTileX());
		json.put("y", entity.getTileY());
		json.put("isNPC", entity.isNPC());
		json.put("name", entity.getName());
		json.put("id", entity.getID());
		json.put("description", entity.getDescription());
		if(includeProperties) {
			JSONObject properties = new JSONObject();
			for(Property p : entity.getProperties())
				properties.put(p.getKey(), p.getValue());
			json.put("properties", properties);
		}
		return json;
	}
	
	public static JSONObject encode(Map map, boolean includeEntityData) {
		JSONObject json = new JSONObject();
		json.put("n_levels", map.getNumberOfLevels());
		JSONArray levels = new JSONArray();
		for(int i = 0; i < map.getNumberOfLevels(); i++) {
			Map.Level level = map.getLevel(i);
			JSONObject levelJson = new JSONObject();
			levelJson.put("width", level.getWidth());
			levelJson.put("height", level.getHeight());
			StringBuilder builder = new StringBuilder();
			for(int k = 0; k < level.getHeight(); k++)
				for(int j = 0; j < level.getWidth(); j++)
					builder.append(':').append(level.getTile(j,k).getType());
			levelJson.put("tiles", builder.substring(1));
			
			List<Entity> entities = level.getEntities();
			levelJson.put("n_entities", entities.size());
			JSONArray entitiesJson = new JSONArray();
			for(Entity e : entities)
				entitiesJson.put(encode(e, includeEntityData));
			
			levelJson.put("entities", entitiesJson);
			levelJson.put("mask", encodeMask(level.getMask()));
			
			levels.put(levelJson);
		}
		json.put("levels", levels);
		return json;
	}
	
	public static JSONObject encodeMask(byte[][] mask) {
		JSONObject json = new JSONObject();
		json.put("type", KEY_FOW_MASK);
		json.put("width", mask[0].length);
		json.put("height", mask.length);
		json.put("mask", new String(Base64.getEncoder().encode(Utils.flatten(mask))));
		return json;
	}
	
	private static JSONObject encodeInitiative(InitiativeEntry entry) {
		JSONObject json = new JSONObject();
		json.put("entity_id", entry.getEntity().getID());
		json.put("initiative", entry.getInitiative());
		return json;
	}
	
	public static JSONObject encodeInitiatives(List<InitiativeEntry> entries) {
		JSONObject json = new JSONObject();
		json.put("type", KEY_INITIATIVELIST);
		JSONArray array = new JSONArray();
		for(InitiativeEntry entry : entries)
			array.put(encodeInitiative(entry));
		json.put("initiatives", array);
		return json;
	}

}
