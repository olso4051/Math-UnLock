package com.olyware.mathlock.service;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.olyware.mathlock.R;

public class PostChirpAds extends AsyncTask<String, Integer, Integer> {
	private String baseURL;
	private String success, error;
	private Context ctx;

	public PostChirpAds(Context ctx) {
		this.ctx = ctx;
		baseURL = ctx.getString(R.string.chirpads_base_url);
	}

	public String getSuccess() {
		if (success != null)
			return success;
		else
			return "";
	}

	public String getError() {
		if (error != null)
			return error;
		else
			return "";
	}

	@Override
	protected Integer doInBackground(String... s) {

		// POST to API with old and new registration, also referral's registration
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);

		try {
			ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
			if (s[0].length() > 0)
				pairs.add(new BasicNameValuePair("action", s[0]));
			if (s[1].length() > 0)
				pairs.add(new BasicNameValuePair("clickGuid", s[1]));
			if (s[2].length() > 0)
				pairs.add(new BasicNameValuePair("os", s[2]));
			if (s[3].length() > 0)
				pairs.add(new BasicNameValuePair("osVersion", s[3]));
			if (s[4].length() > 0)
				pairs.add(new BasicNameValuePair("appPackageName", s[4]));
			if (s[5].length() > 0)
				pairs.add(new BasicNameValuePair("externalAdId", s[5]));
			pairs.add(new BasicNameValuePair("androidId", Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID)));
			String params = URLEncodedUtils.format(pairs, "utf-8");
			Log.d("test", "Params: " + params);
			Log.d("test", "url = " + baseURL + params);
			HttpPost httppost = new HttpPost(baseURL + params);
			HttpResponse response = httpclient.execute(httppost);
			/*entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			jsonResponse = new JSONObject(fullResult);*/
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		/*if (entity != null && fullResult != null && jsonResponse != null) {
			success = getStringFromJSON(jsonResponse, "success");
			error = getStringFromJSON(jsonResponse, "error");
			if (success.equals("true"))
				return 0;
			else
				return 1;
		} else {
			return 1;
		}*/
	}

	@Override
	protected void onPostExecute(Integer result) {
		// override in calling class
		// result == 0 success
	}

	private String getStringFromJSON(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException e) {
			return "";
		}
	}

	private boolean getBooleanFromJSON(JSONObject json, String key) {
		try {
			return json.getBoolean(key);
		} catch (JSONException e) {
			return false;
		}
	}
}
