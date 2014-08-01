package com.olyware.mathlock.utils;

import java.util.Random;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.R;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.utils.PreferenceHelper.ChallengeStatus;

public class NotificationHelper {
	final static public int STREAK_ID = 1, TOTAL_ID = 2, COIN_ID = 3, CHALLENGE_ID = 4, CHALLENGE_RESULT_ID = 5, CHALLENGE_STATUS_ID = 6;
	final static public String EXTRA_OPEN_CHALLENGE = "open_challenge";
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	private Context ctx;
	private Random rand;
	private String[] messages;
	private int message;

	public NotificationHelper(Context ctx) {
		this.ctx = ctx;
		rand = new Random();
		messages = ctx.getResources().getStringArray(R.array.invite_messages);
		message = rand.nextInt(messages.length);
	}

	public void sendNotification(String title, String msg, int number, String titleFacebook, String msgFacebook, int ID) {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		// intent to share app
		String link = ShareHelper.buildShareURL(ctx);
		Intent iShare = ShareHelper.getShareIntent(ctx, null, null, messages[message], link);
		PendingIntent shareIntent = PendingIntent.getActivity(ctx, 0, iShare, 0);

		// intent to share app on Facebook only
		Intent iFacebookShare;
		PendingIntent shareFacebookIntent;
		if (titleFacebook != null && msgFacebook != null && !titleFacebook.equals("") && !msgFacebook.equals("")) {
			iFacebookShare = ShareHelper.getShareFacebookIntent(ctx, titleFacebook, msgFacebook);
			shareFacebookIntent = PendingIntent.getActivity(ctx, 0, iFacebookShare, 0);
		} else {
			iFacebookShare = ShareHelper.getShareFacebookIntent(ctx);
			shareFacebookIntent = PendingIntent.getActivity(ctx, 0, iFacebookShare, 0);
		}

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx);
		mBuilder.setSmallIcon(R.drawable.ic_notification_small);
		mBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification_large));
		mBuilder.setContentTitle(title);
		mBuilder.setContentText(msg);
		if (number > 0)
			mBuilder.setNumber(number);
		mBuilder.setAutoCancel(true);
		mBuilder.addAction(R.drawable.facebook, "Facebook", shareFacebookIntent);
		mBuilder.addAction(R.drawable.share_other, "Other", shareIntent);
		mBuilder.setContentIntent(shareFacebookIntent);
		mNotificationManager.notify(ID, mBuilder.build());
	}

	public void sendCoinNotification(int number) {
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
		Intent iShare = ShareHelper.getShareIntent(ctx, null, null, messages[message], link);
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
		mBuilder.addAction(R.drawable.facebook, "Facebook", facebookShareIntent);
		mBuilder.addAction(R.drawable.share_other, "Other", shareIntent);
		mNotificationManager.notify(COIN_ID, mBuilder.build());
	}

	public void clearCoinNotification() {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(COIN_ID);
	}

	public void sendChallengeNotification(String challengeID, String userName, int questions, int difficultyMin, int difficultyMax, int bet) {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		int modifiedBet = Math.min(bet, MoneyHelper.getMaxBet(ctx));
		String title = ctx.getString(R.string.notification_title_challenge);
		String diff = Difficulty.fromValueToString(difficultyMin);
		if (difficultyMin != difficultyMax)
			diff += " To " + Difficulty.fromValueToString(difficultyMax);
		String msg = userName + ctx.getString(R.string.notification_message_challenge_for) + modifiedBet
				+ ctx.getString(R.string.notification_message_challenge_coins);
		String[] msgInbox = new String[] { questions + ctx.getString(R.string.notification_message_challenge1),
				diff + ctx.getString(R.string.notification_message_challenge2),
				modifiedBet + ctx.getString(R.string.notification_message_challenge_coins) };

		// intent to accept notification
		/*Intent iMain = new Intent(ctx, NotificationBroadcastReceiver.class);
		iMain.setAction(NotificationBroadcastReceiver.ACTION_CHALLENGE_ACCEPTED);
		iMain.putExtra(NotificationBroadcastReceiver.CHALLENGE_ID, challengeID);
		PendingIntent mainIntent = PendingIntent.getBroadcast(ctx, 0, iMain, PendingIntent.FLAG_CANCEL_CURRENT);*/

		// intent when notification cleared
		/*Intent iDelete = new Intent(ctx, NotificationBroadcastReceiver.class);
		iDelete.setAction(NotificationBroadcastReceiver.ACTION_CHALLENGE_DENIED);
		iDelete.putExtra(NotificationBroadcastReceiver.CHALLENGE_ID, challengeID);
		PendingIntent deleteIntent = PendingIntent.getBroadcast(ctx, 0, iDelete, PendingIntent.FLAG_CANCEL_CURRENT);*/

		Intent iMain = new Intent(ctx, MainActivity.class);
		iMain.putExtra(EXTRA_OPEN_CHALLENGE, true);
		PendingIntent mainIntent = PendingIntent.getActivity(ctx, 0, iMain, 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx);
		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		inboxStyle.setBigContentTitle(title);
		for (String line : msgInbox) {
			inboxStyle.addLine(line);
		}
		mBuilder.setStyle(inboxStyle);
		mBuilder.setSmallIcon(R.drawable.ic_notification_small_challenge);
		mBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification_large));
		mBuilder.setContentTitle(title);
		mBuilder.setContentText(msg);
		mBuilder.setAutoCancel(true);
		mBuilder.setContentIntent(mainIntent);
		// mBuilder.setDeleteIntent(deleteIntent);
		mNotificationManager.notify(CHALLENGE_ID, mBuilder.build());
	}

	public void clearChallengeNotification() {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(CHALLENGE_ID);
	}

	public void sendChallengeStatusNotification(String challengeID, ChallengeStatus status) {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		String title = "";
		String msg = "";
		String userName = PreferenceHelper.getChallengeUserName(ctx, challengeID);
		Loggy.d("challenge status challengeID = " + challengeID + " |username = " + userName);
		if (status == ChallengeStatus.Accepted) {
			title = ctx.getString(R.string.notification_title_challenge_status_accepted);
			msg = userName + ctx.getString(R.string.notification_message_challenge_status_accepted);
		} else if (status == ChallengeStatus.Declined) {
			title = ctx.getString(R.string.notification_title_challenge_status_declined);
			msg = userName + ctx.getString(R.string.notification_message_challenge_status_declined);
		} else
			return;

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx);
		Intent iMain = new Intent(ctx, MainActivity.class);
		PendingIntent mainIntent = PendingIntent.getActivity(ctx, 0, iMain, 0);

		mBuilder.setSmallIcon(R.drawable.ic_notification_small_challenge);
		mBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification_large));
		mBuilder.setContentTitle(title);
		mBuilder.setContentText(msg);
		mBuilder.setAutoCancel(true);
		mBuilder.setContentIntent(mainIntent);
		mNotificationManager.notify(CHALLENGE_STATUS_ID, mBuilder.build());
	}

	public void clearChallengeStatusNotification() {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(CHALLENGE_STATUS_ID);
	}

	public void sendChallengeResultNotification(String userName, int scoreYou, int scoreThem, int bet) {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		String title = "";
		String msg = "";
		if (scoreYou == scoreThem) {
			// Tie
			title = ctx.getString(R.string.notification_title_challenge_result_tie);
			msg = userName + ctx.getString(R.string.notification_message_challenge_result_tie);
		} else if (scoreYou > scoreThem) {
			// Win
			title = ctx.getString(R.string.notification_title_challenge_result_won);
			msg = ctx.getString(R.string.notification_message_challenge_result_won1) + userName
					+ ctx.getString(R.string.notification_message_challenge_result2) + bet
					+ ctx.getString(R.string.notification_message_challenge_result3);
		} else {
			// Loss
			title = ctx.getString(R.string.notification_title_challenge_result_loss);
			msg = ctx.getString(R.string.notification_message_challenge_result_loss1) + userName
					+ ctx.getString(R.string.notification_message_challenge_result2) + bet
					+ ctx.getString(R.string.notification_message_challenge_result3);
		}

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx);
		Intent iMain = new Intent(ctx, MainActivity.class);
		iMain.putExtra(EXTRA_OPEN_CHALLENGE, true);
		PendingIntent mainIntent = PendingIntent.getActivity(ctx, 0, iMain, 0);

		mBuilder.setSmallIcon(R.drawable.ic_notification_small_challenge);
		mBuilder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_notification_large));
		mBuilder.setContentTitle(title);
		mBuilder.setContentText(msg);
		mBuilder.setAutoCancel(true);
		mBuilder.setContentIntent(mainIntent);
		mNotificationManager.notify(CHALLENGE_RESULT_ID, mBuilder.build());
	}

	public void clearChallengeResultNotification() {
		mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(CHALLENGE_RESULT_ID);
	}
}
