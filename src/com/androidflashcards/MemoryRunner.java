package com.androidflashcards;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.Menu;
import android.view.MenuItem;

import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector; 
import android.widget.ViewFlipper;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.AlphaAnimation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

public class MemoryRunner extends Activity implements OnGestureListener {

	private class CardWrap implements Comparable,Serializable {
		int card;
		int numRight;
		int target;
		long timestamp;

		public CardWrap(int c,int n, int t, long i) {
			card = c;
			numRight = n;
			target = t;
			timestamp = i;
		}

		public int compareTo(Object o) {
			CardWrap c = (CardWrap)o;
			if (target < c.target)
				return -1;
			if (target > c.target)
				return 1;
			if (timestamp < c.timestamp)
				return -1;
			if (timestamp > c.timestamp)
				return 1;
			return 0;
		}
		private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
			card = stream.readInt();
			numRight = stream.readInt();
			target = stream.readInt();
			timestamp = stream.readLong();
		}
		private void writeObject(ObjectOutputStream stream)
			throws IOException {
			stream.writeInt(card);
			stream.writeInt(numRight);
			stream.writeInt(target);
			stream.writeLong(timestamp);
		}
		private void readObjectNoData() 
			throws ObjectStreamException {
			card = numRight = target = 0;
			timestamp = 0L;
		}
	}

	private class StateWrapper implements Serializable {
		int savCount;
		boolean savFront;
		CardWrap savWrap;
		PriorityQueue<CardWrap> savQ;
		ArrayList<Integer> savAvail;

		StateWrapper(int c, boolean f, CardWrap cw, PriorityQueue<CardWrap> q, ArrayList<Integer> a) {
			savCount = c;
			savFront = f;
			savWrap = cw;
			savQ = q;
			savAvail = a;
		}

		private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
			savCount = stream.readInt();
			savFront = stream.readBoolean();
			savWrap = (CardWrap)stream.readObject();
			int qsize = stream.readInt();
			CardWrap[] wraps = new CardWrap[qsize];
			for(int i = 0; i < qsize;i++)
				wraps[i] = (CardWrap)stream.readObject();
			savQ = new PriorityQueue<CardWrap>(Arrays.asList(wraps));
			int asize = stream.readInt();
			Integer[] avails = new Integer[asize];
			for(int i = 0; i < asize;i++)
				avails[i] = stream.readInt();
			savAvail = new ArrayList<Integer>(Arrays.asList(avails));
		}
		private void writeObject(ObjectOutputStream stream)
			throws IOException {
			stream.writeInt(savCount);
			stream.writeBoolean(savFront);
			stream.writeObject(savWrap);
			stream.writeInt(savQ.size());
			Iterator<CardWrap> it = savQ.iterator();
			while(it.hasNext()) 
				stream.writeObject(it.next());
			stream.writeInt(savAvail.size());
			Iterator<Integer> it2 = savAvail.iterator();
			while(it2.hasNext())
				stream.writeInt(it2.next());
		}
		private void readObjectNoData() 
			throws ObjectStreamException {
			savWrap = null;
			savQ = new PriorityQueue<CardWrap>();
		}
	}

	private Lesson lesson;
	private String lname;
	private Card curCard;
	private boolean showingFront,gameDone = false;

	private GestureDetector gestureScanner; 
	private ViewFlipper slideFlipper;

	private ViewFlipper acFlip,bcFlip, curFlip;
	private ScrollView acFScroll,acBScroll,bcFScroll,bcBScroll;

	private CardWrap curWrap;
	private int curCount;
	private ArrayList<Integer> available;

	private boolean switch_front_back = false;
	private static final int SWITCH_FB_ID = Menu.FIRST;
	private static final int CLEAR_STATUS_ID = Menu.FIRST+1;

	private PriorityQueue<CardWrap> queue;
	

	@Override
  protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memory_runner);

		gestureScanner = new GestureDetector(this); 

		Bundle extras = getIntent().getExtras();

		lname = extras.getString("Lesson");
		SharedPreferences settings = getSharedPreferences(lname+"Prefs", 0);
		SharedPreferences lprefs = getSharedPreferences("lessonPrefs",0);
		switch_front_back = settings.getBoolean("switch_front_back", false);

		File f = new File("/sdcard/flashcards/"+extras.getString("Lesson")+".bin");
		Lesson l = null;
		if (f.exists()) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
				l = (Lesson)ois.readObject();
				ois.close();
			} catch (java.io.InvalidClassException e) {
				// try to reparse first
				try {
					File fl = new File("/sdcard/flashcards/"+extras.getString("Lesson")+".xml");
					if (!fl.exists())
						fl = new File("/sdcard/flashcards/"+extras.getString("Lesson")+".csv");
					if (!fl.exists())
						throw new Exception("No file to parse");
					if (fl.getName().endsWith(".xml"))
						l = AndroidFlashcards.parseXML(fl,extras.getString("Lesson"));
					else
						l = AndroidFlashcards.parseCSV(fl,extras.getString("Lesson"));
					if (l == null)
						throw new Exception("Couldn't parse file");
					FileOutputStream fos = new FileOutputStream(f);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(l);
					oos.close();
					SharedPreferences.Editor editor =  lprefs.edit();
					editor.putString(extras.getString("Lesson")+"Name",l.name());
					editor.putString(extras.getString("Lesson")+"Desc",l.description());
					editor.commit();
				} catch(Exception e2) {
					AlertDialog alertDialog = new AlertDialog.Builder(this).create();
					alertDialog.setTitle("Error");
					alertDialog.setMessage("Sorry, but an error occured trying to load lesson file.");
					alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								finish();
								return;
							} });
					alertDialog.show();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Not found");
			finish();
		}
		
		if (l != null) {
			createAnimations();
			lesson = l;
			slideFlipper = (ViewFlipper) findViewById(R.id.memory_slide_flipper);
			acFlip = (ViewFlipper) slideFlipper.getChildAt(0);
			acFlip.setInAnimation(alphain);
			acFlip.setOutAnimation(alphaout);
			acFScroll = ((ScrollView)(acFlip.findViewById(R.id.card_front_scroll)));
			acBScroll = ((ScrollView)(acFlip.findViewById(R.id.mem_back_scroll)));
			acFScroll.setFillViewport(true);
			acBScroll.setFillViewport(true);
			bcFlip = (ViewFlipper) slideFlipper.getChildAt(1);
			bcFlip.setInAnimation(alphain);
			bcFlip.setOutAnimation(alphaout);
			bcFScroll = ((ScrollView)(bcFlip.findViewById(R.id.card_front_scroll)));
			bcBScroll = ((ScrollView)(bcFlip.findViewById(R.id.mem_back_scroll)));
			bcFScroll.setFillViewport(true);
			bcBScroll.setFillViewport(true);
			curFlip = acFlip;

			StateWrapper sw = null;
			if (savedInstanceState != null) 
				sw = (StateWrapper)(savedInstanceState.getSerializable("savedState"));
			else {
				try {
					ObjectInputStream oin = new ObjectInputStream(openFileInput(lname+".ss"));
					sw = (StateWrapper)oin.readObject();
				} catch(FileNotFoundException fn) {
					sw = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (sw != null) {
				queue = sw.savQ;
				curCount = sw.savCount;
				available = sw.savAvail;
				curWrap = sw.savWrap;
				curCard = lesson.getCard(curWrap.card);				
				showingFront = sw.savFront;
			} else {
				queue = new PriorityQueue<CardWrap>();
				curCount = 0;
				available = new ArrayList<Integer>(lesson.cardCount());
				for (int i = 0;i < lesson.cardCount();i++) 
					available.add(i);
				pickCard();
				showingFront = true;
			}

			setCardToCurrent(acFlip);
			if (!showingFront)
				acFlip.showNext();

			View.OnClickListener dslist = new View.OnClickListener() {
					public void onClick(View v) { 
						// Don't show this card anymore
						pickCard();
						if (curWrap != null)
							goForwardsTo(curWrap.card);
					}
				};
			View.OnClickListener rlist = new View.OnClickListener() {
					public void onClick(View v) { 
						// Got it right, schedule further in future
						int nt = curCount;
						curWrap.numRight++;
						if (curWrap.numRight < 6) {
							switch(curWrap.numRight) {
							case 1:
								nt = curCount+5+((int)(Math.random()*10));
								break;
							case 2:
								nt = curCount+15+((int)(Math.random()*20));
								break;
							case 3:
								nt = curCount+35+((int)(Math.random()*50));
								break;
							case 4:
								nt = curCount+50+((int)(Math.random()*150));
								break;
							case 5:
								nt = curCount+150+((int)(Math.random()*200));
								break;
							}
							curWrap.target = nt;
							curWrap.timestamp = System.currentTimeMillis();
							queue.add(curWrap);
						}
						pickCard();
						if (curWrap != null)
							goForwardsTo(curWrap.card);
					}
				};
			View.OnClickListener wlist = new View.OnClickListener() {
					public void onClick(View v) {
						// Got it wrong, schedule soon
						curWrap.numRight=0;
						curWrap.target = curCount+1+((int)(Math.random()*5));
						curWrap.timestamp = System.currentTimeMillis();
						queue.add(curWrap);
						pickCard();
						if (curWrap != null)
							goForwardsTo(curWrap.card);
					}
				};
			((Button)(acFlip.findViewById(R.id.right_button))).setOnClickListener(rlist);
			((Button)(bcFlip.findViewById(R.id.right_button))).setOnClickListener(rlist);
			((Button)(acFlip.findViewById(R.id.wrong_button))).setOnClickListener(wlist);
			((Button)(bcFlip.findViewById(R.id.wrong_button))).setOnClickListener(wlist);			
			((Button)(acFlip.findViewById(R.id.dont_show_button))).setOnClickListener(dslist);
			((Button)(bcFlip.findViewById(R.id.dont_show_button))).setOnClickListener(dslist);			
		}
	}

	private void saveState() {
		SharedPreferences settings = getSharedPreferences(lname+"Prefs", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("switch_front_back", switch_front_back);
		editor.commit();
		if (!gameDone) {
			StateWrapper sw = new StateWrapper(curCount,showingFront, curWrap,queue,available);
			try {
				deleteFile(lname+".ss");
				ObjectOutputStream out = new ObjectOutputStream(openFileOutput(lname+".ss",0));
				out.writeObject(sw);
				out.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onStop(){
		super.onStop();
		saveState();
	}

	@Override 
	protected void onDestroy() {
		super.onDestroy();
		saveState();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (!gameDone) {
			StateWrapper sw = new StateWrapper(curCount,showingFront, curWrap,queue,available);
			outState.putSerializable("savedState",sw);
		}
	}

	/*
		Dynamically pick a card.
	 */
	private void pickCard() {
		printQ();
		curWrap = queue.peek();
		if ((curWrap == null ||
				 curWrap.target > curCount) &&
				(available.size() != 0)) {
			// queue was empty, or we haven't seen enough cards yet
			Integer c = (int)(Math.random()*available.size());
			curWrap = new CardWrap(c.intValue(),0,0,0);
			available.remove(c);
		}
		else
			curWrap = queue.poll();
		if (curWrap == null) { // nothing left to show
			gameDone = true;
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Congratulations!");
			alertDialog.setMessage("You got every card correct in the desk 5 times in a row!  This run is now over.  You can start a new run by choosing 'Adaptive Memory Game' again");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteFile(lname+".ss");
						finish();
						return;
					} });
			alertDialog.show();
		} else {
			curCard = lesson.getCard(curWrap.card);
			curCount++;
		}
	}

	// fill in animations
	private TranslateAnimation ifr,otl,ifl,otr;
	private AlphaAnimation alphain,alphaout;
	private void createAnimations() {
		AccelerateInterpolator ai = new AccelerateInterpolator();
		ifr = new TranslateAnimation
			(Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
			 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
		ifr.setDuration(300);
		ifr.setInterpolator(ai);

		otl = new TranslateAnimation
			(Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
			 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
		otl.setDuration(300);
		otl.setInterpolator(ai);

		ifl = new TranslateAnimation
			(Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
			 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
		ifl.setDuration(300);
		ifl.setInterpolator(ai);

		otr = new TranslateAnimation
			(Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
			 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
		otr.setDuration(300);
		otr.setInterpolator(ai);

		alphain = new AlphaAnimation(0.0f,1.0f);
		alphain.setDuration(250);
		alphaout = new AlphaAnimation(1.0f,0.0f);
		alphaout.setDuration(250);
	}

	private ViewFlipper nextView() {
		if (curFlip == acFlip)
			return bcFlip;
		else
			return acFlip;
	}

	private void setCardToCurrent(ViewFlipper card) {
		if (switch_front_back) {
			((TextView)(card.findViewById(R.id.card_front_text))).setText(curCard.back);
			((TextView)(card.findViewById(R.id.mem_back_id))).setText(curCard.front);
		} else {
			((TextView)(card.findViewById(R.id.card_front_text))).setText(curCard.front);
			((TextView)(card.findViewById(R.id.mem_back_id))).setText(curCard.back);
		}
		if (card == acFlip) {
			acFScroll.scrollTo(0,0);
			acBScroll.scrollTo(0,0);
		} else {
			bcFScroll.scrollTo(0,0);
			bcBScroll.scrollTo(0,0);
		}
		((TextView)(card.findViewById(R.id.card_front_number))).setText(""+(curWrap.card+1));
	}

	private boolean goForwardsTo(int target) {
		if (target >= lesson.cardCount())
			return false;
		ViewFlipper next = nextView();
		next.setDisplayedChild(1);
		setCardToCurrent(next);
		showingFront = false;
		slideFlipper.setInAnimation(ifr);
		slideFlipper.setOutAnimation(otl);
		slideFlipper.showNext();
		curFlip = next;
		return true;
	}

	// Options menu handlers
	@Override
  public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SWITCH_FB_ID, 3, R.string.switch_fb);
		menu.add(0, CLEAR_STATUS_ID, 3, R.string.clear_stat);
		return true;
	}

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case SWITCH_FB_ID: 
			switch_front_back = !switch_front_back;
			setCardToCurrent(curFlip);
			return true;
		case CLEAR_STATUS_ID: {
			deleteFile(lname+".ss");
			Intent  intent = this.getIntent();
			startActivity(intent);
			finish();
			return true;
		}
		}
		return super.onMenuItemSelected(featureId, item);
	}

	/* *************** *
	 *  Touch handlers *
	 ***************** */

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev){
		super.dispatchTouchEvent(ev);
		return gestureScanner.onTouchEvent(ev);
	} 

	@Override
  public boolean onTouchEvent(MotionEvent me) {
		return gestureScanner.onTouchEvent(me);
	}

  @Override
  public boolean onDown(MotionEvent e) {
		return true;
	}

	private long lastTap = 0;
	@Override
  public boolean onSingleTapUp(MotionEvent e) {
		if ((System.currentTimeMillis() - lastTap) < 500)
			return true;
		lastTap = System.currentTimeMillis();
		if (showingFront) {
			curFlip.showNext();
			showingFront = false;
		} else {
			curFlip.showNext();
			showingFront = true;
		}
		return true;
	}
	
  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}
  
	@Override
  public void onLongPress(MotionEvent e) {
	}
   
  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}
   
	@Override 
  public void onShowPress(MotionEvent e) {
	}

	private void printQ() {
		System.out.println("CurCount: "+curCount);
		Iterator<CardWrap> it = queue.iterator();
		while(it.hasNext()) {
			CardWrap cw = it.next();
			Card c = lesson.getCard(cw.card);
			System.out.println(cw.card+": "+c.front);
			System.out.println("  right: "+cw.numRight);
			System.out.println("  targt: "+cw.target);
			System.out.println("  times: "+cw.timestamp);
		}
	}
             
}
