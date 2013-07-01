package com.application.app.rtwitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.application.app.fragment.StatusFragment;
import com.application.app.utility.ConnectionDetector;
import com.application.app.utility.Constants;
import com.application.app.utility.TwitterSession;

public class FragActivity extends Activity implements StatusFragment.OnTwitterUpdateListener{
	public static final int FRAGMENT_PREFS = 1;
	public static final int FRAGMENT_STATUS = 2;
	public static final String TAG = null;
	private EditText editText;
	private UpdateTwitterStatusTask mStatusTask;
	public TwitterSession session;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);
		 session=new TwitterSession(this);
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
			if(ConnectionDetector.isConnectingToInternet(this)){
				mStatusTask=new UpdateTwitterStatusTask();
				mStatusTask.execute(mStatus);
			}else{
				Toast.makeText(FragActivity.this, R.string.please_connect, Toast.LENGTH_SHORT).show();
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
			 pDialog = new ProgressDialog(FragActivity.this);
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
				Toast.makeText(FragActivity.this,R.string.status_tweeted_successfully, Toast.LENGTH_SHORT).show();
				editText.setText("");
			}else{
				Toast.makeText(FragActivity.this,"Retry", Toast.LENGTH_SHORT).show();
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
