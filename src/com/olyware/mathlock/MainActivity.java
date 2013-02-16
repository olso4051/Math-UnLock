package com.olyware.mathlock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
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
import com.olyware.mathlock.model.EngineerQuestion;
import com.olyware.mathlock.model.HighQTriviaQuestion;
import com.olyware.mathlock.model.LanguageQuestion;
import com.olyware.mathlock.model.MathQuestion;
import com.olyware.mathlock.model.Statistic;
import com.olyware.mathlock.model.VocabQuestion;
import com.olyware.mathlock.utils.ShareHelper;
import com.olyware.mathlock.views.AnswerReadyListener;
import com.olyware.mathlock.views.AnswerView;
import com.olyware.mathlock.views.AutoResizeTextView;
import com.olyware.mathlock.views.JoystickSelectListener;
import com.olyware.mathlock.views.JoystickTouchListener;
import com.olyware.mathlock.views.JoystickView;

public class MainActivity extends Activity {
	final private int multiplier = 5;
	final private int decreaseRate = 1000;
	final private int startingPmoney = 1000;
	private int money;
	private int Pmoney;
	private int dMoney;// change in money after a question is answered
	private int difficulty = 0;
	private long startTime = 0;

	private LinearLayout layout;
	private TextView clock;
	final private float clockSize = 40, dateSize = 20;
	private float currentClockSize;
	private TextView coins, worth;
	private int questionWorth;
	private AutoResizeTextView problem;
	private Drawable imageLeft;	// left,top,right,bottom
	private AnswerView answerView;
	private boolean quizMode = false;
	private JoystickView joystick;
	private int defaultTextColor;

	private String PackageKeys[], unlockPackageKeys[], DifficultyKeys[], LanguageEntries[], LanguageValues[];
	private String currentPack, currentTableName, fromLanguage, toLanguage;
	private int ID;

	private int EnabledPackages = 0;
	private boolean EnabledPacks[];
	private boolean UnlockedPackages = false;
	private boolean dialogOn = false;
	private boolean dontShow = false;
	final private long MONTH = 2592000000l;

	private int answerLoc = 1;		// {correct answer location}
	private String answers[] = { "3", "1", "2", "4" };	// {correct answer, wrong answers...}
	private String answersRandom[] = { "4", "2", "3", "1" };	// {answers in random order}
	private int attempts = 1;

	private Vibrator vib;
	private Random rand = new Random(); // Ideally just create one instance globally

	private SharedPreferences sharedPrefs, sharedPrefsMoney, sharedPrefsStats, sharedPrefsEggs;
	private SharedPreferences.Editor editorPrefsMoney, editorPrefsStats, editorPrefsEggs;

	private Handler mHandler, timerHandler;
	private Runnable reduceWorth;
	private boolean attached = false;

	private DatabaseManager dbManager;
	private static Context ctx;

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

	public static Context getContext() {
		return ctx;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPrefs.getBoolean("notification_bar", true)) {
			setTheme(R.style.AppTheme2);
		} else {
			setTheme(R.style.AppTheme);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		layout = (LinearLayout) findViewById(R.id.layout);

		mHandler = new Handler();
		dbManager = new DatabaseManager(getApplicationContext());
		ctx = this;

		PackageKeys = getResources().getStringArray(R.array.enable_package_keys);
		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		DifficultyKeys = getResources().getStringArray(R.array.difficulty_keys);
		LanguageValues = getResources().getStringArray(R.array.language_values_not_localized);
		EnabledPacks = new boolean[PackageKeys.length];

		clock = (TextView) findViewById(R.id.clock);
		clock.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				toggleClockDate();
			}
		});
		currentClockSize = clockSize;

		coins = (TextView) findViewById(R.id.money);
		worth = (TextView) findViewById(R.id.worth);
		problem = (AutoResizeTextView) findViewById(R.id.problem);
		defaultTextColor = problem.getTextColors().getDefaultColor();

		answerView = (AnswerView) findViewById(R.id.answers2);
		answerView.setColor(defaultTextColor);
		answerView.setReadyListener(new AnswerReadyListener() {
			@Override
			public void Ready() {
				answerView.setAnswers(answersRandom);
				setImage();
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
				questionWorth -= 1;
				if (questionWorth <= 0) {
					questionWorth = 0;
					if (attached)
						getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				} else {
					timerHandler.postDelayed(this, decreaseRate);
					if (attached)
						getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
				worth.setText(String.valueOf(questionWorth));
			}
		};

		vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		IntentFilter c_intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		c_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		c_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
		this.registerReceiver(m_timeChangedReceiver, c_intentFilter);

		if (savedInstanceState != null) {
			quizMode = joystick.setQuizMode(savedInstanceState.getBoolean("Quiz"));
			currentClockSize = savedInstanceState.getFloat("ClockSize");
		}

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		sharedPrefsStats = getSharedPreferences("Stats", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsStats = sharedPrefsStats.edit();
		for (int i = 0; i < EnabledPacks.length; i++)
			EnabledPacks[i] = false;
		showWallpaper();
		getEnabledPackages();
		setProblemAndAnswer(0);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean("Quiz", quizMode);
		savedInstanceState.putFloat("ClockSize", currentClockSize);
	}

	@Override
	public void onAttachedToWindow() {
		attached = true;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// at this point the activity has been measured and we can get the height
		// now we can set a max height for answerView since it is dynamic
		setImage();
		if (hasFocus) {
			showWallpaper();
			answerView.setParentHeight(layout.getBottom());
		}
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onStop() {
		if (attached)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
		sharedPrefsStats = getSharedPreferences("Stats", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsStats = sharedPrefsStats.edit();
		if (sharedPrefsMoney.getBoolean("first", true)) {
			Pmoney = sharedPrefsMoney.getInt("paid_money", startingPmoney);
			editorPrefsMoney.putLong("lastTime", System.currentTimeMillis() - MONTH * 3 / 4);// give them a week before asking
			editorPrefsMoney.putBoolean("first", false);
			editorPrefsMoney.commit();
		} else
			Pmoney = sharedPrefsMoney.getInt("paid_money", 0);
		money = sharedPrefsMoney.getInt("money", 0);

		// set the clock to the correct time
		setTime();
		// set the current handedness
		if (sharedPrefs.getString("handed", getString(R.string.handed_default)).equals(getString(R.string.handed_default)))
			joystick.setLeftRightHanded(false);
		else
			joystick.setLeftRightHanded(true);
		// set the unlock type
		joystick.setUnlockType((java.lang.Integer.parseInt(sharedPrefs.getString("type", getString(R.string.type_default)))));

		// get the localized language entries
		LanguageEntries = getResources().getStringArray(R.array.language_entries);

		// reset attempts to first attempt
		attempts = 1;

		// get unlocked and enabled item changes
		boolean changed = getEnabledPackages();

		// start background service to wait for screen to turn off
		Intent sIntent = new Intent(this, ScreenService.class);
		if (EnabledPackages > 0) {
			this.startService(sIntent);
		} else {
			this.stopService(sIntent);
		}

		// setup the question and answers
		if (changed)
			setProblemAndAnswer(0);
		else if (!UnlockedPackages)
			displayInfo(true);
		else if ((!sharedPrefsMoney.getBoolean("dontShowLastTime", false))
				&& (sharedPrefsMoney.getLong("lastTime", 0) <= System.currentTimeMillis() - MONTH))
			displayRateShare();
		else
			resetTimes();
		// show the settings bar and slide it down after 3 seconds
		joystick.showStartAnimation(0, 3000);
		// save money into shared preferences
		setMoney();
		// set image if it was set when the screen was off
		setImage();
	}

	@Override
	protected void onPause() {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		if (attached)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		editorPrefsMoney.putInt("money", money);
		editorPrefsMoney.putInt("paid_money", Pmoney);
		if (!sharedPrefsMoney.getBoolean("dontShowLastTime", false))
			editorPrefsMoney.putBoolean("dontShowLastTime", dontShow);
		editorPrefsMoney.commit();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		int worth = (difficulty + 1) * multiplier;
		if (money + Pmoney < worth) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.not_enough_coins)).setCancelable(false);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// nothing to do
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			money -= worth;
			if (money < 0) {
				Pmoney += money;
				money = 0;
			}
			setMoney();
			super.onBackPressed();
		}
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		homeTest();
	}

	private void homeTest() {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
		if (recentTasks.get(1).baseActivity.toShortString().indexOf(getPackageName()) > -1) {
			// TODO test on multiple devices I think this is when Home is pressed, can't stop from executing exit code however
			money -= (difficulty + 1) * multiplier;
			money = Math.max(0, money);
			setMoney();
			super.onBackPressed();
		}
	}

	private void launchHomeScreen(int delay) {
		mHandler.removeCallbacksAndMessages(null);
		timerHandler.removeCallbacks(reduceWorth);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		}, delay); // launch home screen after delay time [ms]
	}

	@SuppressWarnings("deprecation")
	private void showWallpaper() {
		if (sharedPrefs.getBoolean("enable_wallpaper", true) && layout.getWidth() > 0) {
			// get wallpaper as a bitmap
			Bitmap bitmap = ((BitmapDrawable) WallpaperManager.getInstance(this).getDrawable()).getBitmap();

			// set scaling factors
			int left = bitmap.getWidth() / 2 - layout.getWidth() / 2;
			// int right = drawable.getIntrinsicWidth() / 2 + layout.getWidth() / 2;
			int top = bitmap.getHeight() / 2 - layout.getHeight() / 2;
			// int bottom = drawable.getIntrinsicHeight() / 2 + layout.getHeight() / 2;

			// scale the bitmap to fit on the background
			bitmap = Bitmap.createBitmap(bitmap, left, top, layout.getWidth(), layout.getHeight());
			// convert bitmap to BitmapDrawable so we can set it as the background
			BitmapDrawable Bdrawable = new BitmapDrawable(getResources(), bitmap);
			Bdrawable.setAlpha(100);
			layout.setBackgroundDrawable(Bdrawable);
		} else
			layout.setBackgroundDrawable(null);
	}

	private void setImage() {
		// use the image height and width to set the bounds and not stretch the image
		// int h = image.getIntrinsicHeight();
		// int w = image.getIntrinsicWidth();
		if (imageLeft == null)
			problem.setCompoundDrawables(null, null, null, null);
		else {
			imageLeft.setBounds(0, 0, problem.getHeight() /* *w/h */, problem.getHeight());
			problem.setCompoundDrawables(imageLeft, null, null, null);
		}
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
					questionWorth = 0;
					answerView.resetGuess();
					imageLeft = null;
					problem.setCompoundDrawables(imageLeft, null, null, null);
					joystick.unPauseSelection();

					// pick a random enabled package
					int randPack = rand.nextInt(EnabledPackageKeys.length);
					String key = DifficultyKeys[location[randPack]];
					String defaultDiff = "0";
					if (key.equals("difficulty_act_sat"))
						defaultDiff = "2";
					else if (key.equals("difficulty_gre"))
						defaultDiff = "4";

					// get the difficulty from the selected package
					difficulty = Integer.parseInt(sharedPrefs.getString(key, defaultDiff));
					switch (location[randPack]) {
					case 0:			// math question
						currentPack = getString(R.string.math);
						setMathProblem(0, difficulty);
						break;
					case 1:			// vocabulary question
						currentPack = getString(R.string.vocab);
						setVocabProblem(0, difficulty);
						break;
					case 2:			// language question
						currentPack = getString(R.string.language);
						setLanguageProblem(difficulty);
						break;
					case 3:			// enabled vocab act/sat question
					case 4:			// enabled math act/sat question
						currentPack = getString(R.string.act_sat);
						setACT_SATProblem(difficulty, sharedPrefs.getBoolean(PackageKeys[3], false),
								sharedPrefs.getBoolean(PackageKeys[4], false));
						break;
					case 5:			// gre vocab question
					case 6:			// gre math question
						currentPack = getString(R.string.gre);
						setGREProblem(difficulty, sharedPrefs.getBoolean(PackageKeys[5], false),
								sharedPrefs.getBoolean(PackageKeys[6], false));
						break;
					case 7:			// toddler question
						currentPack = getString(R.string.toddler);
						setToddlerProblem(difficulty);
						break;
					case 8:			// engineer question
						currentPack = getString(R.string.engineer);
						setEngineerProblem(difficulty);
						break;
					case 9:			// HighQ Trivia question
						currentPack = getString(R.string.engineer);
						setHighQTriviaProblem(difficulty);
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
					resetTimes();
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

	private void resetTimes() {
		startTime = System.currentTimeMillis();
		questionWorth = (difficulty + 1) * multiplier;
		worth.setText(String.valueOf(questionWorth));
		timerHandler.removeCallbacks(reduceWorth);
		timerHandler.postDelayed(reduceWorth, decreaseRate);
	}

	private void setMathProblem(int minDifficulty, int maxDifficulty) {
		int operator = 0;
		int first = 1;
		int second = 1;

		if (minDifficulty == 0)
			difficulty = rand.nextInt(maxDifficulty + 1);
		else
			difficulty = rand.nextInt(maxDifficulty - minDifficulty + 1) + minDifficulty;
		currentTableName = null;
		fromLanguage = null;
		toLanguage = null;
		ID = 0;
		switch (difficulty) {
		case 0:				// Elementary
			// add and subtract options
			operator = rand.nextInt(2);
			first = rand.nextInt(11);					// 0 through 10
			second = rand.nextInt(11);					// 0 through 10
			break;
		case 1:				// Middle School
			// add, subtract, multiply options
			operator = rand.nextInt(3);
			first = rand.nextInt(41) - 20;				// -20 through 20
			second = rand.nextInt(41) - 20;				// -20 through 20
			break;
		case 2:				// High School (basic)
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
		case 3:				// High School (advanced)
		case 4:				// College (basic)
		case 5:				// College (advanced)
			currentTableName = getString(R.string.math_table);
			fromLanguage = null;
			toLanguage = null;
			MathQuestion question = dbManager.getMathQuestion(Difficulty.fromValue(minDifficulty), Difficulty.fromValue(maxDifficulty));
			ID = question.getID();
			question.setVariables();
			// Set the new difficulty based on what question was picked
			difficulty = question.getDifficulty().getValue();

			if (!question.getImage().equals("none")) {
				int id = getResources().getIdentifier(question.getImage(), "drawable", getPackageName());
				imageLeft = getResources().getDrawable(id);
				setImage();
			}
			problem.setText(question.getQuestionText());
			answers = question.getAnswers();
			return;
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

	// TODO: Pass in a Difficulty enum instead of an integer
	private void setVocabProblem(int minDifficulty, int maxDifficulty) {
		currentTableName = getString(R.string.vocab_table);
		fromLanguage = null;
		toLanguage = null;
		// TODO: don't query the DB every time we display a question. Needs a cache.
		List<VocabQuestion> questions = dbManager.getVocabQuestions(Difficulty.fromValue(minDifficulty),
				Difficulty.fromValue(maxDifficulty), answers.length);
		ID = questions.get(0).getID();

		// Set the new difficulty based on what question was picked
		difficulty = questions.get(0).getDifficulty().getValue();

		// Display the vocab question and answers
		for (int i = 0; i < answers.length; i++) {
			answers[i] = questions.get(i).getCorrectAnswer();
		}
		problem.setText("Define: " + questions.get(0).getQuestionText());
	}

	private void setLanguageProblem(int diffNum) {
		// TODO: don't query the DB every time we display a question. Needs a cache.
		currentTableName = getString(R.string.language_table);
		fromLanguage = sharedPrefs.getString("from_language", getString(R.string.language_from_default));
		toLanguage = sharedPrefs.getString("to_language", getString(R.string.language_to_default));
		// String fromLanguage = sharedPrefs.getString("from_language", getString(R.string.language_from_default));
		// String toLanguage = sharedPrefs.getString("to_language", getString(R.string.language_to_default));
		String fromLanguageLocal = fromLanguage, toLanguageLocal = toLanguage;
		for (int i = 0; i < LanguageValues.length; i++) {
			if (LanguageValues[i].equals(fromLanguage))
				fromLanguageLocal = LanguageEntries[i];
			else if (LanguageValues[i].equals(toLanguage))
				toLanguageLocal = LanguageEntries[i];
		}
		List<LanguageQuestion> questions = dbManager.getLanguageQuestions(Difficulty.fromValue(diffNum), answers.length, fromLanguage,
				toLanguage);
		ID = questions.get(0).getID();

		// Set the new difficulty based on what question was picked
		difficulty = questions.get(0).getDifficulty().getValue();

		// Display the vocab question and answers
		for (int i = 0; i < answers.length; i++) {
			answers[i] = questions.get(i).getCorrectAnswer();
		}
		problem.setText(fromLanguageLocal + " -> " + toLanguageLocal + "\n" + questions.get(0).getQuestionText());
	}

	private void setACT_SATProblem(int diffNum, boolean vocab, boolean math) {
		int type;// 0-vocab,1-math
		if ((vocab) && (math))
			type = rand.nextInt(2);
		else if (vocab)
			type = 0;
		else
			type = 1;
		if (type == 0)
			setVocabProblem(2, diffNum);
		else
			setMathProblem(2, diffNum);

	}

	private void setGREProblem(int diffNum, boolean vocab, boolean math) {
		int type;// 0-vocab,1-math
		if ((vocab) && (math))
			type = rand.nextInt(2);
		else if (vocab)
			type = 0;
		else
			type = 1;
		if (type == 0)
			setVocabProblem(4, diffNum);
		else
			setMathProblem(4, diffNum);
	}

	private void setToddlerProblem(int diffNum) {
		currentTableName = null;
		fromLanguage = null;
		toLanguage = null;
		ID = 0;
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
		currentTableName = getString(R.string.engineer_table);
		fromLanguage = null;
		toLanguage = null;
		EngineerQuestion question = dbManager.getEngineerQuestion(Difficulty.fromValue(diffNum));
		ID = question.getID();

		// Set the new difficulty based on what question was picked
		difficulty = question.getDifficulty().getValue();

		problem.setText(question.getQuestionText());
		answers = question.getAnswers();
		return;
	}

	private void setHighQTriviaProblem(int diffNum) {
		currentTableName = getString(R.string.highq_trivia_table);
		fromLanguage = null;
		toLanguage = null;
		HighQTriviaQuestion question = dbManager.getHighQTriviaQuestion(Difficulty.fromValue(diffNum));
		ID = question.getID();

		// Set the new difficulty based on what question was picked
		difficulty = question.getDifficulty().getValue();

		problem.setText(question.getQuestionText());
		answers = question.getAnswers();
		return;
	}

	private boolean getEnabledPackages() {
		int count = 0;
		boolean changed = false;
		boolean EnabledPacksBefore[] = new boolean[EnabledPacks.length];
		for (int i = 0; i < EnabledPacks.length; i++)
			EnabledPacksBefore[i] = EnabledPacks[i];

		for (int i = 0; i < unlockPackageKeys.length; i++)
			if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false)) {
				UnlockedPackages = true;
			}

		for (int i = 0; i < PackageKeys.length; i++) {
			if (sharedPrefs.getBoolean(PackageKeys[i], false)) {
				EnabledPacks[i] = true;
				count++;
			} else
				EnabledPacks[i] = false;
			if (EnabledPacksBefore[i] != EnabledPacks[i])
				changed = true;
		}
		EnabledPackages = count;
		return changed;
	}

	private void displayCorrectOrNot(int correctLoc, int guessLoc, String discription, boolean correct, boolean unknown) {
		int tempMoney = money;
		if (unknown) {
			answerView.setCorrectAnswer(correctLoc);
		} else {
			if (correct) {
				answerView.setCorrectAnswer(correctLoc);
				problem.setTextColor(Color.GREEN);
				money += questionWorth;// Integer.parseInt(worth.getText().toString());
				dbManager.addStat(new Statistic(currentPack, String.valueOf(true), Difficulty.fromValue(difficulty), System
						.currentTimeMillis()));
				dbManager.decreasePriority(currentTableName, fromLanguage, fromLanguage, ID);
			} else {
				answerView.setCorrectAnswer(correctLoc);
				answerView.setIncorrectGuess(guessLoc);
				problem.setTextColor(Color.RED);
				money -= questionWorth;// Integer.parseInt(worth.getText().toString());
				dbManager.addStat(new Statistic(currentPack, String.valueOf(false), Difficulty.fromValue(difficulty), System
						.currentTimeMillis()));
				dbManager.increasePriority(currentTableName, fromLanguage, fromLanguage, ID);
			}
			if (money < 0)
				money = 0;
			dMoney = money - tempMoney;
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
		dialogOn = false;
		if (sharedPrefs.getBoolean("vibration", true)
				&& ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode() != AudioManager.RINGER_MODE_SILENT)
			vib.vibrate(50);	// vibrate for 50ms
		int maxAttempts = Integer.parseInt(sharedPrefs.getString("max_tries", "1"));
		switch (s) {
		case 0:		// A was selected
		case 1:		// B was selected
		case 2:		// C was selected
		case 3:		// D was selected
			timerHandler.removeCallbacks(reduceWorth);
			if (EnabledPackages == 0) {
				this.finish();
			} else if (attempts >= maxAttempts && !(answerLoc == s) && !quizMode && maxAttempts < 4) {
				displayCorrectOrNot(answerLoc, s, "Too Many Wrong\n", false, false);
				updateStats(false);
				joystick.pauseSelection();
				launchHomeScreen(3000);
			} else if ((answerLoc == s) && quizMode) {
				displayCorrectOrNot(answerLoc, s, "Correct!\n", true, false);
				updateStats(true);
				joystick.pauseSelection();
				setProblemAndAnswer(1000);
			} else if ((answerLoc == s) && !quizMode) {
				displayCorrectOrNot(answerLoc, s, "Correct!\n", true, false);
				updateStats(true);
				joystick.pauseSelection();
				launchHomeScreen(100);
			} else {
				displayCorrectOrNot(answerLoc, s, "Wrong\n", false, false);
				updateStats(false);
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
			unlockEgg("unkown", 500);
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
		case 5:		// info was selected
			unlockEgg("info", 500);
			displayInfo(false);
			break;
		case 6:		// Store was selected
			unlockEgg("store", 1000);
			startActivity(new Intent(this, ShowStoreActivity.class));
			break;
		case 7:		// progress was selected
			unlockEgg("progress", 500);
			startActivity(new Intent(this, ShowProgressActivity.class));
			break;
		case 8:		// quiz Mode was selected
			unlockEgg("quiz", 1000);
			quizMode = joystick.setQuizMode(!quizMode);
			break;
		case 9:		// settings was selected
			unlockEgg("settings", 500);
			startActivity(new Intent(this, ShowSettingsActivity.class));
			break;
		}
	}

	private void setMoney() {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsMoney.putInt("money", money);
		editorPrefsMoney.putInt("paid_money", Pmoney);
		editorPrefsMoney.commit();
		coins.setText("" + (money + Pmoney));
	}

	private void updateStats(boolean right) {
		sharedPrefsStats = getSharedPreferences("Stats", 0);
		editorPrefsStats = sharedPrefsStats.edit();
		long ms = System.currentTimeMillis() - startTime;
		int correct = sharedPrefsStats.getInt("correct", 0);
		int wrong = sharedPrefsStats.getInt("wrong", 0);
		int total = correct + wrong;
		int coins = sharedPrefsStats.getInt("coins", 0);
		int bestStreak = sharedPrefsStats.getInt("bestStreak", 0);
		int currentStreak = sharedPrefsStats.getInt("currentStreak", 0);
		long totalTime = sharedPrefsStats.getLong("totalTime", 0);
		long answerTimeFast = sharedPrefsStats.getLong("answerTimeFast", Long.MAX_VALUE);
		if (right) {
			editorPrefsStats.putInt("correct", correct + 1);
			editorPrefsStats.putInt("coins", coins + dMoney);
			if (currentStreak >= bestStreak) {
				editorPrefsStats.putInt("bestStreak", bestStreak + 1);
				editorPrefsStats.putInt("currentStreak", currentStreak + 1);
			} else if (currentStreak >= 0)
				editorPrefsStats.putInt("currentStreak", currentStreak + 1);
			else
				editorPrefsStats.putInt("currentStreak", 1);
			if (answerTimeFast > ms)
				editorPrefsStats.putLong("answerTimeFast", ms);
		} else {
			editorPrefsStats.putInt("wrong", wrong + 1);
			editorPrefsStats.putInt("coins", coins + dMoney);
			if (currentStreak >= 0)
				editorPrefsStats.putInt("currentStreak", 0);
			else
				editorPrefsStats.putInt("currentStreak", currentStreak - 1);
		}
		editorPrefsStats.putLong("totalTime", totalTime + ms);
		editorPrefsStats.putLong("answerTimeAve", (totalTime + ms) / (total + 1));
		editorPrefsStats.commit();
	}

	private void toggleClockDate() {
		unlockEgg("clock", 1000);
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

	private void displayInfo(boolean first) {
		if (!dialogOn) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			if (first) {
				builder.setTitle(R.string.info_title_first);
				builder.setMessage(getString(R.string.info_message_first) + "\n\n" + getString(R.string.info_message)).setCancelable(false);
				builder.setPositiveButton(R.string.goto_store, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialogOn = false;
						startActivity(new Intent(getApplicationContext(), ShowStoreActivity.class));
					}
				});
				builder.setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialogOn = false;
					}
				});
			} else {
				builder.setTitle(R.string.info_title);
				builder.setMessage(R.string.info_message).setCancelable(false);
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialogOn = false;
					}
				});
				builder.setNeutralButton(R.string.rate, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialogOn = false;
						// open app in the Play store
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("market://details?id=com.olyware.mathlock"));
						startActivity(intent);
					}
				});
				builder.setNegativeButton(R.string.share_with, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialogOn = false;
						// TODO make this work for images, currently null is passed as the image, like to pass app thumbnail
						ShareHelper.share(ctx, getString(R.string.share_subject), null, getString(R.string.share_message),
								"http://play.google.com/store/apps/details?id=com.olyware.mathlock");
					}
				});
			}
			AlertDialog alert = builder.create();
			dialogOn = true;
			alert.show();
			if (!first)
				alert.getWindow().setLayout(layout.getWidth(), layout.getHeight() * 2 / 3);
		}
	}

	private void displayRateShare() {
		if (!dialogOn) {
			sharedPrefsMoney = getSharedPreferences("Packages", 0);
			editorPrefsMoney = sharedPrefsMoney.edit();
			editorPrefsMoney.putLong("lastTime", System.currentTimeMillis()).commit();

			boolean initial[] = { dontShow };
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.rate_title).setCancelable(false);
			builder.setMultiChoiceItems(R.array.dont_show_again, initial, new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialogInterface, int item, boolean state) {
					dontShow = state;
				}
			});
			builder.setPositiveButton(R.string.rate, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					sharedPrefsMoney = getSharedPreferences("Packages", 0);
					editorPrefsMoney = sharedPrefsMoney.edit();
					editorPrefsMoney.putBoolean("dontShowLastTime", dontShow).commit();
					dialogOn = false;
					// open app in the Play store
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("market://details?id=com.olyware.mathlock"));
					startActivity(intent);
				}
			});
			builder.setNeutralButton(R.string.share_with, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					sharedPrefsMoney = getSharedPreferences("Packages", 0);
					editorPrefsMoney = sharedPrefsMoney.edit();
					editorPrefsMoney.putBoolean("dontShowLastTime", dontShow).commit();
					dialogOn = false;
					// TODO make this work for images, currently null is passed as the image, like to pass app thumbnail
					ShareHelper.share(ctx, getString(R.string.share_subject), null, getString(R.string.share_message),
							"http://play.google.com/store/apps/details?id=com.olyware.mathlock");
				}
			});
			builder.setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialogOn = false;
				}
			});
			AlertDialog alert = builder.create();
			dialogOn = true;
			alert.show();
		}
	}

	private void unlockEgg(final String Egg, int max) {
		sharedPrefsEggs = getSharedPreferences("Eggs", 0);
		if (!sharedPrefsEggs.getBoolean(Egg, false)) {
			final int amount = getAmount(max);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.egg_title));
			builder.setMessage(getString(R.string.egg_message) + amount).setCancelable(false);
			builder.setIcon(R.drawable.egg);
			builder.setPositiveButton(R.string.cash_it, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					money += amount;
					editorPrefsEggs = sharedPrefsEggs.edit();
					editorPrefsEggs.putBoolean(Egg, true).commit();
					setMoney();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	private int getAmount(int max) {
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
