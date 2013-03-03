package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.backup.BackupManager;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EZ {
	private static final Loggy log = new Loggy(EZ.class);

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

	/**
	 * Recursively sets a {@link Typeface} to all {@link TextView}s in a {@link ViewGroup}.
	 */
	public static final void setFont(ViewGroup container, Typeface font) {
		if (container == null || font == null)
			return;

		final int mCount = container.getChildCount();

		// Loop through all of the children.
		for (int i = 0; i < mCount; ++i) {
			final View mChild = container.getChildAt(i);
			if (mChild instanceof TextView) {
				// Set the font if it is a TextView.
				((TextView) mChild).setTypeface(font);
			} else if (mChild instanceof ViewGroup) {
				// Recursively attempt another ViewGroup.
				setFont((ViewGroup) mChild, font);
			}
		}
	}

	public static final void setFont(View view, Typeface font) {
		if (view != null && font != null) {
			if (view instanceof TextView) {
				((TextView) view).setTypeface(font);
			} else {
				log.d("Unable to set typeface: View is not a TextView!");
			}
		}
	}
}
