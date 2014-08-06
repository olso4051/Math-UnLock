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
import com.olyware.mathlock.model.GenericQuestion;
import com.olyware.mathlock.service.CustomContactData;
import com.olyware.mathlock.views.JoystickSelect;

public class PreferenceHelper {
	final public static String MONEY_PREFS = "Packages";
	final public static String CHALLENGE_PREFS = "Challenge";
	final public static String USER_PREFS = "user_info";
	final public static String LAYOUT_PREFS = "Layout_Size";
	final public static String TUTORIAL_PREFS = "Tutorial";

	final public static String TUTORIAL_QUESTION = "tutrial_question";
	final public static String TUTORIAL_EXTENDED = "tutrial_extended";

	final public static String LAYOUT_WIDTH = "layout_width";
	final public static String LAYOUT_HEIGHT = "layout_height";
	final public static String LAYOUT_STATUSBAR_HEIGHT = "layout_status_bar_height";

	final public static String CHALLENGE_PREFS_STATUS = "status";
	final public static String CHALLENGE_PREFS_STATE = "state";
	final public static String CHALLENGE_PREFS_HIQ_USER_NAME = "hiq_user_name";
	final public static String CHALLENGE_PREFS_HIQ_USER_ID = "hiq_user_id";
	final public static String CHALLENGE_PREFS_BET = "bet";
	final public static String CHALLENGE_PREFS_DIFFMIN = "diff_min";
	final public static String CHALLENGE_PREFS_DIFFMAX = "diff_max";
	final public static String CHALLENGE_PREFS_QUESTIONS = "questions";
	final public static String CHALLENGE_PREFS_PROVISIONED = "provisioned";
	final public static String CHALLENGE_PREFS_COMPLETE_STATUS = "complete_status";

	final public static String CHALLENGE_PREFS_BET_DEFAULT = "default_bet_percent";
	final public static String CHALLENGE_PREFS_QUESTION_NUMBER = "num_questions";
	final public static String CHALLENGE_PREFS_DIFFICULTY_MIN = "difficulty_min";
	final public static String CHALLENGE_PREFS_DIFFICULTY_MAX = "difficulty_max";

	final public static String USER_PREFS_FRIENDS_ASKED = "asked_for_friends";

	final public static String DEFAULT_PREFS_SHARE_HASH = "share_hash_latest";

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

	public static enum ChallengeCompleteStatus {
		NotSent(0), Sending(1), Sent(2);
		private int value;

		private ChallengeCompleteStatus(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static int getDefaultValue() {
			return 0;
		}

		public static ChallengeCompleteStatus valueOf(int value) {
			switch (value) {
			case 0:
				return NotSent;
			case 1:
				return Sending;
			case 2:
				return Sent;
			default:
				return NotSent;
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
			if (sharedPrefsChallenge.getBoolean(questionPack, true))
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
		diff.putFloat(CHALLENGE_PREFS_BET_DEFAULT, sharedPrefsChallengeEdit.getFloat(CHALLENGE_PREFS_BET_DEFAULT, .5f));
		diff.putInt(CHALLENGE_PREFS_QUESTION_NUMBER, sharedPrefsChallengeEdit.getInt(CHALLENGE_PREFS_QUESTION_NUMBER, 1));
		diff.putInt(CHALLENGE_PREFS_DIFFICULTY_MIN, sharedPrefsChallengeEdit.getInt(CHALLENGE_PREFS_DIFFICULTY_MIN, 0));
		diff.putInt(CHALLENGE_PREFS_DIFFICULTY_MAX, sharedPrefsChallengeEdit.getInt(CHALLENGE_PREFS_DIFFICULTY_MAX, 1));
		return diff;
	}

	public static void storeChallengeSettings(Context ctx, float percent, int numQuestions, int difficultyMin, int difficultyMax) {
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putFloat(CHALLENGE_PREFS_BET_DEFAULT, percent);
		sharedPrefsChallengeEdit.putInt(CHALLENGE_PREFS_QUESTION_NUMBER, numQuestions);
		sharedPrefsChallengeEdit.putInt(CHALLENGE_PREFS_DIFFICULTY_MIN, difficultyMin);
		sharedPrefsChallengeEdit.putInt(CHALLENGE_PREFS_DIFFICULTY_MAX, difficultyMax);
		sharedPrefsChallengeEdit.commit();
	}

	public static void storeChallengeDifficulty(Context ctx, int difficultyMin, int difficultyMax) {
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putInt(CHALLENGE_PREFS_DIFFICULTY_MIN, difficultyMin);
		sharedPrefsChallengeEdit.putInt(CHALLENGE_PREFS_DIFFICULTY_MAX, difficultyMax);
		sharedPrefsChallengeEdit.commit();
	}

	public static void storeChallengeQuestions(Context ctx, int numQuestions) {
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putInt(CHALLENGE_PREFS_QUESTION_NUMBER, numQuestions);
		sharedPrefsChallengeEdit.commit();
	}

	public static void storeChallengeBetPercent(Context ctx, float percent) {
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putFloat(CHALLENGE_PREFS_BET_DEFAULT, percent);
		sharedPrefsChallengeEdit.commit();
	}

	public static void updateHiqUserID(Context ctx, String oldHiqUserID, String newHiqUserID) {
		if (!oldHiqUserID.equals("")) {
			SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
			String challengeID = getChallengeIDFromHiqUserID(ctx, oldHiqUserID);
			SharedPreferences.Editor editorPrefsChallenge = sharedPrefsChallenge.edit();
			editorPrefsChallenge.remove(oldHiqUserID);
			editorPrefsChallenge.putString(newHiqUserID, challengeID);
			editorPrefsChallenge.commit();
		}
	}

	public static void removeChallengeID(Context ctx, String challengeID, String hiqUserID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		String challengeIDStored = getChallengeIDFromHiqUserID(ctx, hiqUserID);
		if (challengeIDStored.equals(challengeID)) {
			SharedPreferences.Editor editorPrefsChallenge = sharedPrefsChallenge.edit();
			editorPrefsChallenge.remove(challengeID + CHALLENGE_PREFS_STATUS);
			editorPrefsChallenge.remove(challengeID + CHALLENGE_PREFS_STATE);
			editorPrefsChallenge.remove(challengeID + CHALLENGE_PREFS_HIQ_USER_NAME);
			editorPrefsChallenge.remove(challengeID + CHALLENGE_PREFS_BET);
			editorPrefsChallenge.remove(challengeID + CHALLENGE_PREFS_DIFFMIN);
			editorPrefsChallenge.remove(challengeID + CHALLENGE_PREFS_DIFFMAX);
			editorPrefsChallenge.remove(challengeID + CHALLENGE_PREFS_QUESTIONS);
			editorPrefsChallenge.remove(challengeID + CHALLENGE_PREFS_PROVISIONED);
			editorPrefsChallenge.remove(challengeID + CHALLENGE_PREFS_COMPLETE_STATUS);
			editorPrefsChallenge.remove(hiqUserID);
			editorPrefsChallenge.commit();
		}
	}

	public static void storeChallengeStatus(Context ctx, String challengeID, ChallengeStatus status, CustomContactData.ChallengeState state) {
		SharedPreferences.Editor editorPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		editorPrefsChallenge.putInt(challengeID + CHALLENGE_PREFS_STATUS, status.getValue());
		editorPrefsChallenge.putInt(challengeID + CHALLENGE_PREFS_STATE, state.getValue());
		editorPrefsChallenge.commit();
	}

	public static void storeChallengeStatus(Context ctx, String challengeID, ChallengeStatus status,
			CustomContactData.ChallengeState state, String userName, String hiqUserID, int bet, int diffMin, int diffMax, int questions) {
		SharedPreferences.Editor editPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsChallenge.putInt(challengeID + CHALLENGE_PREFS_STATUS, status.getValue());
		editPrefsChallenge.putInt(challengeID + CHALLENGE_PREFS_STATE, state.getValue());
		editPrefsChallenge.putString(challengeID + CHALLENGE_PREFS_HIQ_USER_NAME, userName);
		editPrefsChallenge.putInt(challengeID + CHALLENGE_PREFS_BET, bet);
		editPrefsChallenge.putInt(challengeID + CHALLENGE_PREFS_DIFFMIN, diffMin);
		editPrefsChallenge.putInt(challengeID + CHALLENGE_PREFS_DIFFMAX, diffMax);
		editPrefsChallenge.putInt(challengeID + CHALLENGE_PREFS_QUESTIONS, questions);
		editPrefsChallenge.putString(hiqUserID, challengeID);
		editPrefsChallenge.commit();
	}

	public static void storeChallengeCompleteStatus(Context ctx, String challengeID, ChallengeCompleteStatus cStatus) {
		if (challengeID == null)
			return;
		if (challengeID.equals(""))
			return;
		SharedPreferences.Editor editPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsChallenge.putInt(challengeID + CHALLENGE_PREFS_COMPLETE_STATUS, cStatus.getValue());
		editPrefsChallenge.commit();
	}

	public static ChallengeCompleteStatus getChallengeCompleteStatus(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return ChallengeCompleteStatus.valueOf(sharedPrefsChallenge.getInt(challengeID + CHALLENGE_PREFS_COMPLETE_STATUS,
				ChallengeCompleteStatus.getDefaultValue()));
	}

	public static void provisionChallengeID(Context ctx, String challengeID) {
		if (challengeID == null)
			return;
		if (challengeID.equals(""))
			return;
		SharedPreferences.Editor editPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsChallenge.putBoolean(challengeID + CHALLENGE_PREFS_PROVISIONED, true);
		editPrefsChallenge.commit();
	}

	public static boolean isChallengeProvisioned(Context ctx, String challengeID) {
		if (challengeID == null)
			return false;
		if (challengeID.equals(""))
			return false;
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getBoolean(challengeID + CHALLENGE_PREFS_PROVISIONED, false);
	}

	public static ChallengeStatus getChallengeStatusFromID(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return ChallengeStatus
				.valueOf(sharedPrefsChallenge.getInt(challengeID + CHALLENGE_PREFS_STATUS, ChallengeStatus.getDefaultValue()));
	}

	public static ChallengeStatus getChallengeStatusFromUserID(Context ctx, String hiqUserID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		String challengeID = sharedPrefsChallenge.getString(hiqUserID, "");
		return getChallengeStatusFromID(ctx, challengeID);
	}

	public static CustomContactData.ChallengeState getChallengeStateFromID(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		CustomContactData.ChallengeState state = CustomContactData.ChallengeState.valueOf(sharedPrefsChallenge.getInt(challengeID
				+ CHALLENGE_PREFS_STATE, CustomContactData.ChallengeState.getDefaultValue()));
		return state;
	}

	public static CustomContactData.ChallengeState getChallengeStateFromUserID(Context ctx, String hiqUserID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		String challengeID = sharedPrefsChallenge.getString(hiqUserID, "");
		CustomContactData.ChallengeState state = getChallengeStateFromID(ctx, challengeID);
		return state;
	}

	public static String getChallengeIDFromHiqUserID(Context ctx, String hiqUserID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		if (!hiqUserID.equals(""))
			return sharedPrefsChallenge.getString(hiqUserID, "");
		else
			return "";
	}

	public static String getChallengeUserName(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getString(challengeID + CHALLENGE_PREFS_HIQ_USER_NAME,
				ctx.getString(R.string.challenge_default_opponent));
	}

	public static int getChallengeBet(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getInt(challengeID + CHALLENGE_PREFS_BET, 0);
	}

	public static int getChallengeDifficultyMin(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getInt(challengeID + CHALLENGE_PREFS_DIFFMIN, 0);
	}

	public static int getChallengeDifficultyMax(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getInt(challengeID + CHALLENGE_PREFS_DIFFMAX, 0);
	}

	public static int getChallengeQuestions(Context ctx, String challengeID) {
		SharedPreferences sharedPrefsChallenge = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallenge.getInt(challengeID + CHALLENGE_PREFS_QUESTIONS, 1);
	}

	public static void increaseMoney(Context ctx, int amount) {
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsMoney.putInt("money", sharedPrefsMoney.getInt("money", 0) + amount);
		editorPrefsMoney.commit();
		SharedPreferences sharedPrefsMoney2 = ctx.getSharedPreferences(MONEY_PREFS, Context.MODE_PRIVATE);
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
		SharedPreferences.Editor editorPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit();
		editorPrefsUsers.putString(ctx.getString(R.string.pref_user_facebook_id), faceID)
				.putString(ctx.getString(R.string.pref_user_facebook_name), faceName)
				.putString(ctx.getString(R.string.pref_user_facebook_gender), gender)
				.putString(ctx.getString(R.string.pref_user_facebook_email), email).commit();
	}

	public static boolean shouldDoMeRequest(Context ctx) {
		SharedPreferences sharedPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		if (sharedPrefsUsers.getString(ctx.getString(R.string.pref_user_facebook_id), "").equals(""))
			return true;
		else
			return false;
	}

	public static void storeLatestShareHash(Context ctx, String hash) {
		SharedPreferences sharedPrefsDefault = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editorPrefsDefault = sharedPrefsDefault.edit();
		editorPrefsDefault.putString(DEFAULT_PREFS_SHARE_HASH, hash).commit();
	}

	public static String getLatestShareHash(Context ctx) {
		SharedPreferences sharedPrefsDefault = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sharedPrefsDefault.getString(DEFAULT_PREFS_SHARE_HASH, "");
	}

	public static void logout(Context ctx) {
		SharedPreferences sharedPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		sharedPrefsUsers.edit().putBoolean(ctx.getString(R.string.pref_user_skipped), false)
				.putBoolean(ctx.getString(R.string.pref_user_logged_in), false).putBoolean(USER_PREFS_FRIENDS_ASKED, false).commit();
		ContactHelper.removeStoredContacts(ctx);
	}

	@SuppressLint("NewApi")
	public static void storeLayoutParams(Activity act) {
		SharedPreferences sharedPrefsLayout = act.getSharedPreferences(LAYOUT_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editPrefsLayout = sharedPrefsLayout.edit();
		Display display = act.getWindowManager().getDefaultDisplay();
		int sizeY, sizeX;
		int y = 0;
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
		if (android.os.Build.VERSION.SDK_INT >= 11)
			y = (int) content.getY();
		if (w > 0 && h > 0) {
			int statusBarHeight = sizeY - h;
			if (y >= 0 && y != statusBarHeight)
				statusBarHeight = y;
			editPrefsLayout.putInt(LAYOUT_STATUSBAR_HEIGHT, statusBarHeight);
		} else {

		}
		editPrefsLayout.commit();
	}

	public static void storeLayoutParams(Context ctx, int width, int height, int statusBarHeight) {
		SharedPreferences sharedPrefsLayout = ctx.getSharedPreferences(LAYOUT_PREFS, Context.MODE_PRIVATE);
		sharedPrefsLayout.edit().putInt(LAYOUT_WIDTH, width).putInt(LAYOUT_HEIGHT, height).putInt(LAYOUT_STATUSBAR_HEIGHT, statusBarHeight)
				.commit();
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
		int sh = sharedPrefsLayout.getInt(LAYOUT_STATUSBAR_HEIGHT, defaultValue);
		if (sh < 0) {
			sh = (int) Math.ceil(25 * ctx.getResources().getDisplayMetrics().density);
		}
		return sh;
	}

	public static void setTutorialQuestion(Context ctx, int question) {
		SharedPreferences.Editor editPrefsTutorial = ctx.getSharedPreferences(TUTORIAL_PREFS, Context.MODE_PRIVATE).edit();
		if (question < 0) {
			question = 0;
		}
		editPrefsTutorial.putInt(TUTORIAL_QUESTION, question).commit();
	}

	public static void setTutorialDone(Context ctx) {
		SharedPreferences.Editor editPrefsTutorial = ctx.getSharedPreferences(TUTORIAL_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsTutorial.putInt(TUTORIAL_QUESTION, -1).commit();
	}

	public static int getTutorialQuestionNumber(Context ctx) {
		SharedPreferences sharedPrefsTutorial = ctx.getSharedPreferences(TUTORIAL_PREFS, Context.MODE_PRIVATE);
		int question = sharedPrefsTutorial.getInt(TUTORIAL_QUESTION, 0);
		boolean extendedTutorial = sharedPrefsTutorial.getBoolean(TUTORIAL_EXTENDED, false);
		if (question >= 0 && question <= 4)
			return question;
		else if (extendedTutorial)
			return question;
		else
			return -1;
	}

	public static void resetTutorial(Context ctx) {
		SharedPreferences.Editor editPrefsTutorial = ctx.getSharedPreferences(TUTORIAL_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsTutorial.putInt(TUTORIAL_QUESTION, 0);
		editPrefsTutorial.putBoolean(TUTORIAL_EXTENDED, true);
		editPrefsTutorial.commit();
	}

	public static GenericQuestion getTutorialQuestion(Context ctx, int question) {
		String[] questions = ctx.getResources().getStringArray(R.array.tutorial_questions);
		if (question >= 0 && question < questions.length) {
			String[] titleQuestions = ctx.getResources().getStringArray(R.array.tutorial_title_questions);
			String[] subQuestions = ctx.getResources().getStringArray(R.array.tutorial_sub_questions);
			int answerID = ctx.getResources().getIdentifier("tutorial_answers_" + (question), "array", ctx.getPackageName());
			String[] answers = ctx.getResources().getStringArray(answerID);
			/*String titleQuestion = "<small><font color='"
					+ String.format("#%06X", (0xFFFFFF & ctx.getResources().getColor(R.color.grey_on_dark))) + "'>"
					+ titleQuestions[question] + "</font></small>";*/
			/*String subQuestion = "<small><font color='"
					+ String.format("#%06X", (0xFFFFFF & ctx.getResources().getColor(R.color.grey_on_dark))) + "'>"
					+ subQuestions[question] + "</font></small>";*/
			// String questionText = titleQuestion + "<br/>" + questions[question] + "<br/>" + subQuestions[question];
			String questionText = questions[question] + "<br/>" + subQuestions[question];
			// return new GenericQuestion("Tutorial", questionText, answers);
			return new GenericQuestion(titleQuestions[question], questionText, answers);
		} else {
			return null;
		}
	}

	public static boolean setLockscreenFrequency(Context ctx, JoystickSelect s) {
		SharedPreferences sharedPrefsTutorial = ctx.getSharedPreferences(TUTORIAL_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
		if (s == JoystickSelect.A) {
			editPrefs.putString("lockscreen2", "0");
		} else if (s == JoystickSelect.B) {
			editPrefs.putString("lockscreen2", "3");
		} else if (s == JoystickSelect.C) {
			editPrefs.putString("lockscreen2", "5");
		} else if (s == JoystickSelect.D) {
			editPrefs.putString("lockscreen2", "7");
		}
		editPrefs.commit();

		return sharedPrefsTutorial.getBoolean(TUTORIAL_EXTENDED, false);
	}

	public static boolean getAskedForFriendsPermission(Context ctx) {
		SharedPreferences sharedPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsUser.getBoolean(USER_PREFS_FRIENDS_ASKED, false);
	}

	public static void setAskedForFriends(Context ctx, boolean asked) {
		SharedPreferences.Editor editPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsUser.putBoolean(USER_PREFS_FRIENDS_ASKED, asked).commit();
	}
}
