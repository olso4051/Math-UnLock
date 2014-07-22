package com.olyware.mathlock.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.google.analytics.tracking.android.CampaignTrackingReceiver;
import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.EncryptionHelper;
import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.MoneyHelper;

/*
*  A simple Broadcast Receiver to receive an INSTALL_REFERRER
*  intent and pass it to other receivers, including
*  the Google Analytics receiver.
*/
public class CustomGAReceiver extends BroadcastReceiver {
	final public static String PREFS_GA = "ga_prefs";
	final private static String[] EXPECTED_PARAMETERS = { "utm_source", "utm_medium", "utm_content", "utm_term", "utm_campaign" };

	@Override
	public void onReceive(Context context, Intent intent) {
		// Workaround for Android security issue: http://code.google.com/p/android/issues/detail?id=16006
		try {
			final Bundle extras = intent.getExtras();
			if (extras != null) {
				extras.containsKey(null);
				Loggy.d("GAtest", extras.toString());
			}
		} catch (final Exception e) {
			return;
		}

		Map<String, String> referralParams = new HashMap<String, String>();

		// Return if this is not the right intent.
		if (!intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) { //$NON-NLS-1$
			return;
		}

		String referrer = intent.getStringExtra("referrer"); //$NON-NLS-1$
		if (referrer == null || referrer.length() == 0) {
			return;
		}

		Loggy.d("GAtest", "referrer = " + referrer);
		try {    // Remove any url encoding
			referrer = URLDecoder.decode(referrer, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			return;
		}

		// Parse the query string, extracting the relevant data
		Loggy.d("GAtest", "referrer = " + referrer);
		String[] params = referrer.split("\\&"); // $NON-NLS-1$
		for (String param : params) {
			Loggy.d("GAtest", "param = " + param);
			String[] pair = param.split("="); // $NON-NLS-1$
			referralParams.put(pair[0], pair[1]);
		}

		storeReferralParams(context, referralParams);

		// pass along intent to GA
		new CampaignTrackingReceiver().onReceive(context, intent);
	}

	public static void storeReferralParams(Context context, Map<String, String> params) {
		SharedPreferences storage = context.getSharedPreferences(PREFS_GA, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = storage.edit();
		SharedPreferences sharedPrefsUserInfo = context.getSharedPreferences(context.getString(R.string.pref_user_info),
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editorUserInfo = sharedPrefsUserInfo.edit();

		for (String key : EXPECTED_PARAMETERS) {
			String value = params.get(key);
			if (value != null) {
				editor.putString(key, value);
				Loggy.d("GAtest", "key = " + value);
			}
		}
		editor.commit();

		// is this a referral link
		if (storage.getString("utm_source", "").equals("app") && storage.getString("utm_medium", "").equals("share")) {
			String referral = EncryptionHelper.decryptForURL(params.get("utm_content"));
			MoneyHelper.increasePaidMoney(context, context.getResources().getInteger(R.integer.coins_from_share));
			editorUserInfo.putString(context.getString(R.string.pref_user_referrer), referral).commit();
			Loggy.d("GAtest", "referral key = " + referral);
		}
		// else is this a chirpads link
		else if (storage.getString("utm_source", "").equals("chirpads")) {
			String clickGuid = params.get("utm_content");
			String action = "install";
			String os = "Android";
			String osVersion = Build.VERSION.RELEASE;
			String appPackageName = "com.olyware.mathlock";
			String externalAdId = "com.olyware.mathlock";
			Loggy.d("test", "clickGuid = " + clickGuid);
			new PostChirpAds(context).execute(action, clickGuid, os, osVersion, appPackageName, externalAdId);
		}

	}
}