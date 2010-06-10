package com.androidflashcards;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.AlphaAnimation;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class CardRunner extends Activity implements OnGestureListener {

	private Lesson lesson;
	private String lname,lprefname;
	private Card curCard;
	private boolean showingFront;

	private GestureDetector gestureScanner; 
	private FixedFlipper slideFlipper;

	private FixedFlipper acFlip,bcFlip,ccFlip;
	private ScrollView acFScroll,acBScroll,bcFScroll,bcBScroll,ccFScroll,ccBScroll;

	private int view_pos,card_pos;

	private boolean switch_front_back = false;

	private static final int FIRST_CARD_ID = Menu.FIRST;
	private static final int GOTO_CARD_ID = Menu.FIRST + 1;
	private static final int LAST_CARD_ID = Menu.FIRST + 2;
	private static final int SWITCH_FB_ID = Menu.FIRST + 3;

	@Override
  protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println(savedInstanceState);
		setContentView(R.layout.card_runner);

		gestureScanner = new GestureDetector(this); 

		Bundle extras = getIntent().getExtras();

		lname = extras.getString("Lesson");
		lprefname = lname.replace("/",".")+"Prefs";
		SharedPreferences settings = getSharedPreferences(lprefname, 0);
		SharedPreferences lprefs = getSharedPreferences("lessonPrefs",0);
		switch_front_back = settings.getBoolean("switch_front_back", false);

		File f = new File(extras.getString("Lesson"));
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
			slideFlipper = (FixedFlipper) findViewById(R.id.slide_flipper);
			acFlip = (FixedFlipper) slideFlipper.getChildAt(0);
			acFlip.setInAnimation(alphain);
			acFlip.setOutAnimation(alphaout);
			acFScroll = ((ScrollView)(acFlip.findViewById(R.id.card_front_scroll)));
			acBScroll = ((ScrollView)(acFlip.findViewById(R.id.card_back_scroll)));
			acFScroll.setFillViewport(true);
			acBScroll.setFillViewport(true);
			bcFlip = (FixedFlipper) slideFlipper.getChildAt(1);
			bcFlip.setInAnimation(alphain);
			bcFlip.setOutAnimation(alphaout);
			bcFScroll = ((ScrollView)(bcFlip.findViewById(R.id.card_front_scroll)));
			bcBScroll = ((ScrollView)(bcFlip.findViewById(R.id.card_back_scroll)));
			bcFScroll.setFillViewport(true);
			bcBScroll.setFillViewport(true);
			ccFlip = (FixedFlipper) slideFlipper.getChildAt(2);
			ccFlip.setInAnimation(alphain);
			ccFlip.setOutAnimation(alphaout);
			ccFScroll = ((ScrollView)(ccFlip.findViewById(R.id.card_front_scroll)));
			ccBScroll = ((ScrollView)(ccFlip.findViewById(R.id.card_back_scroll)));
			ccFScroll.setFillViewport(true);
			ccBScroll.setFillViewport(true);
			card_pos = savedInstanceState != null ? 
				savedInstanceState.getInt("savedCardPos"):0;
			curCard = lesson.getCard(card_pos);
			showingFront = true;
			view_pos = 0;
			setCardToCurrent(acFlip);
			if (savedInstanceState != null &&
					!savedInstanceState.getBoolean("savedShowingFront")) {
				acFlip.showNext();
				showingFront = false;
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		System.out.println("SAVING STATE");
		outState.putInt("savedCardPos", card_pos);
		outState.putBoolean("savedShowingFront", showingFront);
	}

	@Override
	protected void onStop(){
		super.onStop();
		SharedPreferences settings = getSharedPreferences(lprefname, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("switch_front_back", switch_front_back);
		editor.commit();
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

	private FixedFlipper prevView() {
		switch(view_pos) {
		case 0:
			return ccFlip;
		case 1:
			return acFlip;
		case 2:
			return bcFlip;
		}
		return null;
	}

	private FixedFlipper currentView() {
		switch(view_pos) {
		case 0:
			return acFlip;
		case 1:
			return bcFlip;
		case 2:
			return ccFlip;
		}
		return null;
	}

	private FixedFlipper nextView() {
		switch(view_pos) {
		case 0:
			return bcFlip;
		case 1:
			return ccFlip;
		case 2:
			return acFlip;
		}
		return null;
	}

	private void setCardToCurrent(FixedFlipper card) {
		if (switch_front_back) {
			((TextView)(card.findViewById(R.id.card_front_text))).setText(curCard.back);
			((TextView)(card.findViewById(R.id.card_back_text))).setText(curCard.front);
		} else {
			((TextView)(card.findViewById(R.id.card_front_text))).setText(curCard.front);
			((TextView)(card.findViewById(R.id.card_back_text))).setText(curCard.back);
		}
		if (card == acFlip) {
			acFScroll.scrollTo(0,0);
			acBScroll.scrollTo(0,0);
		} else if (card == bcFlip) {
			bcFScroll.scrollTo(0,0);
			bcBScroll.scrollTo(0,0);
		} else {
			ccFScroll.scrollTo(0,0);
			ccBScroll.scrollTo(0,0);
		}
		((TextView)(card.findViewById(R.id.card_front_number))).setText(""+(card_pos+1));
	}

	private boolean goBackwardsTo(int target) {
		if (target < 0) return false;
		card_pos=target;
		curCard = lesson.getCard(card_pos);
		FixedFlipper prev = prevView();
		prev.setDisplayedChild(0);
		setCardToCurrent(prev);
		showingFront = true;
		view_pos--;
		if (view_pos == -1) view_pos = 2;
		slideFlipper.setInAnimation(ifl);
		slideFlipper.setOutAnimation(otr);
		slideFlipper.showPrevious();
		return true;
	}

	private boolean goForwardsTo(int target) {
		if (target >= lesson.cardCount())
			return false;
		card_pos = target;
		curCard = lesson.getCard(card_pos);
		FixedFlipper next = nextView();
		next.setDisplayedChild(0);
		setCardToCurrent(next);
		showingFront = true;
		view_pos++;
		view_pos%=3;
		slideFlipper.setInAnimation(ifr);
		slideFlipper.setOutAnimation(otl);
		slideFlipper.showNext();
		return true;
	}

	// Options menu handlers
	@Override
  public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, FIRST_CARD_ID, 0, R.string.first_card);
		menu.add(0, GOTO_CARD_ID, 1, R.string.goto_card);
		menu.add(0, LAST_CARD_ID, 2, R.string.last_card);
		menu.add(0, SWITCH_FB_ID, 3, R.string.switch_fb);
		return true;
	}

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case FIRST_CARD_ID: 
			goBackwardsTo(0);
			return true;
		case LAST_CARD_ID: 
			goForwardsTo(lesson.cardCount()-1);
			return true;
		case GOTO_CARD_ID: {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Go To Card");
			alert.setMessage("Please enter the card number you want to go to");
			final EditText input = new EditText(this);
			input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
			alert.setView(input);
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						int req_card = Integer.parseInt(input.getText().toString())-1;
						if (req_card < 0)
							req_card = 0;
						if (req_card >= (lesson.cardCount()-1))
							req_card = (lesson.cardCount()-1);
						if (card_pos < req_card)
							goForwardsTo(req_card);
						else if (card_pos > req_card)
							goBackwardsTo(req_card);
					}
				});
			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
			alert.show();
			return true;
		}
		case SWITCH_FB_ID: 
			switch_front_back = !switch_front_back;
			setCardToCurrent(currentView());
			return true;
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
		FixedFlipper cur = currentView();
		if (showingFront) {
			cur.showNext();
			showingFront = false;
		} else {
			cur.showNext();
			showingFront = true;
		}
		return true;
	}
   
  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (velocityX < -1000) // next card
			goForwardsTo(card_pos+1); // should check bool so i can bounce
		if (velocityX > 1000) // prev card
			goBackwardsTo(card_pos-1); // should check bool so i can bounce
		return true;
	}
  
	@Override
  public void onLongPress(MotionEvent e) {
	}
   
  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return true;
	}
   
	@Override 
  public void onShowPress(MotionEvent e) {
	}
             
}
