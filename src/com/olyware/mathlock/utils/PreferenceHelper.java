package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.QuestionSelectData;
import com.olyware.mathlock.database.DatabaseManager;

public class PreferenceHelper {
	final public static String MONEY_PREFS = "Packages";
	final public static String CHALLENGE_PREFS = "Challenge";
	final public static String USER_NAME = "user_name";
	final public static String BET_DEFAULT = "default_bet_percent";
	final public static String QUESTION_NUMBER = "num_questions";
	final public static String DIFFICULTY_MIN = "difficulty_min";
	final public static String DIFFICULTY_MAX = "difficulty_max";
	final public static String SHARE_HASH = "share_hash_latest";

	public static enum ChallengeStatus {
		Accepted(0), Declined(1), Done(2), Undefined(3);
		private int value;

		private ChallengeStatus(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static int getDefaultValue() {
			return 3;
		}

		public static ChallengeStatus valueOf(int value) {
			switch (value) {
			case 0:
				return Accepted;
			case 1:
				return Declined;
			case 2:
				return Done;
			case 3:
				return Undefined;
			default:
				return Undefined;
			}
		}
	}

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

	public static ArrayList<Integer> getDisplayableUnlockedPackageIDs(Context ctx, DatabaseManager dbManager) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, Context.MODE_PRIVATE);
		String[] unlockPackageKeys = ctx.getResources().getStringArray(R.array.unlock_package_keys);
		List<String> displayPackageKeys = EZ.list(ctx.getResources().getStringArray(R.array.display_packages));
		displayPackageKeys.addAll(dbManager.getAllCustomCategories());
		List<Integer> displayPackageIDs = new ArrayList<Integer>(displayPackageKeys.size());
		for (int i = 0; i < displayPackageKeys.size(); i++)
			displayPackageIDs.add(i);

		if (sharedPrefsMoney.getBoolean("unlock_all", false)) {
			list.addAll(displayPackageIDs);
		} else {
			// list.add(displayPackageIDs.get(0)); // All
			for (int i = 1; i < displayPackageKeys.size(); i++) {
				if (i < unlockPackageKeys.length) {
					if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false)) {
						list.add(displayPackageIDs.get(i));
					}
				} else {
					list.add(displayPackageIDs.get(i));
				}
			}
			if (list.size() > 1)
				list.add(0, displayPackageIDs.get(0));
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

	public static void storeChallengeStatus(Context ctx, String challengeID, ChallengeStatus status) {
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putInt(challengeID, status.getValue());
		sharedPrefsChallengeEdit.commit();
	}

	public static void storeChallengeStatus(Context ctx, String challengeID, ChallengeStatus status, String userName) {
		Loggy.d("set challengeID and challenge+username");
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putInt(challengeID, status.getValue());
		sharedPrefsChallengeEdit.putString(challengeID + USER_NAME, userName);
		sharedPrefsChallengeEdit.commit();
	}

	public static ChallengeStatus getChallengeStatus(Context ctx, String challengeID) {
		Loggy.d("set challengeID");
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return ChallengeStatus.valueOf(sharedPrefsChallenge.getInt(challengeID, ChallengeStatus.getDefaultValue()));
	}

	public static String getChallengeUserName(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getString(challengeID + USER_NAME, ctx.getString(R.string.challenge_default_opponent));
	}

	public static void increaseMoney(Context ctx, int amount) {
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsMoney.putInt("money", sharedPrefsMoney.getInt("money", 0) + amount);
		editorPrefsMoney.commit();
	}

	public static void decreaseMoneyNoDebt(Context ctx, int amount) {
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editorPrefsMoney = sharedPrefsMoney.edit();
		int initMoney = sharedPrefsMoney.getInt("money", 0);
		int money = initMoney - amount;
		if (money < 0)
			money = 0;
		int newAmount = initMoney - money;
		editorPrefsMoney.putInt("money", sharedPrefsMoney.getInt("money", 0) - newAmount);
		editorPrefsMoney.commit();
	}

	public static void storeFacebookMe(Context ctx, String faceID, String faceName, String gender, String email) {
		SharedPreferences.Editor editorPrefsUsers = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE)
				.edit();
		editorPrefsUsers.putString(ctx.getString(R.string.pref_user_facebook_id), faceID)
				.putString(ctx.getString(R.string.pref_user_facebook_name), faceName)
				.putString(ctx.getString(R.string.pref_user_facebook_gender), gender)
				.putString(ctx.getString(R.string.pref_user_facebook_email), email).commit();
	}

	public static boolean shouldDoMeRequest(Context ctx) {
		SharedPreferences sharedPrefsUsers = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		if (sharedPrefsUsers.getString(ctx.getString(R.string.pref_user_facebook_id), "").equals(""))
			return true;
		else
			return false;
	}

	public static void storeLatestShareHash(Context ctx, String hash) {
		SharedPreferences sharedPrefsDefault = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editorPrefsDefault = sharedPrefsDefault.edit();
		editorPrefsDefault.putString(SHARE_HASH, hash).commit();
	}

	public static String getLatestShareHash(Context ctx) {
		SharedPreferences sharedPrefsDefault = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sharedPrefsDefault.getString(SHARE_HASH, "");
	}

	public static void logout(Context ctx) {
		SharedPreferences sharedPrefsUsers = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		sharedPrefsUsers.edit().putBoolean(ctx.getString(R.string.pref_user_skipped), false)
				.putBoolean(ctx.getString(R.string.pref_user_logged_in), false).commit();
		ContactHelper.removeStoredContacts(ctx);
	}
}
