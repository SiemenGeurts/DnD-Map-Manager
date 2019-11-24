package helpers.codecs.version1;

import java.util.ArrayList;

import org.json.JSONObject;

import data.mapdata.Property;
import data.mapdata.prefabs.EntityPrefab;
import helpers.Logger;
import helpers.codecs.JSONDecoder;

public class JSONDecoderV1 extends JSONDecoder {
	public EntityPrefab decodeEntity(JSONObject json) {
		JSONObject properties = json.getJSONObject("properties");
		ArrayList<Property> propertylist = new ArrayList<>(properties.length());
		for (String pkey : properties.keySet())
			propertylist.add(new Property(pkey, properties.getString(pkey)));
		EntityPrefab ep = new EntityPrefab(json.getInt("type"), json.getInt("width"), json.getInt("height"),
				propertylist, json.getBoolean("bloodied"), false,
				(json.has("description") ? json.getString("description") : ""), json.getString("name"));
		Logger.println("Loaded entity: " + ep);
		return ep;
	}
}