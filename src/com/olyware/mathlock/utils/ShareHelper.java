package com.olyware.mathlock.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.olyware.mathlock.LoginFragment;
import com.olyware.mathlock.R;
import com.olyware.mathlock.service.UploadImage;

public class ShareHelper {

	public static void share(Context context, String subject, Bitmap bitmap, String message, String link) {
		context.startActivity(getShareIntent(context, subject, bitmap, message, link));
	}

	public static void shareFacebook(Context context) {
		context.startActivity(getShareFacebookIntent(context));
	}

	public static void shareFacebook(Context context, String title, String caption) {
		context.startActivity(getShareFacebookIntent(context, title, caption));
	}

	public static void shareFacebook(final Context context, UiLifecycleHelper uiHelper, Bitmap image) {
		shareFacebook(context, uiHelper, image, context.getString(R.string.share_base_url_facebook_name_readable));
	}

	public static void shareFacebook(final Context context, final UiLifecycleHelper uiHelper, Bitmap image, final String title) {
		final String link = buildShareURL(context);
		final String userID = LoginFragment.getUserID(context);
		final String questionID = "";
		new UploadImage(context, image) {
			@Override
			protected void onPostExecute(Integer result) {
				Log.d("test", "upload image result = " + result);
				Log.d("test", "success = " + getSuccess());
				Log.d("test", "url = " + getURL());
				Log.d("test", "hash = " + getHash());
				if (result == 0 || getSuccess().equals("true")) {
					shareFacebook(context, uiHelper, getURL());
				} else {
					String share = "http://www.learnwithhiq.com/facebook/question.php";
					try {
						String linkEncoded = URLEncoder.encode(link, "utf-8");
						String titleEncoded = URLEncoder.encode(title, "utf-8");
						String nameEncoded = URLEncoder.encode(userID, "utf-8");
						share = share + "?referral=" + linkEncoded;
						share = share + "&title=" + titleEncoded;
						share = share + "&name=" + nameEncoded;
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					shareFacebook(context, uiHelper, share);
				}
				super.onPostExecute(result);
			}
		}.execute(userID, title, link, questionID);

	}

	public static void shareFacebook(final Context context, final UiLifecycleHelper uiHelper, String link) {
		Activity act = (Activity) context;
		if (FacebookDialog.canPresentShareDialog(context.getApplicationContext(), FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
			FacebookDialog.ShareDialogBuilder shareDialogBuilder1 = new FacebookDialog.ShareDialogBuilder(act).setLink(link/*share*/);
			FacebookDialog shareDialog1 = shareDialogBuilder1.build();
			uiHelper.trackPendingDialogCall(shareDialog1.present());
		} else {
			Bundle params = new Bundle();
			params.putString("link", link);

			WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(context, Session.getActiveSession(), params)).setOnCompleteListener(
					new OnCompleteListener() {
						@Override
						public void onComplete(Bundle values, FacebookException error) {
							if (error == null) {
								// When the story is posted, echo the success
								// and the post Id.
								final String postId = values.getString("post_id");
								if (postId != null) {
									Toast.makeText(context, "Posted story, id: " + postId, Toast.LENGTH_SHORT).show();
								} else {
									// User clicked the Cancel button
									Toast.makeText(context, "Publish cancelled", Toast.LENGTH_SHORT).show();
								}
							} else if (error instanceof FacebookOperationCanceledException) {
								// User clicked the "x" button
								Toast.makeText(context, "Publish cancelled", Toast.LENGTH_SHORT).show();
							} else {
								// Generic, ex: network error
								Toast.makeText(context, "Error posting story", Toast.LENGTH_SHORT).show();
							}
						}
					}).build();
			feedDialog.show();
		}
	}

	public static Intent getShareIntent(Context context, String subject, Bitmap bitmap, String message, String link) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("image/png");
		if (subject != null) {
			i.putExtra(Intent.EXTRA_SUBJECT, subject);
		}
		if (bitmap != null) {
			try {
				File path = Environment.getExternalStorageDirectory();
				File imageFile = new File(path, "fileToShare.png");
				FileOutputStream fileOutPutStream;
				fileOutPutStream = new FileOutputStream(imageFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutPutStream);
				fileOutPutStream.flush();
				fileOutPutStream.close();
				Uri uri = Uri.parse("file://" + imageFile.getAbsolutePath());
				i.putExtra(Intent.EXTRA_STREAM, uri);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		String userID = context.getSharedPreferences(context.getString(R.string.pref_user_info), Context.MODE_PRIVATE).getString(
				context.getString(R.string.pref_user_userid), "");
		String baseLink = context.getString(R.string.share_link_url);
		if (userID.equals("")) {
			return baseLink;
		} else {
			String encryptedContentForURL = new EncryptionHelper().encryptForURL(userID);
			return baseLink + context.getString(R.string.share_content_url) + encryptedContentForURL;
		}
	}

	public static String buildShareFacebookURL(Context context) {
		String userID = context.getSharedPreferences(context.getString(R.string.pref_user_info), Context.MODE_PRIVATE).getString(
				context.getString(R.string.pref_user_userid), "");
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
					+ context.getString(R.string.share_base_url_facebook_redirect)
					+ context.getString(R.string.share_base_url_facebook_picture);
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
		String userID = context.getSharedPreferences(context.getString(R.string.pref_user_info), Context.MODE_PRIVATE).getString(
				context.getString(R.string.pref_user_userid), "");
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
					+ context.getString(R.string.share_base_url_facebook_redirect)
					+ context.getString(R.string.share_base_url_facebook_picture);
		}
		return baseLink;
	}
}
