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