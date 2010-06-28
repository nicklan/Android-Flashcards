package com.androidflashcards;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class LessonListItem implements Serializable,Comparable<LessonListItem> {
	String file;
	String name;
	String desc;
	String count;
	boolean isDir;

	public LessonListItem(String f,
												String n,
												String d,
												String c,
												boolean id) {
		file = f;
		name = n;
		desc = d;
		count = c;
		isDir = id;
	}

	private void readObject(ObjectInputStream stream)
		throws IOException, ClassNotFoundException {
		file = (String)stream.readObject();
		name = (String)stream.readObject();
		desc = (String)stream.readObject();
		count = (String)stream.readObject();
		isDir = stream.readBoolean();
	}
	private void writeObject(ObjectOutputStream stream)
		throws IOException {
		stream.writeObject(file);
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