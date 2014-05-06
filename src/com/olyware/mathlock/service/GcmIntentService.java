package com.olyware.mathlock.service;

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
import com.olyware.mathlock.utils.NotificationHelper;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 999;
	private NotificationManager mNotificationManager;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		String userID = stringFromMessage(extras.getString("message"), "user_id");
		String pickupHash = stringFromMessage(extras.getString("message"), "pickup_hash");

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
				if (!userID.equals("")) {
					SharedPreferences sharedPrefsUserInfo = getSharedPreferences(getString(R.string.pref_user_info), Context.MODE_PRIVATE);
					sharedPrefsUserInfo.edit().putString(getString(R.string.pref_user_userid), userID).commit();
					/*Intent iUserID = new Intent(LoginActivity.RECEIVE_USERID);
					iUserID.putExtra("user_id", userID);
					LocalBroadcastManager.getInstance(this).sendBroadcast(iUserID);*/
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
				} else if (!pickupHash.equals("")) {
					SharedPreferences sharedPrefsUserInfo = getSharedPreferences(getString(R.string.pref_user_info), Context.MODE_PRIVATE);
					String storedUserID = sharedPrefsUserInfo.getString(getString(R.string.pref_user_userid), "");
					final Context ctx = this;
					new GetCoins(this) {
						@Override
						protected void onPostExecute(Integer result) {
							SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.pref_money), Context.MODE_PRIVATE);
							int coins = getCoins();
							int total = sharedPrefs.getInt(getString(R.string.pref_money_total_referral), 0) + coins;
							sharedPrefs.edit().putInt(getString(R.string.pref_money_pending_paid), coins)
									.putInt(getString(R.string.pref_money_total_referral), total).commit();
							new NotificationHelper(ctx).sendCoinNotification(total, 3);
						}
					}.execute(storedUserID, pickupHash);
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

	private String stringFromMessage(String msg, String key) {
		try {
			JSONObject json = new JSONObject(msg);
			return json.getString(key);
		} catch (JSONException e) {
			return "";
		}
	}
}