package com.application.app.fragment;


import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.application.app.rtwitter.R;
import com.application.app.utility.ConnectionDetector;
import com.application.app.utility.Constants;
import com.application.app.utility.TwitterSession;

public class StatusFragment extends Fragment implements OnClickListener{
	public static final String TAG = "StatusFragment";
	Button mTwitterUpdateButton;
	private TwitterSession session;
	
	EditText mStatusText;
	private UpdateTwitterStatusTask mStatusTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		session=new TwitterSession(getActivity());
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.status_frag, container, false);
		mStatusText = (EditText) view.findViewById(R.id.status);
		mTwitterUpdateButton = (Button) view.findViewById(R.id.btn_upd);
		mTwitterUpdateButton.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		onTwtterUpdatClick();
	}

	
	private void onTwtterUpdatClick() {
		
		if(mStatusTask!=null){
			return;
		}
		
		String mStatus = mStatusText.getText().toString().trim();
		if(TextUtils.isEmpty(mStatus)){
			mStatusText.setError(getString(R.string.this_field_require));
			mStatusText.requestFocus();
		}else{
			if(ConnectionDetector.isConnectingToInternet(getActivity())){
				mStatusTask=new UpdateTwitterStatusTask();
				mStatusTask.execute(mStatus);
			}else{
				Toast.makeText(getActivity(), R.string.please_connect, Toast.LENGTH_SHORT).show();
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
			pDialog = new ProgressDialog(getActivity());
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
				Toast.makeText(getActivity(),R.string.status_tweeted_successfully, Toast.LENGTH_SHORT).show();
				mStatusText.setText("");
			}else{
				Toast.makeText(getActivity(),"Retry", Toast.LENGTH_SHORT).show();
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
