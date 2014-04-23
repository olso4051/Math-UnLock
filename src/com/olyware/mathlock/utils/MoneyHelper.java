package com.olyware.mathlock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.TextView;

public class MoneyHelper {

	final private static int updateMoneyTime = 1000;
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
		editorPrefsMoney.putInt("money", money);
		editorPrefsMoney.putInt("paid_money", Pmoney);
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
		editorPrefsMoney.putInt("money", money);
		editorPrefsMoney.putInt("paid_money", Pmoney);
		editorPrefsMoney.commit();
	}

	public static void increaseMoney(Context context, int m, int p) {
		sharedPrefsMoney = context.getSharedPreferences("Packages", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsMoney.putInt("money", sharedPrefsMoney.getInt("money", 0) + m);
		editorPrefsMoney.putInt("paid_money", sharedPrefsMoney.getInt("paid_money", 0) + p);
		editorPrefsMoney.commit();
	}
}