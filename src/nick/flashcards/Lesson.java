package nick.flashcards;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

class Lesson implements Serializable {
	private Card[] cards;
	
	public Lesson(Card[] _cards) {
		cards = _cards;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Lesson: \n");
		for(int i = 0; i<cards.length;i++) {
			sb.append("  "+cards[i]+"\n");
		}
		return sb.toString();
	}

	public int pickCard() {
		return (int)(Math.random()*cards.length);
	}

	public Card getCard(int i) {
		return cards[i];
	}

	public Card getCard(Integer i) {
		return cards[i.intValue()];
	}

	public int cardCount() {
		return cards.length;
	}

	private void readObject(ObjectInputStream stream)
		throws IOException, ClassNotFoundException {
		int len = stream.readInt();
		cards = new Card[len];
		for(int i = 0; i < len;i++)
			cards[i] = (Card)stream.readObject();
	}
	private void writeObject(ObjectOutputStream stream)
		throws IOException {
		stream.writeInt(cards.length);
		for(int i = 0; i < cards.length;i++)
			stream.writeObject(cards[i]);
	}
	private void readObjectNoData() 
		throws ObjectStreamException {
		cards = new Card[0];
	}

}