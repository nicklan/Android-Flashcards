package com.androidflashcards;

import android.content.Context;
import android.widget.ViewFlipper;
import android.util.AttributeSet;
import android.os.Build;
import android.util.Log;

/* 
	 fix for android 2.1/2.2 bug.
	 see: http://code.google.com/p/android/issues/detail?id=6191
*/
public class FixedFlipper extends ViewFlipper {

	private final int apiLevel = Integer.parseInt(Build.VERSION.SDK);

	public FixedFlipper(Context context) {
		super(context);
	}

	public FixedFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDetachedFromWindow() {
		if( apiLevel >= 7 ) {
			try {
				super.onDetachedFromWindow();
			}
			catch (IllegalArgumentException e) {
				Log.i(AndroidFlashcards.TAG,"Android project issue 6191 workaround.");
				stopFlipping();
			}
		} else {
			super.onDetachedFromWindow();
		}
	}
}