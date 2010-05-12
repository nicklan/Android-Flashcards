package nick.flashcards;

import java.io.FileReader;
import java.io.File;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

class FCParser extends DefaultHandler {

	private ArrayList<Card> cardList = new ArrayList<Card>();

	public FCParser() {
		super();
	}

	public Card[] getCards() {
		return cardList.toArray(new Card[0]);
	}

	////////////////////////////////////////////////////////////////////
	// Event handlers.
	////////////////////////////////////////////////////////////////////

	private boolean inCard = false;
	private boolean inFront = false;
	private boolean inBack = false;

	private StringBuffer fbuf = new StringBuffer();
	private StringBuffer bbuf = new StringBuffer();

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
				System.err.println("Got card element inside a card");
			else
				inCard = true;
		}
		else if (qn.equals("frontside")) {
			if (!inCard) {
				System.err.println("Got frontside element NOT inside a card");
				return;
			}
			if (inFront) 
				System.err.println("Got frontside element inside a frontside");
			else
				inFront = true;
		}
		else if (qn.equals("backside")) {
			if (!inCard) {
				System.err.println("Got backside element NOT inside a card");
				return;
			}
			if (inBack) 
				System.err.println("Got backside element inside a backside");
			else
				inBack = true;
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
				System.err.println("Ended a card element not inside a card");
			else {
				if (fbuf.length() == 0 ||
						bbuf.length() == 0) {
					System.err.println("Got a card with empty front or back");
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
				System.err.println("Got frontside end NOT inside a card");
				return;
			}
			if (!inFront) 
				System.err.println("Got frontside element NOT inside a frontside");
			else
				inFront = false;
		}
		else if (qn.equals("backside")) {
			if (!inCard) {
				System.err.println("Got backside element NOT inside a card");
				return;
			}
			if (!inBack) 
				System.err.println("Got backside element NOT inside a backside");
			else
				inBack = false;
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
	}

}
