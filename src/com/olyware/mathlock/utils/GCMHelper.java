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
import com.olyware.mathlock.service.ConfirmID;
import com.olyware.mathlock.service.RegisterID;

public class GCMHelper {

	final private static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static String regID;
	static GCMResponse mCallback;

	public interface GCMResponse {
		void GCMResult(boolean result);

		void RegisterIDResult(int result);
	}

	public static boolean registerAndStoreGCM(final Activity act, final Context app) {
		if (checkPlayServices(act)) {
			try {
				mCallback = (GCMResponse) act;
			} catch (ClassCastException e) {
				throw new ClassCastException(act.toString() + " must implement GCMResponse");
			}
			regID = getRegistrationId(app);
			SharedPreferences prefsGA = act.getSharedPreferences("ga_prefs", Context.MODE_PRIVATE);
			SharedPreferences sharedPrefsUserInfo = act.getSharedPreferences(act.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
			String username = sharedPrefsUserInfo.getString(act.getString(R.string.pref_user_username), "");
			String userID = sharedPrefsUserInfo.getString(act.getString(R.string.pref_user_userid), "");
			String referral = sharedPrefsUserInfo.getString(act.getString(R.string.pref_user_referrer), "");
			String birth = sharedPrefsUserInfo.getString(act.getString(R.string.pref_user_facebook_birth), "");
			String gender = sharedPrefsUserInfo.getString(act.getString(R.string.pref_user_facebook_gender), "");
			String location = sharedPrefsUserInfo.getString(act.getString(R.string.pref_user_facebook_location), "");
			if (regID.equals("")) {
				SharedPreferences.Editor editorGA = prefsGA.edit();
				editorGA.putBoolean("reg_uploaded", false).commit();
				registerInBackground(act, app, true);
			} else if (!prefsGA.getBoolean("reg_uploaded", false)) {
				storeRegistrationId(act, app, regID);
				sendRegistrationIdToBackend(act, username, regID, userID, referral, birth, gender, location);
			} else if (!sharedPrefsUserInfo.getBoolean(act.getString(R.string.pref_user_confirmed), false) && !userID.equals("")) {
				confirmID(act, userID);
			}
		} else {
			Toast.makeText(act, "No valid Google Play Services APK found.", Toast.LENGTH_LONG).show();
		}
		return true;
	}

	public static void registerGCM(final Activity act, final Context app) {
		try {
			mCallback = (GCMResponse) act;
		} catch (ClassCastException e) {
			throw new ClassCastException(act.toString() + " must implement GCMResponse");
		}
		if (checkPlayServices(act)) {
			regID = getRegistrationId(app);
			Log.d("GAtest", "regID = " + regID);
			if (regID.equals("")) {
				Log.d("GAtest", "registerInBackground");
				registerInBackground(act, app, false);
			} else {
				mCallback.GCMResult(true);
			}
		} else {
			Toast.makeText(act, "No valid Google Play Services APK found.", Toast.LENGTH_LONG).show();
			mCallback.GCMResult(true);
		}
	}

	public static boolean checkPlayServices(Activity act) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(act);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, act, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				act.finish();
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

	private static void registerInBackground(final Activity act, final Context app, final boolean sendToBackend) {
		SharedPreferences sharedPrefs = act.getSharedPreferences(act.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		final String username = sharedPrefs.getString(act.getString(R.string.pref_user_username), "");
		final String userID = sharedPrefs.getString(act.getString(R.string.pref_user_userid), "");
		final String referral = sharedPrefs.getString(act.getString(R.string.pref_user_referrer), "");
		final String birth = sharedPrefs.getString(act.getString(R.string.pref_user_facebook_birth), "");
		final String gender = sharedPrefs.getString(act.getString(R.string.pref_user_facebook_gender), "");
		final String location = sharedPrefs.getString(act.getString(R.string.pref_user_facebook_location), "");

		new AsyncTask<Void, Integer, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					GoogleCloudMessaging gcm = MyApplication.getGcmInstance();
					regID = gcm.register(act.getString(R.string.gcm_api_id));
					// msg = "Device registered, registration ID=" + regID;

					// send the registration ID to the server
					if (sendToBackend) {
						Log.d("GAtest", "sendRegistrationIdToBackend");
						sendRegistrationIdToBackend(act, username, regID, userID, referral, birth, gender, location);
					}
					// else
					// mCallback.GCMResult(true);

					// Persist the regID - no need to register again.
					storeRegistrationId(act, app, regID);
				} catch (IOException ex) {
					// msg = "Error :" + ex.getMessage();
					// mCallback.GCMResult(false);
					return false;
					// If there is an error, don't just keep trying to register. Require the user to click a button again, or perform
					// exponential back-off.
				}
				return true;
			}

			@Override
			protected void onPostExecute(Boolean GCMresult) {
				// Toast.makeText(act, msg, Toast.LENGTH_LONG).show();
				// Log.d("GAtest", msg);
				if (!sendToBackend)
					mCallback.GCMResult(GCMresult);
			}
		}.execute(null, null, null);
	}

	private static void sendRegistrationIdToBackend(Activity act, String username, String regId, String userID, String referral,
			String birth, String gender, String location) {
		new RegisterID(act) {
			@Override
			protected void onPostExecute(Integer result) {
				mCallback.RegisterIDResult(result);
			}
		}.execute(username, regId, userID, referral, birth, gender, location);
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

	private static void confirmID(final Context ctx, String userID) {
		new ConfirmID(ctx) {
			@Override
			protected void onPostExecute(Integer result) {
				SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info),
						Context.MODE_PRIVATE);
				if (result == 0)
					sharedPrefsUserInfo.edit().putBoolean(ctx.getString(R.string.pref_user_confirmed), true).commit();
				else
					sharedPrefsUserInfo.edit().putBoolean(ctx.getString(R.string.pref_user_confirmed), false).commit();
			}

		}.execute(userID);
	}
}
