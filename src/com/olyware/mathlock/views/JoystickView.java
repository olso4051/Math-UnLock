package com.olyware.mathlock.views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.olyware.mathlock.R;

public class JoystickView extends View {
	private final int NumAnswers = 5, answerSizeSPDefault = 40, answerHintSizeSPDefault = 30, textFrames = 10, textFrameTime = 50,
			answerFrameTime = 33, startFrames = 30, startFrameTime = 50, pulseFrames = 30, pulseFrameTime = 33, spinFrameTime = 33;
	private final long tapLength = 250;
	private final float degreeStepInitial = 1;

	private Bitmap bmpS, bmpQ, bmpQs, bmpP, bmpStore, bmpI, bmpUnlock, bmpHand, bmpArrow;
	private Bitmap[] bmpBack = new Bitmap[3];
	private RectF[] RectForAnswers = new RectF[NumAnswers + 1];
	private Rect[] bounds = new Rect[NumAnswers];
	private RectF dstRectForSet, dstRectForOpt, RectForUnlock, RectForUnlockPulse;
	private Rect dstRectForS, dstRectForQ, dstRectForP, dstRectForE, dstRectForI, srcRectForBack, srcRectForUnlock, srcRectForBig,
			srcRectForSmall;
	private Matrix rotateHand, rotateArrow;

	private TextPaint[] circleTextPaint = new TextPaint[NumAnswers], answerTextPaint = new TextPaint[NumAnswers];
	private TextPaint optionPaintWhite, textPaintWhite, textPaintBlack;
	private Path optionPath;
	private Paint[] circlePaint = new Paint[NumAnswers];
	private Paint settingsPaint, optPaint, unlockPaint;

	private int TextHeight, centerOffset, textSizeSP, textSizePix, answerSizeSP, answerHintSizeSP, Width, Height, dstHeight, pad = 10,
			diffX1, diffY1, diffX, diffY, spacing, rUnlock, rUnlockChange, rBig, rSmall, rApps, swipeLengthOption, swipeLength1, type = 0,
			pulseFrame = 0, correctLoc, correctGuess, wrongGuess, selectAppDrag, numAnswersDisplayed = 0;
	private int state = 0;// state: direction answers are going (0=up-right, 1=up-left, 2=down-left, 3=down-right)
	private long tapTimer, lastTime = 0;
	private float answerSizePix, answerHintSizePix, optionX, optionY, degrees = 0, radians = 0, degreeStep = degreeStepInitial,
			appDragX = 0, appDragY = 0, strokeWidth;
	private double touchX, touchY, startX, startY, appAngle;
	private boolean quizMode, quickUnlock, selectUnlock, options = false, selectSideBar = false, showHint = false, problem = true,
			wrong = false, paused = false, measured = false, isFirstApp = false, isFirstApp2 = false;

	private int[] selectLeft = new int[5], selectRight = new int[5];
	private double[] X = new double[NumAnswers], Y = new double[NumAnswers], sin = new double[pulseFrames];
	private boolean[] selectAnswers = new boolean[NumAnswers], selectOptions = new boolean[5];
	private String[] answers = { "N/A", "N/A", "N/A", "N/A", "?" };
	private String[] answerTitles;

	private StaticLayout[] layout = new StaticLayout[NumAnswers], layoutAnswers = new StaticLayout[NumAnswers];
	private EquationLayout[] layoutE = new EquationLayout[NumAnswers];
	private boolean[] equation = new boolean[NumAnswers];

	private JoystickSelectListener listener;
	private JoystickTouchListener listenerTouch;

	private Runnable revealText, finishText, startAnimate, finishAnimate, pulseLock, spin, revealAnswers;
	private Handler answerHandler, textHandler, animateHandler;

	private List<Drawable> d;
	private List<App> apps;
	private Drawable drawAdd, drawTrash, drawBackBlue, drawBackRed;
	private Resources res;
	private Context ctx;

	private class App {
		private double angle;
		private float X, Y, left, top, right, bottom;
		private boolean select, selectDrag;

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

		public double getAngle() {
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
		type = 0;
		Width = getMeasuredWidth();
		Height = getMeasuredHeight();

		res = getResources();
		answerTitles = res.getStringArray(R.array.answer_titles);

		textSizeSP = 20;
		textSizePix = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, res.getDisplayMetrics());
		textHandler = new Handler();
		answerHandler = new Handler();
		animateHandler = new Handler();

		textPaintWhite = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaintWhite.setTextAlign(Paint.Align.CENTER);
		textPaintWhite.setColor(Color.WHITE);
		textPaintWhite.setTextSize(textSizePix);
		optionPaintWhite = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		optionPaintWhite.setTextAlign(Paint.Align.CENTER);
		optionPaintWhite.setColor(Color.WHITE);
		optionPaintWhite.setTextSize(textSizePix);
		textPaintBlack = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaintBlack.setTextAlign(Paint.Align.CENTER);
		textPaintBlack.setColor(Color.BLACK);
		textPaintBlack.setTextSize(textSizePix);

		settingsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		settingsPaint.setStyle(Paint.Style.FILL);
		optPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		optPaint.setStyle(Paint.Style.FILL);

		optionPath = new Path();
		options = false;

		answerSizeSP = answerSizeSPDefault;
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, res.getDisplayMetrics());

		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i] = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			answerTextPaint[i].setTextAlign(Paint.Align.CENTER);
			answerTextPaint[i].setColor(Color.WHITE);
			answerTextPaint[i].setTextSize(answerSizePix);
			bounds[i] = new Rect();
			equation[i] = false;
			if (answers[i].charAt(0) == '$')
				if (answers[i].length() > 1)
					if (answers[i].charAt(1) != '$')
						equation[i] = true;

			layoutE[i] = new EquationLayout(answers[i], Width, Height, answerTextPaint[i], answerSizeSPDefault);
			layout[i] = new StaticLayout(answers[i], answerTextPaint[i], Width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
		}

		bmpS = BitmapFactory.decodeResource(res, R.drawable.select_s2);
		bmpQ = BitmapFactory.decodeResource(res, R.drawable.select_q2);
		bmpQs = BitmapFactory.decodeResource(res, R.drawable.select_q2s);
		bmpP = BitmapFactory.decodeResource(res, R.drawable.select_p2);
		bmpStore = BitmapFactory.decodeResource(res, R.drawable.select_store2);
		bmpI = BitmapFactory.decodeResource(res, R.drawable.select_i2);
		bmpUnlock = BitmapFactory.decodeResource(res, R.drawable.unlock);
		bmpHand = BitmapFactory.decodeResource(res, R.drawable.swipe_hand);
		bmpArrow = BitmapFactory.decodeResource(res, R.drawable.swipe_arrow);
		bmpBack[0] = BitmapFactory.decodeResource(res, R.drawable.gradient_background_blue);
		bmpBack[1] = BitmapFactory.decodeResource(res, R.drawable.gradient_background_green);
		bmpBack[2] = BitmapFactory.decodeResource(res, R.drawable.gradient_background_red);
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
		drawBackRed.setBounds(-drawBackRed.getIntrinsicWidth() / 2, -drawBackRed.getIntrinsicHeight() / 2,
				drawBackRed.getIntrinsicWidth() / 2, drawBackRed.getIntrinsicHeight() / 2);

		touchX = 0;
		touchY = 0;

		rApps = drawAdd.getIntrinsicHeight() * 3 / 4;
		rBig = Math.max(bmpQ.getWidth(), bmpQ.getHeight()) / 2;
		rUnlock = rBig;
		rUnlockChange = rUnlock / 5;
		swipeLengthOption = rBig * 4;
		swipeLength1 = rUnlock * 2;
		rSmall = Math.max(bmpS.getWidth(), bmpS.getHeight()) / 2;

		setDiffXY();

		srcRectForBack = new Rect(0, 0, bmpBack[0].getWidth(), bmpBack[0].getHeight());
		srcRectForUnlock = new Rect(0, 0, bmpUnlock.getWidth(), bmpUnlock.getHeight());
		srcRectForBig = new Rect(0, 0, bmpQ.getWidth(), bmpQ.getHeight());
		srcRectForSmall = new Rect(0, 0, bmpS.getWidth(), bmpS.getHeight());

		dstRectForSet = new RectF();
		dstRectForOpt = new RectF();
		dstRectForS = new Rect();
		dstRectForQ = new Rect();
		dstRectForP = new Rect();
		dstRectForE = new Rect();
		dstRectForI = new Rect();
		RectForUnlockPulse = new RectF();
		RectForUnlock = new RectF();

		rotateHand = new Matrix();
		rotateArrow = new Matrix();

		strokeWidth = rUnlock / 10;
		unlockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		unlockPaint.setColor(Color.WHITE);
		unlockPaint.setStyle(Paint.Style.STROKE);
		unlockPaint.setStrokeWidth(strokeWidth);
		for (int i = 0; i < NumAnswers; i++) {
			selectAnswers[i] = false;
			selectOptions[i] = false;
			RectForAnswers[i] = new RectF();
			circlePaint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
			circlePaint[i].setColor(Color.WHITE);
			circlePaint[i].setStyle(Paint.Style.STROKE);
			circlePaint[i].setStrokeWidth(strokeWidth);
			circleTextPaint[i] = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			circleTextPaint[i].setTextAlign(Paint.Align.CENTER);
			circleTextPaint[i].setColor(Color.WHITE);
			circleTextPaint[i].setTextSize(rUnlock * 1.5f);
			layoutAnswers[i] = new StaticLayout(answerTitles[i], circleTextPaint[i], rUnlock * 2, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0,
					false);
		}
		RectForAnswers[5] = new RectF();
		selectUnlock = false;
		selectAppDrag = -1;

		for (int i = 0; i < pulseFrames; i++) {
			sin[i] = Math.sin(Math.PI * 2 * i / pulseFrames);
		}
		listener = new JoystickSelectListener() {
			@Override
			public void OnSelect(int s, boolean vibrate, int Extra) {
			}
		};
		tapTimer = 0;
		correctLoc = 0;
		quickUnlock = false;
		centerOffset = 0;
		d = new ArrayList<Drawable>();
		d.add(drawAdd);
		d.add(drawTrash);
		apps = new ArrayList<App>();
	}

	// =========================================
	// Public Methods
	// =========================================

	public void setOnJostickSelectedListener(JoystickSelectListener listener) {
		this.listener = listener;
	}

	public void setOnTouchedListener(JoystickTouchListener listener) {
		this.listenerTouch = listener;
	}

	public void removeTouchListener() {
		this.listenerTouch = null;
	}

	public void addApp(Drawable icon) {
		if (d.size() == apps.size()) {
			isFirstApp = true;
		}
		icon.setBounds(-icon.getIntrinsicWidth() / 2, -icon.getIntrinsicHeight() / 2, icon.getIntrinsicWidth() / 2,
				icon.getIntrinsicHeight() / 2);
		d.add(d.size() - 1, icon);
		if (d.size() >= 6)
			isFirstApp2 = true;
		invalidate();
	}

	public void removeApp(int loc) {
		d.remove(loc);
		if (isFirstApp) {
			isFirstApp = false;
		}
		if (d.size() >= 6)
			isFirstApp2 = false;
		invalidate();
	}

	public void clearApps() {
		d.clear();
		d.add(drawAdd);
		d.add(drawTrash);
	}

	public void setTypeface(Typeface font) {
		for (int i = 0; i < NumAnswers; i++) {
			circleTextPaint[i].setTypeface(font);
			answerTextPaint[i].setTypeface(font);
			if (equation[i])
				layoutE[i].setTypeface(font);
		}
		textPaintWhite.setTypeface(font);
		optionPaintWhite.setTypeface(font);
		textPaintBlack.setTypeface(font);
	}

	public boolean setQuizMode(boolean quizMode) {
		this.quizMode = quizMode;
		return this.quizMode;
	}

	public void setUnlockType(int type) {
		if (this.type != type) {
			this.type = type;
			setAnswers(answers, correctLoc);
			if (measured) {
				switch (type) {
				case 0:
					break;
				case 1:
					animateHandler.removeCallbacks(spin);
					if (degreeStep == 0) {
						degrees = 0;
						radians = 0;
					} else
						animateHandler.postDelayed(spin, spinFrameTime);
					break;
				case 2:
					break;
				}
			}
		}
	}

	public void showHint(int hint) {
		showHint = true;
		boolean mirror = false;
		int centerVert = 0, centerHorz = 0, handRotation = 0, arrowRotation = 0;
		switch (hint) {
		case 0:
			centerVert = (int) ((RectForUnlock.bottom + RectForUnlock.top) / 2);
			centerHorz = (int) ((RectForUnlock.left + RectForUnlock.right) / 2);
			arrowRotation = 225;
			break;
		case 1:
			setSidePaths(Height - rBig * 2 - pad);
			centerVert = (int) ((dstRectForQ.bottom + dstRectForQ.top) / 2);
			centerHorz = (int) ((dstRectForQ.left + dstRectForQ.right) / 2);
			handRotation = 270;
			break;
		case 2:
			setSidePaths(Height - rBig * 2 - pad);
			centerVert = (int) ((dstRectForS.bottom + dstRectForS.top) / 2);
			centerHorz = (int) ((dstRectForS.left + dstRectForS.right) / 2);
			handRotation = 270;
			break;
		case 3:
			setSidePaths(Height - rBig * 2 - pad);
			centerVert = (int) ((dstRectForP.bottom + dstRectForP.top) / 2);
			centerHorz = (int) ((dstRectForP.left + dstRectForP.right) / 2);
			handRotation = 270;
			break;
		case 4:
			mirror = true;
			setSidePaths(Height - rBig * 2 - pad);
			centerVert = (int) ((dstRectForE.bottom + dstRectForE.top) / 2);
			centerHorz = (int) ((dstRectForE.left + dstRectForE.right) / 2);
			handRotation = 90;
			break;
		case 5:
			mirror = true;
			setSidePaths(Height - rBig * 2 - pad);
			centerVert = (int) ((dstRectForI.bottom + dstRectForI.top) / 2);
			centerHorz = (int) ((dstRectForI.left + dstRectForI.right) / 2);
			handRotation = 90;
			break;
		default:
			setSidePaths(Height - pad);
			showHint = false;
			break;
		}
		rotateHand.reset();
		if (mirror)
			rotateHand.preScale(-1, 1);
		rotateHand.postTranslate(centerHorz, centerVert);
		rotateHand.postRotate(handRotation, centerHorz, centerVert);

		rotateArrow.reset();
		rotateArrow.setTranslate(centerHorz - bmpArrow.getWidth() / 2, centerVert - bmpArrow.getHeight());
		rotateArrow.postRotate(arrowRotation, centerHorz, centerVert);

		invalidate();
	}

	public void showStartAnimation(int start, int delay) {
		if (start == 0) {
			options = true;
			setSidePaths(Height - rBig * 2 - pad);
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

	public void setWrongGuess() {
		this.wrong = true;
		if (type == 1) {
			animateHandler.removeCallbacks(spin);
			degrees = 0;
			radians = 0;
		}
		invalidate();
	}

	public void setDegreeStep(int streak) {
		degreeStep = degreeStepInitial * Math.max(streak, 0);
		if (type == 1) {
			animateHandler.removeCallbacks(spin);
			if (degreeStep == 0) {
				degrees = 0;
				radians = 0;
			} else
				animateHandler.postDelayed(spin, spinFrameTime);
		}
	}

	public void resetWrongGuess() {
		this.wrong = false;
		invalidate();
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
	}

	public void setAnswers(String answers[], int correctLoc) {
		this.answers = new String[] { answers[0], answers[1], answers[2], answers[3], "?" };
		this.correctLoc = correctLoc;
		this.quickUnlock = false;
		centerOffset = 0;
		if (measured) {
			animateHandler.removeCallbacks(pulseLock);
			animateHandler.removeCallbacks(spin);
			switch (type) {
			case 1:
				animateHandler.postDelayed(spin, spinFrameTime);
			case 0:
				resetAnswerSize();
				setDimensions();
				int centerX = Width / 2;
				int bottom = TextHeight - textSizePix;
				int centerY = bottom / 2;
				RectForUnlockPulse.set(centerX - rUnlock, centerY - rUnlock, centerX + rUnlock, centerY + rUnlock);
				RectForUnlock.set(centerX - rUnlock, centerY - rUnlock, centerX + rUnlock, centerY + rUnlock);
				RectForAnswers[0].set(0, 0, Width / 2, centerY - rUnlock);
				RectForAnswers[1].set(Width / 2, 0, Width, centerY - rUnlock);
				RectForAnswers[2].set(0, centerY + rUnlock, Width / 2, bottom);
				RectForAnswers[3].set(Width / 2, centerY + rUnlock, Width, bottom);
				RectForAnswers[4].set(Width / 4 - rUnlock * 2, centerY - rUnlock * 2, Width / 4 + rUnlock * 2, centerY + rUnlock * 2);
				RectForAnswers[5].set(Width * 3 / 4 - rUnlock * 2, centerY - rUnlock * 2, Width * 3 / 4 + rUnlock * 2, centerY + rUnlock
						* 2);
				if (type == 0)
					animateHandler.postDelayed(pulseLock, pulseFrameTime);
				break;
			case 2:
				break;
			}
		}
	}

	public void moveCorrect(int loc) {
		if (correctLoc != loc) {
			String temp = answers[loc];
			answers[loc] = answers[correctLoc];
			answers[correctLoc] = temp;
			correctLoc = loc;
			animateHandler.removeCallbacks(pulseLock);
			animateHandler.removeCallbacks(spin);
			switch (type) {
			case 1:
				setDimensions();
				animateHandler.postDelayed(spin, spinFrameTime);
				break;
			case 0:
				setDimensions();
				animateHandler.postDelayed(pulseLock, pulseFrameTime);
				break;
			case 2:
				break;
			}
		}
	}

	public void startAnimations() {
		animateHandler.removeCallbacksAndMessages(null);
		textHandler.removeCallbacksAndMessages(null);
		answerHandler.removeCallbacksAndMessages(null);
		animateHandler.post(finishAnimate);
		switch (type) {
		case 0:
			animateHandler.postDelayed(pulseLock, pulseFrameTime);
			break;
		case 1:
			animateHandler.postDelayed(spin, spinFrameTime);
			break;
		case 2:
			break;
		}
	}

	public void removeCallbacks() {
		animateHandler.removeCallbacksAndMessages(null);
		textHandler.removeCallbacksAndMessages(null);
		answerHandler.removeCallbacksAndMessages(null);
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		animateHandler.removeCallbacks(pulseLock);
		animateHandler.removeCallbacks(spin);
		switch (type) {
		case 0:
			measured = true;
			setDimensions();
			animateHandler.postDelayed(pulseLock, pulseFrameTime);
			break;
		case 1:
			measured = true;
			setDimensions();
			animateHandler.postDelayed(spin, spinFrameTime);
			break;
		case 2:
			break;
		}
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Width = measure(widthMeasureSpec);
		Height = measure(heightMeasureSpec);

		optionY = Height - rBig;

		spacing = (Width - rBig * 6 - rSmall * 4) / 6;
		for (int i = 0; i < selectLeft.length; i++) {
			selectLeft[i] = Width / 2 + rBig * (Math.max(3 - i * 2, -3)) + spacing * (2 - i);
			selectRight[i] = Width / 2 + rBig * (Math.min(5 - i * 2, 3)) + spacing * (2 - i);
		}
		selectLeft[4] = selectLeft[4] - rSmall * 2;
		selectRight[0] = selectRight[0] + rSmall * 2;

		dstHeight = rBig * 2 + textSizePix;

		if (options)
			setSidePaths(Height - rBig * 2 - pad);
		else
			setSidePaths(Height - pad);

		setMeasuredDimension(Width, Height);
		initRunnables();
		measured = true;
		setUnlockType(type);
	}

	private int measure(int measureSpec) {
		int result = 0;
		// Decode the measurement specifications.
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.UNSPECIFIED) {
			// Return a default size of 480 if no bounds are specified.
			result = 480;
		} else {
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		switch (type) {
		case 1:
			if (!quickUnlock)
				canvas.rotate(-degrees, Width / 2, (TextHeight - textSizePix) / 2);	// intentional fall through
		case 0:
			for (int i = 0; i <= NumAnswers; i++) {
				int pos = Math.min(i, NumAnswers - 1);
				if (!quickUnlock || (i == correctLoc)) {
					canvas.save();
					if (selectAnswers[pos])
						canvas.drawBitmap(bmpBack[0], srcRectForBack, RectForAnswers[i], unlockPaint);
					else if (i == correctGuess)
						canvas.drawBitmap(bmpBack[1], srcRectForBack, RectForAnswers[i], unlockPaint);
					else if (i == wrongGuess)
						canvas.drawBitmap(bmpBack[2], srcRectForBack, RectForAnswers[i], unlockPaint);

					// position the text then draw the layout
					if (equation[pos]) {
						canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2,
								(RectForAnswers[i].top + RectForAnswers[i].bottom) / 2);
						layoutE[pos].draw(canvas);
					} else {
						canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2,
								(RectForAnswers[i].top + RectForAnswers[i].bottom) / 2 - layout[pos].getHeight() / 2);
						layout[pos].draw(canvas);
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
						canvas.save();
						canvas.translate(apps.get(i - start).getX(), apps.get(i - start).getY());
						if (apps.get(i - start).getSelect())
							if (i == d.size() - 1)
								drawBackRed.draw(canvas);
							else
								drawBackBlue.draw(canvas);
						canvas.restore();
					}
				}
				for (int i = start; i < end; i++) {
					if (!apps.get(i - start).getSelectDrag()) {
						canvas.save();
						canvas.translate(apps.get(i - start).getX(), apps.get(i - start).getY());
						d.get(i).draw(canvas);
						canvas.restore();
					}
				}
				if (selectAppDrag >= 0) {
					canvas.save();
					canvas.translate(apps.get(selectAppDrag).getX() + appDragX, apps.get(selectAppDrag).getY() + appDragY);
					d.get(selectAppDrag + start).draw(canvas);
					canvas.restore();
				}
			}
			if ((type == 1) && (!quickUnlock))
				canvas.rotate(degrees, Width / 2, (TextHeight - textSizePix) / 2);
			canvas.drawRoundRect(RectForUnlockPulse, (RectForUnlockPulse.right - RectForUnlockPulse.left) / 2f,
					(RectForUnlockPulse.bottom - RectForUnlockPulse.top) / 2f, unlockPaint);
			canvas.drawBitmap(bmpUnlock, srcRectForUnlock, RectForUnlock, unlockPaint);
			break;
		case 2:
			for (int i = 0; i < numAnswersDisplayed; i++) {
				if (!quickUnlock || (i == correctLoc)) {
					canvas.drawRoundRect(RectForAnswers[i], rApps, rApps, circlePaint[i]);
					canvas.save();
					canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2,
							(RectForAnswers[i].top + RectForAnswers[i].bottom) / 2 - layoutAnswers[i].getHeight() / 2); // position the text
					layoutAnswers[i].draw(canvas);
					canvas.restore();
				}
			}
			if (quickUnlock) {

				int start = 0;
				int end = Math.min(d.size() - 2, 4);
				if (isFirstApp2)
					start = 1;
				for (int i = start; i <= end; i++) {
					int selection = i;
					if (i >= correctLoc)
						selection += 1;
					canvas.save();
					canvas.translate((float) X[selection - start], (float) Y[selection - start]);
					if (apps.get(i - start).getSelect())
						drawBackBlue.draw(canvas);
					canvas.restore();
				}
				for (int i = start; i <= end; i++) {
					int selection = i;
					if (i >= correctLoc)
						selection += 1;
					canvas.save();
					canvas.translate((float) X[selection - start], (float) Y[selection - start]);
					d.get(i).draw(canvas);
					canvas.restore();

				}
			}
			if ((selectOptions[0]) || (selectOptions[1]) || (selectOptions[2]) || (selectOptions[3]) || (selectOptions[4]))
				canvas.drawText(res.getString(R.string.swipe_option), Width / 2, (Height - rBig * 2) / 2, textPaintWhite);
			else if (problem)
				if (!wrong)
					canvas.drawText(res.getString(R.string.swipe_screen), Width / 2, (Height - rBig * 2) / 2, textPaintWhite);
				else
					canvas.drawText(res.getString(R.string.swipe_new), Width / 2, (Height - rBig * 2) / 2, textPaintWhite);
			else
				canvas.drawText(res.getString(R.string.swipe_exit), Width / 2, (Height - rBig * 2) / 2, textPaintWhite);
			break;
		}

		// Draw the option bar
		canvas.drawRoundRect(dstRectForSet, textSizePix, textSizePix, settingsPaint);
		if ((selectOptions[0]) || (selectOptions[1]) || (selectOptions[2]) || (selectOptions[3]) || (selectOptions[4])) {
			canvas.drawRect(dstRectForOpt, optPaint);// darken the text behind settings stuff
			canvas.drawTextOnPath(res.getString(R.string.swipe_here), optionPath, 0, 0, optionPaintWhite);
			for (int i = 0; i < selectOptions.length; i++) {
				if (selectOptions[i]) {
					int id = res.getIdentifier("option" + i, "string", ctx.getPackageName());
					canvas.drawTextOnPath(res.getString(id), optionPath, 0, -optionPaintWhite.getTextSize(), optionPaintWhite);
				}
			}
		}
		canvas.drawText(res.getString(R.string.side_bar), Width / 2, TextHeight, textPaintBlack);
		canvas.drawBitmap(bmpS, srcRectForSmall, dstRectForS, settingsPaint);
		if (quizMode)
			canvas.drawBitmap(bmpQs, srcRectForBig, dstRectForQ, settingsPaint);
		else
			canvas.drawBitmap(bmpQ, srcRectForBig, dstRectForQ, settingsPaint);
		canvas.drawBitmap(bmpP, srcRectForBig, dstRectForP, settingsPaint);
		canvas.drawBitmap(bmpStore, srcRectForBig, dstRectForE, settingsPaint);
		canvas.drawBitmap(bmpI, srcRectForSmall, dstRectForI, settingsPaint);

		// Draw hints
		if (showHint) {
			canvas.drawBitmap(bmpArrow, rotateArrow, settingsPaint);
			canvas.drawBitmap(bmpHand, rotateHand, settingsPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int actionType = event.getAction();
		if (!paused) {
			if (actionType == MotionEvent.ACTION_DOWN) {
				textHandler.removeCallbacks(revealText);
				textHandler.removeCallbacks(finishText);
				textPaintWhite.setAlpha(0);
				invalidate();
				startX = event.getX();
				startY = event.getY();
				if ((startY >= Height - rBig * 2) && (options)) {	// Options selected
					animateHandler.removeCallbacks(startAnimate);
					animateHandler.removeCallbacks(finishAnimate);
					touchX = startX;
					touchY = startY;
					if ((startX >= selectLeft[0]) && (startX <= selectRight[0])) {			// info selected
						selectOptions[0] = true;
						optionX = selectLeft[0] + rSmall;
						setArc(180, 150);
					} else if ((startX >= selectLeft[1]) && (startX <= selectRight[1])) {	// store selected
						selectOptions[1] = true;
						optionX = selectLeft[1] + rBig;
						setArc(180, 180);
					} else if ((startX >= selectLeft[2]) && (startX <= selectRight[2])) {	// progress selected
						selectOptions[2] = true;
						optionX = selectLeft[2] + rBig;
						setArc(180, 180);
					} else if ((startX >= selectLeft[3]) && (startX <= selectRight[3])) {	// quiz mode selected
						selectOptions[3] = true;
						optionX = selectLeft[3] + rBig;
						setArc(180, 180);
					} else if ((startX >= selectLeft[4]) && (startX <= selectRight[4])) {	// settings selected
						selectOptions[4] = true;
						optionX = selectLeft[4] + rSmall;
						setArc(210, 150);
					} else
						return true;
					flashText();
					if (listener != null)
						listener.OnSelect(-1, true, -1);				// send a vibrate signal
				} else if (startY >= TextHeight - textSizePix * 2) {	// SettingsBar selected
					animateHandler.removeCallbacks(startAnimate);
					animateHandler.removeCallbacks(finishAnimate);
					selectSideBar = true;
					invalidate();
				} else if (!problem) {						// if there is no problem set
					if (listener != null)
						listener.OnSelect(0, true, -1);		// select A on any touch event
				} else {									// touch was in the main control window
					switch (type) {
					case 0:
						checkSelection(false, true);
						break;
					case 1:
						checkSelection(false, true);
						break;
					case 2:
						if (((tapTimer + tapLength) > System.currentTimeMillis()) && (!quickUnlock)) {
							quickUnlock = true;
							centerOffset = drawBackBlue.getIntrinsicHeight() / 3;
							listener.OnSelect(11, false, -1);		// send a quickUnlock mode activated signal back to mainActivity
						}
						setAnswerLocations();
						revealAnswers();
						if (listener != null)
							listener.OnSelect(-1, true, -1);		// send a vibrate signal
						break;
					}
				}
				switch (type) {
				case 0:
				case 1:
					setHintDimensions();
					break;
				case 2:
					invalidate();
					break;
				}
			} else if (actionType == MotionEvent.ACTION_MOVE) {
				touchX = event.getX();
				touchY = event.getY();
				checkSelection(false, false);
				invalidate();
			} else if (actionType == MotionEvent.ACTION_UP) {
				tapTimer = System.currentTimeMillis();
				checkSelection(true, false);
				returnToDefault();
				flashText();
			}

		} else if (actionType == MotionEvent.ACTION_UP) {
			if (listenerTouch != null)
				listenerTouch.OnTouch();
		}
		return true;
	}

	// =========================================
	// Setup and Initialization Algorithms
	// =========================================

	private void setAppCenters(boolean sel) {
		int centerVert = (TextHeight - textSizePix + drawBackBlue.getIntrinsicHeight() / 3) / 2;
		int centerHorz = Width / 2;
		int rApps = drawAdd.getIntrinsicHeight() / 2;
		int rAppsY = centerVert - rApps - drawBackBlue.getIntrinsicHeight() / 3 - pad;
		int rAppsX = centerHorz - rApps - pad;
		appAngle = Math.atan2(rApps * 3, Math.min(rAppsY, rAppsX));
		int oldMaxApps = apps.size();
		int maxApps = (int) Math.floor((3 * Math.PI / 2) / appAngle);
		if (oldMaxApps < maxApps) {
			apps.clear();
			for (int i = 0; i < maxApps; i++) {
				double angle = Math.PI - appAngle / 2 - appAngle * i;
				apps.add(new App(angle, (float) (centerHorz + Math.cos(angle) * rAppsX), (float) (centerVert - Math.sin(angle) * rAppsY),
						rApps));
			}
		} else {
			for (int i = 0; i < apps.size(); i++) {
				double angle = Math.PI - appAngle / 2 - appAngle * i;
				apps.get(i).setAll(angle, (float) (centerHorz + Math.cos(angle) * rAppsX), (float) (centerVert - Math.sin(angle) * rAppsY),
						rApps);
				if (!sel) {
					apps.get(i).setSelect(false);
					apps.get(i).setSelectDrag(false);
				}
			}
		}
	}

	private void setAnswerLocations() {
		switch (type) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			int maxDiff = 2;
			int diff = 0;
			int attempts = 0;
			int startingState = state;
			while ((maxDiff >= 2) && (attempts < 3)) {
				maxDiff = 0;
				state = (startingState + attempts) % 4;
				attempts += 1;
				setDiffXY();

				X[0] = startX + diffX1;
				Y[0] = startY + diffY1;
				diff = checkDiff(0);

				if (diff == 0) {
					for (int i = 1; i < X.length; i++) {
						X[i] = X[i - 1] + diffX;
						Y[i] = Y[i - 1] + diffY;
						diff = checkDiff(i);
						if (diff > maxDiff)
							maxDiff = diff;
					}
					if (checkOnTop())
						maxDiff = 2;
				} else
					maxDiff = 2;
			}
			if ((maxDiff >= 2) && (attempts == 3)) {
				int centerX;
				int centerY = rApps * 2;
				if (startX <= Width / 2) {
					centerX = Width - (int) (rApps * (Math.sqrt(3) + 1));
					X[4] = centerX + (int) (rApps * Math.sqrt(3));
					X[0] = centerX - (int) (rApps * Math.sqrt(3));
				} else {
					centerX = (int) (rApps * (Math.sqrt(3) + 1));
					X[4] = centerX - (int) (rApps * Math.sqrt(3));
					X[0] = centerX + (int) (rApps * Math.sqrt(3));
				}
				Y[0] = centerY;
				X[1] = centerX;
				Y[1] = centerY - rApps;
				X[2] = X[4];
				Y[2] = centerY;
				X[3] = centerX;
				Y[3] = centerY + rApps;
				Y[4] = centerY + rApps * 2;
			}
			state = startingState;
			for (int i = 0; i < X.length; i++) {
				RectForAnswers[i].set((int) X[i] - rApps + strokeWidth / 2, (int) Y[i] - rApps + strokeWidth / 2, (int) X[i] + rApps
						- strokeWidth / 2, (int) Y[i] + rApps - strokeWidth / 2);
			}
			break;
		}
	}

	private boolean checkOnTop() {
		for (int a = 0; a < X.length - 1; a++) {
			for (int b = a + 1; b < X.length; b++) {
				if (a != b)
					if ((X[a] == X[b]) && (Y[a] == Y[b]))
						return true;
			}
		}
		return false;
	}

	private void setDiffXY() {
		// sets the direction to display the answers in
		switch (state) {
		case 0:		// up-right
			diffX = (int) (rApps * Math.sqrt(3));
			diffY = -rApps;
			break;
		case 1:		// up-left
			diffX = (int) (-rApps * Math.sqrt(3));
			diffY = -rApps;
			break;
		case 2:		// down-left
			diffX = (int) (-rApps * Math.sqrt(3));
			diffY = rApps;
			break;
		case 3:		// down-right
			diffX = (int) (rApps * Math.sqrt(3));
			diffY = rApps;
			break;
		}
		diffX1 = diffX * 2;
		diffY1 = diffY * 2;
	}

	private int checkDiff(int loc) {
		int checks = 0;
		boolean first = (loc == 0);
		switch (state) {
		case 0:		// up-right
			if (Y[loc] - rApps < 0) {				// above top boundary
				checks += 1;
				state = ((state - 1) % 4 + 4) % 4;	// state-> 3=down-right
			}
			if (X[loc] + rApps > Width) {			// right of right boundary
				checks += 1;
				state = (state + checks) % 4;		// state-> 1=up-left or 2=down-left
			}
			break;
		case 1:		// up-left
			if (Y[loc] - rApps < 0) {		// above top boundary
				checks += 1;
				state = (state + 1) % 4;
			}
			if (X[loc] - rApps < 0) {		// left of left boundary
				checks += 1;
				state = ((state - checks) % 4 + 4) % 4;
			}
			break;
		case 2:		// down-left
			if (Y[loc] + rApps > (Height - rBig * 2)) {		// below bottom boundary
				checks += 1;
				state = ((state - 1) % 4 + 4) % 4;
			}
			if (X[loc] - rApps < 0) {		// left of left boundary
				checks += 1;
				state = (state + checks) % 4;
			}
			break;
		case 3:		// down-right
			if (Y[loc] + rApps > (Height - rBig * 2)) {		// below bottom boundary
				checks += 1;
				state = (state + 1) % 4;
			}
			if (X[loc] + rApps > Width) {		// right of right boundary
				checks += 1;
				state = ((state - checks) % 4 + 4) % 4;
			}
			break;
		}
		setDiffXY();
		if (first) {
			X[loc] = startX + diffX1;
			Y[loc] = startY + diffY1;
		} else {
			X[loc] = X[loc - 1] + diffX;
			Y[loc] = Y[loc - 1] + diffY;
		}
		return checks;			// # of walls hit
	}

	private void revealAnswers() {
		numAnswersDisplayed = 0;
		for (int ans = 0; ans < X.length; ans++) {
			answerHandler.postDelayed(revealAnswers, answerFrameTime * (ans + 1));
		}
	}

	private void returnToDefault() {
		touchX = 0;
		touchY = 0;
		startX = -rUnlock;
		startY = -rUnlock;
		for (int ans = 0; ans < X.length; ans++) {
			circlePaint[ans].setColor(Color.WHITE);
			circleTextPaint[ans].setColor(Color.WHITE);
			selectAnswers[ans] = false;
			selectOptions[ans] = false;
		}
		for (int i = 0; i < apps.size(); i++) {
			apps.get(i).setSelect(false);
			apps.get(i).setSelectDrag(false);
			appDragX = 0;
			appDragY = 0;
		}
		selectUnlock = false;
		selectAppDrag = -1;

		if (options)
			showStartAnimation(0, 3000);
		else if (selectSideBar)
			showStartAnimation(1, 0);
		else {
			int currentRadius = (int) ((RectForUnlockPulse.right - RectForUnlockPulse.left) / 2);
			RectForUnlockPulse.set(Width / 2 - currentRadius, (TextHeight - textSizePix + centerOffset) / 2 - currentRadius, Width / 2
					+ currentRadius, (TextHeight - textSizePix + centerOffset) / 2 + currentRadius);
			RectForUnlock.set(Width / 2 - rUnlock, (TextHeight - textSizePix + centerOffset) / 2 - rUnlock, Width / 2 + rUnlock,
					(TextHeight - textSizePix + centerOffset) / 2 + rUnlock);
		}
		selectSideBar = false;

		animateHandler.removeCallbacks(pulseLock);
		for (int ans = 0; ans < X.length; ans++) {
			answerHandler.removeCallbacks(revealAnswers);
			X[ans] = -rApps;
			Y[ans] = -rApps;
			numAnswersDisplayed = 0;
		}
		switch (type) {
		case 0:
			setHintDimensions();
			animateHandler.postDelayed(pulseLock, pulseFrameTime);
			break;
		case 1:
			setHintDimensions();
			break;
		case 2:
			invalidate();
			break;
		}

	}

	private void checkSelection(boolean send, boolean firstTouch) {
		double diffx, diffy;
		int s = -1;
		if ((selectOptions[0]) || (selectOptions[1]) || (selectOptions[2]) || (selectOptions[3]) || (selectOptions[4]) || (selectSideBar)) {
			diffx = touchX - startX;
			diffy = touchY - startY;
			if (selectOptions[0]) {
				s = 5;
				dstRectForI.set((int) touchX - rSmall, (int) touchY - rSmall, (int) touchX + rSmall, (int) touchY + rSmall);
			} else if (selectOptions[1]) {
				s = 6;
				dstRectForE.set((int) touchX - rBig, (int) touchY - rBig, (int) touchX + rBig, (int) touchY + rBig);
			} else if (selectOptions[2]) {
				s = 7;
				dstRectForP.set((int) touchX - rBig, (int) touchY - rBig, (int) touchX + rBig, (int) touchY + rBig);
			} else if (selectOptions[3]) {
				s = 8;
				dstRectForQ.set((int) touchX - rBig, (int) touchY - rBig, (int) touchX + rBig, (int) touchY + rBig);
			} else if (selectOptions[4]) {
				s = 9;
				dstRectForS.set((int) touchX - rSmall, (int) touchY - rSmall, (int) touchX + rSmall, (int) touchY + rSmall);
			} else if (selectSideBar) {
				s = 10;
				setSidePaths((int) Math.max(touchY, Height - rBig * 2 - pad));
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
		} else
			switch (type) {
			case 0:
			case 1:
				for (int i = 0; i < NumAnswers; i++) {
					circlePaint[i].setColor(Color.WHITE);
					circleTextPaint[i].setColor(Color.WHITE);
					selectAnswers[i] = false;
				}
				for (int i = 0; i < apps.size(); i++) {
					apps.get(i).setSelect(false);
				}
				if (selectUnlock) {
					diffx = touchX - startX;
					diffy = touchY - startY;
					double newX = touchX, newY = touchY;
					if ((type == 1) && (!quickUnlock)) {
						double oldX = touchX - Width / 2;
						double oldY = (TextHeight - textSizePix) / 2 - touchY;
						newX = (oldX * Math.cos(-radians) - oldY * Math.sin(-radians)) + Width / 2;
						newY = (TextHeight - textSizePix) / 2 - (oldX * Math.sin(-radians) + oldY * Math.cos(-radians));
					}
					RectForUnlockPulse.set((int) touchX - rUnlock, (int) touchY - rUnlock, (int) touchX + rUnlock, (int) touchY + rUnlock);
					RectForUnlock.set((int) touchX - rUnlock, (int) touchY - rUnlock, (int) touchX + rUnlock, (int) touchY + rUnlock);

					if (Math.sqrt(diffx * diffx + diffy * diffy) > swipeLength1) {
						int select = -1;

						if (newX < RectForAnswers[0].right) {
							if (newY < RectForAnswers[0].bottom)
								select = 0;
							else if (newY > RectForAnswers[2].top)
								select = 2;
							else
								select = 4;
						} else if (newX > RectForAnswers[1].left) {
							if (newY < RectForAnswers[1].bottom)
								select = 1;
							else if (newY > RectForAnswers[3].top)
								select = 3;
							else
								select = 4;
						}

						if (quickUnlock) {
							if (select == correctLoc) {
								selectAnswers[select] = true;
								if (send)
									listener.OnSelect(select, true, -1);
							} else {
								double angle = Math.atan2((TextHeight - textSizePix) / 2 - newY, newX - Width / 2);
								if (angle < 0)// because atan2 returns from -pi to pi, we want 0 to 2pi
									angle += 2 * Math.PI;
								double minAngle = appAngle;
								int selection = -1;
								for (int i = 0; i < apps.size(); i++) {
									double angleDiff = Math.abs(apps.get(i).getAngle() - angle);
									if ((angleDiff < minAngle) || (Math.abs(angleDiff - Math.PI * 2) < minAngle)) {
										minAngle = Math.min(angleDiff, Math.abs(angleDiff - Math.PI * 2));
										selection = i;
									}
								}
								if (selection >= 0)
									if (send)
										if (selection == 0)
											if (isFirstApp)
												listener.OnSelect(13, true, selection);
											else
												listener.OnSelect(12, true, -1);
										else if (isFirstApp)
											listener.OnSelect(13, true, selection);
										else
											listener.OnSelect(13, true, selection - 1);
									else
										apps.get(selection).setSelect(true);
							}
						} else if (select >= 0) {
							if (send)
								listener.OnSelect(select, true, 0);
							else {
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
								listener.OnSelect(14, true, selectAppDrag);
								removeApp(selectAppDrag + 1);
							} else {
								listener.OnSelect(14, true, selectAppDrag - 1);
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
							quickUnlock = true;
							centerOffset = drawBackBlue.getIntrinsicHeight() / 3;
							listener.OnSelect(11, false, -1);		// send a quickUnlock mode activated signal back to mainActivity
						}
						startX = (RectForUnlock.right + RectForUnlock.left) / 2;
						startY = (RectForUnlock.top + RectForUnlock.bottom) / 2;
						RectForUnlockPulse.set((int) touchX - rUnlock, (int) touchY - rUnlock, (int) touchX + rUnlock, (int) touchY
								+ rUnlock);
						RectForUnlock.set((int) touchX - rUnlock, (int) touchY - rUnlock, (int) touchX + rUnlock, (int) touchY + rUnlock);

						selectUnlock = true;
						animateHandler.removeCallbacks(pulseLock);
						if (listener != null)
							listener.OnSelect(-1, true, -1);		// send a vibrate signal
					} else if (firstTouch) {
						if (quickUnlock) {
							int offset = 1;
							if (isFirstApp)
								offset = 0;
							int end = Math.min(d.size() - 1, apps.size() - 1);
							for (int i = offset; i < end; i++) {
								if ((touchX < apps.get(i).getRight()) && (touchX > apps.get(i).getLeft())
										&& (touchY < apps.get(i).getBottom()) && (touchY > apps.get(i).getTop())) {
									apps.get(i).setSelectDrag(true);
									selectAppDrag = i;
									appDragX = (float) touchX - apps.get(i).getX();
									appDragY = (float) touchY - apps.get(i).getY();
								}
							}
						} else
							listener.OnSelect(10, false, -1);		// send a missed the lock signal
					}
				}
				break;
			case 2:
				for (int i = 0; i < X.length; i++) {
					diffx = touchX - X[i];
					diffy = touchY - Y[i];
					int selection = i;
					if (i > correctLoc)
						selection -= 1;
					if (Math.sqrt(diffx * diffx + diffy * diffy) < rApps) {
						if (quickUnlock) {
							if (i == correctLoc) {
								if (send) {
									circlePaint[i].setColor(Color.WHITE);
									circleTextPaint[i].setColor(Color.WHITE);
									selectAnswers[i] = false;
									listener.OnSelect(i, true, -1);
								} else {
									circlePaint[i].setColor(Color.BLUE);
									circleTextPaint[i].setColor(Color.BLUE);
									selectAnswers[i] = true;
								}
							} else {
								if (send)
									if (selection == 0)
										if (isFirstApp2)
											listener.OnSelect(13, true, selection);
										else
											listener.OnSelect(12, true, -1);
									else if (isFirstApp2)
										listener.OnSelect(13, true, selection);
									else
										listener.OnSelect(13, true, selection - 1);
								else
									apps.get(selection).setSelect(true);
							}
						} else if (send) {
							circlePaint[i].setColor(Color.WHITE);
							circleTextPaint[i].setColor(Color.WHITE);
							selectAnswers[i] = false;
							listener.OnSelect(i, true, -1);
						} else {
							circlePaint[i].setColor(Color.BLUE);
							circleTextPaint[i].setColor(Color.BLUE);
							selectAnswers[i] = true;
						}
					} else {
						circlePaint[i].setColor(Color.WHITE);
						circleTextPaint[i].setColor(Color.WHITE);
						selectAnswers[i] = false;
						apps.get(selection).setSelect(false);
					}
				}
				break;
			}
	}

	private void flashText() {
		textHandler.removeCallbacks(revealText);
		textHandler.removeCallbacks(finishText);

		textPaintWhite.setAlpha(0);

		for (int i = 0; i < textFrames; i++) {
			textHandler.postDelayed(revealText, i * textFrameTime);
		}
		textHandler.postDelayed(finishText, textFrames * textFrameTime);
	}

	private void initRunnables() {
		final int textInterval = 255 / textFrames;
		final int slideInterval = (dstHeight - pad) / startFrames;

		startAnimate = new Runnable() {
			@Override
			public void run() {
				options = false;
				if (TextHeight + slideInterval > Height - pad) {
					setSidePaths(Height - pad);
				} else {
					setSidePaths(TextHeight + slideInterval);
				}
				invalidate();
			}
		};
		finishAnimate = new Runnable() {
			@Override
			public void run() {
				setSidePaths(Height - pad);
				options = false;
				invalidate();
			}
		};

		revealAnswers = new Runnable() {
			@Override
			public void run() {
				numAnswersDisplayed++;
				invalidate();
			}
		};

		revealText = new Runnable() {
			@Override
			public void run() {
				textPaintWhite.setAlpha(textPaintWhite.getAlpha() + textInterval);
				invalidate();
			}
		};
		finishText = new Runnable() {
			@Override
			public void run() {
				textPaintWhite.setAlpha(255);
				invalidate();
			}
		};
		pulseLock = new Runnable() {
			@Override
			public void run() {
				if (!selectUnlock) {
					int size = (int) ((RectForUnlockPulse.right - RectForUnlockPulse.left) / 2);
					int oldPulseFrame = pulseFrame;
					pulseFrame = (int) (System.currentTimeMillis() % (pulseFrameTime * pulseFrames) / pulseFrameTime);
					if (pulseFrame < oldPulseFrame) {
						pulseFrame = 0;
						animateHandler.removeCallbacks(pulseLock);
						animateHandler.postDelayed(pulseLock, pulseFrameTime * pulseFrames);
					} else {
						int change = (int) (rUnlockChange * sin[pulseFrame] + rUnlock - size);
						RectForUnlockPulse.set(RectForUnlockPulse.left - change, RectForUnlockPulse.top - change, RectForUnlockPulse.right
								+ change, RectForUnlockPulse.bottom + change);
						invalidate();
						animateHandler.removeCallbacks(pulseLock);
						animateHandler.postDelayed(pulseLock, pulseFrameTime);
					}
				}
			}
		};
		spin = new Runnable() {
			@Override
			public void run() {
				long dT = System.currentTimeMillis() - lastTime;
				lastTime += dT;
				degrees = (degrees + degreeStep * dT / spinFrameTime) % 360;
				radians = (float) (degrees * Math.PI / 180);
				checkSelection(false, false);
				invalidate();
				animateHandler.postDelayed(spin, spinFrameTime);
			}
		};
	}

	private void setSidePaths(int side) {
		TextHeight = side;
		int middle = (TextHeight - textSizePix + centerOffset) / 2;
		int bottom = TextHeight - textSizePix;
		switch (type) {
		case 0:
		case 1:
			RectForAnswers[0].set(0, 0, Width / 2, middle - rUnlock);
			RectForAnswers[1].set(Width / 2, 0, Width, middle - rUnlock);
			RectForAnswers[2].set(0, middle + rUnlock, Width / 2, bottom);
			RectForAnswers[3].set(Width / 2, middle + rUnlock, Width, bottom);
			RectForAnswers[4].set(Width / 4 - rUnlock * 2, middle - rUnlock * 2, Width / 4 + rUnlock * 2, middle + rUnlock * 2);
			RectForAnswers[5].set(Width * 3 / 4 - rUnlock * 2, middle - rUnlock * 2, Width * 3 / 4 + rUnlock * 2, middle + rUnlock * 2);
			break;
		case 2:
			break;
		}
		dstRectForOpt.set(0, TextHeight - textSizePix, Width, optionY - swipeLengthOption * 3);
		dstRectForSet.set(0, TextHeight - textSizePix, Width, TextHeight - textSizePix + dstHeight);
		settingsPaint.setShader(new LinearGradient(0, TextHeight - textSizePix, 0, TextHeight - textSizePix + dstHeight, Color.WHITE,
				Color.TRANSPARENT, TileMode.MIRROR));
		optPaint.setShader(new LinearGradient(0, TextHeight - textSizePix, 0, optionY - swipeLengthOption * 3, Color.BLACK,
				Color.TRANSPARENT, TileMode.MIRROR));
		int temp = side + rBig * 2 + pad;
		dstRectForS.set(selectLeft[4], temp - rBig - rSmall, selectRight[4], temp - rBig + rSmall);
		dstRectForQ.set(selectLeft[3], temp - rBig * 2, selectRight[3], temp);
		dstRectForP.set(selectLeft[2], temp - rBig * 2, selectRight[2], temp);
		dstRectForE.set(selectLeft[1], temp - rBig * 2, selectRight[1], temp);
		dstRectForI.set(selectLeft[0], temp - rBig - rSmall, selectRight[0], temp - rBig + rSmall);
		if (!selectUnlock) {
			int currentRadius = (int) ((RectForUnlockPulse.right - RectForUnlockPulse.left) / 2);
			if (currentRadius <= 0)
				currentRadius = rUnlock;
			RectForUnlockPulse.set(Width / 2 - currentRadius, middle - currentRadius, Width / 2 + currentRadius, middle + currentRadius);
			RectForUnlock.set(Width / 2 - rUnlock, middle - rUnlock, Width / 2 + rUnlock, middle + rUnlock);
		}
		setAppCenters(selectAppDrag >= 0);
	}

	private void setHintDimensions() {
		setHintLayouts();
		float maxH = 0;
		maxH = Math.max(bounds[NumAnswers - 1].height(), layout[NumAnswers - 1].getHeight());
		maxH = Math.max(maxH, answerHintSizePix);
		if ((maxH > (rUnlock * 2 - pad * 2)) && (Height > 0)) {
			decreaseHintSize();
			setHintDimensions();
			return;
		} else if (isLayoutSplittingWords(answers[NumAnswers - 1], layout[NumAnswers - 1])) {
			decreaseHintSize();
			setHintDimensions();
			return;
		}
		answerTextPaint[NumAnswers - 1].setTextSize(answerHintSizePix);
		invalidate();
	}

	private void setDimensions() {
		setLayouts();
		float maxH = 0;
		for (int i = 0; i < NumAnswers - 1; i++) {
			if (!equation[i]) {
				maxH = Math.max(bounds[i].height(), layout[i].getHeight());
				maxH = Math.max(maxH, answerSizePix);
				if ((maxH > ((TextHeight - textSizePix) / 2 - pad * 3 - rUnlock)) && (Height > 0)) {
					decreaseAnswerSize();
					setDimensions();
					return;
				} else if (isLayoutSplittingWords(answers[i], layout[i])) {
					decreaseAnswerSize();
					setDimensions();
					return;
				}
			} else {
				if (layoutE[i].getTextSizePix() > answerSizePix) {
					layoutE[i].setTextSize(answerSizeSP, answerSizePix);
				}
			}
		}
		setHintDimensions();

		for (int i = 0; i < NumAnswers - 1; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
		answerTextPaint[NumAnswers - 1].setTextSize(answerHintSizePix);
		invalidate();
	}

	private void setHintLayouts() {
		String str = "?";
		if ((selectOptions[0]) || (selectOptions[1]) || (selectOptions[2]) || (selectOptions[3]) || (selectOptions[4])) {
			removePulseAnimation();
			str = res.getString(R.string.swipe_option);
		} else if (selectUnlock) {
			removePulseAnimation();
			str = "?";
		} else if (problem) {
			if (wrong) {
				removePulseAnimation();
				str = res.getString(R.string.swipe_new);
			} else {
				str = res.getString(R.string.swipe_unlock);
			}
		} else {
			removePulseAnimation();
			str = res.getString(R.string.swipe_exit);
		}
		if (!answers[NumAnswers - 1].equals(str)) {
			resetHintSize();
			answers[NumAnswers - 1] = str;
		}
		answerTextPaint[NumAnswers - 1].getTextBounds(answers[NumAnswers - 1], 0, answers[NumAnswers - 1].length(), bounds[NumAnswers - 1]);
		layout[NumAnswers - 1] = new StaticLayout(answers[NumAnswers - 1], answerTextPaint[NumAnswers - 1], Width / 2 - pad * 2
				- (rUnlock + rUnlockChange) * 2, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
		invalidate();
	}

	private void setLayouts() {
		for (int i = 0; i < NumAnswers - 1; i++) {
			equation[i] = false;
			if (answers[i].charAt(0) == '$')
				if (answers[i].length() > 1)
					if (answers[i].charAt(1) != '$')
						equation[i] = true;
			answerTextPaint[i].getTextBounds(answers[i], 0, answers[i].length(), bounds[i]);
			if (equation[i]) {
				int W = Width / 2 - pad * 2;
				int H = (Height - rBig * 2 - pad - textSizePix) / 2 - pad * 3 - rUnlock;
				if (!layoutE[i].isComputed(answers[i], W, H)) {
					layoutE[i] = new EquationLayout(answers[i], W, H, answerTextPaint[i], answerSizeSP);
					if (layoutE[i].getTextSizePix() < answerSizePix)
						changeAnswerSize(layoutE[i].getTextSizeSP(), layoutE[i].getTextSizePix());
				}
			} else
				layout[i] = new StaticLayout(answers[i], answerTextPaint[i], Width / 2 - pad * 2, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0,
						false);
		}
		invalidate();
	}

	private boolean isLayoutSplittingWords(String string, StaticLayout layout) {
		for (int line = 0; line < layout.getLineCount() - 1; line++) {
			if (string.charAt(layout.getLineEnd(line) - 1) != ' ')
				return true;
		}
		return false;
	}

	private void decreaseAnswerSize() {
		answerSizeSP = Math.max(answerSizeSP - 1, 1);
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, res.getDisplayMetrics());
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
	}

	private void changeAnswerSize(int SP, float Pix) {
		answerSizeSP = SP;
		answerSizePix = Pix;
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
	}

	private void decreaseHintSize() {
		answerHintSizeSP = Math.max(answerHintSizeSP - 1, 1);
		answerHintSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerHintSizeSP, res.getDisplayMetrics());
		answerTextPaint[NumAnswers - 1].setTextSize(answerHintSizePix);
	}

	private void resetAnswerSize() {
		answerSizeSP = answerSizeSPDefault;
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, res.getDisplayMetrics());
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
	}

	private void resetHintSize() {
		answerHintSizeSP = answerHintSizeSPDefault;
		answerHintSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerHintSizeSP, res.getDisplayMetrics());
		answerTextPaint[NumAnswers - 1].setTextSize(answerHintSizePix);
	}

	private void removePulseAnimation() {
		animateHandler.removeCallbacks(pulseLock);
		int centerX = (int) touchX;
		int centerY = (int) touchY;
		if (!selectUnlock) {
			centerX = Width / 2;
			centerY = (TextHeight - textSizePix + centerOffset) / 2;
		}
		RectForUnlockPulse.set(centerX - rUnlock, centerY - rUnlock, centerX + rUnlock, centerY + rUnlock);
	}

	private void setArc(int startDeg, int totalDeg) {
		optionPath.reset();
		optionPath.arcTo(new RectF(optionX - swipeLengthOption, optionY - swipeLengthOption, optionX + swipeLengthOption, optionY
				+ swipeLengthOption), startDeg, totalDeg);
	}
}