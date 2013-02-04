package com.olyware.mathlock;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.WindowManager;

public class ShowSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private String unlockPackageKeys[];
	private String settingsPackageKeys[];
	private String DifficultyKeys[];
	private int fromOldValueIndex, toOldValueIndex;
	private ListPreference fromLanguage, toLanguage;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		DifficultyKeys = getResources().getStringArray(R.array.difficulty_keys);
		settingsPackageKeys = getResources().getStringArray(R.array.settings_keys);

		SharedPreferences sharedPrefs = getPreferenceScreen().getSharedPreferences();
		SharedPreferences sharedPrefsMoney = this.getSharedPreferences("Packages", 0);

		// set valid language values
		fromLanguage = (ListPreference) findPreference("from_language");
		toLanguage = (ListPreference) findPreference("to_language");
		setLanguageSummaries(sharedPrefs);

		// Set summary to be the user-description for the selected value
		for (int i = 0; i < DifficultyKeys.length; i++) {
			Preference Pref_diff = findPreference(DifficultyKeys[i]);
			Pref_diff.setSummary(difficultyIntToString(DifficultyKeys[i], sharedPrefs.getString(DifficultyKeys[i], "1")));
		}
		Preference Pref_max_tries = findPreference("max_tries");
		Preference Pref_handed = findPreference("handed");
		Preference Pref_type = findPreference("type");
		String summary = ((sharedPrefs.getString("max_tries", "1").equals("4")) ? "Unlimited" : (sharedPrefs.getString("max_tries", "1")));
		Pref_max_tries.setSummary(summary);
		Pref_handed.setSummary(sharedPrefs.getString("handed", "Right"));
		Pref_type.setSummary(typeIntToString(sharedPrefs.getString("type", "2")));

		// enable settings for unlocked packages
		if (sharedPrefsMoney.getBoolean("unlock_all", false))
			for (int i = 0; i < settingsPackageKeys.length; i++) {
				Preference Pref_Packages = findPreference(settingsPackageKeys[i]);
				Pref_Packages.setEnabled(true);
			}
		else
			for (int i = 1; i < unlockPackageKeys.length; i++) {
				Preference Pref_Packages = findPreference(settingsPackageKeys[i - 1]);
				if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false))
					Pref_Packages.setEnabled(true);
				else
					Pref_Packages.setEnabled(false);
			}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
		Preference connectionPref = findPreference(key);
		boolean difficultyChanged = false;
		for (int i = 0; i < DifficultyKeys.length; i++) {
			if (key.equals(DifficultyKeys[i]))
				difficultyChanged = true;
		}
		if (difficultyChanged) {
			// Set summary to be the user-description for the selected value
			connectionPref.setSummary(difficultyIntToString(key, sharedPrefs.getString(key, "")));
		} else if (key.equals("max_tries")) {
			String summary = ((sharedPrefs.getString(key, "1").equals("4")) ? "Unlimited" : (sharedPrefs.getString(key, "1")));
			connectionPref.setSummary(summary);
		} else if (key.equals("handed")) {
			connectionPref.setSummary(sharedPrefs.getString(key, "Right"));
		} else if (key.equals("type")) {
			connectionPref.setSummary(typeIntToString(sharedPrefs.getString(key, "2")));
		} else if (key.equals("from_language")) {
			// if you changed from_language to the same as to_language then swap to_language to old from_language
			if (fromLanguage.findIndexOfValue(fromLanguage.getValue()) == toOldValueIndex)
				toLanguage.setValueIndex(fromOldValueIndex);
			setLanguageSummaries(sharedPrefs);
		} else if (key.equals("to_language")) {
			// opposite of from_language change
			if (toLanguage.findIndexOfValue(toLanguage.getValue()) == fromOldValueIndex)
				fromLanguage.setValueIndex(toOldValueIndex);
			setLanguageSummaries(sharedPrefs);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
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

	private String difficultyIntToString(String key, String Number) {
		int diffNum = 0;
		try {
			diffNum = Integer.parseInt(Number);
		} catch (NumberFormatException nfe) {
			System.out.println("Could not parse " + nfe);
		}
		if (key.equals("difficulty_math") || key.equals("difficulty_vocab"))
			return difficultyMathVocabIntToString(diffNum);
		else if (key.equals("difficulty_language"))
			return difficultyLanguageIntToString(diffNum);
		String[] difficulty = getResources().getStringArray(R.array.difficulty_entries);
		return difficulty[diffNum - 1];
	}

	private String difficultyMathVocabIntToString(int diffNum) {
		String[] difficulty = getResources().getStringArray(R.array.difficulty_math_vocab_entries);
		return difficulty[diffNum - 1];
	}

	private String difficultyLanguageIntToString(int diffNum) {
		String[] difficulty = getResources().getStringArray(R.array.difficulty_language_entries);
		return difficulty[diffNum - 1];
	}

	private String typeIntToString(String key) {
		int diffNum = 0;
		try {
			diffNum = Integer.parseInt(key);
		} catch (NumberFormatException nfe) {
			System.out.println("Could not parse " + nfe);
		}
		Resources res = getResources();
		String[] s = res.getStringArray(R.array.type_entries);
		return s[diffNum];
	}

	private void setLanguageSummaries(SharedPreferences sharedPrefs) {
		fromOldValueIndex = fromLanguage.findIndexOfValue(fromLanguage.getValue());
		fromLanguage.setSummary(fromLanguage.getEntry());
		toOldValueIndex = toLanguage.findIndexOfValue(toLanguage.getValue());
		toLanguage.setSummary(toLanguage.getEntry());
	}
}