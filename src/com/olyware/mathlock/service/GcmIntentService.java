package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.ContactHashes;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.utils.ContactHelper;
import com.olyware.mathlock.utils.EncryptionHelper;
import com.olyware.mathlock.utils.GCMHelper;
import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.MoneyHelper;
import com.olyware.mathlock.utils.NotificationHelper;
import com.olyware.mathlock.utils.PreferenceHelper;
import com.olyware.mathlock.utils.PreferenceHelper.ChallengeStatus;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 999;
	final private static String USER_ID_TO_CONFIRM = "USER_ID_TO_CONFIRM";
	final private static String GET_COINS = "GET_COINS";
	final private static String CHALLENGE = "CHALLENGE";
	final private static String CHALLENGE_RESULT = "CHALLENGE_RESULT";
	final private static String CHALLENGE_STATUS = "CHALLENGE_STATUS";
	final private static String CHALLENGE_CANCEL = "CHALLENGE_CANCEL";
	final private static String CHALLENGE_REMIND = "CHALLENGE_REMIND";

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		String fullMessage = extras.getString("message");
		String type = getStringFromMessage(fullMessage, "type");
		String userID = getStringFromMessage(fullMessage, "user_id");
		String pickupHash = getStringFromMessage(fullMessage, "pickup_hash");

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that GCM
			 * will be extended in the future with new message types, just ignore
			 * any message types you're not interested in, or that you don't
			 * recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				// There was a send error
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				// Server deleted message
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				// If it's a regular GCM message, do some work.
				Loggy.d(fullMessage);
				if (!userID.equals("") || type.equals(USER_ID_TO_CONFIRM)) {
					Loggy.d("userID to confirm = " + userID);
					SharedPreferences sharedPrefsUserInfo = getSharedPreferences(getString(R.string.pref_user_info), Context.MODE_PRIVATE);
					sharedPrefsUserInfo.edit().putString(getString(R.string.pref_user_userid), userID).commit();
					GCMHelper.confirmID(this, userID);
				} else if (!pickupHash.equals("") || type.equals(GET_COINS)) {
					Loggy.d("get coins from gcm");
					SharedPreferences sharedPrefsUserInfo = getSharedPreferences(getString(R.string.pref_user_info), Context.MODE_PRIVATE);
					String storedUserID = sharedPrefsUserInfo.getString(getString(R.string.pref_user_userid), "");
					final Context ctx = this;
					new GetCoins(this) {
						@Override
						protected void onPostExecute(Integer result) {
							SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.pref_money), Context.MODE_PRIVATE);
							int coins = getCoins();
							int total = sharedPrefs.getInt(getString(R.string.pref_money_total_referral), 0) + coins;
							int pending = sharedPrefs.getInt(getString(R.string.pref_money_pending_paid), 0);
							sharedPrefs.edit().putInt(getString(R.string.pref_money_pending_paid), pending + coins)
									.putInt(getString(R.string.pref_money_total_referral), total).commit();
							new NotificationHelper(ctx).sendCoinNotification(pending + coins);
						}
					}.execute(storedUserID, pickupHash);
				} else if (type.equals(CHALLENGE)) {
					String challengeID = getStringFromMessage(fullMessage, "challenge_id");
					String userName = getStringFromMessage(fullMessage, "c_username");
					String cUserID = getStringFromMessage(fullMessage, "c_user_id");
					Loggy.d("userName = " + userName);
					if (userName.equals("")) {
						String phoneHash = getStringFromMessage(fullMessage, "phone_hash");
						String facebookHash = getStringFromMessage(fullMessage, "facebook_hash");
						CustomContactData contact = ContactHelper.findContact(this, ContactHelper.FindType.PhoneAndFacebookHASH,
								new ContactHashes(phoneHash, facebookHash, "", ""));
						userName = contact.getDisplayName();
						if (userName.equals("")) {
							if (!phoneHash.equals(""))
								userName = EncryptionHelper.decryptForURL(phoneHash);
							else
								userName = getString(R.string.challenge_default_opponent);
						}
					}
					int bet = getIntFromMessage(fullMessage, "bet");
					List<String> descriptions = getStringListFromMessage(fullMessage, "descriptions");
					List<String> questions = getStringListFromMessage(fullMessage, "questions");
					List<String[]> answers = getStringArrayListFromMessage(fullMessage, "answers");
					int difficultyMin = getIntFromMessage(fullMessage, "difficulty_min");
					int difficultyMax = getIntFromMessage(fullMessage, "difficulty_max");
					DatabaseManager dbManager = new DatabaseManager(this);
					if (questions.size() == answers.size()) {
						for (int i = 0; i < questions.size(); i++) {
							dbManager.addChallengeQuestion(challengeID, cUserID, descriptions.get(i), questions.get(i), answers.get(i),
									userName);
						}
					}
					PreferenceHelper.storeChallengeStatus(this, challengeID, ChallengeStatus.Undefined,
							CustomContactData.ChallengeState.New, userName, cUserID, bet, difficultyMin, difficultyMax, questions.size());
					NotificationHelper notificationHelper = new NotificationHelper(this);
					notificationHelper
							.sendChallengeNotification(challengeID, userName, questions.size(), difficultyMin, difficultyMax, bet);
					Intent challengeBroadcastIntent = new Intent(getString(R.string.challenge_receiver_filter));
					LocalBroadcastManager.getInstance(this).sendBroadcast(challengeBroadcastIntent);
				} else if (type.equals(CHALLENGE_RESULT)) {
					String userName;
					int score, scoreYou;
					String cUserID = getStringFromMessage(fullMessage, "c_user_id");
					String oUserID = getStringFromMessage(fullMessage, "o_user_id");
					int bet = getIntFromMessage(fullMessage, "bet");
					userID = ContactHelper.getUserID(this);
					Loggy.d("challenge result userID = " + userID + " |c_user_id = " + cUserID + " |o_user_id = " + oUserID);
					if (userID.equals(cUserID)) {										// You are c_user
						userName = getStringFromMessage(fullMessage, "o_username");
						score = getIntFromMessage(fullMessage, "o_score");
						scoreYou = getIntFromMessage(fullMessage, "c_score");
					} else if (userID.equals(oUserID)) {									// You are o_user
						userName = getStringFromMessage(fullMessage, "c_username");
						score = getIntFromMessage(fullMessage, "c_score");
						scoreYou = getIntFromMessage(fullMessage, "o_score");
					} else {																// couldn't figure out who you are
						userName = getString(R.string.challenge_default_opponent);			// assume you won
						int scoreTemp = getIntFromMessage(fullMessage, "o_score");
						scoreYou = getIntFromMessage(fullMessage, "c_score");
						if (scoreTemp > scoreYou) {
							score = scoreYou;
							scoreYou = scoreTemp;
						} else {
							score = scoreTemp;
						}
					}
					Loggy.d("your score = " + scoreYou + " there score = " + score);
					NotificationHelper notificationHelper = new NotificationHelper(this);
					bet = MoneyHelper.getModifiedBet(this, bet);
					if (scoreYou == score) {
						// Tie
						// don't do anything with bet
					} else if (scoreYou > score) {
						// Win - add money to account
						Loggy.d("adding money = " + bet);
						// PreferenceHelper.increaseMoney(this, bet);
						MoneyHelper.increasePendingMoney(this, bet);
					} else {
						// Loss - subtract money from account
						Loggy.d("taking money = " + bet);
						// PreferenceHelper.decreaseMoneyNoDebt(this, bet);
						MoneyHelper.decreasePendingMoneyNoDebt(this, bet);
					}
					notificationHelper.sendChallengeResultNotification(userName, scoreYou, score, bet);
					Intent challengeBroadcastIntent = new Intent(getString(R.string.challenge_receiver_filter));
					LocalBroadcastManager.getInstance(this).sendBroadcast(challengeBroadcastIntent);
				} else if (type.equals(CHALLENGE_STATUS)) {
					String challengeID = getStringFromMessage(fullMessage, "challenge_id");
					String status = getStringFromMessage(fullMessage, "status");
					ChallengeStatus challengeStatus = ChallengeStatus.Declined;
					CustomContactData.ChallengeState challengeState = CustomContactData.ChallengeState.None;
					if (status.equals("accepted")) {
						challengeStatus = ChallengeStatus.Accepted;
						challengeState = CustomContactData.ChallengeState.Active;
					}
					PreferenceHelper.storeChallengeStatus(this, challengeID, challengeStatus, challengeState);
					NotificationHelper notificationHelper = new NotificationHelper(this);
					notificationHelper.sendChallengeStatusNotification(challengeID, challengeStatus);
					Intent challengeBroadcastIntent = new Intent(getString(R.string.challenge_receiver_filter));
					LocalBroadcastManager.getInstance(this).sendBroadcast(challengeBroadcastIntent);
				} else if (type.equals(CHALLENGE_CANCEL)) {
					/*String challengeID = getStringFromMessage(fullMessage, "challenge_id");
					PreferenceHelper.storeChallengeStatus(this, challengeID, ChallengeStatus.Done, CustomContactData.ChallengeState.None);
					Intent challengeBroadcastIntent = new Intent(getString(R.string.challenge_receiver_filter));
					LocalBroadcastManager.getInstance(this).sendBroadcast(challengeBroadcastIntent);*/
				} else if (type.equals(CHALLENGE_REMIND)) {
					/*String challengeID = getStringFromMessage(fullMessage, "challenge_id");
					String userName = PreferenceHelper.getChallengeUserName(this, challengeID);
					int questions = PreferenceHelper.getChallengeQuestions(this, challengeID);
					int difficultyMin = PreferenceHelper.getChallengeDifficultyMin(this, challengeID);
					int difficultyMax = PreferenceHelper.getChallengeDifficultyMax(this, challengeID);
					int bet = PreferenceHelper.getChallengeBet(this, challengeID);
					NotificationHelper notificationHelper = new NotificationHelper(this);
					notificationHelper.sendChallengeNotification(challengeID, userName, questions, difficultyMin, difficultyMax, bet);
					Intent challengeBroadcastIntent = new Intent(getString(R.string.challenge_receiver_filter));
					LocalBroadcastManager.getInstance(this).sendBroadcast(challengeBroadcastIntent);*/
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private String getStringFromMessage(String msg, String key) {
		try {
			JSONObject json = new JSONObject(msg);
			return json.getString(key);
		} catch (JSONException e) {
			return "";
		}
	}

	private List<String> getStringListFromMessage(String msg, String key) {
		try {
			JSONObject json = new JSONObject(msg);
			JSONArray array = json.getJSONArray(key);
			List<String> result = new ArrayList<String>(array.length());
			for (int i = 0; i < array.length(); i++) {
				result.add(array.getString(i));
			}
			return result;
		} catch (JSONException e) {
			return new ArrayList<String>();
		}
	}

	private List<String[]> getStringArrayListFromMessage(String msg, String key) {
		try {
			JSONObject json = new JSONObject(msg);
			JSONArray array = json.getJSONArray(key);
			List<String[]> result = new ArrayList<String[]>(array.length());
			for (int i = 0; i < array.length(); i++) {
				JSONArray innerArray = array.getJSONArray(i);
				String[] results = new String[innerArray.length()];
				for (int j = 0; j < innerArray.length(); j++) {
					results[j] = innerArray.getString(j);
				}
				result.add(results);
			}
			return result;
		} catch (JSONException e) {
			return new ArrayList<String[]>();
		}
	}

	private int getIntFromMessage(String msg, String key) {
		try {
			JSONObject json = new JSONObject(msg);
			int value = Integer.parseInt(json.getString(key));
			return value;
		} catch (JSONException e) {
			return 0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}