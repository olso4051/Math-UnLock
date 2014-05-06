package com.olyware.mathlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.model.CustomQuestion;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.EngineerQuestion;
import com.olyware.mathlock.model.HiqTriviaQuestion;
import com.olyware.mathlock.model.LanguageQuestion;
import com.olyware.mathlock.model.MathQuestion;
import com.olyware.mathlock.model.Statistic;
import com.olyware.mathlock.model.VocabQuestion;
import com.olyware.mathlock.service.RegisterID;
import com.olyware.mathlock.service.ScreenService;
import com.olyware.mathlock.ui.Typefaces;
import com.olyware.mathlock.utils.Clock;
import com.olyware.mathlock.utils.Coins;
import com.olyware.mathlock.utils.EZ;
import com.olyware.mathlock.utils.EggHelper;
import com.olyware.mathlock.utils.GCMHelper;
import com.olyware.mathlock.utils.IabHelper;
import com.olyware.mathlock.utils.IabResult;
import com.olyware.mathlock.utils.Inventory;
import com.olyware.mathlock.utils.MoneyHelper;
import com.olyware.mathlock.utils.NotificationHelper;
import com.olyware.mathlock.utils.Purchase;
import com.olyware.mathlock.utils.ShareHelper;
import com.olyware.mathlock.views.AnswerReadyListener;
import com.olyware.mathlock.views.AnswerView;
import com.olyware.mathlock.views.EquationView;
import com.olyware.mathlock.views.JoystickSelectListener;
import com.olyware.mathlock.views.JoystickTouchListener;
import com.olyware.mathlock.views.JoystickView;

public class MainActivity extends Activity implements RegisterID.RegisterIdResponse {
	final private int startingPmoney = 20000, streakToIncrease = 40;
	final private Coins Money = new Coins(0, 0);
	final private static int[] Cost = { 1000, 5000, 10000 };
	final private static String[] SKU = { "coins1000", "coins5000", "coins10000" };
	final private String[] answersNone = { "", "", "", "" };
	final private int PLAY_CORRECT = 0, PLAY_WRONG = 1, PLAY_BEEP = 2;
	final private static String SCREEN_LABEL = "Home Screen";

	private int dMoney;// change in money after a question is answered
	private int difficultyMax = 0, difficultyMin = 0, difficulty = 0;
	private long startTime = 0;
	private boolean fromSettings = false, fromPlay = false, fromShare = false;

	private LinearLayout layout;
	private Clock clock;
	private TextView coins, worth;
	private int questionWorthMax = 0, questionWorth = 0, decreaseRate = 500;
	private EquationView problem;
	private Drawable imageLeft;	// left,top,right,bottom
	private AnswerView answerView;
	private boolean quizMode = false;
	private JoystickView joystick;
	private int defaultTextColor;

	private List<String> customCategories, PackageKeys;
	private List<Integer> streakToNotify, totalToNotify;
	private String[] unlockPackageKeys, LanguageEntries, LanguageValues, EggKeys, hints;
	private int[] EggMaxValues;
	private String currentPack, currentTableName, fromLanguage, toLanguage;
	private long ID = 0;

	private int EnabledPackages = 0;
	private boolean info, locked, unlocking, UnlockedPackages = false;
	private boolean dialogOn = false, dontShow = false, paused = false;
	final private long MONTH = 2592000000l, WEEK = 604800000l, DAY = 86400000l;

	private int answerLoc = 0;		// {correct answer location}
	private String answers[] = { "3", "1", "2", "4" };	// {correct answer, wrong answers...}
	private String answersRandom[] = { "4", "2", "3", "1" };	// {answers in random order}
	private int attempts = 1;

	MediaPlayer answerIncorrectClick, answerCorrectClick, buttonClick;
	private Vibrator vib;
	private Random rand = new Random(); // Ideally just create one instance globally

	private SharedPreferences sharedPrefs, sharedPrefsMoney, sharedPrefsStats, sharedPrefsApps;
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

	private List<ApplicationInfo> apps;

	public static Context getContext() {
		return ctx;
	}

	public static int[] getCost() {
		return Cost;
	}

	public static String[] getSKU() {
		return SKU;
	}

	private BroadcastReceiver finishBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("onReceive", "Logout in progress");
			// At this point you should start the login activity and finish this one
			finish();
		}
	};

	private class OpenDatabase extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			dbManager = new DatabaseManager(getApplicationContext());
			customCategories = dbManager.getAllCustomCategories();
			PackageKeys = EZ.list(getResources().getStringArray(R.array.enable_package_keys));
			for (String cat : customCategories)
				PackageKeys.add(getString(R.string.custom_enable) + cat);
			UnlockedPackages = getUnlockedPackages();
			EnabledPackages = getEnabledPackages();
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void v) {
			setProblemAndAnswer();
			super.onPostExecute(null);
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		IntentFilter intentFilter = new IntentFilter(getString(R.string.logout_receiver_filter));
		LocalBroadcastManager.getInstance(this).registerReceiver(finishBroadcast, intentFilter);

		Log.d("GAtest", "onCreate");
		ctx = this;
		info = getIntent().getBooleanExtra("info", false);
		Log.d("GAtest", "info = " + info);
		locked = getIntent().getBooleanExtra("locked", false);
		unlocking = locked;

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		sharedPrefsStats = getSharedPreferences("Stats", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsStats = sharedPrefsStats.edit();

		fromLanguage = sharedPrefs.getString("from_language", getString(R.string.language_from_default));
		toLanguage = sharedPrefs.getString("to_language", getString(R.string.language_to_default));

		streakToNotify = new ArrayList<Integer>();
		for (int i : getResources().getIntArray(R.array.notify_streaks))
			streakToNotify.add(i);
		totalToNotify = new ArrayList<Integer>();
		for (int i : getResources().getIntArray(R.array.notify_total_questions))
			totalToNotify.add(i);

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
						updatePaidMoney(Cost[0]);
					} else if (purchase.getSku().equals(SKU[1])) {
						updatePaidMoney(Cost[1]);
					} else if (purchase.getSku().equals(SKU[2])) {
						updatePaidMoney(Cost[2]);
					}
				} else {
					// handle error
				}
			}
		};

		layout = (LinearLayout) findViewById(R.id.layout);
		if (sharedPrefs.getInt("layout_width", 0) == 0 || sharedPrefs.getInt("layout_height", 0) == 0) {
			ViewTreeObserver vtoLayout = layout.getViewTreeObserver();
			vtoLayout.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
					sharedPrefs.edit().putInt("layout_width", layout.getWidth()).putInt("layout_height", layout.getHeight()).commit();
					ViewTreeObserver obs = layout.getViewTreeObserver();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						obs.removeOnGlobalLayoutListener(this);
					} else {
						obs.removeGlobalOnLayoutListener(this);
					}
				}

			});
		}
		typefaces = Typefaces.getInstance(this);
		EZ.setFont((ViewGroup) layout, typefaces.robotoLight);

		mHandler = new Handler();

		PackageKeys = EZ.list(getResources().getStringArray(R.array.enable_package_keys));
		LanguageEntries = getResources().getStringArray(R.array.language_entries);
		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		LanguageValues = getResources().getStringArray(R.array.language_values_not_localized);
		EggKeys = getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = getResources().getIntArray(R.array.egg_max_values);
		hints = getResources().getStringArray(R.array.hints);

		clock = new Clock(this, (TextView) findViewById(R.id.clock), (TextView) findViewById(R.id.money));

		coins = (TextView) findViewById(R.id.money);
		worth = (TextView) findViewById(R.id.worth);
		problem = (EquationView) findViewById(R.id.problem);
		defaultTextColor = problem.getTextColors().getDefaultColor();

		answerView = (AnswerView) findViewById(R.id.answers2);
		answerView.setColor(defaultTextColor);
		answerView.setReadyListener(new AnswerReadyListener() {
			@Override
			public void Ready() {
				answerView.setAnswers(answersRandom, answerLoc);
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
			public void OnSelect(int s, boolean vibrate, int Extra) {
				JoystickSelected(s, vibrate, Extra);
			}
		});
		quizMode = joystick.setQuizMode(!locked);

		timerHandler = new Handler();
		reduceWorth = new Runnable() {
			@Override
			public void run() {
				if (!paused) {
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
			}
		};

		apps = new ArrayList<ApplicationInfo>();

		setUnlockType(Integer.parseInt(sharedPrefs.getString("type", getString(R.string.type_default))));
		showWallpaper();

		new OpenDatabase().execute();

		MyApplication.getGaTracker().set(Fields.SCREEN_NAME, SCREEN_LABEL);
		MyApplication.getGaTracker().set(Fields.SESSION_CONTROL, "start");

		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
		// API_ID = getString(R.string.gcm_api_id);
		// appCtx = getApplicationContext();
		GCMHelper.registerAndStoreGCM(this, getApplicationContext());
		/*if (checkPlayServices()) {
			gcm = MyApplication.getGcmInstance();
			regID = getRegistrationId(appCtx);
			SharedPreferences prefsGA = getSharedPreferences("ga_prefs", Context.MODE_PRIVATE);
			if (regID.equals("")) {
				SharedPreferences.Editor editorGA = prefsGA.edit();
				editorGA.putBoolean("reg_uploaded", false).commit();
				registerInBackground(this);
			} else if (!prefsGA.getBoolean("reg_uploaded", false)) {
				String referral = prefsGA.getString("utm_content", "");
				storeRegistrationId(this, appCtx, regID);
				sendRegistrationIdToBackend(this, regID, referral);
			}
		} else {
			Toast.makeText(this, "No valid Google Play Services APK found.", Toast.LENGTH_LONG).show();
		}*/
	}

	@Override
	public void onAttachedToWindow() {
		attached = true;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	@Override
	protected void onPause() {
		Log.d("GAtest", "onPause");
		paused = true;

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPrefs.edit().putLong("timeout", System.currentTimeMillis()).commit();
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		if (attached)
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		editorPrefsMoney.putInt("money", Money.getMoney());
		editorPrefsMoney.putInt("paid_money", Money.getMoneyPaid());
		if (!sharedPrefsMoney.getBoolean("dontShowLastTime", false))
			editorPrefsMoney.putBoolean("dontShowLastTime", dontShow);
		editorPrefsMoney.commit();

		joystick.removeCallbacks();

		answerIncorrectClick.reset();
		answerCorrectClick.reset();
		buttonClick.reset();
		answerIncorrectClick.release();
		answerCorrectClick.release();
		buttonClick.release();

		super.onPause();
	}

	@Override
	protected void onStart() {
		Log.d("GAtest", "onStart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.d("GAtest", "onStop");
		if (attached)
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		joystick.removeCallbacks();
		MyApplication.getGaTracker().set(Fields.SESSION_CONTROL, "end");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d("GAtest", "onDestroy");
		clock.destroy();
		if (mHelper != null)
			mHelper.dispose();
		mHelper = null;
		if (dbManager != null)
			if (!dbManager.isDestroyed())
				dbManager.destroy();
		joystick.removeCallbacks();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(finishBroadcast);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		Log.d("GAtest", "onResume");
		long currentTime = System.currentTimeMillis();
		paused = false;
		super.onResume();
		GCMHelper.checkPlayServices(this);
		// checkPlayServices();
		if (locked && quizMode)
			quizMode = joystick.setQuizMode(false);

		// load the sound files
		answerIncorrectClick = MediaPlayer.create(this, R.raw.answer_incorrect);
		answerCorrectClick = MediaPlayer.create(this, R.raw.answer_correct);
		buttonClick = MediaPlayer.create(this, R.raw.button_click);

		// get settings
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		sharedPrefsStats = getSharedPreferences("Stats", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		editorPrefsStats = sharedPrefsStats.edit();
		if (sharedPrefsMoney.getBoolean("first", true)) {
			Money.setMoneyPaid(sharedPrefsMoney.getInt("paid_money", startingPmoney));
			editorPrefsMoney.putLong("lastTime", currentTime - MONTH * 3 / 4);// give them a week before asking
			editorPrefsMoney.putBoolean("first", false);
			editorPrefsMoney.commit();
		} else {
			Money.setMoneyPaid(sharedPrefsMoney.getInt("paid_money", 0));
		}
		Money.setMoney(sharedPrefsMoney.getInt("money", 0));
		coins.setText(String.valueOf(Money.getMoney() + Money.getMoneyPaid()));

		// set the unlock type
		setUnlockType(Integer.parseInt(sharedPrefs.getString("type", getString(R.string.type_default))));

		// reset attempts to first attempt
		attempts = 1;

		if (dbManager != null)
			if (!dbManager.isDestroyed()) {
				customCategories = dbManager.getAllCustomCategories();
				PackageKeys = EZ.list(getResources().getStringArray(R.array.enable_package_keys));
				for (String cat : customCategories)
					PackageKeys.add(getString(R.string.custom_enable) + cat);
			}

		// get unlocked and enabled item changes
		UnlockedPackages = getUnlockedPackages();
		EnabledPackages = getEnabledPackages();

		// start background service to wait for screen to turn off. if service is already running startService does nothing
		Intent sIntent = new Intent(this, ScreenService.class);
		if (((EnabledPackages > 0) || (dbManager == null)) && (sharedPrefs.getBoolean("lockscreen", true))) {
			this.startService(sIntent);
		} else {
			this.stopService(sIntent);
		}

		// setup the question and answers
		// resetQuestionWorth(0);
		// startCountdown();
		// resetTimer();
		// if (changed)
		if (!unlocking)
			setProblemAndAnswer();
		else
			startCountdown();

		if (!UnlockedPackages)
			displayInfo(true);
		else if (info)
			displayInfo(false);
		else if ((!sharedPrefsMoney.getBoolean("dontShowLastTime", false))
				&& (sharedPrefsMoney.getLong("lastTime", 0) <= currentTime - MONTH))
			displayRateShare();
		else if (sharedPrefs.getBoolean("hints", true)) {
			setProblemAndAnswer();
			displayHints(0, false);
		}

		// Backup preferences every day
		if (sharedPrefs.getLong("lastTimeBackup", 0) <= currentTime - DAY) {
			sharedPrefs.edit().putLong("lastTimeBackup", currentTime).commit();
			EZ.requestBackup(this);
		}

		// set image if it was set when the screen was off
		setImage();

		if (fromSettings) {
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[0], EggMaxValues[0]));
			fromSettings = false;
		}
		if (fromPlay) {
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[9], EggMaxValues[9]));
			fromPlay = false;
		}
		if (fromShare) {
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[8], EggMaxValues[8]));
			fromShare = false;
		}

		joystick.startAnimations();

		// save money into shared preferences
		MoneyHelper.setMoney(this, coins, Money.getMoney(), Money.getMoneyPaid());
	}

	@Override
	public void onBackPressed() {
		if (locked && UnlockedPackages) {		// if locked then don't allow back button to exit app
			return;
		} else {
			super.onBackPressed();
		}
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
		info = intent.getBooleanExtra("info", false);
		Log.d("GAtest", "info = " + info);
		locked = intent.getBooleanExtra("locked", false);
		unlocking = locked;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		joystick.showStartAnimation(0, 3000);
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == R.id.REQUEST_PICK_APP) {
				sharedPrefsApps = getSharedPreferences("Apps", 0);
				PackageManager pm = getPackageManager();
				List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
				for (ApplicationInfo pack : packages) {
					Intent test = pm.getLaunchIntentForPackage(pack.packageName);
					if (test != null) {
						if (data.getComponent().equals(test.getComponent())) {
							Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[14], EggMaxValues[14]));
							sharedPrefsApps.edit().putInt("size", apps.size() + 1).putString("app" + apps.size(), pack.packageName)
									.commit();
							apps.add(pack);
							joystick.addApp(pack.loadIcon(pm));
							sendEvent("apps", "add_app", pack.name, null);
						}
					}
				}
			}
		}
	}

	private void setApps() {
		sharedPrefsApps = getSharedPreferences("Apps", 0);
		if (sharedPrefsApps.getInt("size", 0) != apps.size()) {
			apps.clear();
			joystick.clearApps();

			int i = 0;
			PackageManager pm = getPackageManager();
			while (sharedPrefsApps.getString("app" + i, null) != null) {
				try {
					apps.add(pm.getApplicationInfo(sharedPrefsApps.getString("app" + i, null), PackageManager.GET_META_DATA));
					joystick.addApp(apps.get(i).loadIcon(pm));
					i++;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
					removeAppFromPrefs(i);
					sharedPrefsApps = getSharedPreferences("Apps", 0);
				}
			}
		}
	}

	private void removeAppFromAll(int loc) {
		if (apps != null) {
			if (loc < apps.size()) {
				apps.remove(loc);
				sharedPrefsApps = getSharedPreferences("Apps", 0);
				SharedPreferences.Editor editorApps = sharedPrefsApps.edit();
				for (int i = loc; i < apps.size(); i++) {
					editorApps.putString("app" + i, apps.get(i).packageName);
				}
				editorApps.putString("app" + apps.size(), null).putInt("size", apps.size()).commit();
			}
		}
	}

	private void removeAppFromPrefs(int loc) {
		sharedPrefsApps = getSharedPreferences("Apps", 0);
		int newSize = sharedPrefsApps.getInt("size", 0) - 1;
		if (sharedPrefsApps.getString("app" + loc, null) != null) {
			SharedPreferences.Editor editorApps = sharedPrefsApps.edit();
			while (sharedPrefsApps.getString("app" + loc, null) != null) {
				editorApps.putString("app" + loc, sharedPrefsApps.getString("app" + (loc + 1), null));
				loc++;
			}
			editorApps.putString("app" + loc, null).putInt("size", newSize).commit();
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

	public void setUnlockType(int type) {
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

	private void setProblemAndAnswer() {
		if ((EnabledPackages > 0) && (dbManager != null)) {
			if (!dbManager.isDestroyed()) {
				joystick.setProblem(true);
				final String EnabledPackageKeys[] = new String[EnabledPackages];
				final int location[] = new int[EnabledPackages];
				double weights[] = new double[EnabledPackages], totalWeight = 0;
				int count = 0;
				boolean success;

				// get the difficulty
				difficultyMax = Integer.parseInt(sharedPrefs.getString("difficulty_max", "0"));
				difficultyMin = Integer.parseInt(sharedPrefs.getString("difficulty_min", "0"));
				difficulty = difficultyMax;

				for (int i = 0; i < PackageKeys.size(); i++) {
					if (sharedPrefs.getBoolean(PackageKeys.get(i), false)) {
						EnabledPackageKeys[count] = PackageKeys.get(i);
						location[count] = i;
						weights[count] = dbManager.getPriority(i, fromLanguage, toLanguage, Difficulty.fromValue(difficultyMin),
								Difficulty.fromValue(difficultyMax), ID);
						totalWeight += weights[count];
						count++;
					}
				}

				questionWorth = 0;
				answerView.resetGuess();
				joystick.resetGuess();
				imageLeft = null;
				problem.setCompoundDrawables(imageLeft, null, null, null);
				joystick.unPauseSelection();
				sharedPrefsStats = getSharedPreferences("Stats", 0);
				joystick.setDegreeStep(sharedPrefsStats.getInt("currentStreak", 0));

				// pick a random enabled package
				if (totalWeight == 0)
					count = 0;
				else {
					int randPack = rand.nextInt((int) Math.floor(totalWeight));
					count = 0;
					double cumulativeWeight = 0;
					while (count < EnabledPackages) {
						cumulativeWeight += weights[count];
						if (cumulativeWeight > randPack) {
							break;
						}
						count++;
					}
				}

				switch (location[count]) {
				case 0:			// math question
					currentPack = getString(R.string.math);
					success = setMathProblem(Difficulty.fromValue(difficultyMin), Difficulty.fromValue(difficultyMax));
					break;
				case 1:			// vocabulary question
					currentPack = getString(R.string.vocab);
					success = setVocabProblem(Difficulty.fromValue(difficultyMin), Difficulty.fromValue(difficultyMax));
					break;
				case 2:			// language question
					currentPack = getString(R.string.language);
					success = setLanguageProblem(Difficulty.fromValue(difficultyMin), Difficulty.fromValue(difficultyMax));
					break;
				case 3:			// engineer question
					currentPack = getString(R.string.engineer);
					success = setEngineerProblem(Difficulty.fromValue(difficultyMin), Difficulty.fromValue(difficultyMax));
					break;
				case 4:			// HiqH Trivia question
					currentPack = getString(R.string.hiqh_trivia);
					success = setHiqHTriviaProblem(Difficulty.fromValue(difficultyMin), Difficulty.fromValue(difficultyMax));
					break;
				default:
					success = false;
					break;
				}
				if (!success) {
					// custom question
					if ((location[count] > 4) && (location[count] < PackageKeys.size())) {
						currentPack = getString(R.string.custom) + " " + customCategories.get(location[count] - 5);
						success = setCustomProblem(customCategories.get(location[count] - 5), Difficulty.fromValue(difficultyMin),
								Difficulty.fromValue(difficultyMax));
					}
					// failed to load a question
					if (!success)
						return;
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

				answerView.setAnswers(answersRandom, answerLoc);
				joystick.setAnswers(answersRandom, answerLoc);
				problem.setTextColor(defaultTextColor);
				resetQuestionWorth(questionWorthMax);
			} else {
				answerView.resetGuess();
				joystick.resetGuess();
				joystick.setProblem(false);
				problem.setText(R.string.db_loading);
				answersRandom = answersNone;
				answerView.setAnswers(answersRandom, 0);
				joystick.setAnswers(answersRandom, 0);
			}
		} else {
			answerView.resetGuess();
			joystick.resetGuess();
			joystick.setProblem(false);
			if (!UnlockedPackages) {
				problem.setText(R.string.none_unlocked);
				answersRandom = answersNone;
			} else if (dbManager == null) {
				problem.setText(R.string.db_loading);
				answersRandom = answersNone;
			} else {
				problem.setText(R.string.none_enabled);
				answersRandom = answersNone;
			}
			answerView.setAnswers(answersRandom, 0);
			joystick.setAnswers(answersRandom, 0);
		}
	}

	private void resetQuestionWorth(int value) {
		startTime = System.currentTimeMillis();
		questionWorth = value;
		worth.setText(String.valueOf(questionWorth));
		timerHandler.removeCallbacks(reduceWorth);
		timerHandler.postDelayed(reduceWorth, decreaseRate);
	}

	private void startCountdown() {
		startTime = System.currentTimeMillis();
		questionWorth = questionWorthMax;
		worth.setText(String.valueOf(questionWorth));
		timerHandler.removeCallbacks(reduceWorth);
		timerHandler.postDelayed(reduceWorth, decreaseRate);
	}

	private boolean setMathProblem(Difficulty min, Difficulty max) {
		currentTableName = getString(R.string.math_table);
		MathQuestion question = dbManager.getMathQuestion(min, max, ID);
		if (question == null)
			return false;
		ID = question.getID();
		question.setVariables();

		// Set the new difficulty based on what question was picked
		difficulty = question.getDifficulty().getValue();
		questionWorthMax = question.getTimeSteps();
		decreaseRate = question.getTimeStep();

		if (!question.getImage().equals("none")) {
			int id = getResources().getIdentifier(question.getImage(), "drawable", getPackageName());
			imageLeft = getResources().getDrawable(id);
			setImage();
		}
		answers = question.getAnswers();
		problem.setText(question.getQuestionText());
		return true;
	}

	private boolean setVocabProblem(Difficulty min, Difficulty max) {
		currentTableName = getString(R.string.vocab_table);
		List<VocabQuestion> questions = dbManager.getVocabQuestions(min, max, answers.length, ID);
		if (questions == null)
			return false;
		ID = questions.get(0).getID();

		// Set the new difficulty based on what question was picked
		difficulty = questions.get(0).getDifficulty().getValue();
		questionWorthMax = questions.get(0).getTimeSteps();
		decreaseRate = questions.get(0).getTimeStep();

		// Display the vocabulary question and answers
		decreaseRate = questions.get(0).getQuestionText().length();
		for (int i = 0; i < answers.length; i++) {
			answers[i] = questions.get(i).getCorrectAnswer();
			decreaseRate += answers[i].length();
		}
		decreaseRate = decreaseRate * 10;
		problem.setText("Define: " + questions.get(0).getQuestionText());
		return true;
	}

	private boolean setLanguageProblem(Difficulty min, Difficulty max) {
		currentTableName = getString(R.string.language_table);
		fromLanguage = sharedPrefs.getString("from_language", getString(R.string.language_from_default));
		toLanguage = sharedPrefs.getString("to_language", getString(R.string.language_to_default));
		String fromLanguageLocal = fromLanguage, toLanguageLocal = toLanguage;
		for (int i = 0; i < LanguageValues.length; i++) {
			if (LanguageValues[i].equals(fromLanguage))
				fromLanguageLocal = LanguageEntries[i];
			else if (LanguageValues[i].equals(toLanguage))
				toLanguageLocal = LanguageEntries[i];
		}
		List<LanguageQuestion> questions = dbManager.getLanguageQuestions(min, max, answers.length, fromLanguage, toLanguage, ID);
		if (questions == null)
			return false;
		ID = questions.get(0).getID();

		// Set the new difficulty based on what question was picked
		difficulty = questions.get(0).getDifficulty().getValue();
		questionWorthMax = questions.get(0).getTimeSteps();
		decreaseRate = questions.get(0).getTimeStep();

		// Display the language question and answers
		decreaseRate = questions.get(0).getQuestionText().length();
		for (int i = 0; i < answers.length; i++) {
			answers[i] = questions.get(i).getCorrectAnswer();
			decreaseRate += answers[i].length();
		}
		decreaseRate = decreaseRate * 10;
		problem.setText(fromLanguageLocal + " → " + toLanguageLocal + "\n" + questions.get(0).getQuestionText());
		return true;
	}

	private boolean setEngineerProblem(Difficulty min, Difficulty max) {
		currentTableName = getString(R.string.engineer_table);
		EngineerQuestion question = dbManager.getEngineerQuestion(min, max, ID + 1);
		if (question == null)
			return false;
		ID = question.getID();

		// Set the new difficulty based on what question was picked
		difficulty = question.getDifficulty().getValue();
		questionWorthMax = question.getTimeSteps();
		decreaseRate = question.getTimeStep();

		problem.setText(question.getQuestionText());
		answers = question.getAnswers();
		return true;
	}

	private boolean setHiqHTriviaProblem(Difficulty min, Difficulty max) {
		currentTableName = getString(R.string.hiq_trivia_table);
		HiqTriviaQuestion question = dbManager.getHiqTriviaQuestion(min, max, ID);
		if (question == null)
			return false;
		ID = question.getID();

		// Set the new difficulty based on what question was picked
		difficulty = question.getDifficulty().getValue();
		questionWorthMax = question.getTimeSteps();
		decreaseRate = question.getTimeStep();

		problem.setText(question.getQuestionText());
		answers = question.getAnswers();
		return true;
	}

	private boolean setCustomProblem(String category, Difficulty min, Difficulty max) {
		currentTableName = getString(R.string.custom_table);
		CustomQuestion question = dbManager.getCustomQuestion(category, min, max, ID);
		if (question == null)
			return false;
		ID = question.getID();

		// Set the new difficulty based on what question was picked
		difficulty = 0;
		questionWorthMax = question.getTimeSteps();
		decreaseRate = question.getTimeStep();

		problem.setText(question.getQuestionText());
		answers = question.getAnswers();
		return true;
	}

	private boolean getUnlockedPackages() {
		for (int i = 0; i < unlockPackageKeys.length; i++)
			if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false)) {
				return true;
			}
		return false;
	}

	public int getEnabledPackages() {
		int count = 0;
		for (int i = 0; i < PackageKeys.size(); i++) {
			if (sharedPrefs.getBoolean(PackageKeys.get(i), false))
				count++;
		}
		return count;
	}

	private void displayCorrectOrNot(int correctLoc, int guessLoc, String description, boolean correct, boolean unknown) {
		if (unknown) {
			sendEvent("question", "question_answered", "unknown", (long) questionWorth);
			answerView.setCorrectGuess(correctLoc);
			joystick.setCorrectGuess(correctLoc);
		} else {
			answerView.setCorrectGuess(correctLoc);
			joystick.setCorrectGuess(correctLoc);
			dbManager.addStat(new Statistic(currentPack, String.valueOf(correct), Difficulty.fromValue(difficulty), System
					.currentTimeMillis(), startTime - System.currentTimeMillis()));
			if (correct) {
				sendEvent("question", "question_answered", "correct", (long) questionWorth);
				if (questionWorth > 0)
					playSound(PLAY_CORRECT);
				else
					playSound(PLAY_BEEP);
				problem.setTextColor(Color.GREEN);
				dMoney = Money.increaseMoney(questionWorth);
				dbManager.decreasePriority(currentTableName, fromLanguage, toLanguage, ID);
			} else {
				sendEvent("question", "question_answered", "incorrect", (long) questionWorth);
				playSound(PLAY_WRONG);
				answerView.setIncorrectGuess(guessLoc);
				joystick.setIncorrectGuess(guessLoc);
				problem.setTextColor(Color.RED);
				dMoney = Money.decreaseMoneyNoDebt(questionWorth);
				dbManager.increasePriority(currentTableName, fromLanguage, toLanguage, ID);
			}
			MoneyHelper.setMoney(this, coins, Money.getMoney(), Money.getMoneyPaid());
		}
	}

	private void JoystickSelected(int s, boolean vibrate, int Extra) {
		dialogOn = false;
		if (vibrate) {
			if (sharedPrefs.getBoolean("vibration", true)
					&& ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode() != AudioManager.RINGER_MODE_SILENT)
				vib.vibrate(50);	// vibrate for 50ms
		}
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
				if (Extra == 0)
					updateStats(false);
				joystick.pauseSelection();
				launchHomeScreen(1500);
			} else if ((answerLoc == s) && quizMode) {
				displayCorrectOrNot(answerLoc, s, "Correct!\n", true, false);
				if (Extra == 0)
					updateStats(true);
				joystick.pauseSelection();
				mHandler.removeCallbacksAndMessages(null);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						setProblemAndAnswer();
					}
				}, 500);
			} else if ((answerLoc == s) && !quizMode) {
				displayCorrectOrNot(answerLoc, s, "Correct!\n", true, false);
				if (Extra == 0)
					updateStats(true);
				joystick.pauseSelection();
				launchHomeScreen(100);
			} else {
				displayCorrectOrNot(answerLoc, s, "Wrong\n", false, false);
				if (Extra == 0)
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
						setProblemAndAnswer();
					}
				});
			}
			break;
		case 4:	// question mark(tell me the answer/I don't know) was selected
			playSound(PLAY_BEEP);
			resetStreak();
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[1], EggMaxValues[1]));
			displayCorrectOrNot(answerLoc, answerLoc, "", false, true);
			joystick.setWrongGuess();
			joystick.pauseSelection();
			joystick.setOnTouchedListener(new JoystickTouchListener() {
				@Override
				public void OnTouch() {
					joystick.removeTouchListener();
					joystick.resetWrongGuess();
					setProblemAndAnswer();
				}
			});
			break;
		case 5:		// info was selected
			// sendEvent("ui_action", "settings_selected", "info", null);
			playSound(PLAY_BEEP);
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[2], EggMaxValues[2]));
			// displayInfo(false);
			startActivity(new Intent(this, FriendActivity.class));
			break;
		case 6:		// Store was selected
			playSound(PLAY_BEEP);
			unlocking = false;
			startActivity(new Intent(this, ShowStoreActivity.class));
			break;
		case 7:		// progress was selected
			playSound(PLAY_BEEP);
			unlocking = false;
			startActivity(new Intent(this, ShowProgressActivity.class));
			break;
		case 8:		// quiz Mode was selected
			sendEvent("ui_action", "settings_selected", "quiz_mode", null);
			playSound(PLAY_BEEP);
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[3], EggMaxValues[3]));
			quizMode = joystick.setQuizMode(!quizMode);
			break;
		case 9:		// settings was selected
			playSound(PLAY_BEEP);
			fromSettings = true;
			unlocking = false;
			startActivity(new Intent(this, ShowSettingsActivity.class));
			break;
		case 10:	// missed the lock button
			if (getSharedPreferences("Stats", 0).getLong("totalTime", 0) == 0) {
				displayHints(0, true);
			}
			break;
		case 11:	// quickUnlock activated
			sendEvent("question", "double_tap", "quick_unlock", null);
			playSound(PLAY_BEEP);
			resetStreak();
			setApps();
			resetQuestionWorth(0);
			switch (Integer.parseInt(sharedPrefs.getString("type", getString(R.string.type_default)))) {
			case 0:
			case 1:
				answerLoc = 2;
				break;
			case 2:
				answerLoc = 0;
				break;
			}
			answerView.setQuickUnlock(true, answerLoc);
			joystick.moveCorrect(answerLoc);
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[13], EggMaxValues[13]));
			break;
		case 12:	// add app was selected
			playSound(PLAY_BEEP);
			selectApp();
			break;
		case 13:	// app was selected
			playSound(PLAY_BEEP);
			if (Extra < apps.size()) {
				sendEvent("apps", "launch_app", apps.get(Extra).name, null);
				startActivity(getPackageManager().getLaunchIntentForPackage(apps.get(Extra).packageName));
				finish();
			}
			break;
		case 14:	// remove app was selected
			sendEvent("apps", "delete_app", apps.get(Extra).name, null);
			playSound(PLAY_BEEP);
			removeAppFromAll(Extra);
			break;
		case 15:	// friend was selected
			playSound(PLAY_BEEP);
			unlocking = false;
			startActivity(new Intent(this, FriendActivity.class));
			break;
		}
	}

	private void resetStreak() {
		sharedPrefsStats = getSharedPreferences("Stats", 0);
		editorPrefsStats = sharedPrefsStats.edit();
		int currentStreak = sharedPrefsStats.getInt("currentStreak", 0);
		editorPrefsStats.putInt("streakToIncrease", streakToIncrease);
		if (currentStreak >= 0)
			editorPrefsStats.putInt("currentStreak", 0);
		editorPrefsStats.commit();
	}

	private void updateStats(boolean right) {
		sharedPrefsStats = getSharedPreferences("Stats", 0);
		editorPrefsStats = sharedPrefsStats.edit();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editorPrefs = sharedPrefs.edit();
		long ms = System.currentTimeMillis() - startTime;
		int correct = sharedPrefsStats.getInt("correct", 0);
		int wrong = sharedPrefsStats.getInt("wrong", 0);
		int total = correct + wrong + 1;
		int coinsPlusMinus = sharedPrefsStats.getInt("coins", 0);
		int bestStreak = sharedPrefsStats.getInt("bestStreak", 0);
		int currentStreak = sharedPrefsStats.getInt("currentStreak", 0);
		long totalTime = sharedPrefsStats.getLong("totalTime", 0);
		long totalDifficulty = sharedPrefsStats.getLong("totalDifficulty", -1);
		if (totalDifficulty == -1)
			if (total > 1)
				totalDifficulty = dbManager.getTotalDifficulty();
			else
				totalDifficulty = 0;
		Log.d("GAtest", "totalDifficulty = " + totalDifficulty);
		long answerTimeFast = sharedPrefsStats.getLong("answerTimeFast", Long.MAX_VALUE);
		if (right) {
			editorPrefsStats.putInt("correct", correct + 1);
			editorPrefsStats.putInt("coins", coinsPlusMinus + dMoney);
			if (currentStreak >= bestStreak) {
				editorPrefsStats.putInt("bestStreak", bestStreak + 1);
				editorPrefsStats.putInt("currentStreak", currentStreak + 1);
			} else if (currentStreak >= 0)
				editorPrefsStats.putInt("currentStreak", currentStreak + 1);
			else
				editorPrefsStats.putInt("currentStreak", 1);
			if ((currentStreak + 1) >= sharedPrefsStats.getInt("streakToIncrease", streakToIncrease)) {
				int currentMax = Integer.parseInt(sharedPrefs.getString("difficulty_max", "0"));
				if ((currentMax < 5) && (sharedPrefs.getBoolean("algorithm", true))) {
					int max = Math.min(5, currentMax + 1);
					editorPrefs.putString("difficulty_max", String.valueOf(max)).commit();
					Toast.makeText(this, getString(R.string.difficulty_increased), Toast.LENGTH_SHORT).show();
				} else {
					switch (currentMax) {
					case 0:
						Toast.makeText(this, getString(R.string.difficulty_increase0), Toast.LENGTH_LONG).show();
						break;
					case 1:
						Toast.makeText(this, getString(R.string.difficulty_increase1), Toast.LENGTH_LONG).show();
						break;
					case 2:
						Toast.makeText(this, getString(R.string.difficulty_increase2), Toast.LENGTH_LONG).show();
						break;
					case 3:
						Toast.makeText(this, getString(R.string.difficulty_increase3), Toast.LENGTH_LONG).show();
						break;
					case 4:
						Toast.makeText(this, getString(R.string.difficulty_increase4), Toast.LENGTH_LONG).show();
						break;
					case 5:
						Toast.makeText(this, getString(R.string.difficulty_increase5), Toast.LENGTH_LONG).show();
						break;
					}
				}
				editorPrefsStats.putInt("streakToIncrease", currentStreak + streakToIncrease);
			}
			if (answerTimeFast > ms)
				editorPrefsStats.putLong("answerTimeFast", ms);
			if (streakToNotify.contains(currentStreak + 1)) {
				int number = sharedPrefsStats.getInt("streak" + (currentStreak + 1), 0);
				int value = (currentStreak + 1) / 6 * (difficulty + 1);
				editorPrefsStats.putInt("streak" + (currentStreak + 1), number + 1);
				updateMoney(value);
				new NotificationHelper(this).sendNotification((currentStreak + 1) + " " + getString(R.string.notification_title_streak),
						getString(R.string.notification_message_streak), number + 1,
						getString(R.string.notification_title_streak_facebook1) + " " + (currentStreak + 1) + " "
								+ getString(R.string.notification_title_streak_facebook2),
						getString(R.string.notification_message_streak_facebook), 0);
			}
		} else {
			editorPrefsStats.putInt("streakToIncrease", streakToIncrease);
			editorPrefsStats.putInt("wrong", wrong + 1);
			editorPrefsStats.putInt("coins", coinsPlusMinus + dMoney);
			if (currentStreak >= 0)
				editorPrefsStats.putInt("currentStreak", 0);
			else
				editorPrefsStats.putInt("currentStreak", currentStreak - 1);
		}
		editorPrefsStats.putLong("totalTime", totalTime + ms);
		editorPrefsStats.putLong("answerTimeAve", (totalTime + ms) / total);
		editorPrefsStats.putLong("totalDifficulty", totalDifficulty + difficulty);
		editorPrefsStats.putInt("difficultyAve", (int) ((totalDifficulty + difficulty) / total));
		if (totalToNotify.contains(total)) {
			int value = total / 15;
			updateMoney(value);
			new NotificationHelper(this).sendNotification(total + " " + getString(R.string.notification_title_total),
					getString(R.string.notification_message_streak), value, getString(R.string.notification_title_total_facebook1) + " "
							+ total + " " + getString(R.string.notification_title_total_facebook2),
					getString(R.string.notification_message_total_facebook), 1);
		}
		editorPrefsStats.commit();
	}

	private void displayInfo(boolean first) {
		if (!dialogOn) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			if (first) {
				builder.setTitle(R.string.info_title_first);
				builder.setMessage(getString(R.string.info_message_first)).setCancelable(false);
				builder.setPositiveButton(R.string.goto_store, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialogOn = false;
						startActivity(new Intent(getApplicationContext(), ShowStoreActivity.class));
					}
				});
			} else {
				final String link = ShareHelper.buildShareURL(this);
				builder.setTitle(R.string.info_title);
				builder.setMessage(R.string.info_message).setCancelable(false);
				builder.setPositiveButton(R.string.share_with_other, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialogOn = false;
						// TODO make this work for images, currently null is passed as the image, like to pass app thumbnail
						// String fileName = "android.resource://" + MainActivity.this.getPackageName() + "/" + R.drawable.ic_launcher;
						// String fileName = "content://" + MainActivity.this.getPackageName() + "/ic_launcher.png";
						// ShareHelper.share(ctx, null, null, getString(R.string.share_message), link);
						ShareHelper.share(ctx, null, null, ctx.getString(R.string.share_message), link);
						fromShare = true;
					}
				});
				builder.setNeutralButton(R.string.rate, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialogOn = false;
						// open app in the Play store
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("market://details?id=com.olyware.mathlock"));
						startActivity(intent);
						fromPlay = true;
					}
				});
				builder.setNegativeButton(R.string.share_with_facebook, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialogOn = false;
						ShareHelper.shareFacebook(ctx);
						fromShare = true;
					}
				});
			}
			builder.setCancelable(true);
			AlertDialog alert = builder.create();
			dialogOn = true;
			alert.show();
			int w = sharedPrefs.getInt("layout_width", 0);
			int h = sharedPrefs.getInt("layout_height", 0);
			if (!first && w > 0 && h > 0)
				alert.getWindow().setLayout(w, h * 2 / 3);
		}
	}

	private void displayRateShare() {
		if (!dialogOn) {
			sharedPrefsMoney = getSharedPreferences("Packages", 0);
			editorPrefsMoney = sharedPrefsMoney.edit();
			editorPrefsMoney.putLong("lastTime", System.currentTimeMillis()).commit();
			final String link = ShareHelper.buildShareURL(this);
			boolean initial[] = { dontShow };
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
					fromPlay = true;
				}
			});
			builder.setNeutralButton(R.string.share_with_facebook, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					sharedPrefsMoney = getSharedPreferences("Packages", 0);
					editorPrefsMoney = sharedPrefsMoney.edit();
					editorPrefsMoney.putBoolean("dontShowLastTime", dontShow).commit();
					dialogOn = false;
					// TODO make this work for images, currently null is passed as the image
					// ShareHelper.share(ctx, null, null, getString(R.string.share_message), link);
					ShareHelper.shareFacebook(ctx);
					fromShare = true;
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

	private void displayHints(int hint, final boolean noMore) {
		if (!dialogOn) {
			final int h = hint;
			joystick.showHint(h);
			if (h < hints.length) {
				if (h == 0) {
					SharedPreferences.Editor editorPrefs = sharedPrefs.edit();
					editorPrefs.putBoolean("hints", false).commit();
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.hint_title).setCancelable(false);
				builder.setMessage(hints[hint]).setCancelable(false);
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialogOn = false;
						if (noMore)
							joystick.showHint(-1);
						else
							displayHints(h + 1, false);
					}
				});
				if ((h > 0) && (!noMore))
					builder.setNegativeButton(R.string.no_more_hints, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialogOn = false;
							joystick.showHint(-1);
						}
					});
				builder.create().show();
				dialogOn = true;
			}
		}
	}

	private void updatePaidMoney(int amount) {
		Money.increaseMoneyPaid(amount);
		MoneyHelper.setMoney(this, coins, Money.getMoney(), Money.getMoneyPaid());
	}

	private void updateMoney(int amount) {
		Money.increaseMoney(amount);
		MoneyHelper.setMoney(this, coins, Money.getMoney(), Money.getMoneyPaid());
	}

	private void selectApp() {
		// List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
		// packages.get(0).loadIcon(pm);//this gets a drawable
		// packages.get(0).packageName;//this gets the package name
		// pm.getLaunchIntentForPackage(packages.get(0).packageName);//this gets an intent to start activity
		Intent mainIntent = new Intent(android.content.Intent.ACTION_MAIN);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
		pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
		startActivityForResult(pickIntent, R.id.REQUEST_PICK_APP);
	}

	private void playSound(int ID) {
		if (!locked) {
			if (ID == PLAY_CORRECT) {
				if (answerCorrectClick.isPlaying()) {
					answerCorrectClick.reset();
					answerCorrectClick = MediaPlayer.create(this, R.raw.answer_correct);
				}
				answerCorrectClick.start();
			} else if (ID == PLAY_WRONG) {
				if (answerIncorrectClick.isPlaying()) {
					answerIncorrectClick.reset();
					answerIncorrectClick = MediaPlayer.create(this, R.raw.answer_incorrect);
				}
				answerIncorrectClick.start();
			} else if (ID == PLAY_BEEP) {
				if (buttonClick.isPlaying()) {
					buttonClick.reset();
					buttonClick = MediaPlayer.create(this, R.raw.button_click);
				}
				buttonClick.start();
			}
		}
	}

	private void sendEvent(String category, String action, String label, Long value) {
		if (action.equals("question_answered")) {
			if (quizMode)
				action = "quiz_mode_" + action;
			if (answerView.getQuickUnlock())
				action = "quick_unlock_" + action;
		}
		MyApplication.getGaTracker().send(MapBuilder.createEvent(category, action, label, value).build());
	}

	public void registrationResult(int result) {
		Log.d("GAtest", "upload result = " + result);
		/*Log.d("GAtest", "userID = " + userID);
		SharedPreferences sharedPrefsUserInfo = getSharedPreferences(getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		SharedPreferences.Editor editPrefsUserInfo = sharedPrefsUserInfo.edit();
		if ((result == 0) && (userID != null)) {
			editPrefsUserInfo.putBoolean(getString(R.string.pref_user_reg_uploaded), true);
			editPrefsUserInfo.putString(getString(R.string.pref_user_userid), userID);
		} else if (result == 1) {
			editPrefsUserInfo.putBoolean(getString(R.string.pref_user_reg_uploaded), false);
		}
		editPrefsUserInfo.commit();*/
	}
}
