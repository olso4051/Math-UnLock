package com.olyware.mathlock.utils;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class AppBackupAgent extends BackupAgentHelper {
	// The names of the SharedPreferences groups that the application maintains. These
	// are the same strings that are passed to getSharedPreferences(String, int).
	static final String PREFS_PACKAGES = "Packages";
	static final String PREFS_STATS = "Stats";
	static final String PREFS_EGGS = "Eggs";
	static final String PREFS_APPS = "Apps";

	// An arbitrary string used within the BackupAgentHelper implementation to
	// identify the SharedPreferencesBackupHelper's data.
	static final String MY_PREFS_BACKUP_KEY = "myprefs";

	// Simply allocate a helper and install it
	@Override
	public void onCreate() {
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS_PACKAGES, PREFS_STATS, PREFS_EGGS, PREFS_APPS);
		addHelper(MY_PREFS_BACKUP_KEY, helper);
	}
}
