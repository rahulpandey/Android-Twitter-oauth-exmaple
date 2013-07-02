package com.application.app.rtwitter;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.application.app.fragment.StatusFragment;
import com.application.app.utility.TwitterSession;

public class FragActivity extends Activity {
	public static final int FRAGMENT_PREFS = 1;
	public static final int FRAGMENT_STATUS = 2;
	public static final String TAG = null;
	public TwitterSession session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);
		session = new TwitterSession(this);
		if (savedInstanceState == null) {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			switch (getIntent().getIntExtra("fragment_id", -1)) {
			case FRAGMENT_STATUS:
				transaction.add(R.id.fragment_container, new StatusFragment());
				this.setTitle(R.string.status);
				break;
			}

			transaction.commit();

		}
	}

}
