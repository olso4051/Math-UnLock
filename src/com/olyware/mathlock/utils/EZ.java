package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.olyware.mathlock.views.AnswerView;
import com.olyware.mathlock.views.GraphView;
import com.olyware.mathlock.views.JoystickView;

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

	/**
	 * Starts a restore of all preferences files
	 * 
	 * @param context
	 */
	public static void requestRestore(Context context) {
		BackupManager bm = new BackupManager(context);
		RestoreObserver observer = new RestoreObserver() {

			@Override
			public void onUpdate(int nowBeingRestored, String currentPackage) {
				super.onUpdate(nowBeingRestored, currentPackage);
			}

			@Override
			public void restoreFinished(int error) {
				super.restoreFinished(error);
			}

			@Override
			public void restoreStarting(int numPackages) {
				super.restoreStarting(numPackages);
			}
		};
		bm.requestRestore(observer);
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
			/*if (mChild instanceof EquationView) {
				// Set the font if it is a TextView.
				((EquationView) mChild).setTopRightTypeface(font);
			}*/
			if (mChild instanceof TextView) {
				// Set the font if it is a TextView.
				((TextView) mChild).setTypeface(font);
			} else if (mChild instanceof JoystickView) {
				((JoystickView) mChild).setTypeface(font);
			} else if (mChild instanceof AnswerView) {
				((AnswerView) mChild).setTypeface(font);
			} else if (mChild instanceof GraphView) {
				((GraphView) mChild).setTypeface(font);
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
				// Unable to set typeface: View is not a TextView!
			}
		}
	}
}
