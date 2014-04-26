package com.olyware.mathlock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Base64;

public class RegisterID extends AsyncTask<String, Integer, Integer> {
	private String baseURL;
	private String userID;
	RegisterIdResponse mCallback;

	public interface RegisterIdResponse {
		void registrationResult(int result, String userID);
	}

	public RegisterID(Activity act) {
		baseURL = act.getString(R.string.service_base_url);
		try {
			mCallback = (RegisterIdResponse) act;
		} catch (ClassCastException e) {
			throw new ClassCastException(act.toString() + " must implement RegisterIdResponse");
		}
	}

	public String getUserID() {
		if (userID != null)
			return userID;
		else
			return "";
	}

	@Override
	protected Integer doInBackground(String... s) {
		// POST to API with old and new registration, also referral's registration
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		HttpPost httppost = new HttpPost(baseURL + "register");
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			if (s[0].length() > 0) {
				data.put("username", s[0]);
			}
			if (s[1].length() > 0) {
				data.put("password", s[1]);
			}
			if (s[2].length() > 0) {
				data.put("registration_id", s[2]);
			}
			if (s[3].length() > 0) {
				data.put("user_id", s[3]);
				data.put("status", "UPDATE");
			} else {
				data.put("status", "NEW");
			}
			if (s[4].length() > 0) {
				data.put("referral", s[4]);
			}
			String authorizationString = "Basic " + Base64.encodeToString(("roll" + ":" + "over").getBytes(), Base64.NO_WRAP);
			httppost.setEntity(new StringEntity(data.toString()));
			httppost.setHeader("Content-Type", "application/json");
			httppost.setHeader("Authorization", authorizationString);
			HttpResponse response = httpclient.execute(httppost);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			jsonResponse = new JSONObject(fullResult);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			try {
				userID = jsonResponse.getString("user_id");
			} catch (JSONException e) {
				e.printStackTrace();
				return 1;
			}
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		mCallback.registrationResult(result, userID);
	}
}
