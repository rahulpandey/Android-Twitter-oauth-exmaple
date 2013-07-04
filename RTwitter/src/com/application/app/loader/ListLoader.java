package com.application.app.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.application.app.utility.Constants;
import com.application.app.utility.TwitterSession;

public class ListLoader extends AsyncTaskLoader<List<HashMap<String, String>>> {
	public static final String KEY_NAME = "name"; // parent node
	public static final String KEY_PROFILE_PIC = "profilepic";
	public static final String KEY_TWEETS = "tweets";
	private List<HashMap<String, String>> stautsList;
	private TwitterSession session;
	private InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
	public static final String KEY_CREATED_AT = "createdat";
	private static final String TAG = "ListLoader";
	private List<twitter4j.Status> statuses = null;
	
	public ListLoader(Context context) {
		super(context);
		session = new TwitterSession(context);
		
	}
	
	

	@Override
	public List<HashMap<String, String>> loadInBackground() {
		// TODO Auto-generated method stub
		
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY);
		builder.setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET);
		// Access Token
		String access_token = session.getDefaultAccessToaken();
		// Access Token Secret
		stautsList = new ArrayList<HashMap<String, String>>();
		String access_token_secret = session.getDefaultSecret();
		AccessToken accessToken = new AccessToken(access_token,access_token_secret);
		Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

		
		try {
			statuses = twitter.getHomeTimeline();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "TwitterException=>" + e.getMessage());
		}
		List<HashMap<String, String>> entries = new ArrayList<HashMap<String, String>>(stautsList.size());
		if (!statuses.isEmpty()) {
			for (twitter4j.Status status : statuses) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(KEY_NAME, status.getUser().getName());
				map.put(KEY_PROFILE_PIC, status.getUser().getProfileImageURL());
				map.put(KEY_TWEETS, status.getText());
				Log.d(TAG, "Status=>" + status.getText());
				map.put(KEY_CREATED_AT,String.valueOf(status.getCreatedAt().getTime()));
				entries.add(map);
			
			}
		}
		return entries;
	}

	@Override
	public void deliverResult(List<HashMap<String, String>> data) {
		// TODO Auto-generated method stub

		if (isReset()) {
			if (data != null) {
				onReleaseResources(data);
			}
		}
		List<HashMap<String, String>> oldData = data;
		stautsList = data;

		// If the Loader is currently started, we can immediately
		// deliver its results.
		if (isStarted()) super.deliverResult(data);

		// At this point we can release the resources associated with
		// 'oldApps' if needed; now that the new result is delivered we
		// know that it is no longer in use.
		if (oldData != null) onReleaseResources(oldData);

	}
	@Override
	protected void onStartLoading() {
        if (stautsList != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(stautsList);
        }

        
        boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());
        if (takeContentChanged() || stautsList == null || configChange) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }
	
	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Handles a request to cancel a load.
	 */
	@Override
	public void onCanceled(List<HashMap<String, String>> data) {
		super.onCanceled(data);

		// At this point we can release the resources associated with 'apps'
		// if needed.
		onReleaseResources(data);
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();
		// Ensure the loader is stopped
		onStopLoading();
		// At this point we can release the resources associated with 'apps'
		// if needed.
		if (stautsList != null) {
			onReleaseResources(stautsList);
			stautsList = null;
		}
	}

	protected void onReleaseResources(List<HashMap<String, String>> data) {
		// For a simple List<> there is nothing to do. For something
		// like a Cursor, we would close it here.

	}
	public static class InterestingConfigChanges {
        final Configuration mLastConfiguration = new Configuration();
        int mLastDensity;

        boolean applyNewConfig(Resources res) {
            int configChanges = mLastConfiguration.updateFrom(res.getConfiguration());
            boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
            if (densityChanged || (configChanges&(ActivityInfo.CONFIG_LOCALE
                   |ActivityInfo.CONFIG_UI_MODE|ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
                mLastDensity = res.getDisplayMetrics().densityDpi;
                return true;
            }
            return false;
        }
    }
	 
}
