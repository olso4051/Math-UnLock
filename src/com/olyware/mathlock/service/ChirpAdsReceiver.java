package com.olyware.mathlock.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ChirpAdsReceiver extends BroadcastReceiver {

	final public static String ACTION_CHALLENGE_ACCEPTED = "challenge_accepted";
	final public static String ACTION_CHALLENGE_DENIED = "challenge_denied";
	final public static String CHALLENGE_ID = "challenge_id";

	@Override
	public void onReceive(final Context context, Intent intent) {
		if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
			String pack = intent.getDataString().replace("package:", "");
			Intent sIntent = new Intent(context, ChirpAdsService.class);
			sIntent.putExtra("package", pack);
			context.startService(sIntent);
		}
	}

}