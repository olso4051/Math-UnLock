package com.olyware.mathlock.service;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.utils.URLEncodedUtils;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.Loggy;

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
		HttpClient httpclient = HttpClientBuilder.create().build();

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
			Loggy.d("test", "Params: " + params);
			Loggy.d("test", "url = " + baseURL + params);
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
