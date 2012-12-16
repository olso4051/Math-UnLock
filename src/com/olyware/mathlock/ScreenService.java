package com.olyware.mathlock;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class ScreenService extends Service {

	private BroadcastReceiver sReceiver = new ScreenReceiver();

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		IntentFilter s_intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		s_intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		s_intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		this.registerReceiver(sReceiver, s_intentFilter);
		Toast.makeText(this, "started service", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "ended service", Toast.LENGTH_SHORT).show();
		// Do not forget to unregister the receiver!!!
		if (sReceiver != null) {
			this.unregisterReceiver(sReceiver);
		}
	}
}