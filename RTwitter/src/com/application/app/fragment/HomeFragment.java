package com.application.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.application.app.rtwitter.R;
import com.application.app.rtwitter.TwitterSession;

public class HomeFragment extends Fragment implements OnClickListener {
	TwitterSession session;
	Button mTwitterUpdateButton; 
	EditText mStatusText;
	OnTwitterUpdateListener listener;
	public interface OnTwitterUpdateListener{
		public void onTwtterUpdatClick(EditText mStatusText);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		 View view = inflater.inflate(R.layout.home, container, false);
		 TextView textView=(TextView)view.findViewById(R.id.txtUserName);
		 mTwitterUpdateButton=(Button)view.findViewById(R.id.btn_upd);
		 mStatusText=(EditText)view.findViewById(R.id.status);
		 mTwitterUpdateButton.setOnClickListener(this);
		 session=new TwitterSession(getActivity());
		 textView.setText(session.getUserName());
		return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			listener=(OnTwitterUpdateListener) activity;
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			Log.e(getTag(),String.format("%s Must Implement %s", activity.toString(),listener.toString()));
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		
			listener.onTwtterUpdatClick(mStatusText);

		
	}
}
