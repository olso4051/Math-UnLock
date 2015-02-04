package com.olyware.mathlock.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.JSONHelper;
import com.olyware.mathlock.utils.Loggy;

public class GetSponsoredAppQuestion extends AsyncTask<Void, Integer, Integer> {
	private String baseURL;
	private String userID;
	private String packHash, sponsor, description, error;
	private List<String> installedPacks, questionHashes, questions, backgroundtextUrls;
	private List<String[]> answers, urls;
	private boolean customquestion = false;
	private Context context;

	public GetSponsoredAppQuestion(Context ctx, String userID, List<String> installedPacks, boolean value) {
		baseURL = ctx.getString(R.string.service_base_url);
		context = ctx;
		this.userID = userID;
		this.installedPacks = installedPacks;
		this.questions = new ArrayList<String>();
		this.questionHashes = new ArrayList<String>();
		this.answers = new ArrayList<String[]>();
		this.urls = new ArrayList<String[]>();
		customquestion = value;
	}

	public String getPackHash() {
		if (packHash != null)
			return packHash;
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
		HttpPost httpPost = new HttpPost(baseURL + "question");
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			data.put("user_id", userID);
			// data.put("user_id", "testuser");
			JSONArray installedPacksArray = null;
			if (customquestion) {
				installedPacksArray = new JSONArray();
				for (String installedPack : installedPacks) {
					installedPacksArray.put(installedPack);
				}
				data.put("installed_packs", installedPacksArray);
			} else {
				// data.put("installed_packs", "");
			}
			Log.d("JSON to question = ", "" + data.toString());

			httpPost.setEntity(new StringEntity(data.toString(), ContentType.create("text/plain", "UTF-8")));
			httpPost.setHeader("Content-Type", "application/json");
			HttpResponse response = httpclient.execute(httpPost);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			Log.d("JSON to question = ", "" + fullResult);
			Loggy.d("fullResult = " + fullResult);
			jsonResponse = new JSONObject(fullResult);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		String error = JSONHelper.getStringFromJSON(jsonResponse, "error");
		if (error.equals("true")) {
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			packHash = JSONHelper.getStringFromJSON(jsonResponse, "hash");
			sponsor = JSONHelper.getStringFromJSON(jsonResponse, "sponsor");
			description = JSONHelper.getStringFromJSON(jsonResponse, "type");
			questionHashes = JSONHelper.getStringListFromJSON2(jsonResponse, "questions", "question_hash");
			questions = JSONHelper.getStringListFromJSON2(jsonResponse, "questions", "text");
			// String hai = "image://http://media-cache-ak0.pinimg.com/originals/1b/42/87/1b42870a08499773664d125bc3ef6600.jpg";
			// questions.clear();
			// questions.add(hai);
			answers = JSONHelper.getStringArrayListFromJSON2(jsonResponse, "questions", "answers");
			urls = getStringArrayListFromJSON2(jsonResponse, "questions", "urls");
			error = JSONHelper.getStringFromJSON(jsonResponse, "error");
			// background
			backgroundtextUrls = JSONHelper.getStringListFromJSON2(jsonResponse, "questions", "background");
			if (backgroundtextUrls != null && backgroundtextUrls.size() > 0 && !TextUtils.isEmpty(backgroundtextUrls.get(0))) {
				// download the image
				downloadandSaveImage(backgroundtextUrls.get(0), "temp");
			}

			// if question has the image
			if (questions != null && questions.size() > 0 && !TextUtils.isEmpty(questions.get(0))
					&& questions.get(0).startsWith("image://")) {
				// download the image
				downloadandSaveImage(questions.get(0).split("image://")[1], "question");
			}
			if (answers != null && answers.size() > 0 && answers.get(0) != null && answers.get(0).length > 0) {
				String[] urlArray = answers.get(0);
				for (int i = 0; i < urlArray.length; i++) {
					if (!TextUtils.isEmpty(urlArray[i]) && urlArray[i].startsWith("image://")) {
						String url = urlArray[i].split("image://")[1];
						String name = urlArray[i].substring(urlArray[i].lastIndexOf("/") + 1);
						downloadandSaveImage(url, name);
					}
				}
			}

			Loggy.d("userID = " + userID);
			Loggy.d("packHash = " + packHash);
			Loggy.d("sponsor = " + sponsor);
			Loggy.d("desc = " + description);
			Loggy.d("qhashes = " + questionHashes != null && questionHashes.size() > 0 ? questionHashes.get(0) : "0");
			Loggy.d("qs = " + questions != null && questions.size() > 0 ? questions.get(0) : "" + 0);
			if (answers != null && answers.size() > 0)
				Loggy.d("as = [" + answers.get(0)[0] + " , " + answers.get(0)[1] + " , " + answers.get(0)[2] + " , " + answers.get(0)[3]
						+ "]");
			if (urls != null && urls.size() > 0)
				Loggy.d("urls = [" + urls.get(0)[0] + " , " + urls.get(0)[1] + " , " + urls.get(0)[2] + " , " + urls.get(0)[3] + "]");
			if (questionHashes != null && questionHashes.size() > 0)
				return 0;
			else
				return 1;
		} else {
			return 1;
		}
	}

	public static List<String[]> getStringArrayListFromJSON2(JSONObject json, String key, String key2) {
		try {
			JSONArray array = json.getJSONArray(key);
			List<String[]> result = new ArrayList<String[]>(array.length());
			for (int i = 0; i < array.length(); i++) {
				JSONArray innerArray = array.getJSONObject(i).getJSONArray(key2);
				String[] results = getStringArrayFromJSONArray(innerArray);
				result.add(results);
			}
			return result;
		} catch (JSONException e) {
			return new ArrayList<String[]>();
		}
	}

	public static String[] getStringArrayFromJSONArray(JSONArray array) {
		List<String> list = getStringListFromJSONArray(array);
		return list.toArray(new String[list.size()]);
	}

	public static List<String> getStringListFromJSONArray(JSONArray array) {
		List<String> list = new ArrayList<String>();
		try {
			for (int i = 0; i < array.length(); i++) {
				list.add(array.getString(i));
			}
			return list;
		} catch (JSONException e) {
			return new ArrayList<String>();
		}
	}

	private void downloadandSaveImage(String url, String filename) {

		File fileForImage = new File(context.getExternalCacheDir().getAbsolutePath() + "/" + filename);

		InputStream sourceStream;
		ImageDownloader downloader = new BaseImageDownloader(context);
		try {
			// Log.d("TAG", "backgroundtextUrls : " + backgroundtextUrls.get(0));
			sourceStream = downloader.getStream(url, null);
			// sourceStream = downloader.getStream("http://shechive.files.wordpress.com/2010/09/beautiful-nature-151.jpg", null);

			if (sourceStream != null) {
				try {
					OutputStream targetStream = new FileOutputStream(fileForImage);
					try {
						IoUtils.copyStream(sourceStream, targetStream, null);
					} finally {
						targetStream.close();
					}
				} finally {
					sourceStream.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onPostExecute(Integer result) {
		// override in calling class
		// result == 0 success
	}
}
