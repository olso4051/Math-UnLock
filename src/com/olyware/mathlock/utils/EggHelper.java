package com.olyware.mathlock.utils;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.olyware.mathlock.MyApplication;
import com.olyware.mathlock.R;
import com.olyware.mathlock.views.JoystickView;

public class EggHelper {

	private static SharedPreferences sharedPrefsMoney, sharedPrefsEggs;
	private static SharedPreferences.Editor editorPrefsEggs;
	private static Random rand = new Random();
	private static TextView coins;
	private static Context ctx;

	public static int unlockEgg(FragmentActivity context, TextView c, JoystickView j, final String Egg, int max) {

		ctx = context;
		coins = c;
		sharedPrefsEggs = ctx.getSharedPreferences("Eggs", 0);
		int amount;
		if (!sharedPrefsEggs.getBoolean(Egg, false)) {
			sharedPrefsMoney = ctx.getSharedPreferences("Packages", 0);

			amount = getAmount(max);
			final int a = amount;

			editorPrefsEggs = sharedPrefsEggs.edit();
			editorPrefsEggs.putBoolean(Egg, true).commit();
			MoneyHelper.setMoney(context, coins, j, sharedPrefsMoney.getInt("money", 0) + a, sharedPrefsMoney.getInt("paid_money", 0), 0);

			// MyApplication.getGaTracker().send(MapBuilder.createEvent("easter_egg", "egg_found", Egg, (long) amount).build());
			MyApplication.getGaTracker().send(
					new HitBuilders.EventBuilder().setCategory("easter_egg").setAction("egg_found").setLabel(Egg).setValue((long) amount)
							.build());
			Toast.makeText(ctx, ctx.getString(R.string.egg_found) + " " + amount, Toast.LENGTH_SHORT).show();
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

			editorPrefsEggs = sharedPrefsEggs.edit();
			editorPrefsEggs.putBoolean(Egg, true).commit();
			MoneyHelper.setMoney(ctx, sharedPrefsMoney.getInt("money", 0) + a, sharedPrefsMoney.getInt("paid_money", 0));

			// MyApplication.getGaTracker().send(MapBuilder.createEvent("easter_egg", "egg_found", Egg, (long) amount).build());
			MyApplication.getGaTracker().send(
					new HitBuilders.EventBuilder().setCategory("easter_egg").setAction("egg_found").setLabel(Egg).setValue((long) amount)
							.build());

			Toast.makeText(ctx, ctx.getString(R.string.egg_found) + " " + amount, Toast.LENGTH_SHORT).show();
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
