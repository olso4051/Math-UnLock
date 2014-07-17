package com.olyware.mathlock.service;

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
import com.olyware.mathlock.utils.ContactHelper;
import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.PreferenceHelper;
import com.olyware.mathlock.utils.PreferenceHelper.ChallengeStatus;

public class CompleteChallenge extends AsyncTask<Void, Integer, Integer> {
	private String baseURL;
	private String success, error, challengeID, userID;
	private int score, bet;
	private Context ctx;

	public CompleteChallenge(Context ctx, String challengeID, int score, int bet) {
		this.ctx = ctx;
		this.userID = ContactHelper.getUserID(ctx);
		this.challengeID = challengeID;
		this.score = score;
		this.bet = bet;
		baseURL = ctx.getString(R.string.service_base_url);
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
	protected Integer doInBackground(Void... v) {
		String endpoint = "challenge/complete";

		// PUT to API challenge
		HttpClient httpclient = HttpClientBuilder.create().build();

		HttpPut httpput = new HttpPut(baseURL + endpoint);
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			data.put("challenge_id", challengeID);
			data.put("user_id", userID);
			data.put("score", score);
			data.put("bet", bet);

			Loggy.d("test", "JSON to " + endpoint + ": " + data.toString());
			httpput.setEntity(new StringEntity(data.toString(), ContentType.create("text/plain", "UTF-8")));
			httpput.setHeader("Content-Type", "application/json");
			HttpResponse response = httpclient.execute(httpput);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			jsonResponse = new JSONObject(fullResult);
		} catch (JSONException j) {
			j.printStackTrace();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			success = getStringFromJSON(jsonResponse, "success");
			error = getStringFromJSON(jsonResponse, "error");
			if (success.equals("true")) {
				PreferenceHelper.storeChallengeStatus(ctx, challengeID, ChallengeStatus.Done);
				return 0;
			} else
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

	private String getStringFromJSON(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException e) {
			return "";
		}
	}
}
