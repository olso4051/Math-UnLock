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
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.VocabQuestion;
import com.olyware.mathlock.utils.EZ;

public class MainActivity extends Activity {
	final private int startingPmoney = 1000;
	final private int multiplier = 5;
	final private int decreaseRate = 2000;
	private int money;
	private int Pmoney;
	private int difficulty = 0;
	private long startTime = 0;

	private LinearLayout layout;
	private TextView clock;
	final private float clockSize = 45, dateSize = 15;
	private float currentClockSize;
	private TextView coins, pack, worth;
	private TextView problem;
	// private TextView probAnswers;
	private AnswerView answerView;
	private boolean quizMode = false;
	private boolean silentMode;
	private JoystickView joystick;
	private int defaultTextColor;

	private String PackageKeys[] = { "enable_math", "enable_vocab", "enable_language", "enable_act", "enable_sat", "enable_gre",
			"enable_toddler", "enable_engineer" };
	private String unlockPackageKeys[] = { "unlock_all", "unlock_math", "unlock_vocab", "unlock_language", "unlock_act", "unlock_sat",
			"unlock_gre", "unlock_toddler", "unlock_engineer" };
	private String DifficultyKeys[] = { "difficulty_math", "difficulty_vocab", "difficulty_language", "difficulty_act", "difficulty_sat",
			"difficulty_gre", "difficulty_toddler", "difficulty_engineer" };
	private int EnabledPackages = 0;
	private boolean UnlockedPackages = false;

	private int answerLoc = 1;		// {correct answer location}
	private String answers[] = { "3", "1", "2", "4" };	// {correct answer, wrong answers...}
	private String answersRandom[] = { "4", "2", "3", "1" };	// {random answers}
	private int attempts = 1;

	private AudioManager am;
	private Vibrator vib;
	private Random rand = new Random(); // Ideally just create one instance globally

	private SharedPreferences sharedPrefs;
	private SharedPreferences sharedPrefsMoney;

	private Handler mHandler, timerHandler;
	private Runnable reduceWorth;

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

		mHandler = new Handler();
		dbManager = new DatabaseManager(getApplicationContext());
		setContentView(R.layout.activity_main);

		layout = (LinearLayout) findViewById(R.id.layout);

		clock = (TextView) findViewById(R.id.clock);
		clock.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				toggleClockDate();
			}
		});
		currentClockSize = clockSize;

		coins = (TextView) findViewById(R.id.money);
		pack = (TextView) findViewById(R.id.pack);
		worth = (TextView) findViewById(R.id.difficulty);
		problem = (TextView) findViewById(R.id.problem);
		defaultTextColor = problem.getTextColors().getDefaultColor();

		answerView = (AnswerView) findViewById(R.id.answers2);
		answerView.setReadyListener(new AnswerReadyListener() {
			@Override
			public void Ready() {
				if (!answerView.getAnswers().equals(answersRandom))
					answerView.setAnswers(answersRandom);
			}
		});
		joystick = (JoystickView) findViewById(R.id.joystick);
		joystick.setOnJostickSelectedListener(new JoystickSelectListener() {
			@Override
			public void OnSelect(int s) {
				JoystickSelected(s);
			}
		});

		timerHandler = new Handler();
		reduceWorth = new Runnable() {
			@Override
			public void run() {
				int d = Integer.parseInt(worth.getText().toString());
				d -= 1;
				if (d <= 0) {
					d = 0;
					worth.setText(String.valueOf(d));
				} else {
					worth.setText(String.valueOf(d));
					timerHandler.postDelayed(this, decreaseRate);
				}
			}
		};

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

		IntentFilter c_intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		c_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		c_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
		this.registerReceiver(m_timeChangedReceiver, c_intentFilter);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		if (sharedPrefs.getString("handed", getString(R.string.handed_default)).equals(getString(R.string.handed_default)))
			joystick.setLeftRightHanded(false);
		else
			joystick.setLeftRightHanded(true);
		joystick.setUnlockType((Integer.parseInt(sharedPrefs.getString("type", getString(R.string.type_default)))));

		SharedPreferences.Editor editor = sharedPrefsMoney.edit();
		if (sharedPrefsMoney.getBoolean("first", true)) {
			Pmoney = sharedPrefsMoney.getInt("paid_money", startingPmoney);
			editor.putBoolean("first", false).commit();
		} else
			Pmoney = sharedPrefsMoney.getInt("paid_money", 0);
		money = sharedPrefsMoney.getInt("money", 0);

		setMoney();

		if (savedInstanceState != null) {
			quizMode = joystick.setQuizMode(savedInstanceState.getBoolean("Quiz"));
			currentClockSize = savedInstanceState.getFloat("ClockSize");
		}
		setTime();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean("Quiz", quizMode);
		savedInstanceState.putFloat("ClockSize", currentClockSize);
	}

	@Override
	public void onAttachedToWindow() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		problem.setHeight(layout.getBottom() / 5);
		answerView.setParentHeight(layout.getBottom());
		super.onWindowFocusChanged(hasFocus);
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
		super.onResume();
		// get settings
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		money = sharedPrefsMoney.getInt("money", 0);
		Pmoney = sharedPrefsMoney.getInt("paid_money", 0);
		setMoney();

		// set the current handedness
		if (sharedPrefs.getString("handed", getString(R.string.handed_default)).equals(getString(R.string.handed_default)))
			joystick.setLeftRightHanded(false);
		else
			joystick.setLeftRightHanded(true);
		// set the unlock type
		joystick.setUnlockType((Integer.parseInt(sharedPrefs.getString("type", getString(R.string.type_default)))));
		// start background service to wait for screen to turn off
		getEnabledPackages();
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
		SharedPreferences.Editor editor = sharedPrefsMoney.edit();
		editor.putInt("money", money);
		editor.putInt("paid_money", Pmoney);
		editor.commit();
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
			joystick.setProblem(true);
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
					startTime = System.currentTimeMillis();
					timerHandler.removeCallbacks(reduceWorth);
					timerHandler.postDelayed(reduceWorth, decreaseRate);
					answerView.resetGuess();
					problem.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
					joystick.unPauseSelection();
					// pick a random enabled package
					int randPack = rand.nextInt(EnabledPackageKeys.length);
					difficulty = Integer.parseInt(sharedPrefs.getString(DifficultyKeys[location[randPack]], "1"));
					switch (location[randPack]) {
					case 0:			// math question
						setMathProblem(difficulty);
						break;
					case 1:			// vocabulary question
						setVocabProblem(difficulty);
						break;
					case 2:			// language question
						setLanguageProblem(difficulty);
						break;
					case 3:			// act question
						setACTProblem(difficulty);
						break;
					case 4:			// sat question
						setSATProblem(difficulty);
						break;
					case 5:			// gre question
						setGREProblem(difficulty);
						break;
					case 6:			// toddler question
						setToddlerProblem(difficulty);
						break;
					case 7:			// engineer question
						setEngineerProblem(difficulty);
						break;
					default:
						break;
					}

					answerLoc = rand.nextInt(4);			// set a random location for the correct answer
					int offset = 1;
					for (int i = 0; i < 4; i++) {
						if (i == answerLoc) {
							answersRandom[i] = answers[0];
							offset = 0;
						} else {
							answersRandom[i] = answers[i + offset];
						}
					}
					answerView.setAnswers(answersRandom);
					problem.setTextColor(defaultTextColor);
				}
			}, delay); // set new problem after delay time [ms]

		} else {
			answerView.resetGuess();
			joystick.setProblem(false);
			if (!UnlockedPackages)
				problem.setText(R.string.none_unlocked);
			else
				problem.setText(R.string.none_enabled);
			String temp[] = { "N/A", "N/A", "N/A", "N/A" };
			answersRandom = temp;
			for (int i = 0; i < 4; i++) {
				answerView.setAnswers(answersRandom);
			}
		}
	}

	private void setMathProblem(int diffNum) {
		pack.setText(R.string.unlock_math);
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

		if ((first <= 10) && (first >= 0) && (second <= 10) && (second >= 0))
			difficulty = 1;
		else if (((first <= 20) && (first >= -20)) || ((second <= 20) && (second >= -20)))
			difficulty = 2;
		else
			difficulty = 3;
		worth.setText(String.valueOf(difficulty * multiplier));

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

	// TODO: Pass in a Difficulty enum instead of an integer
	private void setVocabProblem(int diffNum) {
		pack.setText(R.string.unlock_vocab);
		// TODO: don't query the DB every time we display a question. Needs a cache.
		List<VocabQuestion> questions = dbManager.getVocabQuestions(Difficulty.fromValue(diffNum));

		// Get random question
		Random random = new Random();
		List<Integer> questionIndexes = EZ.list();
		int rand = random.nextInt(questions.size() - 1);
		questionIndexes.add(rand);
		VocabQuestion question = questions.get(rand);

		// Add the correct answer to the answers list
		List<String> answersList = EZ.list();
		answersList.add(question.getCorrectAnswer());

		// Set the new difficulty based on what question was picked
		difficulty = question.getDifficulty().getValue();
		worth.setText(String.valueOf(difficulty * multiplier));

		// Get 3 wrong answers and avoid duplicates
		for (int i = 0; i < 3; i++) {
			while (true) {
				rand = random.nextInt(questions.size());
				if (!questionIndexes.contains(rand)) {
					questionIndexes.add(rand);
					break;
				}
			}
			answersList.add(questions.get(rand).getCorrectAnswer());
		}

		// Display the vocab question and answers
		answers = answersList.toArray(new String[answersList.size()]);
		problem.setText("Define: " + question.getText());
	}

	private void setLanguageProblem(int diffNum) {
		pack.setText(R.string.unlock_language);
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

	private void setACTProblem(int diffNum) {
		pack.setText(R.string.unlock_act);
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

	private void setSATProblem(int diffNum) {
		pack.setText(R.string.unlock_sat);
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

	private void setGREProblem(int diffNum) {
		pack.setText(R.string.unlock_gre);
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

	private void setToddlerProblem(int diffNum) {
		pack.setText(R.string.unlock_toddler);
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

	private void setEngineerProblem(int diffNum) {
		pack.setText(R.string.unlock_engineer);
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

	private void getEnabledPackages() {
		int count = 0;
		if (sharedPrefsMoney.getBoolean(unlockPackageKeys[0], false)) {
			UnlockedPackages = true;
		}
		for (int i = 0; i < PackageKeys.length; i++) {
			if (sharedPrefs.getBoolean(PackageKeys[i], false)) {
				count++;
			}
			if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i + 1], false)) {
				UnlockedPackages = true;
			}
		}
		EnabledPackages = count;
	}

	private void displayCorrectOrNot(int correctLoc, int guessLoc, String discription, boolean correct, boolean unknown) {
		if (unknown) {
			answerView.setCorrectAnswer(correctLoc);
		} else {
			if (correct) {
				answerView.setCorrectAnswer(correctLoc);
				problem.setTextColor(Color.GREEN);
				money += Integer.parseInt(worth.getText().toString());
				;
			} else {
				answerView.setCorrectAnswer(correctLoc);
				answerView.setIncorrectGuess(guessLoc);
				problem.setTextColor(Color.RED);
				money -= Integer.parseInt(worth.getText().toString());
			}
			if (money < 0)
				money = 0;
			setMoney();
			problem.setText(discription + problem.getText());
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

	private void JoystickSelected(int s) {
		vib.vibrate(50);	// vibrate for 50ms
		switch (s) {
		case 0:		// A was selected
		case 1:		// B was selected
		case 2:		// C was selected
		case 3:		// D was selected
			long ms = System.currentTimeMillis() - startTime;
			float sec = ms / 1000f;
			// TODO store this in a new database table for statistics
			if (EnabledPackages == 0) {
				this.finish();
			} else if (attempts >= Integer.parseInt(sharedPrefs.getString("max_tries", "1")) && !(answerLoc == s) && !quizMode) {
				displayCorrectOrNot(answerLoc, s, "Too Many Wrong\n", false, false);
				joystick.pauseSelection();
				launchHomeScreen(3000);
			} else if ((answerLoc == s) && quizMode) {
				displayCorrectOrNot(answerLoc, s, "Correct!\n", true, false);
				joystick.pauseSelection();
				setProblemAndAnswer(1000);
			} else if ((answerLoc == s) && !quizMode) {
				displayCorrectOrNot(answerLoc, s, "Correct!\n", true, false);
				joystick.pauseSelection();
				launchHomeScreen(100);
			} else {
				displayCorrectOrNot(answerLoc, s, "Wrong\n", false, false);
				if (!quizMode)
					attempts++;
				joystick.setWrongGuess();
				joystick.pauseSelection();
				joystick.setOnTouchedListener(new JoystickTouchListener() {
					@Override
					public void OnTouch() {
						joystick.removeTouchListener();
						joystick.resetWrongGuess();
						setProblemAndAnswer(0);
					}
				});
			}
			break;
		case 4:
			displayCorrectOrNot(answerLoc, answerLoc, "", false, true);
			joystick.setWrongGuess();
			joystick.pauseSelection();
			joystick.setOnTouchedListener(new JoystickTouchListener() {
				@Override
				public void OnTouch() {
					joystick.removeTouchListener();
					joystick.resetWrongGuess();
					setProblemAndAnswer(0);
				}
			});
			break;
		case 5:		// sound/silent was selected
			if (silentMode) {
				am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			} else {
				am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			}
			silentMode = joystick.setSilentMode(!silentMode);

			break;
		case 6:		// Store was selected
			startActivity(new Intent(this, ShowStoreActivity.class));
			break;
		case 7:		// progress was selected
			startActivity(new Intent(this, ShowProgressActivity.class));
			break;
		case 8:		// quiz Mode was selected
			quizMode = joystick.setQuizMode(!quizMode);
			break;
		case 9:		// settings was selected
			startActivity(new Intent(this, ShowSettingsActivity.class));
			break;
		}
	}

	private void setMoney() {
		SharedPreferences.Editor editor = sharedPrefsMoney.edit();
		editor.putInt("money", money);
		editor.putInt("paid_money", Pmoney);
		editor.commit();
		coins.setText("" + (money + Pmoney));
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
