package com.olyware.mathlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenReceiver extends BroadcastReceiver {
	public static boolean wasScreenOn = true;

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		boolean screenOff = action.equals(Intent.ACTION_SCREEN_OFF);
		boolean screenOn = action.equals(Intent.ACTION_SCREEN_ON);

		if (screenOn) {
			wasScreenOn = true;
		} else if (screenOff) {
			Intent i = new Intent(context, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			context.startActivity(i);
			wasScreenOn = false;
		}
	}
}
