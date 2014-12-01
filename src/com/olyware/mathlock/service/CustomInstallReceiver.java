package com.olyware.mathlock.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.CampaignTrackingReceiver;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.olyware.mathlock.MyApplication;
import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.EncryptionHelper;
import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.MoneyHelper;
import com.olyware.mathlock.utils.PreferenceHelper;
import com.olyware.mathlock.utils.ShareHelper;

/*
*  A simple Broadcast Receiver to receive an INSTALL_REFERRER
*  intent and pass it to other receivers, including
*  the Google Analytics receiver.
*/
public class CustomInstallReceiver extends BroadcastReceiver {
	final public static String PREFS_GA = "ga_prefs";
	final private static String UTM_SOURCE = "utm_source";
	final private static String UTM_MEDIUM = "utm_medium";
	final private static String UTM_CONTENT = "utm_content";
	final private static String UTM_TERM = "utm_term";
	final private static String UTM_CAMPAIGN = "utm_campaign";
	final private static String SHARE_ID = "deeldat_share_id";
	final private static String PROMO_COIN_ID = "promo_coin";
	final public static String SWISHER_KEY = "kara_swisher";
	final public static String ENTRE_KEY = "entrepreneur";

	// final private static String[] EXPECTED_PARAMETERS = { "utm_source", "utm_medium", "utm_content", "utm_term", "utm_campaign" };

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (sharedPrefs.getBoolean("first_open_install", true)) {
			Loggy.d("first open_install");
			Tracker trackerGA = MyApplication.getGaTracker();
			// trackerGA.send(MapBuilder.createEvent("acquisition", "install", "done", 0l).build());
			trackerGA.send(new HitBuilders.EventBuilder().setCategory("acquisition").setAction("install").setLabel("done").setValue(0l)
					.build());

			sharedPrefs.edit().putBoolean("first_open_install", false).commit();
		}

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
		Loggy.d("intent action = " + intent.getAction());
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
			if (pair != null)
				if (pair.length >= 2)
					referralParams.put(pair[0], pair[1]);
		}

		storeReferralParams(context, referralParams);

		// pass along intent to GA
		new CampaignTrackingReceiver().onReceive(context, intent);
	}

	public static void storeReferralParams(Context context, Map<String, String> params) {
		SharedPreferences sharedPrefsGA = context.getSharedPreferences(PREFS_GA, Context.MODE_PRIVATE);
		SharedPreferences.Editor editorPrefsGA = sharedPrefsGA.edit();
		SharedPreferences sharedPrefsUserInfo = context.getSharedPreferences(context.getString(R.string.pref_user_info),
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editorPrefsUserInfo = sharedPrefsUserInfo.edit();

		for (Map.Entry<String, String> entry : params.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (value != null) {
				editorPrefsGA.putString(key, value);
				Loggy.d("GAtest", key + " = " + value);
			}
		}
		editorPrefsGA.commit();
		String source = sharedPrefsGA.getString(UTM_SOURCE, "");
		String medium = sharedPrefsGA.getString(UTM_MEDIUM, "");
		String content = sharedPrefsGA.getString(UTM_CONTENT, "");
		String shareID = sharedPrefsGA.getString(SHARE_ID, "");
		String coinHash = sharedPrefsGA.getString(PROMO_COIN_ID, "");
		Loggy.d("source(" + source + ")medium(" + medium + ")content(" + content + ")shareID(" + shareID + ")");
		// is this a referral link from share or invite
		if (source.equals("app") && (medium.equals("share") || medium.equals("invite"))) {
			String referral = EncryptionHelper.decryptForURL(content);
			MoneyHelper.increasePaidMoney(context, context.getResources().getInteger(R.integer.coins_from_share));
			editorPrefsUserInfo.putString(context.getString(R.string.pref_user_referrer), referral).commit();
			Loggy.d("GAtest", "referral key = " + referral);
		}
		// else is this a chirpads link
		else if (sharedPrefsGA.getString(UTM_SOURCE, "").equals("chirpads")) {
			String clickGuid = content;
			String action = "install";
			String os = "Android";
			String osVersion = Build.VERSION.RELEASE;
			String appPackageName = "com.olyware.mathlock";
			String externalAdId = "com.olyware.mathlock";
			Loggy.d("test", "clickGuid = " + clickGuid);
			new PostChirpAds(context).execute(action, clickGuid, os, osVersion, appPackageName, externalAdId);
		}
		if (!shareID.equals("")) {
			new PostDeelDatInstall(context, ShareHelper.DEELDAT_APP_ID, shareID).execute();
		}
		if (!coinHash.equals("")) {
			if (coinHash.toLowerCase(Locale.ENGLISH).equals(SWISHER_KEY)) {
				PreferenceHelper.setCustomFileName(context, PreferenceHelper.SWISHER_FILENAME);
				PreferenceHelper.turnSwisherPackOn(context, PreferenceHelper.SWISHER_FILENAME_WITH_EXTENSION);
			} else if (coinHash.toLowerCase(Locale.ENGLISH).equals(CustomInstallReceiver.ENTRE_KEY)) {
				PreferenceHelper.setCustomFileName(context, PreferenceHelper.ENTRE_FILENAME);
				PreferenceHelper.turnSwisherPackOn(context, PreferenceHelper.ENTRE_FILENAME_WITH_EXTENSION);
			} else {
				MoneyHelper.addPromoCoins(context, coinHash);
			}
		}
	}
}