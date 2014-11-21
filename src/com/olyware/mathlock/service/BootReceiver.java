package com.olyware.mathlock.service;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.EZ;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

			if ((getEnabledPackages(context, sharedPrefs) > 0) && (sharedPrefs.getBoolean("lockscreen", true))) {
				// start background service to wait for screen to turn off
				Intent sIntent = new Intent(context, ScreenService.class);
				context.startService(sIntent);
			}
		}
	}

	public int getEnabledPackages(Context context, SharedPreferences sharedPrefs) {
		List<String> PackageKeys = EZ.list(context.getResources().getStringArray(R.array.enable_package_keys));
		int count = 0;
		for (int i = 0; i < PackageKeys.size(); i++) {
			if (sharedPrefs.getBoolean(PackageKeys.get(i), false))
				count++;
		}
		return count;
	}
}