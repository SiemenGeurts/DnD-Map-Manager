package helpers.codecs.versions;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.Tile;

public class JSONDecoderV4 extends JSONDecoderV3{
	public Map decodeMap(JSONObject json) {
		Map map = new Map();
		ArrayList<Map.Level> levels = new ArrayList<>(json.getInt("n_levels"));
		JSONArray levelsJson = json.getJSONArray("levels");
		for(int i = 0; i < levels.size(); i++) {
			JSONObject level = levelsJson.getJSONObject(i);
			String[] ids = level.getString("tiles").split(":");
			Tile[][] tiles = new Tile[level.getInt("height")][level.getInt("width")];
			int rowlen = tiles[0].length;
			for(int j = 0; j < tiles.length; j++)
				for(int k = 0; k < tiles[0].length; k++)
					tiles[j][k] = new Tile(Integer.valueOf(ids[j*rowlen+k]));
			
			int nEntities = level.getInt("n_entities");
			ArrayList<Entity> entities = new ArrayList<>();
			JSONArray entitiesJson = level.getJSONArray("entities");
			for(int j = 0; j < nEntities; j++)
				entities.add(decodeEntity(entitiesJson.getJSONObject(j)));
			
			byte[][] mask = decodeMask(level.getJSONObject("mask"));
			map.addLevel(tiles, entities, mask);
		}
		return map;
	}
}
