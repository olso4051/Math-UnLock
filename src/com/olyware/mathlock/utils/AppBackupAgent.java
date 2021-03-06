package com.olyware.mathlock.utils;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

public class AppBackupAgent extends BackupAgentHelper {
	// The names of the SharedPreferences groups that the application maintains. These
	// are the same strings that are passed to getSharedPreferences(String, int).
	static final String PREFS_DEFAULT = "com.olyware.mathlock_preferences";
	static final String PREFS_PACKAGES = "Packages";
	static final String PREFS_STATS = "Stats";
	static final String PREFS_EGGS = "Eggs";
	static final String PREFS_APPS = "Apps";
	static final String PREFS_CHALLENGE = "Challenge";
	static final String PREFS_USER = "user_info";

	// An arbitrary string used within the BackupAgentHelper implementation to
	// identify the SharedPreferencesBackupHelper's data.
	static final String MY_PREFS_BACKUP_KEY = "myprefs";

	// Simply allocate a helper and install it
	@Override
	public void onCreate() {
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS_DEFAULT, PREFS_PACKAGES, PREFS_STATS,
				PREFS_EGGS, PREFS_APPS, PREFS_CHALLENGE, PREFS_USER);
		addHelper(MY_PREFS_BACKUP_KEY, helper);
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		super.onRestore(data, appVersionCode, newState);
	}

}
