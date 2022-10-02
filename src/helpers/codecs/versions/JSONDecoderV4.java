package helpers.codecs.versions;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.Tile;
import gui.ErrorHandler;
import helpers.Utils;
import helpers.codecs.JSONKeys;

public class JSONDecoderV4 extends JSONDecoderV3{
	private Integer[][] decodeIntArray(String encoded, int width, int height) {
		Integer[] types = new Integer[width*height];
		String[] groups = encoded.split(":");
		int index = 0;
		for(String group : groups) {
			int sepIndex = group.indexOf("#");
			if(sepIndex == -1)
				types[index++] = Integer.valueOf(group);
			if(group.contains("#")) {
				int count = Integer.valueOf(group.substring(0,sepIndex));
				int type = Integer.valueOf(group.substring(sepIndex+1));
				for(int i = 0; i < count;i++)
					types[index++] = type;
			}
		}
		Integer[][] output = new Integer[height][width];
		Utils.unflatten(types, width, height, output);
		return output;
	}
	
	@Override
	public byte[][] decodeMask(JSONObject json) {
		if(!json.getString("type").equals(JSONKeys.KEY_FOW_MASK))
			throw new IllegalArgumentException("JSON type is not FoW mask");
		int width = json.getInt("width");
		int height = json.getInt("height");
		String mask = json.getString("mask");
		try {
			Integer[][] decoded = decodeIntArray(mask,width,height);
			byte[][] mask2 = new byte[decoded.length][decoded[0].length];
			for(int i = 0; i < mask2.length; i++)
				for(int j = 0; j < mask2[0].length; j++)
					mask2[i][j] = decoded[i][j].byteValue();
			return mask2;
		} catch (Exception e) {
			ErrorHandler.handle("Could not decode mask.", e);
			byte[][] mask2 = new byte[height][width];
			for(int y = 0; y < height; y++)
				for(int x = 0; x < width; x++)
					mask2[y][x] = 64;
			return mask2;
		}
	}
	
	@Override
	public Map decodeMap(JSONObject json) {
		Map map = new Map();
		JSONArray levelsJson = json.getJSONArray("levels");
		for(int i = 0; i < levelsJson.length(); i++) {
			JSONObject level = levelsJson.getJSONObject(i);
			Integer[][] types = decodeIntArray(level.getString("tiles"), level.getInt("width"), level.getInt("height"));
			Tile[][] tiles = new Tile[types.length][types[0].length];
			for(int j = 0; j < tiles.length; j++)
				for(int k = 0; k < tiles[0].length; k++)
					tiles[j][k] = new Tile(types[j][k]);
			
			int nEntities = level.getInt("n_entities");
			ArrayList<Entity> entities = new ArrayList<>();
			JSONArray entitiesJson = level.getJSONArray("entities");
			for(int j = 0; j < nEntities; j++)
				entities.add(decodeEntity(entitiesJson.getJSONObject(j)));
			
			byte[][] mask = decodeMask(level.getJSONObject("mask"));
			map.addLevel(tiles, entities, mask).setName(level.getString("name"));
		}
		return map;
	}
}
