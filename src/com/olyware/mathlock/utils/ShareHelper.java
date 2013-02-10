package com.olyware.mathlock.utils;

import android.content.Context;
import android.content.Intent;

import com.olyware.mathlock.R;

public class ShareHelper {

	public static void share(Context context, String subject, String message, String link) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/html");
		if (message != null) {
			if (link != null) {
				i.putExtra(Intent.EXTRA_HTML_TEXT, link);
			}
		}
		i.setType("text/plain");
		if (subject != null) {
			i.putExtra(Intent.EXTRA_SUBJECT, subject);
		}
		if (message != null) {
			i.putExtra(Intent.EXTRA_TEXT, message);
		}

		context.startActivity(Intent.createChooser(i, context.getString(R.string.share_with)));
	}
}
