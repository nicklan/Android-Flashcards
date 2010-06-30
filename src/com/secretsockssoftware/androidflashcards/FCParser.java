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

class FCParser extends DefaultHandler {

	private ArrayList<Card> cardList = new ArrayList<Card>();

	public FCParser() {
		super();
	}

	public Card[] getCards() {
		return cardList.toArray(new Card[0]);
	}

	public String getName() { return nbuf.toString(); }
	public String getDesc() { return dbuf.toString(); }

	////////////////////////////////////////////////////////////////////
	// Event handlers.
	////////////////////////////////////////////////////////////////////

	private boolean inCard = false;
	private boolean inFront = false;
	private boolean inBack = false;
	private boolean inName = false;
	private boolean inDesc = false;

	private StringBuffer fbuf = new StringBuffer();
	private StringBuffer bbuf = new StringBuffer();
	private StringBuffer nbuf = new StringBuffer();
	private StringBuffer dbuf = new StringBuffer("[no description]");

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

		if (qn.equals("card")) {
			if (inCard) 
				Log.e(AndroidFlashcards.TAG,"Got card element inside a card");
			else
				inCard = true;
		}
		else if (qn.equals("frontside")) {
			if (!inCard) {
				Log.e(AndroidFlashcards.TAG,"Got frontside element NOT inside a card");
				return;
			}
			if (inFront) 
				Log.e(AndroidFlashcards.TAG,"Got frontside element inside a frontside");
			else
				inFront = true;
		}
		else if (qn.equals("backside") || qn.equals("reverseside")) {
			if (!inCard) {
				Log.e(AndroidFlashcards.TAG,"Got backside element NOT inside a card");
				return;
			}
			if (inBack) 
				Log.e(AndroidFlashcards.TAG,"Got backside element inside a backside");
			else
				inBack = true;
		}
		else if (qn.equals("name")) {
			if (inCard || inFront || inBack) {
				Log.e(AndroidFlashcards.TAG,"Got a name inside a card somewhere");
				return;
			}
			inName = true;
		}
		else if (qn.equals("description")) {
			if (inCard || inFront || inBack) {
				Log.e(AndroidFlashcards.TAG,"Got a description inside a card somewhere");
				return;
			}
			inDesc = true;
			dbuf.setLength(0);
		}
	}


	public void endElement (String uri, String name, String qName) {
		String qn = null;
		if (qName.equals("")) 
			qn = name.toLowerCase();
		else
			qn = qName.toLowerCase();

		if (qn.equals("card")) {
			if (!inCard) 
				Log.e(AndroidFlashcards.TAG,"Ended a card element not inside a card");
			else {
				if (fbuf.length() == 0 ||
						bbuf.length() == 0) {
					Log.e(AndroidFlashcards.TAG,"Got a card with empty front or back");
				} else {
					cardList.add(new Card(fbuf.toString().trim(),bbuf.toString().trim()));
				}
			}
			inCard = false;
			fbuf.setLength(0);
			bbuf.setLength(0);
		}
		else if (qn.equals("frontside")) {
			if (!inCard) {
				Log.e(AndroidFlashcards.TAG,"Got frontside end NOT inside a card");
				return;
			}
			if (!inFront) 
				Log.e(AndroidFlashcards.TAG,"Got frontside element NOT inside a frontside");
			else
				inFront = false;
		}
		else if (qn.equals("backside") || qn.equals("reverseside")) {
			if (!inCard) {
				Log.e(AndroidFlashcards.TAG,"Got backside element NOT inside a card");
				return;
			}
			if (!inBack) 
				Log.e(AndroidFlashcards.TAG,"Got backside element NOT inside a backside");
			else
				inBack = false;
		}
		else if (qn.equals("name")) {
			if (!inName)
				Log.e(AndroidFlashcards.TAG,"Got end name not inside a name");
			else
				inName = false;
		}
		else if (qn.equals("description")) {
			if (!inDesc)
				Log.e(AndroidFlashcards.TAG,"Got end description not inside a description.");
			else
				inDesc = false;
		}
	}


	public void characters (char ch[], int start, int len)
	{
		if (inCard) {
			if (inFront)
				fbuf.append(ch,start,len);
			if (inBack)
				bbuf.append(ch,start,len);
		}
		else if (inName) 
			nbuf.append(ch,start,len);
		else if (inDesc)
			dbuf.append(ch,start,len);
	}

}
