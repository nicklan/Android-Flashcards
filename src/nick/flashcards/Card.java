package nick.flashcards;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

class Card implements Serializable {
	public String front;
	public String back;
	Card(String _front, String _back) {
		front = _front;
		back = _back;
	}
	
	public String toString() {
		return "Card ["+front+"/"+back+"]";
	}

	private void readObject(ObjectInputStream stream)
		throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
	}
	private void writeObject(ObjectOutputStream stream)
		throws IOException {
		stream.writeObject(front);
		stream.writeObject(back);
	}
	private void readObjectNoData() 
		throws ObjectStreamException {
		front = null;
		back = null;
	}

}