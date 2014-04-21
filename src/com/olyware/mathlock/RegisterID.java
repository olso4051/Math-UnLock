package com.olyware.mathlock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Base64;

public class RegisterID extends AsyncTask<String, Integer, Integer> {
	private String baseURL;
	RegisterIdResponse mCallback;

	public interface RegisterIdResponse {
		void registrationResult(int result);
	}

	public RegisterID(Activity act) {
		baseURL = act.getString(R.string.service_base_url);
		try {
			mCallback = (RegisterIdResponse) act;
		} catch (ClassCastException e) {
			throw new ClassCastException(act.toString() + " must implement RegisterIdResponse");
		}
	}

	@Override
	protected Integer doInBackground(String... s) {
		// POST to API with old and new registration, also referral's registration
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		HttpPost httppost = new HttpPost(baseURL + "GCMregistration");
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			if (s[1].equals(""))
				data.put("status", "new");
			else
				data.put("status", "update");
			data.put("newID", s[0]);
			data.put("oldID", s[1]);
			data.put("referral", s[2]);
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
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		mCallback.registrationResult(result);
	}
}
