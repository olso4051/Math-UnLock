package com.olyware.mathlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ScreenReceiver extends BroadcastReceiver {
	public static boolean wasScreenOn = true;

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		boolean screenOff = action.equals(Intent.ACTION_SCREEN_OFF);
		boolean screenOn = action.equals(Intent.ACTION_SCREEN_ON);

		if (screenOn) {
			// Intent i = new Intent(context, MainActivity.class);
			// i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// context.startActivity(i);
			Toast.makeText(context, "I'm turing on Bitch", Toast.LENGTH_SHORT).show();
			wasScreenOn = true;
		} else if (screenOff) {
			wasScreenOn = false;
		}
	}
}
