package data.mapdata;

public enum PresetTile {

	
	;
	
	private Tile tile;
	
	PresetTile(Tile _tile) {
		tile = _tile;
	}
	
	public Tile getTile() {
		return tile;
	}
	
}
