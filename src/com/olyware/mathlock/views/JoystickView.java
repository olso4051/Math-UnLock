package com.olyware.mathlock.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.olyware.mathlock.R;

public class JoystickView extends View {
	public static int IN_OUT_DURATION = 250, PULSE_DURATION = 400, PULSE_PAUSE = 3000, ICON_DURATION = 800, ICON_PAUSE = 1200;
	private final int pad = 5, NumAnswers = 4, NumOptions = 5, startFrames = 30, startFrameTime = 50, frameTimeReveal = 10,
			pulseFrames = 100;
	private final long tapLength = 250;
	private int answerSizeSPDefault = 20;

	private Bitmap bmpSelectBar, bmpS, bmpQ, bmpQs, bmpP, bmpStore, bmpFriend, bmpFriendSelected, bmpUnlock, bmpBackRed;
	// private Bitmap[] bmpBack = new Bitmap[3];
	private RectF[] RectForAnswers = new RectF[NumAnswers];
	private RectF[] RectForAnswersBackground = new RectF[NumAnswers];
	private RectF[] RectForQuickAnswers = new RectF[NumAnswers];
	private Rect[] bounds = new Rect[NumAnswers];
	private RectF dstRectForOpt, RectForUnlock, RectForUnlockPulse, rectForBackRed;
	private Rect dstRectForS, dstRectForQ, dstRectForP, dstRectForE, dstRectForI, srcRectForUnlock, srcRectForBack, srcRectForBig,
			srcRectForSmall, challengeBounds;

	private TextPaint[] answerTextPaint = new TextPaint[NumAnswers];
	private TextPaint optionPaintWhite, answerTextPaintBackup;
	private Path optionPath, dstPathForSet;
	private Paint settingsPaint, unlockPaint, transparentBlue, linePaint, backBlue, backRed, backGreen, transparent;

	private int optionPathCenterX, optionPathCenterY, tutorial, barY, barHeight, centerOffset, textSizeSP, textSizePix, answerSizeSP,
			Width, Height, dstHeight, spacing, rUnlock, radiusOfSettingsIcons, rApps, swipeLengthOption, swipeLength1, correctLoc,
			shareLoc, sponsoredLoc, MoreGamesLoc, correctGuess, wrongGuess, selectAppDrag, appCenterVert, appCenterHorz, alphaAnswer = 255,
			alphaTutorial = 255, pulseFrame = 0, numberOfChallenges, backupTries = 0;
	private long tapTimer, lastTimeRevealOrHide = 0, startTimeRevealOrHide = 0, lastTimePulse = 0, startTimePulse = 0;
	private float answerSizePix, optionX, optionY, appDragX = 0, appDragY = 0, strokeWidth;
	private double touchX, touchY, startX, startY, appAngle;
	private boolean quizMode, quickUnlock, selectUnlock, options = false, selectSideBar = false, problem = true, paused = false,
			measured = false, isFirstApp = false, shouldStartAnimations = false;
	private String shareOldAnswer;

	private int[] selectLeft = new int[5], selectRight = new int[5];
	private double[] X = new double[NumAnswers], Y = new double[NumAnswers], easingPulseFunctionX = new double[pulseFrames],
			easingPulseFunctionY = new double[pulseFrames], easingSlideUpFunctionX = new double[pulseFrames],
			easingSlideUpFunctionY = new double[pulseFrames];
	private boolean[] selectAnswers = new boolean[NumAnswers], selectOptions = new boolean[NumOptions];
	private String[] answers = { " ", " ", " ", " ", "?" };

	private StaticLayout[] layout = new StaticLayout[NumAnswers];
	private StaticLayout[] layoutBackup = new StaticLayout[NumAnswers];
	private EquationLayout[] layoutE = new EquationLayout[NumAnswers];
	private boolean[] equation = new boolean[NumAnswers];
	private boolean isAnsImage = false;

	private JoystickSelectListener listener;

	private Runnable startAnimate, finishAnimate, revealAnswers, hideAnswers, pulseLock;
	private Handler animateHandler;

	private List<Drawable> d;
	private List<App> apps;
	private Drawable test, drawAdd, drawTrash, drawBackBlue, drawBackRed;
	private Resources res;
	private Context ctx;

	private class App {
		private double angle;
		public float X, Y, left, top, right, bottom;
		private boolean select, selectDrag;
		public RectF f = new RectF();;

		private App(double angle, float X, float Y, int radius) {
			this.angle = angle;
			this.X = X;
			this.Y = Y;
			this.left = X - radius;
			this.top = Y - radius;
			this.right = X + radius;
			this.bottom = Y + radius;
			select = false;
			selectDrag = false;

		}

		public void setAll(double angle, float X, float Y, int radius) {
			this.angle = angle;
			this.X = X;
			this.Y = Y;
			this.left = X - radius;
			this.top = Y - radius;
			this.right = X + radius;
			this.bottom = Y + radius;
		}

		public void makeRect() {
			f.set(left, top, right, bottom);
		}

		public double getAngle() {
			if (angle < 0)
				return angle + 2 * Math.PI;
			else
				return angle;
		}

		public float getX() {
			return X;
		}

		public float getY() {
			return Y;
		}

		public float getLeft() {
			return left;
		}

		public float getTop() {
			return top;
		}

		public float getRight() {
			return right;
		}

		public float getBottom() {
			return bottom;
		}

		public boolean getSelect() {
			return select;
		}

		public boolean getSelectDrag() {
			return selectDrag;
		}

		public void setSelect(boolean select) {
			this.select = select;
		}

		public void setSelectDrag(boolean selectDrag) {
			this.selectDrag = selectDrag;
		}
	}

	// =========================================
	// Constructors
	// =========================================

	public JoystickView(Context context) {
		super(context);
		initView(context);
	}

	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Custom, 0, 0);

		try {
			answerSizePix = a.getDimension(R.styleable.Custom_textSizeDefault, 0);
			answerSizeSPDefault = (int) PixelHelper.pixelToSP(context, answerSizePix);
		} finally {
			a.recycle();
		}
		initView(context);
	}

	public JoystickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	// =========================================
	// Initialization
	// =========================================

	private void initView(Context ctx) {
		this.ctx = ctx;
		setFocusable(true);
		Width = getMeasuredWidth();
		Height = getMeasuredHeight();
		res = getResources();
		numberOfChallenges = 0;
		tutorial = -1;

		textSizeSP = 20;
		textSizePix = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, res.getDisplayMetrics());
		bmpSelectBar = BitmapFactory.decodeResource(res, R.drawable.select);
		test = res.getDrawable(R.drawable.select);
		test.setBounds(-test.getIntrinsicWidth() / 2, -test.getIntrinsicHeight() / 2, test.getIntrinsicWidth() / 2,
				test.getIntrinsicHeight() / 2);
		barHeight = test.getIntrinsicHeight();// bmpSelectBar.getHeight();
		animateHandler = new Handler();

		optionPaintWhite = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		optionPaintWhite.setTextAlign(Paint.Align.CENTER);
		optionPaintWhite.setColor(Color.WHITE);
		optionPaintWhite.setTextSize(textSizePix);

		settingsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		settingsPaint.setStyle(Paint.Style.FILL);
		settingsPaint.setARGB(100, 150, 150, 150);

		transparentBlue = new Paint(Paint.ANTI_ALIAS_FLAG);
		transparentBlue.setStyle(Paint.Style.FILL);
		transparentBlue.setARGB(191, 0, 127, 255);

		transparent = new Paint(Paint.ANTI_ALIAS_FLAG);
		transparent.setFilterBitmap(true);
		transparent.setDither(true);

		optionPath = new Path();
		dstPathForSet = new Path();
		options = false;

		answerSizeSP = answerSizeSPDefault;
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, res.getDisplayMetrics());

		challengeBounds = new Rect();
		answerTextPaintBackup = new TextPaint(optionPaintWhite);
		answerTextPaintBackup.setTextSize(answerSizePix);
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i] = new TextPaint(answerTextPaintBackup);
			answerTextPaint[i].setAlpha(alphaAnswer);
			bounds[i] = new Rect();
			equation[i] = false;
			if (answers[i].length() > 1)
				if (answers[i].charAt(0) == '$')
					if (answers[i].length() > 1)
						if (answers[i].charAt(1) != '$')
							equation[i] = true;
			layoutE[i] = new EquationLayout(answers[i], Width, Height, answerTextPaint[i], answerSizeSPDefault);
			layout[i] = new StaticLayout(answers[i], answerTextPaint[i], Width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
		}

		bmpS = BitmapFactory.decodeResource(res, R.drawable.select_settings);
		bmpQ = BitmapFactory.decodeResource(res, R.drawable.select_quiz_mode);
		bmpQs = BitmapFactory.decodeResource(res, R.drawable.select_quiz_mode_selected);
		bmpP = BitmapFactory.decodeResource(res, R.drawable.select_progress);
		bmpStore = BitmapFactory.decodeResource(res, R.drawable.select_store);
		bmpFriend = BitmapFactory.decodeResource(res, R.drawable.select_friend);
		bmpFriendSelected = BitmapFactory.decodeResource(res, R.drawable.select_friend_selected);
		bmpUnlock = BitmapFactory.decodeResource(res, R.drawable.unlock);
		bmpBackRed = BitmapFactory.decodeResource(res, R.drawable.gradient_background_red);
		// bmpBack[0] = BitmapFactory.decodeResource(res, R.drawable.gradient_background_blue);
		// bmpBack[1] = BitmapFactory.decodeResource(res, R.drawable.gradient_background_green);
		// bmpBack[2] = BitmapFactory.decodeResource(res, R.drawable.gradient_background_red);
		drawAdd = res.getDrawable(R.drawable.add);
		drawAdd.setBounds(-drawAdd.getIntrinsicWidth() / 2, -drawAdd.getIntrinsicHeight() / 2, drawAdd.getIntrinsicWidth() / 2,
				drawAdd.getIntrinsicHeight() / 2);
		drawTrash = res.getDrawable(R.drawable.trash);
		drawTrash.setBounds(-drawTrash.getIntrinsicWidth() / 2, -drawTrash.getIntrinsicHeight() / 2, drawTrash.getIntrinsicWidth() / 2,
				drawTrash.getIntrinsicHeight() / 2);
		drawBackBlue = res.getDrawable(R.drawable.gradient_background_blue);
		drawBackBlue.setBounds(-drawBackBlue.getIntrinsicWidth() / 2, -drawBackBlue.getIntrinsicHeight() / 2,
				drawBackBlue.getIntrinsicWidth() / 2, drawBackBlue.getIntrinsicHeight() / 2);
		drawBackRed = res.getDrawable(R.drawable.gradient_background_red);
		// drawBackRed.setBounds(-drawBackRed.getIntrinsicWidth() / 2, -drawBackRed.getIntrinsicHeight() / 2,
		// drawBackRed.getIntrinsicWidth() / 2, drawBackRed.getIntrinsicHeight() / 2);
		// drawBackRed.setBounds(Width / 2 - drawBackRed.getIntrinsicWidth() / 2, getMiddlePoint() - drawBackRed.getIntrinsicHeight() / 2,
		// Width / 2 + drawBackRed.getIntrinsicWidth() / 2, getMiddlePoint() + drawBackRed.getIntrinsicHeight() / 2);

		touchX = 0;
		touchY = 0;

		rApps = drawAdd.getIntrinsicHeight() * 3 / 4;
		radiusOfSettingsIcons = Math.max(bmpQ.getWidth(), bmpQ.getHeight()) / 2;
		rUnlock = radiusOfSettingsIcons;
		swipeLengthOption = radiusOfSettingsIcons * 4;
		swipeLength1 = rUnlock * 2;

		// srcRectForBack = new Rect(0, 0, bmpBack[0].getWidth(), bmpBack[0].getHeight());
		srcRectForUnlock = new Rect(0, 0, bmpUnlock.getWidth(), bmpUnlock.getHeight());
		srcRectForBig = new Rect(0, 0, bmpQ.getWidth(), bmpQ.getHeight());
		srcRectForSmall = new Rect(0, 0, bmpS.getWidth(), bmpS.getHeight());

		dstRectForOpt = new RectF();
		dstRectForS = new Rect();
		dstRectForQ = new Rect();
		dstRectForP = new Rect();
		dstRectForE = new Rect();
		dstRectForI = new Rect();
		RectForUnlockPulse = new RectF();
		RectForUnlock = new RectF();

		strokeWidth = rUnlock / 10;
		unlockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		unlockPaint.setColor(Color.WHITE);
		unlockPaint.setStyle(Paint.Style.STROKE);
		unlockPaint.setStrokeWidth(strokeWidth);

		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setColor(Color.argb(160, 255, 255, 255));
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(0);

		backBlue = new Paint(Paint.ANTI_ALIAS_FLAG);
		backBlue.setColor(Color.argb(70, 0, 0, 255));
		backBlue.setStyle(Paint.Style.FILL);

		backGreen = new Paint();
		backGreen.setColor(Color.argb(70, 0, 255, 0));
		backGreen.setStyle(Paint.Style.FILL);

		backRed = new Paint();
		backRed.setColor(Color.argb(70, 255, 0, 0));
		backRed.setStyle(Paint.Style.FILL);

		for (int i = 0; i < NumAnswers; i++) {
			selectAnswers[i] = false;
			RectForAnswers[i] = new RectF();
			RectForAnswersBackground[i] = new RectF();
			RectForQuickAnswers[i] = new RectF();
		}
		for (int i = 0; i < NumOptions; i++) {
			selectOptions[i] = false;
		}
		selectUnlock = false;
		selectAppDrag = -1;

		listener = new JoystickSelectListener() {
			@Override
			public void OnSelect(JoystickSelect s, boolean vibrate, int Extra) {
			}
		};
		tapTimer = 0;
		correctLoc = 0;
		shareLoc = -1;
		sponsoredLoc = -1;
		MoreGamesLoc = -1;
		quickUnlock = false;
		centerOffset = 0;
		d = new ArrayList<Drawable>();
		d.add(drawAdd);
		d.add(drawTrash);
		apps = new ArrayList<App>();
		for (int i = 0; i < pulseFrames; i++) {
			easingPulseFunctionX[i] = rUnlock * Math.sin(Math.PI * 4 * i / pulseFrames) / 10;
			easingPulseFunctionY[i] = 0;
			easingSlideUpFunctionX[i] = 0;
			easingSlideUpFunctionY[i] = -swipeLengthOption * 2 * Math.pow((double) i / (double) pulseFrames, 2);
		}
	}

	// =========================================
	// Public Methods
	// =========================================

	public boolean getQuickUnlock() {
		return quickUnlock;
	}

	public int getParentTop() {
		return ((View) getParent()).getTop();
	}

	public int getLockCenterY() {
		return (int) RectForUnlock.centerY();
	}

	public int getLockCenterX() {
		return (int) RectForUnlock.centerX();
	}

	public void setOnJostickSelectedListener(JoystickSelectListener listener) {
		this.listener = listener;
	}

	public void addApp(Drawable icon) {
		if (d.size() == apps.size()) {
			isFirstApp = true;
		}

		if (d.size() <= apps.size()) {
			icon.setBounds(-icon.getIntrinsicWidth() / 2, -icon.getIntrinsicHeight() / 2, icon.getIntrinsicWidth() / 2,
					icon.getIntrinsicHeight() / 2);
			d.add(d.size() - 1, icon);
			invalidate();
		}
		setAppCenters(false);// pai wrote
	}

	public void removeApp(int loc) {
		d.remove(loc);
		if (isFirstApp) {
			isFirstApp = false;
		}
		setAppCenters(false); // pai added
		invalidate();
	}

	public void clearApps() {
		d.clear();
		d.add(drawAdd);
		d.add(drawTrash);
	}

	public void setTypeface(Typeface font) {
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTypeface(font);
			if (equation[i])
				layoutE[i].setTypeface(font);
		}
		optionPaintWhite.setTypeface(font);
		answerTextPaintBackup.setTypeface(font);
	}

	public boolean setQuizMode(boolean quizMode) {
		this.quizMode = quizMode;
		return this.quizMode;
	}

	public void setTutorial(int tutorial) {
		this.tutorial = tutorial;
		alphaTutorial = 255;
		switch (tutorial) {
		case 0:	// Press the Lock
			setSidePaths(Height - pad);
			options = false;
			break;
		case 1: // Double tap the Lock To Quickly Unlock Your Device \n (slide to "+" to add your favorite apps)
			setSidePaths(Height - pad);
			options = false;
			break;
		case 2: // Challenge your friends \n (slide the icon up)
			setSidePaths(Height - pad); // pai added
			// setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);// pai commented
			options = true;
			break;
		case 3: // Change Any Setting at any time \n (slide the icon up)
			setSidePaths(Height - pad); // pai added
			// setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
			options = true;
			break;
		case 4: // How often would you like Hiq on your lockscreen (change this setting in settings / advanced)
			setSidePaths(Height - pad);
			options = false;
			break;
		case 5: // Quiz mode for endless questions (slide the icon up)
			setSidePaths(Height - pad); // pai added
			// setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
			options = true;
			break;
		case 6: // Check your progress (slide the icon up)
			setSidePaths(Height - pad); // pai added
			// setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
			options = true;
			break;
		case 7: // Unlock more question packs (slide the icon up)
			setSidePaths(Height - pad); // pai added
			// setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
			options = true;
			break;
		default:
			setSidePaths(Height - pad);
			options = false;
			break;
		}
		invalidate();
	}

	private boolean shouldOpenOptionFromTutorial(int tutorial) {
		switch (tutorial) {
		case 0:	// Press the Lock
			return false;
		case 1: // Double tap the Lock To Quickly Unlock Your Device \n (slide to "+" to add your favorite apps)
			return false;
		case 2: // Challenge your friends \n (slide the icon up)
			return true;
		case 3: // Change Any Setting at any time \n (slide the icon up)
			return true;
		case 4: // How often would you like Hiq on your lockscreen (change this setting in settings / advanced)
			return false;
		case 5: // Quiz mode for endless questions (slide the icon up)
			return true;
		case 6: // Check your progress (slide the icon up)
			return true;
		case 7: // Unlock more question packs (slide the icon up)
			return true;
		default:
			return false;
		}
	}

	public void showStartAnimation(int start, int delay) {
		if (start == 0) {
			options = true;
			// setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
			setSidePaths(Height - pad);
		}
		invalidate();
		animateHandler.removeCallbacks(startAnimate);
		animateHandler.removeCallbacks(finishAnimate);
		for (int i = 0; i < startFrames; i++) {
			animateHandler.postDelayed(startAnimate, i * startFrameTime + delay);
		}
		animateHandler.postDelayed(finishAnimate, startFrames * startFrameTime + delay);
	}

	public void setProblem(boolean problem) {
		this.problem = problem;
	}

	public void pauseSelection() {
		this.paused = true;
	}

	public void unPauseSelection() {
		this.paused = false;
	}

	public void setCorrectGuess(int location) {
		this.correctGuess = location;
	}

	public void setIncorrectGuess(int location) {
		this.wrongGuess = location;
	}

	public void resetGuess() {
		this.correctGuess = -1;
		this.wrongGuess = -1;
		this.tutorial = -1;
	}

	public void setAnswers(String answers[], int correctLoc) {
		setAnswers(answers, correctLoc, -1, -1, -1);
	}

	// pai -
	public void setAnswers(String answers[], int correctLoc, int shareLoc, int sponsoredLoc, int MoreGamesLoc) {
		this.answers = new String[] { answers[0], answers[1], answers[2], answers[3], res.getString(R.string.unknown) };
		this.correctLoc = correctLoc;
		this.shareLoc = shareLoc;
		this.sponsoredLoc = sponsoredLoc;
		this.MoreGamesLoc = MoreGamesLoc;
		this.quickUnlock = false;
		centerOffset = 0;
		if (measured) {
			resetAnswerSize();
			setDimensions();
			startTimePulse = 0;
			int centerX = Width / 2;
			int bottom = barY - barHeight;
			int centerY = bottom / 2;

			RectForUnlockPulse.set(centerX - rUnlock, (int) (centerY * 1.9) - rUnlock, centerX + rUnlock, (int) (centerY * 1.9) + rUnlock);
			RectForUnlock.set(centerX - rUnlock, (int) (centerY * 1.9) - rUnlock, centerX + rUnlock, (int) (centerY * 1.9) + rUnlock);

			int middle = getMiddlePoint();

			RectForAnswers[0].set(0, 50, Width / 2, middle);
			RectForAnswers[1].set(Width / 2, 50, Width, middle);
			RectForAnswers[2].set(0, middle, Width / 2, middle * 2 - 50);
			RectForAnswers[3].set(Width / 2, middle, Width, middle * 2 - 50);

			RectForAnswersBackground[0].set(0, 50, Width / 2, middle);
			RectForAnswersBackground[1].set(Width / 2, 50, Width, middle);
			RectForAnswersBackground[2].set(0, middle, Width / 2, middle * 2 - 50);
			RectForAnswersBackground[3].set(Width / 2, middle, Width, middle * 2 - 50);

			// srcRectForBack = RectForAnswers;
		}
	}

	public void askToShare(String prompt) {
		if (shareLoc < 0 && sponsoredLoc < 0 && MoreGamesLoc < 0) {
			Random rand = new Random();
			int loc = rand.nextInt(3);
			if (loc >= correctLoc) {
				loc++;
			}
			shareLoc = loc;
			shareOldAnswer = answers[loc];
			// answers[loc] = res.getString(R.string.ask_to_share0);
			answers[loc] = prompt;
			setAnswers(answers, correctLoc, shareLoc, -1, -1);
		}
	}

	public void resetAskToShare() {
		if (shareLoc >= 0) {
			answers[shareLoc] = shareOldAnswer;
			setAnswers(answers, correctLoc);
		}
	}

	public void askMoreGames() {
		if (shareLoc < 0 && sponsoredLoc < 0 && MoreGamesLoc < 0) {
			Random rand = new Random();
			int loc = rand.nextInt(3);
			if (loc >= correctLoc) {
				loc++;
			}
			MoreGamesLoc = loc;
			answers[loc] = res.getString(R.string.ask_more_games);
			setAnswers(answers, correctLoc, -1, -1, MoreGamesLoc);
		}
	}

	public boolean clickedMoreGames(int loc) {
		return (MoreGamesLoc == loc);
	}

	public void askForSponsored(int questions, String description) {
		if (shareLoc < 0 && sponsoredLoc < 0 && MoreGamesLoc < 0) {
			Random rand = new Random();
			int loc = rand.nextInt(3);
			if (loc >= correctLoc) {
				loc++;
			}
			sponsoredLoc = loc;
			answers[loc] = questions + res.getString(R.string.sponsored_questions) + description + res.getString(R.string.sponsored_quiz);
			setAnswers(answers, correctLoc, -1, sponsoredLoc, -1);
		}
	}

	public boolean isAskingForSponsored() {
		return (sponsoredLoc >= 0);
	}

	public void moveCorrect(int loc) {
		shareLoc = -1;
		sponsoredLoc = -1;
		MoreGamesLoc = -1;
		if (correctLoc != loc) {
			String temp = answers[loc];
			answers[loc] = answers[correctLoc];
			answers[correctLoc] = temp;
			correctLoc = loc;
			setDimensions();
		}
	}

	public void setNumberOfChallenges(int numberOfChallenges) {
		this.numberOfChallenges = numberOfChallenges;
		invalidate();
	}

	public void startAnimations() {
		shouldStartAnimations = true;
		startTimePulse = 0;
		animateHandler.removeCallbacksAndMessages(null);
		animateHandler.post(finishAnimate);
		if (shouldOpenOptionFromTutorial(tutorial))
			animateHandler.postDelayed(pulseLock, ICON_PAUSE);
		else
			animateHandler.postDelayed(pulseLock, PULSE_PAUSE);
	}

	public void removeCallbacks() {
		animateHandler.removeCallbacksAndMessages(null);
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		measured = true;
		setDimensions();
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Width = measure(widthMeasureSpec);
		Height = measure(heightMeasureSpec);

		optionY = Height - radiusOfSettingsIcons;

		// spacing = (Width - rBig * 6 - rSmall * 4) / 6;
		spacing = (Width - radiusOfSettingsIcons * 10) / 6;
		for (int i = 0; i < selectLeft.length; i++) {
			selectLeft[i] = Width / 2 + radiusOfSettingsIcons * (Math.max(3 - i * 2, -3)) + spacing * (2 - i);
			selectRight[i] = Width / 2 + radiusOfSettingsIcons * (Math.min(5 - i * 2, 3)) + spacing * (2 - i);
		}
		selectLeft[4] = selectLeft[4] - radiusOfSettingsIcons * 2;
		// selectRight[0] = selectRight[0] + rSmall * 2;
		selectRight[0] = selectRight[0] + radiusOfSettingsIcons * 2;

		dstHeight = radiusOfSettingsIcons * 2 + barHeight;

		if (options || shouldOpenOptionFromTutorial(tutorial)) {
			// setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3); pai changed
			setSidePaths(Height - pad);
		} else {
			setSidePaths(Height - pad);
		}

		setMeasuredDimension(Width, Height);
		initRunnables();
		if (shouldStartAnimations)
			startAnimations();
		measured = true;

	}

	private int measure(int measureSpec) {
		int result = 0;
		// Decode the measurement specifications.
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.UNSPECIFIED) {
			// Return a default size of 480 if no bounds are specified.
			result = 480;
		} else if (specMode == MeasureSpec.AT_MOST) {
			result = specSize;
		} else {
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@SuppressLint("NewApi")
	@Override
	protected void onDraw(Canvas canvas) {
		for (int i = 0; i < NumAnswers; i++) {
			if (!quickUnlock || (i == correctLoc)) {
				canvas.save();
				// Draw the background for selected answers
				unlockPaint.setAlpha((wrongGuess < 0 && !quickUnlock) ? alphaAnswer : 255);
				if (selectAnswers[i]) {
					// canvas.drawBitmap(bmpBack[0], srcRectForBack, RectForAnswers[i], unlockPaint);
					canvas.drawRect(RectForAnswersBackground[i], backBlue);
				} else if (i == correctGuess) {
					// canvas.drawBitmap(bmpBack[1], srcRectForBack, RectForAnswers[i], unlockPaint);
					canvas.drawRect(RectForAnswersBackground[i], backGreen);
				} else if (i == wrongGuess) {
					// canvas.drawBitmap(bmpBack[2], srcRectForBack, RectForAnswers[i], unlockPaint);
					canvas.drawRect(RectForAnswersBackground[i], backRed);
				}
				// Pai change here
				// position the text then draw the layout

				// canvas.drawBitmap(bmpUnlock, srcRectForUnlock, RectForAnswers[i], unlockPaint);
				if (answers[i] != null && answers[i].startsWith("image://")) {
					// canvas.drawBitmap(bitmapAnswers[i], srcRectForBitmap[i], srcRectForBitmap[i], transparent);
					if (bitmapAnswers[i] == null) {
						setUpBitmapForAnswer(false);
					}
					RectF rectReduced = new RectF(RectForAnswers[i]);
					rectReduced.left = rectReduced.left + 10;
					rectReduced.top = rectReduced.top + 10;
					rectReduced.right = rectReduced.right - 10;
					rectReduced.bottom = rectReduced.bottom - 10;
					if (bitmapAnswers[i] != null)
						canvas.drawBitmap(bitmapAnswers[i], null, rectReduced, transparent);
					else {
						if (equation[i]) {
							canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2,
									(RectForAnswers[i].top + RectForAnswers[i].bottom) / 2);
							layoutE[i].setAlpha((wrongGuess < 0 && !quickUnlock) ? alphaAnswer : 255);
							layoutE[i].draw(canvas);
						} else {
							canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2,
									(RectForAnswers[i].top + RectForAnswers[i].bottom) / 2 - layout[i].getHeight() / 2);
							if (selectUnlock && alphaAnswer == 0) {
								if (backupTries > NumAnswers) {
									layoutBackup[i].draw(canvas);
								} else {
									backupTries++;
									layout[i].draw(canvas);
								}
							} else {
								backupTries = 0;
								layout[i].draw(canvas);
							}
						}
					}
				} else {
					if (equation[i]) {
						canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2,
								(RectForAnswers[i].top + RectForAnswers[i].bottom) / 2);
						layoutE[i].setAlpha((wrongGuess < 0 && !quickUnlock) ? alphaAnswer : 255);
						layoutE[i].draw(canvas);
					} else {
						canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2,
								(RectForAnswers[i].top + RectForAnswers[i].bottom) / 2 - layout[i].getHeight() / 2);
						if (selectUnlock && alphaAnswer == 0) {
							if (backupTries > NumAnswers) {
								layoutBackup[i].draw(canvas);
							} else {
								backupTries++;
								layout[i].draw(canvas);
							}
						} else {
							backupTries = 0;
							layout[i].draw(canvas);
						}
					}
				}
				canvas.restore();
			}
		}
		if (quickUnlock) {
			int start = 0;
			if (isFirstApp)
				start = 1;
			int end = d.size() - 1;
			for (int i = 1 - start; i < end - start; i++)
				if (apps.get(i).getSelectDrag())
					end++;
			for (int i = start; i < end; i++) {
				if (!apps.get(i - start).getSelectDrag()) {
					// canvas.save();
					// canvas.translate(apps.get(i - start).getX(), apps.get(i - start).getY());
					if (apps.get(i - start).getSelect())
						if (i == d.size() - 1)
							// drawBackRed.draw(canvas);
							canvas.drawRect(rectForBackRed, backRed);
						else
							// drawBackBlue.draw(canvas);
							canvas.drawRect(apps.get(i - start).f, backBlue);
					// canvas.restore();
				}
			}
			for (int i = start; i < end; i++) {
				if (!apps.get(i - start).getSelectDrag()) {
					canvas.save();
					canvas.translate(apps.get(i - start).getX(), apps.get(i - start).getY());
					// d.get(i).setAlpha((wrongGuess < 0&&!quickUnlock) ? answerAlpha : 255);
					d.get(i).draw(canvas);
					canvas.restore();
				}
			}
			if (selectAppDrag >= 0) {
				canvas.save();
				canvas.translate(apps.get(selectAppDrag).getX() + appDragX, apps.get(selectAppDrag).getY() + appDragY);
				// d.get(selectAppDrag + start).setAlpha((wrongGuess < 0&&!quickUnlock) ? answerAlpha : 255);
				d.get(selectAppDrag + start).draw(canvas);
				canvas.restore();
			}
		}

		// Draw the lock
		unlockPaint.setAlpha(255);
		canvas.drawRoundRect(RectForUnlockPulse, (RectForUnlockPulse.right - RectForUnlockPulse.left) / 2f,
				(RectForUnlockPulse.bottom - RectForUnlockPulse.top) / 2f, unlockPaint);
		canvas.drawBitmap(bmpUnlock, srcRectForUnlock, RectForUnlock, unlockPaint);

		// Added by pai started here for lines

		// canvas.drawLine(0, (int) (((barY - barHeight) / 2) * 1.7), Width, (int) (((barY - barHeight) / 2) * 1.7), linePaint);// last
		// canvas.drawLine(0, (barY - barHeight) / 2, Width, (barY - barHeight) / 2, linePaint);// middle
		// int dis = (int) (((barY - barHeight) / 2) * 1.7) - (int) ((barY - barHeight) / 1.4);
		// canvas.drawLine(Width / 2, dis, Width / 2, (int) (((barY - barHeight) / 2) * 1.7), linePaint); // Vertical
		// canvas.drawLine(0, dis, Width, dis, linePaint); // Top
		// horoizontal
		int middle = getMiddlePoint();
		canvas.drawLine(0, middle * 2 - 50, Width, middle * 2 - 50, linePaint);// last
		canvas.drawLine(0, middle, Width, middle, linePaint);// middle
		// int dis = (int) (((barY - barHeight) / 2) * 1.7) - (int) ((barY - barHeight) / 1.4);
		canvas.drawLine(Width / 2, 50, Width / 2, middle * 2 - 50, linePaint); // Vertical
		canvas.drawLine(0, 50, Width, 50, linePaint);
		// Added by pai ends here for lines

		// Draw the option bar text hints
		if ((selectOptions[0]) || (selectOptions[1]) || (selectOptions[2]) || (selectOptions[3]) || (selectOptions[4])) {
			boolean drawOnPath = true;
			if (android.os.Build.VERSION.SDK_INT >= 11 && android.os.Build.VERSION.SDK_INT <= 16) {
				if (canvas.isHardwareAccelerated()) {
					drawOnPath = false;
				}
			}
			if (drawOnPath) {
				canvas.drawTextOnPath(res.getString(R.string.swipe_here), optionPath, 0, 0, optionPaintWhite);
				for (int i = 0; i < selectOptions.length; i++) {
					if (selectOptions[i]) {
						int id = res.getIdentifier("option" + i, "string", ctx.getPackageName());
						canvas.drawTextOnPath(res.getString(id), optionPath, 0, -optionPaintWhite.getTextSize(), optionPaintWhite);
					}
				}
			} else {
				canvas.drawText(res.getString(R.string.swipe_here), optionPathCenterX, optionPathCenterY, optionPaintWhite);
				for (int i = 0; i < selectOptions.length; i++) {
					if (selectOptions[i]) {
						int id = res.getIdentifier("option" + i, "string", ctx.getPackageName());
						canvas.drawText(res.getString(id), optionPathCenterX, optionPathCenterY - optionPaintWhite.getTextSize(),
								optionPaintWhite);
					}
				}
			}

		}

		// PAI COMMENTED - new UI on oct 27th- 2014 started here //
		// Draw the settings bar background
		// canvas.drawPath(dstPathForSet, settingsPaint);
		// canvas.save();
		// canvas.translate(Width / 2, barY - barHeight / 2 + pad);
		// test.draw(canvas);
		// canvas.restore();

		// // Draw the settings icons
		// // Draw the settings icon
		// if (tutorial == 3 && !selectOptions[4]) {
		// optionPaintWhite.setAlpha(alphaTutorial);
		// canvas.drawBitmap(bmpS, srcRectForSmall, dstRectForS, optionPaintWhite);
		// optionPaintWhite.setAlpha(255);
		// } else {
		// canvas.drawBitmap(bmpS, srcRectForSmall, dstRectForS, optionPaintWhite);
		// }
		// // Draw the quiz mode icon
		// if (quizMode) {
		// if (tutorial == 5 && !selectOptions[3]) {
		// optionPaintWhite.setAlpha(alphaTutorial);
		// canvas.drawBitmap(bmpQs, srcRectForBig, dstRectForQ, optionPaintWhite);
		// optionPaintWhite.setAlpha(255);
		// } else {
		// canvas.drawBitmap(bmpQs, srcRectForBig, dstRectForQ, optionPaintWhite);
		// }
		// } else {
		// if (tutorial == 5 && !selectOptions[3]) {
		// optionPaintWhite.setAlpha(alphaTutorial);
		// canvas.drawBitmap(bmpQ, srcRectForBig, dstRectForQ, optionPaintWhite);
		// optionPaintWhite.setAlpha(255);
		// } else {
		// canvas.drawBitmap(bmpQ, srcRectForBig, dstRectForQ, optionPaintWhite);
		// }
		// }
		// // Draw the progress icon
		// if (tutorial == 6 && !selectOptions[2]) {
		// optionPaintWhite.setAlpha(alphaTutorial);
		// canvas.drawBitmap(bmpP, srcRectForBig, dstRectForP, optionPaintWhite);
		// optionPaintWhite.setAlpha(255);
		// } else {
		// canvas.drawBitmap(bmpP, srcRectForBig, dstRectForP, optionPaintWhite);
		// }
		// // Draw the store icon
		// if (tutorial == 7 && !selectOptions[1]) {
		// optionPaintWhite.setAlpha(alphaTutorial);
		// canvas.drawBitmap(bmpStore, srcRectForBig, dstRectForE, optionPaintWhite);
		// optionPaintWhite.setAlpha(255);
		// } else {
		// canvas.drawBitmap(bmpStore, srcRectForBig, dstRectForE, optionPaintWhite);
		// }
		//
		// // Draw the Challenges Bubble and Friends selector
		// if (numberOfChallenges > 0) {
		// if (tutorial == 2 && !selectOptions[0]) {
		// optionPaintWhite.setAlpha(alphaTutorial);
		// canvas.drawBitmap(bmpFriendSelected, srcRectForBig, dstRectForI, optionPaintWhite);
		// optionPaintWhite.setAlpha(255);
		// } else {
		// canvas.drawBitmap(bmpFriendSelected, srcRectForBig, dstRectForI, optionPaintWhite);
		// }
		// String challenges = String.valueOf(numberOfChallenges);
		// optionPaintWhite.getTextBounds(challenges, 0, challenges.length(), challengeBounds);
		// int challengeRadius = (int) (Math.sqrt(Math.pow(challengeBounds.width(), 2) + Math.pow(challengeBounds.height(), 2)) * 7 / 8);
		// canvas.drawCircle((Width + test.getIntrinsicWidth()) / 2 + challengeBounds.width(), barY - barHeight, challengeRadius,
		// transparentBlue);
		// canvas.drawText(challenges, (Width + test.getIntrinsicWidth()) / 2 + challengeBounds.width(), barY - barHeight + textSizePix
		// * 3 / 8, optionPaintWhite);
		// } else {
		// if (tutorial == 2 && !selectOptions[0]) {
		// optionPaintWhite.setAlpha(alphaTutorial);
		// canvas.drawBitmap(bmpFriend, srcRectForBig, dstRectForI, optionPaintWhite);
		// optionPaintWhite.setAlpha(255);
		// } else {
		// canvas.drawBitmap(bmpFriend, srcRectForBig, dstRectForI, optionPaintWhite);
		// }
		// }

		// PAI COMMENTED - new UI on oct 27th- 2014 ends here //
	}

	private Bitmap[] bitmapAnswers;
	private Rect[] srcRectForBitmap;

	public void setUpBitmapForAnswer(boolean recycle) {
		if (recycle) {
			if (bitmapAnswers != null) {
				for (int i = 0; i < bitmapAnswers.length; i++) {
					if (bitmapAnswers[i] != null)
						bitmapAnswers[i].recycle();
					bitmapAnswers[i] = null;
				}

			}
			bitmapAnswers = null;
			return;
		}

		if (bitmapAnswers == null) {
			bitmapAnswers = new Bitmap[4];
			srcRectForBitmap = new Rect[4];
		}
		for (int i = 0; i < answers.length; i++) {
			if (answers[i] != null && answers[i].startsWith("image://")) {
				if (Width > 0 && getMiddlePoint() > 0) {
					bitmapAnswers[i] = BitmapFactory.decodeFile(getContext().getExternalCacheDir().getAbsolutePath() + "/"
							+ answers[i].substring(answers[i].lastIndexOf("/") + 1));

					if (bitmapAnswers[i] != null) {
						bitmapAnswers[i] = downscaleBitmap(bitmapAnswers[i], (Width / 2), getMiddlePoint() - 50);
						srcRectForBitmap[i] = new Rect(0, 0, bitmapAnswers[i].getWidth(), bitmapAnswers[i].getHeight());
					}

				}
			}
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int actionType = event.getAction();
		if (!paused) {
			if (actionType == MotionEvent.ACTION_DOWN) {
				invalidate();
				startX = event.getX();
				startY = event.getY();
				if ((startY >= Height - radiusOfSettingsIcons * 2) && (options)) {	// Options selected
					animateHandler.removeCallbacks(startAnimate);
					animateHandler.removeCallbacks(finishAnimate);
					animateHandler.removeCallbacks(pulseLock);
					alphaTutorial = 255;
					touchX = startX;
					touchY = startY;
					// PAI COMMENTED - new UI on oct 27th- 2014 start here //
					// if ((startX >= selectLeft[0]) && (startX <= selectRight[0])) { // friend selected
					// selectOptions[0] = true;
					// optionX = selectLeft[0] + radiusOfSettingsIcons;
					// setArc(180, 150);
					// } else if ((startX >= selectLeft[1]) && (startX <= selectRight[1])) { // store selected
					// selectOptions[1] = true;
					// optionX = selectLeft[1] + radiusOfSettingsIcons;
					// setArc(180, 180);
					// } else if ((startX >= selectLeft[2]) && (startX <= selectRight[2])) { // progress selected
					// selectOptions[2] = true;
					// optionX = selectLeft[2] + radiusOfSettingsIcons;
					// setArc(180, 180);
					// } else if ((startX >= selectLeft[3]) && (startX <= selectRight[3])) { // quiz mode selected
					// selectOptions[3] = true;
					// optionX = selectLeft[3] + radiusOfSettingsIcons;
					// setArc(180, 180);
					// } else if ((startX >= selectLeft[4]) && (startX <= selectRight[4])) { // settings selected
					// selectOptions[4] = true;
					// optionX = selectLeft[4] + radiusOfSettingsIcons;
					// setArc(210, 150);
					// } else
					// return true;
					// if (listener != null)
					// listener.OnSelect(JoystickSelect.ShouldDimScreen, true, -1); // send a vibrate signal
					// PAI COMMENTED - new UI on oct 27th- 2014 ends here //
				} else if (startY >= barY - barHeight * 2) {	// SettingsBar selected
					// PAI COMMENTED - new UI on oct 27th- 2014 start here //
					// animateHandler.removeCallbacks(startAnimate);
					// animateHandler.removeCallbacks(finishAnimate);
					// animateHandler.removeCallbacks(pulseLock);
					// alphaTutorial = 255;
					// selectSideBar = true;
					// invalidate();
					// PAI COMMENTED - new UI on oct 27th- 2014 end here //
				} else if (!problem) {										// if there is no problem set
					if (listener != null)
						listener.OnSelect(JoystickSelect.A, true, -1);		// select A on any touch event
				} else {													// touch was in the main control window
					checkSelection(false, true);
				}
			} else if (actionType == MotionEvent.ACTION_MOVE) {
				touchX = event.getX();
				touchY = event.getY();
				if (touchY > 40 + (bmpUnlock.getHeight() / 2)) { // Pai code for upper bound for lock
					// wentOutOfBox = true;
					// returnToDefault();
					checkSelection(false, false);
					invalidate();
				}
			} else if (actionType == MotionEvent.ACTION_UP) {
				tapTimer = System.currentTimeMillis();
				checkSelection(true, false);
				returnToDefault();
			}

		} else if (actionType == MotionEvent.ACTION_UP) {
			resetGuess();
			// setAlpha(0);
			invalidate();
			if (listener != null && paused) {
				listener.OnSelect(JoystickSelect.Touch, false, -1);
			}
			paused = false;
		}
		return true;
	}

	// =========================================
	// Setup and Initialization Algorithms
	// =========================================

	private void setAppCenters(boolean sel) {
		appCenterVert = (barY - barHeight + drawBackBlue.getIntrinsicHeight() / 3) / 2;
		appCenterHorz = Width / 2;
		int rApps = drawAdd.getIntrinsicHeight() / 2;
		int rAppsY = appCenterVert - rApps - drawBackBlue.getIntrinsicHeight() / 3 - pad;
		int rAppsX = appCenterHorz - rApps - pad;
		appAngle = Math.atan2(rApps * 3, Math.min(rAppsY, rAppsX));
		int oldMaxApps = apps.size();
		// int maxApps = (int) Math.floor((3 * Math.PI / 2) / appAngle); pai commented
		int maxApps = 4;
		if (maxApps < oldMaxApps) {
			maxApps = oldMaxApps;
		}
		if (oldMaxApps < maxApps) {
			apps.clear();
			for (int i = 0; i < maxApps; i++) {
				double angle = Math.PI - appAngle / 2 - appAngle * i;
				apps.add(new App(angle, (float) (appCenterHorz + Math.cos(angle) * rAppsX), (float) (appCenterVert - Math.sin(angle)
						* rAppsY), rApps));
			}
		} else {
			// Pai COde starts here

			int x = Width / 2;
			int y = getMiddlePoint() - 50 + d.get(0).getIntrinsicHeight() / 2;
			apps.get(0).setAll(0, x / 2, y / 2, rApps);
			apps.get(0).left = 0;
			apps.get(0).top = 50;
			apps.get(0).right = Width / 2;
			apps.get(0).bottom = getMiddlePoint();
			apps.get(0).makeRect();

			if (apps.size() > 2) {
				x = (Width / 2) + (Width / 2) / 2;
				y = getMiddlePoint() - 50 + d.get(0).getIntrinsicHeight() / 2;
				apps.get(1).setAll(0, x, y / 2, (Width / 2) / 2);
				apps.get(1).left = Width / 2;
				apps.get(1).top = 50;
				apps.get(1).right = Width;
				apps.get(1).bottom = getMiddlePoint();
				apps.get(1).makeRect();
			}

			if (apps.size() > 3) {
				x = (Width / 2) + (Width / 2) / 2;
				y = getMiddlePoint() + ((getMiddlePoint() - 50) / 2) + d.get(0).getIntrinsicHeight() / 2;
				apps.get(2).setAll(0, x, y - 50, (Width / 2) / 2);
				apps.get(2).left = Width / 2;
				apps.get(2).top = getMiddlePoint();
				apps.get(2).right = Width;
				apps.get(2).bottom = getMiddlePoint() * 2 - 50;
				apps.get(2).makeRect();
			}

			// Set for trash
			int end = d.size() - 1;
			if (end == 4)
				end--;
			x = (Width / 2);
			y = getMiddlePoint();
			apps.get(end).setAll(0, x, y, (Width / 2) / 2);
			apps.get(end).left = Width / 2 - d.get(d.size() - 1).getIntrinsicWidth() / 2;
			apps.get(end).top = getMiddlePoint() - d.get(d.size() - 1).getIntrinsicHeight() / 2;
			apps.get(end).right = Width / 2 + d.get(d.size() - 1).getIntrinsicWidth() / 2;
			apps.get(end).bottom = getMiddlePoint() + d.get(d.size() - 1).getIntrinsicHeight() / 2;
			apps.get(end).makeRect();
			if (rectForBackRed == null)
				rectForBackRed = new RectF();

			// rectForBackRed.set(apps.get(end).f.left - apps.get(end).f.left / 5, apps.get(end).f.top - apps.get(end).f.top / 4,
			// apps.get(end).f.right + apps.get(end).f.right / 5, apps.get(end).f.bottom + apps.get(end).f.bottom / 4);
			int mxLeft = Width / 2 - (int) (drawAdd.getIntrinsicWidth() * 1.5);
			int myTop = getMiddlePoint() - (int) (drawAdd.getIntrinsicHeight() * 1.5);
			int mxRight = Width / 2 + (int) (drawAdd.getIntrinsicWidth() * 1.5);
			int myBottom = getMiddlePoint() + (int) (drawAdd.getIntrinsicHeight() * 1.5);
			rectForBackRed.set(mxLeft, myTop, mxRight, myBottom);

			// apps.get(2).left = Width / 2;
			// apps.get(2).top = getMiddlePoint();
			// apps.get(2).right = Width;
			// apps.get(2).bottom = getMiddlePoint() * 2 - 50;
			// apps.get(2).makeRect();
			// Pai COde ends here
			for (int i = 0; i < apps.size(); i++) {
				double angle = Math.PI - appAngle / 2 - appAngle * i;
				// apps.get(i).setAll(angle, (float) (appCenterHorz + Math.cos(angle) * rAppsX),
				// (float) (appCenterVert - Math.sin(angle) * rAppsY), rApps);
				// int x = Width / 2;
				// int y = getMiddlePoint() - 50 + d.get(0).getIntrinsicHeight() / 2;
				// apps.get(i).setAll(0, x / 2, y / 2, rApps);
				if (!sel) {
					apps.get(i).setSelect(false);
					apps.get(i).setSelectDrag(false);
				}
			}
		}
	}

	// pai -
	private void returnToDefault() {
		touchX = 0;
		touchY = 0;
		startX = -rUnlock;
		startY = -rUnlock;
		for (int ans = 0; ans < NumAnswers; ans++) {
			selectAnswers[ans] = false;
		}
		for (int opt = 0; opt < NumOptions; opt++) {
			selectOptions[opt] = false;
		}
		for (int i = 0; i < apps.size(); i++) {
			apps.get(i).setSelect(false);
			apps.get(i).setSelectDrag(false);
			appDragX = 0;
			appDragY = 0;
		}
		selectUnlock = false;
		selectAppDrag = -1;

		if (options && !shouldOpenOptionFromTutorial(tutorial)) {
			showStartAnimation(0, 3000);
		} else if (selectSideBar && !shouldOpenOptionFromTutorial(tutorial)) {
			showStartAnimation(1, 0);
		} else if (shouldOpenOptionFromTutorial(tutorial)) {
			// setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
			setSidePaths(Height - pad);
			options = true;
		} else {
			int currentRadius = (int) ((RectForUnlockPulse.right - RectForUnlockPulse.left) / 2);
			RectForUnlockPulse.set(Width / 2 - currentRadius, ((int) (((barY - barHeight) / 2) * 1.9) + centerOffset) - currentRadius,
					Width / 2 + currentRadius, ((int) (((barY - barHeight) / 2) * 1.9) + centerOffset) + currentRadius);
			RectForUnlock.set(Width / 2 - rUnlock, ((int) (((barY - barHeight) / 2) * 1.9) + centerOffset) - rUnlock, Width / 2 + rUnlock,
					((int) (((barY - barHeight) / 2) * 1.9) + centerOffset) + rUnlock);
		}
		selectSideBar = false;

		for (int ans = 0; ans < NumAnswers; ans++) {
			X[ans] = -rApps;
			Y[ans] = -rApps;
		}

		if (listener != null)
			listener.OnSelect(JoystickSelect.ReturnToDefault, false, -1);
		startTimeRevealOrHide = 0;
		startTimePulse = 0;
		// animateHandler.removeCallbacks(hideAnswers);
		animateHandler.removeCallbacks(pulseLock);
		// animateHandler.postDelayed(hideAnswers, tapLength);
		if (shouldOpenOptionFromTutorial(tutorial))
			animateHandler.postDelayed(pulseLock, ICON_PAUSE);
		else
			animateHandler.postDelayed(pulseLock, PULSE_PAUSE);
		invalidate();
	}

	private void checkSelection(boolean send, boolean firstTouch) {
		double diffx, diffy;
		if ((selectOptions[0]) || (selectOptions[1]) || (selectOptions[2]) || (selectOptions[3]) || (selectOptions[4]) || (selectSideBar)) {
			JoystickSelect s = JoystickSelect.Vibrate;
			diffx = touchX - startX;
			diffy = touchY - startY;
			if (selectOptions[0]) {
				s = JoystickSelect.Friends;
				dstRectForI.set((int) touchX - radiusOfSettingsIcons, (int) touchY - radiusOfSettingsIcons, (int) touchX
						+ radiusOfSettingsIcons, (int) touchY + radiusOfSettingsIcons);
			} else if (selectOptions[1]) {
				s = JoystickSelect.Store;
				dstRectForE.set((int) touchX - radiusOfSettingsIcons, (int) touchY - radiusOfSettingsIcons, (int) touchX
						+ radiusOfSettingsIcons, (int) touchY + radiusOfSettingsIcons);
			} else if (selectOptions[2]) {
				s = JoystickSelect.Progress;
				dstRectForP.set((int) touchX - radiusOfSettingsIcons, (int) touchY - radiusOfSettingsIcons, (int) touchX
						+ radiusOfSettingsIcons, (int) touchY + radiusOfSettingsIcons);
			} else if (selectOptions[3]) {
				s = JoystickSelect.QuizMode;
				dstRectForQ.set((int) touchX - radiusOfSettingsIcons, (int) touchY - radiusOfSettingsIcons, (int) touchX
						+ radiusOfSettingsIcons, (int) touchY + radiusOfSettingsIcons);
			} else if (selectOptions[4]) {
				s = JoystickSelect.Settings;
				dstRectForS.set((int) touchX - radiusOfSettingsIcons, (int) touchY - radiusOfSettingsIcons, (int) touchX
						+ radiusOfSettingsIcons, (int) touchY + radiusOfSettingsIcons);
			} else if (selectSideBar) {
				setSidePaths((int) Math.max(touchY, Height - radiusOfSettingsIcons * 2 - pad * 3));
			}
			if (selectSideBar) {
				if ((send) && (touchY <= Height - dstHeight)) {
					options = true;
				} else if ((send) && (touchY > Height - dstHeight)) {
					options = false;
				}
			} else if (Math.sqrt(diffx * diffx + diffy * diffy) > swipeLengthOption) {
				for (int i = 0; i < selectOptions.length; i++)
					selectOptions[i] = false;
				listener.OnSelect(s, true, -1);
			} else if (send) {
				Toast.makeText(ctx, res.getString(R.string.swipe_option), Toast.LENGTH_SHORT).show();
			}
		} else {
			for (int i = 0; i < NumAnswers; i++) {
				selectAnswers[i] = false;
			}
			for (int i = 0; i < apps.size(); i++) {
				apps.get(i).setSelect(false);
			}
			if (selectUnlock) {
				diffx = touchX - startX;
				diffy = touchY - startY;
				double newX = touchX, newY = touchY;
				RectForUnlockPulse.set((int) touchX - rUnlock, (int) touchY - rUnlock, (int) touchX + rUnlock, (int) touchY + rUnlock);
				RectForUnlock.set((int) touchX - rUnlock, (int) touchY - rUnlock, (int) touchX + rUnlock, (int) touchY + rUnlock);

				if (Math.sqrt(diffx * diffx + diffy * diffy) > swipeLength1) {
					int select = -1;

					// if (newX < RectForAnswers[0].right) {
					// if (newY < RectForAnswers[0].bottom)
					// select = 0;
					// else if (newY > RectForAnswers[2].top)
					// select = 2;
					// } else if (newX > RectForAnswers[1].left) {
					// if (newY < RectForAnswers[1].bottom)
					// select = 1;
					// else if (newY > RectForAnswers[3].top)
					// select = 3;
					// }
					for (int kk = 0; kk < 4; kk++) {
						if (RectForAnswers[kk].contains((float) newX, (float) newY)) {
							select = kk;
							break;
						}
					}

					if (quickUnlock) {
						if (select == correctLoc) {
							selectAnswers[select] = true;
							if (send) {
								listener.OnSelect(JoystickSelect.fromValue(select), true, -1);
							}
						} else {
							double angle = Math.atan2(appCenterVert - newY, newX - appCenterHorz);
							if (angle < 0)// because atan2 returns from -pi to pi, we want 0 to 2pi
								angle += 2 * Math.PI;
							int selection = -1;
							for (int i = 0; i < apps.size(); i++) {
								double angleDiff = Math.abs(apps.get(i).getAngle() - angle);
								RectF rect = new RectF(apps.get(i).left, apps.get(i).top, apps.get(i).right, apps.get(i).bottom);
								if (rect.contains((float) newX, (float) newY)) {
									selection = i;
									break;
								}
								// if (angleDiff < appAngle / 2) {
								// selection = i;
								// break;
								// }
							}
							if (selection >= 0)
								if (send)
									if (selection == 0)
										if (isFirstApp)
											listener.OnSelect(JoystickSelect.SelectApp, true, selection);
										else
											listener.OnSelect(JoystickSelect.AddApp, true, -1);
									else if (isFirstApp)
										listener.OnSelect(JoystickSelect.SelectApp, true, selection);
									else
										listener.OnSelect(JoystickSelect.SelectApp, true, selection - 1);
								else
									apps.get(selection).setSelect(true);
						}
					} else if (select >= 0) {
						if (send) {
							if (select == shareLoc) {
								if (answers[shareLoc].equals(res.getString(R.string.store)))
									listener.OnSelect(JoystickSelect.Store, true, -1);
								else
									listener.OnSelect(JoystickSelect.Share, true, 0);
							} else if (select == sponsoredLoc) {
								listener.OnSelect(JoystickSelect.Sponsored, true, select);
							} else if (select == MoreGamesLoc) {
								listener.OnSelect(JoystickSelect.MoreGames, true, select);
							} else {
								listener.OnSelect(JoystickSelect.fromValue(select), true, 0);
							}
						} else {
							selectAnswers[select] = true;
						}
					}
				}
			} else if (selectAppDrag >= 0) {
				appDragX = (float) touchX - apps.get(selectAppDrag).getX();
				appDragY = (float) touchY - apps.get(selectAppDrag).getY();
				int trashLoc = Math.min(apps.size() - 1, d.size() - 1);
				App trashApp = apps.get(trashLoc);
				if ((touchX < trashApp.getRight()) && (touchX > trashApp.getLeft()) && (touchY < trashApp.getBottom())
						&& (touchY > trashApp.getTop())) {
					if (send) {
						if (isFirstApp) {
							listener.OnSelect(JoystickSelect.DeleteApp, true, selectAppDrag);
							removeApp(selectAppDrag + 1);
						} else {
							listener.OnSelect(JoystickSelect.DeleteApp, true, selectAppDrag - 1);
							removeApp(selectAppDrag);
						}
					} else
						apps.get(trashLoc).setSelect(true);
				}
			} else {
				touchX = startX;
				touchY = startY;
				diffx = startX - (RectForUnlock.right + RectForUnlock.left) / 2;
				diffy = startY - (RectForUnlock.top + RectForUnlock.bottom) / 2;
				if (Math.sqrt(diffx * diffx + diffy * diffy) < rUnlock * 1.5) {
					if (((tapTimer + tapLength) > System.currentTimeMillis()) && (!quickUnlock)) {
						Log.d("TAG", "TESTING");
						quickUnlock = true;
						// centerOffset = drawBackBlue.getIntrinsicHeight() / 3;
						// send a quickUnlock mode activated signal back to mainActivity
						listener.OnSelect(JoystickSelect.QuickUnlock, false, -1);
					}
					startX = (RectForUnlock.right + RectForUnlock.left) / 2;
					startY = (RectForUnlock.top + RectForUnlock.bottom) / 2;
					RectForUnlockPulse.set((int) touchX - rUnlock, (int) touchY - rUnlock, (int) touchX + rUnlock, (int) touchY + rUnlock);
					RectForUnlock.set((int) touchX - rUnlock, (int) touchY - rUnlock, (int) touchX + rUnlock, (int) touchY + rUnlock);

					selectUnlock = true;	// the lock was selected
					if (listener != null)
						listener.OnSelect(JoystickSelect.ShouldDimScreen, true, -1);		// send a vibrate signal
					startTimeRevealOrHide = System.currentTimeMillis();
					// animateHandler.removeCallbacks(hideAnswers);
					// animateHandler.removeCallbacks(revealAnswers);
					// animateHandler.post(revealAnswers);
				} else if (firstTouch) {
					if (quickUnlock) {
						int offset = 1;
						if (isFirstApp)
							offset = 0;
						int end = Math.min(d.size() - 1, apps.size() - 1);
						for (int i = offset; i < end; i++) {
							if ((touchX < apps.get(i).getRight()) && (touchX > apps.get(i).getLeft()) && (touchY < apps.get(i).getBottom())
									&& (touchY > apps.get(i).getTop())) {
								apps.get(i).setSelectDrag(true);
								selectAppDrag = i;
								appDragX = (float) touchX - apps.get(i).getX();
								appDragY = (float) touchY - apps.get(i).getY();
							}
						}
					} else {
						if (listener != null)
							listener.OnSelect(JoystickSelect.Missed, false, -1);		// send a missed the lock signal
					}
				}
			}
		}
	}

	private void initRunnables() {
		final int slideInterval = (dstHeight - pad) / startFrames;

		startAnimate = new Runnable() {
			@Override
			public void run() {
				if (shouldOpenOptionFromTutorial(tutorial)) {
					setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
					options = true;
				} else {
					options = false;
					if (barY + slideInterval > Height - pad) {
						setSidePaths(Height - pad);
					} else {
						setSidePaths(barY + slideInterval);
					}
				}
				invalidate();
			}
		};
		finishAnimate = new Runnable() {
			@Override
			public void run() {
				if (shouldOpenOptionFromTutorial(tutorial)) {
					// setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
					setSidePaths(Height - pad);
					options = true;
				} else {
					setSidePaths(Height - pad);
					options = false;
				}
				invalidate();
			}
		};
		// revealAnswers = new Runnable() {
		// @Override
		// public void run() {
		// lastTimeRevealOrHide = System.currentTimeMillis();
		// float percent = (lastTimeRevealOrHide - startTimeRevealOrHide) / (float) IN_OUT_DURATION;
		// if (percent <= 1) {
		// setAlpha(Math.max(alphaAnswer, (int) (percent * 255)));
		// invalidate();
		// animateHandler.postDelayed(revealAnswers, frameTimeReveal);
		// } else {
		// setAlpha(255);
		// invalidate();
		// }
		// }
		// };
		// hideAnswers = new Runnable() {
		// @Override
		// public void run() {
		// if (startTimeRevealOrHide == 0)
		// startTimeRevealOrHide = System.currentTimeMillis();
		// lastTimeRevealOrHide = System.currentTimeMillis();
		// float percent = (lastTimeRevealOrHide - startTimeRevealOrHide) / (float) IN_OUT_DURATION;
		// if (percent <= 1) {
		// setAlpha(Math.min(alphaAnswer, (int) ((1 - percent) * 255)));
		// invalidate();
		// animateHandler.postDelayed(hideAnswers, frameTimeReveal);
		// } else {
		// setAlpha(0);
		// invalidate();
		// }
		// }
		// };
		// pai -
		pulseLock = new Runnable() {
			@Override
			public void run() {
				float percent = -1, iconPercent = -1;
				boolean shouldPulseLock = false;
				if (!selectUnlock) {
					shouldPulseLock = true;
					if (startTimePulse == 0)
						startTimePulse = System.currentTimeMillis();
					lastTimePulse = System.currentTimeMillis();
					percent = (lastTimePulse - startTimePulse) / (float) PULSE_DURATION;
					iconPercent = (lastTimePulse - startTimePulse) / (float) ICON_DURATION;
					int iconFrame = 0;
					if (percent < 1 && percent > 0) {
						pulseFrame = (int) (percent * pulseFrames);
					} else {
						pulseFrame = 0;
					}
					if (iconPercent < 1 && iconPercent > 0) {
						iconFrame = (int) (iconPercent * pulseFrames);
						alphaTutorial = Math.min(alphaTutorial, (int) ((1 - iconPercent * 1.1) * 255));
						if (alphaTutorial < 0)
							alphaTutorial = 0;
					} else {
						iconFrame = 0;
						alphaTutorial = 255;
					}
					int centerX, centerY;
					switch (tutorial) {
					case 2: // Challenge your friends \n (slide the icon up)
						shouldPulseLock = false;
						centerX = (int) (easingSlideUpFunctionX[iconFrame] + (selectLeft[0] + selectRight[0]) / 2);
						centerY = (int) (easingSlideUpFunctionY[iconFrame] + dstRectForS.centerY());
						dstRectForI.set(centerX - radiusOfSettingsIcons, centerY - radiusOfSettingsIcons, centerX + radiusOfSettingsIcons,
								centerY + radiusOfSettingsIcons);
						break;
					case 3: // Change Any Setting at any time \n (slide the icon up)
						shouldPulseLock = false;
						centerX = (int) (easingSlideUpFunctionX[iconFrame] + (selectLeft[4] + selectRight[4]) / 2);
						centerY = (int) (easingSlideUpFunctionY[iconFrame] + dstRectForQ.centerY());
						dstRectForS.set(centerX - radiusOfSettingsIcons, centerY - radiusOfSettingsIcons, centerX + radiusOfSettingsIcons,
								centerY + radiusOfSettingsIcons);
						break;
					case 5: // Quiz mode for endless questions (slide the icon up)
						shouldPulseLock = false;
						centerX = (int) (easingSlideUpFunctionX[iconFrame] + (selectLeft[3] + selectRight[3]) / 2);
						centerY = (int) (easingSlideUpFunctionY[iconFrame] + dstRectForP.centerY());
						dstRectForQ.set(centerX - radiusOfSettingsIcons, centerY - radiusOfSettingsIcons, centerX + radiusOfSettingsIcons,
								centerY + radiusOfSettingsIcons);
						break;
					case 6: // Check your progress (slide the icon up)
						shouldPulseLock = false;
						centerX = (int) (easingSlideUpFunctionX[iconFrame] + (selectLeft[2] + selectRight[2]) / 2);
						centerY = (int) (easingSlideUpFunctionY[iconFrame] + dstRectForE.centerY());
						dstRectForP.set(centerX - radiusOfSettingsIcons, centerY - radiusOfSettingsIcons, centerX + radiusOfSettingsIcons,
								centerY + radiusOfSettingsIcons);
						break;
					case 7: // Unlock more question packs (slide the icon up)
						shouldPulseLock = false;
						centerX = (int) (easingSlideUpFunctionX[iconFrame] + (selectLeft[1] + selectRight[1]) / 2);
						centerY = (int) (easingSlideUpFunctionY[iconFrame] + dstRectForI.centerY());
						dstRectForE.set(centerX - radiusOfSettingsIcons, centerY - radiusOfSettingsIcons, centerX + radiusOfSettingsIcons,
								centerY + radiusOfSettingsIcons);
						break;
					}
					if (shouldPulseLock) {
						centerX = (int) (easingPulseFunctionX[pulseFrame] + Width / 2);
						centerY = (int) (easingPulseFunctionY[pulseFrame] + (barY - barHeight + centerOffset) / 2);
						RectForUnlockPulse.set(centerX - rUnlock, (int) (centerY * 1.9) - rUnlock, centerX + rUnlock, (int) (centerY * 1.9)
								+ rUnlock);
						RectForUnlock.set(centerX - rUnlock, (int) (centerY * 1.9) - rUnlock, centerX + rUnlock, (int) (centerY * 1.9)
								+ rUnlock);
					}
				}
				invalidate();
				if (shouldPulseLock) {
					if (percent < 1) {
						animateHandler.post(pulseLock);
					} else {
						startTimePulse = 0;
						animateHandler.postDelayed(pulseLock, PULSE_PAUSE);
					}
				} else {
					if (iconPercent < 1) {
						animateHandler.post(pulseLock);
					} else {
						startTimePulse = 0;
						animateHandler.postDelayed(pulseLock, ICON_PAUSE);
					}
				}
			}
		};
	}

	// pai -
	private void setSidePaths(int side) {
		barY = side;
		int middle = (barY - barHeight + centerOffset) / 2;
		int bottom = barY - barHeight;
		int middlepoint = getMiddlePoint();
		// RectForAnswers[0].set(0, 0, Width / 2, middle);
		// RectForAnswers[1].set(Width / 2, 0, Width, middle);
		// RectForAnswers[2].set(0, middle, Width / 2, middle * 2);
		// RectForAnswers[3].set(Width / 2, middle * 2, Width, middle * 2);

		RectForAnswers[0].set(0, 50, Width / 2, middlepoint);
		RectForAnswers[1].set(Width / 2, 50, Width, middlepoint);
		RectForAnswers[2].set(0, middlepoint, Width / 2, middlepoint * 2 - 50);
		RectForAnswers[3].set(Width / 2, middlepoint, Width, middlepoint * 2 - 50);

		RectForAnswersBackground[0].set(0, 50, Width / 2, middlepoint);
		RectForAnswersBackground[1].set(Width / 2, 50, Width, middlepoint);
		RectForAnswersBackground[2].set(0, middlepoint, Width / 2, middlepoint * 2 - 50);
		RectForAnswersBackground[3].set(Width / 2, middlepoint, Width, middlepoint * 2 - 50);

		// srcRectForBack = RectForAnswers;

		dstRectForOpt.set(0, Math.max(optionY - swipeLengthOption * 3, 0), Width, Height);
		dstPathForSet.reset();
		dstPathForSet.moveTo(spacing / 2, Height);
		dstPathForSet.lineTo(spacing / 2, barY + pad * 2);
		dstPathForSet.lineTo(Width / 2 - bmpSelectBar.getWidth(), barY + pad * 2);
		dstPathForSet.lineTo(Width / 2 - bmpSelectBar.getWidth() / 2, barY - barHeight + pad);
		dstPathForSet.lineTo(Width / 2 + bmpSelectBar.getWidth() / 2, barY - barHeight + pad);
		dstPathForSet.lineTo(Width / 2 + bmpSelectBar.getWidth(), barY + pad * 2);
		dstPathForSet.lineTo(Width - spacing / 2, barY + pad * 2);
		dstPathForSet.lineTo(Width - spacing / 2, Height);

		int temp = side + radiusOfSettingsIcons * 2 + pad * 3;
		dstRectForS.set(selectLeft[4], temp - radiusOfSettingsIcons * 2, selectRight[4], temp);
		dstRectForQ.set(selectLeft[3], temp - radiusOfSettingsIcons * 2, selectRight[3], temp);
		dstRectForP.set(selectLeft[2], temp - radiusOfSettingsIcons * 2, selectRight[2], temp);
		dstRectForE.set(selectLeft[1], temp - radiusOfSettingsIcons * 2, selectRight[1], temp);
		// dstRectForI.set(selectLeft[0], temp - rBig * 2, selectRight[0], temp);
		dstRectForI.set(selectLeft[0], temp - radiusOfSettingsIcons * 2, selectRight[0], temp);
		if (!selectUnlock) {
			int currentRadius = (int) ((RectForUnlockPulse.right - RectForUnlockPulse.left) / 2);
			if (currentRadius <= 0)
				currentRadius = rUnlock;
			RectForUnlockPulse.set(Width / 2 - currentRadius, (int) ((middle * 1.9)) - currentRadius, Width / 2 + currentRadius,
					(int) ((middle * 1.9)) + currentRadius);
			RectForUnlock.set(Width / 2 - rUnlock, (int) ((middle * 1.9)) - rUnlock, Width / 2 + rUnlock, (int) ((middle * 1.9)) + rUnlock);// pai
																																			// *2
		}
		setAppCenters(selectAppDrag >= 0);
	}

	private int getMiddlePoint() {
		int[] loc = new int[2];
		getLocationOnScreen(loc);
		WindowManager window = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		window.getDefaultDisplay().getMetrics(displaymetrics);
		return displaymetrics.heightPixels / 2 - loc[1];
	}

	private void setDimensions() {
		setLayouts();
		float maxH = 0;
		for (int i = 0; i < NumAnswers; i++) {
			if (!equation[i]) {
				maxH = Math.max(bounds[i].height(), layout[i].getHeight());
				maxH = Math.max(maxH, answerSizePix);
				if ((maxH > ((barY - barHeight) / 2 - pad * 3 - rUnlock)) && (Height > 0)) {
					if (decreaseAnswerSize()) {
						setDimensions();
						return;
					}
				} else if (isLayoutSplittingWords(answers[i], layout[i])) {
					if (decreaseAnswerSize()) {
						setDimensions();
						return;
					}
				}
			} else {
				if (layoutE[i].getTextSizePix() > answerSizePix) {
					layoutE[i].setTextSize(answerSizeSP, answerSizePix);
				}
			}
		}

		answerTextPaintBackup.setTextSize(answerSizePix);
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
		invalidate();
	}

	private void setLayouts() {
		for (int i = 0; i < NumAnswers; i++) {
			equation[i] = false;
			if (answers[i].length() > 1)
				if (answers[i].charAt(0) == '$')
					if (answers[i].length() > 1)
						if (answers[i].charAt(1) != '$')
							equation[i] = true;
			answerTextPaint[i].getTextBounds(answers[i], 0, answers[i].length(), bounds[i]);
			if (equation[i]) {
				int W = Width / 2 - pad * 2;
				int H = (Height - radiusOfSettingsIcons * 2 - pad - barHeight) / 2 - pad * 3 - rUnlock;
				if (!layoutE[i].isComputed(answers[i], W, H)) {
					layoutE[i] = new EquationLayout(answers[i], W, H, answerTextPaint[i], answerSizeSP);
					if (layoutE[i].getTextSizePix() < answerSizePix)
						changeAnswerSize(layoutE[i].getTextSizeSP(), layoutE[i].getTextSizePix());
				}
			} else {
				layout[i] = new StaticLayout(answers[i], answerTextPaint[i], Width / 2 - pad * 2, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0,
						false);
				layoutBackup[i] = new StaticLayout(answers[i], answerTextPaintBackup, Width / 2 - pad * 2, Layout.Alignment.ALIGN_NORMAL,
						1.0f, 0, false);
			}
		}
		invalidate();
	}

	private void setAlpha(int alpha) {
		alphaAnswer = alpha;// (wrongGuess < 0) ? alpha : 255;
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setAlpha((wrongGuess < 0 && !quickUnlock) ? alpha : 255);
			if (!equation[i]) {
				layout[i] = new StaticLayout(answers[i], answerTextPaint[i], Width / 2 - pad * 2, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0,
						false);
			}
		}
	}

	private boolean isLayoutSplittingWords(String string, StaticLayout layout) {
		for (int line = 0; line < layout.getLineCount() - 1; line++) {
			if (string.charAt(layout.getLineEnd(line) - 1) != ' ' && string.charAt(layout.getLineEnd(line) - 1) != '\n') {
				return true;
			}
		}
		return false;
	}

	private boolean decreaseAnswerSize() {
		if (answerSizeSP == 1)
			return false;
		answerSizeSP = Math.max(answerSizeSP - 3, 1);
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, res.getDisplayMetrics());
		answerTextPaintBackup.setTextSize(answerSizePix);
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
		return true;
	}

	private void changeAnswerSize(int SP, float Pix) {
		answerSizeSP = SP;
		answerSizePix = Pix;
		answerTextPaintBackup.setTextSize(answerSizePix);
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
	}

	private void resetAnswerSize() {
		answerSizeSP = answerSizeSPDefault;
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, res.getDisplayMetrics());
		answerTextPaintBackup.setTextSize(answerSizePix);
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
	}

	private Bitmap downscaleBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scaleWidth = ((float) reqWidth) / width;
		float scaleHeight = ((float) reqHeight) / height;

		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// RECREATE THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
		bitmap.recycle();
		return resizedBitmap;
	}

	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		int height = options.outHeight;
		int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		}

		if (inSampleSize >= 3) {
			inSampleSize = 2;
		}
		return inSampleSize;
	}

	private Bitmap scaleImage(Bitmap map) {
		// Get the ImageView and its bitmap
		if (map == null) {
			return null; // Checking for null & return, as suggested in comments
		}

		// Get current dimensions AND the desired bounding box
		int width = map.getWidth();
		int height = map.getHeight();
		int bounding = getMiddlePoint() - 50;
		Log.i("Test", "original width = " + Integer.toString(width));
		Log.i("Test", "original height = " + Integer.toString(height));
		Log.i("Test", "bounding = " + Integer.toString(bounding));

		// Determine how much to scale: the dimension requiring less scaling is
		// closer to the its side. This way the image always stays inside your
		// bounding box AND either x/y axis touches it.
		float xScale = ((float) bounding) / width;
		float yScale = ((float) bounding) / height;
		float scale = (xScale <= yScale) ? xScale : yScale;
		Log.i("Test", "xScale = " + Float.toString(xScale));
		Log.i("Test", "yScale = " + Float.toString(yScale));
		Log.i("Test", "scale = " + Float.toString(scale));

		// Create a matrix for the scaling and add the scaling data
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		// Create a new bitmap and convert it to a format understood by the ImageView
		Bitmap scaledBitmap = Bitmap.createBitmap(map, 0, 0, width, height, matrix, true);
		width = scaledBitmap.getWidth(); // re-use
		height = scaledBitmap.getHeight(); // re-use
		Log.i("Test", "scaled width = " + Integer.toString(width));
		Log.i("Test", "scaled height = " + Integer.toString(height));

		// Now change ImageView's dimensions to match the scaled image

		Log.i("Test", "done");
		return scaledBitmap;
	}

	private void setArc(int startDeg, int totalDeg) {
		RectF optionPathRectF = new RectF(optionX - swipeLengthOption, optionY - swipeLengthOption, optionX + swipeLengthOption, optionY
				+ swipeLengthOption);
		int centerX = (int) (optionPathRectF.centerX());
		Rect bounds = new Rect();
		optionPaintWhite.getTextBounds(res.getString(R.string.swipe_here), 0, res.getString(R.string.swipe_here).length(), bounds);
		if (centerX - bounds.width() / 2 < 0)
			centerX -= centerX - bounds.width() / 2 - pad * 3;
		else if (centerX + bounds.width() / 2 > Width)
			centerX += Width - centerX - bounds.width() / 2 - pad * 3;
		optionPathCenterX = centerX;
		optionPathCenterY = (int) (optionPathRectF.top);
		optionPath.reset();
		optionPath.arcTo(optionPathRectF, startDeg, totalDeg);
	}
}