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

import android.app.*;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.ArrayAdapter;
import android.view.View.OnTouchListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ImageView;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.FilenameFilter;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

import au.com.bytecode.opencsv.CSVParser;


public class AndroidFlashcards extends ListActivity implements Runnable {

	private ProgressDialog pd;
	private AndroidFlashcards me;
	private SharedPreferences lprefs;
	private LessonListItem[] lessons = null;

	private static final int FEED_BACK_ID = Menu.FIRST;
	private static final int GET_LESSONS_ID = Menu.FIRST+1;
	private static final int DELETE_ID = Menu.FIRST+2;
	private static final int RESCAN_ID = Menu.FIRST+3;

	public static final String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
	private final String rootDir = sdDir+File.separator+"flashcards";
	private String curDir = rootDir;

	public static final String TAG = "AndroidFlashcards";

	/* Called when the activity is first created. */
	@Override
  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		me = this;

		lprefs = getSharedPreferences("lessonPrefs",0);

		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");

		registerForContextMenu(getListView());

		parseLessons();
	}

	// This (and run()) are done in another thread so the ProgressDialog can be shown
	private void parseLessons() {
		pd = ProgressDialog.show(this, "", 
														 "Checking for flashcards.\nPlease wait...", 
														 true);
		Thread t = new Thread(this);
		t.start();
	}

	public void run() {
		File f = new File(curDir);
		if (!f.exists())  {
			if (curDir.equals(rootDir)) {
				if (f.mkdirs())
					run();
				else
					handler.sendMessage(handler.obtainMessage(1,"could not create root directory: "+curDir));
			}
			else
				handler.sendMessage(handler.obtainMessage(1,curDir+" does not exist!"));
		}
		else if (!f.isDirectory()) 
			handler.sendMessage(handler.obtainMessage(1,curDir+" exists, but is not a directory!"));
		else {
			ensureInstructions();
			lessons = loadDir(f,false);
			if (lessons == null)
				handler.sendMessage(handler.obtainMessage(1,"Sorry, an error occured while loading this directory"));				
			else
				handler.sendEmptyMessage(0);
		}
	}

	
  @Override
	public void onListItemClick(ListView parent, View v,int position, long id) {
		Intent i = new Intent(this, LessonSelect.class);
		LessonListItem l = lessons[position];
		if (l.isDir) {
			curDir = l.file;
			parseLessons();
		} else {
			i.putExtra("LessonFile", l.file);
			i.putExtra("LessonName", l.name);
			i.putExtra("LessonDesc", l.desc);
			i.putExtra("LessonCount", l.count);
			startActivity(i);
		}
	}

	// Options menu handlers
	@Override
  public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, GET_LESSONS_ID, 0, R.string.get_lessons);
		menu.add(0, RESCAN_ID, 1, R.string.rescan);
		menu.add(0, FEED_BACK_ID, 1, R.string.feedback);
		return true;
	}

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case FEED_BACK_ID: 
      final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
      emailIntent .setType("plain/text");
      emailIntent .putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"androidflashcards@secretsockssoftware.com"});
      emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, "Android Flashcards Feedback");
      startActivity(Intent.createChooser(emailIntent, "Send Feedback..."));
			return true;
		case GET_LESSONS_ID:
			final Intent glIntent = new Intent(this,DownloadableLessonList.class);
			startActivityForResult(glIntent,0);
			return true;
		case RESCAN_ID:
			parseLessons();
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
	
	static private boolean deleteDir(File dir) {
    if (dir.exists()) {
      File[] files = dir.listFiles();
      for (int i=0; i<files.length; i++)
				if (files[i].isDirectory()) 
					deleteDir(files[i]);
				else 
					files[i].delete();
    }
    return dir.delete();
  }

  @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			final LessonListItem lesson_item = lessons[(int)info.id];
			AlertDialog alertDialog = new AlertDialog.Builder(me).create();
			alertDialog.setTitle("Confirm Delete");
			if (lesson_item.isDir)
				alertDialog.setMessage("Are you sure you want to delete:\n"+lesson_item.name+"\n\nAll lessons in the directory will be deleted");				
			else
				alertDialog.setMessage("Are you sure you want to delete the lesson:\n"+lesson_item.name);
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						File f = new File(lesson_item.file);
						if (lesson_item.isDir) {
							deleteDir(f);
						} else {
							if (f.exists())
								f.delete();
							f = new File(lesson_item.source);
							if (f.exists())
								f.delete();
						}
						parseLessons();
					}});
			alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}});
			alertDialog.show();
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


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && 
				!curDir.equals(rootDir)) {
			File f = new File(curDir);
			curDir = f.getParent();
			parseLessons();
			return true;
    }
    return super.onKeyDown(keyCode, event);
	}


	// parsing stuff
	private Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0: {
					pd.dismiss();
					LessonAdapter ad = new LessonAdapter(lessons);
					setListAdapter(ad);
					getListView().setTextFilterEnabled(true);
					break;
				}
				case 1: {
					pd.dismiss();
					AlertDialog alertDialog = new AlertDialog.Builder(me).create();
					alertDialog.setTitle("Error");
					alertDialog.setMessage("Sorry, but an error occured trying to read the directory:\n\n"+msg.obj);
					alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								setResult(0);
								finish();
								return;
							} });
					alertDialog.show();
					break;
				} 
				case 2: 
					pd.setMessage((String)msg.obj);
					break;
				case 3: {
					AlertDialog alertDialog = new AlertDialog.Builder(me).create();
					alertDialog.setTitle("Empty File");
					alertDialog.setMessage("Sorry, but "
																 +msg.obj+
																 " is an empty file and will be removed.\n\nThis may be due to a download error, you can try downloading the lesson again.");
					alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}});	
					alertDialog.show();
					break;
				}
				default:
					Log.e(TAG,"Invalid message recieved by handler: "+msg.what);
				}
			}
		};


	private LessonListItem[] loadDir(File dir,boolean ignoreCache) {
		if (!dir.isDirectory())
			return null;

		SharedPreferences.Editor editor = null;
		File cache = new File(dir,".dircache");
		LessonListItem[] ret = null;
		if (ignoreCache ||
				!cache.exists() ||
				(cache.lastModified() < dir.lastModified())) {
			ArrayList<LessonListItem> items = new ArrayList<LessonListItem>();
			if (!dir.getAbsolutePath().equals(rootDir))
				items.add(new LessonListItem(dir.getParent(),null,"..","Go Back","",true));
			String[] files = dir.list(new FilenameFilter() {
					public boolean accept(File dir,String name) {
						File f = new File(dir,name);
						if (f.isDirectory())
							return true;
						else if (name.endsWith(".xml") ||
										 name.endsWith(".csv"))
							return true;
						return false;
					}
				});
			for(int i = 0;i < files.length;i++) {
				File curF = new File(dir.getAbsolutePath()+File.separator+files[i]);
				if (curF.isDirectory()) 
					items.add(new LessonListItem(curF.getAbsolutePath(),
																			 null,
																			 files[i],
																			 "Directory",
																			 "",true));
				else {
					String fbase = curF.getName().substring(0,curF.getName().lastIndexOf("."));
					File bf = new File(curF.getAbsolutePath().substring(0,curF.getAbsolutePath().lastIndexOf("."))+".bin");
					if (!bf.exists() ||
							bf.lastModified() < curF.lastModified() ||
							!(lprefs.contains(fbase+"Name") &&
								lprefs.contains(fbase+"Desc") &&
								lprefs.contains(fbase+"Count"))) {
						LessonListItem item = parseLesson(curF,bf,fbase);
						if (item != null) {
							items.add(item);
							if (editor == null)
								editor = lprefs.edit();
							editor.putString(fbase+"Name",item.name);
							editor.	putString(fbase+"Desc",item.desc);
							editor.putString	(fbase+"Count",item.count);
							editor.commit();
						}
					}
					else {
						items.add(new LessonListItem
											(bf.getAbsolutePath(),
											 curF.getAbsolutePath(),
											 lprefs.getString(fbase+"Name","[No Name]"),
											 lprefs.getString(fbase+"Desc","[No Description]"),
											 lprefs.getString(fbase+"Count","[No Count]"),false));
					}
				}
			}
			ret = items.toArray(new LessonListItem[0]);
			java.util.Arrays.sort(ret);
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cache));
				oos.writeInt(ret.length);
				for (int i = 0;i < ret.length;i++)
					oos.writeObject(ret[i]);
				oos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ret;
		} else {
			// read the cache
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cache));
				int cnt = ois.readInt();
				ret = new LessonListItem[cnt];
				for (int i = 0;i<cnt;i++)
					ret[i] = (LessonListItem)ois.readObject();
			} catch (Exception e) {
				e.printStackTrace();
				return loadDir(dir,true);
			}
		}
		return ret;
	}

	private LessonListItem parseLesson(File f,File bf,String fbase) {
		Lesson l = null;
		LessonListItem lli = null;
		handler.sendMessage(handler.obtainMessage(2,"Parsing: "+fbase));
		if (f.length() <= 0) {
			// clean up any empty files	
			if (f.delete())
				handler.sendMessage(handler.obtainMessage(3,f.getName()));
			return null;
		}
		try {
			FileOutputStream fos = new FileOutputStream(bf);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			if (f.getName().endsWith(".xml"))
				l = parseXML(f,fbase);
			else if (f.getName().endsWith(".csv"))
				l = parseCSV(f,fbase);
			if (l != null) {
				oos.writeObject(l);
				oos.close();
				lli = new LessonListItem(bf.getAbsolutePath(),
																 f.getAbsolutePath(),
																 l.name(),
																 l.description(),
																 "Cards: "+l.cardCount(),false);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return lli;
	}


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
		ArrayList<Card> cardList = new ArrayList<Card>();
		BufferedReader br = new BufferedReader(new FileReader(file));

		boolean first = true;
		String name = default_name;
		String desc = "[no description]";
		String line;

    String[] toks;
		CSVParser parser = new CSVParser();
    while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() <= 0)
				continue;
			try {
				toks = parser.parseLine(line);
			} catch(IOException e) {
				Log.e(TAG,"Invalid line: "+line+" ("+e.getMessage()+")");
				continue;
			}
			if (toks.length < 2) {
				Log.e(TAG,"Warning, invalid line, not enough fields: "+line);
				continue;
			}
			if (toks.length > 2) 
				Log.e(TAG,"Warning, too many fields on a line, ignoring all but the first two: "+line);
			if (first) {
				name = toks[0].trim();
				desc = toks[1].trim();
				first = false;
			} else {
				String front = toks[0].trim();
				front = front.replaceAll("\\\\n","\n");
				String back = toks[1].trim();
				back = back.replaceAll("\\\\n","\n");
				cardList.add(new Card(front,back));
			}
    }
		return new Lesson(cardList.toArray(new Card[0]),name,desc);
	}

	private void ensureInstructions() {
		File f = new File(sdDir+File.separator+"flashcards/android_flashcards_instructions.xml");
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

	// ListAdapter
	class LessonAdapter extends ArrayAdapter<LessonListItem> {
		LessonListItem[] items;
		LessonAdapter(LessonListItem[] _items) {
			super(AndroidFlashcards.this, R.layout.lesson_list, _items);
			items = _items;
		}
		
		public View getView(int position, View convertView,	ViewGroup parent) {
			View row=convertView;
			if (row==null) {
				LayoutInflater inflater=getLayoutInflater();
				row=inflater.inflate(R.layout.lesson_list, parent, false);
				row.setTag(R.id.list_label, row.findViewById(R.id.list_label));
				row.setTag(R.id.list_desc, row.findViewById(R.id.list_desc));
				row.setTag(R.id.list_count, row.findViewById(R.id.list_count));
				row.setTag(R.id.list_icon, row.findViewById(R.id.list_icon));
			}
			((TextView)row.getTag(R.id.list_label)).setText(items[position].name);
			((TextView)row.getTag(R.id.list_desc)).setText(items[position].desc);
			((TextView)row.getTag(R.id.list_count)).setText(items[position].count);
			ImageView icon=(ImageView)row.getTag(R.id.list_icon);
			if (items[position].isDir)
				icon.setImageResource(R.drawable.folder);
			else
				icon.setImageResource(R.drawable.list_icon);
			return(row);
		}
	}

}
