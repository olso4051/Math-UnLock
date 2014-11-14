package com.olyware.mathlock;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.ui.Typefaces;
import com.olyware.mathlock.utils.Coins;
import com.olyware.mathlock.utils.EZ;
import com.olyware.mathlock.utils.EggHelper;
import com.olyware.mathlock.views.GraphView;

public class ShowProgressActivity extends Fragment {
	final private static String SCREEN_LABEL = "Progress Screen";
	private LinearLayout layout;
	private Typefaces typefaces;
	private Coins Money = new Coins(0, 0);
	private SharedPreferences sharedPrefsMoney, sharedPrefsStats;
	// private ImageButton back;
	// private Clock clock;
	// private TextView coins;
	private Spinner spinTime, spinPackage, spinDifficulty;
	private GraphView graphView;
	private String[] unlockPackageKeys, EggKeys;
	private int[] EggMaxValues;
	private String[] times;
	private long[] oldestTimes = { System.currentTimeMillis(), 31536000000l, 15768000000l, 2628000000l, 604800000l, 86400000l };
	private List<String> packages, customCategories, displayPackageKeys, displayCustomPackageKeys;
	private String[] difficulties = new String[Difficulty.getSize() + 1];	// +1 for all difficulties
	private String selectedPackage, selectedDifficulty;

	private DatabaseManager dbManager;

	private class OpenDatabase extends AsyncTask<Void, Integer, Integer> {
		@Override
		protected Integer doInBackground(Void... voids) {
			publishProgress(0);
			if (isVisible()) {
				dbManager = new DatabaseManager(getActivity().getApplicationContext());
				customCategories = dbManager.getAllCustomCategories();
				for (String cat : customCategories)
					displayCustomPackageKeys.add(getString(R.string.custom) + " " + cat);
			}
			return 0;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == 0) {
				initSpinners();
				setGraph();
			}
			super.onPostExecute(result);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_progress, null);

		layout = (LinearLayout) view.findViewById(R.id.layout);
		typefaces = Typefaces.getInstance(getActivity());
		EZ.setFont((ViewGroup) layout, typefaces.robotoLight);

		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		displayPackageKeys = EZ.list(getResources().getStringArray(R.array.display_packages));
		displayCustomPackageKeys = EZ.list();
		times = getResources().getStringArray(R.array.times);
		EggKeys = getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = getResources().getIntArray(R.array.egg_max_values);

		new OpenDatabase().execute();

		// back = (ImageButton) view.findViewById(R.id.back);
		// back.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// getActivity().finish();
		// }
		// });
		view.findViewById(R.id.crossImage).setOnClickListener((MainActivity) getActivity());

		// coins = (TextView) view.findViewById(R.id.money);
		// clock = new Clock(getActivity(), (TextView) view.findViewById(R.id.clock), coins);

		spinTime = (Spinner) view.findViewById(R.id.spinner_time);
		spinPackage = (Spinner) view.findViewById(R.id.spinner_package);
		spinDifficulty = (Spinner) view.findViewById(R.id.spinner_difficulty);

		graphView = (GraphView) view.findViewById(R.id.graph);

		sharedPrefsMoney = getActivity().getSharedPreferences("Packages", 0);
		sharedPrefsStats = getActivity().getSharedPreferences("Stats", 0);
		Money = new Coins(sharedPrefsMoney.getInt("money", 0), sharedPrefsMoney.getInt("paid_money", 0));

		initSpinners();
		setGraph();

		MyApplication.getGaTracker().set(Fields.SCREEN_NAME, SCREEN_LABEL);

		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_progress);

		// layout = (LinearLayout) findViewById(R.id.layout);
		// typefaces = Typefaces.getInstance(this);
		// EZ.setFont((ViewGroup) layout, typefaces.robotoLight);
		//
		// unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		// displayPackageKeys = EZ.list(getResources().getStringArray(R.array.display_packages));
		// displayCustomPackageKeys = EZ.list();
		// times = getResources().getStringArray(R.array.times);
		// EggKeys = getResources().getStringArray(R.array.egg_keys);
		// EggMaxValues = getResources().getIntArray(R.array.egg_max_values);
		//
		// new OpenDatabase().execute();
		//
		// back = (ImageButton) findViewById(R.id.back);
		// back.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// finish();
		// }
		// });
		//
		// coins = (TextView) findViewById(R.id.money);
		// clock = new Clock(this, (TextView) findViewById(R.id.clock), coins);
		//
		// spinTime = (Spinner) findViewById(R.id.spinner_time);
		// spinPackage = (Spinner) findViewById(R.id.spinner_package);
		// spinDifficulty = (Spinner) findViewById(R.id.spinner_difficulty);
		//
		// graphView = (GraphView) findViewById(R.id.graph);
		//
		// sharedPrefsMoney = getSharedPreferences("Packages", 0);
		// sharedPrefsStats = getSharedPreferences("Stats", 0);
		// Money = new Coins(sharedPrefsMoney.getInt("money", 0), sharedPrefsMoney.getInt("paid_money", 0));
		//
		// initSpinners();
		// setGraph();
		//
		// MyApplication.getGaTracker().set(Fields.SCREEN_NAME, SCREEN_LABEL);
	}

	@Override
	public void onStart() {
		super.onStart();
		MyApplication.getGaTracker().send(MapBuilder.createAppView().build());
	}

	@Override
	public void onDestroy() {
		// clock.destroy();
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		sharedPrefsMoney = getActivity().getSharedPreferences("Packages", 0);
		sharedPrefsStats = getActivity().getSharedPreferences("Stats", 0);
		Money.setMoneyPaid(sharedPrefsMoney.getInt("paid_money", 0));
		Money.setMoney(sharedPrefsMoney.getInt("money", 0));
		// coins.setText(String.valueOf(Money.getMoney() + Money.getMoneyPaid()));
		// Money.increaseMoney(EggHelper.unlockEgg(getActivity(), coins, null, EggKeys[6], EggMaxValues[6]));
	}

	@Override
	public void onPause() {
		SharedPreferences.Editor editor = sharedPrefsMoney.edit();
		editor.putInt("money", Money.getMoney());
		editor.putInt("paid_money", Money.getMoneyPaid());
		editor.commit();
		super.onPause();
	}

	// @Override
	// public void onAttachedToWindow() {
	// getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	// getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	// }

	private void initSpinners() {
		packages = getUnlockedPackages();
		difficulties[0] = getString(R.string.all);
		for (int i = 1; i < difficulties.length; i++) {
			difficulties[i] = Difficulty.fromValueToString(i - 1);
		}
		ArrayAdapter<String> adapterTime = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, times) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView) v).setTypeface(typefaces.robotoLight);
				((TextView) v).setGravity(Gravity.CENTER);
				return v;
			}

			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View v = super.getDropDownView(position, convertView, parent);
				((TextView) v).setTypeface(typefaces.robotoLight);
				((TextView) v).setGravity(Gravity.CENTER);
				return v;
			}
		};
		ArrayAdapter<String> adapterPackages = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, packages) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView) v).setTypeface(typefaces.robotoLight);
				((TextView) v).setGravity(Gravity.CENTER);
				return v;
			}

			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View v = super.getDropDownView(position, convertView, parent);
				((TextView) v).setTypeface(typefaces.robotoLight);
				((TextView) v).setGravity(Gravity.CENTER);
				return v;
			}
		};
		ArrayAdapter<String> adapterDifficulties = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,
				difficulties) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView) v).setTypeface(typefaces.robotoLight);
				((TextView) v).setGravity(Gravity.CENTER);
				return v;
			}

			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View v = super.getDropDownView(position, convertView, parent);
				((TextView) v).setTypeface(typefaces.robotoLight);
				((TextView) v).setGravity(Gravity.CENTER);
				return v;
			}
		};
		adapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapterPackages.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapterDifficulties.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		Drawable drawable = getResources().getDrawable(R.drawable.dropdown);
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
		spinTime.setDropDownVerticalOffset(drawable.getIntrinsicHeight() + px + 5);
		spinPackage.setDropDownVerticalOffset(drawable.getIntrinsicHeight() + px + 5);
		spinDifficulty.setDropDownVerticalOffset(drawable.getIntrinsicHeight() + px + 5);
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
		if (sharedPrefsMoney.getBoolean("unlock_all", false)) {
			for (int i = 0; i < unlockPackageKeys.length - 1; i++) {
				list.add(displayPackageKeys.get(i));
			}
			list.addAll(displayCustomPackageKeys);
		} else {
			list.add(displayPackageKeys.get(0));
			for (int i = 1; i < unlockPackageKeys.length - 1; i++) {
				if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false))
					list.add(displayPackageKeys.get(i));
			}
			if (sharedPrefsMoney.getBoolean(unlockPackageKeys[unlockPackageKeys.length - 1], false))
				list.addAll(displayCustomPackageKeys);
		}
		return list;
	}

	private void setGraph() {
		sharedPrefsStats = getActivity().getSharedPreferences("Stats", 0);
		long oldestTime = getOldestTime();

		if (dbManager != null) {
			List<Integer> percent = dbManager.getStatPercentArray(oldestTime, selectedPackage, selectedDifficulty);
			int test[];
			if (percent.size() < 1) {
				test = new int[] { 50 };
			} else {
				test = new int[percent.size()];
				for (int i = 0; i < test.length; i++)
					test[i] = percent.get(i);
			}
			// graphView.setMovingAverage(rnd.nextInt(50) + 1);
			graphView.setArray(test);
		} else
			graphView.setArray(new int[] { 50 });
		int correct = sharedPrefsStats.getInt("correct", 0);
		int wrong = sharedPrefsStats.getInt("wrong", 0);
		int coins = sharedPrefsStats.getInt("coins", 0);
		int bestStreak = sharedPrefsStats.getInt("bestStreak", 0);
		int currentStreak = sharedPrefsStats.getInt("currentStreak", 0);
		long totalTime = sharedPrefsStats.getLong("totalTime", 0);
		long answerTimeFast = sharedPrefsStats.getLong("answerTimeFast", 0);
		long answerTimeAve = sharedPrefsStats.getLong("answerTimeAve", 0);
		int difficultyAve = sharedPrefsStats.getInt("difficultyAve", 0);

		String eggs = EggHelper.getNumberUnlocked(getActivity()) + " / " + EggHelper.getTotalEggs(getActivity());
		graphView.setStats(difficultyAve, correct, wrong, coins, totalTime, bestStreak, currentStreak, answerTimeFast, answerTimeAve, eggs);
	}

	private long getOldestTime() {
		return System.currentTimeMillis() - oldestTimes[spinTime.getSelectedItemPosition()];
	}
}
