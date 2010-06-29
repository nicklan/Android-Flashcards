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
		stream.writeObject(back);
		stream.writeObject(front);
	}
	private void readObjectNoData() 
		throws ObjectStreamException {
		front = null;
		back = null;
	}

}