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
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private TextView clock;
	private RadioGroup radioGroup1;
	// private RadioButton radioPhone;
	private RadioButton radioSilent;
	// private RadioButton radioSettings;
	private TextView problem;
	private RadioGroup radioGroup2;
	private RadioButton radioAnswer[] = new RadioButton[4];
	private Button buttonUnlock;
	private Button buttonSure;
	private ToggleButton quizMode;
	private int defaultTextColor;

	private String PackageKeys[] = { "enable_math", "enable_vocab", "enable_translate" };
	private int EnabledPackages = 0;
	private String DifficultyKeys[] = { "difficulty_math", "difficulty_vocab", "difficulty_translate" };
	private int answerLoc = 1;		// {correct radiobutton location}
	private String answers[] = { "3", "1", "2", "4" };	// {correct answer, wrong answers...}
	private int attempts = 1;
	private int dayCorrects = 0;
	private int dayWrongs = 0;

	private AudioManager am;
	private Random rand = new Random(); // Ideally just create one instance globally

	// private double pi = Math.PI;

	private SharedPreferences sharedPrefs;

	private Handler mHandler;

	public final BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {

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

		mHandler = new Handler();
		setContentView(R.layout.activity_main);

		clock = (TextView) findViewById(R.id.clock);
		problem = (TextView) findViewById(R.id.problem);
		defaultTextColor = problem.getTextColors().getDefaultColor();
		radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
		radioGroup2 = (RadioGroup) findViewById(R.id.radioGroup2);
		// radioPhone = (RadioButton) findViewById(R.id.radioPhone);
		radioSilent = (RadioButton) findViewById(R.id.radioSilent);
		// radioSettings = (RadioButton) findViewById(R.id.radioSettings);
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
		quizMode = (ToggleButton) findViewById(R.id.quizMode);

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

		IntentFilter c_intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		c_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		c_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
		this.registerReceiver(m_timeChangedReceiver, c_intentFilter);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		// sharedPrefs.edit().putBoolean(arg0, arg1)
		if (savedInstanceState != null) {
			quizMode.setChecked(savedInstanceState.getBoolean("Quiz"));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean("Quiz", quizMode.isChecked());
	}

	@Override
	public void onAttachedToWindow() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (m_timeChangedReceiver != null)
			this.unregisterReceiver(m_timeChangedReceiver);
	}

	@Override
	protected void onResume() {
		// ONLY WHEN SCREEN TURNS ON
		if (!ScreenReceiver.wasScreenOn) {
			// started as a lockscreen
		} else {
			// started by user
		}
		super.onResume();
		// get settings
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		// start background service to wait for screen to turn off
		EnabledPackages = getEnabledPackages();
		Intent sIntent = new Intent(this, ScreenService.class);
		if (EnabledPackages > 0) {
			buttonUnlock.setEnabled(false);
			this.startService(sIntent);
		} else {
			buttonUnlock.setEnabled(true);
			this.stopService(sIntent);
		}
		// reset attempts to first attempt
		attempts = 1;
		// setup the question and answer and display it
		setProblemAndAnswer(0);
	}

	@Override
	protected void onPause() {
		if (ScreenReceiver.wasScreenOn) {
			// THIS IS THE CASE WHEN ONPAUSE() IS CALLED BY THE SYSTEM DUE TO A SCREEN STATE CHANGE
		} else {
			// THIS IS WHEN ONPAUSE() IS CALLED WHEN THE SCREEN STATE HAS NOT CHANGED
		}
		super.onPause();
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
			}, 3000); // disable unlock and sure button after 3s
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
		if (EnabledPackages > 0)
			buttonSure.setEnabled(true);
		else
			this.finish();
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
			buttonUnlock.setEnabled(false);
			buttonSure.setEnabled(false);
			if (attempts >= Integer.parseInt(sharedPrefs.getString("max_tries", "1")) && !radioAnswer[answerLoc].isChecked()
					&& !quizMode.isChecked()) {
				displayCorrectOrNot("Wrong, Too many wrong answers", false);
				launchHomeScreen(2000);
			} else if (radioAnswer[answerLoc].isChecked() && quizMode.isChecked()) {
				displayCorrectOrNot("Correct!", true);
				setProblemAndAnswer(1000);
			} else if (radioAnswer[answerLoc].isChecked() && !quizMode.isChecked()) {
				launchHomeScreen(0);
			} else {
				displayCorrectOrNot("Wrong", false);
				if (!quizMode.isChecked())
					attempts++;
				setProblemAndAnswer(2000);
			}
			radioGroup2.clearCheck();
		}
	}

	private void launchHomeScreen(int delay) {
		/*Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);*/
		mHandler.removeCallbacksAndMessages(null);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		}, delay); // launch home screen after time [ms]
		// this.finish();

	}

	private void setProblemAndAnswer(int delay) {
		// int count = getEnabledPackages();
		// only create a question if more than 1 package is enabled
		if (EnabledPackages > 0) {
			final String EnabledPackageKeys[] = new String[EnabledPackages];
			final int location[] = new int[EnabledPackages];
			int count = 0;

			for (int i = 0; i < PackageKeys.length; i++) {
				if (sharedPrefs.getBoolean(PackageKeys[i], false)) {
					EnabledPackageKeys[count] = PackageKeys[i];
					location[count] = i;
					count++;
				}
			}
			mHandler.removeCallbacksAndMessages(null);
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// pick a random enabled package
					int randPack = rand.nextInt(EnabledPackageKeys.length);
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

					answerLoc = rand.nextInt(4);			// set a random location for the correct answer
					int offset = 1;
					for (int i = 0; i < 4; i++) {
						if (i == answerLoc) {
							radioAnswer[i].setText(answers[0]);
							offset = 0;
						} else {
							radioAnswer[i].setText(answers[i + offset]);
						}
					}
					problem.setTextColor(defaultTextColor);
				}
			}, delay); // set new problem after delay time [ms]

		} else {
			problem.setText(R.string.none_enabled);
			for (int i = 0; i < 4; i++) {
				radioAnswer[i].setText("N/A");
			}
		}
	}

	private void setMathProblem(int diffNum) {
		int operator = 0;
		int first = 1;
		int second = 1;

		switch (diffNum) {
		case 1:				// Easy question
			// add and subtract options
			operator = rand.nextInt(2);
			first = rand.nextInt(11);					// 0 through 10
			second = rand.nextInt(11);					// 0 through 10
			break;
		case 2:				// Medium question
			// add, subtract, multiply options
			operator = rand.nextInt(3);
			first = rand.nextInt(41) - 20;				// -20 through 20
			second = rand.nextInt(41) - 20;				// -20 through 20
			break;
		case 3:				// Hard question
			// add, subtract, multiply, and divide options
			operator = rand.nextInt(4);
			first = rand.nextInt(201) - 100;			// -100 through 100
			second = rand.nextInt(201) - 100;			// -100 through 100
			if (operator == 3) {
				// check that answer will be an integer
				while (first % second != 0) {
					first = rand.nextInt(201) - 100;	// new numbers
					second = rand.nextInt(201) - 100;
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
				int offset = rand.nextInt(21) - 10;
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

	private int getEnabledPackages() {
		int count = 0;
		for (int i = 0; i < PackageKeys.length; i++) {
			if (sharedPrefs.getBoolean(PackageKeys[i], false)) {
				count++;
			}
		}
		return count;
	}

	private void displayCorrectOrNot(String discription, boolean correct) {
		if (correct)
			problem.setTextColor(Color.GREEN);
		else
			problem.setTextColor(Color.RED);
		String s = discription + "\n" + problem.getText();
		problem.setText(s.substring(0, s.length() - 1) + radioAnswer[answerLoc].getText());
	}
}
