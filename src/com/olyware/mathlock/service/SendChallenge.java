package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;

import com.olyware.mathlock.R;
import com.olyware.mathlock.model.GenericQuestion;
import com.olyware.mathlock.utils.ContactHelper;
import com.olyware.mathlock.utils.Loggy;

public class SendChallenge extends AsyncTask<String, Integer, Integer> {
	private String baseURL;
	private String success, error, challengeID;
	private String userID, opponentUserID;
	private List<String> questions, descriptions;
	private List<String[]> answers;
	private int bet = 0;

	public SendChallenge(Activity act, String opponentUserID, List<GenericQuestion> questions, int bet) {
		userID = ContactHelper.getUserID(act);
		this.opponentUserID = opponentUserID;
		this.descriptions = new ArrayList<String>(questions.size());
		this.questions = new ArrayList<String>(questions.size());
		this.answers = new ArrayList<String[]>(questions.size());
		for (GenericQuestion question : questions) {
			this.descriptions.add(question.getDescription());
			this.questions.add(question.getQuestion());
			this.answers.add(question.getAnswers());
		}
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

	public List<String> getDescriptions() {
		if (descriptions != null)
			return descriptions;
		else
			return new ArrayList<String>();
	}

	public List<String> getQuestions() {
		if (questions != null)
			return questions;
		else
			return new ArrayList<String>();
	}

	public List<String[]> getAnswers() {
		if (answers != null)
			return answers;
		else
			return new ArrayList<String[]>();
	}

	public int getBet() {
		return bet;
	}

	@Override
	protected Integer doInBackground(String... s) {
		if (questions.size() != answers.size() || questions.size() <= 0 || answers.size() <= 0)
			return 1;
		String endpoint = "challenge";

		// PUT to API challenge
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		DefaultHttpClient httpclient = new DefaultHttpClient(params);
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		httpclient.getParams().setParameter("http.protocol.content-charset", "UTF-8");

		HttpPut httpput = new HttpPut(baseURL + endpoint);
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			data.put("o_user_id", opponentUserID);
			data.put("c_user_id", userID);
			JSONArray descriptionArray = new JSONArray();
			for (String question : questions) {
				descriptionArray.put(question);
			}
			data.put("descriptions", descriptionArray);
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

			Loggy.d("test", "JSON to challenge: " + data.toString());
			httpput.setEntity(new StringEntity(data.toString(), "UTF-8"));
			httpput.setHeader("Content-Type", "application/json");
			HttpResponse response = httpclient.execute(httpput);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			Loggy.d("test", fullResult);
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
