package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.QuestionSelectData;
import com.olyware.mathlock.database.DatabaseManager;

public class PreferenceHelper {
	final public static String MONEY_PREFS = "Packages";
	final public static String CHALLENGE_PREFS = "Challenge";
	final public static String BET_DEFAULT = "default_bet_percent";
	final public static String QUESTION_NUMBER = "num_questions";
	final public static String DIFFICULTY_MIN = "difficulty_min";
	final public static String DIFFICULTY_MAX = "difficulty_max";

	public static ArrayList<String> getDisplayableUnlockedPackages(Context ctx, DatabaseManager dbManager) {
		ArrayList<String> list = new ArrayList<String>();
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, Context.MODE_PRIVATE);
		String[] unlockPackageKeys = ctx.getResources().getStringArray(R.array.unlock_package_keys);
		List<String> displayPackageKeys = EZ.list(ctx.getResources().getStringArray(R.array.display_packages));
		displayPackageKeys.addAll(dbManager.getAllCustomCategories());

		if (sharedPrefsMoney.getBoolean("unlock_all", false)) {
			list.addAll(displayPackageKeys);
		} else {
			// list.add(displayPackageKeys.get(0)); // All
			for (int i = 1; i < displayPackageKeys.size(); i++) {
				if (i < unlockPackageKeys.length) {
					if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false)) {
						list.add(displayPackageKeys.get(i));
					}
				} else {
					list.add(displayPackageKeys.get(i));
				}
			}
			if (list.size() > 1)
				list.add(0, displayPackageKeys.get(0));
		}
		return list;
	}

	public static boolean[] getChallengePacksChecked(Context ctx, ArrayList<String> questionPacks) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		boolean[] checked = new boolean[questionPacks.size()];
		for (int i = 0; i < questionPacks.size(); i++) {
			String questionPack = questionPacks.get(i);
			if (sharedPrefsChallenge.getBoolean(questionPack, false))
				checked[i] = true;
			else
				checked[i] = false;
		}
		return checked;
	}

	public static void storeChallengePacks(Context ctx, ArrayList<QuestionSelectData> questionPacks) {
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		for (QuestionSelectData questionPack : questionPacks) {
			sharedPrefsChallengeEdit.putBoolean(questionPack.getName(), questionPack.isChecked());
		}
		sharedPrefsChallengeEdit.commit();
	}

	public static Bundle getChallengeSettings(Context ctx) {
		Bundle diff = new Bundle();
		SharedPreferences sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		diff.putFloat(BET_DEFAULT, sharedPrefsChallengeEdit.getFloat(BET_DEFAULT, .5f));
		diff.putInt(QUESTION_NUMBER, sharedPrefsChallengeEdit.getInt(QUESTION_NUMBER, 1));
		diff.putInt(DIFFICULTY_MIN, sharedPrefsChallengeEdit.getInt(DIFFICULTY_MIN, 0));
		diff.putInt(DIFFICULTY_MAX, sharedPrefsChallengeEdit.getInt(DIFFICULTY_MAX, 1));
		return diff;
	}

	public static void storeChallengeSettings(Context ctx, float percent, int numQuestions, int difficultyMin, int difficultyMax) {
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putFloat(BET_DEFAULT, percent);
		sharedPrefsChallengeEdit.putInt(QUESTION_NUMBER, numQuestions);
		sharedPrefsChallengeEdit.putInt(DIFFICULTY_MIN, difficultyMin);
		sharedPrefsChallengeEdit.putInt(DIFFICULTY_MAX, difficultyMax);
		sharedPrefsChallengeEdit.commit();
	}

	public static void storeChallengeDifficulty(Context ctx, int difficultyMin, int difficultyMax) {
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putInt(DIFFICULTY_MIN, difficultyMin);
		sharedPrefsChallengeEdit.putInt(DIFFICULTY_MAX, difficultyMax);
		sharedPrefsChallengeEdit.commit();
	}

	public static void storeChallengeQuestions(Context ctx, int numQuestions) {
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putInt(QUESTION_NUMBER, numQuestions);
		sharedPrefsChallengeEdit.commit();
	}

	public static void storeChallengeBetPercent(Context ctx, float percent) {
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putFloat(BET_DEFAULT, percent);
		sharedPrefsChallengeEdit.commit();
	}
}
