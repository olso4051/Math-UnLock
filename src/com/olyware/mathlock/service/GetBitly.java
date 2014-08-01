package com.olyware.mathlock.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.utils.URLEncodedUtils;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.JSONHelper;
import com.olyware.mathlock.utils.Loggy;

public class GetBitly extends AsyncTask<Void, Integer, Integer> {
	private String baseURL, queryAccess, queryLongURL;
	private String accessToken, originalURL, longURL, shortURL, success, error;
	private Context ctx;

	public GetBitly(Context ctx, String longURL, boolean encode) {
		this.ctx = ctx;
		baseURL = ctx.getString(R.string.bitly_base_url);
		queryAccess = ctx.getString(R.string.bitly_query_access_token);
		queryLongURL = ctx.getString(R.string.bitly_query_long_url);
		accessToken = ctx.getString(R.string.bitly_access_token);
		this.originalURL = longURL;
		if (encode) {
			try {
				this.longURL = URLEncoder.encode(longURL, "utf-8");
			} catch (UnsupportedEncodingException e) {
				this.longURL = longURL;
			}
		} else
			this.longURL = longURL;
	}

	public String getShortURL() {
		if (shortURL != null)
			return shortURL;
		else
			return originalURL;
	}

	@Override
	protected Integer doInBackground(Void... v) {

		// POST to API with old and new registration, also referral's registration
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair(queryAccess, accessToken));
			pairs.add(new BasicNameValuePair(queryLongURL, longURL));
			String params = URLEncodedUtils.format(pairs, "utf-8");
			Loggy.d("url to bitly = " + baseURL + params);
			HttpGet httpget = new HttpGet(baseURL + params);
			HttpResponse response = httpclient.execute(httpget);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			Loggy.d("fullResult = " + fullResult);
			jsonResponse = new JSONObject(fullResult);

		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			success = JSONHelper.getStringFromJSON(jsonResponse, "success");
			error = JSONHelper.getStringFromJSON(jsonResponse, "error");
			shortURL = JSONHelper.getStringFromJSON2(jsonResponse, "data", "url");
			if (success.equals("true"))
				return 0;
			else
				return 1;
		} else {
			return 1;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		// override in calling class
		// result == 0 success
	}
}
