package com.olyware.mathlock.service;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import ch.boye.httpclientandroidlib.entity.mime.content.ByteArrayBody;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.olyware.mathlock.utils.ShareHelper;

public class UploadImage extends AsyncTask<String, Integer, Integer> {
	private final static String ENDPOINT = "share/fb";
	private String success, hash, url, error;
	private Bitmap image;

	public UploadImage(Context ctx, Bitmap b) {
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
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost postRequest = new HttpPost(ShareHelper.DEELDAT_BASE_URL + ENDPOINT);
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			image.compress(CompressFormat.JPEG, 75, bos);
			byte[] data = bos.toByteArray();

			MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
			multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			multipartEntity.addPart("image", new ByteArrayBody(data, "test.jpeg"));

			if (s[0] != null)
				if (s[0].length() > 0)
					multipartEntity.addTextBody("user_id", s[0]);
			if (s[1] != null)
				if (s[1].length() > 0)
					multipartEntity.addTextBody("title", s[1]);
			if (s[2] != null)
				if (s[2].length() > 0)
					multipartEntity.addTextBody("description", s[2]);
			if (s[3] != null)
				if (s[3].length() > 0)
					multipartEntity.addTextBody("site_name", s[3]);
			if (s[4] != null)
				if (s[4].length() > 0)
					multipartEntity.addTextBody("url", s[4]);
			if (s[5] != null)
				if (s[5].length() > 0)
					multipartEntity.addTextBody("app_name", s[5]);
			if (s[6] != null)
				if (s[6].length() > 0)
					multipartEntity.addTextBody("package", s[6]);
			if (s[7] != null)
				if (s[7].length() > 0)
					multipartEntity.addTextBody("app_class", s[7]);
			if (s[8] != null)
				if (s[8].length() > 0)
					multipartEntity.addTextBody("deeplink", s[8]);
			multipartEntity.addTextBody("extension", "jpeg");

			postRequest.setEntity(multipartEntity.build());
			HttpResponse response = httpClient.execute(postRequest);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			jsonResponse = new JSONObject(fullResult);
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
