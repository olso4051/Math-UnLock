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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.EngineerQuestion;
import com.olyware.mathlock.model.HiQHTriviaQuestion;
import com.olyware.mathlock.model.LanguageQuestion;
import com.olyware.mathlock.model.MathQuestion;
import com.olyware.mathlock.model.Statistic;
import com.olyware.mathlock.model.VocabQuestion;
import com.olyware.mathlock.ui.Typefaces;
import com.olyware.mathlock.utils.Coins;
import com.olyware.mathlock.utils.EZ;
import com.olyware.mathlock.utils.EggHelper;
import com.olyware.mathlock.utils.IabHelper;
import com.olyware.mathlock.utils.IabResult;
import com.olyware.mathlock.utils.Inventory;
import com.olyware.mathlock.utils.MoneyHelper;
import com.olyware.mathlock.utils.Purchase;
import com.olyware.mathlock.utils.ShareHelper;
import com.olyware.mathlock.views.AnswerReadyListener;
import com.olyware.mathlock.views.AnswerView;
import com.olyware.mathlock.views.EquationView;
import com.olyware.mathlock.views.JoystickSelectListener;
import com.olyware.mathlock.views.JoystickTouchListener;
import com.olyware.mathlock.views.JoystickView;

public class MainActivity extends Activity {
	final private int multiplier = 3, lowestAmount = 5, decreaseRate = 500, startingPmoney = 0;
	final private Coins Money = new Coins(0, 0);
	final private static int[] Cost = { 1000, 5000, 10000 };
	final private static String[] SKU = { "coins1000", "coins5000", "coins10000" };
	private int dMoney;// change in money after a question is answered
	private int difficultyMax = 0, difficultyMin = 0, difficulty = 0;
	private long startTime = 0;
	private boolean fromSettings = false;

	private LinearLayout layout;
	private TextView clock;
	final private float clockSize = 40, dateSize = 20;
	private float currentClockSize;
	private TextView coins, worth;
	private int questionWorth, questionWorthMax;
	private EquationView problem;
	private Drawable imageLeft;	// left,top,right,bottom
	private AnswerView answerView;
	private boolean quizMode = false;
	private JoystickView joystick;
	private int defaultTextColor;

	private String[] PackageKeys, unlockPackageKeys, LanguageEntries, LanguageValues, EggKeys;
	private int[] EggMaxValues;
	private String currentPack, currentTableName, fromLanguage, toLanguage;
	private int ID;

	private int EnabledPackages = 0;
	private boolean EnabledPacks[];
	private boolean locked, UnlockedPackages = false;
	private boolean dialogOn = false;
	private boolean dontShow = false;
	final private long MONTH = 2592000000l;

	private int answerLoc = 1;		// {correct answer location}
	private String answers[] = { "3", "1", "2", "4" };	// {correct answer, wrong answers...}
	private String answersRandom[] = { "4", "2", "3", "1" };	// {answers in random order}
	private int attempts = 1;

	private Vibrator vib;
	private Random rand = new Random(); // Ideally just create one instance globally

	private SharedPreferences sharedPrefs, sharedPrefsMoney, sharedPrefsStats;
	private SharedPreferences.Editor editorPrefsMoney, editorPrefsStats;

	private Handler mHandler, timerHandler;
	private Runnable reduceWorth;
	private boolean attached = false;

	private DatabaseManager dbManager;
	private static Context ctx;

	private Typefaces typefaces;

	private IabHelper mHelper;
	private IabHelper.QueryInventoryFinishedListener mQueryFinishedListener;
	private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener;

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

	public static int[] getCost() {
		return Cost;
	}

	public static String[] getSKU() {
		return SKU;
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ctx = this;
		locked = this.getIntent().getBooleanExtra("locked", false);

		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvFriusQ7xzxd5eXOnodv5f/XFohXXDHyguNboQC5kPBbwF+Dje/LwdnNN4tzFYN/SbelMPu4sGFdKh6sA4f13wmzIvVOynG3WUqRzut53mAq7/2ljNjwTO0enfYh6F54lnHrp2FpZsLpbzSMnC95dd07k4YbDs5e4AbqtgHIRCLPOsTnmsihOQO8kf1cR0G/b+B37sqaLEnMAKFDcSICup5LMHLOimQMQ3K9eFjBsyU8fiIe+JqnXOdQfknshxZ33tFu+hO3JXs7wxOs/n2uaIm14e95FlC4T/RXC/duAi8LWt3NOFXgJIqAwztncGJHi3u787wEQkiDKNBO8AkSkwIDAQAB";
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// handle error
				} else {
					// in app billing is set up. check for non-consumed purchases
					List<String> additionalSkuList = new ArrayList<String>();
					additionalSkuList.add(SKU[0]);
					additionalSkuList.add(SKU[1]);
					additionalSkuList.add(SKU[2]);
					mHelper.queryInventoryAsync(true, additionalSkuList, mQueryFinishedListener);
				}
			}
		});
		// this listener checks the google play server for prices and consumable products purchased but not yet
		// provisioned to the user
		mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
			public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
				if (result.isFailure()) {
					// handle error
				} else {
					// check for non-consumed purchases
					if (inventory.hasPurchase(SKU[0])) {
						mHelper.consumeAsync(inventory.getPurchase(SKU[0]), mConsumeFinishedListener);
					} else if (inventory.hasPurchase(SKU[1])) {
						mHelper.consumeAsync(inventory.getPurchase(SKU[1]), mConsumeFinishedListener);
					} else if (inventory.hasPurchase(SKU[2])) {
						mHelper.consumeAsync(inventory.getPurchase(SKU[2]), mConsumeFinishedListener);
					}
				}
			}
		};
		// this listener checks for products that have been consumed and provisions them to the user
		mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
			public void onConsumeFinished(Purchase purchase, IabResult result) {
				if (result.isSuccess()) {
					if (purchase.getSku().equals(SKU[0])) {
						updateMoney(Cost[0]);
					} else if (purchase.getSku().equals(SKU[1])) {
						updateMoney(Cost[1]);
					} else if (purchase.getSku().equals(SKU[2])) {
						updateMoney(Cost[2]);
					}
				} else {
					// handle error
				}
			}
		};

		layout = (LinearLayout) findViewById(R.id.layout);
		typefaces = Typefaces.getInstance(this);
		EZ.setFont((ViewGroup) layout, typefaces.robotoLight);

		mHandler = new Handler();
		dbManager = new DatabaseManager(getApplicationContext());

		PackageKeys = getResources().getStringArray(R.array.enable_package_keys);
		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		LanguageValues = getResources().getStringArray(R.array.language_values_not_localized);
		EggKeys = getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = getResources().getIntArray(R.array.egg_max_values);
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
		problem = (EquationView) findViewById(R.id.problem);

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
		Display display = getWindowManager().getDefaultDisplay();
		int sizeY;
		if (android.os.Build.VERSION.SDK_INT < 13)
			sizeY = display.getHeight();
		else {
			Point size = new Point();
			display.getSize(size);
			sizeY = size.y;
		}
		answerView.setParentHeight(sizeY);

		joystick = (JoystickView) findViewById(R.id.joystick);
		joystick.setOnJostickSelectedListener(new JoystickSelectListener() {
			@Override
			public void OnSelect(int s) {
				JoystickSelected(s);
			}
		});
		quizMode = joystick.setQuizMode(!locked);

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
			// quizMode = joystick.setQuizMode(!locked && savedInstanceState.getBoolean("Quiz"));
			currentClockSize = savedInstanceState.getFloat("ClockSize");
		}

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		sharedPrefsStats = getSharedPreferences("Stats", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsStats = sharedPrefsStats.edit();
		for (int i = 0; i < EnabledPacks.length; i++)
			EnabledPacks[i] = false;

		setUnlockType(Integer.parseInt(sharedPrefs.getString("type", getString(R.string.type_default))));
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
		if (mHelper != null)
			mHelper.dispose();
		mHelper = null;
		joystick.removeCallbacks();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (locked && quizMode)
			quizMode = joystick.setQuizMode(false);

		// get settings
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		sharedPrefsStats = getSharedPreferences("Stats", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsStats = sharedPrefsStats.edit();
		if (sharedPrefsMoney.getBoolean("first", true)) {
			Money.setMoneyPaid(sharedPrefsMoney.getInt("paid_money", startingPmoney));
			editorPrefsMoney.putLong("lastTime", System.currentTimeMillis() - MONTH * 3 / 4);// give them a week before asking
			editorPrefsMoney.putBoolean("first", false);
			editorPrefsMoney.commit();
		} else {
			Money.setMoneyPaid(sharedPrefsMoney.getInt("paid_money", 0));
		}
		Money.setMoney(sharedPrefsMoney.getInt("money", 0));
		coins.setText(String.valueOf(Money.getMoney() + Money.getMoneyPaid()));

		// set the clock to the correct time
		setTime();

		// set the unlock type
		setUnlockType(Integer.parseInt(sharedPrefs.getString("type", getString(R.string.type_default))));

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

		// save money into shared preferences
		MoneyHelper.setMoney(this, coins, Money.getMoney(), Money.getMoneyPaid());

		// set image if it was set when the screen was off
		setImage();

		if (fromSettings) {
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[0], EggMaxValues[0]));
			fromSettings = false;
		}
	}

	@Override
	protected void onPause() {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		if (attached)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		editorPrefsMoney.putInt("money", Money.getMoney());
		editorPrefsMoney.putInt("paid_money", Money.getMoneyPaid());
		if (!sharedPrefsMoney.getBoolean("dontShowLastTime", false))
			editorPrefsMoney.putBoolean("dontShowLastTime", dontShow);
		editorPrefsMoney.commit();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (locked) {
			return;
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		if (locked)
			homeTest();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (locked) {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				return true;	// doesn't execute code to change the volume when the screen is locked
			}
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				return true;	// doesn't execute code to change the volume when the screen is locked
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		locked = intent.getBooleanExtra("locked", false);
	}

	private boolean homeTest() {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
		if (recentTasks.get(1).baseActivity.toShortString().indexOf(getPackageName()) > -1) {
			// TODO test on multiple devices I think this is when Home or Notification is pressed, can't stop from executing exit code
			Money.decreaseMoneyAndPaidWithDebt(questionWorthMax);
			return true;
		}
		return false;
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

	public void setUnlockType(int type) {
		if (type == 2) {
			problem.setOffset(-worth.getCompoundDrawables()[0].getIntrinsicHeight() / 2);
		} else
			problem.setOffset(0);
		joystick.setUnlockType(type);
		answerView.setUnlockType(type);
	}

	private void showWallpaper() {
		// dims the wallpaper so app has more contrast
		layout.setBackgroundColor(Color.argb(150, 0, 0, 0));
	}

	private void setImage() {
		// use the image height and width to set the bounds and not stretch the image
		// int h = image.getIntrinsicHeight();
		// int w = image.getIntrinsicWidth();
		if (imageLeft == null)
			problem.setCompoundDrawables(null, null, null, null);
		else {
			// imageLeft.setBounds(0, 0, problem.getHeight() * w / h, problem.getHeight());//no stretch
			imageLeft.setBounds(0, 0, problem.getHeight(), problem.getHeight());	// stretch to square
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
					joystick.resetGuess();
					imageLeft = null;
					problem.setCompoundDrawables(imageLeft, null, null, null);
					joystick.unPauseSelection();
					sharedPrefsStats = getSharedPreferences("Stats", 0);
					joystick.setDegreeStep(sharedPrefsStats.getInt("currentStreak", 0));

					// pick a random enabled package
					int randPack = rand.nextInt(EnabledPackageKeys.length);

					// get the difficulty
					difficultyMax = Integer.parseInt(sharedPrefs.getString("difficulty_max", "0"));
					difficultyMin = Integer.parseInt(sharedPrefs.getString("difficulty_min", "0"));
					difficulty = difficultyMax;
					switch (location[randPack]) {
					case 0:			// math question
						currentPack = getString(R.string.math);
						setMathProblem(difficultyMin, difficultyMax);
						break;
					case 1:			// vocabulary question
						currentPack = getString(R.string.vocab);
						setVocabProblem(difficultyMin, difficultyMax);
						break;
					case 2:			// language question
						currentPack = getString(R.string.language);
						setLanguageProblem(difficultyMin, difficultyMax);
						break;
					/*case 3:			// enabled vocab act/sat question
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
						setToddlerProblem(difficultyMax);
						break;*/
					case 3:			// engineer question
						currentPack = getString(R.string.engineer);
						setEngineerProblem(difficultyMin, difficultyMax);
						break;
					case 4:			// HiQH Trivia question
						currentPack = getString(R.string.hiqh_trivia);
						setHiQHTriviaProblem(difficultyMin, difficultyMax);
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
					joystick.setAnswers(answersRandom);
					problem.setTextColor(defaultTextColor);
					resetTimes();
				}
			}, delay); // set new problem after delay time [ms]

		} else {
			answerView.resetGuess();
			joystick.resetGuess();
			joystick.setProblem(false);
			if (!UnlockedPackages)
				problem.setText(R.string.none_unlocked);
			else
				problem.setText(R.string.none_enabled);
			String temp[] = { "N/A", "N/A", "N/A", "N/A" };
			answersRandom = temp;
			for (int i = 0; i < 4; i++) {
				answerView.setAnswers(answersRandom);
				joystick.setAnswers(answersRandom);
			}
		}
	}

	private void resetTimes() {
		startTime = System.currentTimeMillis();
		questionWorth = difficulty * multiplier + lowestAmount;
		questionWorthMax = questionWorth;
		worth.setText(String.valueOf(questionWorth));
		timerHandler.removeCallbacks(reduceWorth);
		timerHandler.postDelayed(reduceWorth, decreaseRate);
	}

	private void setMathProblem(int minDifficulty, int maxDifficulty) {
		currentTableName = getString(R.string.math_table);
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
		answers = question.getAnswers();
		problem.setText(question.getQuestionText());
		return;
	}

	// TODO: Pass in a Difficulty enum instead of an integer
	private void setVocabProblem(int minDifficulty, int maxDifficulty) {
		currentTableName = getString(R.string.vocab_table);
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

	private void setLanguageProblem(int minDifficulty, int maxDifficulty) {
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
		int diff = rand.nextInt(maxDifficulty - minDifficulty + 1) + minDifficulty;
		List<LanguageQuestion> questions = dbManager.getLanguageQuestions(Difficulty.fromValue(diff), answers.length, fromLanguage,
				toLanguage);
		// List<LanguageQuestion> questions = dbManager.getLanguageQuestions(Difficulty.fromValue(minDifficulty),
		// Difficulty.fromValue(maxDifficulty), answers.length, fromLanguage, toLanguage);
		ID = questions.get(0).getID();

		// Set the new difficulty based on what question was picked
		difficulty = questions.get(0).getDifficulty().getValue();

		// Display the vocab question and answers
		for (int i = 0; i < answers.length; i++) {
			answers[i] = questions.get(i).getCorrectAnswer();
		}
		problem.setText(fromLanguageLocal + " -> " + toLanguageLocal + "\n" + questions.get(0).getQuestionText());
	}

	/*private void setACT_SATProblem(int diffNum, boolean vocab, boolean math) {
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
	}*/

	private void setEngineerProblem(int minDifficulty, int maxDifficulty) {
		currentTableName = getString(R.string.engineer_table);
		EngineerQuestion question = dbManager.getEngineerQuestion(Difficulty.fromValue(minDifficulty), Difficulty.fromValue(maxDifficulty));
		ID = question.getID();

		// Set the new difficulty based on what question was picked
		difficulty = question.getDifficulty().getValue();

		problem.setText(question.getQuestionText());
		answers = question.getAnswers();
		return;
	}

	private void setHiQHTriviaProblem(int minDifficulty, int maxDifficulty) {
		currentTableName = getString(R.string.hiqh_trivia_table);
		HiQHTriviaQuestion question = dbManager.getHiQHTriviaQuestion(Difficulty.fromValue(minDifficulty),
				Difficulty.fromValue(maxDifficulty));
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
		int difficultyMaxBefore = difficultyMax;
		int difficultyMinBefore = difficultyMin;
		String fromLanguageBefore = fromLanguage;
		String toLanguageBefore = toLanguage;

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

		difficultyMax = Integer.parseInt(sharedPrefs.getString("difficulty_max", "0"));
		difficultyMin = Integer.parseInt(sharedPrefs.getString("difficulty_min", "0"));
		difficulty = difficultyMax;
		fromLanguage = sharedPrefs.getString("from_language", getString(R.string.language_from_default));
		toLanguage = sharedPrefs.getString("to_language", getString(R.string.language_to_default));
		if ((difficultyMaxBefore != difficultyMax) || (difficultyMinBefore != difficultyMin) || (fromLanguageBefore != fromLanguage)
				|| (toLanguageBefore != toLanguage))
			changed = true;

		return changed;
	}

	private void displayCorrectOrNot(int correctLoc, int guessLoc, String description, boolean correct, boolean unknown) {
		if (unknown) {
			answerView.setCorrectAnswer(correctLoc);
			joystick.setCorrectAnswer(correctLoc);
		} else {
			if (correct) {
				answerView.setCorrectAnswer(correctLoc);
				joystick.setCorrectAnswer(correctLoc);
				problem.setTextColor(Color.GREEN);
				dMoney = Money.increaseMoney(questionWorth);
				dbManager.addStat(new Statistic(currentPack, String.valueOf(true), Difficulty.fromValue(difficulty), System
						.currentTimeMillis()));
				dbManager.decreasePriority(currentTableName, fromLanguage, toLanguage, ID);
			} else {
				answerView.setCorrectAnswer(correctLoc);
				joystick.setCorrectAnswer(correctLoc);
				answerView.setIncorrectGuess(guessLoc);
				joystick.setIncorrectGuess(guessLoc);
				problem.setTextColor(Color.RED);
				dMoney = Money.decreaseMoneyNoDebt(questionWorth);
				dbManager.addStat(new Statistic(currentPack, String.valueOf(false), Difficulty.fromValue(difficulty), System
						.currentTimeMillis()));
				dbManager.increasePriority(currentTableName, fromLanguage, toLanguage, ID);
			}
			MoneyHelper.setMoney(this, coins, Money.getMoney(), Money.getMoneyPaid());
			// problem.setText(description + problem.getText());
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
				setProblemAndAnswer(500);
			} else if ((answerLoc == s) && !quizMode) {
				displayCorrectOrNot(answerLoc, s, "Correct!\n", true, false);
				updateStats(true);
				joystick.pauseSelection();
				launchHomeScreen(10);
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
		case 4:	// unknown was selected
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[1], EggMaxValues[1]));
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
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[2], EggMaxValues[2]));
			displayInfo(false);
			break;
		case 6:		// Store was selected
			startActivity(new Intent(this, ShowStoreActivity.class));
			break;
		case 7:		// progress was selected
			startActivity(new Intent(this, ShowProgressActivity.class));
			break;
		case 8:		// quiz Mode was selected
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[3], EggMaxValues[3]));
			quizMode = joystick.setQuizMode(!quizMode);
			break;
		case 9:		// settings was selected
			fromSettings = true;
			startActivity(new Intent(this, ShowSettingsActivity.class));
			break;
		}
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
		Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[4], EggMaxValues[4]));
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
				builder.setMessage(getString(R.string.info_message_first)).setCancelable(false);
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
						Money.increaseMoney(EggHelper.unlockEgg(MainActivity.this, coins, EggKeys[9], EggMaxValues[9]));
					}
				});
				builder.setNegativeButton(R.string.share_with, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialogOn = false;
						// TODO make this work for images, currently null is passed as the image, like to pass app thumbnail
						// String fileName = "android.resource://" + MainActivity.this.getPackageName() + "/" + R.drawable.ic_launcher;
						String fileName = "content://" + MainActivity.this.getPackageName() + "/ic_launcher.png";
						ShareHelper.share(ctx, getString(R.string.share_subject), fileName, getString(R.string.share_message),
								"http://play.google.com/store/apps/details?id=com.olyware.mathlock");
						Money.increaseMoney(EggHelper.unlockEgg(MainActivity.this, coins, EggKeys[8], EggMaxValues[8]));
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
					// TODO make this work for images, currently null is passed as the image, would like to pass app
					// thumbnail(ic_launcher.png)
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

	private void updateMoney(int amount) {
		Money.increaseMoneyPaid(amount);
		MoneyHelper.setMoney(this, coins, Money.getMoney(), Money.getMoneyPaid());
	}
}
