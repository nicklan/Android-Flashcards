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

import java.io.FileReader;
import java.io.File;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import android.util.Log;

class LessonXMLParser extends DefaultHandler {

	private ArrayList<DownloadableLessonList.AvailLesson> lessonList = new ArrayList<DownloadableLessonList.AvailLesson>();

	public LessonXMLParser() {
		super();
	}

	public ArrayList<DownloadableLessonList.AvailLesson> getLessons() {
		return lessonList;
	}

	////////////////////////////////////////////////////////////////////
	// Event handlers.
	////////////////////////////////////////////////////////////////////

	private boolean inLesson = false;
	private boolean inName = false;
	private boolean inDesc = false;
	private boolean inURL = false;
	private boolean inFilter = false;
	private boolean inTarget = false;
	private boolean inEnc = false;

	private StringBuffer nbuf = new StringBuffer();
	private StringBuffer dbuf = new StringBuffer();
	private StringBuffer ubuf = new StringBuffer();
	private StringBuffer fbuf = new StringBuffer();
	private StringBuffer tbuf = new StringBuffer();
	private StringBuffer ebuf = new StringBuffer();

	public void startDocument ()
	{}


	public void endDocument ()
	{}


	public void startElement (String uri, String name,
														String qName, Attributes atts)
	{
		String qn = null;
		if (qName.equals("")) 
			qn = name.toLowerCase();
		else
			qn = qName.toLowerCase();

		if (qn.equals("lessons")) {}
		else if (qn.equals("lesson")) {
			if (inLesson) 
				Log.e(AndroidFlashcards.TAG,"Got lesson element inside a lesson");
			else
				inLesson = true;
		}
		else if (qn.equals("name")) {
			if (!inLesson) {
				Log.e(AndroidFlashcards.TAG,"Got name element NOT inside a lesson");
				return;
			}
			if (inName || inDesc || inURL || inFilter || inTarget || inEnc)
				Log.e(AndroidFlashcards.TAG,"Unexpected name element");
			else {
				inName = true;
				nbuf.setLength(0);
			}
		}
		else if (qn.equals("description")) {
			if (!inLesson) {
				Log.e(AndroidFlashcards.TAG,"Got description element NOT inside a lesson");
				return;
			}
			if (inName || inDesc || inURL || inFilter || inTarget || inEnc) 
				Log.e(AndroidFlashcards.TAG,"Unexpected description element");
			else {
				inDesc = true;
				dbuf.setLength(0);
			}
		}
		else if (qn.equals("url")) {
			if (!inLesson) {
				Log.e(AndroidFlashcards.TAG,"Got url element NOT inside a lesson");
				return;
			}
			if (inName || inDesc || inURL || inFilter || inTarget || inEnc) 
				Log.e(AndroidFlashcards.TAG,"Unexpected url element");
			else {
				inURL = true;
				ubuf.setLength(0);
			}
		}
		else if (qn.equals("filter")) {
			if (!inLesson) {
				Log.e(AndroidFlashcards.TAG,"Got filter element NOT inside a lesson");
				return;
			}
			if (inName || inDesc || inURL || inFilter || inTarget || inEnc)
				Log.e(AndroidFlashcards.TAG,"Unexpected filter element");
			else {
				inFilter = true;
				fbuf.setLength(0);
			}
		}
		else if (qn.equals("target")) {
			if (!inLesson) {
				Log.e(AndroidFlashcards.TAG,"Got target element NOT inside a lesson");
				return;
			}
			if (inName || inDesc || inURL || inFilter || inTarget || inEnc)
				Log.e(AndroidFlashcards.TAG,"Unexpected target element");
			else {
				inTarget = true;
				tbuf.setLength(0);
			}
		}
		else if (qn.equals("encoding")) {
			if (!inLesson) {
				Log.e(AndroidFlashcards.TAG,"Got encoding element NOT inside a lesson");
				return;
			}
			if (inName || inDesc || inURL || inFilter || inTarget || inEnc)
				Log.e(AndroidFlashcards.TAG,"Unexpected encoding element");
			else {
				inEnc = true;
				ebuf.setLength(0);
			}
		}
		else {
			Log.e(AndroidFlashcards.TAG,"Unexpected element: "+qn);
		}
	}


	public void endElement (String uri, String name, String qName) {
		String qn = null;
		if (qName.equals("")) 
			qn = name.toLowerCase();
		else
			qn = qName.toLowerCase();

		if (qn.equals("lessons")) {}
		else if (qn.equals("lesson")) {
			if (!inLesson) 
				Log.e(AndroidFlashcards.TAG,"Ended a lesson element not inside a lesson");
			else {
				if (nbuf.length() == 0 ||
						ubuf.length() == 0) {
					Log.e(AndroidFlashcards.TAG,"Got a lesson with no name or url");
				} else {
					DownloadableLessonList.AvailLesson less = new DownloadableLessonList.AvailLesson();
					less.name = nbuf.toString().trim();
					less.desc = dbuf.toString().trim();
					less.url = ubuf.toString().trim();
					if (fbuf.length() != 0) 
						less.filter = fbuf.toString().trim();
					if (tbuf.length() != 0) 
						less.target = tbuf.toString().trim();
					if (ebuf.length() != 0) 
						less.encoding = ebuf.toString().trim();
					lessonList.add(less);
				}
			}
			inLesson = false;
			nbuf.setLength(0);
			dbuf.setLength(0);
			ubuf.setLength(0);
			fbuf.setLength(0);
			tbuf.setLength(0);
			ebuf.setLength(0);
		}
		else if (qn.equals("name")) {
			if (!inLesson) {
				Log.e(AndroidFlashcards.TAG,"Got name end NOT inside a lesson");
				return;
			}
			if (!inName) 
				Log.e(AndroidFlashcards.TAG,"Got end name element NOT inside a name");
			else
				inName = false;
		}
		else if (qn.equals("description")) {
			if (!inLesson) {
				Log.e(AndroidFlashcards.TAG,"Got end description element NOT inside a lesson");
				return;
			}
			if (!inDesc)
				Log.e(AndroidFlashcards.TAG,"Got end description NOT inside a description.");
			else
				inDesc = false;
		}
		else if (qn.equals("url")) {
			if (!inLesson) {
				Log.e(AndroidFlashcards.TAG,"Got end url element NOT inside a lesson");
				return;
			}
			if (!inURL) 
				Log.e(AndroidFlashcards.TAG,"Got end url element NOT inside a url");
			else
				inURL = false;
		}
		else if (qn.equals("filter")) {
			if (!inLesson) {
				Log.e(AndroidFlashcards.TAG,"Got end filter element NOT inside a lesson");
				return;
			}
			if (!inFilter) 
				Log.e(AndroidFlashcards.TAG,"Got end filter element NOT inside a url");
			else
				inFilter = false;
		}
		else if (qn.equals("target")) {
			if (!inLesson) {
				Log.e(AndroidFlashcards.TAG,"Got end target element NOT inside a lesson");
				return;
			}
			if (!inTarget) 
				Log.e(AndroidFlashcards.TAG,"Got end target element NOT inside a url");
			else
				inTarget = false;
		}
		else if (qn.equals("encoding")) {
			if (!inLesson) {
				Log.e(AndroidFlashcards.TAG,"Got end encoding element NOT inside a lesson");
				return;
			}
			if (!inEnc) 
				Log.e(AndroidFlashcards.TAG,"Got end encoding element NOT inside a url");
			else
				inEnc = false;
		}
	}


	public void characters (char ch[], int start, int len) {
		if (inLesson) {
			if (inName)
				nbuf.append(ch,start,len);
			if (inDesc)
				dbuf.append(ch,start,len);
			if (inURL)
				ubuf.append(ch,start,len);
			if (inFilter)
				fbuf.append(ch,start,len);
			if (inTarget)
				tbuf.append(ch,start,len);
			if (inEnc)
				ebuf.append(ch,start,len);
		}
	}

}
