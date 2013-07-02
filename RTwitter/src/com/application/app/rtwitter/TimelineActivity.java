package com.application.app.rtwitter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.bitmapfun.util.Utils;

public class TimelineActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (BuildConfig.DEBUG) Utils.enableStrictMode();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** Called every time an options item is selected. */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent(this, FragActivity.class);

		switch (item.getItemId()) {

		case R.id.updateStauts:
			startActivity(intent.putExtra("fragment_id",FragActivity.FRAGMENT_STATUS));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
