package comms;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class Message<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = 8800544401446453582L;
	private static final AtomicInteger idGen = new AtomicInteger();
	
	private T message;
	private int id;
	public Message(T message) {
		this.message = message;
		id = idGen.getAndIncrement();
	}
	
	public T getMessage() {
		return message;
	}
	
	public int getID() {
		return id;
	}
	
	@Override
	public String toString() {
		return "message[" + message.toString() + ", id=" + id + "]";
	}
}