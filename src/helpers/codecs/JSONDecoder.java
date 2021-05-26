package helpers.codecs;

import org.json.JSONObject;

import data.mapdata.Entity;
import data.mapdata.prefabs.EntityPrefab;
import helpers.codecs.versions.JSONDecoderV1;
import helpers.codecs.versions.JSONDecoderV2;
import helpers.codecs.versions.JSONDecoderV3;

public abstract interface JSONDecoder {
	
	public EntityPrefab decodeEntityPrefab(JSONObject json);
	
	public default Entity decodeEntity(JSONObject json) {
		throw new RuntimeException("Decoding entities not supported in this version");
	}
	
	public default byte[][] decodeMask(JSONObject json) {
		throw new RuntimeException("Decoding FoW mask not supported in this version");
	}
	
	public static JSONDecoder get(int version) {
		switch(version) {
		case 1:
			return new JSONDecoderV1();
		case 2:
			return new JSONDecoderV2();
		case 3:
			return new JSONDecoderV3();
		}
		throw new RuntimeException("Unknown version " + version);
	}
}