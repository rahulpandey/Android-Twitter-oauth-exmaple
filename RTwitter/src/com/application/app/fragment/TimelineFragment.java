package com.application.app.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.annotation.SuppressLint;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.application.app.rtwitter.R;
import com.application.app.utility.Constants;
import com.application.app.utility.TwitterSession;
import com.example.android.bitmapfun.util.ImageCache.ImageCacheParams;
import com.example.android.bitmapfun.util.ImageFetcher;




public class TimelineFragment extends ListFragment {
	private TwitterSession session;
	private RetriveTweetsTask mTweetsTask;

	// XML node keys
	static final String KEY_NAME = "name"; // parent node
	static final String KEY_PROFILE_PIC = "profilepic";
	static final String KEY_TWEETS = "tweets";
	private ArrayList<HashMap<String, String>> stautsList;
	private static final String IMAGE_CACHE_DIR = "thumbs";
	static final String KEY_CREATED_AT = "createdat";
	private int mImageThumbSize;
	private TweetAdapter mAdapter;
	private ImageFetcher mImageFetcher;

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_default_thumbnail_size);
		session = new TwitterSession(getActivity());
		stautsList = new ArrayList<HashMap<String, String>>();
		
		mAdapter=new TweetAdapter(getActivity(), stautsList);
		setListAdapter(mAdapter);
		if (mTweetsTask != null) {
			return;
		}
		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(),IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of// app memory
		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.addImageCache(getActivity().getFragmentManager(),cacheParams);
		getListView().setFastScrollEnabled(true);
		getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView,
					int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					mImageFetcher.setPauseWork(true);
				} else {
					mImageFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
			}
		});
		
		/***
		 * Fragment always suck its executing when activity created
		 */
		mTweetsTask=new RetriveTweetsTask();
		if(session.isTwitterLoggedInAlready()){
			mTweetsTask.execute();
		}else{
			mTweetsTask=null;
		}
		
	}

	class RetriveTweetsTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog pDialog;

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Retriving tweets...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);	
			pDialog.show();
		}

		/**
		 * getting Places JSON
		 * */
		protected Boolean doInBackground(String... args) {
			
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

				List<twitter4j.Status> statuses = twitter.getHomeTimeline();
				
				if (!statuses.isEmpty()) {
					for (twitter4j.Status status : statuses) {
						HashMap<String, String> map = new HashMap<String, String>();
					
						// adding each child node to HashMap key => value
						map.put(KEY_NAME, status.getUser().getName());
						map.put(KEY_PROFILE_PIC, status.getUser().getProfileImageURL());
						map.put(KEY_TWEETS, status.getText());
						map.put(KEY_CREATED_AT,String.valueOf(status.getCreatedAt().getTime()));
						stautsList.add(map);
					
		                
					}
					return true;
				}

			} catch (TwitterException e) {
				// Error in updating status
				Log.e(getTag(), "TwitterException=>" + e.getMessage());
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
			mTweetsTask = null;
			pDialog.cancel();
			if (results) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mAdapter.notifyDataSetChanged();
					}
				});
			} else {
				Toast.makeText(getActivity(), "Retry", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mTweetsTask = null;
			pDialog.cancel();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@SuppressLint("SimpleDateFormat")
	public class TweetAdapter extends BaseAdapter {

		private Context mContext;
		private ArrayList<HashMap<String, String>> data;
		public ImageFetcher imageLoader;

		public TweetAdapter(Context context, ArrayList<HashMap<String, String>> d) {
			mContext=context;
			data=d;
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder mHolder;
			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView=mInflater.inflate(R.layout.listitem, parent,false);
				mHolder=new ViewHolder();
				mHolder.username = (TextView) convertView.findViewById(R.id.txtUserName); // title
				mHolder.tweets = (TextView) convertView.findViewById(R.id.txtTweets);
				mHolder.created_at = (TextView) convertView.findViewById(R.id.txtCreatedAt);
				mHolder.thumb_image = (ImageView) convertView.findViewById(R.id.imageView1); // thumb
				convertView.setTag(mHolder);																		// image
			}else{
				mHolder=(ViewHolder)convertView.getTag();
			}
			 
			HashMap<String, String> tweets = new HashMap<String, String>();
			tweets = data.get(position);
			// Setting all values in GridView
			mHolder.username.setText(tweets.get(KEY_NAME));
			mHolder.tweets.setText(tweets.get(KEY_TWEETS));
			long milliseconds=Long.parseLong(tweets.get(KEY_CREATED_AT));
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
			Date resultdate = new Date(milliseconds);
			String date=sdf.format(resultdate);
			mHolder.created_at.setText(date);
			mImageFetcher.loadImage(tweets.get(KEY_PROFILE_PIC), mHolder.thumb_image);
			return convertView;
		}
		

	}
	static class ViewHolder{
		public TextView created_at;
		TextView username;
		TextView tweets;
		ImageView thumb_image;
	}
	
}
