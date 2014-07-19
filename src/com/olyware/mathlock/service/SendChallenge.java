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
import com.olyware.mathlock.utils.JSONHelper;
import com.olyware.mathlock.utils.Loggy;

public class SendChallenge extends AsyncTask<Void, Integer, Integer> {
	final private static String Endpoint = "challenge";
	final private static String OpponentUserID = "o_user_id";
	final private static String ChallengerUserID = "c_user_id";
	final private static String ChallengeID = "challenge_id";
	final private static String Descs = "descriptions";
	final private static String Quests = "questions";
	final private static String Answers = "answers";
	final private static String Bet = "bet";
	final private static String DiffMin = "difficulty_min";
	final private static String DiffMax = "difficulty_max";
	final private static String Success = "success";
	final private static String Error = "error";

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

	public int getQuestionNumber() {
		if (genericQuestions != null)
			return genericQuestions.size();
		else
			return 0;
	}

	@Override
	protected Integer doInBackground(Void... v) {
		Loggy.d("questions size = " + questions.size());
		Loggy.d("answers size = " + answers.size());
		if (questions.size() != answers.size() || questions.size() <= 0 || answers.size() <= 0)
			return 1;

		// PUT to API challenge
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPut httpput = new HttpPut(baseURL + Endpoint);
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			data.put(OpponentUserID, opponentUserID);
			data.put(ChallengerUserID, userID);
			JSONArray descriptionArray = new JSONArray();
			for (String description : descriptions) {
				descriptionArray.put(description);
			}
			data.put(Descs, descriptionArray);

			JSONArray questionArray = new JSONArray();
			for (String question : questions) {
				questionArray.put(question);
			}
			data.put(Quests, questionArray);

			JSONArray answerArray = new JSONArray();
			for (String[] answerSet : answers) {
				JSONArray answerSetArray = new JSONArray();
				for (int i = 0; i < answerSet.length; i++) {
					answerSetArray.put(answerSet[i]);
				}
				answerArray.put(answerSetArray);
			}
			data.put(Answers, answerArray);
			data.put(Bet, bet);
			data.put(DiffMin, difficultyMin);
			data.put(DiffMax, difficultyMax);

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
			success = JSONHelper.getStringFromJSON(jsonResponse, Success);
			error = JSONHelper.getStringFromJSON(jsonResponse, Error);
			challengeID = JSONHelper.getStringFromJSON(jsonResponse, ChallengeID);
			bet = JSONHelper.getIntFromJSON(jsonResponse, Bet);
			difficultyMin = JSONHelper.getIntFromJSON(jsonResponse, DiffMin);
			difficultyMax = JSONHelper.getIntFromJSON(jsonResponse, DiffMax);
			getGenericQuestionsFromJSON(jsonResponse, Descs, Quests, Answers);
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

	private void getGenericQuestionsFromJSON(JSONObject json, String descKey, String quesKey, String ansKey) {
		genericQuestions.clear();

		descriptions.clear();
		descriptions.addAll(JSONHelper.getStringListFromJSON(json, descKey));

		questions.clear();
		questions.addAll(JSONHelper.getStringListFromJSON(json, quesKey));

		answers.clear();
		answers.addAll(JSONHelper.getStringArrayListFromJSON(json, ansKey));
		if (descriptions.size() == questions.size() && questions.size() == answers.size() && answers.size() == descriptions.size()) {
			for (int i = 0; i < descriptions.size(); i++) {
				genericQuestions.add(new GenericQuestion(descriptions.get(i), questions.get(i), answers.get(i)));
			}
		}
	}
}
