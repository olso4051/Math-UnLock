package com.olyware.mathlock.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;

import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.R;

public class ScreenReceiver extends BroadcastReceiver {
	public static boolean wasScreenOn = true;
	public static boolean PhoneOn = false;
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
		final boolean lockscreen = sharedPrefs.getBoolean("lockscreen", true);
		int timeoutLoc = Integer.parseInt(sharedPrefs.getString("lockscreen2", "0"));
		final long timeLast = sharedPrefs.getLong("timeout", 0);
		long timeoutPeriod = 0;
		long currentTime = System.currentTimeMillis();
		if (timeoutLoc != 0) {
			timeoutPeriod = Long.parseLong(ctx.getResources().getStringArray(R.array.lockscreen2_times)[timeoutLoc]);
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
			} else {
				Intent broadcastIntent = new Intent(ctx.getString(R.string.screen_on_receiver_filter));
				LocalBroadcastManager.getInstance(ctx).sendBroadcast(broadcastIntent);
			}
			wasScreenOn = true;
		} else if (screenOff) {
			if (!PhoneOn) {
				if (timeLast + timeoutPeriod < currentTime) {
					startMainActivity(ctx, lockscreen);
				} else {
					offTimer = new Timer();
					offTimerTask = new TimerTask() {
						@Override
						public void run() {
							startMainActivity(ctx, lockscreen);
							offTimer = null;
						}
					};
					offTimer.schedule(offTimerTask, timeLast + timeoutPeriod - currentTime);
				}
			}
			wasScreenOn = false;
		}
	}

	private void startMainActivity(Context ctx, boolean lockscreen) {
		boolean navigation = false;
		ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> procInfos = activityManager.getRunningServices(Integer.MAX_VALUE);
		for (int idx = 0; idx < procInfos.size(); idx++) {
			if (procInfos.get(idx).service.getClassName().equals("com.google.android.apps.gmm.navigation.base.NavigationService"))
				navigation = true;
		}
		if (lockscreen && !navigation) {
			Intent i = new Intent(ctx, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.putExtra("locked", true);
			ctx.startActivity(i);
		}
	}
}
