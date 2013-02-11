package com.olyware.mathlock.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.olyware.mathlock.R;

public class ShareHelper {

	public static void share(Context context, String subject, String fileName, String message, String link) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		if (subject != null) {
			i.putExtra(Intent.EXTRA_SUBJECT, subject);
		}
		if (fileName != null) {
			// TODO make this work
			Uri uri = Uri.parse(fileName);// .fromFile(new File(fileName));
			i.putExtra(Intent.EXTRA_STREAM, uri);
		}
		if (message != null) {
			if (link != null)
				i.putExtra(Intent.EXTRA_TEXT, message + "\n\n" + link);
			else
				i.putExtra(Intent.EXTRA_TEXT, message);
		}
		// TODO maybe make this return a result if they actually shared the app
		context.startActivity(Intent.createChooser(i, context.getString(R.string.share_with)));
	}
}
