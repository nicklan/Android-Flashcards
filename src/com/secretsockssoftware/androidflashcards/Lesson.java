package com.androidflashcards;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

class Lesson implements Serializable {
	private String name;
	private String description;
	private Card[] cards;
	
	public Lesson(Card[] _cards, String _name, String _description) {
		cards = _cards;
		name = _name;
		description = _description;
	}

	public String name() { return name; }
	public String description() { return description; }

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
		name = (String)stream.readObject();
		description = (String)stream.readObject();
		int len = stream.readInt();
		cards = new Card[len];
		for(int i = 0; i < len;i++)
			cards[i] = (Card)stream.readObject();
	}
	private void writeObject(ObjectOutputStream stream)
		throws IOException {
		stream.writeObject(name);
		stream.writeObject(description);
		stream.writeInt(cards.length);
		for(int i = 0; i < cards.length;i++)
			stream.writeObject(cards[i]);
	}
	private void readObjectNoData() 
		throws ObjectStreamException {
		cards = new Card[0];
	}

}