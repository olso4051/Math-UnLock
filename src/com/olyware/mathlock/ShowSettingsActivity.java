package com.olyware.mathlock;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.WindowManager;

import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.utils.EggHelper;

public class ShowSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private String[] unlockPackageKeys, unlockAllKeys, settingsPackageKeys, EggKeys;
	private List<String> categories;
	private int[] EggMaxValues;
	private int fromOldValueIndex, toOldValueIndex;
	private ListPreference fromLanguage, toLanguage, maxDiff, minDiff, lockscreen2;
	private DatabaseManager dbManager;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		unlockAllKeys = ArrayUtils.addAll(unlockPackageKeys, getResources().getStringArray(R.array.unlock_extra_keys));
		settingsPackageKeys = getResources().getStringArray(R.array.settings_keys);
		EggKeys = getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = getResources().getIntArray(R.array.egg_max_values);

		dbManager = new DatabaseManager(getApplicationContext());
		categories = dbManager.getAllCustomCategories();

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
			// Preference Pref_Packages2 = findPreference(settingsPackageKeys[i - 1] + "2");
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
			// if (Pref_Packages2 != null)
			// Pref_Packages2.setEnabled(set);
			if (settingsPackageKeys[i - 1].equals(getString(R.string.custom_settings)) && (categories.size() > 0)) {
				PreferenceCategory customSettingsCategory = (PreferenceCategory) Pref_Packages;
				for (String cat : categories) {
					CheckBoxPreference pref = new CheckBoxPreference(this);
					pref.setKey(getString(R.string.custom_enable) + cat);
					pref.setTitle(getString(R.string.enable));
					pref.setSummary(getString(R.string.enable_custom_summary) + " " + cat);
					customSettingsCategory.addPreference(pref);
				}
				// findPreference(settingsPackageKeys[i - 1] + "2").setEnabled(set);
			}
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
			setDifficultySummaries(min, max);
		} else if (key.equals("max_tries")) {
			String summary = ((sharedPrefs.getString(key, "1").equals("4")) ? "Unlimited" : (sharedPrefs.getString(key, "1")));
			connectionPref.setSummary(summary);
		} else if (key.equals("type")) {
			EggHelper.unlockEgg(this, EggKeys[7], EggMaxValues[7]);
			connectionPref.setSummary(typeIntToString(sharedPrefs.getString(key, "0")));
		} else if (key.equals("from_language")) {
			// if you changed from_language to the same as to_language then swap to_language to old from_language
			if (fromLanguage.findIndexOfValue(fromLanguage.getValue()) == toOldValueIndex)
				toLanguage.setValueIndex(fromOldValueIndex);
			setLanguageSummaries();
		} else if (key.equals("to_language")) {
			// opposite of from_language change
			if (toLanguage.findIndexOfValue(toLanguage.getValue()) == fromOldValueIndex)
				fromLanguage.setValueIndex(toOldValueIndex);
			setLanguageSummaries();
		} else if (key.equals("lockscreen2")) {
			lockscreen2.setSummary(lockscreen2.getEntry());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		categories = dbManager.getAllCustomCategories();
		PreferenceCategory prefCat = (PreferenceCategory) findPreference(getString(R.string.custom_settings));
		for (String cat : categories) {
			if (prefCat.findPreference(getString(R.string.custom_enable) + cat) == null) {
				CheckBoxPreference pref = new CheckBoxPreference(this);
				pref.setKey(getString(R.string.custom_enable) + cat);
				pref.setTitle(getString(R.string.enable));
				pref.setSummary(getString(R.string.enable_custom_summary) + " " + cat);
				prefCat.addPreference(pref);
			}
		}
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
}