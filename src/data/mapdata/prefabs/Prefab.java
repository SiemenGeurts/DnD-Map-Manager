package data.mapdata.prefabs;

public abstract class Prefab<T> {
	
	int id;
	
	protected Prefab(int _id) {
		id = _id;
	}
	
	public int getID() { 
		return id;
	}
	
	public abstract T getInstance(int x, int y);
}