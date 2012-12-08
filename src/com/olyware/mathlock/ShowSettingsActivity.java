package com.olyware.mathlock;

import com.olyware.math.lock.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ShowSettingsActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}