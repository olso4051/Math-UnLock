package com.olyware.mathlock.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;

import com.olyware.mathlock.R;
import com.olyware.mathlock.service.AutoClick.AutoClickResult;
import com.olyware.mathlock.utils.Loggy;

public class AutoClick extends AsyncTask<Void, Integer, AutoClickResult> {

	final public static String REFERRER_QUERY = "referrer";
	final public static String PLAY_ID = "id";
	final private static String INSTALL_REFERRER = "com.android.vending.INSTALL_REFERRER";
	private String pack, referrer;
	private boolean fakeReferrer;
	private Context ctx;

	public static enum AutoClickResult {
		failedClick, failedReferrer, success;
	}

	public AutoClick(Context ctx, String pack, boolean fakeReferrer) {
		this.pack = pack;
		this.fakeReferrer = fakeReferrer;
		this.ctx = ctx;
	}

	public String getReferrer() {
		if (referrer != null)
			return referrer;
		else
			return "";
	}

	@Override
	protected AutoClickResult doInBackground(Void... v) {
		try {
			List<String> packageNames = new ArrayList<String>(Arrays.asList(ctx.getResources().getStringArray(R.array.package_names)));
			List<String> packageLinks = new ArrayList<String>(Arrays.asList(ctx.getResources().getStringArray(R.array.package_name_links)));
			int packLoc = packageNames.indexOf(pack);
			if (packLoc == -1) {
				return AutoClickResult.failedClick;
			}

			URL url = new URL(packageLinks.get(packLoc));
			HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
			ucon.setInstanceFollowRedirects(false);
			String redirected = ucon.getHeaderField("Location");
			if (redirected == null)
				return AutoClickResult.failedClick;
			referrer = Uri.parse(redirected).getQueryParameter(REFERRER_QUERY);
			if (referrer == null)
				referrer = "";
			int count = 0;
			while (!isUrlPlayStore(redirected)) {
				URL urlNext = new URL(redirected);
				HttpURLConnection uconNext = (HttpURLConnection) urlNext.openConnection();
				uconNext.setInstanceFollowRedirects(false);
				redirected = uconNext.getHeaderField("Location");
				if (redirected == null)
					return AutoClickResult.failedClick;
				referrer = Uri.parse(redirected).getQueryParameter(REFERRER_QUERY);
				if (referrer == null)
					referrer = "";
				if (count > 1)
					break;
				count++;
			}

			if (fakeReferrer) {
				Intent i = new Intent(INSTALL_REFERRER);
				i.setPackage(pack);
				PackageManager pm = ctx.getPackageManager();
				List<ResolveInfo> receivers = pm.queryBroadcastReceivers(i, 0);
				if (receivers != null && receivers.size() > 0) {
					i.setClassName(receivers.get(0).activityInfo.packageName, receivers.get(0).activityInfo.name);
					i.putExtra("referrer", referrer);
					ctx.sendBroadcast(i);
				} else {
					return AutoClickResult.failedReferrer;
				}
			}
			return AutoClickResult.success;
		} catch (MalformedURLException e) {
			return AutoClickResult.failedClick;
		} catch (IOException e) {
			return AutoClickResult.failedClick;
		}
	}

	@Override
	protected void onPostExecute(AutoClickResult result) {
		// override in calling class
		// result == 0 success
	}

	public static boolean isUrlPlayStore(String url) {
		if (url == null)
			return false;
		String host = Uri.parse(url).getHost();
		if (host == null)
			host = "";
		String scheme = Uri.parse(url).getScheme();
		if (scheme == null)
			scheme = "";
		Loggy.d("url = " + url + " |scheme = " + scheme + " |host = " + host);
		if ((scheme.equals("https") && host.equals("play.google.com")) || (scheme.equals("market") && host.equals("details")))
			return true;
		else
			return false;
	}

	public static boolean hasLinkForPack(Context ctx, String pack) {
		if (pack == null)
			return false;
		List<String> packageNames = new ArrayList<String>(Arrays.asList(ctx.getResources().getStringArray(R.array.package_names)));
		if (packageNames.contains(pack))
			return true;
		else
			return false;
	}
}
