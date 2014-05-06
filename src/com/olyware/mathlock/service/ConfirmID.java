package com.olyware.mathlock.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;

import com.olyware.mathlock.R;

public class ConfirmID extends AsyncTask<String, Integer, Integer> {
	private String baseURL;
	private boolean success, error;
	ConfirmIdResponse mCallback;

	public interface ConfirmIdResponse {
		void confirmIDResult(int result);
	}

	public ConfirmID(Context ctx) {
		baseURL = ctx.getString(R.string.service_base_url);
		try {
			mCallback = (ConfirmIdResponse) ctx;
		} catch (ClassCastException e) {
			throw new ClassCastException(ctx.toString() + " must implement ConfirmIdResponse");
		}
	}

	public boolean getSuccess() {
		return success;
	}

	public boolean getError() {
		return error;
	}

	@Override
	protected Integer doInBackground(String... s) {
		// PUT to API with user_id
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		HttpPut httpput = new HttpPut(baseURL + "confirm");
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			if (s[0].length() > 0) {
				data.put("user_id", s[0]);
			}
			String authorizationString = "Basic " + Base64.encodeToString(("roll" + ":" + "over").getBytes(), Base64.NO_WRAP);
			httpput.setEntity(new StringEntity(data.toString()));
			httpput.setHeader("Content-Type", "application/json");
			httpput.setHeader("Authorization", authorizationString);
			HttpResponse response = httpclient.execute(httpput);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			jsonResponse = new JSONObject(fullResult);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			success = getBooleanFromJSON(jsonResponse, "success");
			error = getBooleanFromJSON(jsonResponse, "error");
			if (success)
				return 0;
			else
				return 1;
		} else {
			return 1;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		mCallback.confirmIDResult(result);
	}

	private boolean getBooleanFromJSON(JSONObject json, String key) {
		try {
			return json.getBoolean(key);
		} catch (JSONException e) {
			return false;
		}
	}
}