package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

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

public class GetSponsoredQuestions extends AsyncTask<Void, Integer, Integer> {
	private String baseURL;
	private String userID;
	private String questionHash, sponsor, description, error;
	private List<String> questionHashes, questions;
	private List<String[]> answers, urls;

	public GetSponsoredQuestions(Context ctx, String userID) {
		baseURL = ctx.getString(R.string.service_base_url);
		this.userID = userID;
		this.questionHashes = new ArrayList<String>();
		this.questions = new ArrayList<String>();
		this.answers = new ArrayList<String[]>();
		this.urls = new ArrayList<String[]>();
	}

	public String getQuestionHash() {
		if (questionHash != null)
			return questionHash;
		else
			return "";
	}

	public String getSponsor() {
		if (sponsor != null)
			return sponsor;
		else
			return "";
	}

	public String getDescription() {
		if (description != null)
			return description;
		else
			return "";
	}

	public List<String> getQuestionHashes() {
		if (questionHashes != null)
			return questionHashes;
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

	public List<String[]> getURLs() {
		if (urls != null)
			return urls;
		else
			return new ArrayList<String[]>();
	}

	public String getError() {
		if (error != null)
			return error;
		else
			return "";
	}

	@Override
	protected Integer doInBackground(Void... v) {
		// PUT to API with user_id
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpPut httpput = new HttpPut(baseURL + "questions");
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			data.put("user_id", userID);

			httpput.setEntity(new StringEntity(data.toString(), ContentType.create("text/plain", "UTF-8")));
			httpput.setHeader("Content-Type", "application/json");
			HttpResponse response = httpclient.execute(httpput);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			jsonResponse = new JSONObject(fullResult);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			questionHash = JSONHelper.getStringFromJSON(jsonResponse, "hash");
			sponsor = JSONHelper.getStringFromJSON(jsonResponse, "sponsor");
			description = JSONHelper.getStringFromJSON(jsonResponse, "description");
			questionHashes = JSONHelper.getStringListFromJSON2(jsonResponse, "questions", "question_hash");
			questions = JSONHelper.getStringListFromJSON2(jsonResponse, "questions", "text");
			answers = JSONHelper.getStringArrayListFromJSON2(jsonResponse, "questions", "answers");
			urls = JSONHelper.getStringArrayListFromJSON2(jsonResponse, "questions", "urls");
			error = JSONHelper.getStringFromJSON(jsonResponse, "error");
			if (!questionHash.equals(""))
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
