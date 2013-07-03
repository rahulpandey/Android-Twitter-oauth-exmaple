package com.application.app.rtwitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.application.app.utility.ConnectionDetector;
import com.application.app.utility.Constants;
import com.application.app.utility.TwitterSession;
import com.example.android.bitmapfun.util.Utils;


public class MainActivity extends FragmentActivity {

	private static final String TAG = "MainActivity";
	public static final int REQUEST_CODE = 100;
	
	private Twitter twitter;
	private RequestToken requestToken;
	private TwitterSession session;
	private AuthenticationTask mAuthTask;
	private RetriveAcessTokenTask mAccessTokenTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) Utils.enableStrictMode();
	    
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		session=new TwitterSession(this);
		
		// Check if Internet present
		if (!ConnectionDetector.isConnectingToInternet(this)) {
			// Internet Connection is not present
			Toast.makeText(MainActivity.this,getString(R.string.please_connect), Toast.LENGTH_LONG).show();
			// stop executing code by return
			return;
		}

		// Check if twitter keys are set
		if (TextUtils.isEmpty(Constants.TWITTER_CONSUMER_KEY) ||TextUtils.isEmpty( Constants.TWITTER_CONSUMER_SECRET)) {
			// Internet Connection is not present
			Toast.makeText(MainActivity.this, "Twitter oAuth tokens",Toast.LENGTH_LONG).show();
			// stop executing code by return
			return;
		}
		

	}

	@Override
	public void onResume() {
		super.onResume();
		if(session!=null)
			if(session.isTwitterLoggedInAlready()) start();
		
	}

	@Override
	public void onPause() {
		super.onPause();
		if(session!=null)
			if(session.isTwitterLoggedInAlready()) finish();
	}
	@Override
	protected void onNewIntent(Intent intent) {
		
		super.onNewIntent(intent);
		Log.d(TAG, "Method calldes=>");
		Uri uri = intent.getData();
		if(mAccessTokenTask!=null) return;
		if (!session.isTwitterLoggedInAlready()) {
			if (uri != null && uri.getScheme().equals(Constants.TWITTER_SCHEME)) {
				// oAuth verifier
				Log.d(TAG, "callback: " + uri.getScheme().toString());
				String verifier = uri.getQueryParameter(Constants.URL_TWITTER_OAUTH_VERIFIER);
				mAccessTokenTask=new RetriveAcessTokenTask();
				mAccessTokenTask.execute(verifier);

			}
		}
	}
	

	public void onAuth(View view) {
		// Check if already logged in
		if(mAuthTask!=null) return;
		if (!session.isTwitterLoggedInAlready()) {
			mAuthTask=new AuthenticationTask();
			mAuthTask.execute((Void) null);
			
		} else {
			// user already logged into twitter
			Toast.makeText(this, "Already Logged into twitter",Toast.LENGTH_LONG).show();
		}
	}

	public class RetriveAcessTokenTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog pDialog;
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			pDialog=new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Please Wait Retriving Acess.......");
			pDialog.show();
		}
		@Override
		protected Boolean doInBackground(String... params) {
			
			try {
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, params[0]);
				if(accessToken!=null){
					session.saveSaveSession(accessToken, twitter);
					String username = session.getUserName();
					Log.d(TAG, "Twitter username=>" + username);
					return true;
				}else {
					session.Logout();
					return false;
				}

			} catch (TwitterException e) {
				//
				e.printStackTrace();
				Log.e(TAG, "TwitterException=>" + e.getMessage());
			}
			return false;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			
			super.onPostExecute(result);
			mAccessTokenTask=null;
			pDialog.cancel();
				
			if(result) start();
			else Toast.makeText(MainActivity.this, R.string.please_login_again,Toast.LENGTH_SHORT).show();
			
		}
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			mAccessTokenTask=null;
			pDialog.cancel();
		}

	}
	private void start() {
		Intent intent=new Intent(MainActivity.this,TimelineActivity.class);
		startActivity(intent);
	}
	public class AuthenticationTask extends AsyncTask<Void, Void,Boolean> {
		ProgressDialog dialog;
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			dialog=new ProgressDialog(MainActivity.this);
			dialog.setMessage("Please Wait...");
			dialog.show();
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET);
			Configuration configuration = builder.build();
			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();
			try {
				requestToken = twitter.getOAuthRequestToken(Constants.TWITTER_CALLBACK_URL);
				Log.d(TAG, "requestToken=>" +requestToken.toString());
				if (requestToken!=null) {
					return true;
				}
			
			
			} catch (TwitterException e) {
				e.printStackTrace();
				Log.e(TAG, "TwitterException=>" + e.getMessage());
			}
			return false;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			
			super.onPostExecute(result);
			mAuthTask=null;
			dialog.cancel();
			if(!result) Toast.makeText(MainActivity.this, "Retry",Toast.LENGTH_SHORT).show();
			else startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
		}
		@Override
		protected void onCancelled() {
			
			super.onCancelled();
			mAuthTask=null;
			dialog.cancel();
		}

	}
	

}
