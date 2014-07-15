package com.olyware.mathlock.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.Loggy;

public class AcceptChallenge extends AsyncTask<Void, Integer, Integer> {
	private String baseURL;
	private String success, error, challengeID;
	private boolean accept;

	public AcceptChallenge(Context ctx, String challengeID, boolean accept) {
		this.challengeID = challengeID;
		this.accept = accept;
		baseURL = ctx.getString(R.string.service_base_url);
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
	protected Integer doInBackground(Void... v) {
		String endpoint = "challenge/" + (accept ? "accept" : "decline");

		// PUT to API challenge
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		DefaultHttpClient httpclient = new DefaultHttpClient(params);
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		httpclient.getParams().setParameter("http.protocol.content-charset", "UTF-8");

		HttpPut httpput = new HttpPut(baseURL + endpoint);
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			data.put("challenge_id", challengeID);

			Loggy.d("test", "JSON to " + endpoint + ": " + data.toString());
			httpput.setEntity(new StringEntity(data.toString(), "UTF-8"));
			httpput.setHeader("Content-Type", "application/json");
			HttpResponse response = httpclient.execute(httpput);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
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
