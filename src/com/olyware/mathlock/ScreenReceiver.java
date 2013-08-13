package com.olyware.mathlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class ScreenReceiver extends BroadcastReceiver {
	public static boolean wasScreenOn = true;
	public static boolean PhoneOn = false;
	private long timeLast = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		final Context ctx = context;
		boolean screenOff = action.equals(Intent.ACTION_SCREEN_OFF);
		boolean screenOn = action.equals(Intent.ACTION_SCREEN_ON);
		boolean phoneStateChange = action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED);

		if (phoneStateChange) {
			boolean ringing = intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING);
			boolean offHook = intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK);
			boolean phoneIdle = intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE);

			if (ringing || offHook) {
				PhoneOn = true;
			} else if (phoneIdle) {
				PhoneOn = false;
			}
		}
		if (screenOn) {
			wasScreenOn = true;
		} else if (screenOff) {
			if (!PhoneOn) {
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
				int timeoutLoc = Integer.parseInt(sharedPrefs.getString("lockscreen2", "0"));
				long timeoutPeriod = Long.parseLong(ctx.getResources().getStringArray(R.array.lockscreen2_times)[timeoutLoc]);
				if (timeLast + timeoutPeriod < System.currentTimeMillis()) {
					timeLast = System.currentTimeMillis();
					Intent i = new Intent(context, MainActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					i.putExtra("locked", true);
					// i.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
					context.startActivity(i);
				}
			}
			wasScreenOn = false;
		}
	}
}
