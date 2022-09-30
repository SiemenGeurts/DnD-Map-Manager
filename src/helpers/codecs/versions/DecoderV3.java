package helpers.codecs.versions;

import org.json.JSONObject;

import data.mapdata.Entity;
import data.mapdata.Map;
import helpers.codecs.Decoder;
import helpers.codecs.JSONDecoder;

public class DecoderV3 extends Decoder {
	
	JSONDecoder decoder;
	
	public DecoderV3(int jsonVersion) {
		decoder = JSONDecoder.get(jsonVersion);
	}
	
	@Override
	public Entity decodeEntity(String s) {
		return decoder.decodeEntity(new JSONObject(s));
	}

	@Override
	public Map decodeMap(String s) {
		return decoder.decodeMap(new JSONObject(s));
	}
}
