package comms;

import java.io.Serializable;

public class Message<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = 8800544401446453582L;
	private T message;
	public Message(T message) {
		this.message = message;
	}
	
	public T getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return "message[" + message.toString() + "]";
	}
}