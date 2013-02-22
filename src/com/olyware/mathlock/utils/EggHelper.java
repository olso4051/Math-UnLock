package com.olyware.mathlock.utils;

import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.TextView;

import com.olyware.mathlock.R;

public class EggHelper {

	private static SharedPreferences sharedPrefsMoney, sharedPrefsEggs;
	private static SharedPreferences.Editor editorPrefsEggs;
	private static Random rand = new Random();
	private static TextView coins;
	private static Context ctx;

	public static int unlockEgg(Context contex, TextView c, final String Egg, int max) {
		ctx = contex;
		coins = c;
		sharedPrefsEggs = ctx.getSharedPreferences("Eggs", 0);
		int amount;
		if (!sharedPrefsEggs.getBoolean(Egg, false)) {
			sharedPrefsMoney = ctx.getSharedPreferences("Packages", 0);

			amount = getAmount(max);
			final int a = amount;

			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setTitle(ctx.getString(R.string.egg_title));
			builder.setMessage(ctx.getString(R.string.egg_message) + amount).setCancelable(false);
			builder.setIcon(R.drawable.egg);
			builder.setPositiveButton(R.string.cash_it, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					editorPrefsEggs = sharedPrefsEggs.edit();
					editorPrefsEggs.putBoolean(Egg, true).commit();
					MoneyHelper.setMoney(ctx, coins, sharedPrefsMoney.getInt("money", 0) + a, sharedPrefsMoney.getInt("paid_money", 0));// setMoney();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		} else
			amount = 0;
		return amount;
	}

	public static int unlockEgg(Context contex, final String Egg, int max) {
		ctx = contex;
		sharedPrefsEggs = ctx.getSharedPreferences("Eggs", 0);
		int amount;
		if (!sharedPrefsEggs.getBoolean(Egg, false)) {
			sharedPrefsMoney = ctx.getSharedPreferences("Packages", 0);

			amount = getAmount(max);
			final int a = amount;

			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setTitle(ctx.getString(R.string.egg_title));
			builder.setMessage(ctx.getString(R.string.egg_message) + amount).setCancelable(false);
			builder.setIcon(R.drawable.egg);
			builder.setPositiveButton(R.string.cash_it, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					editorPrefsEggs = sharedPrefsEggs.edit();
					editorPrefsEggs.putBoolean(Egg, true).commit();
					MoneyHelper.setMoney(ctx, sharedPrefsMoney.getInt("money", 0) + a, sharedPrefsMoney.getInt("paid_money", 0));
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		} else
			amount = 0;
		return amount;
	}

	public static int getNumberUnlocked(Context context) {
		String[] keys = context.getResources().getStringArray(R.array.egg_keys);
		sharedPrefsEggs = context.getSharedPreferences("Eggs", 0);
		int count = 0;
		for (int i = 0; i < keys.length; i++)
			if (sharedPrefsEggs.getBoolean(keys[i], false))
				count++;
		return count;
	}

	public static int getTotalEggs(Context context) {
		return context.getResources().getIntArray(R.array.egg_max_values).length;
	}

	private static int getAmount(int max) {
		int grand = 1000000;
		int odds[] = { grand * 9 / 10, grand * 99 / 100, grand };
		int select = rand.nextInt(grand) + 1;
		int amount = 0;
		if (select <= odds[0])
			amount = max / 10;
		else if (select <= odds[1])
			amount = max / 5;
		else if (select < odds[2])
			amount = max / 2;
		else
			amount = max;
		return amount;
	}
}
