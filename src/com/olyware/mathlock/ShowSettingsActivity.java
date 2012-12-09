package com.olyware.mathlock;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class ShowSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private SharedPreferences sharedPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPrefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {

		boolean allPackagesDisabled = !sharedPrefs.getBoolean("enable_math", true) && !sharedPrefs.getBoolean("enable_vocab", true)
				&& !sharedPrefs.getBoolean("enable_translate", true);
		boolean appEnabled = sharedPrefs.getBoolean("enable", true);

		// Toast.makeText(this, String.valueOf(!key.equals("enable")),
		// Toast.LENGTH_SHORT).show();

		if (allPackagesDisabled && appEnabled) {
			if (!key.equals("enable")) {
				sharedPrefs.edit().putBoolean("enable", false).commit();
			} else {
				sharedPrefs.edit().putBoolean("enable_math", true);
			}
		}
	}

}