package com.olyware.mathlock.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.olyware.mathlock.R;
import com.olyware.mathlock.views.PixelHelper;

public class Clock {

	private float clockSizeSP = 40, dateSize = 20;
	private TextView clock, coins;
	private float currentClockSize;

	private String[] EggKeys;
	private int[] EggMaxValues;

	final private Coins Money = new Coins(0, 0);

	private Context ctx;

	private final BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			boolean timeChange = (action.equals(Intent.ACTION_TIME_TICK) || action.equals(Intent.ACTION_TIME_CHANGED) || action
					.equals(Intent.ACTION_TIMEZONE_CHANGED));
			if (timeChange) {
				setTime();
			}
		}
	};

	public Clock(Context ctx, TextView clock) {
		this.ctx = ctx;
		this.clock = clock;
		this.coins = null;
		EggKeys = ctx.getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = ctx.getResources().getIntArray(R.array.egg_max_values);
		this.clock.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				toggleClockDate();
			}
		});
		clockSizeSP = PixelHelper.pixelToSP(ctx, clock.getTextSize());
		currentClockSize = clockSizeSP;

		IntentFilter c_intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		c_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		c_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
		this.ctx.registerReceiver(m_timeChangedReceiver, c_intentFilter);

		setTime();
	}

	public Clock(Context ctx, TextView clock, TextView coins) {
		this.ctx = ctx;
		this.clock = clock;
		this.coins = coins;
		EggKeys = ctx.getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = ctx.getResources().getIntArray(R.array.egg_max_values);
		this.clock.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				toggleClockDate();
			}
		});
		clockSizeSP = PixelHelper.pixelToSP(ctx, clock.getTextSize());
		currentClockSize = clockSizeSP;

		IntentFilter c_intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		c_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		c_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
		this.ctx.registerReceiver(m_timeChangedReceiver, c_intentFilter);

		SharedPreferences sharedPrefsMoney = this.ctx.getSharedPreferences("Packages", 0);
		Money.setMoneyPaid(sharedPrefsMoney.getInt("paid_money", 0));
		Money.setMoney(sharedPrefsMoney.getInt("money", 0));
		this.coins.setText(String.valueOf(Money.getMoney() + Money.getMoneyPaid()));

		setTime();
	}

	public void destroy() {
		if (m_timeChangedReceiver != null)
			ctx.unregisterReceiver(m_timeChangedReceiver);
	}

	private void toggleClockDate() {
		if (coins != null)
			Money.increaseMoney(EggHelper.unlockEgg(ctx, coins, EggKeys[4], EggMaxValues[4]));
		else
			EggHelper.unlockEgg(ctx, EggKeys[4], EggMaxValues[4]);

		if (currentClockSize == dateSize) {
			clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, clockSizeSP);	// clock
			currentClockSize = clockSizeSP;
			setTime();
		} else {
			clock.setHeight(clock.getHeight());
			clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, dateSize);	// date
			currentClockSize = dateSize;
			setTime();
		}
	}

	@SuppressLint("SimpleDateFormat")
	private void setTime() {
		Date curDateTime = new Date(System.currentTimeMillis());

		if (currentClockSize == dateSize) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE,\nMMMM d");
			clock.setText(dateFormatter.format(curDateTime));
		} else {
			// hour:minute am/pm newline Day, Month DayOfMonth
			SimpleDateFormat hourFormatter = new SimpleDateFormat("hh");
			int hour = Integer.parseInt(hourFormatter.format(curDateTime));
			int start = 0;
			if (hour < 10)
				start = 1;
			SimpleDateFormat clockFormatter = new SimpleDateFormat("hh:mm");
			String time = clockFormatter.format(curDateTime);
			time = time.substring(start);
			SimpleDateFormat AMPMFormatter = new SimpleDateFormat("a");

			clock.setText(Html.fromHtml(time + "<small><small><small>" + AMPMFormatter.format(curDateTime) + "</small></small></small>"));
		}
	}
}