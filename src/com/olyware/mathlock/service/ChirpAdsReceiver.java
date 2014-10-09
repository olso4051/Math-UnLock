package com.olyware.mathlock.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.olyware.mathlock.MyApplication;

public class ChirpAdsReceiver extends BroadcastReceiver {

	final public static String ACTION_CHALLENGE_ACCEPTED = "challenge_accepted";
	final public static String ACTION_CHALLENGE_DENIED = "challenge_denied";
	final public static String CHALLENGE_ID = "challenge_id";

	@Override
	public void onReceive(final Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		boolean updating = extras.getBoolean(Intent.EXTRA_REPLACING, false);
		String pack = intent.getDataString().replace("package:", "");
		if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
			if (!updating) {
				Intent sIntent = new Intent(context, ChirpAdsService.class);
				sIntent.putExtra("package", pack);
				context.startService(sIntent);
			} else {
				Tracker trackerGA;
				trackerGA = MyApplication.getGaTracker();
				trackerGA.set(Fields.SCREEN_NAME, "FlashAds Service");
				trackerGA.send(MapBuilder.createEvent("install", "updated", pack, 0l).build());
			}
		}
	}

}