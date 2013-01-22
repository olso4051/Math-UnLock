package com.olyware.mathlock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class ShowProgressActivity extends Activity {
	private int money;
	private int Pmoney;
	private SharedPreferences sharedPrefsMoney;

	private TextView clock;
	final private float clockSize = 45, dateSize = 15;
	private float currentClockSize;
	private TextView coins;
	private Spinner spinTime, spinPackage, spinDifficulty;
	private String unlockPackageKeys[] = { "unlock_all", "unlock_math", "unlock_vocab", "unlock_language", "unlock_act", "unlock_sat",
			"unlock_gre", "unlock_toddler", "unlock_engineer" };
	private String displayPackageKeys[] = { "All", "Math", "Vocab", "Language", "ACT", "SAT", "GRE", "Toddler", "Engineer" };

	public final BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_progress);

		clock = (TextView) findViewById(R.id.clock);
		clock.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				toggleClockDate();
			}
		});
		currentClockSize = clockSize;
		coins = (TextView) findViewById(R.id.money);

		spinTime = (Spinner) findViewById(R.id.spinner_time);
		spinPackage = (Spinner) findViewById(R.id.spinner_package);
		spinDifficulty = (Spinner) findViewById(R.id.spinner_difficulty);

		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		money = sharedPrefsMoney.getInt("money", 0);
		Pmoney = sharedPrefsMoney.getInt("paid_money", 0);

		if (savedInstanceState != null) {
			currentClockSize = savedInstanceState.getFloat("ClockSize");
		}
		initSpinners();
		setTime();
		setMoney();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putFloat("ClockSize", currentClockSize);
	}

	@Override
	protected void onResume() {
		super.onResume();
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		money = sharedPrefsMoney.getInt("money", 0);
		Pmoney = sharedPrefsMoney.getInt("paid_money", 0);
		setMoney();
	}

	@Override
	protected void onPause() {
		SharedPreferences.Editor editor = sharedPrefsMoney.edit();
		editor.putInt("money", money);
		editor.putInt("paid_money", Pmoney);
		editor.commit();
		super.onPause();
	}

	@Override
	public void onAttachedToWindow() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	private void initSpinners() {
		String[] times = new String[] { "All", "Last Year", "Last 6 Months", "Last Month", "Last Week", "Today" };
		List<String> packages = getUnlockedPackages();
		String[] difficulties = new String[] { "All", "Hard", "Medium", "Easy" };
		ArrayAdapter<String> adapterTime = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, times);
		ArrayAdapter<String> adapterPackages = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, packages);
		ArrayAdapter<String> adapterDifficulties = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, difficulties);
		adapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapterPackages.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapterDifficulties.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinTime.setAdapter(adapterTime);
		spinTime.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// parent.getItemAtPosition(pos).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spinPackage.setAdapter(adapterPackages);
		spinPackage.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spinDifficulty.setAdapter(adapterDifficulties);
		spinDifficulty.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	private List<String> getUnlockedPackages() {
		List<String> list = new ArrayList<String>();
		if (sharedPrefsMoney.getBoolean("unlock_all", false))
			for (int i = 0; i < unlockPackageKeys.length; i++) {
				list.add(displayPackageKeys[i]);
			}
		else
			for (int i = 1; i < unlockPackageKeys.length; i++) {
				if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false))
					list.add(displayPackageKeys[i]);
			}
		return list;
	}

	private void setMoney() {
		SharedPreferences.Editor editor = sharedPrefsMoney.edit();
		editor.putInt("money", money);
		editor.putInt("paid_money", Pmoney);
		editor.commit();
		coins.setText("" + (money + Pmoney));
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

	private void toggleClockDate() {
		if (currentClockSize == dateSize) {
			clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, clockSize);	// clock
			currentClockSize = clockSize;
			setTime();
		} else {
			clock.setHeight(clock.getHeight());
			clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, dateSize);	// date
			currentClockSize = dateSize;
			setTime();
		}
	}
}
