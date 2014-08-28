package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
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
import com.olyware.mathlock.utils.JSONHelper;

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
		HttpClient httpclient = HttpClientBuilder.create().build();
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
					String filename = JSONHelper.getStringFromJSON(obj, "filename");
					String name = JSONHelper.getStringFromJSON(obj, "name");
					String ID = JSONHelper.getStringFromJSON(obj, "id");
					String userID = JSONHelper.getStringFromJSON(obj, "user_id");
					int downloads = JSONHelper.getIntFromJSON(obj, "downloads");
					List<String> tags = JSONHelper.getStringListFromJSON(obj, "tag");
					list.add(new CustomPackData(filename, name, ID, userID, downloads, tags));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
