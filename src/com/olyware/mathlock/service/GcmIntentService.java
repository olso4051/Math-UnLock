package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.ContactHashes;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.utils.ContactHelper;
import com.olyware.mathlock.utils.EncryptionHelper;
import com.olyware.mathlock.utils.NotificationHelper;
import com.olyware.mathlock.utils.PreferenceHelper;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 999;
	final private static String USER_ID_TO_CONFIRM = "USER_ID_TO_CONFIRM";
	final private static String GET_COINS = "GET_COINS";
	final private static String CHALLENGE = "CHALLENGE";
	final private static String CHALLENGE_RESULT = "CHALLENGE_RESULT";
	final private static String CHALLENGE_STATUS = "CHALLENGE_STATUS";

	private NotificationManager mNotificationManager;

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
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				sendNotification("Deleted messages on server: " + extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				if (!userID.equals("") || type.equals(USER_ID_TO_CONFIRM)) {
					SharedPreferences sharedPrefsUserInfo = getSharedPreferences(getString(R.string.pref_user_info), Context.MODE_PRIVATE);
					sharedPrefsUserInfo.edit().putString(getString(R.string.pref_user_userid), userID).commit();
					new ConfirmID(this) {
						@Override
						protected void onPostExecute(Integer result) {
							SharedPreferences sharedPrefsUserInfo = getSharedPreferences(getString(R.string.pref_user_info),
									Context.MODE_PRIVATE);
							if (result == 0)
								sharedPrefsUserInfo.edit().putBoolean(getString(R.string.pref_user_confirmed), true).commit();
							else
								sharedPrefsUserInfo.edit().putBoolean(getString(R.string.pref_user_confirmed), false).commit();
						}

					}.execute(userID);
				} else if (!pickupHash.equals("") || type.equals(GET_COINS)) {
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
					String userName = getStringFromMessage(fullMessage, "c_user_name");
					if (userName.equals("")) {
						String phoneHash = getStringFromMessage(fullMessage, "phone_hash");
						String facebookHash = getStringFromMessage(fullMessage, "facebook_hash");
						CustomContactData contact = ContactHelper.findContact(this, ContactHelper.FindType.PhoneAndFacebookHASH,
								new ContactHashes(phoneHash, facebookHash, "", ""));
						userName = contact.getName();
						if (userName.equals("")) {
							if (!phoneHash.equals(""))
								userName = EncryptionHelper.decryptForURL(phoneHash);
							else
								userName = "Someone";
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
							dbManager.addChallengeQuestion(challengeID, descriptions.get(i), questions.get(i), answers.get(i), userName);
						}
					}
					NotificationHelper notificationHelper = new NotificationHelper(this);
					notificationHelper
							.sendChallengeNotification(challengeID, userName, questions.size(), difficultyMin, difficultyMax, bet);
				} else if (type.equals(CHALLENGE_RESULT)) {
					String userName;
					int score, scoreYou;
					String cUserID = getStringFromMessage(fullMessage, "c_user_id");
					String oUserID = getStringFromMessage(fullMessage, "o_user_id");
					int bet = getIntFromMessage(fullMessage, "bet");
					userID = ContactHelper.getUserID(this);
					if (userID.equals(cUserID)) {
						userName = getStringFromMessage(fullMessage, "c_user_name");
						score = getIntFromMessage(fullMessage, "c_score");
						scoreYou = getIntFromMessage(fullMessage, "o_score");
					} else {
						userName = getStringFromMessage(fullMessage, "o_user_name");
						score = getIntFromMessage(fullMessage, "o_score");
						scoreYou = getIntFromMessage(fullMessage, "c_score");
					}
					NotificationHelper notificationHelper = new NotificationHelper(this);
					if (scoreYou == score) {
						// Tie
						// don't do anything with bet
					} else if (scoreYou > score) {
						// Win - add money to account
						PreferenceHelper.increaseMoney(this, bet);
					} else {
						// Loss - subtract money from account
						PreferenceHelper.decreaseMoneyNoDebt(this, bet);
					}
					notificationHelper.sendChallengeResultNotification(userName, scoreYou, score, bet);
				} else if (type.equals(CHALLENGE_STATUS)) {
					String challengeID = getStringFromMessage(fullMessage, "challenge_id");
					String status = getStringFromMessage(fullMessage, "status");
					boolean accepted = false;
					if (status.equals("accepted"))
						accepted = true;
					PreferenceHelper.storeChallengeAccepted(this, challengeID, accepted);
				} else {
					// Post notification of received message.
					sendNotification("Received: " + extras.toString());
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setSmallIcon(R.drawable.ic_notification_large);
		mBuilder.setContentTitle("GCM Notification");
		mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
		mBuilder.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
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