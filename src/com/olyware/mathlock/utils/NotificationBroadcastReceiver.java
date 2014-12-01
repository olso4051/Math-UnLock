package com.olyware.mathlock.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.olyware.mathlock.MyApplication;
import com.olyware.mathlock.service.AutoClick;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

	final public static String ACTION_REMIND_INSTALLED = "com.olyware.mathlock.utils.NotificationBroadcastReceiver.ACTION_REMIND_INSTALLED";
	final public static String ACTION_REMIND_CLICKED = "clicked";
	final public static String ACTION_REMIND_PACKAGE = "package_name";

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundletest = intent.getExtras();
		boolean clickedtest = bundletest.getBoolean(ACTION_REMIND_CLICKED);
		final String packtest = bundletest.getString(ACTION_REMIND_PACKAGE);
		Loggy.d("pack = " + packtest + " clicked = " + clickedtest);
		if (ACTION_REMIND_INSTALLED.equals(intent.getAction())) {
			Tracker trackerGA = MyApplication.getGaTracker();
			final Context ctx = context;
			Bundle bundle = intent.getExtras();
			boolean clicked = bundle.getBoolean(ACTION_REMIND_CLICKED);
			final String pack = bundle.getString(ACTION_REMIND_PACKAGE);
			Loggy.d("pack = " + pack + " clicked = " + clicked);
			if (clicked) {
				// trackerGA.send(MapBuilder.createEvent("install", "notification_clicked", pack, 0l).build());
				trackerGA.send(new HitBuilders.EventBuilder().setCategory("install").setAction("notification_clicked").setLabel(pack)
						.setValue((long) 0l).build());

				new AutoClick(context, pack, false) {
					@Override
					protected void onPostExecute(AutoClickResult result) {
						PackageManager pm = ctx.getPackageManager();
						Intent launchIntent = pm.getLaunchIntentForPackage(pack);
						ctx.startActivity(launchIntent);
					}
				}.execute();
			} else {
				// trackerGA.send(MapBuilder.createEvent("install", "notification_cleared", pack, 0l).build());
				trackerGA.send(new HitBuilders.EventBuilder().setCategory("install").setAction("notification_cleared").setLabel(pack)
						.setValue((long) 0l).build());

			}
		}
	}
}
