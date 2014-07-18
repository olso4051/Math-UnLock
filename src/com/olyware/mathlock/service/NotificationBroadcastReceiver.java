package com.olyware.mathlock.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.olyware.mathlock.utils.PreferenceHelper;
import com.olyware.mathlock.utils.PreferenceHelper.ChallengeStatus;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

	final public static String ACTION_CHALLENGE_ACCEPTED = "challenge_accepted";
	final public static String ACTION_CHALLENGE_DENIED = "challenge_denied";
	final public static String CHALLENGE_ID = "challenge_id";

	@Override
	public void onReceive(final Context context, Intent intent) {
		if (ACTION_CHALLENGE_ACCEPTED.equals(intent.getAction())) {
			// TODO accept challenge
			Bundle bundle = intent.getExtras();
			final String challengeID = bundle.getString(CHALLENGE_ID);
			new AcceptChallenge(context, challengeID, true) {
				@Override
				protected void onPostExecute(Integer result) {
					if (result == 0) {
						PreferenceHelper.storeChallengeStatus(context, challengeID, ChallengeStatus.Accepted);
						Toast.makeText(context, "Challenge Accepted", Toast.LENGTH_LONG).show();
					}
				}
			}.execute();
		} else if (ACTION_CHALLENGE_DENIED.equals(intent.getAction())) {
			// TODO deny challenge
			Bundle bundle = intent.getExtras();
			final String challengeID = bundle.getString(CHALLENGE_ID);
			new AcceptChallenge(context, challengeID, false) {
				@Override
				protected void onPostExecute(Integer result) {
					if (result == 0) {
						PreferenceHelper.storeChallengeStatus(context, challengeID, ChallengeStatus.Declined);
						Toast.makeText(context, "Challenge Denied", Toast.LENGTH_LONG).show();
					}
				}
			}.execute();
		}
	}

}
