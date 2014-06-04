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

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.olyware.mathlock.R;

public class RegisterID extends AsyncTask<String, Integer, Integer> {
	private String baseURL;
	private String success, error;

	public interface RegisterIdResponse {
		void registrationResult(int result);
	}

	public RegisterID(Activity act) {
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

	@Override
	protected Integer doInBackground(String... s) {
		// POST to API with old and new registration, also referral's registration
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		String endpoint = "register";
		if (s[2].length() > 0) {
			endpoint = endpoint + "/update";
			s[3] = "";
		}
		HttpPut httpput = new HttpPut(baseURL + endpoint);
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			if (s[0].length() > 0) {
				data.put("username", s[0]);
				Log.d("GAtest", "username:" + s[0]);
			}
			if (s[1].length() > 0) {
				data.put("registration_id", s[1]);
				Log.d("GAtest", "registration_id:" + s[1]);
			}
			if (s[2].length() > 0) {
				data.put("user_id", s[2]);
				Log.d("GAtest", "user_id:" + s[2]);
			}
			if (s[3].length() > 0) {
				data.put("referral", s[3]);
				Log.d("GAtest", "referral:" + s[3]);
			}
			if (s[4].length() > 0) {
				data.put("birthday", s[4]);
				Log.d("GAtest", "birthday:" + s[4]);
			}
			if (s[5].length() > 0) {
				data.put("gender", s[5]);
				Log.d("GAtest", "gender:" + s[5]);
			}
			if (s[6].length() > 0) {
				data.put("location", s[6]);
				Log.d("GAtest", "location:" + s[6]);
			}
			// String authorizationString = "Basic " + Base64.encodeToString(("roll" + ":" + "over").getBytes(), Base64.NO_WRAP);
			httpput.setEntity(new StringEntity(data.toString()));
			httpput.setHeader("Content-Type", "application/json");
			// httpput.setHeader("Authorization", authorizationString);
			HttpResponse response = httpclient.execute(httpput);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			jsonResponse = new JSONObject(fullResult);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			success = getStringFromJSON(jsonResponse, "success");
			error = getStringFromJSON(jsonResponse, "error");
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

	private boolean getBooleanFromJSON(JSONObject json, String key) {
		try {
			return json.getBoolean(key);
		} catch (JSONException e) {
			return false;
		}
	}
}
