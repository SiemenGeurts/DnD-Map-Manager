package helpers.codecs;

import java.util.Base64;

import data.mapdata.Entity;
import data.mapdata.Map;
import data.mapdata.Property;
import data.mapdata.Tile;

public class Encoder {
	
	public static final int VERSION_ID = 1;
	public static java.util.Base64.Encoder base64 = Base64.getEncoder();
	
	public static String encode(Entity entity, boolean includeProperties) {
		StringBuilder builder = new StringBuilder();
		builder.append(entity.getType()).append(',').append(entity.getTileX()).append(',').append(entity.getTileY()).
			append(',').append(entity.getWidth()).append(',').append(entity.getHeight());
		builder.append(',').append(entity.isNPC()).append(',').append(entity.getID()).append(',').
			append(base64.encodeToString(entity.getDescription().getBytes())).append(',').append(entity.getName());
		if(includeProperties)
			for(Property p : entity.getProperties())
				builder.append(',').append(p.getKey()).append('/').append(base64.encodeToString(p.getValue().getBytes()));
		return builder.toString();
	}
	
	public static String encode(Map map, boolean includeEntityData) {
		StringBuilder builder = new StringBuilder();
		Tile[][] tiles = map.getTiles();
		builder.append(tiles.length).append(':')
				.append(tiles[0].length);
		for(int i = 0; i < tiles.length; i++)
			for(int j = 0; j < tiles[0].length; j++)
				builder.append(':').append(tiles[i][j].getType());

		builder.append(';').append(map.getEntities().size()).append(':');
		for(Entity e : map.getEntities())
			builder.append(encode(e, includeEntityData)).append(':');
		return builder.toString();
	}
}
