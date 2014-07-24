package com.olyware.mathlock.service;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.methods.HttpPut;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.ShareHelper;

public class ConfirmID extends AsyncTask<Void, Integer, Integer> {
	final public static String AlreadyConfirmed = "User_id already validated.";
	final private static String HASH_ID_USER = "user_id";
	final private static String HASH_ID_SHARE = "hash";

	private String baseURL, endpoint;
	private String hashID, hash;
	private String success, error;
	private ConfirmType type;

	public static enum ConfirmType {
		USER_ID, SHARE_HASH
	}

	public ConfirmID(Context ctx, ConfirmType type, String hash) {
		this.type = type;
		switch (type) {
		case USER_ID:
			hashID = HASH_ID_USER;
			baseURL = ctx.getString(R.string.service_base_url);
			break;
		case SHARE_HASH:
			hashID = HASH_ID_SHARE;
			baseURL = ShareHelper.DEELDAT_BASE_URL;
			break;
		default:
			hashID = HASH_ID_USER;
			baseURL = ctx.getString(R.string.service_base_url);
		}
		this.hash = hash;
		this.endpoint = "confirm";
	}

	public String getSuccess() {
		if (success == null)
			return "";
		else
			return success;
	}

	public String getError() {
		if (error == null)
			return "";
		else
			return error;
	}

	@Override
	protected Integer doInBackground(Void... v) {
		// PUT to API with user_id
		HttpClient httpclient = HttpClientBuilder.create().build();
		Loggy.d("confirm to " + baseURL + endpoint);
		HttpPut httpput = new HttpPut();
		HttpPost httppost = new HttpPost();
		if (type.equals(ConfirmType.USER_ID))
			httpput = new HttpPut(baseURL + endpoint);
		else if (type.equals(ConfirmType.SHARE_HASH))
			httppost = new HttpPost(baseURL + endpoint);
		else
			return 1;
		HttpResponse response;
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			data.put(hashID, hash);
			Loggy.d("json to confirm = " + data.toString());

			String authorizationString = "Basic " + Base64.encodeToString(("roll" + ":" + "over").getBytes(), Base64.NO_WRAP);
			if (type.equals(ConfirmType.USER_ID)) {
				httpput.setEntity(new StringEntity(data.toString(), ContentType.create("text/plain", "UTF-8")));
				httpput.setHeader("Content-Type", "application/json");
				httpput.setHeader("Authorization", authorizationString);
				response = httpclient.execute(httpput);
			} else if (type.equals(ConfirmType.SHARE_HASH)) {
				httppost.setEntity(new StringEntity(data.toString(), ContentType.create("text/plain", "UTF-8")));
				httppost.setHeader("Content-Type", "application/json");
				httppost.setHeader("Authorization", authorizationString);
				response = httpclient.execute(httppost);
			} else
				return 1;
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			Loggy.d("fullResult from confirm = " + fullResult);
			jsonResponse = new JSONObject(fullResult);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			success = getStringFromJSON(jsonResponse, "success");
			error = getStringFromJSON(jsonResponse, "error");
			if (success.equals("true"))
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

	private String getStringFromJSON(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException e) {
			return "";
		}
	}
}
