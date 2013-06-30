package com.application.app.rtwitter;



import com.application.app.fragment.HomeFragment;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements HomeFragment.OnTwitterUpdateListener{
	
	private static final int LOGIN = 0;
	private static final int HOME = 1;
	private static final int FRAGMENT_COUNT = HOME + 1;
	private static final int REQUEST_CODE = 1001;
	private static final String TAG = "MainActivity";
	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
	private ConnectionDetector cd;
	private Twitter twitter;
	private RequestToken requestToken;
	private TwitterSession session;
	private UpdateTwitterStatusTask mStatusTask;
	private EditText editText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		if (Build.VERSION.SDK_INT >= 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		cd = new ConnectionDetector(getApplicationContext());
		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			Toast.makeText(MainActivity.this,getString(R.string.please_connect), Toast.LENGTH_SHORT).show();
			// stop executing code by return
			return;
		}

		// Check if twitter keys are set
		if (Constants.TWITTER_CONSUMER_KEY.trim().length() == 0 || Constants.TWITTER_CONSUMER_SECRET.trim().length() == 0) {
			// Internet Connection is not present
			Toast.makeText(MainActivity.this, "Twitter oAuth tokens",Toast.LENGTH_SHORT).show();
			// stop executing code by return
			return;
		}
		session = new TwitterSession(this);
		FragmentManager fm = getSupportFragmentManager();
		fragments[LOGIN] = fm.findFragmentById(R.id.t_login);
		fragments[HOME] = fm.findFragmentById(R.id.t_home);
		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < fragments.length; i++) {
			transaction.hide(fragments[i]);
		}
		transaction.commit();

	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		if (session.isTwitterLoggedInAlready()) {
			showFragment(HOME, false);
		} else {
			showFragment(LOGIN, false);
		}

	}

	private void showFragment(int fragmentIndex, boolean addToBackStack) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < fragments.length; i++) {
			if (i == fragmentIndex) {
				transaction.show(fragments[i]);
			} else {
				transaction.hide(fragments[i]);
			}
		}
		if (addToBackStack) {
			transaction.addToBackStack(null);
		}
		transaction.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (session.isTwitterLoggedInAlready()) {
			showFragment(HOME, false);
		} else {
			showFragment(LOGIN, false);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "Method calldes=>");
		Uri uri = intent.getData();
		super.onNewIntent(intent);
		Log.d(TAG, "callback: " + uri.getScheme().toString());
		if(!session.isTwitterLoggedInAlready()){
			if (uri != null && uri.getScheme().equals(Constants.TWITTER_SCHEME)) {
				// oAuth verifier
				String verifier = uri.getQueryParameter(Constants.URL_TWITTER_OAUTH_VERIFIER);
				// Get the access token
				try {
					AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,verifier);
					session.saveSaveSession(accessToken, twitter);
					String username = session.getUserName();
					Log.d(TAG, "Twitter username=>" + username);
					showFragment(HOME, false);
				} catch (TwitterException e) {
					// 
					e.printStackTrace();
					Log.e(TAG, "TwitterException=>"+e.getMessage());
				}
	
			}
		}

	}

	public void onAuth(View view) {
		// Check if already logged in
		if (!session.isTwitterLoggedInAlready()) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET);
			Configuration configuration = builder.build();
			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();
			try {
				requestToken = twitter.getOAuthRequestToken(Constants.TWITTER_CALLBACK_URL);
				startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())), REQUEST_CODE);
			} catch (TwitterException e) {
				e.printStackTrace();
				Log.e(TAG, "TwitterException=>"+e.getMessage());
			}
		} else {
			// user already logged into twitter
			Toast.makeText(this, "Already Logged into twitter",Toast.LENGTH_LONG).show();
		}
	}
	
	public void onLogout(View view){
		session.Logout();
		showFragment(LOGIN, false);
	}

	@Override
	public void onTwtterUpdatClick(EditText mStatusText) {
		this.editText=mStatusText;
		if(mStatusTask!=null){
			return;
		}
		mStatusText.setError(null);
		String mStatus = editText.getText().toString().trim();
		if(TextUtils.isEmpty(mStatus)){
			editText.setError(getString(R.string.this_field_require));
			editText.requestFocus();
		}else{
			if(cd.isConnectingToInternet()){
				mStatusTask=new UpdateTwitterStatusTask();
				mStatusTask.execute(mStatus);
			}else{
				Toast.makeText(MainActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	class UpdateTwitterStatusTask extends AsyncTask<String, String, Boolean> {
		ProgressDialog pDialog;
		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			 pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Updating to twitter...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Places JSON
		 * */
		protected Boolean doInBackground(String... args) {
			Log.d("Tweet Text", "> " + args[0]);
			String status = args[0];
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(Constants.TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(Constants.TWITTER_CONSUMER_SECRET);
				// Access Token
				String access_token = session.getDefaultAccessToaken();
				// Access Token Secret
				String access_token_secret = session.getDefaultSecret();
				AccessToken accessToken = new AccessToken(access_token,access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
				// Update status
				twitter4j.Status response = twitter.updateStatus(status);
				Log.d(TAG,"Response Text=>"+response.getText());
				if(!TextUtils.isEmpty(response.getText())) return true;
				
			} catch (TwitterException e) {
				// Error in updating status
				Log.e(TAG,"TwitterException=>"+e.getMessage());
			}
			return false;
		}

		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 * **/
		@Override
		protected void onPostExecute(Boolean results) {
			// dismiss the dialog after getting all products
			// TODO Auto-generated method stub
			mStatusTask=null;
			pDialog.cancel();
			if(results){
				Toast.makeText(MainActivity.this,R.string.status_tweeted_successfully, Toast.LENGTH_SHORT).show();
				editText.setText("");
			}else{
				Toast.makeText(MainActivity.this,"Retry", Toast.LENGTH_SHORT).show();
			}
		}
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mStatusTask=null;
			pDialog.cancel();
		}

	}
	
	

}
