package com.example.superrookie;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private boolean detectEnabled;
	
	private TextView textViewDetectState;
	private Button buttonToggleDetect;
	private Button buttonExit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textViewDetectState = (TextView)findViewById(R.id.textViewDetectState);
		
		buttonToggleDetect = (Button)findViewById(R.id.buttonDetectToggle);		
		buttonToggleDetect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setDetectEnabled(!detectEnabled);
			}
		});
		
		buttonExit = (Button)findViewById(R.id.buttonExit);
		buttonExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setDetectEnabled(false);
				MainActivity.this.finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void setDetectEnabled(boolean enable) {	
		detectEnabled = enable;
		
		Intent intent = new Intent(this, CallDetectService.class);
		Resources resources = getResources();
		String buttonText;
		String textViewText;
		if (enable) {
			startService(intent);
			
			buttonText = resources.getString(R.string.turn_off);
			textViewText = resources.getString(R.string.detecting);			
		} else {
			stopService(intent);
			
			buttonText = resources.getString(R.string.turn_on);
			textViewText = resources.getString(R.string.not_detecting);
		}
		
		buttonToggleDetect.setText(buttonText);
		textViewDetectState.setText(textViewText);
	}
}