package helpers.codecs.versions;

import java.util.ArrayList;

import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.Property;
import data.mapdata.Tile;
import helpers.codecs.Decoder;

public class DecoderV1 extends Decoder {
	
	@Override
	public Entity decodeEntity(String s) {
		String[] arr = s.split(",");
		int i = 0;
		Entity entity = new Entity(Integer.valueOf(arr[i++]), Integer.valueOf(arr[i++]), Integer.valueOf(arr[i++]), Integer.valueOf(arr[i++]), Integer.valueOf(arr[i++]), Boolean.valueOf(arr[i++]));
		entity.setID(Integer.valueOf(arr[i++]));
		String desc = arr[i++];
		if(desc.strip().length() > 0)
			entity.setDescription(new String(base64.decode(arr[i++])));
		else
			entity.setDescription("");
		entity.setName(new String(arr[i++]));
		ArrayList<Property> properties = new ArrayList<Property>();
		for(;i < arr.length; i++) {
			int index = arr[i].indexOf("/");
			properties.add(new Property(arr[i].substring(0, index), new String(base64.decode(arr[i].substring(index+1)))));
		}
		entity.setProperties(properties);
		return entity;
	}

	@Override
	public Map decodeMap(String string) {
		int index = string.indexOf(";");
		String[] s = string.substring(0, index).split(":");
		Tile[][] tiles = new Tile[Integer.valueOf(s[0])][Integer.valueOf(s[1])];
		int rowlen = tiles[0].length;
		for(int i = 0; i < tiles.length; i++)
			for(int j = 0; j < rowlen; j++)
				tiles[i][j] = new Tile(Integer.valueOf(s[i*rowlen+j+2]));
		Map map = new Map(tiles);
		
		s = string.substring(index+1).split(":");
		ArrayList<Entity> entities = new ArrayList<>(Integer.valueOf(s[0]));
		for(int i = 1; i < s.length; i++) {
			entities.add(decodeEntity(s[i]));
		}
		map.setEntities(entities);
		return map;
	}
}
