package nick.flashcards;

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
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.io.File;
import java.io.FileReader;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

public class DownloadableLessonList extends ListActivity implements Runnable {

	static class AvailLesson {
		String name;
		String desc;
		String url;

		public String toString() {
			return name;
		}
	}

	private ArrayList<AvailLesson> availLessons;
	private ProgressDialog pd;
	private DownloadableLessonList me;

	private static final int RELOAD_ID = Menu.FIRST;

	/** Called when the activity is first created. */
	@Override
  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		me = this;
		setResult(0);
		downloadLessons();
	}
	
  @Override
	public void onListItemClick(ListView parent, View v,int position, long id) {
		AvailLesson l = availLessons.get(position);
		Intent i = new Intent(this, LessonDownload.class);
		i.putExtra("LessonName", l.name);
		i.putExtra("LessonDesc", l.desc);
		i.putExtra("LessonUrl", l.url);
		startActivityForResult(i,0);
	}

	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data) {
		switch(requestCode) {
		case 0: // lesson downloader
			setResult(resultCode);
		}
	}

	// Options menu handlers
	@Override
  public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, RELOAD_ID, 3, R.string.reload);
		return true;
	}

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case RELOAD_ID: 
			downloadLessons();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	// Startup stuff
	private Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				pd.dismiss();
				switch (msg.what) {
				case 0: {
					availLessons = (ArrayList<AvailLesson>)(msg.obj);
					setListAdapter(new ArrayAdapter<AvailLesson>
												 (me,
													android.R.layout.simple_list_item_1, availLessons));
					getListView().setTextFilterEnabled(true);
					return;
				}	
				case 1: {
					AlertDialog alertDialog = new AlertDialog.Builder(me).create();
					alertDialog.setTitle("Error");
					alertDialog.setMessage("Sorry, but an error occured trying to download the list of available lessons:\n"+msg.obj);
					alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								finish();
								return;
							} });
					alertDialog.show();
				}
				}
			}
		};

	private void downloadLessons() {
		pd = ProgressDialog.show(this, "", 
														 "Downloading lesson list.\nPlease wait...", 
														 true);
		Thread t = new Thread(this);
		t.start();
	}

	// Pull down list of lessons
	public void run() {
		try {
			URL url = new URL("http://nick.afternight.org/tfc/lesson_list.xml");
			URLConnection conn = url.openConnection();
			XMLReader xr = XMLReaderFactory.createXMLReader();
			LessonXMLParser lxp = new LessonXMLParser();
			xr.setContentHandler(lxp);
			xr.setErrorHandler(lxp);
			xr.parse(new InputSource(conn.getInputStream()));
			handler.sendMessage(handler.obtainMessage(0,lxp.getLessons()));
		}
		catch (Exception e)	{
			e.printStackTrace();
			handler.sendMessage(handler.obtainMessage(1,e.getMessage()));
		}
	}
}
