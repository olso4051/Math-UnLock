package com.olyware.mathlock.service;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPut;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.olyware.mathlock.R;
import com.olyware.mathlock.service.AdvertisingIdClient.AdInfo;
import com.olyware.mathlock.utils.ContactHelper;
import com.olyware.mathlock.utils.JSONHelper;
import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.PreferenceHelper;

public class RegisterID extends AsyncTask<Void, Integer, Integer> {
	private String baseURL;
	private String success, error;
	private String userName, regID, userID, referral, birthday, gender, location, email, facebookID, phoneNumberEncrypted, ad_id;
	private Context ctx;

	public RegisterID(Context ctx, String userName, String regID, String userID, String referral, String birthday, String gender,
			String location, String email, String facebookID) {
		if (userName != null)
			this.userName = userName;
		else
			this.userName = "";

		if (regID != null)
			this.regID = regID;
		else
			this.regID = "";

		if (userID != null)
			this.userID = userID;
		else
			this.userID = "";

		if (referral != null)
			this.referral = referral;
		else
			this.referral = "";

		if (birthday != null)
			this.birthday = birthday;
		else
			this.birthday = "";

		if (gender != null)
			this.gender = gender;
		else
			this.gender = "";

		if (location != null)
			this.location = location;
		else
			this.location = "";

		if (email != null)
			this.email = email;
		else
			this.email = "";

		if (facebookID != null)
			this.facebookID = facebookID;
		else
			this.facebookID = "";

		String number = ContactHelper.getUserPhoneNumber(ctx);
		if (number != null && number.length() > 0) {
			phoneNumberEncrypted = ContactHelper.storeUserPhoneNumber(ctx, number);
		} else {
			phoneNumberEncrypted = "";
		}
		baseURL = ctx.getString(R.string.service_base_url);
		this.ctx = ctx;
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

		final Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			// Get the user's list of friends
			try {
				Request meRequest = new Request(session, "/me");
				Response response = meRequest.executeAndWait();
				String rawResponse = "";
				if (response != null)
					rawResponse = response.getRawResponse();
				if (rawResponse == null)
					rawResponse = "";
				JSONObject json = new JSONObject(rawResponse);
				userName = JSONHelper.getStringFromJSON(json, "name");
				facebookID = JSONHelper.getStringFromJSON(json, "id");
				gender = JSONHelper.getStringFromJSON(json, "gender");
				email = JSONHelper.getStringFromJSON(json, "email");
				PreferenceHelper.storeFacebookMe(ctx, facebookID, userName, gender, email);
			} catch (JSONException e) {
				// Do Nothing
			} catch (FacebookException e) {
				// Do Nothing
			}
		}

		String endpoint = "register";
		if (userID.length() > 0) {
			endpoint = endpoint + "/update";
			referral = "";
		}
		if (regID.length() <= 0) {
			return 1;
		}

		AdInfo adInfo;
		try {
			adInfo = AdvertisingIdClient.getAdvertisingIdInfo(ctx);
			ad_id = adInfo.getId();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Log.d("TAG", "ad_id" + ad_id);
		// POST to API new or update registration, also referral's registration
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPut httpput = new HttpPut(baseURL + endpoint);
		HttpEntity entity = null;
		String fullResult = null;
		JSONObject jsonResponse = null;
		try {
			JSONObject data = new JSONObject();
			if (userName.length() > 0) {
				data.put("username", userName);
			}
			if (regID.length() > 0) {
				data.put("registration_id", regID);
			}
			if (userID.length() > 0) {
				data.put("user_id", userID);
			}
			if (referral.length() > 0) {
				data.put("referral", referral);
			}
			if (birthday.length() > 0) {
				data.put("birthday", birthday);
			}
			if (gender.length() > 0) {
				data.put("gender", gender);
			}
			if (location.length() > 0) {
				data.put("location", location);
			}
			if (email.length() > 0) {
				data.put("email", email);
			}
			if (facebookID.length() > 0) {
				data.put("facebook_hash", facebookID);
			}
			if (!phoneNumberEncrypted.equals("")) {
				data.put("phone_hash", phoneNumberEncrypted);
			}
			if (!TextUtils.isEmpty(ad_id)) {
				data.put("google_ad_id", ad_id);
			}

			Loggy.d("test", "JSON to " + endpoint + ": " + data.toString());
			httpput.setEntity(new StringEntity(data.toString(), ContentType.create("text/plain", "UTF-8")));
			httpput.setHeader("Content-Type", "application/json");
			HttpResponse response = httpClient.execute(httpput);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			Loggy.d("test", fullResult);
			jsonResponse = new JSONObject(fullResult);
		} catch (JSONException j) {
			j.printStackTrace();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			success = JSONHelper.getStringFromJSON(jsonResponse, "success");
			error = JSONHelper.getStringFromJSON(jsonResponse, "error");
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
}
