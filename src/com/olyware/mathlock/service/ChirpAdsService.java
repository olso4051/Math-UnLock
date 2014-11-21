package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.olyware.mathlock.MyApplication;
import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.NotificationHelper;
import com.olyware.mathlock.utils.PreferenceHelper;

public class ChirpAdsService extends Service {

	final private static long CHECK_PERIOD = 1000;// milliseconds
	final private static long STOP_TIME = 600000;// milliseconds

	private static Timer timer = new Timer();
	private static Timer timerToStop = new Timer();
	private List<PackageData> packagesToWatch = new ArrayList<PackageData>();
	private Tracker trackerGA;

	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String pack = intent.getExtras().getString("package");
			if (!packagesToWatch.contains(new PackageData(pack, 0))) {
				PackageData pd = new PackageData(pack, System.currentTimeMillis());
				packagesToWatch.add(pd);
				PreferenceHelper.addPackToOpen(this, pd);
			}
			try {
				ApplicationInfo packInfo = this.getPackageManager().getApplicationInfo(pack, 0);
				NotificationHelper.sendNotification(getApplicationContext(), packInfo);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			// new AutoClick(this, pack, false).execute();

			startService();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		Loggy.d("chirpadsservice onCreate");
		trackerGA = MyApplication.getGaTracker();
		// trackerGA.set(Fields.SCREEN_NAME, "ChirpAds Service");
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		for (PackageData data : packagesToWatch) {
			String pkg = data.getPack();
			long time = data.getTimeToOpen();
			// trackerGA.send(MapBuilder.createEvent("chirpAds", "app_opened", pkg, time).build());
			trackerGA.send(new HitBuilders.EventBuilder().setCategory("chirpAds").setAction("app_opened").setLabel(pkg).setValue(time)
					.build());
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (timerToStop != null) {
			timerToStop.cancel();
			timerToStop = null;
		}
		super.onDestroy();
	}

	private void startService() {
		if (timer != null)
			timer.cancel();
		if (timerToStop != null)
			timerToStop.cancel();
		timer = new Timer();
		timerToStop = new Timer();
		timer.scheduleAtFixedRate(new mainTask(), 0, CHECK_PERIOD);
		timerToStop.schedule(new stopTask(), STOP_TIME);
	}

	private class mainTask extends TimerTask {
		public void run() {
			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo process : processes) {
				String[] pkglist = process.pkgList;
				for (String pkg : pkglist) {
					if (packagesToWatch.contains(new PackageData(pkg, 0))) {
						int i = packagesToWatch.indexOf(new PackageData(pkg, 0));
						if (i >= 0 && i < packagesToWatch.size()) {
							long time = packagesToWatch.get(i).getTimeToOpen();
							// trackerGA.send(MapBuilder.createEvent("chirpAds", "app_opened", pkg, time).build());
							trackerGA.send(new HitBuilders.EventBuilder().setCategory("chirpAds").setAction("app_opened").setLabel(pkg)
									.setValue(time).build());
							NotificationHelper.clearAppNotification(getApplicationContext());
							PreferenceHelper.removePackToOpen(getApplicationContext(), pkg);
							packagesToWatch.remove(i);
							if (packagesToWatch.size() == 0) {
								timer.cancel();
								timer = null;
								stopSelf();
							}
						}
					}
				}
			}
		}
	}

	private class stopTask extends TimerTask {
		public void run() {
			timer.cancel();
			timer = null;
			stopSelf();
		}
	}
}
