package com.olyware.mathlock;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {
	public static boolean wasScreenOn = true;
	public static boolean PhoneOn = false;
	private long timeLast = 0;
	private Timer offTimer;
	private TimerTask offTimerTask;

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		final Context ctx = context;
		boolean screenOff = action.equals(Intent.ACTION_SCREEN_OFF);
		boolean screenOn = action.equals(Intent.ACTION_SCREEN_ON);
		boolean phoneStateChange = action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		int timeoutLoc = Integer.parseInt(sharedPrefs.getString("lockscreen2", "0"));
		long timeoutPeriod = Long.parseLong(ctx.getResources().getStringArray(R.array.lockscreen2_times)[timeoutLoc]);
		long currentTime = System.currentTimeMillis();

		if (timeLast == 0) {
			timeLast = System.currentTimeMillis();
			Log.d("test", "timeLast=0 so set timeLast = " + timeLast);
		}
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
			if (offTimer != null) {
				offTimer.cancel();
				offTimer = null;
				Log.d("test", "timer canceled");
			}
			if (timeLast + timeoutPeriod < currentTime) {
				timeLast = currentTime;
				Log.d("test", "timeLast reset " + timeLast);
			}
			wasScreenOn = true;
		} else if (screenOff) {
			if (!PhoneOn) {
				if (timeLast + timeoutPeriod < currentTime) {
					startMainActivity(ctx);
				} else {
					offTimer = new Timer();
					offTimerTask = new TimerTask() {
						@Override
						public void run() {
							startMainActivity(ctx);
						}
					};
					offTimer.schedule(offTimerTask, timeLast + timeoutPeriod - currentTime);
					Log.d("test", "started timer " + ((timeLast + timeoutPeriod - currentTime) / 60000d) + " minutes");
				}
			}
			wasScreenOn = false;
		}
	}

	private void startMainActivity(Context ctx) {
		timeLast = System.currentTimeMillis();
		Intent i = new Intent(ctx, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		i.putExtra("locked", true);
		// i.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		ctx.startActivity(i);
		Log.d("test", "started app " + timeLast);
	}
}
