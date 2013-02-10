package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.backup.BackupManager;
import android.content.Context;

public class EZ {
	public static <T> List<T> list(T... objects) {
		return new ArrayList<T>(Arrays.asList(objects));
	}

	public static <T> List<T> list(Collection<T> collection) {
		return new ArrayList<T>(collection);
	}

	public static <T> Set<T> set(T... objects) {
		return set(Arrays.asList(objects));
	}

	public static <T> Set<T> set(Collection<T> collection) {
		return new HashSet<T>(collection);
	}

	/**
	 * Starts a backup of all preferences files
	 * 
	 * @param context
	 */
	public static void requestBackup(Context context) {
		BackupManager bm = new BackupManager(context);
		bm.dataChanged();
	}
}
