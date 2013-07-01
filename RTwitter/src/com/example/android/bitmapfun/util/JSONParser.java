package com.example.android.bitmapfun.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {

	private static final String TAG = "JSONParser";

	/**
	 * 
	 * @param url
	 * @return web response
	 * @throws Exception
	 * @description execute url and return response print same thing in log with tag JsonString
	 */
	public static JSONObject getJSONResponce(String url) {
		JSONObject jsonObject=null;
		try {
			Log.d(TAG, "JSON request of :=>" +url);
			StringBuilder sb = new StringBuilder();
			HttpGet requst=new HttpGet(url.replace(" ", "%20"));	
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(requst);
			Log.d(TAG, "responce code =>" +response.getStatusLine().getStatusCode());
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String s = "";
			while ((s = br.readLine()) != null) {
				sb.append(s);
			}
			br.close();
			Log.d(TAG, "JSON responce of :=>" +url+ " is \n=>"+sb.toString());
			jsonObject=new JSONObject(sb.toString()) ;
			return jsonObject;
		} catch (ClientProtocolException e) {
			
			Log.e(TAG, "ClientProtocolException :=> " + e.getMessage());
		} catch (IllegalStateException e) {
			
			Log.e(TAG, "IllegalStateException :=> " + e.getMessage());
		} catch (IOException e) {
			
			Log.e(TAG, "IOException :=> " + e.getMessage());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "JSONException :=>" + e.getMessage());
		}
		return null;

	}
}
