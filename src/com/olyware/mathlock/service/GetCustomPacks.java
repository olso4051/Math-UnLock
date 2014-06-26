package com.olyware.mathlock.service;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.olyware.mathlock.R;

public class GetCustomPacks extends AsyncTask<String, Integer, Integer> {
	private ArrayList<CustomPackData> customPackDataList = new ArrayList<CustomPackData>();
	private String baseURL;

	public GetCustomPacks(Context ctx) {
		baseURL = ctx.getString(R.string.service_base_url);
	}

	public ArrayList<CustomPackData> getCustomPackList() {
		return customPackDataList;
	}

	@Override
	protected Integer doInBackground(String... s) {
		// PUT to API with user_id
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		HttpGet httpget = new HttpGet(baseURL + "pack");
		HttpEntity entity;
		String fullResult;
		JSONArray jsonResponse;
		try {
			// String authorizationString = "Basic " + Base64.encodeToString(("roll" + ":" + "over").getBytes(), Base64.NO_WRAP);
			// httpget.setHeader("Authorization", authorizationString);
			httpget.setHeader("Content-Type", "application/json");
			HttpResponse response = httpclient.execute(httpget);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			jsonResponse = new JSONArray(fullResult);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			customPackDataList.addAll(getCustomPackDataListFromJSON(jsonResponse));
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

	private ArrayList<CustomPackData> getCustomPackDataListFromJSON(JSONArray json) {
		ArrayList<CustomPackData> list = new ArrayList<CustomPackData>();
		list.clear();

		if (json.length() > 0) {
			try {
				for (int i = 0; i < json.length(); i++) {
					JSONObject obj = (JSONObject) json.get(i);
					list.add(new CustomPackData(getStringFromJSON(obj, "filename"), getStringFromJSON(obj, "name"), getStringFromJSON(obj,
							"id"), getStringFromJSON(obj, "user_id"), getStringFromJSON(obj, "downloads")));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
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
