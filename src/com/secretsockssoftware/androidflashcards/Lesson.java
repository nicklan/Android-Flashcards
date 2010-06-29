/*
* Copyright 2010 Nick Lanham
*
* This file is part of "AndroidFlashcards".
*
* "AndroidFlashcards" is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* "AndroidFlashcards" is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with "AndroidFlashcards". If not, see <http://www.gnu.org/licenses/>.
*/

package com.secretsockssoftware.androidflashcards;

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