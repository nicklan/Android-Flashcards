import java.io.FileReader;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;


public class FCParser extends DefaultHandler
{

	class Card {
		String front;
		String back;
		Card(String _front, String _back) {
			front = _front;
			back = _back;
		}

		public String toString() {
			return "Card ["+front+"/"+back+"]";
		}
	}
	
	public static void main (String args[])
		throws Exception
	{
		XMLReader xr = XMLReaderFactory.createXMLReader();
		FCParser handler = new FCParser();
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);

		// Parse each file provided on the
		// command line.
		for (int i = 0; i < args.length; i++) {
	    FileReader r = new FileReader(args[i]);
	    xr.parse(new InputSource(r));
		}
	}


	public FCParser ()
	{
		super();
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
		String qn = qName.toLowerCase();

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
		String qn = qName.toLowerCase();

		if (qn.equals("card")) {
			if (!inCard) 
				System.err.println("Ended a card element not inside a card");
			else {
				if (fbuf.length() == 0 ||
						bbuf.length() == 0) {
					System.err.println("Got a card with empty front or back");
				} else {
					Card c = new Card(fbuf.toString().trim(),bbuf.toString().trim());
					System.out.println(c);
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
