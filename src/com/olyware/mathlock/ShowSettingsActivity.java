package com.olyware.mathlock;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

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
import android.view.WindowManager;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.utils.EggHelper;

public class ShowSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	final private static String SCREEN_LABEL = "Settings Screen";
	private String[] unlockPackageKeys, unlockAllKeys, settingsPackageKeys, EggKeys;
	private List<String> categories;
	private int[] EggMaxValues;
	private int fromOldValueIndex, toOldValueIndex;
	private ListPreference fromLanguage, toLanguage, maxDiff, minDiff, lockscreen2;
	private DatabaseManager dbManager;

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

		MyApplication.getGaTracker().set(Fields.SCREEN_NAME, SCREEN_LABEL);

		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		unlockAllKeys = ArrayUtils.addAll(unlockPackageKeys, getResources().getStringArray(R.array.unlock_extra_keys));
		settingsPackageKeys = getResources().getStringArray(R.array.settings_keys);
		EggKeys = getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = getResources().getIntArray(R.array.egg_max_values);

		new OpenDatabase().execute();

		SharedPreferences sharedPrefs = getPreferenceScreen().getSharedPreferences();
		SharedPreferences sharedPrefsMoney = this.getSharedPreferences("Packages", 0);

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
		Preference Pref_max_tries = findPreference("max_tries");
		String summary = ((sharedPrefs.getString("max_tries", "1").equals("4")) ? "Unlimited" : (sharedPrefs.getString("max_tries", "1")));
		Pref_max_tries.setSummary(summary);

		// Set summary of timeout period
		lockscreen2 = (ListPreference) findPreference("lockscreen2");
		lockscreen2.setSummary(lockscreen2.getEntry());

		// Set the available entries and values in the "type" setting
		ListPreference Pref_type = (ListPreference) findPreference("type");
		if (sharedPrefsMoney.getBoolean("unlock_rotating_slide", false))
			if (sharedPrefsMoney.getBoolean("unlock_dynamic_slide", false)) {
				Pref_type.setEntries(getResources().getStringArray(R.array.type_entries012));
				Pref_type.setEntryValues(getResources().getStringArray(R.array.type_values012));
			} else {
				Pref_type.setEntries(getResources().getStringArray(R.array.type_entries01));
				Pref_type.setEntryValues(getResources().getStringArray(R.array.type_values01));
			}
		else if (sharedPrefsMoney.getBoolean("unlock_dynamic_slide", false)) {
			Pref_type.setEntries(getResources().getStringArray(R.array.type_entries02));
			Pref_type.setEntryValues(getResources().getStringArray(R.array.type_values02));
		} else {
			Pref_type.setEntries(getResources().getStringArray(R.array.type_entries012));
			Pref_type.setEntryValues(getResources().getStringArray(R.array.type_values012));
		}
		Pref_type.setSummary(typeIntToString(sharedPrefs.getString("type", "0")));

		// enable settings for unlocked packages
		for (int i = 1; i < unlockAllKeys.length; i++) {
			Preference Pref_Packages = findPreference(settingsPackageKeys[i - 1]);
			boolean set = false;
			if (i < unlockPackageKeys.length)
				if (sharedPrefsMoney.getBoolean(unlockAllKeys[i], false) || sharedPrefsMoney.getBoolean("unlock_all", false))
					set = true;
				else
					set = Pref_Packages.isEnabled();
			else if (sharedPrefsMoney.getBoolean(unlockAllKeys[i], false))
				set = true;
			else
				set = Pref_Packages.isEnabled();

			Pref_Packages.setEnabled(set);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
		Preference connectionPref = findPreference(key);

		if (key.equals("difficulty_max") || key.equals("difficulty_min")) {
			int max = Math.max(Integer.parseInt(sharedPrefs.getString(key, "0")),
					Integer.parseInt(sharedPrefs.getString("difficulty_max", "0")));
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
		} else if (key.equals("type")) {
			EggHelper.unlockEgg(this, EggKeys[7], EggMaxValues[7]);
			String type = typeIntToString(sharedPrefs.getString(key, "0"));
			sendEvent("settings", "unlock_type_changed", type, null);
			connectionPref.setSummary(type);
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
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		MyApplication.getGaTracker().send(MapBuilder.createAppView().build());
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
			customEdit.setKey("settings_custom2");
			customEdit.setIntent(new Intent(this, ShowCustomEditActivity.class));
			prefCat.addPreference(customEdit);
			categories = dbManager.getAllCustomCategories();
			for (String cat : categories) {
				CheckBoxPreference pref = new CheckBoxPreference(this);
				pref.setKey(getString(R.string.custom_enable) + cat);
				pref.setTitle(getString(R.string.enable));
				pref.setSummary(getString(R.string.enable_custom_summary) + " " + cat);
				pref.setDefaultValue(false);
				prefCat.addPreference(pref);
			}
		}
	}

	private String typeIntToString(String key) {
		int diffNum = 0;
		try {
			diffNum = Integer.parseInt(key);
		} catch (NumberFormatException nfe) {
			System.out.println("Could not parse " + nfe);
		}
		String[] s = getResources().getStringArray(R.array.type_entries012);
		return s[diffNum];
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
		MyApplication.getGaTracker().send(MapBuilder.createEvent(category, action, label, value).build());
	}
}