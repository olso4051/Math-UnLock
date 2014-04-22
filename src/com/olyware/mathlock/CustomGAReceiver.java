package com.olyware.mathlock;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.analytics.tracking.android.CampaignTrackingReceiver;
import com.olyware.mathlock.utils.EncryptionHelper;

/*
*  A simple Broadcast Receiver to receive an INSTALL_REFERRER
*  intent and pass it to other receivers, including
*  the Google Analytics receiver.
*/
public class CustomGAReceiver extends BroadcastReceiver {
	final private static String[] EXPECTED_PARAMETERS = { "utm_source", "utm_medium", "utm_term", "utm_content", "utm_campaign" };

	@Override
	public void onReceive(Context context, Intent intent) {
		// Workaround for Android security issue: http://code.google.com/p/android/issues/detail?id=16006
		try {
			final Bundle extras = intent.getExtras();
			if (extras != null) {
				extras.containsKey(null);
				Log.d("GAtest", extras.toString());
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

		Log.d("GAtest", "referrer = " + referrer);
		try {    // Remove any url encoding
			referrer = URLDecoder.decode(referrer, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			return;
		}

		// Parse the query string, extracting the relevant data
		Log.d("GAtest", "referrer = " + referrer);
		String[] params = referrer.split("&"); // $NON-NLS-1$
		for (String param : params) {
			Log.d("GAtest", "param = " + param);
			String[] pair = param.split("="); // $NON-NLS-1$
			referralParams.put(pair[0], pair[1]);
		}

		storeReferralParams(context, referralParams);

		// pass along intent to GA
		new CampaignTrackingReceiver().onReceive(context, intent);
	}

	public static void storeReferralParams(Context context, Map<String, String> params) {
		SharedPreferences storage = context.getSharedPreferences("ga_prefs", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = storage.edit();

		for (String key : EXPECTED_PARAMETERS) {
			String value = params.get(key);
			if (value != null) {
				if (key.equals("utm_content")) {
					value = new EncryptionHelper().decryptForURL(value);
				}
				editor.putString(key, value);
				Log.d("GAtest", "key = " + value);
			}
		}

		editor.commit();
	}
}