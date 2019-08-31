package data.mapdata;

public class Property {
	
	private String key, value;
	
	public Property(String _key, String _value) {
		key = _key;
		value = _value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "Property " + key + ": " + value;
	}
}
