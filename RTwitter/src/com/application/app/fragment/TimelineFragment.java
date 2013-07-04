package com.application.app.fragment;

import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.application.app.loader.ListLoader;
import com.application.app.rtwitter.R;
import com.application.app.rtwitter.TimelineActivity;
import com.example.android.bitmapfun.util.ImageCache.ImageCacheParams;
import com.example.android.bitmapfun.util.ImageFetcher;

public class TimelineFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<HashMap<String, String>>>,TimelineActivity.OnRsetListListener{
	private static final String IMAGE_CACHE_DIR = "thumbs";
	private static final String TAG = "TimelineFragment";
	private int mImageThumbSize;
	private TweetAdapter mAdapter;
	private ImageFetcher mImageFetcher;
	private IntentFilter intentFilter;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		intentFilter=new IntentFilter();
		intentFilter.addAction(TimelineActivity.RELOAD_LOADER);
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_default_thumbnail_size);
		mAdapter = new TweetAdapter(getActivity());
		setListAdapter(mAdapter);
		setListShown(false);

		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(),IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory
		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(),cacheParams);
		getListView().setFastScrollEnabled(true);
		getListView().setSmoothScrollbarEnabled(true);
		getListView().setOnScrollListener(onScrollListener);
		getLoaderManager().initLoader(0, null, this);

	}

	AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
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
		public void onScroll(AbsListView absListView, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	};

	@Override
	public Loader<List<HashMap<String, String>>> onCreateLoader(int id,Bundle argg) {
		ListLoader listLoader=new ListLoader(getActivity());
		listLoader.setUpdateThrottle(2000);
		Log.d(TAG, "ListLoader update called");
		return listLoader;
	}

	@Override
	public void onLoadFinished(Loader<List<HashMap<String, String>>> loader,List<HashMap<String, String>> data) {
		mAdapter.setData(data);
		// The list should now be shown.
		Log.d(TAG, "Loader=>" + data.toArray().toString());
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}

	}
	
	
	
	@Override
	public void onLoaderReset(Loader<List<HashMap<String, String>>> loader) {
		mAdapter.setData(null);
	}

	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		mAdapter.notifyDataSetChanged();
		getActivity().registerReceiver(receiver, intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
		getActivity().unregisterReceiver(receiver);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@SuppressLint("SimpleDateFormat")
	public class TweetAdapter extends ArrayAdapter<HashMap<String, String>> {
		private Context mContext;
		public ImageFetcher imageLoader;
		private HashMap<String, String> tweets;

		public TweetAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_2);
			mContext = context;
			tweets = new HashMap<String, String>();

		}

		public void setData(List<HashMap<String, String>> data) {
			
			clear();
			if (data != null) {
				for (HashMap<String, String> hashMap : data) {
					add(hashMap);
				}
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder mHolder;
			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.listitem, parent,false);
				mHolder = new ViewHolder();
				mHolder.username = (TextView) convertView.findViewById(R.id.txtUserName); // username
				mHolder.tweets = (TextView) convertView.findViewById(R.id.txtTweets);//tweets
				mHolder.created_at = (TextView) convertView.findViewById(R.id.txtCreatedAt);
				mHolder.thumb_image = (ImageView) convertView.findViewById(R.id.imageView1); // thumb
				convertView.setTag(mHolder); 
			} else {
				mHolder = (ViewHolder) convertView.getTag();
			}

			tweets = getItem(position);
			// Setting all values in GridView
			mHolder.username.setText(tweets.get(ListLoader.KEY_NAME));
			mHolder.tweets.setText(tweets.get(ListLoader.KEY_TWEETS));
			long milliseconds = Long.parseLong(tweets.get(ListLoader.KEY_CREATED_AT));
			mHolder.created_at.setText(DateUtils.getRelativeTimeSpanString(milliseconds));
			mImageFetcher.loadImage(tweets.get(ListLoader.KEY_PROFILE_PIC),mHolder.thumb_image);
			return convertView;
		}

	}

	static class ViewHolder {
		public TextView created_at;
		TextView username;
		TextView tweets;
		ImageView thumb_image;
	}

	@Override
	public void onReset() {
		int i=0;
		Log.d(TAG, "onReset=>click"+(i++));
		getLoaderManager().restartLoader(0, null, this);
	}
	BroadcastReceiver receiver=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getExtras().getString("rel").equals(TimelineActivity.RELOAD)){
				onReset();
			}
		}
	};
}
