package com.olyware.mathlock.utils;

import android.content.Context;
import android.widget.Toast;

public class Toaster {

	public static void sendChallengeFailed(Context ctx) {
		Toast.makeText(ctx, "Sending challenge failed. Try again later", Toast.LENGTH_LONG).show();
	}
}
