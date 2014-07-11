package com.olyware.mathlock.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.olyware.mathlock.LoginFragment;
import com.olyware.mathlock.R;
import com.olyware.mathlock.service.UploadImage;

public class ShareHelper {
	final public static float FACEBOOK_LINK_RATIO = 1.9178082191780821917808219178082f;
	private static String staticLink;
	private static Context staticContext;
	private static Activity staticActivity;
	private static ProgressDialog staticProgressDialog;
	private static UiLifecycleHelper staticUiHelper;

	public static void share(Context context, String subject, Bitmap bitmap, String message, String link) {
		context.startActivity(getShareIntent(context, subject, bitmap, message, link));
	}

	public static void shareFacebook(Context context) {
		context.startActivity(getShareFacebookIntent(context));
	}

	public static void shareFacebook(Context context, String title, String caption) {
		context.startActivity(getShareFacebookIntent(context, title, caption));
	}

	public static void getLinkAndShareFacebook(final Context context, UiLifecycleHelper uiHelper, ProgressDialog pDialog, Bitmap image) {
		getLinkAndShareFacebook(context, uiHelper, pDialog, image, context.getString(R.string.share_base_url_facebook_name_readable), "");
	}

	public static void getLinkAndShareFacebook(final Context context, final UiLifecycleHelper uiHelper, final ProgressDialog pDialog,
			Bitmap image, String question, String deepLink) {
		final String link = buildShareURL(context);
		final String DeelDatApiKey = context.getString(R.string.deeldat_api_key);
		final String title = "Can you answer " + question + " to unlock your phone?";
		String name = LoginFragment.getUserName(context);
		if (name.equals(""))
			name = context.getString(R.string.share_base_url_facebook_description_readable);
		else
			name = context.getString(R.string.share_base_url_facebook_description_readable1) + " " + name + " "
					+ context.getString(R.string.share_base_url_facebook_description_readable2);
		final String description = name;
		String siteName = "Hiq Lockscreen";
		String appName = context.getString(R.string.app_name);
		String appPackage = context.getApplicationContext().getPackageName();
		String appClass = "MainActivity";
		new UploadImage(context, image) {
			@Override
			protected void onPostExecute(Integer result) {
				Log.d("test", "success = " + getSuccess());
				Log.d("test", "error = " + getError());
				Log.d("test", "url = " + getURL());
				Log.d("test", "hash = " + getHash());
				if (result == 0 || getSuccess().equals("true")) {
					loginOrShareFacebook(context, uiHelper, pDialog, getURL());
				} else {
					String share = "http://www.learnwithhiq.com/facebook/question.php";
					try {
						String linkEncoded = URLEncoder.encode(link, "utf-8");
						String titleEncoded = URLEncoder.encode(title, "utf-8");
						String descriptionEncoded = URLEncoder.encode(description, "utf-8");
						share = share + "?referral=" + linkEncoded;
						share = share + "&title=" + titleEncoded;
						share = share + "&name=" + descriptionEncoded;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					loginOrShareFacebook(context, uiHelper, pDialog, share);
				}
			}
		}.execute(DeelDatApiKey, title, description, siteName, link, appName, appPackage, appClass, deepLink);

	}

	private static Session.StatusCallback loginCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private static void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state.isOpened()) {
			shareFacebook();
		}
	}

	public static void loginOrShareFacebook(final Context context, final UiLifecycleHelper uiHelper, ProgressDialog pDialog, String link) {
		staticLink = link;
		staticContext = context;
		staticActivity = (Activity) context;
		staticUiHelper = uiHelper;
		staticProgressDialog = pDialog;
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			Log.d("test", "openForRead");
			session.openForRead(new Session.OpenRequest(staticActivity).setPermissions(LoginFragment.PERMISSIONS)
					.setCallback(loginCallback));
		} else if (session.isOpened()) {
			shareFacebook();
		} else {
			Log.d("test", "openActiveSession");
			Session.openActiveSession(staticActivity, true, LoginFragment.PERMISSIONS, loginCallback);
		}
	}

	private static void shareFacebook() {
		if (FacebookDialog.canPresentShareDialog(staticContext.getApplicationContext(), FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
			FacebookDialog.ShareDialogBuilder shareDialogBuilder1 = new FacebookDialog.ShareDialogBuilder(staticActivity)
					.setLink(staticLink);
			FacebookDialog shareDialog1 = shareDialogBuilder1.build();
			staticUiHelper.trackPendingDialogCall(shareDialog1.present());
		} else {
			showFeedDialog(staticContext, staticLink);
		}
	}

	private static void showFeedDialog(Context context, String link) {
		Bundle params = new Bundle();
		params.putString("link", link);
		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(context, Session.getActiveSession(), params)).setOnCompleteListener(
		// feedDialogCallback
				new OnCompleteListener() {
					@Override
					public void onComplete(Bundle values, FacebookException error) {
						if (error == null) {
							// When the story is posted, echo the success
							// and the post Id.
							final String postId = values.getString("post_id");
							if (postId != null) {
								Log.d("test", "Posted story, id: " + postId);
							} else {
								// User clicked the Cancel button
								Log.d("test", "Publish cancelled");
							}
						} else if (error instanceof FacebookOperationCanceledException) {
							// User clicked the "x" button
							Log.d("test", "Publish cancelled");
						} else {
							// Generic, ex: network error
							Log.d("test", "Error posting story");
						}
						if (staticProgressDialog != null) {
							staticProgressDialog.dismiss();
							staticProgressDialog = null;
						}
					}
				}).build();
		feedDialog.show();
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

	public static String getInvite(Context context) {
		return context.getString(R.string.share_message) + buildShareURL(context);
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
