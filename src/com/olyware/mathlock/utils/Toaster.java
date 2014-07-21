package com.olyware.mathlock.utils;

import android.content.Context;
import android.widget.Toast;

import com.olyware.mathlock.R;

public class Toaster {

	public static void sendChallengeFailed(Context ctx) {
		Toast.makeText(ctx, ctx.getString(R.string.challenge_send_failed), Toast.LENGTH_LONG).show();
	}

	public static void sendChallengeSuccess(Context ctx) {
		Toast.makeText(ctx, ctx.getString(R.string.challenge_send_success), Toast.LENGTH_LONG).show();
	}
}
