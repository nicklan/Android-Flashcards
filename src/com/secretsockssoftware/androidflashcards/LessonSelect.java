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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LessonSelect extends Activity  {
	
	private String lname,lfile,ldesc;
	private LessonSelect me;

	@Override
  protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lesson_type_select);
		Bundle extras = getIntent().getExtras();
		lname = extras.getString("LessonName");
		lfile = extras.getString("LessonFile");
		ldesc = extras.getString("LessonDesc");
		me = this;

		final TextView nametv = (TextView) findViewById(R.id.lesson_name_tv);
		nametv.setText(lname);
		final TextView desctv = (TextView) findViewById(R.id.lesson_desc_tv);
		desctv.setText(ldesc);
		
		final Button rev_button = (Button) findViewById(R.id.review_button);
		rev_button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(me, CardRunner.class);
					i.putExtra("Lesson", lfile);
					startActivity(i);
				}
			});

		final Button mem_button = (Button) findViewById(R.id.memory_button);
		mem_button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent i = new Intent(me, MemoryRunner.class);
					i.putExtra("Lesson", lfile);
					startActivity(i);
				}
			});
	}

}