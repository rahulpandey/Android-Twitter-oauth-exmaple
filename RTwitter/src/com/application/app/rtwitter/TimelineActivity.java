package com.application.app.rtwitter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.bitmapfun.util.Utils;

public class TimelineActivity extends FragmentActivity {
	public static final String RELOAD_LOADER = "RELOAD_LOADER";
	public static final String RELOAD = "RELOAD";
	
	/**
	 * This is timeline actvity
	 */
	OnRsetListListener listener;
	public interface OnRsetListListener{
		public void onReset();
	}
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
			
		case R.id.refresh:
			intent=new Intent();
			intent.setAction(RELOAD_LOADER);
			intent.putExtra("rel",RELOAD);
			sendBroadcast(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
