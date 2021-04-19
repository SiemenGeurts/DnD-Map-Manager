package comms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.json.JSONObject;

public class SerializableJSON implements Serializable {

	private static final long serialVersionUID = 7002773323322995121L;
	
	private String jsonString;
	
	public SerializableJSON(JSONObject json) {
		jsonString = json.toString();
	}
	
	public JSONObject getJSON() {
		return new JSONObject(jsonString);
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
		stream.defaultReadObject();
	}
	
}
