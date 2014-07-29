package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.olyware.mathlock.MyApplication;
import com.olyware.mathlock.utils.Loggy;

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
			Loggy.d("onStartCommand intent = " + intent.toString());
			String pack = intent.getExtras().getString("package");
			Loggy.d("onStartCommand pack = " + pack);
			if (!packagesToWatch.contains(new PackageData(pack, 0))) {
				Loggy.d("add package to watch " + pack);
				packagesToWatch.add(new PackageData(pack, System.currentTimeMillis()));
				if (!packagesToWatch.contains(new PackageData(pack, 0))) {
					Loggy.d("SHOULD NOT HAPPEN");
				} else {
					Loggy.d("GOOD WORK");
				}
			}
			startService();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		Loggy.d("chirpadsservice onCreate");
		trackerGA = MyApplication.getGaTracker();
		trackerGA.set(Fields.SCREEN_NAME, "ChirpAds Service");
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		for (PackageData data : packagesToWatch) {
			String pkg = data.getPack();
			long time = data.getTimeToOpen();
			Loggy.d("send event open event");
			trackerGA.send(MapBuilder.createEvent("chirpAds", "app_opened", pkg, time).build());
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
			Loggy.d("mainTask running");
			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo process : processes) {
				String[] pkglist = process.pkgList;
				for (String pkg : pkglist) {
					if (packagesToWatch.contains(new PackageData(pkg, 0))) {
						// TODO app opened
						Loggy.d("opened " + pkg);
						int i = packagesToWatch.indexOf(new PackageData(pkg, 0));
						Loggy.d("remove index = " + i);
						if (i >= 0 && i < packagesToWatch.size()) {
							long time = packagesToWatch.get(i).getTimeToOpen();
							Loggy.d("send event open event");
							trackerGA.send(MapBuilder.createEvent("chirpAds", "app_opened", pkg, time).build());
							packagesToWatch.remove(i);
							if (packagesToWatch.size() == 0) {
								timer.cancel();
								timer = null;
								stopSelf();
							}
						} else {
							Loggy.d("SHOULD NOT HAPPEN");
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
