package com.olyware.mathlock;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;
import android.view.WindowManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.service.ScreenService;
import com.olyware.mathlock.utils.PreferenceHelper;

public class ShowSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	final private static String SCREEN_LABEL = "Settings Screen";
	private String mPrefUserInfo, mPrefUserSkipped, mPrefUserLoggedIn;
	private String[] unlockPackageKeys, unlockSubPackageKeys, settingsPackageKeys;
	private List<String> categories;
	private int fromOldValueIndex, toOldValueIndex;
	private ListPreference fromLanguage, toLanguage, maxDiff, minDiff, lockscreen2;
	private DatabaseManager dbManager;
	private Context ctx;

	private class OpenDatabase extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			publishProgress(0);
			dbManager = new DatabaseManager(getApplicationContext());
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void v) {
			addCustomPreferences();
			super.onPostExecute(null);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		ctx = this;

		// MyApplication.getGaTracker().set(Fields.SCREEN_NAME, SCREEN_LABEL);

		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		unlockSubPackageKeys = getResources().getStringArray(R.array.unlock_sub_package_keys);
		settingsPackageKeys = getResources().getStringArray(R.array.settings_keys);
		mPrefUserInfo = getString(R.string.pref_user_info);
		mPrefUserSkipped = getString(R.string.pref_user_skipped);
		mPrefUserLoggedIn = getString(R.string.pref_user_logged_in);

		new OpenDatabase().execute();

		SharedPreferences sharedPrefs = getPreferenceScreen().getSharedPreferences();
		SharedPreferences sharedPrefsMoney = getSharedPreferences("Packages", 0);
		SharedPreferences sharedPrefsUsers = getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);

		// set valid language values
		fromLanguage = (ListPreference) findPreference("from_language");
		toLanguage = (ListPreference) findPreference("to_language");
		setLanguageSummaries();

		// set difficulty
		minDiff = (ListPreference) findPreference("difficulty_min");
		maxDiff = (ListPreference) findPreference("difficulty_max");
		minDiff.setSummary(minDiff.getEntry());
		maxDiff.setSummary(maxDiff.getEntry());

		// Set summary to be the user-description for the selected value
		ListPreference Pref_max_tries = (ListPreference) findPreference("max_tries");
		String summary = ((sharedPrefs.getString("max_tries", "1").equals("4")) ? "Unlimited" : (sharedPrefs.getString("max_tries", "1")));
		Pref_max_tries.setSummary(summary);

		// Set summary of timeout period
		lockscreen2 = (ListPreference) findPreference("lockscreen2");
		lockscreen2.setSummary(lockscreen2.getEntry());

		// enable settings for unlocked packages
		for (int i = 1; i < unlockPackageKeys.length; i++) {
			Preference Pref_Packages = findPreference(settingsPackageKeys[i - 1]);
			boolean set = false;
			if (i < unlockPackageKeys.length)
				if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false) || sharedPrefsMoney.getBoolean(unlockSubPackageKeys[i], false)
						|| sharedPrefsMoney.getBoolean("unlock_all", false))
					set = true;
				else
					set = Pref_Packages.isEnabled();
			else if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false))
				set = true;
			else
				set = Pref_Packages.isEnabled();

			Pref_Packages.setEnabled(set);
		}

		// set logout button title depending on logged in setting
		Preference logoutButton = (PreferenceScreen) findPreference("logout_button");
		if (!sharedPrefsUsers.getBoolean(mPrefUserSkipped, false)) {
			logoutButton.setTitle(getString(R.string.settings_logout));
			// logoutButton.setSummary(getString(R.string.settings_logout));
		} else {
			logoutButton.setTitle(getString(R.string.settings_login));
			// logoutButton.setSumamry(getString(R.string.settings_login));
		}
		final Intent sIntent = new Intent(this, ScreenService.class);
		logoutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				PreferenceHelper.logout(ctx);
				ctx.stopService(sIntent);
				Intent broadcastIntent = new Intent(getString(R.string.logout_receiver_filter));
				LocalBroadcastManager.getInstance(ctx).sendBroadcast(broadcastIntent);
				Intent loginIntent = new Intent(ctx, MainActivity.class);
				loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				loginIntent.putExtra("facebook_logout", true);
				startActivity(loginIntent);
				finish();
				return true;
			}
		});

		// set info button to display info
		Preference tutorialButton = (PreferenceScreen) findPreference("tutorial_button");
		tutorialButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				PreferenceHelper.resetTutorial(ctx);
				Intent mainIntent = new Intent(ctx, MainActivity.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				PreferenceHelper.setShareCompeteShown(getApplicationContext(), false);
				startActivity(mainIntent);
				finish();
				return true;
			}
		});

		// set info button to display info
		Preference infoButton = (PreferenceScreen) findPreference("info_button");
		infoButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent mainIntent = new Intent(ctx, MainActivity.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mainIntent.putExtra("info", true);
				startActivity(mainIntent);
				finish();
				return true;
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
		Preference connectionPref = findPreference(key);

		if (key.equals("difficulty_max") || key.equals("difficulty_min")) {
			int max = Math.max(Integer.parseInt(sharedPrefs.getString(key, "1")),
					Integer.parseInt(sharedPrefs.getString("difficulty_max", "1")));
			int min = Math.min(Integer.parseInt(sharedPrefs.getString(key, "0")),
					Integer.parseInt(sharedPrefs.getString("difficulty_min", "0")));
			if (key.equals("difficulty_max"))
				sendEvent("settings", "difficulty_changed", key, (long) max);
			else
				sendEvent("settings", "difficulty_changed", key, (long) min);
			setDifficultySummaries(min, max);
		} else if (key.equals("max_tries")) {
			String summary = ((sharedPrefs.getString(key, "1").equals("4")) ? "Unlimited" : (sharedPrefs.getString(key, "1")));
			sendEvent("settings", "max_tries_changed", summary, null);
			connectionPref.setSummary(summary);
		} else if (key.equals("from_language")) {
			sendEvent("settings", "language_changed", fromLanguage.getEntry().toString(), null);
			// if you changed from_language to the same as to_language then swap to_language to old from_language
			if (fromLanguage.findIndexOfValue(fromLanguage.getValue()) == toOldValueIndex)
				toLanguage.setValueIndex(fromOldValueIndex);
			setLanguageSummaries();
		} else if (key.equals("to_language")) {
			sendEvent("settings", "language_changed", toLanguage.getEntry().toString(), null);
			// opposite of from_language change
			if (toLanguage.findIndexOfValue(toLanguage.getValue()) == fromOldValueIndex)
				fromLanguage.setValueIndex(toOldValueIndex);
			setLanguageSummaries();
		} else if (key.equals("lockscreen2")) {
			sendEvent("settings", "timeout_changed", lockscreen2.getEntry().toString(), null);
			lockscreen2.setSummary(lockscreen2.getEntry());
		} else if (key.equals("lockscreen")) {
			CheckBoxPreference prefLockscreen = (CheckBoxPreference) findPreference("lockscreen");
			if (prefLockscreen.isChecked())
				sendEvent("settings", "lockscreen", "lockscreen_on", null);
			else
				sendEvent("settings", "lockscreen", "lockscreen_off", null);
		} else if (key.equals("algorithm")) {
			CheckBoxPreference prefLockscreen = (CheckBoxPreference) findPreference("algorithm");
			if (prefLockscreen.isChecked())
				sendEvent("settings", "algorithm", "algorithm_on", null);
			else
				sendEvent("settings", "algorithm", "algorithm_off", null);
		} else if (key.equals("vibration")) {
			CheckBoxPreference prefLockscreen = (CheckBoxPreference) findPreference("vibration");
			if (prefLockscreen.isChecked())
				sendEvent("settings", "vibration", "vibration_on", null);
			else
				sendEvent("settings", "vibration", "vibration_off", null);
		} else if (key.equals("analytics_tracking")) {
			CheckBoxPreference prefLockscreen = (CheckBoxPreference) findPreference("analytics_tracking");
			if (prefLockscreen.isChecked())
				sendEvent("settings", "analytics_tracking", "analytics_tracking_on", null);
			else
				sendEvent("settings", "analytics_tracking", "analytics_tracking_off", null);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		MyApplication.getGaTracker().send(new HitBuilders.AppViewBuilder().build());
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}

	@Override
	protected void onStop() {
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
		super.onStop();

	}

	@Override
	protected void onResume() {
		super.onResume();
		addCustomPreferences();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onAttachedToWindow() {
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	@SuppressWarnings("deprecation")
	private void addCustomPreferences() {
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		if (dbManager != null) {
			PreferenceCategory prefCat = (PreferenceCategory) findPreference(getString(R.string.custom_settings));
			prefCat.removeAll();
			Preference customEdit = new Preference(this);
			customEdit.setTitle(getString(R.string.custom_pack_edit));
			// customEdit.setSummary(getString(R.string.custom_pack_edit));
			customEdit.setKey("settings_custom2");
			customEdit.setIntent(new Intent(this, ShowCustomEditActivity.class));
			customEdit.setEnabled(true);
			prefCat.setEnabled(true);
			prefCat.addPreference(customEdit);
			categories = dbManager.getAllCustomCategories();
			String[] customPacks = ctx.getResources().getStringArray(R.array.custom_packs);
			List<String> customCats = Arrays.asList(customPacks);
			SharedPreferences sharedPrefsMoney = getSharedPreferences("Packages", 0);
			for (String cat : categories) {
				CheckBoxPreference pref = new CheckBoxPreference(this);
				// pref.setLayoutResource(R.layout.preference_layout);
				pref.setKey(getString(R.string.custom_enable) + cat);
				pref.setTitle(getString(R.string.enable));
				pref.setSummary(getString(R.string.enable_custom_summary) + " " + cat);
				pref.setDefaultValue(false);
				if (!customCats.contains(cat) || sharedPrefsMoney.getBoolean(unlockSubPackageKeys[6], false)) {
					pref.setEnabled(true);
				} else {
					pref.setEnabled(false);
				}
				prefCat.addPreference(pref);
			}
		}
	}

	private void setLanguageSummaries() {
		fromOldValueIndex = fromLanguage.findIndexOfValue(fromLanguage.getValue());
		fromLanguage.setSummary(fromLanguage.getEntry());
		toOldValueIndex = toLanguage.findIndexOfValue(toLanguage.getValue());
		toLanguage.setSummary(toLanguage.getEntry());
	}

	private void setDifficultySummaries(int min, int max) {
		minDiff.setValueIndex(min);
		maxDiff.setValueIndex(max);
		minDiff.setSummary(minDiff.getEntry());
		maxDiff.setSummary(maxDiff.getEntry());
	}

	private void sendEvent(String category, String action, String label, Long value) {
		MyApplication.getGaTracker().send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());

	}
}