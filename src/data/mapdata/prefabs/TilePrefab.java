package data.mapdata.prefabs;

import data.mapdata.Tile;

public class TilePrefab extends Prefab<Tile> {
	
	public TilePrefab(int id) {
		super(id);
	}
	
	@Override
	public Tile getInstance(int x, int y) {
		return new Tile(id);
	}		
}
