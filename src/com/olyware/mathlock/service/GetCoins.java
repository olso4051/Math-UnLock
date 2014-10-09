package com.olyware.mathlock.service;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPut;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.JSONHelper;

public class GetCoins extends AsyncTask<String, Integer, Integer> {
	private String baseURL;
	private String error;
	private int coins;

	public GetCoins(Context ctx) {
		baseURL = ctx.getString(R.string.service_base_url);
	}

	public int getCoins() {
		if (coins < 0)
			return 0;
		else
			return coins;
	}

	public String getError() {
		if (error != null)
			return error;
		else
			return "";
	}

	@Override
	protected Integer doInBackground(String... s) {
		// PUT to API with user_id
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpPut httpput = new HttpPut(baseURL + "coin");
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			if (s[0].length() > 0) {
				data.put("user_id", s[0]);
			}
			if (s[1].length() > 0) {
				data.put("pickup_hash", s[1]);
			}
			// String authorizationString = "Basic " + Base64.encodeToString(("roll" + ":" + "over").getBytes(), Base64.NO_WRAP);
			httpput.setEntity(new StringEntity(data.toString(), ContentType.create("text/plain", "UTF-8")));
			httpput.setHeader("Content-Type", "application/json");
			// httpput.setHeader("Authorization", authorizationString);
			HttpResponse response = httpclient.execute(httpput);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			jsonResponse = new JSONObject(fullResult);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			coins = JSONHelper.getIntFromJSON(jsonResponse, "coins");
			error = JSONHelper.getStringFromJSON(jsonResponse, "error");
			return 0;
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
