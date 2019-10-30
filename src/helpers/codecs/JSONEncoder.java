package helpers.codecs;

import org.json.JSONObject;

import data.mapdata.Property;
import data.mapdata.prefabs.EntityPrefab;

public class JSONEncoder {
	public JSONObject encode(EntityPrefab prefab) {
		JSONObject entity = new JSONObject();
		entity.put("width", prefab.width);
		entity.put("height", prefab.height);
		entity.put("bloodied", prefab.bloodied);
		entity.put("type", prefab.getID());
		entity.put("description", prefab.description);
		entity.put("name", prefab.name);
		JSONObject properties = new JSONObject();
		for (Property p : prefab.getProperties())
			properties.put(p.getKey(), p.getValue());
		entity.put("properties", properties);
		return entity;
	}

}
