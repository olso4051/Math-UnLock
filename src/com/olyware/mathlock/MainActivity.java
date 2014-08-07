package com.olyware.mathlock;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.olyware.mathlock.adapter.ChallengeData;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.dialog.ChallengeDialog;
import com.olyware.mathlock.dialog.ChallengeNewDialog;
import com.olyware.mathlock.dialog.ChallengeNewDialog.OnAcceptOrDeclineListener;
import com.olyware.mathlock.dialog.QuestionDialog;
import com.olyware.mathlock.dialog.QuestionDialog.QuestionDialogListener;
import com.olyware.mathlock.model.ChallengeQuestion;
import com.olyware.mathlock.model.CustomQuestion;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.EngineerQuestion;
import com.olyware.mathlock.model.GenericQuestion;
import com.olyware.mathlock.model.HiqTriviaQuestion;
import com.olyware.mathlock.model.LanguageQuestion;
import com.olyware.mathlock.model.MathQuestion;
import com.olyware.mathlock.model.Statistic;
import com.olyware.mathlock.model.VocabQuestion;
import com.olyware.mathlock.service.AcceptChallenge;
import com.olyware.mathlock.service.CancelChallenge;
import com.olyware.mathlock.service.CompleteChallenge;
import com.olyware.mathlock.service.CustomContactData;
import com.olyware.mathlock.service.RemindChallenge;
import com.olyware.mathlock.service.ScreenService;
import com.olyware.mathlock.service.SendChallenge;
import com.olyware.mathlock.ui.Typefaces;
import com.olyware.mathlock.utils.ChallengeBuilder;
import com.olyware.mathlock.utils.Clock;
import com.olyware.mathlock.utils.Coins;
import com.olyware.mathlock.utils.ContactHelper;
import com.olyware.mathlock.utils.EZ;
import com.olyware.mathlock.utils.EggHelper;
import com.olyware.mathlock.utils.EncryptionHelper;
import com.olyware.mathlock.utils.GCMHelper;
import com.olyware.mathlock.utils.IabHelper;
import com.olyware.mathlock.utils.IabResult;
import com.olyware.mathlock.utils.Inventory;
import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.MoneyHelper;
import com.olyware.mathlock.utils.NotificationHelper;
import com.olyware.mathlock.utils.PreferenceHelper;
import com.olyware.mathlock.utils.PreferenceHelper.ChallengeStatus;
import com.olyware.mathlock.utils.Purchase;
import com.olyware.mathlock.utils.ShareHelper;
import com.olyware.mathlock.utils.Toaster;
import com.olyware.mathlock.views.EquationView;
import com.olyware.mathlock.views.JoystickSelect;
import com.olyware.mathlock.views.JoystickSelectListener;
import com.olyware.mathlock.views.JoystickView;
import com.tapjoy.TapjoyConnect;

public class MainActivity extends FragmentActivity implements LoginFragment.OnFinishedListener, GCMHelper.GCMResponse {
	final private int startingPmoney = 0, streakToIncrease = 40;
	final private Coins Money = new Coins(0, 0);
	final private static int[] Cost = { 1000, 5000, 10000 };
	final private static String[] SKU = { "coins1000", "coins5000", "coins10000" };
	final private String[] answersNone = { "", "", "", "" };
	final private static String SCREEN_LABEL = "Home Screen", LOGIN_LABEL = "Login Screen";
	final private static int REQUEST_PICK_APP = 42;
	public final static int INVITE_SENT = 142;

	private int dMoney;// change in money after a question is answered
	private int difficultyMax = 0, difficultyMin = 0, difficulty = 0;
	private long startTime = 0;
	private boolean fromSettings = false, fromPlay = false, fromShare = false, fromDeepLink = false, fromChallenge = false,
			fromTutorial = false;

	private LinearLayout layout;
	private Clock clock;
	private TextView clockTextView, coins, questionDescription;
	private int questionWorthMax = 0, questionWorth = 0, decreaseRate = 500, backgroundState = 0;
	private EquationView problem;
	private Drawable imageLeft;	// left,top,right,bottom
	private TransitionDrawable backgroundTransition;
	private boolean quizMode = false;
	private JoystickView joystick;
	private int defaultTextColor;

	private List<String> customCategories, PackageKeys, displayAllPackageKeys;
	private List<Integer> streakToNotify, totalToNotify;
	private String[] unlockPackageKeys, LanguageEntries, LanguageValues, EggKeys;
	private int[] EggMaxValues;
	private String currentPack, currentTableName, fromLanguage, toLanguage, questionFromDeepLink, challengeIDToDisplay;
	private long ID = 0;

	private int EnabledPackages = 0, fromTutorialPosition = -1;
	private boolean loggedIn, info, challenge, locked, unlocking, UnlockedPackages = false;
	private boolean dialogOn = false, dontShow = false, paused = false, isWallpaperShown = false;
	final private long MONTH = 2592000000l, WEEK = 604800000l, DAY = 86400000l;

	private int answerLoc = 0;		// {correct answer location}
	private String answers[] = { "3", "1", "2", "4" };	// {correct answer, wrong answers...}
	private String[] answersFromDeepLink = new String[4];
	private String answersRandom[] = { "4", "2", "3", "1" };	// {answers in random order}
	private int attempts = 1;

	private Vibrator vib;
	private Random rand = new Random(); // Ideally just create one instance globally

	private SharedPreferences sharedPrefs, sharedPrefsMoney, sharedPrefsStats, sharedPrefsApps;
	private SharedPreferences.Editor editorPrefsMoney, editorPrefsStats;

	private Tracker trackerGA;
	private Handler mHandler, timerHandler;
	private Runnable reduceWorth;
	private boolean screenViewed = false;

	private DatabaseManager dbManager;
	private static Context ctx;

	private Typefaces typefaces;

	private IabHelper mHelper;
	private IabHelper.QueryInventoryFinishedListener mQueryFinishedListener;
	private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener;

	private List<ApplicationInfo> apps;

	private LoginFragment loginFragment;
	private UiLifecycleHelper uiHelper;
	private Bitmap HelpQuestionImage;
	private ProgressDialog progressDialog;

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
			// At this point you should start the login activity and finish this one
			finish();
		}
	};

	private BroadcastReceiver screenOnBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// screen has come on
			trackerGA.set(Fields.SESSION_CONTROL, "start");
			trackerGA.set(Fields.SCREEN_NAME, SCREEN_LABEL);
			startCountdown();
			// showWallpaper();
		}
	};

	private BroadcastReceiver challengeCompleteBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// provision coins to the user
			if (loggedIn) {
				provisionPendingMoney();
				if (joystick != null) {
					joystick.setNumberOfChallenges(ContactHelper.getNumberOfChallenges(MainActivity.this));
				}
			}
		}
	};

	private class OpenDatabase extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			dbManager = new DatabaseManager(getApplicationContext());
			customCategories = dbManager.getAllCustomCategories();
			PackageKeys = EZ.list(getResources().getStringArray(R.array.enable_package_keys));
			displayAllPackageKeys = EZ.list(getResources().getStringArray(R.array.display_packages));
			for (String cat : customCategories) {
				PackageKeys.add(getString(R.string.custom_enable) + cat);
				displayAllPackageKeys.add(cat);
			}
			UnlockedPackages = isAnyPackageUnlocked();
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

	/*private class BlurBackground extends AsyncTask<Bitmap, Void, BitmapDrawable> {
		private Context ctx;

		BlurBackground(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		protected BitmapDrawable doInBackground(Bitmap... bmps) {
			String wallpaper = ctx.getString(R.string.wallpaper);
			String blurred = ctx.getString(R.string.blurred_background);
			// save the original wallpaper
			long time = System.currentTimeMillis();
			SaveHelper.SaveBitmapPrivate(ctx, bmps[0], wallpaper);

			// blur bitmap
			final RenderScript rs = RenderScript.create(ctx);
			final Allocation input = Allocation
					.createFromBitmap(rs, bmps[0], Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
			final Allocation output = Allocation.createTyped(rs, input.getType());
			final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
			script.setRadius(25f);
			script.setInput(input);
			script.forEach(output);
			output.copyTo(bmps[0]);

			// dim the bitmap
			final RenderScript rs2 = RenderScript.create(ctx);
			final ScriptC_dim scriptDim = new ScriptC_dim(rs2);
			scriptDim.set_dimmingValue(0.75f);
			final Allocation alloc1 = Allocation.createFromBitmap(rs2, bmps[0], Allocation.MipmapControl.MIPMAP_NONE,
					Allocation.USAGE_SCRIPT);
			final Allocation alloc2 = Allocation.createTyped(rs2, alloc1.getType());
			scriptDim.forEach_dim(alloc1, alloc2);
			alloc2.copyTo(bmps[0]);

			// save bitmap to internal private storage
			time = System.currentTimeMillis();
			SaveHelper.SaveBitmapPrivate(ctx, bmps[0], blurred);

			// convert bitmap to BitmapDrawable so we can set it as the background of a view
			BitmapDrawable bDrawable = new BitmapDrawable(getResources(), bmps[0]);
			return bDrawable;
		}
	}*/

	@SuppressLint("NewApi")
	public void restart() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPrefs.edit().putBoolean("from_login", true).commit();
		if (loginFragment != null)
			getSupportFragmentManager().beginTransaction().remove(loginFragment).commit();
		if (android.os.Build.VERSION.SDK_INT < 11) {
			Intent i = new Intent(this, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			finish();
			startActivity(i);
		} else {
			recreate();
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getDeepLinkData(getIntent().getData());
		trackerGA = MyApplication.getGaTracker();
		TapjoyConnect.requestTapjoyConnect(this, "937ee2a5-b377-4ed3-8156-16f635e69749", "m7lfX2V6hofuY9pKz34t");

		// Add code to print out the key hash
		/*try {
			PackageInfo info = getPackageManager().getPackageInfo("com.olyware.mathlock", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				SaveHelper.SaveTextFilePublic("KeyHash.txt", "keyhash = " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {

		} catch (NoSuchAlgorithmException e) {

		}*/

		// check if user is logged in, if not display loginscreen
		SharedPreferences sharedPrefsUserInfo = getSharedPreferences(getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		loggedIn = sharedPrefsUserInfo.getBoolean(getString(R.string.pref_user_logged_in), false);
		if (!loggedIn) {
			Settings.addLoggingBehavior(LoggingBehavior.REQUESTS);
			setTheme(R.style.LoginTheme);
			if (savedInstanceState == null) {
				loginFragment = new LoginFragment();
				Bundle args = new Bundle();
				args.putBoolean("facebook_logout", getIntent().getBooleanExtra("facebook_logout", false));
				loginFragment.setArguments(args);
				getSupportFragmentManager().beginTransaction().add(android.R.id.content, loginFragment).commit();
			} else {
				// Or set the fragment from restored state info
				loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
			}
			GCMHelper.registerGCM(this, getApplicationContext());
		} else {
			setTheme(R.style.AppThemeWall);
			setContentView(R.layout.activity_main);

			uiHelper = new UiLifecycleHelper(this, null);
			uiHelper.onCreate(savedInstanceState);

			IntentFilter logoutFilter = new IntentFilter(getString(R.string.logout_receiver_filter));
			LocalBroadcastManager.getInstance(this).registerReceiver(finishBroadcast, logoutFilter);
			IntentFilter screenOnFilter = new IntentFilter(getString(R.string.screen_on_receiver_filter));
			LocalBroadcastManager.getInstance(this).registerReceiver(screenOnBroadcast, screenOnFilter);
			IntentFilter challengeFilter = new IntentFilter(getString(R.string.challenge_receiver_filter));
			LocalBroadcastManager.getInstance(this).registerReceiver(challengeCompleteBroadcast, challengeFilter);

			ctx = this;
			info = getIntent().getBooleanExtra("info", false);
			challenge = getIntent().getBooleanExtra(NotificationHelper.EXTRA_OPEN_CHALLENGE, false);
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

			PreferenceHelper.storeLayoutParams(this);

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
			BitmapDrawable gradient = new BitmapDrawable(getResources(), getWallpaperImage(false));
			BitmapDrawable dim = new BitmapDrawable(getResources(), getWallpaperImage(true));
			backgroundTransition = new TransitionDrawable(new Drawable[] { gradient, dim });
			setLayoutBackground(layout, backgroundTransition);

			typefaces = Typefaces.getInstance(this);
			EZ.setFont((ViewGroup) layout, typefaces.robotoLight);

			mHandler = new Handler();

			PackageKeys = EZ.list(getResources().getStringArray(R.array.enable_package_keys));
			displayAllPackageKeys = EZ.list(getResources().getStringArray(R.array.display_packages));
			LanguageEntries = getResources().getStringArray(R.array.language_entries);
			unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
			LanguageValues = getResources().getStringArray(R.array.language_values_not_localized);
			EggKeys = getResources().getStringArray(R.array.egg_keys);
			EggMaxValues = getResources().getIntArray(R.array.egg_max_values);

			clockTextView = (TextView) findViewById(R.id.clock);
			coins = (TextView) findViewById(R.id.money);
			clock = new Clock(this, clockTextView, coins);
			questionDescription = (TextView) findViewById(R.id.description);
			problem = (EquationView) findViewById(R.id.problem);
			defaultTextColor = problem.getTextColors().getDefaultColor();

			joystick = (JoystickView) findViewById(R.id.joystick);
			joystick.setOnJostickSelectedListener(new JoystickSelectListener() {
				@Override
				public void OnSelect(JoystickSelect s, boolean vibrate, int Extra) {
					boolean setProblem = false;
					switch (fromTutorialPosition) {
					case 0:	// Press the Lock
						if (s == JoystickSelect.A)
							PreferenceHelper.setTutorialQuestion(MainActivity.this, 1);
						break;
					case 1: // Double tap the Lock To Quickly Unlock Your Device \n (slide to "+" to add your favorite apps)
						if (s == JoystickSelect.QuickUnlock)
							PreferenceHelper.setTutorialQuestion(MainActivity.this, 2);
						break;
					case 2: // Challenge your friends \n (slide the icon up)
						if (s == JoystickSelect.Friends || s == JoystickSelect.A) {
							PreferenceHelper.setTutorialQuestion(MainActivity.this, 3);
							setProblem = true;
						}
						break;
					case 3: // Change Any Setting at any time \n (slide the icon up)
						if (s == JoystickSelect.Settings || s == JoystickSelect.A)
							PreferenceHelper.setTutorialQuestion(MainActivity.this, 4);
						break;
					case 4: // Lockscreen Frequency
						if (s == JoystickSelect.A || s == JoystickSelect.B || s == JoystickSelect.C || s == JoystickSelect.D) {
							boolean continueQuizMode = PreferenceHelper.setLockscreenFrequency(MainActivity.this, s);
							quizMode = joystick.setQuizMode(continueQuizMode);
							PreferenceHelper.setTutorialQuestion(MainActivity.this, 5);
						}
						break;
					case 5: // Quiz mode for endless questions (slide the icon up)
						if (s == JoystickSelect.QuizMode || s == JoystickSelect.A) {
							PreferenceHelper.setTutorialQuestion(MainActivity.this, 6);
							setProblem = true;
						}
						break;
					case 6: // Check your progress (slide the icon up)
						if (s == JoystickSelect.Progress || s == JoystickSelect.A)
							PreferenceHelper.setTutorialQuestion(MainActivity.this, 7);
						break;
					case 7: // Unlock more question packs (slide the icon up)
						if (s == JoystickSelect.Store || s == JoystickSelect.A)
							PreferenceHelper.setTutorialDone(MainActivity.this);
						break;
					}
					JoystickSelected(s, vibrate, Extra);
					if (setProblem) {
						setProblemAndAnswer();
					}
				}
			});
			boolean fromLogin = sharedPrefs.getBoolean("from_login", true);
			quizMode = joystick.setQuizMode(!locked && !fromLogin);
			if (fromLogin)
				sharedPrefs.edit().putBoolean("from_login", false).commit();

			timerHandler = new Handler();
			reduceWorth = new Runnable() {
				@Override
				public void run() {
					if (!paused) {
						questionWorth -= 1;
						if (questionWorth <= 0) {
							questionWorth = 0;
							/*if (attached)
								getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
							if (!joystick.getQuickUnlock()) {
								HelpQuestionImage = takeScreenShot();
								if (rand != null)
									if (!fromChallenge && !fromTutorial && rand.nextBoolean())
										joystick.askToShare();
							}
						} else {
							timerHandler.postDelayed(this, decreaseRate);
							/*if (attached)
								getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
						}
					}
				}
			};

			apps = new ArrayList<ApplicationInfo>();

			new OpenDatabase().execute();

			// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
			GCMHelper.registerAndStoreGCM(this, getApplicationContext());
		}
	}

	@Override
	public void onAttachedToWindow() {
		if (loggedIn) {
			// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		}
	}

	@Override
	protected void onPause() {
		screenViewed = false;
		if (loggedIn) {
			paused = true;

			ChallengeDialog challengeDialog = (ChallengeDialog) getSupportFragmentManager().findFragmentByTag(ChallengeDialog.TAG);
			QuestionDialog questionDialog = (QuestionDialog) getSupportFragmentManager().findFragmentByTag(QuestionDialog.TAG);
			ChallengeNewDialog challengeNewDialog = (ChallengeNewDialog) getSupportFragmentManager().findFragmentByTag(
					ChallengeNewDialog.TAG);
			if (challengeDialog != null) {
				challengeDialog.dismiss();
			}
			if (questionDialog != null) {
				questionDialog.dismiss();
			}
			if (challengeNewDialog != null) {
				challengeNewDialog.dismiss();
			}

			sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			sharedPrefs.edit().putLong("timeout", System.currentTimeMillis()).commit();
			sharedPrefsMoney = getSharedPreferences("Packages", 0);
			editorPrefsMoney = sharedPrefsMoney.edit();
			editorPrefsMoney.putInt("money", Money.getMoney());
			editorPrefsMoney.putInt("paid_money", Money.getMoneyPaid());
			if (!sharedPrefsMoney.getBoolean("dontShowLastTime", false))
				editorPrefsMoney.putBoolean("dontShowLastTime", dontShow);
			editorPrefsMoney.commit();

			joystick.removeCallbacks();

			uiHelper.onPause();
		}
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		System.gc();
		super.onPause();
	}

	@Override
	protected void onStop() {
		if (loggedIn) {
			joystick.removeCallbacks();
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (loggedIn) {
			clock.destroy();
			if (mHelper != null) {
				try {
					mHelper.dispose();
				} catch (java.lang.IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
			mHelper = null;
			if (dbManager != null)
				if (!dbManager.isDestroyed())
					dbManager.destroy();
			joystick.removeCallbacks();
			LocalBroadcastManager.getInstance(this).unregisterReceiver(finishBroadcast);
			LocalBroadcastManager.getInstance(this).unregisterReceiver(screenOnBroadcast);
			LocalBroadcastManager.getInstance(this).unregisterReceiver(challengeCompleteBroadcast);

			uiHelper.onDestroy();
		}
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (loggedIn)
			uiHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		long currentTime = System.currentTimeMillis();
		paused = false;
		super.onResume();
		AppEventsLogger.activateApp(this, getString(R.string.facebook_app_id));
		GCMHelper.checkPlayServices(this);

		if (loggedIn) {
			uiHelper.onResume();

			joystick.setNumberOfChallenges(ContactHelper.getNumberOfChallenges(this));
			if (locked && quizMode && !fromTutorial)
				quizMode = joystick.setQuizMode(false);

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
				provisionPendingMoney();
			}
			coins.setText(String.valueOf(Money.getTotalMoney()));

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
			UnlockedPackages = isAnyPackageUnlocked();
			EnabledPackages = getEnabledPackages();

			// start background service to wait for screen to turn off. if service is already running startService does nothing
			Intent sIntent = new Intent(this, ScreenService.class);
			if (((EnabledPackages > 0) || (dbManager == null)) && (sharedPrefs.getBoolean("lockscreen", true))) {
				this.startService(sIntent);
			} else {
				this.stopService(sIntent);
			}

			// setup the question and answers
			if (!unlocking) {
				setProblemAndAnswer();
			}

			if (!UnlockedPackages)
				displayInfo(true);
			else if (challenge) {
				challenge = false;
				displayFriends();
			} else if (info) {
				info = false;
				displayInfo(false);
			} else if ((!sharedPrefsMoney.getBoolean("dontShowLastTime", false))
					&& (sharedPrefsMoney.getLong("lastTime", 0) <= currentTime - MONTH))
				displayRateShare();
			else if (fromTutorial) {
				setProblemAndAnswer();
			}

			// Backup preferences every day
			if (sharedPrefs.getLong("lastTimeBackup", 0) <= currentTime - DAY) {
				sharedPrefs.edit().putLong("lastTimeBackup", currentTime).commit();
				EZ.requestBackup(this);
			}

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

			// save money into shared preferences
			MoneyHelper.setMoney(this, coins, Money.getMoney(), Money.getMoneyPaid());

			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			if (pm.isScreenOn()) {
				trackerGA.set(Fields.SESSION_CONTROL, "start");
				trackerGA.set(Fields.SCREEN_NAME, SCREEN_LABEL);
				joystick.startAnimations();
				new NotificationHelper(this).clearChallengeResultNotification();
				new NotificationHelper(this).clearChallengeNotification();

				// showWallpaper();
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (loggedIn) {
			if (locked && UnlockedPackages && !quizMode) {		// if locked then don't allow back button to exit app
				return;
			} else {
				super.onBackPressed();
			}
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (locked && loggedIn) {
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
		if (loggedIn) {
			challenge = intent.getBooleanExtra(NotificationHelper.EXTRA_OPEN_CHALLENGE, false);
			info = intent.getBooleanExtra("info", false);
			locked = intent.getBooleanExtra("locked", false);
			unlocking = locked;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (loggedIn) {
			joystick.showStartAnimation(0, 3000);
			return false;
		} else
			return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (loggedIn) {
			uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
				@Override
				public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
				}

				@Override
				public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
					boolean didFinishNormal = FacebookDialog.getNativeDialogDidComplete(data);
					String completionGesture = FacebookDialog.getNativeDialogCompletionGesture(data);
					// String postID = FacebookDialog.getNativeDialogPostId(data);
					if (didFinishNormal && completionGesture.equals("post")) {
						ShareHelper.confirmShare(MainActivity.this);
						Money.increaseMoney(EggHelper.unlockEgg(MainActivity.this, coins, EggKeys[8], EggMaxValues[8]));
					}
				}
			});
			if (resultCode == RESULT_OK) {
				if (requestCode == REQUEST_PICK_APP) {
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
								sendEvent("apps", "add_app", pack.packageName, null);
							}
						}
					}
				} else if (requestCode == INVITE_SENT) {
					// never gets called resultCode never equal RESULT_OK
				}
			}
		} else
			super.onActivityResult(requestCode, resultCode, data);
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

	@SuppressLint("NewApi")
	private void setLayoutBackground(LinearLayout layout, TransitionDrawable background) {
		if (android.os.Build.VERSION.SDK_INT < 16)
			layout.setBackgroundDrawable(background);
		else
			layout.setBackground(background);
	}

	private Bitmap getWallpaperImage(boolean dimOrGradient) {

		int w = PreferenceHelper.getLayoutWidth(this, -1);
		int h = PreferenceHelper.getLayoutHeight(this, -1);
		int statusBarHeight = PreferenceHelper.getLayoutStatusBarHeight(this, -1);
		if (w > 0 && h > 0 && statusBarHeight >= 0) {
			try {
				BitmapDrawable background = (BitmapDrawable) WallpaperManager.getInstance(this).getDrawable();

				// get wallpaper as a bitmap need two references since the blurred image is put back in the first reference
				Bitmap bitmap = background.getBitmap();

				// set scaling factors
				int x = bitmap.getWidth() / 2 - w / 2;
				int y = bitmap.getHeight() / 2 - h / 2 + statusBarHeight;
				int width = w;
				int height = h - statusBarHeight;

				if (x < 0)
					x = 0;
				if (width > bitmap.getWidth())
					width = bitmap.getWidth();
				if (x + width > bitmap.getWidth())
					width = bitmap.getWidth() - x;

				if (y < 0)
					y = 0;
				if (height > bitmap.getHeight())
					height = bitmap.getHeight();
				if (y + height > bitmap.getHeight())
					height = bitmap.getHeight() - y;

				if (x >= 0 && x <= bitmap.getWidth() && y >= 0 && y <= bitmap.getHeight() && width > 0 && height > 0 && x + width > 0
						&& x + width <= bitmap.getWidth() && y + height > 0 && y + height <= bitmap.getHeight()) {
					// scale the bitmap to fit on the background
					bitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
				} else {
					// Should never happen but user reported y>bitmap.getHeight so here it is and x<0
					bitmap.recycle();
					return getDimOrGradient(dimOrGradient);
				}

				if (!bitmap.isMutable()) {
					// bitmap not mutable trying to create mutable one"
					bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
					if (!bitmap.isMutable()) {
						// bitmap still not mutable return default
						bitmap.recycle();
						return getDimOrGradient(dimOrGradient);
					}
				}

				Canvas canvas = new Canvas(bitmap);
				canvas.drawBitmap(bitmap, 0, 0, null);
				Rect dstRectForOpt = new Rect();
				dstRectForOpt.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
				Paint optPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
				optPaint.setStyle(Paint.Style.FILL);
				if (dimOrGradient) {
					canvas.drawARGB(150, 0, 0, 0);
				} else {
					optPaint.setShader(new LinearGradient(0, 0, 0, h, new int[] { Color.argb(150, 0, 0, 0), Color.argb(150, 0, 0, 0),
							Color.argb(0, 0, 0, 0) }, new float[] { 0, 0.3f, 1 }, TileMode.MIRROR));
					canvas.drawRect(dstRectForOpt, optPaint);
				}

				return bitmap;
			} catch (SecurityException e) {
				return getDimOrGradient(dimOrGradient);
			} catch (OutOfMemoryError e) {
				return getDimOrGradient(dimOrGradient);
			}
		}
		return getDimOrGradient(dimOrGradient);
	}

	private Bitmap getDimOrGradient(boolean dimOrGradient) {
		int h = 1000;
		Bitmap b = Bitmap.createBitmap(1, h, Bitmap.Config.ALPHA_8);
		Rect dstRectForOpt = new Rect();
		dstRectForOpt.set(0, 0, b.getWidth(), b.getHeight());

		Canvas canvas = new Canvas(b);
		Paint optPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		if (dimOrGradient) {
			canvas.drawARGB(150, 0, 0, 0);
		} else {
			optPaint.setShader(new LinearGradient(0, 0, 0, h, new int[] { Color.argb(150, 0, 0, 0), Color.argb(150, 0, 0, 0),
					Color.argb(0, 0, 0, 0) }, new float[] { 0, 0.3f, 1 }, TileMode.MIRROR));
			canvas.drawRect(dstRectForOpt, optPaint);
		}
		return b;

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
		fromTutorialPosition = PreferenceHelper.getTutorialQuestionNumber(ctx);
		GenericQuestion tutorialQuestion = PreferenceHelper.getTutorialQuestion(this, fromTutorialPosition);
		fromTutorial = false;
		if (tutorialQuestion != null) {
			fromTutorial = true;
			quizMode = joystick.setQuizMode(true);
			joystick.setProblem(true);
			questionWorth = 0;
			questionWorthMax = 0;
			joystick.resetGuess();
			joystick.unPauseSelection();
			joystick.setTutorial(fromTutorialPosition);

			problem.setText(Html.fromHtml(tutorialQuestion.getQuestion()), TextView.BufferType.SPANNABLE);
			problem.setTextColor(defaultTextColor);

			questionDescription.setText(tutorialQuestion.getDescription());
			answers = tutorialQuestion.getAnswers();
			answerLoc = 0;
			joystick.setAnswers(answers, answerLoc);
			resetQuestionWorth(questionWorthMax);
		} else if (fromDeepLink) {
			joystick.setProblem(true);
			questionWorth = 0;
			questionWorthMax = 0;
			joystick.resetGuess();
			joystick.unPauseSelection();
			problem.setText(questionFromDeepLink);
			problem.setTextColor(defaultTextColor);
			questionDescription.setText(getString(R.string.question_description_prefix) + " | "
					+ getString(R.string.question_description_share));
			answers = answersFromDeepLink;
			setRandomAnswers();
			joystick.setAnswers(answersRandom, answerLoc);
			resetQuestionWorth(questionWorthMax);
		} else if (dbManager != null) {
			if (!dbManager.isDestroyed()) {
				fromChallenge = false;
				challengeIDToDisplay = "";
				Map<String, ChallengeData> challengeIDs = dbManager.getChallengeIDs();
				for (Map.Entry<String, ChallengeData> entry : challengeIDs.entrySet()) {
					String challengeID = entry.getKey();
					int questionsToAnswer = entry.getValue().getNumberOfQuestions();
					String hiqUserID = entry.getValue().getUserID();
					ChallengeStatus status = PreferenceHelper.getChallengeStatusFromID(this, challengeID);
					Loggy.d("challenge ID = " + challengeID + " |status = " + status.toString() + " |questions = " + questionsToAnswer);
					if (status.equals(ChallengeStatus.Accepted) && challengeIDToDisplay.equals("")) {
						if (questionsToAnswer > 0) {
							// display challenge Question
							challengeIDToDisplay = challengeID;
						} else {
							// send challenge complete to the api
							sendChallengeComplete(challengeID);
						}
					} else if (status.equals(ChallengeStatus.Declined) || status.equals(ChallengeStatus.Done)) {
						// delete questions from database
						dbManager.removeChallengeQuestions(challengeID);
						PreferenceHelper.removeChallengeID(this, challengeID, hiqUserID);
					}
				}
				if (!challengeIDToDisplay.equals("")) {
					new NotificationHelper(MainActivity.this).clearChallengeStatusNotification();
					ChallengeQuestion question = dbManager.getChallengeQuestion(challengeIDToDisplay);
					if (question != null) {
						fromChallenge = true;
						joystick.setProblem(true);
						ID = question.getID();
						String description = question.getDescription();
						String userName = question.getUserName();

						questionWorthMax = 0;
						decreaseRate = 0;

						answers = question.getAnswers();
						problem.setText(question.getQuestionText());
						problem.setTextColor(defaultTextColor);

						joystick.resetGuess();
						joystick.unPauseSelection();

						questionDescription.setText(getString(R.string.question_description_challenge_prefix) + userName + " | "
								+ description);

						setRandomAnswers();
						joystick.setAnswers(answersRandom, answerLoc);
						resetQuestionWorth(questionWorthMax);
					}
				} else if (EnabledPackages > 0) {
					joystick.setProblem(true);
					final String EnabledPackageKeys[] = new String[EnabledPackages];
					final int location[] = new int[EnabledPackages];
					double weights[] = new double[EnabledPackages], totalWeight = 0;
					int count = 0;
					boolean success;

					// get the difficulty
					difficultyMax = Integer.parseInt(sharedPrefs.getString("difficulty_max", "1"));
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
					joystick.resetGuess();
					imageLeft = null;
					problem.setCompoundDrawables(imageLeft, null, null, null);
					joystick.unPauseSelection();
					sharedPrefsStats = getSharedPreferences("Stats", 0);

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

					setRandomAnswers();

					joystick.setAnswers(answersRandom, answerLoc);
					problem.setTextColor(defaultTextColor);
					questionDescription.setText(getString(R.string.question_description_prefix) + " | " + currentPack);
					resetQuestionWorth(questionWorthMax);
				} else {
					setDefaultQuestion();
				}
			}
		} else {
			setDefaultQuestion();
		}
	}

	private void setDefaultQuestion() {
		joystick.resetGuess();
		joystick.setProblem(false);
		joystick.unPauseSelection();
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
		questionDescription.setText(getString(R.string.question_description_prefix));
		joystick.setAnswers(answersRandom, 0);
	}

	private void setRandomAnswers() {
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
	}

	private void resetQuestionWorth(int value) {
		startTime = System.currentTimeMillis();
		questionWorth = value;
		timerHandler.removeCallbacks(reduceWorth);
		timerHandler.postDelayed(reduceWorth, decreaseRate);
	}

	private void startCountdown() {
		startTime = System.currentTimeMillis();
		questionWorth = questionWorthMax;
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
		currentPack = fromLanguageLocal + " → " + toLanguageLocal;
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
			answers[i] = DatabaseManager.unescape(questions.get(i).getCorrectAnswer());
			decreaseRate += answers[i].length();
		}
		decreaseRate = decreaseRate * 10;
		problem.setText(DatabaseManager.unescape(questions.get(0).getQuestionText()));
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

	private boolean isAnyPackageUnlocked() {
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

	private int getChallengeScore(long startTime) {
		int timeToAnswer = (int) Math.min(System.currentTimeMillis() - startTime, ChallengeQuestion.MAX_SCORE);
		return ChallengeQuestion.MAX_SCORE - timeToAnswer;
	}

	private void sendChallengeComplete(long ID) {
		sendChallengeComplete(dbManager.getChallengeID(ID));
	}

	private void sendChallengeComplete(String challengeID) {
		int score = dbManager.getChallengeScore(challengeID);
		if (score >= 0) {
			new CompleteChallenge(this, challengeID, score, MoneyHelper.getMaxBet(this)) {
				@Override
				protected void onPostExecute(Integer result) {
					super.onPostExecute(result);
					if (joystick != null) {
						joystick.setNumberOfChallenges(ContactHelper.getNumberOfChallenges(MainActivity.this));
					}
				}
			}.execute();
		}
	}

	private void displayCorrectOrNot(int correctLoc, int guessLoc) {
		if (fromTutorialPosition == 4) {
			sendEvent("lockscreen_tutorial", "set_lockscreen_timeout", guessLoc + "", null);
			correctLoc = guessLoc;
		}
		boolean correct = (correctLoc == guessLoc);
		if (dbManager != null) {
			if (!dbManager.isDestroyed()) {
				joystick.setCorrectGuess(correctLoc);
				if (fromChallenge) {
					if (correct) {
						sendEvent("question", "challenge_question_answered", "correct", (long) questionWorth);
						problem.setTextColor(Color.GREEN);
					} else {
						sendEvent("question", "challenge_question_answered", "incorrect", (long) questionWorth);
						joystick.setIncorrectGuess(guessLoc);
						problem.setTextColor(Color.RED);
					}
					boolean done = false;
					if (joystick.getQuickUnlock() || !correct)
						done = dbManager.addChallengeScore(ID, 0);
					else
						done = dbManager.addChallengeScore(ID, getChallengeScore(startTime));
					if (done) {
						sendChallengeComplete(ID);
					}
				} else if (fromTutorial) {
					if (correct) {
						problem.setTextColor(Color.GREEN);
					} else {
						joystick.setIncorrectGuess(guessLoc);
						problem.setTextColor(Color.RED);
					}
				} else {
					dbManager.addStat(new Statistic(currentPack, String.valueOf(correct), Difficulty.fromValue(difficulty), System
							.currentTimeMillis(), startTime - System.currentTimeMillis()));
					if (correct) {
						sendEvent("question", "question_answered", "correct", (long) questionWorth);
						problem.setTextColor(Color.GREEN);
						dMoney = Money.increaseMoney(questionWorth);
						dbManager.decreasePriority(currentTableName, fromLanguage, toLanguage, ID);
					} else {
						sendEvent("question", "question_answered", "incorrect", (long) questionWorth);
						joystick.setIncorrectGuess(guessLoc);
						problem.setTextColor(Color.RED);
						dMoney = Money.decreaseMoneyNoDebt(0);
						dbManager.increasePriority(currentTableName, fromLanguage, toLanguage, ID);
					}
					MoneyHelper.setMoney(this, coins, Money.getMoney(), Money.getMoneyPaid());
				}
			}
		}
	}

	private void JoystickSelected(JoystickSelect s, boolean vibrate, int Extra) {
		dialogOn = false;
		if (vibrate) {
			if (sharedPrefs.getBoolean("vibration", true)
					&& ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode() != AudioManager.RINGER_MODE_SILENT)
				vib.vibrate(50);	// vibrate for 50ms
		}
		int maxAttempts = Integer.parseInt(sharedPrefs.getString("max_tries", "1"));
		if (s == JoystickSelect.Settings || s == JoystickSelect.QuizMode || s == JoystickSelect.Progress || s == JoystickSelect.Store
				|| s == JoystickSelect.Friends) {
			PreferenceHelper.storeLayoutParams(this);
		}
		switch (s) {
		case A:		// A was selected
		case B:		// B was selected
		case C:		// C was selected
		case D:		// D was selected
			if (fromDeepLink)
				fromDeepLink = false;
			int answer = JoystickSelect.fromValue(s);
			timerHandler.removeCallbacks(reduceWorth);
			if (fromTutorial) {
				displayCorrectOrNot(answerLoc, answer);
				joystick.pauseSelection();
				mHandler.removeCallbacksAndMessages(null);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						setProblemAndAnswer();
					}
				}, 500);
			} else if (EnabledPackages == 0) {
				this.finish();
			} else if (attempts >= maxAttempts && !(answerLoc == answer) && !quizMode && maxAttempts < 4) {
				displayCorrectOrNot(answerLoc, answer); // Too Many Wrong in a row
				if (Extra == 0)
					updateStats(false);
				joystick.pauseSelection();
				launchHomeScreen(1500);
			} else if ((answerLoc == answer) && quizMode) {
				displayCorrectOrNot(answerLoc, answer); // Correct
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
			} else if ((answerLoc == answer) && !quizMode) {
				displayCorrectOrNot(answerLoc, answer); // Correct
				if (Extra == 0)
					updateStats(true);
				joystick.pauseSelection();
				launchHomeScreen(100);
			} else {
				displayCorrectOrNot(answerLoc, answer); // Incorrect
				if (Extra == 0)
					updateStats(false);
				if (!quizMode)
					attempts++;
				joystick.pauseSelection();
			}
			break;
		case Friends:		// friends was selected
			/*unlocking = false;
			startActivity(new Intent(this, FriendActivity.class));*/
			/*if (!dialogOn) {
				dialogOn = true;
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.coming_soon_title);
				builder.setMessage(R.string.coming_soon_message).setCancelable(false);
				builder.setPositiveButton(R.string.coming_soon_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialogOn = false;
					}
				});
				builder.create().show();
			}*/
			displayFriends();
			/*unlocking = false;
			startActivity(new Intent(this,ChallengeActivity.class));*/
			break;
		case Store:		// Store was selected
			unlocking = false;
			startActivity(new Intent(this, ShowStoreActivity.class));
			break;
		case Progress:		// progress was selected
			unlocking = false;
			startActivity(new Intent(this, ShowProgressActivity.class));
			break;
		case QuizMode:		// quiz Mode was selected
			sendEvent("ui_action", "settings_selected", "quiz_mode", null);
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[3], EggMaxValues[3]));
			quizMode = joystick.setQuizMode(!quizMode);
			break;
		case Settings:		// settings was selected
			fromSettings = true;
			unlocking = false;
			startActivity(new Intent(this, ShowSettingsActivity.class));
			break;
		case Missed:	// missed the lock button
			break;
		case QuickUnlock:	// quickUnlock activated
			sendEvent("question", "double_tap", "quick_unlock", null);
			resetStreak();
			setApps();
			resetQuestionWorth(0);
			answerLoc = 2;
			joystick.moveCorrect(answerLoc);
			Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[13], EggMaxValues[13]));
			break;
		case AddApp:	// add app was selected
			selectApp();
			break;
		case SelectApp:	// app was selected
			if (Extra < apps.size()) {
				sendEvent("apps", "launch_app", apps.get(Extra).packageName, null);
				startActivity(getPackageManager().getLaunchIntentForPackage(apps.get(Extra).packageName));
				finish();
			}
			break;
		case DeleteApp:	// remove app was selected
			sendEvent("apps", "delete_app", apps.get(Extra).packageName, null);
			removeAppFromAll(Extra);
			break;
		case Share:	// share was selected
			progressDialog = ProgressDialog.show(this, "", "Starting Facebook", true);
			ShareHelper.getLinkAndShareFacebook(this, uiHelper, progressDialog, HelpQuestionImage, getString(R.string.share_title),
					getDeepLinkToShare());
			break;
		case Touch:
			setProblemAndAnswer();
			break;
		case Exit:
			break;
		case Question:
			break;
		case Vibrate:
			break;
		case ReturnToDefault:
			if (backgroundState == 1) {
				backgroundState = 0;
				backgroundTransition.reverseTransition(JoystickView.IN_OUT_DURATION);
			}
			break;
		case ShouldDimScreen:
			backgroundState = 1;
			backgroundTransition.startTransition(JoystickView.IN_OUT_DURATION);
			break;
		default:
			break;
		}
	}

	private String getDeepLinkToShare() {
		try {
			// String deepLink = "sharequestion://";
			String deepLink = "";
			deepLink += URLEncoder.encode(problem.getOriginalText(), "utf-8");
			for (int i = 0; i < answers.length; i++) {
				deepLink += "-" + URLEncoder.encode(answers[i], "utf-8");
			}
			return deepLink;
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	private void getDeepLinkData(Uri data) {
		if (data != null) {
			String scheme = data.getScheme();
			String host = data.getHost();
			String path = data.getPath();
			String coinHash = data.getQueryParameter(getString(R.string.coin_query));
			if (scheme != null && host != null && path != null && coinHash != null) {
				if ((scheme.equals(getString(R.string.coin_scheme1)) || scheme.equals(getString(R.string.coin_scheme2)))
						&& host.equals(getString(R.string.coin_host)) && path.equals(getString(R.string.coin_path))) {
					MoneyHelper.addPromoCoins(this, coinHash);
				}
			}
			// String scheme = "sharequestion";
			String target = "http://deeldat.com/f/";
			String url = data.toString();
			int start = url.indexOf(target);
			if (start >= 0) {
				try {
					fromDeepLink = true;
					start = url.indexOf('/', target.length()) + 1;
					int end = url.indexOf('-', start);
					questionFromDeepLink = URLDecoder.decode(url.substring(start, end), "utf-8");
					for (int i = 0; i < answers.length; i++) {
						start = end + 1;
						end = url.indexOf('-', start);
						if (end > 0)
							answersFromDeepLink[i] = URLDecoder.decode(url.substring(start, end), "utf-8");
						else
							answersFromDeepLink[i] = URLDecoder.decode(url.substring(start), "utf-8");
					}
				} catch (UnsupportedEncodingException e) {
					fromDeepLink = false;
				}
			}
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
				int currentMax = Integer.parseInt(sharedPrefs.getString("difficulty_max", "1"));
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
						getString(R.string.notification_message_streak_facebook), NotificationHelper.STREAK_ID);
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
					getString(R.string.notification_message_total_facebook), NotificationHelper.TOTAL_ID);
		}
		editorPrefsStats.commit();
	}

	private void displayFriends() {
		final Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			if (!session.getPermissions().contains("user_friends") && !PreferenceHelper.getAskedForFriendsPermission(this)) {
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, LoginFragment.PERMISSIONS);
				session.requestNewReadPermissions(newPermissionsRequest);
				PreferenceHelper.setAskedForFriends(this, true);
				return;
			}
		}
		sendEvent("social", "challenge_selected", "", null);
		final ChallengeDialog challengeDialog = ChallengeDialog.newInstance(this, PreferenceHelper.getLayoutHeight(this, 0));
		challengeDialog.setCancelable(true);
		challengeDialog.setDatabaseManager(dbManager);
		challengeDialog.setChallengeDialogListener(new ChallengeDialog.ChallengeDialogListener() {
			@Override
			public void onInviteSelected(String address) {
				String[] messages = getResources().getStringArray(R.array.invite_messages);
				int message = rand.nextInt(messages.length);
				sendEvent("social", "invite_selected", message + "", null);
				challengeDialog.dismiss();
				progressDialog = ProgressDialog.show(MainActivity.this, "", getString(R.string.invite_progress_message), true);
				ShareHelper.invite(MainActivity.this, address, progressDialog, message);
			}

			@Override
			public void onInactiveSelected(ChallengeBuilder builder) {
				challengeDialog.dismiss();
				if (dbManager != null && !dbManager.isDestroyed()) {
					final QuestionDialog questionDialog = QuestionDialog.newInstance(MainActivity.this,
							PreferenceHelper.getDisplayableUnlockedPackages(MainActivity.this, dbManager),
							PreferenceHelper.getDisplayableUnlockedPackageIDs(MainActivity.this, dbManager),
							MoneyHelper.getMaxBet(MainActivity.this), PreferenceHelper.getLayoutHeight(MainActivity.this, 0));
					questionDialog.setBuilder(builder);
					questionDialog.setCancelable(true);
					questionDialog.setQuestionDialogListener(new QuestionDialogListener() {
						@Override
						public void onChallenge(final ChallengeBuilder builder) {
							boolean success = false;
							questionDialog.dismiss();
							if (dbManager != null) {
								if (!dbManager.isDestroyed()) {
									List<GenericQuestion> questions = dbManager.createChallengeQuestions(builder);
									new SendChallenge(MainActivity.this, builder.getUserHash(), questions, builder.getBet(), builder
											.getDifficultyMin(), builder.getDifficultyMax()) {
										@Override
										protected void onPostExecute(Integer result) {
											boolean success = false;
											if (result == 0) {
												if (dbManager != null) {
													if (!dbManager.isDestroyed()) {
														String userID = builder.getUserHash();
														if (userID.equals(""))
															userID = getOpponentUserID();
														dbManager.addChallengeQuestions(getChallengeID(), userID, getGenericQuestions(),
																builder.getUserName());
														PreferenceHelper.storeChallengeStatus(MainActivity.this, getChallengeID(),
																ChallengeStatus.Undefined, CustomContactData.ChallengeState.Sent,
																builder.getUserName(), userID, getBet(), getDifficultyMin(),
																getDifficultyMax(), getQuestionNumber());
														success = true;
														String encryptedUserID = ContactHelper.getUserID(MainActivity.this);
														encryptedUserID = encryptedUserID.equals("") ? "Unknown" : EncryptionHelper
																.encryptForURL(encryptedUserID);
														sendEvent("social", "challenge_sent", encryptedUserID, (long) builder.getBet());
													}
												}
											}
											if (!success)
												Toaster.sendChallengeFailed(MainActivity.this);
											else {
												Toaster.sendChallengeSuccess(MainActivity.this);
												Money.increaseMoney(EggHelper.unlockEgg(MainActivity.this, coins, EggKeys[17],
														EggMaxValues[17]));
											}
										}
									}.execute();
									success = true;
								}
							}
							if (!success)
								Toaster.sendChallengeFailed(MainActivity.this);
						}
					});
					questionDialog.show(getSupportFragmentManager(), QuestionDialog.TAG);
				}
			}

			@Override
			public void onActiveStateSelected() {
				challengeDialog.dismiss();
				setProblemAndAnswer();
			}

			@Override
			public void onSentStateSelected(final String challengeID, final String hiqUserID, String userName, int bet, int diffMin,
					int diffMax, int questions, CustomContactData.ChallengeState state) {
				Map<String, Integer> challengeDescriptions = new HashMap<String, Integer>();
				if (dbManager != null) {
					if (!dbManager.isDestroyed()) {
						challengeDescriptions = dbManager.getChallengeDescriptions(challengeID);
					}
				}
				challengeDialog.dismiss();
				final ChallengeNewDialog challengeNewDialog = ChallengeNewDialog.newInstance(MainActivity.this, userName, bet, diffMin,
						diffMax, questions, challengeDescriptions, state);
				challengeNewDialog.setCancelable(true);
				challengeNewDialog.setChallengeDialogListener(new OnAcceptOrDeclineListener() {
					@Override
					public void onClick(ChallengeNewDialog.ClickType type) {
						challengeNewDialog.dismiss();
						if (type == ChallengeNewDialog.ClickType.Positive) {
							// do nothing return to main screen
						} else if (type == ChallengeNewDialog.ClickType.Nuetral) {
							new RemindChallenge(MainActivity.this, challengeID, hiqUserID).execute();
							String encryptedUserID = ContactHelper.getUserID(MainActivity.this);
							encryptedUserID = encryptedUserID.equals("") ? "Unknown" : EncryptionHelper.encryptForURL(encryptedUserID);
							sendEvent("social", "challenge_remind", encryptedUserID, null);
						} else if (type == ChallengeNewDialog.ClickType.Negative) {
							new CancelChallenge(MainActivity.this, challengeID, hiqUserID, ContactHelper.getUserID(MainActivity.this))
									.execute();
							String encryptedUserID = ContactHelper.getUserID(MainActivity.this);
							encryptedUserID = encryptedUserID.equals("") ? "Unknown" : EncryptionHelper.encryptForURL(encryptedUserID);
							sendEvent("social", "challenge_canceled", encryptedUserID, null);
						}
					}
				});
				challengeNewDialog.show(getSupportFragmentManager(), ChallengeNewDialog.TAG);
			}

			@Override
			public void onNewStateSelected(final String challengeID, String userName, final int bet, int diffMin, int diffMax,
					int questions, CustomContactData.ChallengeState state) {
				Map<String, Integer> challengeDescriptions = new HashMap<String, Integer>();
				if (dbManager != null) {
					if (!dbManager.isDestroyed()) {
						challengeDescriptions = dbManager.getChallengeDescriptions(challengeID);
					}
				}
				challengeDialog.dismiss();
				final ChallengeNewDialog challengeNewDialog = ChallengeNewDialog.newInstance(MainActivity.this, userName, bet, diffMin,
						diffMax, questions, challengeDescriptions, state);
				challengeNewDialog.setCancelable(true);
				challengeNewDialog.setChallengeDialogListener(new OnAcceptOrDeclineListener() {
					@Override
					public void onClick(ChallengeNewDialog.ClickType type) {
						challengeNewDialog.dismiss();
						if (type == ChallengeNewDialog.ClickType.Positive) {
							new AcceptChallenge(MainActivity.this, challengeID, true) {
								@Override
								protected void onPostExecute(Integer result) {
									new NotificationHelper(MainActivity.this).clearChallengeNotification();
									if (result == 0) {
										PreferenceHelper.storeChallengeStatus(MainActivity.this, challengeID, ChallengeStatus.Accepted,
												CustomContactData.ChallengeState.Active);
										Toast.makeText(MainActivity.this, getString(R.string.challenge_accepted), Toast.LENGTH_LONG).show();
										setProblemAndAnswer();
										quizMode = joystick.setQuizMode(!quizMode);
										String encryptedUserID = ContactHelper.getUserID(MainActivity.this);
										encryptedUserID = encryptedUserID.equals("") ? "Unknown" : EncryptionHelper
												.encryptForURL(encryptedUserID);
										sendEvent("social", "challenge_accepted", encryptedUserID, (long) bet);
										Money.increaseMoney(EggHelper.unlockEgg(MainActivity.this, coins, EggKeys[18], EggMaxValues[18]));
									} else {
										Toast.makeText(MainActivity.this, getString(R.string.challenge_status_failed), Toast.LENGTH_LONG)
												.show();
									}
								}
							}.execute();
						} else if (type == ChallengeNewDialog.ClickType.Nuetral) {
						} else if (type == ChallengeNewDialog.ClickType.Negative) {
							new AcceptChallenge(MainActivity.this, challengeID, false) {
								@Override
								protected void onPostExecute(Integer result) {
									new NotificationHelper(MainActivity.this).clearChallengeNotification();
									if (result == 0) {
										PreferenceHelper.storeChallengeStatus(MainActivity.this, challengeID, ChallengeStatus.Declined,
												CustomContactData.ChallengeState.None);
										if (joystick != null) {
											joystick.setNumberOfChallenges(ContactHelper.getNumberOfChallenges(MainActivity.this));
										}
										Toast.makeText(MainActivity.this, getString(R.string.challenge_declined), Toast.LENGTH_LONG).show();
									} else {
										Toast.makeText(MainActivity.this, getString(R.string.challenge_status_failed), Toast.LENGTH_LONG)
												.show();
									}
								}
							}.execute();
							String encryptedUserID = ContactHelper.getUserID(MainActivity.this);
							encryptedUserID = encryptedUserID.equals("") ? "Unknown" : EncryptionHelper.encryptForURL(encryptedUserID);
							sendEvent("social", "challenge_declined", encryptedUserID, (long) bet);
						}
					}
				});
				challengeNewDialog.show(getSupportFragmentManager(), ChallengeNewDialog.TAG);
			}
		});
		challengeDialog.show(getSupportFragmentManager(), ChallengeDialog.TAG);
	}

	private void displayInfo(boolean first) {
		if (!dialogOn) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			if (first) {
				builder.setCancelable(false);
				builder.setTitle(R.string.question_types_title).setItems(R.array.starting_packages, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialogOn = false;
						SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
						SharedPreferences sharedPrefsMoney = getSharedPreferences("Packages", 0);
						SharedPreferences.Editor editorPrefs = sharedPrefs.edit();
						SharedPreferences.Editor editorPrefsMoney = sharedPrefsMoney.edit();
						sendEvent("store", "unlocked_pack", PackageKeys.get(which), null);
						editorPrefs.putBoolean(PackageKeys.get(which), true).commit();			// enables the question pack
						editorPrefsMoney.putBoolean(unlockPackageKeys[which + 1], true).commit();	// unlocks the question pack

						UnlockedPackages = isAnyPackageUnlocked();
						EnabledPackages = getEnabledPackages();
						setProblemAndAnswer();
					}
				});
			} else {
				final String link = ShareHelper.buildShareURL(this);
				builder.setCancelable(true);
				builder.setTitle(R.string.info_title);
				builder.setMessage(R.string.info_message);
				builder.setPositiveButton(R.string.share_with_other, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						String[] messages = getResources().getStringArray(R.array.invite_messages);
						int message = rand.nextInt(messages.length);
						dialogOn = false;
						ShareHelper.share(ctx, null, null, messages[message], link);
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
						ShareHelper.loginOrShareFacebook(ctx, uiHelper, progressDialog, ShareHelper.buildShareURL(ctx));
					}
				});
			}
			AlertDialog alert = builder.create();
			dialogOn = true;
			alert.show();
			int w = PreferenceHelper.getLayoutWidth(this, -1);
			int h = PreferenceHelper.getLayoutHeight(this, -1);
			if (!first && w > 0 && h > 0)
				alert.getWindow().setLayout(w, h * 2 / 3);
		}
	}

	private void displayRateShare() {
		if (!dialogOn) {
			sharedPrefsMoney = getSharedPreferences("Packages", 0);
			editorPrefsMoney = sharedPrefsMoney.edit();
			editorPrefsMoney.putLong("lastTime", System.currentTimeMillis()).commit();
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

	private void provisionPendingMoney() {
		if (Money != null) {
			sharedPrefsMoney = getSharedPreferences("Packages", Context.MODE_PRIVATE);
			editorPrefsMoney = sharedPrefsMoney.edit();

			int pendingPaidMoney = sharedPrefsMoney.getInt(getString(R.string.pref_money_pending_paid), 0);
			Money.setMoneyPaid(sharedPrefsMoney.getInt("paid_money", 0) + pendingPaidMoney);

			int pendingMoney = sharedPrefsMoney.getInt(getString(R.string.pref_money_pending), 0);
			Money.setMoney(sharedPrefsMoney.getInt("money", 0) + pendingMoney);

			editorPrefsMoney.putInt(getString(R.string.pref_money_pending_paid), 0);
			editorPrefsMoney.putInt(getString(R.string.pref_money_pending), 0);
			editorPrefsMoney.commit();
			MoneyHelper.setMoney(this, coins, Money.getMoney(), Money.getMoneyPaid());
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
		Intent mainIntent = new Intent(android.content.Intent.ACTION_MAIN);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
		pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
		startActivityForResult(pickIntent, REQUEST_PICK_APP);
	}

	private void sendEvent(String category, String action, String label, Long value) {
		if (action.equals("question_answered")) {
			if (quizMode)
				action = "quiz_mode_" + action;
			if (joystick.getQuickUnlock())
				action = "quick_unlock_" + action;
		}
		if (!screenViewed) {
			trackerGA.send(MapBuilder.createAppView().build());
			screenViewed = true;
		}
		trackerGA.send(MapBuilder.createEvent(category, action, label, value).build());
	}

	@SuppressLint("NewApi")
	private Bitmap takeScreenShot() {
		View view = this.getWindow().getDecorView();
		int layerTypeOld = -1;
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			if (view.getLayerType() == View.LAYER_TYPE_HARDWARE) {
				layerTypeOld = View.LAYER_TYPE_HARDWARE;
				view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
		}
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();
		if (b1 == null) {
			return getWallpaperImage(true);
		} else {
			int cH = clockTextView.getHeight();
			int statusBarHeight = PreferenceHelper.getLayoutStatusBarHeight(this, -1);
			if (statusBarHeight == -1 && layout.getHeight() > 0 && layout.getHeight() < b1.getHeight()) {
				statusBarHeight += b1.getHeight() - layout.getHeight();
			}
			statusBarHeight += cH;

			int height = questionDescription.getHeight() + problem.getHeight();
			int minHeight = (int) (b1.getWidth() / ShareHelper.FACEBOOK_LINK_RATIO);
			int maxHeight = b1.getHeight() - statusBarHeight;
			height = (height < minHeight) ? minHeight : height;
			height = (height > maxHeight) ? maxHeight : height;
			Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, b1.getWidth(), height);
			// Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, b1.getWidth(), b1.getHeight() - statusBarHeight);
			if (layerTypeOld != -1)
				view.setLayerType(layerTypeOld, null);
			view.destroyDrawingCache();
			return b;
		}
	}

	@Override
	public void GCMResult(boolean result) {
		if (loginFragment != null) {
			loginFragment.GCMRegistrationDone(result);
		}
	}
}
