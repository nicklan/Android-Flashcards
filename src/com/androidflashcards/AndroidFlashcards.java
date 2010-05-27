package com.androidflashcards;

import android.app.*;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.ArrayAdapter;
import android.view.View.OnTouchListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.TextView;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.FileFilter;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.StringTokenizer;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

public class AndroidFlashcards extends ListActivity implements Runnable {

	private class LessonsCont {
		String[] files;
		String[] names;
		String[] descriptions;
		String[] counts;
	}

	private ProgressDialog pd;
	private AndroidFlashcards me;
	private SharedPreferences lprefs;
	private LessonsCont lessons;

	private static final int FEED_BACK_ID = Menu.FIRST;
	private static final int GET_LESSONS_ID = Menu.FIRST+1;
	private static final int DELETE_ID = Menu.FIRST+2;

	/** Called when the activity is first created. */
	@Override
  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		me = this;

		lprefs = getSharedPreferences("lessonPrefs",0);
		lessons = new LessonsCont();

		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");

		registerForContextMenu(getListView());

		parseLessons();
	}
	
  @Override
	public void onListItemClick(ListView parent, View v,int position, long id) {
		Intent i = new Intent(this, LessonSelect.class);
		i.putExtra("LessonFile", lessons.files[position]);
		i.putExtra("LessonName", lessons.names[position]);
		i.putExtra("LessonDesc", lprefs.getString(lessons.files[position]+"Desc","[no description]"));
		startActivity(i);
	}

	// Options menu handlers
	@Override
  public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, FEED_BACK_ID, 3, R.string.feedback);
		menu.add(0, GET_LESSONS_ID, 3, R.string.get_lessons);
		return true;
	}

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case FEED_BACK_ID: 
      final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
      emailIntent .setType("plain/text");
      emailIntent .putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"nick@afternight.org"});
      emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, "Android Flashcards Feedback");
      startActivity(Intent.createChooser(emailIntent, "Send Feedback..."));
			return true;
		case GET_LESSONS_ID:
			final Intent glIntent = new Intent(this,DownloadableLessonList.class);
			startActivityForResult(glIntent,0);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
  @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
																	ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

  @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			String fn = lessons.files[(int)info.id];
			File f = new File("/sdcard/flashcards/"+fn+".csv");
			if (f.exists())
				f.delete();
			f = new File("/sdcard/flashcards/"+fn+".xml");
			if (f.exists())
				f.delete();
			f = new File("/sdcard/flashcards/"+fn+".bin");
			if (f.exists())
				f.delete();
			parseLessons();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data) {
		switch(requestCode) {
		case 0: // lesson downloader
			if (resultCode == 1) // downloaded something
				parseLessons();
		}
	}

	// Startup stuff

	private Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				pd.dismiss();
				if (msg.what == 0) {
					LessonAdapter ad = new LessonAdapter(lessons);
					setListAdapter(ad);
					getListView().setTextFilterEnabled(true);
				}
			}
		};


	static protected Lesson parseXML(File file, String default_name) 
		throws Exception {
		XMLReader xr = XMLReaderFactory.createXMLReader();
		FCParser fcp = new FCParser();
		xr.setContentHandler(fcp);
		xr.setErrorHandler(fcp);
		FileReader r = new FileReader(file);
		xr.parse(new InputSource(r));
		String name = fcp.getName();
		if (name == "")
			name = default_name;
		String description = fcp.getDesc();
		return new Lesson(fcp.getCards(),name,description);
	}

	static protected Lesson parseCSV(File file, String default_name)
		throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		ArrayList<Card> cardList = new ArrayList<Card>();
		boolean first = true;
		String name = default_name;
		String desc = "[no description]";
		while((line = br.readLine()) != null) {
			StringTokenizer stok = new StringTokenizer(line,",");
			if (stok.countTokens() < 2) {
				System.err.println("Warning, invalid line: "+line);
				continue;
			}
			if (stok.countTokens() > 2) 
				System.err.println("Warning, too many fields on a line, ignoring all but the first two: "+line);
			if (first) {
				name = stok.nextToken().trim();
				desc = stok.nextToken().trim();
				first = false;
			} else {
				String front = stok.nextToken().trim();
				front = front.replaceAll("\\\\n","\n");
				String back = stok.nextToken().trim();
				back = back.replaceAll("\\\\n","\n");
				cardList.add(new Card(front,back));
			}
		}
		return new Lesson(cardList.toArray(new Card[0]),name,desc);
	}

	private void parseLessons() {
		pd = ProgressDialog.show(this, "", 
														 "Checking for flashcards.\nPlease wait...", 
														 true);
		Thread t = new Thread(this);
		t.start();
	}

	private void ensureInstructions() {
		File f = new File("/sdcard/flashcards/android_flashcards_instructions.xml");
		if (!f.exists()) {
			try {
				BufferedInputStream bis = new BufferedInputStream(getResources().openRawResource(R.raw.android_flashcards_instructions));
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
				int i;
				while ((i = bis.read()) != -1)
					bos.write(i);
				bos.close();
				bis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {
		File f = new File("/sdcard/flashcards");
		SharedPreferences.Editor editor = null;
		if (!f.exists()) {
			f.mkdir();
		}
		else if (!f.isDirectory()) {
			// HANDLE THIS CASE
			handler.sendEmptyMessage(0);
		} else {
			ensureInstructions();
			File[] files = f.listFiles(new FileFilter() {
					public boolean accept(File f) {
						if (f.getName().endsWith(".xml") ||
								f.getName().endsWith(".csv"))
							return true;
						return false;
					}
				});
			ArrayList<String> al_files = new ArrayList<String>();
			ArrayList<String> al_names = new ArrayList<String>();
			ArrayList<String> al_descriptions = new ArrayList<String>();
			ArrayList<String> al_counts = new ArrayList<String>();
			for(int i = 0;i < files.length;i++) {
				File bf = new File(files[i].getAbsolutePath().substring(0,files[i].getAbsolutePath().lastIndexOf("."))+".bin");
				Lesson l = null;
				String fbase = files[i].getName().substring(0,files[i].getName().lastIndexOf("."));
				if (!bf.exists() ||
						bf.lastModified() < files[i].lastModified() ||
						!(lprefs.contains(fbase+"Name") &&
							lprefs.contains(fbase+"Desc") &&
							lprefs.contains(fbase+"Count"))) {
					try {
						FileOutputStream fos = new FileOutputStream(bf);
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						if (files[i].getName().endsWith(".xml"))
							l = parseXML(files[i],fbase);
						else if (files[i].getName().endsWith(".csv"))
							l = parseCSV(files[i],fbase);
						if (l != null) {
							oos.writeObject(l);
							oos.close();
							al_files.add(fbase);
							al_names.add(l.name());
							al_descriptions.add(l.description());
							al_counts.add("Cards: "+l.cardCount());
						}
						if (editor == null)
							editor = lprefs.edit();
						editor.putString(fbase+"Name",l.name());
						editor.putString(fbase+"Desc",l.description());
						editor.putString(fbase+"Count","Cards: "+l.cardCount());
						editor.commit();
					} catch(Exception e) {
						e.printStackTrace();
					}
				} else {
					al_files.add(fbase);
					al_names.add(lprefs.getString(fbase+"Name","[No Name]"));
					al_descriptions.add(lprefs.getString(fbase+"Desc","[No Description]"));
					al_counts.add(lprefs.getString(fbase+"Count","[No Count"));
				}
			}
			lessons.files = al_files.toArray(new String[0]);
			lessons.names = al_names.toArray(new String[0]);
			lessons.descriptions = al_descriptions.toArray(new String[0]);
			lessons.counts = al_counts.toArray(new String[0]);
			handler.sendMessage(handler.obtainMessage(0));
		}
	}


	// ListAdapter
	class LessonAdapter extends ArrayAdapter<String> {
		LessonsCont cont;
		LessonAdapter(LessonsCont _cont) {
			super(AndroidFlashcards.this, R.layout.lesson_list, _cont.names);
			cont = _cont;
		}
		
		public View getView(int position, View convertView,	ViewGroup parent) {
			View row=convertView;
			if (row==null) {
				LayoutInflater inflater=getLayoutInflater();
				row=inflater.inflate(R.layout.lesson_list, parent, false);
				row.setTag(R.id.list_label, row.findViewById(R.id.list_label));
				row.setTag(R.id.list_desc, row.findViewById(R.id.list_desc));
				row.setTag(R.id.list_count, row.findViewById(R.id.list_count));
			}
			((TextView)row.getTag(R.id.list_label)).setText(getItem(position));
			((TextView)row.getTag(R.id.list_desc)).setText(cont.descriptions[position]);
			((TextView)row.getTag(R.id.list_count)).setText(cont.counts[position]);
			return(row);
		}
	}

}
