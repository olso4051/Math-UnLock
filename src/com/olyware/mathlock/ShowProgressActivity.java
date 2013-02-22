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

import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.utils.Coins;
import com.olyware.mathlock.utils.EggHelper;
import com.olyware.mathlock.views.GraphView;

public class ShowProgressActivity extends Activity {
	private Coins Money = new Coins(0, 0);
	private SharedPreferences sharedPrefsMoney, sharedPrefsStats;
	private TextView clock;
	final private float clockSize = 45, dateSize = 15;
	private float currentClockSize;
	private TextView coins;
	private Spinner spinTime, spinPackage, spinDifficulty;
	private GraphView graphView;
	private String[] unlockPackageKeys, displayPackageKeys, EggKeys;
	private int[] EggMaxValues;
	private String[] times = { "All", "Last Year", "Last 6 Months", "Last Month", "Last Week", "Today" };
	private long[] oldestTimes = { System.currentTimeMillis(), 31536000000l, 15768000000l, 2628000000l, 604800000l, 86400000l };
	private List<String> packages;
	private String[] difficulties = new String[Difficulty.getSize() + 1];	// +1 for all difficulties
	private String selectedPackage, selectedDifficulty;

	private DatabaseManager dbManager;

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

		dbManager = new DatabaseManager(getApplicationContext());

		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		displayPackageKeys = getResources().getStringArray(R.array.display_packages);
		EggKeys = getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = getResources().getIntArray(R.array.egg_max_values);

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

		graphView = (GraphView) findViewById(R.id.graph);

		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		sharedPrefsStats = getSharedPreferences("Stats", 0);
		Money = new Coins(sharedPrefsMoney.getInt("money", 0), sharedPrefsMoney.getInt("paid_money", 0));

		if (savedInstanceState != null) {
			currentClockSize = savedInstanceState.getFloat("ClockSize");
		}
		initSpinners();
		setTime();
		setGraph();
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
		sharedPrefsStats = getSharedPreferences("Stats", 0);
		Money.setMoneyPaid(sharedPrefsMoney.getInt("paid_money", 0));
		Money.setMoney(sharedPrefsMoney.getInt("money", 0));
		coins.setText(String.valueOf(Money.getMoney() + Money.getMoneyPaid()));
		Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[6], EggMaxValues[6]));
	}

	@Override
	protected void onPause() {
		SharedPreferences.Editor editor = sharedPrefsMoney.edit();
		editor.putInt("money", Money.getMoney());
		editor.putInt("paid_money", Money.getMoneyPaid());
		editor.commit();
		super.onPause();
	}

	@Override
	public void onAttachedToWindow() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	private void initSpinners() {
		packages = getUnlockedPackages();
		difficulties[0] = getString(R.string.all);
		for (int i = 1; i < difficulties.length; i++) {
			difficulties[i] = Difficulty.fromValueString(i - 1);
		}
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
				setGraph();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spinPackage.setAdapter(adapterPackages);
		selectedPackage = adapterPackages.getItem(0);
		spinPackage.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				selectedPackage = parent.getItemAtPosition(pos).toString();
				setGraph();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spinDifficulty.setAdapter(adapterDifficulties);
		selectedDifficulty = adapterDifficulties.getItem(0);
		spinDifficulty.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				selectedDifficulty = parent.getItemAtPosition(pos).toString();
				setGraph();
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
		else {
			list.add(displayPackageKeys[0]);
			for (int i = 1; i < unlockPackageKeys.length; i++) {
				if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false))
					list.add(displayPackageKeys[i]);
			}
		}
		return list;
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

	private void setGraph() {
		sharedPrefsStats = getSharedPreferences("Stats", 0);
		long oldestTime = getOldestTime();
		List<Integer> percent = dbManager.getStatPercentArray(oldestTime, selectedPackage, selectedDifficulty);

		int test[];
		if (percent.size() < 1) {
			test = new int[1];
			test[0] = 50;
		} else {
			test = new int[percent.size()];
			for (int i = 0; i < test.length; i++)
				test[i] = percent.get(i);
		}
		// graphView.setMovingAverage(rnd.nextInt(50) + 1);
		graphView.setArray(test);
		int correct = sharedPrefsStats.getInt("correct", 0);
		int wrong = sharedPrefsStats.getInt("wrong", 0);
		int coins = sharedPrefsStats.getInt("coins", 0);
		int bestStreak = sharedPrefsStats.getInt("bestStreak", 0);
		int currentStreak = sharedPrefsStats.getInt("currentStreak", 0);
		long totalTime = sharedPrefsStats.getLong("totalTime", 0);
		long answerTimeFast = sharedPrefsStats.getLong("answerTimeFast", 0);
		long answerTimeAve = sharedPrefsStats.getLong("answerTimeAve", 0);
		String eggs = EggHelper.getNumberUnlocked(this) + " / " + EggHelper.getTotalEggs(this);
		graphView.setStats(correct, wrong, coins, totalTime, bestStreak, currentStreak, answerTimeFast, answerTimeAve, eggs);
	}

	private long getOldestTime() {
		return System.currentTimeMillis() - oldestTimes[spinTime.getSelectedItemPosition()];
	}
}
