package com.olyware.mathlock.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.util.Log;

import com.olyware.mathlock.R;

public class UploadImage extends AsyncTask<String, Integer, Integer> {
	private String baseURL;
	private String success, hash, url, error;
	private Bitmap image;

	public UploadImage(Context ctx, Bitmap b) {
		baseURL = ctx.getString(R.string.service_base_url);
		image = b;
	}

	public String getSuccess() {
		if (success != null)
			return success;
		else
			return "";
	}

	public String getHash() {
		if (hash != null)
			return hash;
		else
			return "";
	}

	public String getURL() {
		if (url != null)
			return url;
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
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(baseURL + "share/fb");
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			image.compress(CompressFormat.PNG, 100, bos);
			byte[] data = bos.toByteArray();
			InputStreamBody inputStreamBody = new InputStreamBody(new ByteArrayInputStream(data), "abc.png");

			MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
			multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			// multipartEntity.addPart("image", inputStreamBody);
			multipartEntity.addPart("image", new ByteArrayBody(data, "test.png"));
			// multipartEntity.addBinaryBody("image", data);
			multipartEntity.addTextBody("user_id", s[0]);
			multipartEntity.addTextBody("title", s[1]);
			multipartEntity.addTextBody("url", s[2]);
			multipartEntity.addTextBody("extension", "png");
			multipartEntity.addTextBody("question_id", "blah");
			multipartEntity.addTextBody("description", "blah");
			Log.d("test", "multipartEntity = " + multipartEntity.toString());
			Log.d("test", "HttpEntity = " + multipartEntity.build().toString());

			postRequest.setEntity(multipartEntity.build());
			Log.d("test", "postRequest = " + postRequest.toString());
			HttpResponse response = httpClient.execute(postRequest);
			Log.d("test", "response = " + response.toString());
			entity = response.getEntity();
			Log.d("test", "entity = " + entity.toString());
			fullResult = EntityUtils.toString(entity);
			Log.d("test", "fullResult = " + fullResult.toString());
			jsonResponse = new JSONObject(fullResult);
			Log.d("test", "jsonResponse = " + jsonResponse.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			success = getStringFromJSON(jsonResponse, "success");
			hash = getStringFromJSON(jsonResponse, "hash");
			url = getStringFromJSON(jsonResponse, "url");
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
