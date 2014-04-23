package com.olyware.mathlock.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.olyware.mathlock.R;

public class NotificationHelper {
	// private static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	private Context ctx;

	/*private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("GAtest", "onReceive my receiver works!");
			MoneyHelper.increaseMoney(ctx, intent.getIntExtra("money", 0), 0);
			ctx.unregisterReceiver(this);
		}
	};*/

	public NotificationHelper(Context ctx) {
		this.ctx = ctx;
	}

	public void sendNotification(String title, String msg, int number, int value, int ID) {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		/*Intent intent = new Intent(NOTIFICATION_DELETED_ACTION).putExtra("money", value);
		PendingIntent deleteIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
		ctx.registerReceiver(receiver, new IntentFilter(NOTIFICATION_DELETED_ACTION));*/

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
		mBuilder.setNumber(number);
		mBuilder.setAutoCancel(true);
		mBuilder.addAction(R.drawable.trash, "Facebook", shareIntent);
		mBuilder.addAction(R.drawable.trash, "Twitter", shareIntent);
		mBuilder.setContentIntent(shareIntent);
		// mBuilder.setDeleteIntent(deleteIntent);
		mNotificationManager.notify(ID, mBuilder.build());
	}
}
