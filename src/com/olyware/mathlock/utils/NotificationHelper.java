package com.olyware.mathlock.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.R;

public class NotificationHelper {
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	private Context ctx;

	public NotificationHelper(Context ctx) {
		this.ctx = ctx;
	}

	public void sendNotification(String title, String msg, int number, String titleFacebook, String msgFacebook, int ID) {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		// intent to share app
		String link = ShareHelper.buildShareURL(ctx);
		Intent iShare = ShareHelper.getShareIntent(ctx, null, null, ctx.getString(R.string.share_message), link);
		PendingIntent shareIntent = PendingIntent.getActivity(ctx, 0, iShare, 0);

		// intent to share app on Facebook only
		Intent iFacebookShare;
		PendingIntent facebookShareIntent;
		if (titleFacebook != null && msgFacebook != null && !titleFacebook.equals("") && !msgFacebook.equals("")) {
			iFacebookShare = ShareHelper.getShareFacebookIntent(ctx, titleFacebook, msgFacebook);
			facebookShareIntent = PendingIntent.getActivity(ctx, 0, iFacebookShare, 0);
		} else {
			iFacebookShare = ShareHelper.getShareFacebookIntent(ctx);
			facebookShareIntent = PendingIntent.getActivity(ctx, 0, iFacebookShare, 0);
		}

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx);
		mBuilder.setSmallIcon(R.drawable.ic_notification_small);
		mBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification_large));
		mBuilder.setContentTitle(title);
		mBuilder.setContentText(msg);
		if (number > 0)
			mBuilder.setNumber(number);
		mBuilder.setAutoCancel(true);
		mBuilder.addAction(R.drawable.trash, "Facebook", facebookShareIntent);
		mBuilder.addAction(R.drawable.trash, "Other", shareIntent);
		mBuilder.setContentIntent(shareIntent);
		mNotificationManager.notify(ID, mBuilder.build());
	}

	public void sendCoinNotification(int number, int ID) {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		String title = ctx.getString(R.string.notification_title_referral);
		String msg = ctx.getString(R.string.notification_message_referral1) + " " + number + " "
				+ ctx.getString(R.string.notification_message_referral2);
		String titleFacebook = ctx.getString(R.string.notification_title_referral_facebook1) + " " + number + " "
				+ ctx.getString(R.string.notification_title_referral_facebook2);
		String msgFacebook = ctx.getString(R.string.notification_message_referral_facebook);

		// intent to start app
		Intent iMain = new Intent(ctx, MainActivity.class);
		PendingIntent mainIntent = PendingIntent.getActivity(ctx, 0, iMain, 0);

		// intent to share app
		String link = ShareHelper.buildShareURL(ctx);
		Intent iShare = ShareHelper.getShareIntent(ctx, null, null, ctx.getString(R.string.share_message), link);
		PendingIntent shareIntent = PendingIntent.getActivity(ctx, 0, iShare, 0);

		// intent to share app on Facebook only
		Intent iFacebookShare = ShareHelper.getShareFacebookIntent(ctx, titleFacebook, msgFacebook);
		PendingIntent facebookShareIntent = PendingIntent.getActivity(ctx, 0, iFacebookShare, 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx);
		mBuilder.setSmallIcon(R.drawable.ic_notification_small);
		mBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification_large));
		mBuilder.setContentTitle(title);
		mBuilder.setContentText(msg);
		if (number > 0)
			mBuilder.setNumber(number);
		mBuilder.setAutoCancel(true);
		mBuilder.setContentIntent(mainIntent);
		mBuilder.addAction(R.drawable.trash, "Facebook", facebookShareIntent);
		mBuilder.addAction(R.drawable.trash, "Other", shareIntent);
		mNotificationManager.notify(ID, mBuilder.build());
	}
}
