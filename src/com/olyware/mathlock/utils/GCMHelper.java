package com.olyware.mathlock.utils;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.MyApplication;
import com.olyware.mathlock.R;
import com.olyware.mathlock.RegisterID;

public class GCMHelper {

	final private static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static String regID;

	public static boolean registerAndStoreGCM(final Activity act, final Context app) {
		if (checkPlayServices(act)) {
			regID = getRegistrationId(app);
			SharedPreferences prefsGA = act.getSharedPreferences("ga_prefs", Context.MODE_PRIVATE);
			if (regID.equals("")) {
				SharedPreferences.Editor editorGA = prefsGA.edit();
				editorGA.putBoolean("reg_uploaded", false).commit();
				registerInBackground(act, app);
			} else if (!prefsGA.getBoolean("reg_uploaded", false)) {
				String referral = prefsGA.getString("utm_content", "");
				storeRegistrationId(act, app, regID);
				sendRegistrationIdToBackend(act, regID, referral);
			}
		} else {
			Toast.makeText(act, "No valid Google Play Services APK found.", Toast.LENGTH_LONG).show();
		}
		return true;
	}

	public static boolean checkPlayServices(Activity ctx) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, ctx, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				ctx.finish();
			}
			return false;
		}
		return true;
	}

	public static String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(context.getString(R.string.gcm_reg_id_property), "");
		if (registrationId.equals("")) {
			Log.d("GAtest", "Registration not found");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID since the existing regID is not guaranteed to work with the
		// new app version.
		int registeredVersion = prefs.getInt(context.getString(R.string.gcm_app_version_property), Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.d("GAtest", "App version changed");
			return "";
		}
		return registrationId;
	}

	private static SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences, but
		// how you store the regID in your app is up to you.
		return context.getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private static void registerInBackground(final Activity act, final Context app) {
		final String referral = act.getSharedPreferences("ga_prefs", Context.MODE_PRIVATE).getString("utm_content", "");
		new AsyncTask<Void, Integer, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					GoogleCloudMessaging gcm = MyApplication.getGcmInstance();
					regID = gcm.register(act.getString(R.string.gcm_api_id));
					msg = "Device registered, registration ID=" + regID;

					// send the registration ID to the server
					sendRegistrationIdToBackend(act, regID, referral);

					// Persist the regID - no need to register again.
					storeRegistrationId(act, app, regID);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register. Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				// Toast.makeText(act, msg, Toast.LENGTH_LONG).show();
				Log.d("GAtest", msg);
			}
		}.execute(null, null, null);
	}

	private static void sendRegistrationIdToBackend(Activity act, String regId, String referral) {
		new RegisterID(act).execute("", "", "", regId, "", referral);
	}

	private static void storeRegistrationId(Context act, Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(act);
		int appVersion = getAppVersion(context);
		Log.d("GAtest", "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(act.getString(R.string.gcm_reg_id_property), regId);
		editor.putInt(act.getString(R.string.gcm_app_version_property), appVersion);
		editor.commit();
		/*if (SaveHelper.SaveTextFile(regId)) {
			Toast.makeText(act, "created file with reg_id", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(act, "failed to create file with reg_id", Toast.LENGTH_LONG).show();
		}*/
	}
}
