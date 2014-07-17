package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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
import com.olyware.mathlock.model.GenericQuestion;
import com.olyware.mathlock.utils.ContactHelper;
import com.olyware.mathlock.utils.Loggy;

public class SendChallenge extends AsyncTask<Void, Integer, Integer> {
	private String baseURL;
	private String success, error, challengeID;
	private String userID, opponentUserID;
	private List<GenericQuestion> genericQuestions;
	private List<String> questions, descriptions;
	private List<String[]> answers;
	private int bet = 0, difficultyMin, difficultyMax;

	public SendChallenge(Activity act, String opponentUserID, List<GenericQuestion> questions, int bet, int difficultyMin, int difficultyMax) {
		Loggy.d("SendChallenge");
		userID = ContactHelper.getUserID(act);
		this.opponentUserID = opponentUserID;
		this.genericQuestions = new ArrayList<GenericQuestion>(questions.size());
		this.genericQuestions.addAll(questions);
		this.descriptions = new ArrayList<String>(questions.size());
		this.questions = new ArrayList<String>(questions.size());
		this.answers = new ArrayList<String[]>(questions.size());
		for (GenericQuestion question : questions) {
			this.descriptions.add(question.getDescription());
			this.questions.add(question.getQuestion());
			this.answers.add(question.getAnswers());
		}
		this.bet = bet;
		this.difficultyMin = difficultyMin;
		this.difficultyMax = difficultyMax;
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

	public List<GenericQuestion> getGenericQuestions() {
		if (genericQuestions != null)
			return genericQuestions;
		else
			return new ArrayList<GenericQuestion>();
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

	public int getDifficultyMin() {
		return difficultyMin;
	}

	public int getDifficultyMax() {
		return difficultyMax;
	}

	@Override
	protected Integer doInBackground(Void... v) {
		Loggy.d("questions size = " + questions.size());
		Loggy.d("answers size = " + answers.size());
		if (questions.size() != answers.size() || questions.size() <= 0 || answers.size() <= 0)
			return 1;
		String endpoint = "challenge";

		// PUT to API challenge
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPut httpput = new HttpPut(baseURL + endpoint);
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			data.put("o_user_id", opponentUserID);
			data.put("c_user_id", userID);
			JSONArray descriptionArray = new JSONArray();
			for (String description : descriptions) {
				descriptionArray.put(description);
			}
			data.put("descriptions", descriptionArray);

			JSONArray questionArray = new JSONArray();
			for (String question : questions) {
				descriptionArray.put(question);
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
			data.put("difficulty_min", difficultyMin);
			data.put("difficulty_max", difficultyMax);

			Loggy.d("JSON to challenge: " + data.toString());
			httpput.setEntity(new StringEntity(data.toString(), ContentType.create("text/plain", "UTF-8")));
			httpput.setHeader("Content-Type", "application/json");
			HttpResponse response = httpClient.execute(httpput);
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
				return 0;
			} else
				return 1;
		} else {
			return 1;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		// Store the list of generic questions in the database in the calling thread
	}

	private String getStringFromJSON(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException e) {
			return "";
		}
	}
}
