package com.olyware.mathlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			// start background service to wait for screen to turn off
			Intent sIntent = new Intent(context, ScreenService.class);
			context.startService(sIntent);
		}
	}
}