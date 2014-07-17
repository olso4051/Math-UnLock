package com.olyware.mathlock.service;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.olyware.mathlock.R;

public class GetSponsoredQuestion extends AsyncTask<String, Integer, Integer> {
	final private int NumAnswers = 4;
	private String baseURL;
	private String questionHash, question, error;
	private String[] answers = new String[NumAnswers];
	private String[] links = new String[NumAnswers];

	public GetSponsoredQuestion(Context ctx) {
		baseURL = ctx.getString(R.string.service_base_url);
	}

	public String getQuestionHash() {
		if (questionHash != null)
			return questionHash;
		else
			return "";
	}

	public String getQuestion() {
		if (question != null)
			return question;
		else
			return "";
	}

	public String[] getAnswers() {
		boolean badAnswers = false;
		for (int i = 0; i < NumAnswers; i++) {
			if (answers[i].equals(""))
				badAnswers = true;
		}

		if (!badAnswers)
			return answers;
		else
			return new String[] { "", "", "", "" };
	}

	public String[] getLinks() {
		return links;
	}

	public boolean isWellDefined() {
		boolean badAnswers = false;
		for (int i = 0; i < NumAnswers; i++) {
			if (answers[i].equals(""))
				badAnswers = true;
		}
		return !badAnswers;
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
		HttpGet httpget = new HttpGet(baseURL + "question");
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			// String authorizationString = "Basic " + Base64.encodeToString(("roll" + ":" + "over").getBytes(), Base64.NO_WRAP);
			// httpget.setHeader("Authorization", authorizationString);
			httpget.setHeader("Content-Type", "application/json");
			HttpResponse response = httpclient.execute(httpget);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			jsonResponse = new JSONObject(fullResult);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			questionHash = getStringFromJSON(jsonResponse, "question_hash");
			question = getStringFromJSON(jsonResponse, "text");
			answers = getStringArrayFromJSON(jsonResponse, "answers");
			links = getStringArrayFromJSON(jsonResponse, "urls");
			error = getStringFromJSON(jsonResponse, "error");
			if (!error.equals(""))
				return 1;
			else
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

	private String getStringFromJSON(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException e) {
			return "";
		}
	}

	private String[] getStringArrayFromJSON(JSONObject json, String key) {
		String[] strings = { "", "", "", "" };
		try {
			JSONObject array = json.getJSONObject(key);
			/*for (int i=0;i<NumAnswers;i++)
				strings[i]=array.getString(""+i);*/
			strings[0] = array.getString("a");
			strings[1] = array.getString("b");
			strings[2] = array.getString("c");
			strings[3] = array.getString("d");
			return strings;
		} catch (JSONException e) {
			return strings;
		}
	}
}
