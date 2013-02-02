package com.olyware.mathlock;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.WindowManager;

public class ShowSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private String unlockPackageKeys[] = { "unlock_all", "unlock_math", "unlock_vocab", "unlock_language", "unlock_act", "unlock_sat",
			"unlock_gre", "unlock_toddler", "unlock_engineer" };
	private String settingsPackageKeys[] = { "settings_math", "settings_vocab", "settings_language", "settings_act", "settings_sat",
			"settings_gre", "settings_toddler", "settings_engineer" };
	private String difficultyPackageKeys[] = { "difficulty_math", "difficulty_vocab", "difficulty_language", "difficulty_act",
			"difficulty_sat", "difficulty_gre", "difficulty_toddler", "difficulty_engineer" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences sharedPrefs = getPreferenceScreen().getSharedPreferences();
		SharedPreferences sharedPrefsMoney = this.getSharedPreferences("Packages", 0);

		for (int i = 0; i < difficultyPackageKeys.length; i++) {
			Preference Pref_diff = findPreference(difficultyPackageKeys[i]);
			Pref_diff.setSummary(difficultyIntToString(sharedPrefs.getString(difficultyPackageKeys[i], "1")));
		}
		// Preference Pref_diff_math = findPreference("difficulty_math");
		// Preference Pref_diff_vocab = findPreference("difficulty_vocab");
		// Preference Pref_diff_trans = findPreference("difficulty_language");
		Preference Pref_max_tries = findPreference("max_tries");
		Preference Pref_handed = findPreference("handed");
		Preference Pref_type = findPreference("type");
		// Set summary to be the user-description for the selected value
		// Pref_diff_math.setSummary(difficultyIntToString(sharedPrefs.getString("difficulty_math", "1")));
		// Pref_diff_vocab.setSummary(difficultyIntToString(sharedPrefs.getString("difficulty_vocab", "1")));
		// Pref_diff_trans.setSummary(difficultyIntToString(sharedPrefs.getString("difficulty_language", "1")));
		Pref_max_tries.setSummary(sharedPrefs.getString("max_tries", "1"));
		Pref_handed.setSummary(sharedPrefs.getString("handed", "Right"));
		Pref_type.setSummary(typeIntToString(sharedPrefs.getString("type", "2")));

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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
		Preference connectionPref = findPreference(key);
		boolean difficultyChanged = false;
		for (int i = 0; i < difficultyPackageKeys.length; i++) {
			if (key.equals(difficultyPackageKeys[i]))
				difficultyChanged = true;
		}
		if (difficultyChanged) {
			// Set summary to be the user-description for the selected value
			connectionPref.setSummary(difficultyIntToString(sharedPrefs.getString(key, "")));
		} else if (key.equals("max_tries")) {
			String summary = ((sharedPrefs.getString(key, "1").equals("4")) ? "Unlimited" : (sharedPrefs.getString(key, "1")));
			connectionPref.setSummary(summary);
		} else if (key.equals("handed")) {
			connectionPref.setSummary(sharedPrefs.getString(key, "Right"));
		} else if (key.equals("type")) {
			connectionPref.setSummary(typeIntToString(sharedPrefs.getString(key, "2")));
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