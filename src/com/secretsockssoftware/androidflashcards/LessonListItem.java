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

public class LessonListItem implements Serializable,Comparable<LessonListItem> {
	String file;
	String source;
	String name;
	String desc;
	String count;
	boolean isDir;

	public LessonListItem(String f,
												String s,
												String n,
												String d,
												String c,
												boolean id) {
		file = f;
		source = s;
		name = n;
		desc = d;
		count = c;
		isDir = id;
	}

	private void readObject(ObjectInputStream stream)
		throws IOException, ClassNotFoundException {
		file = (String)stream.readObject();
		source = (String)stream.readObject();
		name = (String)stream.readObject();
		desc = (String)stream.readObject();
		count = (String)stream.readObject();
		isDir = stream.readBoolean();
	}
	private void writeObject(ObjectOutputStream stream)
		throws IOException {
		stream.writeObject(file);
		stream.writeObject(source);
		stream.writeObject(name);
		stream.writeObject(desc);
		stream.writeObject(count);
		stream.writeBoolean(isDir);
	}
	private void readObjectNoData() 
		throws ObjectStreamException {
		file = name = desc = count = null;
	}

	public int compareTo(LessonListItem l) {
		return name.compareTo(l.name);
	}
}