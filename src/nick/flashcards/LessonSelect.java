package nick.flashcards;

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