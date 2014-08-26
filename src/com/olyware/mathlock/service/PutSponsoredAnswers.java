package com.olyware.mathlock.service;

import org.json.JSONArray;
import org.json.JSONException;
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
import com.olyware.mathlock.utils.Loggy;

public class PutSponsoredAnswers extends AsyncTask<Void, Integer, Integer> {
	private String baseURL;
	private String userID, hash, answers;

	public PutSponsoredAnswers(Context ctx, String userID, String hash, String answers) {
		if (userID != null)
			this.userID = userID;
		else
			this.userID = "";
		if (hash != null)
			this.hash = hash;
		else
			this.hash = "";
		if (answers != null)
			this.answers = answers;
		else
			this.answers = "";
		baseURL = ctx.getString(R.string.service_base_url);
	}

	@Override
	protected Integer doInBackground(Void... v) {
		String endpoint = "questions/answer";

		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPut httpput = new HttpPut(baseURL + endpoint);
		HttpEntity entity = null;
		String fullResult = null;
		try {
			JSONObject data = new JSONObject();
			data.put("user_id", userID);
			data.put("hash", hash);
			data.put("answers", new JSONArray(answers));

			Loggy.d("test", "JSON to " + endpoint + ": " + data.toString());
			httpput.setEntity(new StringEntity(data.toString(), ContentType.create("text/plain", "UTF-8")));
			httpput.setHeader("Content-Type", "application/json");
			HttpResponse response = httpClient.execute(httpput);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			Loggy.d("test", fullResult);
		} catch (JSONException j) {
			j.printStackTrace();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		return 0;
	}
}
