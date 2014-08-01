package com.olyware.mathlock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.TextView;

import com.olyware.mathlock.R;
import com.olyware.mathlock.service.GetPromoCoins;

public class MoneyHelper {

	final public static long UTC_SEPTEMBER_1 = 1409529600000l;
	final private static int updateMoneyTime = 1000;
	final private static String Money = "money";
	final private static String PaidMoney = "paid_money";

	private static int updateMoneyStep = 1000;
	private static SharedPreferences sharedPrefsMoney;
	private static SharedPreferences.Editor editorPrefsMoney;
	private static int money, Pmoney;
	private static TextView coins;

	private static Handler timerHandler = new Handler();
	private static Runnable updateMoney = new Runnable() {
		@Override
		public void run() {
			int total = Integer.parseInt(coins.getText().toString()) - money - Pmoney;
			if (total != 0) {
				int step;
				if (Math.abs(total) > 20) {
					step = total / 20;
				} else
					step = total / Math.abs(total);
				timerHandler.postDelayed(this, updateMoneyStep);
				coins.setText("" + (total + money + Pmoney - step));
			} else
				timerHandler.removeCallbacks(this);
		}
	};

	public static void setMoney(Context context, TextView c, int m, int p) {
		coins = c;
		money = m;
		Pmoney = p;
		sharedPrefsMoney = context.getSharedPreferences("Packages", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsMoney.putInt(Money, money);
		editorPrefsMoney.putInt(PaidMoney, Pmoney);
		editorPrefsMoney.commit();

		timerHandler.removeCallbacks(updateMoney);
		int total = Integer.parseInt(coins.getText().toString());
		if ((total - money - Pmoney != 0)) {
			updateMoneyStep = Math.max(1, Math.abs(updateMoneyTime / (total - money - Pmoney)));
			timerHandler.postDelayed(updateMoney, updateMoneyStep);
		}
	}

	public static void setMoney(Context context, int m, int p) {
		money = m;
		Pmoney = p;
		sharedPrefsMoney = context.getSharedPreferences("Packages", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsMoney.putInt(Money, money);
		editorPrefsMoney.putInt(PaidMoney, Pmoney);
		editorPrefsMoney.commit();
	}

	public static void increasePaidMoney(Context context, int amount) {
		sharedPrefsMoney = context.getSharedPreferences("Packages", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsMoney.putInt(PaidMoney, sharedPrefsMoney.getInt(PaidMoney, 0) + amount);
		editorPrefsMoney.commit();
	}

	public static void increaseMoney(Context context, int amount) {
		sharedPrefsMoney = context.getSharedPreferences("Packages", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsMoney.putInt(Money, sharedPrefsMoney.getInt(Money, 0) + amount);
		editorPrefsMoney.commit();
	}

	public static void increasePendingMoney(Context context, int amount) {
		Loggy.d("increaseing pending money by " + amount);
		Loggy.d("pending money before = " + sharedPrefsMoney.getInt(context.getString(R.string.pref_money_pending), 0));
		sharedPrefsMoney = context.getSharedPreferences(context.getString(R.string.pref_money), Context.MODE_PRIVATE);
		editorPrefsMoney = sharedPrefsMoney.edit();
		int pending = sharedPrefsMoney.getInt(context.getString(R.string.pref_money_pending), 0);
		editorPrefsMoney.putInt(context.getString(R.string.pref_money_pending), pending + amount);
		editorPrefsMoney.commit();
		Loggy.d("pending money after = " + sharedPrefsMoney.getInt(context.getString(R.string.pref_money_pending), 0));
	}

	public static void decreasePendingMoneyNoDebt(Context context, int amount) {
		sharedPrefsMoney = context.getSharedPreferences(context.getString(R.string.pref_money), Context.MODE_PRIVATE);
		editorPrefsMoney = sharedPrefsMoney.edit();
		int pending = sharedPrefsMoney.getInt(context.getString(R.string.pref_money_pending), 0);
		int initMoney = sharedPrefsMoney.getInt(Money, 0);
		int money = initMoney + pending - amount;
		if (money < 0)
			money = 0;
		int newAmount = initMoney + pending - money;
		editorPrefsMoney.putInt(context.getString(R.string.pref_money_pending), pending - newAmount);
		editorPrefsMoney.commit();
	}

	public static int getTotalMoney(Context context) {
		sharedPrefsMoney = context.getSharedPreferences("Packages", 0);
		return sharedPrefsMoney.getInt(Money, 0) + sharedPrefsMoney.getInt(PaidMoney, 0);
	}

	public static int getMaxBet(Context context) {
		int maxBet = getTotalMoney(context) / 2;
		if (maxBet >= Integer.MAX_VALUE)
			maxBet = Integer.MAX_VALUE;
		return maxBet;
	}

	public static int getModifiedBet(Context context, int maxBet) {
		int newMaxBet = getMaxBet(context);
		return Math.min(newMaxBet, maxBet);
	}

	public static void addPromoCoins(final Context context, final String coinHash) {
		sharedPrefsMoney = context.getSharedPreferences(context.getString(R.string.pref_money), Context.MODE_PRIVATE);
		editorPrefsMoney = sharedPrefsMoney.edit();
		if (System.currentTimeMillis() < UTC_SEPTEMBER_1) {
			if (coinHash.equals(context.getString(R.string.coin_fountain_1000))) {
				if (!sharedPrefsMoney.getBoolean(coinHash, false)) {
					Loggy.d("increase coins");
					increasePendingMoney(context, 1000);
					editorPrefsMoney.putBoolean(coinHash, true).commit();
				}
			} else if (coinHash.equals(context.getString(R.string.coin_fountain_2000))) {
				if (!sharedPrefsMoney.getBoolean(coinHash, false)) {
					Loggy.d("increase coins");
					increasePendingMoney(context, 2000);
					editorPrefsMoney.putBoolean(coinHash, true).commit();
				}
			} else if (coinHash.equals(context.getString(R.string.coin_fountain_3000))) {
				if (!sharedPrefsMoney.getBoolean(coinHash, false)) {
					Loggy.d("increase coins");
					increasePendingMoney(context, 3000);
					editorPrefsMoney.putBoolean(coinHash, true).commit();
				}
			} else {
				new GetPromoCoins(context, coinHash).execute();
			}
		} else {
			new GetPromoCoins(context, coinHash).execute();
		}
	}
}