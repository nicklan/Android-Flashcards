package nick.flashcards;

import android.app.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.ArrayAdapter;
import android.view.View.OnTouchListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.content.Intent;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.FileFilter;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.util.StringTokenizer;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

public class TimedFlashcards extends ListActivity implements Runnable {

	private ProgressDialog pd;
	private TimedFlashcards me;

	/** Called when the activity is first created. */
	@Override
  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		me = this;

		pd = ProgressDialog.show(this, "", 
														 "Checking for flashcards.\nPlease wait...", 
														 true);

		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
		Thread t = new Thread(this);
		t.start();
	}

	
	public void onListItemClick(ListView parent, View v,int position, long id) {
		Intent i = new Intent(this, CardRunner.class);
		i.putExtra("Lesson", (String)(parent.getAdapter().getItem(position)));
		startActivity(i);
	}
	
	// Startup stuff

	private Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				pd.dismiss();
				if (msg.what == 0) {
					setListAdapter(new ArrayAdapter<String>
												 (me,
													android.R.layout.simple_list_item_1, (String[])msg.obj));
					getListView().setTextFilterEnabled(true);
				}
			}
		};


	static protected Lesson parseXML(File file) 
		throws Exception {
		XMLReader xr = XMLReaderFactory.createXMLReader();
		FCParser fcp = new FCParser();
		xr.setContentHandler(fcp);
		xr.setErrorHandler(fcp);
		FileReader r = new FileReader(file);
		xr.parse(new InputSource(r));
		return new Lesson(fcp.getCards());
	}

	static protected Lesson parseCSV(File file)
		throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		ArrayList<Card> cardList = new ArrayList<Card>();
		while((line = br.readLine()) != null) {
			StringTokenizer stok = new StringTokenizer(line,",");
			if (stok.countTokens() < 2) {
				System.err.println("Warning, invalid line: "+line);
				continue;
			}
			if (stok.countTokens() > 2) 
				System.err.println("Warning, too many fields on a line, ignoring all but the first two: "+line);
			cardList.add(new Card(stok.nextToken().trim(),stok.nextToken().trim()));
		}
		return new Lesson(cardList.toArray(new Card[0]));
	}

	public void run() {
		File f = new File("/sdcard/flashcards");
		if (!f.exists()) {
			f.mkdir();
		}
		else if (!f.isDirectory()) {
			// HANDLE THIS CASE
			handler.sendEmptyMessage(0);
		} else {
			File[] files = f.listFiles(new FileFilter() {
					public boolean accept(File f) {
						if (f.getName().endsWith(".xml") ||
								f.getName().endsWith(".csv"))
							return true;
						return false;
					}
				});
			ArrayList<String> al = new ArrayList<String>();
			for(int i = 0;i < files.length;i++) {
				File bf = new File(files[i].getAbsolutePath().substring(0,files[i].getAbsolutePath().lastIndexOf("."))+".bin");
				Lesson l = null;
				if (!bf.exists() ||
						bf.lastModified() < files[i].lastModified()) {
					try {
						FileOutputStream fos = new FileOutputStream(bf);
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						if (files[i].getName().endsWith(".xml"))
							l = parseXML(files[i]);
						else if (files[i].getName().endsWith(".csv"))
							l = parseCSV(files[i]);
						if (l != null) {
							oos.writeObject(l);
							oos.close();
							al.add(files[i].getName().substring(0,files[i].getName().lastIndexOf(".")));
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				} else {
					al.add(files[i].getName().substring(0,files[i].getName().lastIndexOf(".")));
				}
			}
			handler.sendMessage(handler.obtainMessage(0,al.toArray(new String[0])));
		}
	}
}
