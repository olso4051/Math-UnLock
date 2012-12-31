package com.olyware.mathlock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
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
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {
	private TextView clock;
	private TextView date;
	private TextView problem;
	private TextView probAnswers;
	private boolean quizMode = false;
	private boolean silentMode;
	private JoystickView joystick;
	private int defaultTextColor;

	private String PackageKeys[] = { "enable_math", "enable_vocab", "enable_translate" };
	private boolean PackageDefaults[] = { true, false, false };
	private int EnabledPackages = 0;
	private String DifficultyKeys[] = { "difficulty_math", "difficulty_vocab", "difficulty_translate" };
	private int answerLoc = 1;		// {correct radiobutton location}
	private String answers[] = { "3", "1", "2", "4" };	// {correct answer, wrong answers...}
	private int attempts = 1;

	private AudioManager am;
	private Vibrator vib;
	private Random rand = new Random(); // Ideally just create one instance globally

	private SharedPreferences sharedPrefs;

	private Handler mHandler;

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

		mHandler = new Handler();
		setContentView(R.layout.activity_main);

		clock = (TextView) findViewById(R.id.clock);
		date = (TextView) findViewById(R.id.date);
		date.setGravity(Gravity.CENTER_VERTICAL);
		problem = (TextView) findViewById(R.id.problem);
		probAnswers = (TextView) findViewById(R.id.answers);
		defaultTextColor = problem.getTextColors().getDefaultColor();
		joystick = (JoystickView) findViewById(R.id.joystick);
		joystick.setOnJostickSelectedListener(new JoystickSelectListener() {
			@Override
			public void OnSelect(int s) {
				JoystickSelected(s);
			}
		});

		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		switch (am.getRingerMode()) {
		case AudioManager.RINGER_MODE_SILENT:
		case AudioManager.RINGER_MODE_VIBRATE:
			silentMode = joystick.setSilentMode(true);
			break;
		case AudioManager.RINGER_MODE_NORMAL:
			silentMode = joystick.setSilentMode(false);
			break;
		}
		vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		setTime();

		IntentFilter c_intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		c_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		c_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
		this.registerReceiver(m_timeChangedReceiver, c_intentFilter);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPrefs.getString("handed", getString(R.string.handed_default)).equals(getString(R.string.handed_default)))
			joystick.setLeftRightHanded(false);
		else
			joystick.setLeftRightHanded(true);
		joystick.setUnlockType((Integer.parseInt(sharedPrefs.getString("type", getString(R.string.type_default)))));

		if (savedInstanceState != null) {
			quizMode = joystick.setQuizMode(savedInstanceState.getBoolean("Quiz"));
		}

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean("Quiz", quizMode);
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
		// set the current handedness
		if (sharedPrefs.getString("handed", getString(R.string.handed_default)).equals(getString(R.string.handed_default)))
			joystick.setLeftRightHanded(false);
		else
			joystick.setLeftRightHanded(true);
		// set the unlocktype
		joystick.setUnlockType((Integer.parseInt(sharedPrefs.getString("type", getString(R.string.type_default)))));
		// start background service to wait for screen to turn off
		EnabledPackages = getEnabledPackages();
		Intent sIntent = new Intent(this, ScreenService.class);
		if (EnabledPackages > 0) {
			this.startService(sIntent);
		} else {
			this.stopService(sIntent);
		}
		// reset attempts to first attempt
		attempts = 1;
		// setup the question and answer and display it
		setProblemAndAnswer(0);
		joystick.showStartAnimation(0, 3000);
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

	private void launchHomeScreen(int delay) {
		mHandler.removeCallbacksAndMessages(null);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		}, delay); // launch home screen after delay time [ms]
	}

	private void setProblemAndAnswer(int delay) {
		if (EnabledPackages > 0) {
			final String EnabledPackageKeys[] = new String[EnabledPackages];
			final int location[] = new int[EnabledPackages];
			int count = 0;

			for (int i = 0; i < PackageKeys.length; i++) {
				if (sharedPrefs.getBoolean(PackageKeys[i], PackageDefaults[i])) {
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
					String temp[] = new String[4];
					for (int i = 0; i < 4; i++) {
						if (i == answerLoc) {
							temp[i] = answers[0];
							offset = 0;
						} else {
							temp[i] = answers[i + offset];
						}
					}
					probAnswers.setText(setAnswerText(temp));
					problem.setTextColor(defaultTextColor);
				}
			}, delay); // set new problem after delay time [ms]

		} else {
			problem.setText(R.string.none_enabled);
			String temp[] = { "N/A", "N/A", "N/A", "N/A" };
			for (int i = 0; i < 4; i++) {
				probAnswers.setText(setAnswerText(temp));
			}
		}
	}

	private Spanned setAnswerText(String ans[]) {
		int abcd = 4;
		int maxLength = 0;
		for (int i = 0; i < ans.length; i++) {
			if (ans[i].length() > maxLength)
				maxLength = ans[i].length();
		}
		if (maxLength < 4)
			abcd = 4;
		else if (maxLength < 10)
			abcd = 2;
		else
			abcd = 1;

		String sTemp = "<b>A</b>: <small>" + ans[0] + "</small>   ";
		if (abcd == 1)
			sTemp = sTemp + "<br/>";
		sTemp = sTemp + "<b>B</b>: <small>" + ans[1] + "</small> ";
		if (abcd <= 2)
			sTemp = sTemp + "<br/>";
		sTemp = sTemp + "<b>C</b>: <small>" + ans[2] + "</small> ";
		if (abcd == 1)
			sTemp = sTemp + "<br/>";
		sTemp = sTemp + "<b>D</b>: <small>" + ans[3] + "</small> ";

		return Html.fromHtml(sTemp);
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
				// check that answer will be an integer and not divide by zero
				while (second == 0) {
					second = rand.nextInt(201) - 100;
				}
				while (first % second != 0) {
					first = rand.nextInt(201) - 100;	// new numbers
					second = rand.nextInt(201) - 100;
					while (second == 0) {
						second = rand.nextInt(201) - 100;
					}
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
			if (sharedPrefs.getBoolean(PackageKeys[i], PackageDefaults[i])) {
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
		problem.setText(s.substring(0, s.length() - 1) + answers[0]);
	}

	@SuppressLint("SimpleDateFormat")
	private void setTime() {
		Date curDateTime = new Date(System.currentTimeMillis());
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
		SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE,\nMMMM d");
		clock.setText(Html.fromHtml(time + "<small><small><small>" + AMPMFormatter.format(curDateTime) + "</small></small></small>"));
		date.setText(dateFormatter.format(curDateTime));
	}

	private void JoystickSelected(int s) {
		vib.vibrate(50);	// vibrate for 50ms
		switch (s) {
		case 0:		// A was selected
		case 1:		// B was selected
		case 2:		// C was selected
		case 3:		// D was selected
			if (EnabledPackages == 0) {
				this.finish();
			} else if (attempts >= Integer.parseInt(sharedPrefs.getString("max_tries", "1")) && !(answerLoc == s) && !quizMode) {
				displayCorrectOrNot("Wrong, Too many wrong answers", false);
				launchHomeScreen(2000);
			} else if ((answerLoc == s) && quizMode) {
				displayCorrectOrNot("Correct!", true);
				setProblemAndAnswer(1000);
				// joystick.showStartAnimation();
			} else if ((answerLoc == s) && !quizMode) {
				launchHomeScreen(0);
			} else {
				displayCorrectOrNot("Wrong", false);
				if (!quizMode)
					attempts++;
				setProblemAndAnswer(2000);
			}
			break;
		case 4:		// sound/silent was selected
			if (silentMode) {
				am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			} else {
				am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			}
			silentMode = joystick.setSilentMode(!silentMode);

			break;
		case 5:		// Emergency was selected
			Intent i = new Intent(Intent.ACTION_DIAL, null);
			startActivity(i);
			break;
		case 6:		// quiz Mode was selected
			quizMode = joystick.setQuizMode(!quizMode);
			break;
		case 7:		// settings was selected
			startActivity(new Intent(this, ShowSettingsActivity.class));
			break;
		case 8:		// sidebar was selected
			startActivity(new Intent(this, ShowProgressActivity.class));
			break;
		}
	}
}
