package com.application.app.rtwitter;

import android.app.Application;
import android.util.Log;

public class RTwitterApplication extends Application {


	private static final String TAG = "RTwitterApplication";

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i(TAG, "Application started");
	}

}
