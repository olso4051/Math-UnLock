package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.ContactHelper;

public class SendChallenge extends AsyncTask<String, Integer, Integer> {
	private String baseURL;
	private String success, error, challengeID;
	private String userID, challengerType, challenger;
	private List<String> questions;
	private List<String[]> answers;
	private int bet;

	public static enum FriendType {
		UserID, Facebook;
	}

	public SendChallenge(Activity act, FriendType type, String challenger, List<String> questions, List<String[]> answers, int bet) {
		userID = ContactHelper.getUserID(act);
		switch (type) {
		case UserID:
			challengerType = "o_user_id";
			break;
		case Facebook:
			challengerType = "o_facebook_hash";
			break;
		}
		this.challenger = challenger;
		this.questions = new ArrayList<String>(questions.size());
		this.questions.addAll(questions);
		this.answers = new ArrayList<String[]>(answers.size());
		this.answers.addAll(answers);
		this.bet = bet;
		baseURL = act.getString(R.string.service_base_url);
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

	public String getChallengeID() {
		if (challengeID != null)
			return challengeID;
		else
			return "";
	}

	@Override
	protected Integer doInBackground(String... s) {
		if (questions.size() != answers.size() || questions.size() <= 0 || answers.size() <= 0)
			return 1;
		String endpoint = "challenge";

		// POST to API with old and new registration, also referral's registration
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);

		HttpPut httpput = new HttpPut(baseURL + endpoint);
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			data.put(challengerType, challenger);
			data.put("c_user_id", userID);
			JSONArray questionArray = new JSONArray();
			for (String question : questions) {
				questionArray.put(question);
			}
			data.put("questions", questionArray);
			JSONArray answerArray = new JSONArray();
			for (String[] answerSet : answers) {
				JSONArray answerSetArray = new JSONArray();
				for (int i = 0; i < answerSet.length; i++) {
					answerSetArray.put(answerSet[i]);
				}
				answerArray.put(answerSetArray);
			}
			data.put("answers", answerArray);
			data.put("bet", bet);

			Log.d("test", "JSON to register: " + data.toString());
			// String authorizationString = "Basic " + Base64.encodeToString(("roll" + ":" + "over").getBytes(), Base64.NO_WRAP);
			httpput.setEntity(new StringEntity(data.toString()));
			httpput.setHeader("Content-Type", "application/json");
			// httpput.setHeader("Authorization", authorizationString);
			HttpResponse response = httpclient.execute(httpput);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			Log.d("test", fullResult);
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
			challengeID = getStringFromJSON(jsonResponse, "challenge_id");
			if (success.equals("true")) {
				storeChallenge();
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

	private boolean getBooleanFromJSON(JSONObject json, String key) {
		try {
			return json.getBoolean(key);
		} catch (JSONException e) {
			return false;
		}
	}

	private void storeChallenge() {
		// TODO store challenge in challenge DB table
	}
}
