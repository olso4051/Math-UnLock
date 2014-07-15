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
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;

import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.ContactHelper;
import com.olyware.mathlock.utils.Loggy;

public class RegisterID extends AsyncTask<Void, Integer, Integer> {
	private String baseURL;
	private String success, error;
	private String userName, regID, userID, referral, birthday, gender, location, email, facebookID, phoneNumberEncrypted;

	public RegisterID(Activity act, String userName, String regID, String userID, String referral, String birthday, String gender,
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

		TelephonyManager telephonyManager = (TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
		String number = telephonyManager.getLine1Number();
		if (number != null && number.length() > 0) {
			phoneNumberEncrypted = ContactHelper.getPhoneHashFromString(number);
		} else {
			phoneNumberEncrypted = "";
		}
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
	protected Integer doInBackground(Void... v) {
		String endpoint = "register";
		if (userID.length() > 0) {
			endpoint = endpoint + "/update";
			referral = "";
		}
		if (regID.length() <= 0) {
			return 1;
		}

		// POST to API new or update registration, also referral's registration
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);

		HttpPut httpput = new HttpPut(baseURL + endpoint);
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
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

			Loggy.d("test", "JSON to register: " + data.toString());
			httpput.setEntity(new StringEntity(data.toString()));
			httpput.setHeader("Content-Type", "application/json");
			HttpResponse response = httpclient.execute(httpput);
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
			success = getStringFromJSON(jsonResponse, "success");
			error = getStringFromJSON(jsonResponse, "error");
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
