package com.olyware.mathlock.service;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;

import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.PreferenceHelper;
import com.olyware.mathlock.utils.ShareHelper;

public class PostDeelDatPlayhaven extends AsyncTask<Void, Integer, Integer> {
	private final static String ENDPOINT = "playhaven";
	private final static String USERID = "userid";
	private Context ctx;

	public PostDeelDatPlayhaven(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected Integer doInBackground(Void... v) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(ShareHelper.DEELDAT_BASE_URL + ENDPOINT);
		try {
			JSONObject data = new JSONObject();
			data.put(USERID, PreferenceHelper.getUserID(ctx));
			Loggy.d("JSON to playhaven: " + data.toString());
			httpPost.setEntity(new StringEntity(data.toString(), ContentType.create("text/plain", "UTF-8")));
			httpPost.setHeader("Content-Type", "application/json");
			httpClient.execute(httpPost);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		return 0;
	}

	@Override
	protected void onPostExecute(Integer result) {
		// override in calling class
		// result == 0 success
	}
}
