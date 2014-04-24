package com.olyware.mathlock.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.olyware.mathlock.R;

public class NotificationHelper {
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	private Context ctx;

	public NotificationHelper(Context ctx) {
		this.ctx = ctx;
	}

	public void sendNotification(String title, String msg, int number, int ID) {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		// intent to share app
		String link = ShareHelper.buildShareURL(ctx);
		Intent i = ShareHelper.getShareIntent(ctx, null, null, ctx.getString(R.string.share_message), link);
		PendingIntent shareIntent = PendingIntent.getActivity(ctx, 0, i, 0);

		/*Intent resultIntent = new Intent(ctx, MainActivity.class).putExtra("share", true);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);*/

		// PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, MainActivity.class).putExtra("share", true), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx);
		mBuilder.setSmallIcon(R.drawable.ic_notification_small);
		mBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification_large));
		mBuilder.setContentTitle(title);
		mBuilder.setContentText(msg);
		if (number >= 0)
			mBuilder.setNumber(number);
		mBuilder.setAutoCancel(true);
		// mBuilder.addAction(R.drawable.trash, "Facebook", shareIntent);
		// mBuilder.addAction(R.drawable.trash, "Twitter", shareIntent);
		mBuilder.setContentIntent(shareIntent);
		mNotificationManager.notify(ID, mBuilder.build());
	}
}
