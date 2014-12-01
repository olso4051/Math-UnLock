package com.olyware.mathlock.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.Window;

import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.QuestionSelectData;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.database.contracts.CustomQuestionContract;
import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.GenericQuestion;
import com.olyware.mathlock.service.CustomContactData;
import com.olyware.mathlock.service.GetSponsoredAppQuestion;
import com.olyware.mathlock.service.GetSponsoredQuestions;
import com.olyware.mathlock.service.PackageData;
import com.olyware.mathlock.service.PutSponsoredAnswers;
import com.olyware.mathlock.views.JoystickSelect;

public class PreferenceHelper {
	final public static String MONEY_PREFS = "Packages";
	final public static String CHALLENGE_PREFS = "Challenge";
	final public static String USER_PREFS = "user_info";
	final public static String LAYOUT_PREFS = "Layout_Size";
	final public static String TUTORIAL_PREFS = "Tutorial";
	final public static String SWISHER_PREFS = "Swisher";
	final public static String CUSTOM_PACKS_PREFS = "Custom_Packs";

	final public static String CUSTOM_PACKS_LOADED = "loaded_packs";
	final public static String CUSTOM_PACKS_VERSION = "packs_version";

	final public static String MONEY_PREFS_PACKS = "packs_to_open";

	final public static String SWISHER_ON = "swisher_enabled";
	final public static String CUSTOM_FILENAME = "fileName";
	final public static String SWISHER_JSON = "swisher_json";
	final public static String SWISHER_COUNT = "swisher_count";
	final public static String SWISHER_TOTAL = "swisher_total";
	final public static String SWISHER_FILENAME_WITH_EXTENSION = "Kara Swisher Trivia.csv"; // chnaged for testing pai
	final public static String SWISHER_FILENAME = "Kara Swisher Trivia";
	final public static String ENTRE_FILENAME = "Entrepreneur Pack";
	final public static String ENTRE_FILENAME_WITH_EXTENSION = "Entrepreneur pack.csv";

	final public static int SWISHER_MAX_COUNT = 3;

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
	final public static String USER_PREFS_LAST_SPONSORED = "last_sponsored_request_time";
	final public static String USER_PREFS_LAST_SPONSORED_APP = "last_sponsored_app_request_time";
	final public static String USER_PREFS_SPONSORED_QUIZ_MODE_ANSWERED = "quiz_mode_questions_answered";
	final public static String USER_PREFS_SPONSORED_HASH = "hash";
	final public static String USER_PREFS_SPONSORED_QUESTIONS = "questions";
	final public static String USER_PREFS_SPONSORED_SPONSOR = "sponsor";
	final public static String USER_PREFS_SPONSORED_DESCRIPTION = "description";
	final public static String USER_PREFS_SPONSORED_QUESTION_HASH = "question_hash";
	final public static String USER_PREFS_SPONSORED_QUESTION = "question";
	final public static String USER_PREFS_SPONSORED_ANSWERS = "answers";
	final public static String USER_PREFS_SPONSORED_ANSWERS_STORED = "answers_stored";
	final public static String USER_PREFS_SPONSORED_URLS = "urls";
	final public static String USER_PREFS_SPONSORED_STARTED = "started";

	final public static long SPONSORED_PACK_INTERVAL = 14400000l; // 4 hours
	final public static long SPONSORED_APP_INTERVAL = 40000l; // 40 seconds
	final public static int SPONSORED_APP_QUESTION_INTERVAL = 15;

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

	public static void setShareCompeteShown(Context ctx, boolean val) {
		SharedPreferences.Editor sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE).edit();
		sharedPrefsChallengeEdit.putBoolean("shown", val);
		sharedPrefsChallengeEdit.commit();
	}

	public static boolean isShareCompeteShown(Context ctx) {
		SharedPreferences sharedPrefsChallengeEdit = ctx.getSharedPreferences(CHALLENGE_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsChallengeEdit.getBoolean("shown", false);
	}

	public static void addPackToOpen(Context ctx, PackageData pd) {
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, Context.MODE_PRIVATE);
		try {
			JSONArray pds = new JSONArray(sharedPrefsMoney.getString(MONEY_PREFS_PACKS, "[]"));
			SharedPreferences.Editor editPrefsMoney = sharedPrefsMoney.edit();
			pds.put(pd.getJSON());
			editPrefsMoney.putString(MONEY_PREFS_PACKS, pds.toString()).commit();
			return;
		} catch (JSONException j) {
			return;
		}
	}

	public static boolean isSubscribedToQuizeMode(Context ctx) {
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsMoney.getBoolean("sub_quiz_mode", false);

	}

	public static void setSubscribedToQuizeMode(Context ctx, boolean value) {
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editPrefsMoney = sharedPrefsMoney.edit();
		editPrefsMoney.putBoolean("sub_quiz_mode", value);
		editPrefsMoney.commit();

	}

	public static String getPackToOpen(Context ctx) {
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, Context.MODE_PRIVATE);
		try {
			JSONArray pds = new JSONArray(sharedPrefsMoney.getString(MONEY_PREFS_PACKS, "[]"));
			if (pds.length() > 0) {
				JSONObject pd = pds.getJSONObject(0);
				return PackageData.getPackFromJSON(pd);
			} else {
				return "";
			}
		} catch (JSONException j) {
			return "";
		}
	}

	public static void removePackToOpen(Context ctx, String pack) {
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, Context.MODE_PRIVATE);
		try {
			JSONArray pds = new JSONArray(sharedPrefsMoney.getString(MONEY_PREFS_PACKS, "[]"));
			if (pds.length() > 0) {
				JSONArray pdsNew = new JSONArray();
				for (int i = 0; i < pds.length(); i++) {
					JSONObject pd = pds.getJSONObject(i);
					if (!pack.equals(PackageData.getPackFromJSON(pd)))
						pdsNew.put(pd);
				}
				SharedPreferences.Editor editPrefsMoney = sharedPrefsMoney.edit();
				editPrefsMoney.putString(MONEY_PREFS_PACKS, pdsNew.toString()).commit();
				return;
			} else {
				return;
			}
		} catch (JSONException j) {
			return;
		}
	}

	public static ArrayList<String> getDisplayableUnlockedPackages(Context ctx, DatabaseManager dbManager) {
		ArrayList<String> list = new ArrayList<String>();
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, Context.MODE_PRIVATE);
		String[] unlockPackageKeys = ctx.getResources().getStringArray(R.array.unlock_package_keys);
		String[] unlockSubPackageKeys = ctx.getResources().getStringArray(R.array.unlock_sub_package_keys);
		List<String> displayPackageKeys = EZ.list(ctx.getResources().getStringArray(R.array.display_packages));
		displayPackageKeys.addAll(dbManager.getAllCustomCategories());

		if (sharedPrefsMoney.getBoolean("unlock_all", false)) {
			list.addAll(displayPackageKeys);
		} else {
			// list.add(displayPackageKeys.get(0)); // All
			for (int i = 1; i < displayPackageKeys.size(); i++) {
				if (i < unlockPackageKeys.length) {
					if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false)
							|| sharedPrefsMoney.getBoolean(unlockSubPackageKeys[i], false)) {
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
		String[] unlockSubPackageKeys = ctx.getResources().getStringArray(R.array.unlock_sub_package_keys);
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
					if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false)
							|| sharedPrefsMoney.getBoolean(unlockSubPackageKeys[i], false)) {
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

	public static void unlockSubscription(Context ctx, int product) {
		String[] unlockSubscriptionPackageKeys = ctx.getResources().getStringArray(R.array.unlock_sub_package_keys);
		String[] PackageKeys; // ctx.getResources().getStringArray(R.array.enable_package_keys);
		ArrayList<String> pack = new ArrayList<String>(Arrays.asList(ctx.getResources().getStringArray(R.array.enable_package_keys)));
		pack.add(0, "unlock_all");
		PackageKeys = new String[pack.size()];
		pack.toArray(PackageKeys);
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, 0);
		boolean unlocked = sharedPrefsMoney.getBoolean(unlockSubscriptionPackageKeys[product], false);
		SharedPreferences.Editor editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsMoney.putBoolean(unlockSubscriptionPackageKeys[product], true);		// unlocks the product
		editorPrefsMoney.commit();

		// if the first time being unlocked
		if (!unlocked) {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			SharedPreferences.Editor editorPrefs = sharedPrefs.edit();
			editorPrefs.putBoolean(PackageKeys[product], true);
			editorPrefs.commit();
		}
	}

	public static void lockSubscription(Context ctx, int product) {
		String[] unlockPackageKeys = ctx.getResources().getStringArray(R.array.unlock_package_keys);
		String[] unlockSubscriptionPackageKeys = ctx.getResources().getStringArray(R.array.unlock_sub_package_keys);
		String[] PackageKeys; // ctx.getResources().getStringArray(R.array.enable_package_keys);
		ArrayList<String> pack = new ArrayList<String>(Arrays.asList(ctx.getResources().getStringArray(R.array.enable_package_keys)));
		pack.add(0, "unlock_all");
		PackageKeys = new String[pack.size()];
		pack.toArray(PackageKeys);
		SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(MONEY_PREFS, 0);
		SharedPreferences.Editor editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsMoney.putBoolean(unlockSubscriptionPackageKeys[product], false);		// locks the product
		editorPrefsMoney.commit();

		if (!sharedPrefsMoney.getBoolean(unlockPackageKeys[product], false)) {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			SharedPreferences.Editor editorPrefs = sharedPrefs.edit();
			editorPrefs.putBoolean(PackageKeys[product], false);
			if (product == 6) {
				String[] customPacks = ctx.getResources().getStringArray(R.array.enable_custom_packs);
				for (String customPack : customPacks) {
					editorPrefs.putBoolean(customPack, false);
				}
			}
			editorPrefs.commit();
		}
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

	public static String getSponsoredQuestionSponsor(Context ctx) {
		SharedPreferences sharedPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		String sponsor = sharedPrefsUsers.getString(USER_PREFS_SPONSORED_SPONSOR, "");
		if (!sponsor.equals(""))
			return ctx.getString(R.string.sponsored_by) + sponsor;
		else
			return ctx.getString(R.string.sponsored);
	}

	public static boolean shouldGetSponsoredQuestion(Context ctx, long currentTime) {
		/*if (!MoneyHelper.hasBoughtAnything(ctx)) {
			int paidMoney = MoneyHelper.getPaidMoney(ctx);
			if (paidMoney == 0) {*/
		long lastTime = getLastSponsoredRequestTime(ctx);
		if (lastTime <= currentTime - SPONSORED_PACK_INTERVAL) {
			return (getSponsoredQuestionsCount(ctx) == 0);
		} else
			return false;
		/*} else {
			return false;
		}
		} else {
		return false;
		}*/
	}

	public static boolean shouldGetSponsoredApp(Context ctx, long currentTime) {
		/*if (!MoneyHelper.hasBoughtAnything(ctx)) {
		int paidMoney = MoneyHelper.getPaidMoney(ctx);
		if (paidMoney == 0) {*/
		long lastTime = getLastSponsoredAppRequestTime(ctx);
		int questions = getTotalQuizModeQuestions(ctx);
		if (lastTime <= currentTime - SPONSORED_APP_INTERVAL && questions > SPONSORED_APP_QUESTION_INTERVAL) {
			return (getSponsoredQuestionsCount(ctx) == 0);
		} else
			return false;
		/*} else {
		return false;
		}
		} else {
		return false;
		}*/
	}

	public static long getLastSponsoredRequestTime(Context ctx) {
		SharedPreferences sharedPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		long lastTime = sharedPrefsUsers.getLong(USER_PREFS_LAST_SPONSORED, 0);
		if (lastTime == 0) {
			lastTime = System.currentTimeMillis();
			setLastSponsoredRequestTime(ctx, lastTime);
			return lastTime;
		} else {
			return sharedPrefsUsers.getLong(USER_PREFS_LAST_SPONSORED, 0);
		}
	}

	public static long getLastSponsoredAppRequestTime(Context ctx) {
		SharedPreferences sharedPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		long lastTime = sharedPrefsUsers.getLong(USER_PREFS_LAST_SPONSORED_APP, 0);
		if (lastTime == 0) {
			lastTime = System.currentTimeMillis();
			setLastSponsoredAppRequestTime(ctx, lastTime);
			return lastTime;
		} else {
			return sharedPrefsUsers.getLong(USER_PREFS_LAST_SPONSORED_APP, 0);
		}
	}

	public static int getTotalQuizModeQuestions(Context ctx) {
		SharedPreferences sharedPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsUsers.getInt(USER_PREFS_SPONSORED_QUIZ_MODE_ANSWERED, 0);
	}

	public static void setLastSponsoredRequestTime(Context ctx, long time) {
		SharedPreferences.Editor editPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsUsers.putLong(USER_PREFS_LAST_SPONSORED, time).commit();
	}

	public static void setLastSponsoredAppRequestTime(Context ctx, long time) {
		SharedPreferences.Editor editPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsUsers.putLong(USER_PREFS_LAST_SPONSORED_APP, time).commit();
	}

	public static void incrementQuizModeQuestionsAnswered(Context ctx) {
		SharedPreferences sharedPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editPrefsUsers = sharedPrefsUsers.edit();
		editPrefsUsers.putInt(USER_PREFS_SPONSORED_QUIZ_MODE_ANSWERED,
				sharedPrefsUsers.getInt(USER_PREFS_SPONSORED_QUIZ_MODE_ANSWERED, 0) + 1).commit();
	}

	public static void resetQuizModeQuestionsAnswered(Context ctx) {
		SharedPreferences.Editor editPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsUsers.putInt(USER_PREFS_SPONSORED_QUIZ_MODE_ANSWERED, 0).commit();
	}

	public static void getSponsoredQuestion(final Context ctx) {
		resetQuizModeQuestionsAnswered(ctx);
		SharedPreferences sharedPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		String userID = sharedPrefsUsers.getString(ctx.getString(R.string.pref_user_userid), "");
		final PackageManager pm = ctx.getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		List<String> packs = new ArrayList<String>(packages.size());
		for (ApplicationInfo packageInfo : packages) {
			packs.add(packageInfo.packageName);
		}
		new GetSponsoredAppQuestion(ctx, userID, packs) {
			@Override
			protected void onPostExecute(Integer result) {
				if (result == 0) {
					storeSponsoredQuestions(ctx, getPackHash(), getSponsor(), getDescription(), getQuestionHashes(), getQuestions(),
							getAnswers(), getURLs());
					setSponsoredQuestionsStarted(ctx);
				}
			}
		}.execute();
		/*String hash = "1234";
		String sponsor = "";
		String desc = "";
		List<String> questionHashes = new ArrayList<String>();
		questionHashes.add("abcd");
		List<String> questions = new ArrayList<String>();
		questions.add("Would you like to try one of these Apps?");
		List<String[]> answers = new ArrayList<String[]>();
		answers.add(new String[] { "Game Of War", "Orbitz", "CBS", "No Thanks" });
		List<String[]> urls = new ArrayList<String[]>();
		urls.add(new String[] { "http://api.chirpads.com/tracking/impressionclick/3b645652-0a84-4cd3-8e38-61a19c8d3d3d/",
				"http://api.chirpads.com/tracking/impressionclick/53ade82f-cd94-42b4-a002-a7f519b47f35/",
				"http://api.chirpads.com/tracking/impressionclick/055290ee-5ed9-4690-9ae9-f2f107ba7a84/", "" });
		storeSponsoredQuestions(ctx, hash, sponsor, desc, questionHashes, questions, answers, urls);
		setSponsoredQuestionsStarted(ctx);*/
	}

	public static void getSponsoredQuestions(final Context ctx) {
		SharedPreferences sharedPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		String userID = sharedPrefsUsers.getString(ctx.getString(R.string.pref_user_userid), "");
		new GetSponsoredQuestions(ctx, userID) {
			@Override
			protected void onPostExecute(Integer result) {
				if (result == 0) {
					storeSponsoredQuestions(ctx, getPackHash(), getSponsor(), getDescription(), getQuestionHashes(), getQuestions(),
							getAnswers(), getURLs());
				}
			}
		}.execute();
		/*String hash = "";
		String sponsor = "";
		String desc = "";
		List<String> questionHashes = new ArrayList<String>();
		List<String> questions = new ArrayList<String>();
		List<String[]> answers = new ArrayList<String[]>();
		List<String[]> urls = new ArrayList<String[]>();
		storeSponsoredQuestions(ctx, hash, sponsor, desc, questionHashes, questions, answers, urls);*/
	}

	public static void storeSponsoredQuestions(Context ctx, String hash, String sponsor, String description, List<String> questionHashes,
			List<String> questions, List<String[]> answers, List<String[]> urls) {
		SharedPreferences.Editor editorPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit();
		String questionsJSON = "[";
		boolean first = true;
		int qHS = questionHashes.size();
		int qS = questions.size();
		int aS = answers.size();
		int uS = urls.size();
		if (qHS == qS && qS == aS && aS == uS && uS == qHS) {
			for (int i = 0; i < qS; i++) {
				if (first) {
					questionsJSON += getJsonFromQuestion(questionHashes.get(i), questions.get(i), answers.get(i), urls.get(i));
					first = false;
				} else
					questionsJSON += "," + getJsonFromQuestion(questionHashes.get(i), questions.get(i), answers.get(i), urls.get(i));
			}
		}
		questionsJSON += "]";
		Loggy.d("sponsored questions JSON = " + questionsJSON);
		editorPrefsUser.putString(USER_PREFS_SPONSORED_HASH, hash);
		editorPrefsUser.putString(USER_PREFS_SPONSORED_SPONSOR, sponsor);
		editorPrefsUser.putString(USER_PREFS_SPONSORED_DESCRIPTION, description);
		editorPrefsUser.putString(USER_PREFS_SPONSORED_QUESTIONS, questionsJSON).commit();
	}

	private static String getJsonFromQuestion(String questionHash, String question, String[] answers, String[] urls) {
		String json = "{";
		if (answers.length == 4 && urls.length == 4) {
			json += "\"" + USER_PREFS_SPONSORED_QUESTION_HASH + "\":\"" + JSONHelper.encodeJSON(questionHash) + "\",";
			json += "\"" + USER_PREFS_SPONSORED_QUESTION + "\":\"" + JSONHelper.encodeJSON(question) + "\",";
			json += "\"" + USER_PREFS_SPONSORED_ANSWERS + "\":[";
			boolean first = true;
			json += "\"";
			for (int i = 0; i < answers.length; i++) {
				if (first) {
					json += JSONHelper.encodeJSON(answers[i]) + "\"";
					first = false;
				} else
					json += ",\"" + JSONHelper.encodeJSON(answers[i]) + "\"";
			}
			json += "],\"" + USER_PREFS_SPONSORED_URLS + "\":[";
			first = true;
			json += "\"";
			for (int i = 0; i < urls.length; i++) {
				if (first) {
					json += JSONHelper.encodeJSON(urls[i]) + "\"";
					first = false;
				} else
					json += ",\"" + JSONHelper.encodeJSON(urls[i]) + "\"";
			}
			json += "]";
		}
		json += "}";
		return json;
	}

	public static List<GenericQuestion> getStoredSponsoredQuestions(Context ctx) {
		SharedPreferences sharedPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		String questionsJSON = sharedPrefsUser.getString(USER_PREFS_SPONSORED_QUESTIONS, "");
		if (!questionsJSON.equals("")) {
			try {
				List<GenericQuestion> questions = new ArrayList<GenericQuestion>();
				JSONArray questionsJSONArray = new JSONArray(questionsJSON);
				for (int i = 0; i < questionsJSONArray.length(); i++) {
					JSONObject questionJSONObject = questionsJSONArray.getJSONObject(i);
					String hash = JSONHelper.decodeJSON(questionJSONObject.getString(USER_PREFS_SPONSORED_QUESTION_HASH));
					String question = JSONHelper.decodeJSON(questionJSONObject.getString(USER_PREFS_SPONSORED_QUESTION));
					JSONArray answers = questionJSONObject.getJSONArray(USER_PREFS_SPONSORED_ANSWERS);
					JSONArray urls = questionJSONObject.getJSONArray(USER_PREFS_SPONSORED_URLS);
					String[] answersArray = JSONHelper.getStringArrayFromJSONArray(answers);
					String[] urlsArray = JSONHelper.getStringArrayFromJSONArray(urls);
					questions.add(new GenericQuestion("", hash, question, answersArray, urlsArray));
				}
				return questions;
			} catch (JSONException e) {
				return new ArrayList<GenericQuestion>();
			}
		} else
			return new ArrayList<GenericQuestion>();
	}

	public static int getSponsoredQuestionsCount(Context ctx) {
		return getStoredSponsoredQuestions(ctx).size();
	}

	public static String getSponsoredQuestionsDescription(Context ctx) {
		SharedPreferences sharedPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsUser.getString(USER_PREFS_SPONSORED_DESCRIPTION, "");
	}

	public static boolean isSponsoredQuestionsStarted(Context ctx) {
		SharedPreferences sharedPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsUser.getBoolean(USER_PREFS_SPONSORED_STARTED, false);
	}

	public static boolean isSponsoredQuestionsStored(Context ctx) {
		SharedPreferences sharedPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		String questionsJSON = sharedPrefsUser.getString(USER_PREFS_SPONSORED_QUESTIONS, "");
		Loggy.d("questionsJSON = " + questionsJSON);
		return !questionsJSON.equals("");
	}

	public static void setSponsoredQuestionsStarted(Context ctx) {
		SharedPreferences.Editor editPrefsUsers = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsUsers.putBoolean(USER_PREFS_SPONSORED_STARTED, true).commit();
	}

	public static void removeSponsoredQuestion(Context ctx, String hash, int answer) {
		SharedPreferences sharedPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		String packHash = sharedPrefsUser.getString(USER_PREFS_SPONSORED_HASH, "");
		String sponsor = sharedPrefsUser.getString(USER_PREFS_SPONSORED_SPONSOR, "");
		List<GenericQuestion> questions = getStoredSponsoredQuestions(ctx);
		if (hash != null && questions.size() > 0 && !hash.equals("")) {
			Loggy.d("answer location = " + answer);
			storeSponsoredAnswers(ctx, hash, answer);
			questions.remove(0);
			if (questions.size() == 0) {
				String userID = sharedPrefsUser.getString(ctx.getString(R.string.pref_user_userid), "");
				new PutSponsoredAnswers(ctx, userID, packHash, getStoredSponsoredAnswers(ctx)).execute();
				resetSponsoredQuestions(ctx);
			} else {
				String description;
				List<String> questionHash = new ArrayList<String>();
				List<String> question = new ArrayList<String>();
				List<String[]> answers = new ArrayList<String[]>();
				List<String[]> urls = new ArrayList<String[]>();
				for (int i = 0; i < questions.size(); i++) {
					description = questions.get(i).getDescription();
					questionHash.add(questions.get(i).getHash());
					question.add(questions.get(i).getQuestion());
					answers.add(questions.get(i).getAnswers());
					urls.add(questions.get(i).getURLs());
					storeSponsoredQuestions(ctx, packHash, sponsor, description, questionHash, question, answers, urls);
				}
			}
		} else {
			resetSponsoredQuestions(ctx);
		}
	}

	public static void resetSponsoredQuestions(Context ctx) {
		SharedPreferences.Editor editPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsUser.putBoolean(USER_PREFS_SPONSORED_STARTED, false);
		editPrefsUser.putString(USER_PREFS_SPONSORED_HASH, "");
		editPrefsUser.putString(USER_PREFS_SPONSORED_SPONSOR, "");
		editPrefsUser.putString(USER_PREFS_SPONSORED_DESCRIPTION, "");
		editPrefsUser.putString(USER_PREFS_SPONSORED_QUESTIONS, "");
		editPrefsUser.putString(USER_PREFS_SPONSORED_ANSWERS_STORED, "");
		editPrefsUser.putLong(USER_PREFS_LAST_SPONSORED, System.currentTimeMillis()).commit();
	}

	public static String getStoredSponsoredAnswers(Context ctx) {
		SharedPreferences sharedPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsUser.getString(USER_PREFS_SPONSORED_ANSWERS_STORED, "");
	}

	public static void storeSponsoredAnswers(Context ctx, String hash, int answer) {
		try {
			String storedAnswers = getStoredSponsoredAnswers(ctx);
			JSONArray answers;
			JSONObject answerJSON = new JSONObject();
			answerJSON.put("question_hash", hash);
			answerJSON.put("answer", answer);
			if (!storedAnswers.equals("")) {
				answers = new JSONArray(storedAnswers);
			} else {
				answers = new JSONArray();
			}
			answers.put(answerJSON);
			SharedPreferences.Editor editorPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit();
			editorPrefsUser.putString(USER_PREFS_SPONSORED_ANSWERS_STORED, answers.toString()).commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
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

	public static String getUserID(Context ctx) {
		SharedPreferences sharedPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsUser.getString(ctx.getString(R.string.pref_user_userid), "");
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

		return true;// sharedPrefsTutorial.getBoolean(TUTORIAL_EXTENDED, false);
	}

	public static boolean getAskedForFriendsPermission(Context ctx) {
		SharedPreferences sharedPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsUser.getBoolean(USER_PREFS_FRIENDS_ASKED, false);
	}

	public static void setAskedForFriends(Context ctx, boolean asked) {
		SharedPreferences.Editor editPrefsUser = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsUser.putBoolean(USER_PREFS_FRIENDS_ASKED, asked).commit();
	}

	public static boolean isSwisherPackAdded(Context ctx, DatabaseManager dbManager) {
		SharedPreferences sharedPrefsSwisher = ctx.getSharedPreferences(SWISHER_PREFS, Context.MODE_PRIVATE);
		if (!sharedPrefsSwisher.getBoolean(SWISHER_ON, false))
			return false;
		else
			return dbManager.isSwisherPackAdded(getCustomFileName(ctx));
	}

	public static void addSwisherPack(Context ctx, DatabaseManager dbManager) {
		SharedPreferences sharedPrefsSwisher = ctx.getSharedPreferences(SWISHER_PREFS, Context.MODE_PRIVATE);
		if (!sharedPrefsSwisher.getBoolean(SWISHER_ON, false))
			return;
		List<String[]> questions = getSwisherQuestions(ctx);
		for (int i = 0; i < questions.size(); i++) {
			dbManager.addSwisherQuestion(questions.get(i));
		}
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editPrefs = sharedPrefs.edit();
		editPrefs.putBoolean(ctx.getString(R.string.custom_enable) + getCustomFileName(ctx), true).commit();
	}

	public static int getSwisherPackCount(Context ctx) {
		SharedPreferences sharedPrefsSwisher = ctx.getSharedPreferences(SWISHER_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsSwisher.getInt(SWISHER_COUNT, 0);
	}

	public static String getCustomFileName(Context ctx) {
		SharedPreferences sharedPrefsSwisher = ctx.getSharedPreferences(SWISHER_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsSwisher.getString(CUSTOM_FILENAME, "");
	}

	public static void setCustomFileName(Context ctx, String filename) {
		ctx.getSharedPreferences(SWISHER_PREFS, Context.MODE_PRIVATE).edit().putString(CUSTOM_FILENAME, filename).commit();
	}

	public static void incrementSwisherPackCount(Context ctx) {
		SharedPreferences sharedPrefsSwisher = ctx.getSharedPreferences(SWISHER_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editPrefsSwisher = sharedPrefsSwisher.edit();
		int count = sharedPrefsSwisher.getInt(SWISHER_COUNT, 0) + 1;
		if (count >= sharedPrefsSwisher.getInt(SWISHER_TOTAL, 0)) {
			turnSwisherPackOff(ctx);
			setCustomFileName(ctx, "");
		} else {
			editPrefsSwisher.putInt(SWISHER_COUNT, count).commit();
		}
	}

	public static boolean isSwisherPackDone(Context ctx) {
		SharedPreferences sharedPrefsSwisher = ctx.getSharedPreferences(SWISHER_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsSwisher.getBoolean(SWISHER_ON, false);
	}

	public static boolean isSwisherPackOn(Context ctx) {
		SharedPreferences sharedPrefsSwisher = ctx.getSharedPreferences(SWISHER_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsSwisher.getBoolean(SWISHER_ON, false);
	}

	public static void turnSwisherPackOff(Context ctx) {
		SharedPreferences.Editor editPrefsSwisher = ctx.getSharedPreferences(SWISHER_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsSwisher.putBoolean(SWISHER_ON, false).putInt(SWISHER_COUNT, -1).commit();
	}

	public static void turnSwisherPackOn(Context ctx, String filenamewithextension) {
		SharedPreferences.Editor editPrefsSwisher = ctx.getSharedPreferences(SWISHER_PREFS, Context.MODE_PRIVATE).edit();
		editPrefsSwisher.putBoolean(SWISHER_ON, true).putInt(SWISHER_COUNT, 0).commit();
		ArrayList<String[]> tempQuestions = new ArrayList<String[]>();
		try {
			InputStream is = ctx.getAssets().open(filenamewithextension);
			Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			CSVReader csvReader = new CSVReader(reader);
			String[] lineEntries = csvReader.readNext();
			if (lineEntries[0].equals(QuestionContract.QUESTION_TEXT) && lineEntries[1].equals(QuestionContract.ANSWER_CORRECT)
					&& lineEntries[2].equals(CustomQuestionContract.ANSWER_INCORRECT1)
					&& lineEntries[3].equals(CustomQuestionContract.ANSWER_INCORRECT2)
					&& lineEntries[4].equals(CustomQuestionContract.ANSWER_INCORRECT3)
					&& lineEntries[5].equals(QuestionContract.DIFFICULTY)) {
				lineEntries = csvReader.readNext();
				while (lineEntries != null) {
					if (lineEntries.length == 6) {
						if (Difficulty.isDifficulty(lineEntries[5])) {
							tempQuestions.add(new String[] { lineEntries[0], lineEntries[1], lineEntries[2], lineEntries[3],
									lineEntries[4], getCustomFileName(ctx), lineEntries[5] });
						}
					}
					lineEntries = csvReader.readNext();
				}
			}
			csvReader.close();
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String questionsJSON = "[";
		boolean first = true;
		for (String[] question : tempQuestions) {
			if (first) {
				questionsJSON += getJSONFromStringArray(question);
				first = false;
			} else
				questionsJSON += "," + getJSONFromStringArray(question);
		}
		questionsJSON += "]";
		int totalSwisherQuestions = tempQuestions.size();
		if (totalSwisherQuestions > SWISHER_MAX_COUNT)
			totalSwisherQuestions = SWISHER_MAX_COUNT;
		editPrefsSwisher.putString(SWISHER_JSON, questionsJSON).putInt(SWISHER_TOTAL, totalSwisherQuestions).commit();
		MoneyHelper.addPromoCoins(ctx, ctx.getString(R.string.coin_fountain_3000));
	}

	private static String getJSONFromStringArray(String[] strings) {
		String JSON = "[";
		boolean first = true;
		for (String s : strings) {
			if (first) {
				JSON += JSONHelper.encodeJSON(s);
				first = false;
			} else
				JSON += "," + JSONHelper.encodeJSON(s);
		}
		JSON += "]";
		return JSON;
	}

	public static List<String[]> getSwisherQuestions(Context ctx) {
		SharedPreferences sharedPrefsSwisher = ctx.getSharedPreferences(SWISHER_PREFS, Context.MODE_PRIVATE);
		String swisherJSON = sharedPrefsSwisher.getString(SWISHER_JSON, "");
		if (!swisherJSON.equals("")) {
			try {
				List<String[]> questions = new ArrayList<String[]>();
				JSONArray swisherJSONArray = new JSONArray(swisherJSON);
				for (int i = 0; i < swisherJSONArray.length(); i++) {
					JSONArray swisherQuestionJSONArray = swisherJSONArray.getJSONArray(i);
					String question = URLDecoder.decode(swisherQuestionJSONArray.getString(0), "utf-8");
					String answer1 = URLDecoder.decode(swisherQuestionJSONArray.getString(1), "utf-8");
					String answer2 = URLDecoder.decode(swisherQuestionJSONArray.getString(2), "utf-8");
					String answer3 = URLDecoder.decode(swisherQuestionJSONArray.getString(3), "utf-8");
					String answer4 = URLDecoder.decode(swisherQuestionJSONArray.getString(4), "utf-8");
					String filename = URLDecoder.decode(swisherQuestionJSONArray.getString(5), "utf-8");
					String diff = URLDecoder.decode(swisherQuestionJSONArray.getString(6), "utf-8");
					questions.add(new String[] { question, answer1, answer2, answer3, answer4, filename, diff });
				}
				return questions;
			} catch (JSONException e) {
				return new ArrayList<String[]>();
			} catch (UnsupportedEncodingException e) {
				return new ArrayList<String[]>();
			}
		} else
			return new ArrayList<String[]>();
	}

	/*public static boolean updateCustomPacks(Context ctx) {
		SharedPreferences sharedPrefsCustom = ctx.getSharedPreferences(CUSTOM_PACKS_PREFS, Context.MODE_PRIVATE);
		try {
			PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			Loggy.d("loaded = " + sharedPrefsCustom.getBoolean(CUSTOM_PACKS_LOADED, false));
			Loggy.d("versionCode = " + packageInfo.versionCode);
			Loggy.d("saved versionCode = " + sharedPrefsCustom.getInt(CUSTOM_PACKS_VERSION, Integer.MIN_VALUE));
			if (sharedPrefsCustom.getBoolean(CUSTOM_PACKS_LOADED, false)
					&& packageInfo.versionCode <= sharedPrefsCustom.getInt(CUSTOM_PACKS_VERSION, Integer.MIN_VALUE))
				return false;
			else
				return true;
		} catch (NameNotFoundException e) {
			return false;
		}

	}*/

	/*public static void loadCustomPacks(Context ctx, DatabaseManager dbManager) {
		ArrayList<String[]> tempQuestions = new ArrayList<String[]>();
		String[] customPacks = ctx.getResources().getStringArray(R.array.custom_packs);
		dbManager.removeCustomQuestion(customPacks);
		for (String customPack : customPacks) {
			try {
				InputStream is = ctx.getAssets().open(customPack + ".csv");
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				CSVReader csvReader = new CSVReader(reader);
				String[] lineEntries = csvReader.readNext();
				if (lineEntries[1].equals(QuestionContract.QUESTION_TEXT) && lineEntries.length >= 7) {
					Loggy.d("fix csv header");
					lineEntries[0] = lineEntries[1];
					lineEntries[1] = lineEntries[2];
					lineEntries[2] = lineEntries[3];
					lineEntries[3] = lineEntries[4];
					lineEntries[4] = lineEntries[5];
					lineEntries[5] = lineEntries[6];
				}
				if (lineEntries[0].equals(QuestionContract.QUESTION_TEXT) && lineEntries[1].equals(QuestionContract.ANSWER_CORRECT)
						&& lineEntries[2].equals(CustomQuestionContract.ANSWER_INCORRECT1)
						&& lineEntries[3].equals(CustomQuestionContract.ANSWER_INCORRECT2)
						&& lineEntries[4].equals(CustomQuestionContract.ANSWER_INCORRECT3)
						&& lineEntries[5].equals(QuestionContract.DIFFICULTY)) {
					lineEntries = csvReader.readNext();
					while (lineEntries != null) {
						if (lineEntries.length == 6) {
							if (Difficulty.isDifficulty(lineEntries[5])) {
								tempQuestions.add(new String[] { lineEntries[0], lineEntries[1], lineEntries[2], lineEntries[3],
										lineEntries[4], customPack, lineEntries[5] });
							}
						}
						lineEntries = csvReader.readNext();
					}
				}
				csvReader.close();
				is.close();
				for (int i = 0; i < tempQuestions.size(); i++) {
					dbManager.addCustomQuestion(tempQuestions.get(i));
				}
				tempQuestions.clear();
				Loggy.d("succeed on " + customPack);
			} catch (IOException e) {
				Loggy.d("failed on " + customPack);
			}
		}
		try {
			SharedPreferences sharedPrefsCustom = ctx.getSharedPreferences(CUSTOM_PACKS_PREFS, Context.MODE_PRIVATE);
			PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			sharedPrefsCustom.edit().putBoolean(CUSTOM_PACKS_LOADED, true).putInt(CUSTOM_PACKS_VERSION, packageInfo.versionCode).commit();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}*/
}
