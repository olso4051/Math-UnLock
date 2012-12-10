package com.olyware.mathlock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private TextView clock;
	private RadioGroup radioGroup1;
	private RadioButton radioPhone;
	private RadioButton radioSilent;
	private RadioButton radioSettings;
	private TextView problem;
	private RadioGroup radioGroup2;
	private RadioButton radioAnswer[] = new RadioButton[4];
	// private RadioButton radioAnswer2;
	// private RadioButton radioAnswer3;
	// private RadioButton radioAnswer4;
	private Button buttonUnlock;
	private Button buttonSure;
	private String PackageKeys[] = { "enable_math", "enable_vocab", "enable_translate" };
	private String DifficultyKeys[] = { "difficulty_math", "difficulty_vocab", "difficulty_translate" };
	private AudioManager am;
	private Random rng = new Random(); // Ideally just create one instance globally

	private int answerLoc = 1;		// {correct radiobutton location}
	private String answers[] = { "3", "1", "2", "4" };	// { answers}

	private double pi = Math.PI;

	private SharedPreferences sharedPrefs;

	private Handler mHandler;

	private final BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			boolean timeChange = (action.equals(Intent.ACTION_TIME_TICK) || action.equals(Intent.ACTION_TIME_CHANGED) || action
					.equals(Intent.ACTION_TIMEZONE_CHANGED));
			if (timeChange) {
				Date curDateTime = new Date(System.currentTimeMillis());
				// hour:minute am/pm newline Day, Month DayOfMonth
				SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a\n EEEE, MMMM d");
				clock.setText(formatter.format(curDateTime));
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		IntentFilter s_intentFilter = new IntentFilter();
		s_intentFilter.addAction(Intent.ACTION_TIME_TICK);
		s_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		s_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);

		mHandler = new Handler();
		setContentView(R.layout.activity_main);

		clock = (TextView) findViewById(R.id.clock);
		problem = (TextView) findViewById(R.id.problem);
		radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
		radioGroup2 = (RadioGroup) findViewById(R.id.radioGroup2);
		radioPhone = (RadioButton) findViewById(R.id.radioPhone);
		radioSilent = (RadioButton) findViewById(R.id.radioSilent);
		radioSettings = (RadioButton) findViewById(R.id.radioSettings);
		radioAnswer[0] = (RadioButton) findViewById(R.id.radioAnswer1);
		radioAnswer[1] = (RadioButton) findViewById(R.id.radioAnswer2);
		radioAnswer[2] = (RadioButton) findViewById(R.id.radioAnswer3);
		radioAnswer[3] = (RadioButton) findViewById(R.id.radioAnswer4);
		buttonUnlock = (Button) findViewById(R.id.buttonUnlock);
		buttonUnlock.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buttonUnlockClick();
			}
		});
		buttonSure = (Button) findViewById(R.id.buttonSure);
		buttonSure.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buttonSureClick();
			}
		});
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		switch (am.getRingerMode()) {
		case AudioManager.RINGER_MODE_SILENT:
			radioSilent.setText(getString(R.string.sound));
			break;
		case AudioManager.RINGER_MODE_VIBRATE:
			radioSilent.setText(getString(R.string.sound));
			break;
		case AudioManager.RINGER_MODE_NORMAL:
			radioSilent.setText(getString(R.string.silent));
			break;
		}
		Date curDateTime = new Date(System.currentTimeMillis());
		// hour:minute am/pm newline Day, Month DayOfMonth
		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a\n EEEE, MMMM d");
		clock.setText(formatter.format(curDateTime));

		registerReceiver(m_timeChangedReceiver, s_intentFilter);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		// TODO set problem, answer, and wrong answers
		setProblemAndAnswer();
	}

	@Override
	protected void onResume() {
		super.onResume();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onRadioButtonClicked2(View view) {
		if (!buttonUnlock.isEnabled()) {
			buttonUnlock.setEnabled(true);
			mHandler.removeCallbacksAndMessages(null);
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					buttonUnlock.setEnabled(false);
					buttonSure.setEnabled(false);
					radioGroup2.clearCheck();
				}
			}, 3000); // disable unlock button after 3s
		}
		buttonSure.setEnabled(false);
		radioGroup1.clearCheck();
	}

	public void onRadioButtonClicked1(View view) {
		if (!buttonSure.isEnabled() || buttonUnlock.isEnabled()) {
			buttonSure.setEnabled(true);
			mHandler.removeCallbacksAndMessages(null);
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					buttonSure.setEnabled(false);
					radioGroup1.clearCheck();
				}
			}, 3000); // disable sure button after 3s
		}
		buttonUnlock.setEnabled(false);
		radioGroup2.clearCheck();
	}

	private void buttonUnlockClick() {
		buttonSure.setEnabled(true);
	}

	private void buttonSureClick() {
		if (!buttonUnlock.isEnabled()) {
			switch (radioGroup1.getCheckedRadioButtonId()) {
			case R.id.radioPhone:
				Intent i = new Intent(Intent.ACTION_DIAL, null);
				startActivity(i);
				break;
			case R.id.radioSilent:
				if (radioSilent.getText().toString().equals(getString(R.string.silent))) {
					am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
					radioSilent.setText(getString(R.string.sound));
				} else {
					am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					radioSilent.setText(getString(R.string.silent));
				}
				break;
			case R.id.radioSettings:
				startActivity(new Intent(this, ShowSettingsActivity.class));
				break;
			default:
				break;
			}
			buttonSure.setEnabled(false);
			radioGroup1.clearCheck();
		} else {
			// TODO check if answer is correct
			buttonUnlock.setEnabled(false);
			buttonSure.setEnabled(false);
			radioGroup2.clearCheck();
			launchHomeScreen();
		}
	}

	private void launchHomeScreen() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}

	private void setProblemAndAnswer() {
		int count = 0;

		// count enabled packages
		for (int i = 0; i < PackageKeys.length; i++) {
			if (sharedPrefs.getBoolean(PackageKeys[i], false)) {
				count++;
			}
		}
		// only create a question if more than 1 package is enabled
		if (count > 0) {
			String EnabledPackageKeys[] = new String[count];
			int location[] = new int[count];
			count = 0;

			for (int i = 0; i < PackageKeys.length; i++) {
				if (sharedPrefs.getBoolean("enable_math", false)) {
					EnabledPackageKeys[count] = PackageKeys[i];
					location[count] = i;
					count++;
				}
			}

			// pick a random enabled package
			int randPack = rng.nextInt(EnabledPackageKeys.length);
			switch (location[randPack]) {
			case 0:			// math question
				setMathProblem(Integer.parseInt(sharedPrefs.getString(DifficultyKeys[0], "1")));
				break;
			case 1:			// vocabulary question
				setVocabProblem(Integer.parseInt(sharedPrefs.getString(DifficultyKeys[1], "1")));
				break;
			case 2:			// translate question
				setTranslateProblem(Integer.parseInt(sharedPrefs.getString(DifficultyKeys[2], "1")));
				break;
			default:
				break;
			}

			answerLoc = rng.nextInt(3);			// set a random location for the correct answer
			int offset = 1;
			for (int i = 0; i < 4; i++) {
				if (i == answerLoc) {
					radioAnswer[i].setText(answers[0]);
					offset = 0;
				} else {
					radioAnswer[i].setText(answers[i + offset]);
				}
			}
		}
	}

	private void setMathProblem(int diffNum) {
		int operator = 0;
		int first = 1;
		int second = 1;
		Toast.makeText(this, String.valueOf(diffNum), Toast.LENGTH_SHORT).show();

		switch (diffNum) {
		case 1:				// Easy question
			// add and subtract options
			operator = rng.nextInt(1);
			first = rng.nextInt(10);					// 0 through 10
			second = rng.nextInt(10);					// 0 through 10
			break;
		case 2:				// Medium question
			// add, subtract, multiply options
			operator = rng.nextInt(2);
			first = rng.nextInt(40) - 20;				// -20 through 20
			second = rng.nextInt(40) - 20;				// -20 through 20
			break;
		case 3:				// Hard question
			// add, subtract, multiply, and divide options
			operator = rng.nextInt(3);
			first = rng.nextInt(200) - 100;				// -100 through 100
			second = rng.nextInt(200) - 100;			// -100 through 100
			if (operator == 3) {
				// check that answer will be an integer
				while (first % second != 0) {
					first = rng.nextInt(200) - 100;		// new numbers
					second = rng.nextInt(200) - 100;
				}
			}
			break;
		}

		switch (operator) {
		case 0:			// add
			answers[0] = String.valueOf(first + second);
			problem.setText(String.valueOf(first) + " + " + String.valueOf(second) + " = ?");
			break;
		case 1:			// subtract
			answers[0] = String.valueOf(first - second);
			problem.setText(String.valueOf(first) + " - " + String.valueOf(second) + " = ?");
			break;
		case 2:			// multiply
			answers[0] = String.valueOf(first * second);
			problem.setText(String.valueOf(first) + " * " + String.valueOf(second) + " = ?");
			break;
		case 3:			// divide
			answers[0] = String.valueOf(first / second);
			problem.setText(String.valueOf(first) + " / " + String.valueOf(second) + " = ?");
			break;
		}

		// generate 3 random numbers to add to correct answer, not equal to zero or themselves
		List<Integer> generated = new ArrayList<Integer>();
		for (int i = 0; i < 3; i++) {
			while (true) {
				int offset = rng.nextInt(20) - 10;
				if (!generated.contains(offset) && offset != 0) {
					// Done for this iteration
					generated.add(offset);
					answers[i + 1] = String.valueOf(offset + Integer.parseInt(answers[0]));
					break;
				}
			}
		}
	}

	private void setVocabProblem(int diffNum) {
		switch (diffNum) {
		case 1:				// Easy question
			break;
		case 2:				// Medium question
			break;
		case 3:				// Hard question
			break;
		default:
			break;
		}
	}

	private void setTranslateProblem(int diffNum) {
		switch (diffNum) {
		case 1:				// Easy question
			break;
		case 2:				// Medium question
			break;
		case 3:				// Hard question
			break;
		default:
			break;
		}
	}
}
