package com.olyware.mathlock.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.olyware.mathlock.R;

public class ShareHelper {

	public static void share(Context context, String subject, String fileName, String message, String link) {
		// TODO maybe make this return a result if they actually shared the app
		context.startActivity(getShareIntent(context, subject, fileName, message, link));
	}

	public static void shareFacebook(Context context) {
		context.startActivity(getShareFacebookIntent(context));
	}

	public static void shareFacebook(Context context, String title, String caption) {
		context.startActivity(getShareFacebookIntent(context, title, caption));
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

	public static Intent getShareFacebookIntent(Context context) {
		return new Intent(Intent.ACTION_VIEW, Uri.parse(buildShareFacebookURL(context)));
	}

	public static Intent getShareFacebookIntent(Context context, String title, String caption) {
		return new Intent(Intent.ACTION_VIEW, Uri.parse(buildShareFacebookURL(context, title, caption)));
	}

	public static String buildShareURL(Context context) {
		String userID = context.getSharedPreferences("ga_prefs", Context.MODE_PRIVATE).getString("user_id", "");
		String baseLink = context.getString(R.string.share_link_url);
		if (userID.equals("")) {
			return baseLink;
		} else {
			// encryptedContentForURL = new EncryptionHelper().encryptForURL(regID);
			String encryptedContentForURL = new EncryptionHelper().encryptForURL(userID);
			// String decryptedContentForURL = new EncryptionHelper().decryptForURL(encryptedContentForURL);
			return baseLink + context.getString(R.string.share_content_url) + encryptedContentForURL;
			/* Log.d("GAtest", "regID = " + regID);
			Log.d("GAtest", "userID = " + userID);
			Log.d("GAtest", "encryptedID = " + encryptedContentForURL);
			Log.d("GAtest", "decryptedID = " + decryptedContentForURL);
			Log.d("GAtest", "(decryptedID == regID) = " + regID.equals(decryptedContentForURL));
			Log.d("GAtest", "(decryptedID == userID) = " + userID.equals(decryptedContentForURL));*/
		}
	}

	public static String buildShareFacebookURL(Context context) {
		String userID = context.getSharedPreferences("ga_prefs", Context.MODE_PRIVATE).getString("user_id", "");
		String baseLink = context.getString(R.string.share_base_url_facebook) + context.getString(R.string.app_id)
				+ context.getString(R.string.share_base_url_facebook_link);
		String baseLinkEasy = baseLink + context.getString(R.string.share_link_url_easy);
		if (userID.equals("")) {
			return baseLinkEasy;
		} else {
			String encryptedContentForURL = new EncryptionHelper().encryptForURL(userID);
			baseLink = baseLink + context.getString(R.string.share_link_url_facebook) + encryptedContentForURL
					+ context.getString(R.string.share_base_url_facebook_name)
					+ context.getString(R.string.share_base_url_facebook_caption)
					+ context.getString(R.string.share_base_url_facebook_description)
					+ context.getString(R.string.share_base_url_facebook_redirect);
			return baseLink;
		}
	}

	public static String buildShareFacebookURL(Context context, String title, String caption) {
		try {
			title = context.getString(R.string.share_base_url_facebook_base_name) + URLEncoder.encode(title, "UTF-8");
			caption = context.getString(R.string.share_base_url_facebook_base_caption) + URLEncoder.encode(caption, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			title = context.getString(R.string.share_base_url_facebook_name);
			caption = context.getString(R.string.share_base_url_facebook_caption);
		}
		String userID = context.getSharedPreferences("ga_prefs", Context.MODE_PRIVATE).getString("user_id", "");
		String baseLink = context.getString(R.string.share_base_url_facebook) + context.getString(R.string.app_id)
				+ context.getString(R.string.share_base_url_facebook_link);
		String baseLinkEasy = baseLink + context.getString(R.string.share_link_url_easy);
		if (userID.equals("")) {
			baseLink = baseLinkEasy + title + caption + context.getString(R.string.share_base_url_facebook_description)
					+ context.getString(R.string.share_base_url_facebook_redirect);
		} else {
			String encryptedContentForURL = new EncryptionHelper().encryptForURL(userID);
			baseLink = baseLink + context.getString(R.string.share_link_url_facebook) + encryptedContentForURL + title + caption
					+ context.getString(R.string.share_base_url_facebook_description)
					+ context.getString(R.string.share_base_url_facebook_redirect);
		}
		return baseLink;
	}
}
