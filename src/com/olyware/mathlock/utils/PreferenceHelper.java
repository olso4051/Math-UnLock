package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.Window;

import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.QuestionSelectData;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.service.CustomContactData;

public class PreferenceHelper {
	final public static String MONEY_PREFS = "Packages";
	final public static String CHALLENGE_PREFS = "Challenge";
	final public static String LAYOUT_PREFS = "Layout_Size";

	final public static String LAYOUT_WIDTH = "layout_width";
	final public static String LAYOUT_HEIGHT = "layout_height";
	final public static String LAYOUT_STATUS = "layout_status_bar_height";

	final public static String STATUS = "status";
	final public static String STATE = "state";
	final public static String HIQ_USER_NAME = "hiq_user_name";
	final public static String HIQ_USER_ID = "hiq_user_id";

	final public static String BET = "bet";
	final public static String DIFFMIN = "diff_min";
	final public static String DIFFMAX = "diff_max";
	final public static String QUESTIONS = "questions";

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

	public static void storeChallengeStatus(Context ctx, String challengeID, ChallengeStatus status, CustomContactData.ChallengeState state) {
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putInt(challengeID + STATUS, status.getValue());
		sharedPrefsChallengeEdit.putInt(challengeID + STATE, state.getValue());
		sharedPrefsChallengeEdit.commit();
	}

	public static void storeChallengeStatus(Context ctx, String challengeID, ChallengeStatus status,
			CustomContactData.ChallengeState state, String userName, String hiqUserID, int bet, int diffMin, int diffMax, int questions) {
		Loggy.d("set challengeID and challenge+username");
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putInt(challengeID + STATUS, status.getValue());
		sharedPrefsChallengeEdit.putInt(challengeID + STATE, state.getValue());
		sharedPrefsChallengeEdit.putString(challengeID + HIQ_USER_NAME, userName);
		sharedPrefsChallengeEdit.putInt(challengeID + BET, bet);
		sharedPrefsChallengeEdit.putInt(challengeID + DIFFMIN, diffMin);
		sharedPrefsChallengeEdit.putInt(challengeID + DIFFMAX, diffMax);
		sharedPrefsChallengeEdit.putInt(challengeID + QUESTIONS, questions);
		sharedPrefsChallengeEdit.putString(hiqUserID, challengeID);
		sharedPrefsChallengeEdit.commit();
	}

	public static ChallengeStatus getChallengeStatusFromID(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return ChallengeStatus.valueOf(sharedPrefsChallenge.getInt(challengeID + STATUS, ChallengeStatus.getDefaultValue()));
	}

	public static ChallengeStatus getChallengeStatusFromUserID(Context ctx, String hiqUserID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		String challengeID = sharedPrefsChallenge.getString(hiqUserID, "");
		return getChallengeStatusFromID(ctx, challengeID);
	}

	public static CustomContactData.ChallengeState getChallengeStateFromID(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return CustomContactData.ChallengeState.valueOf(sharedPrefsChallenge.getInt(challengeID + STATE,
				CustomContactData.ChallengeState.getDefaultValue()));
	}

	public static CustomContactData.ChallengeState getChallengeStateFromUserID(Context ctx, String hiqUserID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		String challengeID = sharedPrefsChallenge.getString(hiqUserID, "");
		return getChallengeStateFromID(ctx, challengeID);
	}

	public static String getChallengeIDFromHiqUserID(Context ctx, String hiqUserID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getString(hiqUserID, "");
	}

	public static String getChallengeUserName(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getString(challengeID + HIQ_USER_NAME, ctx.getString(R.string.challenge_default_opponent));
	}

	public static int getChallengeBet(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getInt(challengeID + BET, 0);
	}

	public static int getChallengeDifficultyMin(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getInt(challengeID + DIFFMIN, 0);
	}

	public static int getChallengeDifficultyMax(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getInt(challengeID + DIFFMAX, 0);
	}

	public static int getChallengeQuestions(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getInt(challengeID + QUESTIONS, 1);
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

	@SuppressLint("NewApi")
	public static void storeLayoutParams(Activity act) {
		SharedPreferences sharedPrefsLayout = act.getSharedPreferences(LAYOUT_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editPrefsLayout = sharedPrefsLayout.edit();
		Display display = act.getWindowManager().getDefaultDisplay();
		int sizeY, sizeX;
		if (android.os.Build.VERSION.SDK_INT < 13) {
			sizeY = display.getHeight();
			sizeX = display.getWidth();
		} else {
			Point size = new Point();
			display.getSize(size);
			sizeY = size.y;
			sizeX = size.x;
		}
		editPrefsLayout.putInt(LAYOUT_WIDTH, sizeX).putInt(LAYOUT_HEIGHT, sizeY);
		View content = act.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
		int w = content.getWidth();
		int h = content.getHeight();
		if (w > 0 && h > 0) {
			int statusBarHeight = sizeY - h;
			Loggy.d("content width = " + w + " sizeY = " + sizeY + " | content.getHeight() = " + h + " | statusBarHeight = "
					+ statusBarHeight);
			editPrefsLayout.putInt(LAYOUT_STATUS, statusBarHeight);
		} else {

		}
		editPrefsLayout.commit();
	}

	public static void storeLayoutParams(Context ctx, int width, int height, int statusBarHeight) {
		SharedPreferences sharedPrefsLayout = ctx.getSharedPreferences(LAYOUT_PREFS, Context.MODE_PRIVATE);
		sharedPrefsLayout.edit().putInt(LAYOUT_WIDTH, width).putInt(LAYOUT_HEIGHT, height).putInt(LAYOUT_STATUS, statusBarHeight).commit();
	}

	public static int getLayoutWidth(Context ctx, int defaultValue) {
		SharedPreferences sharedPrefsLayout = ctx.getSharedPreferences(LAYOUT_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsLayout.getInt(LAYOUT_WIDTH, defaultValue);
	}

	public static int getLayoutHeight(Context ctx, int defaultValue) {
		SharedPreferences sharedPrefsLayout = ctx.getSharedPreferences(LAYOUT_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsLayout.getInt(LAYOUT_HEIGHT, defaultValue);
	}

	public static int getLayoutStatusBarHeight(Context ctx, int defaultValue) {
		SharedPreferences sharedPrefsLayout = ctx.getSharedPreferences(LAYOUT_PREFS, Context.MODE_PRIVATE);
		int sh = sharedPrefsLayout.getInt(LAYOUT_STATUS, defaultValue);
		if (sh < 0) {
			sh = (int) Math.ceil(25 * ctx.getResources().getDisplayMetrics().density);
		}
		return sh;
	}
}
