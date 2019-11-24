package helpers.codecs;

import org.json.JSONObject;

import data.mapdata.prefabs.EntityPrefab;
import helpers.codecs.version1.JSONDecoderV1;

public abstract class JSONDecoder {
	
	public abstract EntityPrefab decodeEntity(JSONObject json);
	
	public static JSONDecoder get(int version) {
		switch(version) {
		case 1:
			return new JSONDecoderV1();
		}
		return null;
	}
}
