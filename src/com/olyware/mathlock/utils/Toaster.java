package com.olyware.mathlock.utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.olyware.mathlock.R;

public class Toaster {

	public static void sendChallengeFailed(Context ctx) {
		Toast.makeText(ctx, ctx.getString(R.string.challenge_send_failed), Toast.LENGTH_LONG).show();
	}

	public static void sendChallengeSuccess(Context ctx) {
		Toast.makeText(ctx, ctx.getString(R.string.challenge_send_success), Toast.LENGTH_LONG).show();
	}

	public static void toastChallengeNoPacksSelected(Context ctx) {
		Toast.makeText(ctx, ctx.getString(R.string.challenge_send_no_questions), Toast.LENGTH_LONG).show();
	}

	public static void toastLoginWithFacebook(final Context ctx, boolean fromBackgroundThread) {
		if (fromBackgroundThread) {
			Handler h = new Handler(ctx.getMainLooper());
			h.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(ctx, ctx.getString(R.string.fragment_challenge_facebook_prompt), Toast.LENGTH_LONG).show();
				}
			});
		} else {
			Toast.makeText(ctx, ctx.getString(R.string.fragment_challenge_facebook_prompt), Toast.LENGTH_LONG).show();
		}
	}

	public static void toastAllowFacebookFriends(final Context ctx, boolean fromBackgroundThread) {
		if (fromBackgroundThread) {
			Handler h = new Handler(ctx.getMainLooper());
			h.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(ctx, ctx.getString(R.string.fragment_challenge_facebook_friends_prompt), Toast.LENGTH_LONG).show();
				}
			});
		} else {
			Toast.makeText(ctx, ctx.getString(R.string.fragment_challenge_facebook_friends_prompt), Toast.LENGTH_LONG).show();
		}
	}

	public static void toastChallengeWaiting(Context ctx) {
		Toast.makeText(ctx, ctx.getString(R.string.fragment_challenge_waiting), Toast.LENGTH_LONG).show();
	}
}
