package com.olyware.mathlock.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.olyware.mathlock.R;

public class ShareHelper {

	public static void share(Context context, String subject, String fileName, String message, String link) {
		// TODO maybe make this return a result if they actually shared the app
		context.startActivity(getShareIntent(context, subject, fileName, message, link));
	}

	public static Intent getShareIntent(Context context, String subject, String fileName, String message, String link) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		if (subject != null) {
			i.putExtra(Intent.EXTRA_SUBJECT, subject);
		}
		if (fileName != null) {
			// TODO make this work
			Uri uri = Uri.parse(fileName);
			i.putExtra(Intent.EXTRA_STREAM, uri);
		}
		if (message != null) {
			if (link != null)
				i.putExtra(Intent.EXTRA_TEXT, message + "\n\n" + link);
			else
				i.putExtra(Intent.EXTRA_TEXT, message);
		} else {
			if (link != null)
				i.putExtra(Intent.EXTRA_TEXT, link);
		}
		return Intent.createChooser(i, context.getString(R.string.share_with));
	}

	public static String buildShareURL(Context ctx) {
		String userID = ctx.getSharedPreferences("ga_prefs", Context.MODE_PRIVATE).getString("user_id", "");
		String baseLink = ctx.getString(R.string.share_base_url);
		if (userID.equals("")) {
			return baseLink;
		} else {
			// encryptedContentForURL = new EncryptionHelper().encryptForURL(regID);
			String encryptedContentForURL = new EncryptionHelper().encryptForURL(userID);
			// String decryptedContentForURL = new EncryptionHelper().decryptForURL(encryptedContentForURL);
			return baseLink + ctx.getString(R.string.share_content_url) + encryptedContentForURL;
			/* Log.d("GAtest", "regID = " + regID);
			Log.d("GAtest", "userID = " + userID);
			Log.d("GAtest", "encryptedID = " + encryptedContentForURL);
			Log.d("GAtest", "decryptedID = " + decryptedContentForURL);
			Log.d("GAtest", "(decryptedID == regID) = " + regID.equals(decryptedContentForURL));
			Log.d("GAtest", "(decryptedID == userID) = " + userID.equals(decryptedContentForURL));*/
		}
	}
}
