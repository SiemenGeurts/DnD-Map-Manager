package helpers.codecs;

import java.util.Base64;

import data.mapdata.Entity;
import data.mapdata.Map;
import helpers.codecs.versions.DecoderV1;
import helpers.codecs.versions.DecoderV2;

public abstract class Decoder {
	public static final java.util.Base64.Decoder base64 = Base64.getDecoder();
	
	public abstract Entity decodeEntity(String s);
	public abstract Map decodeMap(String s);
	
	public final static Decoder getDecoder(int version) {
		switch(version) {
		case 2:
			return new DecoderV2();
		case 1:
		default:
			return new DecoderV1();
		}
	}
}
