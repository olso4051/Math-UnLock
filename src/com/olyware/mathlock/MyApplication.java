package com.olyware.mathlock;

import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.Tracker;

@ReportsCrashes(formKey = "", // will not be used
mailTo = "olso4051@umn.edu", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text)
public class MyApplication extends Application {
	private static GoogleAnalytics mGa;
	private static Tracker mTracker;

	// Prevent hits from being sent to reports, i.e. during testing.
	private static final boolean GA_IS_DRY_RUN = false;

	// GA Logger verbosity.
	private static final LogLevel GA_LOG_VERBOSITY = LogLevel.VERBOSE;

	// Key used to store a user's tracking preferences in SharedPreferences.
	private static final String TRACKING_PREF_KEY = "analytics_tracking";

	@Override
	public void onCreate() {
		super.onCreate();

		// The following line triggers the initialization of ACRA
		// ACRA.init(this);

		initializeGa();
	}

	/*
	 * Method to handle basic Google Analytics initialization. This call will not
	 * block as all Google Analytics work occurs off the main thread.
	 */
	private void initializeGa() {
		mGa = GoogleAnalytics.getInstance(this);
		mTracker = mGa.getTracker(getString(R.string.google_analytics_tracking_id));

		// Set dryRun flag.
		mGa.setDryRun(GA_IS_DRY_RUN);

		// Set Logger verbosity.
		mGa.getLogger().setLogLevel(GA_LOG_VERBOSITY);

		// Set the opt out flag when user updates a tracking preference.
		SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(!userPrefs.getBoolean(TRACKING_PREF_KEY, true));
		userPrefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals(TRACKING_PREF_KEY)) {
					GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(!sharedPreferences.getBoolean(key, true));
				}
			}
		});
	}

	/*
	 * Returns the Google Analytics tracker.
	 */
	public static Tracker getGaTracker() {
		return mTracker;
	}

	/*
	 * Returns the Google Analytics instance.
	 */
	public static GoogleAnalytics getGaInstance() {
		return mGa;
	}
}