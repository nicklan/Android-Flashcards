package nick.flashcards;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector; 
import android.widget.ViewFlipper;
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
	private Card curCard;
	private boolean showingFront;

	private GestureDetector gestureScanner; 
	private ViewFlipper slideFlipper;

	private ViewFlipper ac_flip,bc_flip,cc_flip;

	private int view_pos,card_pos;

	@Override
  protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.card_runner);

		gestureScanner = new GestureDetector(this); 

		Bundle extras = getIntent().getExtras();

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
						l = TimedFlashcards.parseXML(fl);
					else
						l = TimedFlashcards.parseCSV(fl);
					if (l == null)
						throw new Exception("Couldn't parse file");
					FileOutputStream fos = new FileOutputStream(f);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(l);
					oos.close();
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
			slideFlipper = (ViewFlipper) findViewById(R.id.slide_flipper);
			ac_flip = (ViewFlipper) slideFlipper.getChildAt(0);
			ac_flip.setInAnimation(alphain);
			ac_flip.setOutAnimation(alphaout);
			bc_flip = (ViewFlipper) slideFlipper.getChildAt(1);
			bc_flip.setInAnimation(alphain);
			bc_flip.setOutAnimation(alphaout);
			cc_flip = (ViewFlipper) slideFlipper.getChildAt(2);
			cc_flip.setInAnimation(alphain);
			cc_flip.setOutAnimation(alphaout);
			card_pos = 0;
			curCard = lesson.getCard(card_pos);
			showingFront = true;
			view_pos = 0;
			setCardToCurrent(ac_flip);
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

	private ViewFlipper prevView() {
		switch(view_pos) {
		case 0:
			return cc_flip;
		case 1:
			return ac_flip;
		case 2:
			return bc_flip;
		}
		return null;
	}

	private ViewFlipper currentView() {
		switch(view_pos) {
		case 0:
			return ac_flip;
		case 1:
			return bc_flip;
		case 2:
			return cc_flip;
		}
		return null;
	}

	private ViewFlipper nextView() {
		switch(view_pos) {
		case 0:
			return bc_flip;
		case 1:
			return cc_flip;
		case 2:
			return ac_flip;
		}
		return null;
	}

	private void setCardToCurrent(ViewFlipper card) {
		((TextView)(((LinearLayout)(card.getChildAt(0))).getChildAt(0))).setText(curCard.front);
		((TextView)(((LinearLayout)(card.getChildAt(1))).getChildAt(0))).setText(curCard.back);
		((TextView)(((LinearLayout)(card.getChildAt(0))).getChildAt(1))).setText(""+(card_pos+1));
	}

	@Override
  public boolean onTouchEvent(MotionEvent me) {
		return gestureScanner.onTouchEvent(me);
	}

  @Override
  public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override 
  public boolean onSingleTapUp(MotionEvent e) {
		ViewFlipper cur = currentView();
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
		if (velocityX < -1000) { // next card
			card_pos++;
			if (card_pos >= lesson.cardCount()) {
				card_pos--;
				return true; // should bounce
			}
			curCard = lesson.getCard(card_pos);
			ViewFlipper cur = null;
			if (!showingFront)
				cur = currentView();
			ViewFlipper next = nextView();
			setCardToCurrent(next);
			showingFront = true;
			view_pos++;
			view_pos%=3;
			slideFlipper.setInAnimation(ifr);
			slideFlipper.setOutAnimation(otl);
			slideFlipper.showNext();
			if (cur != null)
				cur.showNext();
		}
		if (velocityX > 1000) { // prev card
			if (card_pos == 0) return true; // maybe animate a bounce here?
			card_pos--;
			curCard = lesson.getCard(card_pos);
			ViewFlipper cur = null;
			if (!showingFront)
				cur = currentView();
			ViewFlipper prev = prevView();
			setCardToCurrent(prev);
			showingFront = true;
			view_pos--;
			if (view_pos == -1) view_pos = 2;
			slideFlipper.setInAnimation(ifl);
			slideFlipper.setOutAnimation(otr);
			slideFlipper.showPrevious();
			if (cur != null)
				cur.showNext();
		}
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
