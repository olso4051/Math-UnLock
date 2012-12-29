package com.olyware.mathlock;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.WindowManager;

public class ShowSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences sharedPrefs = getPreferenceScreen().getSharedPreferences();

		Preference Pref_diff_math = findPreference("difficulty_math");
		Preference Pref_diff_vocab = findPreference("difficulty_vocab");
		Preference Pref_diff_trans = findPreference("difficulty_translate");
		Preference Pref_max_tries = findPreference("max_tries");
		Preference Pref_handed = findPreference("handed");
		Preference Pref_type = findPreference("type");
		// Set summary to be the user-description for the selected value
		Pref_diff_math.setSummary(difficultyIntToString(sharedPrefs.getString("difficulty_math", "1")));
		Pref_diff_vocab.setSummary(difficultyIntToString(sharedPrefs.getString("difficulty_vocab", "1")));
		Pref_diff_trans.setSummary(difficultyIntToString(sharedPrefs.getString("difficulty_translate", "1")));
		Pref_max_tries.setSummary(sharedPrefs.getString("max_tries", "1"));
		Pref_handed.setSummary(sharedPrefs.getString("handed", "Right"));
		Pref_type.setSummary(typeIntToString(sharedPrefs.getString("type", "2")));
		// Toast.makeText(this, sharedPrefs.getString("max_tries", "1"), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
		Preference connectionPref = findPreference(key);
		if (key.equals("difficulty_math") || key.equals("difficulty_vocab") || key.equals("difficulty_translate")) {
			// Set summary to be the user-description for the selected value
			connectionPref.setSummary(difficultyIntToString(sharedPrefs.getString(key, "")));
		} else if (key.equals("max_tries")) {
			connectionPref.setSummary(sharedPrefs.getString(key, "1"));
		} else if (key.equals("handed")) {
			connectionPref.setSummary(sharedPrefs.getString(key, "Right"));
		}
		/* Toast.makeText(this, String.valueOf(!key.equals("enable")),Toast.LENGTH_SHORT).show();*/
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onAttachedToWindow() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	private String difficultyIntToString(String key) {
		int diffNum = 0;
		try {
			diffNum = Integer.parseInt(key);
		} catch (NumberFormatException nfe) {
			System.out.println("Could not parse " + nfe);
		}
		switch (diffNum) {
		case 1:
			return "Easy";
		case 2:
			return "Medium";
		case 3:
			return "Hard";
		default:
			return "Unknown";
		}
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

}