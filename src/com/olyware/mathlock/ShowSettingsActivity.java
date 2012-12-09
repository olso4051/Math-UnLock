package com.olyware.mathlock;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class ShowSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	// private SharedPreferences sharedPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences sharedPrefs = getPreferenceScreen().getSharedPreferences();

		Preference Pref_diff_math = findPreference("difficulty_math");
		Preference Pref_diff_vocab = findPreference("difficulty_vocab");
		Preference Pref_diff_trans = findPreference("difficulty_translate");
		// Set summary to be the user-description for the selected value
		Pref_diff_math.setSummary(difficultyIntToString(sharedPrefs.getString("difficulty_math", "1")));
		Pref_diff_vocab.setSummary(difficultyIntToString(sharedPrefs.getString("difficulty_vocab", "1")));
		Pref_diff_trans.setSummary(difficultyIntToString(sharedPrefs.getString("difficulty_translate", "1")));

		/*sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPrefs.registerOnSharedPreferenceChangeListener(this);*/
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
		if (key.equals("difficulty_math") || key.equals("difficulty_vocab") || key.equals("difficulty_translate")) {
			Preference connectionPref = findPreference(key);
			// Set summary to be the user-description for the selected value
			connectionPref.setSummary(difficultyIntToString(sharedPrefs.getString(key, "")));
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

}