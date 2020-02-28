package data.mapdata.prefabs;

public abstract class Prefab<T> {
	
	int type;
	
	protected Prefab(int _type) {
		type = _type;
	}
	
	public int getType() { 
		return type;
	}
	
	public abstract T getInstance(int x, int y);
}