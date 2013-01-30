package com.olyware.mathlock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class JoystickView extends View {
	private final int NumAnswers = 5;
	// private Bitmap bmp, bmp_handle;
	// private Bitmap bmp_silent, bmp_sound, bmp_quizMode, bmp_set, bmp_em, bmp_sil, bmp_A, bmp_B, bmp_C, bmp_D;
	// private Bitmap bmp_select;
	private Bitmap bmpA, bmpB, bmpC, bmpD, bmpU, bmpSet, bmpS, bmpQ, bmpP, bmpStore, bmpSil, bmpSnd;
	private Bitmap bmpAs, bmpBs, bmpCs, bmpDs, bmpUs, bmpQs;
	// private Rect dstRectForRender, dstRectForHandle;
	// private Rect srcRectForRender, srcRectForHandle;
	private Rect dstRectForA, dstRectForB, dstRectForC, dstRectForD, dstRectForU;
	private Rect dstRectForSet, dstRectForS, dstRectForQ, dstRectForP, dstRectForE, dstRectForSnd;
	private Rect srcRectForAns, srcRectForBig, srcRectForSmall, srcRectForSet;
	private Paint circlePaint[] = new Paint[NumAnswers];
	private Paint textPaint, sidePaint, optionPaint;
	private int textSizeSP, textSizePix;
	private double touchX, touchY;
	private double startX, startY;
	private float optionX, optionY, optionR;
	private int Width, Height, dstHeight, pad = 10;
	private double X[] = new double[NumAnswers];		// a=0,b=1,c=2,d=3
	private double Y[] = new double[NumAnswers];
	private int type = 0;
	private int diffX1, diffY1, diffX, diffY;
	// direction answers are going (0=up-right, 1=up-left, 2=down-left, 3=down-right)
	private int state = 0;
	// private int handleRadius;
	// private double thetaMax, rMax, fX, fY; // for maximum area of a rectangle in an ellipse
	// private double rX, rY, rCurrent;
	private int spacing, rAns, rBig, rSmall, swipeLength;
	private int TextHeight;
	private JoystickSelectListener listener;
	private JoystickTouchListener listenerTouch;

	private boolean settingsMode;
	private boolean quizMode;
	private boolean emergencyMode;
	private boolean silentMode;
	private boolean LtrueRfalse;
	private boolean selectAnswers[] = new boolean[NumAnswers];
	private boolean selectOptions[] = new boolean[5];
	private boolean options = true, selectSideBar = false;
	private boolean problem = true, wrong = false, paused = false;
	private int selectLeft[] = new int[5];
	private int selectRight[] = new int[5];

	// private double pi = Math.PI;

	// private AngleSelect angles[] = new AngleSelect[8];

	private Resources res;
	private Runnable revealText, finishText, startAnimate, finishAnimate, revealCircle, finishCircle;
	private Runnable revealAnswer[] = new Runnable[NumAnswers];
	private Runnable finishAnswer[] = new Runnable[NumAnswers];
	private Handler answerHandler, textHandler, animateHandler;
	private final int textFrames = 10, textFrameTime = 50, answerFrames = 5, answerFrameTime = 10;
	private final int startFrames = 30, startFrameTime = 50;
	private final int circleFrames = 30, circleFrameTime = 20;

	private final Context ctx;

	// =========================================
	// Constructors
	// =========================================

	public JoystickView(Context context) {
		super(context);
		ctx = context;
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ctx = context;
		initJoystickView();
	}

	// =========================================
	// Initialization
	// =========================================

	private void initJoystickView() {
		setFocusable(true);
		Width = getMeasuredWidth();
		Height = getMeasuredHeight();

		res = getResources();

		textSizeSP = 20; // 5dp
		textSizePix = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, getResources().getDisplayMetrics());
		textHandler = new Handler();
		answerHandler = new Handler();
		animateHandler = new Handler();

		for (int ans = 0; ans < X.length; ans++) {
			selectAnswers[ans] = false;
			selectOptions[ans] = false;
			circlePaint[ans] = new Paint(Paint.ANTI_ALIAS_FLAG);
			circlePaint[ans].setAlpha(0);
		}
		selectOptions[4] = false;

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(textSizePix);

		sidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sidePaint.setStrokeJoin(Paint.Join.ROUND);
		sidePaint.setStyle(Paint.Style.STROKE);
		sidePaint.setColor(Color.BLACK);
		sidePaint.setAlpha(255);
		sidePaint.setTextSize(textSizePix);
		sidePaint.setTextAlign(Paint.Align.CENTER);

		optionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		optionPaint.setColor(Color.RED);
		optionPaint.setAlpha(0);
		optionPaint.setStyle(Paint.Style.STROKE);
		optionPaint.setStrokeWidth(3);
		optionR = 0;
		options = true;

		bmpA = BitmapFactory.decodeResource(getResources(), R.drawable.select_a2);
		bmpB = BitmapFactory.decodeResource(getResources(), R.drawable.select_b2);
		bmpC = BitmapFactory.decodeResource(getResources(), R.drawable.select_c2);
		bmpD = BitmapFactory.decodeResource(getResources(), R.drawable.select_d2);
		bmpU = BitmapFactory.decodeResource(getResources(), R.drawable.select_u2);
		bmpAs = BitmapFactory.decodeResource(getResources(), R.drawable.select_a2s);
		bmpBs = BitmapFactory.decodeResource(getResources(), R.drawable.select_b2s);
		bmpCs = BitmapFactory.decodeResource(getResources(), R.drawable.select_c2s);
		bmpDs = BitmapFactory.decodeResource(getResources(), R.drawable.select_d2s);
		bmpUs = BitmapFactory.decodeResource(getResources(), R.drawable.select_u2s);
		bmpSet = BitmapFactory.decodeResource(getResources(), R.drawable.settings_background);
		bmpS = BitmapFactory.decodeResource(getResources(), R.drawable.select_s2);
		bmpQ = BitmapFactory.decodeResource(getResources(), R.drawable.select_q2);
		bmpQs = BitmapFactory.decodeResource(getResources(), R.drawable.select_q2s);
		bmpP = BitmapFactory.decodeResource(getResources(), R.drawable.select_p2);
		bmpStore = BitmapFactory.decodeResource(getResources(), R.drawable.select_store2);
		bmpSil = BitmapFactory.decodeResource(getResources(), R.drawable.select_sil2);
		bmpSnd = BitmapFactory.decodeResource(getResources(), R.drawable.select_sound2);
		/*bmp = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_background2);
		bmp_handle = BitmapFactory.decodeResource(getResources(), R.drawable.unlock);
		bmp_set = BitmapFactory.decodeResource(getResources(), R.drawable.select_s);
		bmp_quizMode = BitmapFactory.decodeResource(getResources(), R.drawable.select_q);
		bmp_em = BitmapFactory.decodeResource(getResources(), R.drawable.select_e);
		bmp_sil = BitmapFactory.decodeResource(getResources(), R.drawable.select_sil);
		bmp_A = BitmapFactory.decodeResource(getResources(), R.drawable.select_a);
		bmp_B = BitmapFactory.decodeResource(getResources(), R.drawable.select_b);
		bmp_C = BitmapFactory.decodeResource(getResources(), R.drawable.select_c);
		bmp_D = BitmapFactory.decodeResource(getResources(), R.drawable.select_d);
		bmp_silent = BitmapFactory.decodeResource(getResources(), R.drawable.silent_background);
		bmp_sound = BitmapFactory.decodeResource(getResources(), R.drawable.sound_background);
		if (select)
			bmp_select = BitmapFactory.decodeResource(getResources(), R.drawable.select_a);*/

		touchX = 0;
		touchY = 0;
		// angle = 0;
		rAns = Math.max(bmpA.getWidth(), bmpA.getHeight()) / 2;
		rBig = Math.max(bmpQ.getWidth(), bmpQ.getHeight()) / 2;
		swipeLength = rBig * 4;
		rSmall = Math.max(bmpS.getWidth(), bmpS.getHeight()) / 2;

		setDiffXY();

		/*srcRectForRender = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
		dstRectForRender = new Rect();
		srcRectForHandle = new Rect(0, 0, bmp_handle.getWidth(), bmp_handle.getHeight());
		dstRectForHandle = new Rect();*/
		srcRectForAns = new Rect(0, 0, bmpA.getWidth(), bmpA.getHeight());
		srcRectForBig = new Rect(0, 0, bmpQ.getWidth(), bmpQ.getHeight());
		srcRectForSmall = new Rect(0, 0, bmpS.getWidth(), bmpS.getHeight());
		srcRectForSet = new Rect(0, 0, bmpSet.getWidth(), bmpSet.getHeight());
		dstRectForA = new Rect();
		dstRectForB = new Rect();
		dstRectForC = new Rect();
		dstRectForD = new Rect();
		dstRectForU = new Rect();
		dstRectForSet = new Rect();
		dstRectForS = new Rect();
		dstRectForQ = new Rect();
		dstRectForP = new Rect();
		dstRectForE = new Rect();
		dstRectForSnd = new Rect();

		initRunnables();

		listener = new JoystickSelectListener() {
			@Override
			public void OnSelect(int s) {
			}
		};
		/*for (int i = 0; i < angles.length; i++)
			angles[i] = new AngleSelect();

		angles[0].lessThan = -pi * 5 / 6;
		angles[0].greaterThan = pi * 5 / 6;
		angles[1].lessThan = pi * 5 / 6;
		angles[1].greaterThan = pi / 2;
		angles[2].lessThan = pi / 2;
		angles[2].greaterThan = pi / 6;
		angles[3].lessThan = pi / 6;
		angles[3].greaterThan = -pi / 6;
		angles[4].lessThan = -pi / 6;
		angles[4].greaterThan = -pi / 4;
		angles[5].lessThan = -pi / 4;
		angles[5].greaterThan = -pi / 2;
		angles[6].lessThan = -pi / 2;
		angles[6].greaterThan = -pi * 3 / 4;
		angles[7].lessThan = -pi * 3 / 4;
		angles[7].greaterThan = -pi * 5 / 6;*/
	}

	// =========================================
	// Public Methods
	// =========================================

	public boolean isTooSmall() {
		if (Height < Width / 2)
			return true;
		else
			return false;
	}

	public void setOnJostickSelectedListener(JoystickSelectListener listener) {
		this.listener = listener;
	}

	public void setOnTouchedListener(JoystickTouchListener listener) {
		this.listenerTouch = listener;
	}

	public void removeTouchListener() {
		this.listenerTouch = null;
	}

	public boolean setQuizMode(boolean quizMode) {
		this.quizMode = quizMode;
		return this.quizMode;
	}

	public boolean setSettingsMode(boolean settingsMode) {
		this.settingsMode = settingsMode;
		return this.settingsMode;
	}

	public boolean setSilentMode(boolean silentMode) {
		this.silentMode = silentMode;
		return this.silentMode;
	}

	public boolean setEmergencyMode(boolean emergencyMode) {
		this.emergencyMode = emergencyMode;
		return this.emergencyMode;
	}

	public void setLeftRightHanded(boolean LtrueRfalse) {
		if (LtrueRfalse)
			state = 1;
		else
			state = 0;
		this.LtrueRfalse = LtrueRfalse;
		setDiffXY();
	}

	public void setUnlockType(int type) {
		this.type = type;
	}

	public void showStartAnimation(int start, int delay) {
		if (start == 0) {
			options = true;
			setSidePaths(Height - rBig * 2 - pad);// - dstHeight + textSizePix - pad);
		}
		sidePaint.setAlpha(255);

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
		invalidate();
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

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Width = measure(widthMeasureSpec);
		Height = measure(heightMeasureSpec);

		optionY = Height - rBig;

		spacing = (Width - rBig * 6 - rSmall * 4) / 6;
		selectLeft[4] = Width / 2 - rBig * 3 - rSmall * 2 - spacing * 2;
		selectRight[4] = Width / 2 - rBig * 3 - spacing * 2;
		selectLeft[3] = Width / 2 - rBig * 3 - spacing;
		selectRight[3] = Width / 2 - rBig - spacing;
		selectLeft[2] = Width / 2 - rBig;
		selectRight[2] = Width / 2 + rBig;
		selectLeft[1] = Width / 2 + rBig + spacing;
		selectRight[1] = Width / 2 + rBig * 3 + spacing;
		selectLeft[0] = Width / 2 + rBig * 3 + spacing * 2;
		selectRight[0] = Width / 2 + rBig * 3 + rSmall * 2 + spacing * 2;

		dstHeight = Width * bmpSet.getHeight() / bmpSet.getWidth();
		if (options)
			setSidePaths(Height - rBig * 2 - pad);// - dstHeight + textSizePix - pad);
		else
			setSidePaths(Height - pad);
		// dstRectForSet.set(0, Height - dstHeight + textSizePix, Width, Height - textSizePix + dstHeight);
		// dstRectForS.set(selectLeft[4], Height - rBig - rSmall, selectRight[4], Height - rBig + rSmall);
		// dstRectForQ.set(selectLeft[3], Height - rBig * 2, selectRight[3], Height);
		// dstRectForP.set(selectLeft[2], Height - rBig * 2, selectRight[2], Height);
		// dstRectForE.set(selectLeft[1], Height - rBig * 2, selectRight[1], Height);
		// dstRectForSnd.set(selectLeft[0], Height - rBig - rSmall, selectRight[0], Height - rBig + rSmall);

		// W = Math.min(W, bmp.getWidth()); // use the size of the bmp so we don't have to scale

		/*if (H > W) {
			H = W; // use a circle if there is room
			// int tempH = bmp.getHeight() * W / bmp.getWidth(); // use ellipse the size of the bmp
			// H = Math.min(tempH, H); // use the space available
		} else {
			int tempH = bmp.getHeight() * W / bmp.getWidth();	// use ellipse the size of the bmp
			H = Math.min(tempH, H);								// use the space available
		}

		rX = W / 2 * 0.8;
		rY = H / 2 * 0.7;
		thetaMax = Math.cos(rX / Math.sqrt(rX * rX + rY * rY));
		rMax = rX * rY / Math.sqrt(Math.pow(rY * Math.cos(thetaMax), 2) + Math.pow(rX * Math.sin(thetaMax), 2));
		fX = 1 / Math.cos(thetaMax);
		fY = 1 / Math.sin(thetaMax);

		handleRadius = (int) (W * 0.1);*/

		setMeasuredDimension(Width, Height);
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
		Width = getMeasuredWidth();
		Height = getMeasuredHeight();

		canvas.drawBitmap(bmpSet, srcRectForSet, dstRectForSet, sidePaint);
		canvas.drawCircle(optionX, optionY, optionR, optionPaint);
		canvas.drawText(res.getString(R.string.side_bar), Width / 2, TextHeight, sidePaint);
		canvas.drawBitmap(bmpS, srcRectForSmall, dstRectForS, sidePaint);
		if (quizMode)
			canvas.drawBitmap(bmpQs, srcRectForBig, dstRectForQ, sidePaint);
		else
			canvas.drawBitmap(bmpQ, srcRectForBig, dstRectForQ, sidePaint);
		canvas.drawBitmap(bmpP, srcRectForBig, dstRectForP, sidePaint);
		canvas.drawBitmap(bmpStore, srcRectForBig, dstRectForE, sidePaint);
		if (silentMode) {
			canvas.drawBitmap(bmpSil, srcRectForSmall, dstRectForSnd, sidePaint);
		} else {
			canvas.drawBitmap(bmpSnd, srcRectForSmall, dstRectForSnd, sidePaint);
		}

		dstRectForA.set((int) X[0] - rAns, (int) Y[0] - rAns, (int) X[0] + rAns, (int) Y[0] + rAns);
		dstRectForB.set((int) X[1] - rAns, (int) Y[1] - rAns, (int) X[1] + rAns, (int) Y[1] + rAns);
		dstRectForC.set((int) X[2] - rAns, (int) Y[2] - rAns, (int) X[2] + rAns, (int) Y[2] + rAns);
		dstRectForD.set((int) X[3] - rAns, (int) Y[3] - rAns, (int) X[3] + rAns, (int) Y[3] + rAns);
		dstRectForU.set((int) X[4] - rAns, (int) Y[4] - rAns, (int) X[4] + rAns, (int) Y[4] + rAns);
		if (selectAnswers[0])
			canvas.drawBitmap(bmpAs, srcRectForAns, dstRectForA, circlePaint[0]);
		else
			canvas.drawBitmap(bmpA, srcRectForAns, dstRectForA, circlePaint[0]);
		if (selectAnswers[1])
			canvas.drawBitmap(bmpBs, srcRectForAns, dstRectForB, circlePaint[1]);
		else
			canvas.drawBitmap(bmpB, srcRectForAns, dstRectForB, circlePaint[1]);
		if (selectAnswers[2])
			canvas.drawBitmap(bmpCs, srcRectForAns, dstRectForC, circlePaint[2]);
		else
			canvas.drawBitmap(bmpC, srcRectForAns, dstRectForC, circlePaint[2]);
		if (selectAnswers[3])
			canvas.drawBitmap(bmpDs, srcRectForAns, dstRectForD, circlePaint[3]);
		else
			canvas.drawBitmap(bmpD, srcRectForAns, dstRectForD, circlePaint[3]);
		if (selectAnswers[4])
			canvas.drawBitmap(bmpUs, srcRectForAns, dstRectForU, circlePaint[4]);
		else
			canvas.drawBitmap(bmpU, srcRectForAns, dstRectForU, circlePaint[4]);

		if ((selectOptions[0]) || (selectOptions[1]) || (selectOptions[2]) || (selectOptions[3]) || (selectOptions[4]))
			canvas.drawText(res.getString(R.string.swipe_option), Width / 2, (Height - rBig * 2) / 2, textPaint);
		else if (problem)
			if (!wrong)
				canvas.drawText(res.getString(R.string.swipe_screen), Width / 2, (Height - rBig * 2) / 2, textPaint);
			else
				canvas.drawText(res.getString(R.string.swipe_new), Width / 2, (Height - rBig * 2) / 2, textPaint);
		else
			canvas.drawText(res.getString(R.string.swipe_exit), Width / 2, (Height - rBig * 2) / 2, textPaint);

		canvas.save();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int actionType = event.getAction();

		if (!paused) {
			if (actionType == MotionEvent.ACTION_DOWN) {
				textHandler.removeCallbacks(revealText);
				textHandler.removeCallbacks(finishText);
				textHandler.removeCallbacks(revealCircle);
				textHandler.removeCallbacks(finishCircle);
				textPaint.setAlpha(0);
				invalidate();
				startX = event.getX();
				startY = event.getY();
				if ((startY >= Height - rBig * 2) && (options)) {	// Options selected
					animateHandler.removeCallbacks(startAnimate);
					animateHandler.removeCallbacks(finishAnimate);
					touchX = startX;
					touchY = startY;
					if ((startX >= selectLeft[0]) && (startX <= selectRight[0])) {
						selectOptions[0] = true;
						optionX = selectLeft[0] + rSmall;
						flashText(true);
					} else if ((startX >= selectLeft[1]) && (startX <= selectRight[1])) {
						selectOptions[1] = true;
						optionX = selectLeft[1] + rBig;
						flashText(true);
					} else if ((startX >= selectLeft[2]) && (startX <= selectRight[2])) {
						selectOptions[2] = true;
						optionX = selectLeft[2] + rBig;
						flashText(true);
					} else if ((startX >= selectLeft[3]) && (startX <= selectRight[3])) {
						selectOptions[3] = true;
						optionX = selectLeft[3] + rBig;
						flashText(true);
					} else if ((startX >= selectLeft[4]) && (startX <= selectRight[4])) {
						selectOptions[4] = true;
						optionX = selectLeft[4] + rSmall;
						flashText(true);
					}
					if (listener != null)
						listener.OnSelect(-1);		// send a vibrate signal
				} else if (startY >= TextHeight - textSizePix) {	// SideBar selected
					animateHandler.removeCallbacks(startAnimate);
					animateHandler.removeCallbacks(finishAnimate);
					sidePaint.setAlpha(255);
					selectSideBar = true;
					invalidate();
				} else if (!problem) {
					if (listener != null)
						listener.OnSelect(0);		// select A on any touch event
				} else {			// select answers
					switch (type) {
					case 0:
						break;
					case 1:
						break;
					case 2:
						setAnswerLocations();
						revealAnswers();
						break;
					}
					if (listener != null)
						listener.OnSelect(-1);		// send a vibrate signal
				}
			}
			if (actionType == MotionEvent.ACTION_MOVE) {
				touchX = event.getX();
				touchY = event.getY();
				checkSelection(false);
				invalidate();
				/*double tempX = (event.getX() - px);
				double tempY = (py - event.getY());

				if ((touchX == 0) && (touchY == 0) && (Math.abs(tempX) < handleRadius * 1.25) && (Math.abs(tempY) < handleRadius * 1.25)) {
					if (listener != null)
						listener.OnSelect(-1);	// center selected
					revealDisappearBackground(true);
					touchX = tempX;
					touchY = tempY;
					rCurrent = rX;
					angle = 0;
					invalidate();
				} else if ((touchX != 0) || (touchY != 0)) {
					touchX = tempX;
					touchY = tempY;
					angle = Math.atan2(touchY, touchX);
					// if ((Math.abs(touchX * fX) > rMax) || (Math.abs(touchY * fY) > rMax)) {
					rCurrent = rX * rY / Math.sqrt(Math.pow(rY * Math.cos(angle), 2) + Math.pow(rX * Math.sin(angle), 2));
					// set to radius if on edge
					double dis = Math.sqrt(touchX * touchX + touchY * touchY);
					if (dis > rCurrent) {
						touchX = rCurrent * Math.cos(angle);
						touchY = rCurrent * Math.sin(angle);
						checkSelection(angle, false);
					} else if (dis > rCurrent * .6) {
						checkSelection(angle, false);
					} else {
						select = false;
					}
					invalidate();
				}*/
			} else if (actionType == MotionEvent.ACTION_UP) {
				/*if (listenerTouch != null)
					listenerTouch.OnTouch();*/
				checkSelection(true);
				returnToDefault();
				flashText(false);
				/*if ((Math.sqrt(touchX * touchX + touchY * touchY) > rCurrent * .6) && (rCurrent > 0))
					checkSelection(angle, true);
				revealDisappearBackground(false);
				returnHandleToCenter();*/
			}
			return true;
		}
		if (actionType == MotionEvent.ACTION_UP) {
			if (listenerTouch != null)
				listenerTouch.OnTouch();
		}
		return true;
	}

	// =========================================
	// Setup and Initialization Algorithms
	// =========================================

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
				if (LtrueRfalse)
					state = ((startingState - attempts) % 4 + 4) % 4;
				else
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
				Width = getMeasuredWidth();
				int centerX;
				int centerY = rAns * 2;
				if (startX <= Width / 2) {
					centerX = Width - (int) (rAns * (Math.sqrt(3) + 1));
					X[4] = centerX + (int) (rAns * Math.sqrt(3));
					X[0] = centerX - (int) (rAns * Math.sqrt(3));
				} else {
					centerX = (int) (rAns * (Math.sqrt(3) + 1));
					X[4] = centerX - (int) (rAns * Math.sqrt(3));
					X[0] = centerX + (int) (rAns * Math.sqrt(3));
				}
				Y[0] = centerY;
				X[1] = centerX;
				Y[1] = centerY - rAns;
				X[2] = X[4];
				Y[2] = centerY;
				X[3] = centerX;
				Y[3] = centerY + rAns;
				Y[4] = centerY + rAns * 2;
			}
			state = startingState;
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
			diffX = (int) (rAns * Math.sqrt(3));
			diffY = -rAns;
			break;
		case 1:		// up-left
			diffX = (int) (-rAns * Math.sqrt(3));
			diffY = -rAns;
			break;
		case 2:		// down-left
			diffX = (int) (-rAns * Math.sqrt(3));
			diffY = rAns;
			break;
		case 3:		// down-right
			diffX = (int) (rAns * Math.sqrt(3));
			diffY = rAns;
			break;
		}
		diffX1 = diffX * 2;
		diffY1 = diffY * 2;
	}

	private int checkDiff(int loc) {
		Width = getMeasuredWidth();
		Height = getMeasuredHeight() - rBig * 2;
		int checks = 0;
		boolean first = (loc == 0);
		switch (state) {
		case 0:		// up-right
			if (Y[loc] - rAns < 0) {				// above top boundary
				checks += 1;
				state = ((state - 1) % 4 + 4) % 4;	// state-> 3=down-right
			}
			if (X[loc] + rAns > Width) {			// right of right boundary
				checks += 1;
				state = (state + checks) % 4;		// state-> 1=up-left or 2=down-left
			}
			break;
		case 1:		// up-left
			if (Y[loc] - rAns < 0) {		// above top boundary
				checks += 1;
				state = (state + 1) % 4;
			}
			if (X[loc] - rAns < 0) {		// left of left boundary
				checks += 1;
				state = ((state - checks) % 4 + 4) % 4;
			}
			break;
		case 2:		// down-left
			if (Y[loc] + rAns > Height) {		// below bottom boundary
				checks += 1;
				state = ((state - 1) % 4 + 4) % 4;
			}
			if (X[loc] - rAns < 0) {		// left of left boundary
				checks += 1;
				state = (state + checks) % 4;
			}
			break;
		case 3:		// down-right
			if (Y[loc] + rAns > Height) {		// below bottom boundary
				checks += 1;
				state = (state + 1) % 4;
			}
			if (X[loc] + rAns > Width) {		// right of right boundary
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
		for (int ans = 0; ans < X.length; ans++) {
			answerHandler.removeCallbacks(revealAnswer[ans]);
			answerHandler.removeCallbacks(finishAnswer[ans]);
		}

		for (int ans = 0; ans < X.length; ans++)
			circlePaint[ans].setAlpha(0);

		for (int ans = 0; ans < X.length; ans++) {
			for (int i = 0; i < answerFrames; i++) {
				answerHandler.postDelayed(revealAnswer[ans], i * answerFrameTime + ans * answerFrames * answerFrameTime);
			}
			answerHandler.postDelayed(finishAnswer[ans], answerFrames * answerFrameTime * (ans + 1));
		}
	}

	private void returnToDefault() {
		Height = getMeasuredHeight();
		Width = getMeasuredWidth();

		touchX = 0;
		touchY = 0;
		startX = -rAns;
		startY = -rAns;
		for (int ans = 0; ans < X.length; ans++) {
			selectAnswers[ans] = false;
			selectOptions[ans] = false;
		}
		if (options)
			showStartAnimation(0, 3000);
		else if (selectSideBar)
			showStartAnimation(1, 0);
		selectSideBar = false;
		switch (type) {
		case 0:
			for (int ans = 0; ans < X.length; ans++) {
				X[ans] = rAns;
				circlePaint[ans].setAlpha(255);
			}
			break;
		case 1:
			break;
		case 2:
			for (int ans = 0; ans < X.length; ans++) {
				answerHandler.removeCallbacks(revealAnswer[ans]);
				answerHandler.removeCallbacks(finishAnswer[ans]);
				X[ans] = -rAns;
				Y[ans] = -rAns;
				circlePaint[ans].setAlpha(0);
			}
			break;
		}
		invalidate();
	}

	private void checkSelection(boolean send) {
		double diffx, diffy;
		int s = -1;
		if ((selectOptions[0]) || (selectOptions[1]) || (selectOptions[2]) || (selectOptions[3]) || (selectOptions[4]) || (selectSideBar)) {
			diffx = touchX - startX;
			diffy = touchY - startY;
			if (selectOptions[0]) {
				s = 5;
				dstRectForSnd.set((int) touchX - rSmall, (int) touchY - rSmall, (int) touchX + rSmall, (int) touchY + rSmall);
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
				setSidePaths((int) Math.max(touchY, Height - rBig * 2 - pad));// - dstHeight + textSizePix - pad));
			}
			if (selectSideBar) {
				if ((send) && (touchY <= Height - dstHeight)) {
					options = true;
				} else if ((send) && (touchY > Height - dstHeight)) {
					options = false;
				}
			} else if (Math.sqrt(diffx * diffx + diffy * diffy) > swipeLength) {
				for (int i = 0; i < selectOptions.length; i++)
					selectOptions[i] = false;
				listener.OnSelect(s);
			}
		} else
			for (int i = 0; i < X.length; i++) {
				diffx = touchX - X[i];
				diffy = touchY - Y[i];
				if (Math.sqrt(diffx * diffx + diffy * diffy) < rAns)
					if (send) {
						selectAnswers[i] = false;
						listener.OnSelect(i);
					} else
						selectAnswers[i] = true;
				else
					selectAnswers[i] = false;
			}
	}

	private void flashText(boolean circle) {
		textHandler.removeCallbacks(revealText);
		textHandler.removeCallbacks(finishText);
		textHandler.removeCallbacks(revealCircle);
		textHandler.removeCallbacks(finishCircle);

		textPaint.setAlpha(0);

		for (int i = 0; i < textFrames; i++) {
			textHandler.postDelayed(revealText, i * textFrameTime);
		}
		textHandler.postDelayed(finishText, textFrames * textFrameTime);

		if (circle) {
			optionR = rSmall;
			for (int i = 0; i < circleFrames; i++) {
				textHandler.postDelayed(revealCircle, i * circleFrameTime);
			}
			textHandler.postDelayed(finishCircle, circleFrames * circleFrameTime);
		} else {
			optionR = 0;
			optionPaint.setAlpha(0);
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void initRunnables() {
		final int answerInterval = 255 / answerFrames;
		final int textInterval = 255 / textFrames;
		// final int startInterval = -255 / startFrames;
		final int radiusInterval = (swipeLength - rSmall) / circleFrames;
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		int w;
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			display.getSize(size);
			w = size.x;
		} else {
			w = display.getWidth();
		}
		dstHeight = w * bmpSet.getHeight() / bmpSet.getWidth();
		final int slideInterval = (dstHeight - pad) / startFrames;

		startAnimate = new Runnable() {
			@Override
			public void run() {
				options = false;
				// sidePaint.setAlpha(sidePaint.getAlpha() + startInterval);
				if (TextHeight + slideInterval > Height - pad)
					setSidePaths(Height - pad);
				else
					setSidePaths(TextHeight + slideInterval);
				invalidate();
			}
		};
		finishAnimate = new Runnable() {
			@Override
			public void run() {
				// sidePaint.setAlpha(0);
				setSidePaths(Height - pad);
				options = false;
				invalidate();
			}
		};

		for (int i = 0; i < X.length; i++) {
			final int a = i;
			revealAnswer[a] = new Runnable() {
				@Override
				public void run() {
					circlePaint[a].setAlpha(circlePaint[a].getAlpha() + answerInterval);
					invalidate();
				}
			};
			finishAnswer[a] = new Runnable() {
				@Override
				public void run() {
					circlePaint[a].setAlpha(255);
					invalidate();
				}
			};
		}
		revealText = new Runnable() {
			@Override
			public void run() {
				textPaint.setAlpha(textPaint.getAlpha() + textInterval);
				invalidate();
			}
		};
		finishText = new Runnable() {
			@Override
			public void run() {
				textPaint.setAlpha(255);
				invalidate();
			}
		};
		revealCircle = new Runnable() {
			@Override
			public void run() {
				optionPaint.setAlpha(255);
				optionR += radiusInterval;
				invalidate();
			}
		};
		finishCircle = new Runnable() {
			@Override
			public void run() {
				optionPaint.setAlpha(0);
				optionR = 0;
				invalidate();
			}
		};
	}

	private void setSidePaths(int side) {
		TextHeight = side;
		dstRectForSet.set(0, TextHeight - textSizePix, Width, TextHeight - textSizePix + dstHeight);
		int temp = side + rBig * 2 + pad;// + dstHeight - textSizePix + pad;
		dstRectForS.set(selectLeft[4], temp - rBig - rSmall, selectRight[4], temp - rBig + rSmall);
		dstRectForQ.set(selectLeft[3], temp - rBig * 2, selectRight[3], temp);
		dstRectForP.set(selectLeft[2], temp - rBig * 2, selectRight[2], temp);
		dstRectForE.set(selectLeft[1], temp - rBig * 2, selectRight[1], temp);
		dstRectForSnd.set(selectLeft[0], temp - rBig - rSmall, selectRight[0], temp - rBig + rSmall);
	}
	/*private void checkSelection(double ang, boolean send) {
		select = false;

		if (listener != null) {
			if ((angle >= angles[3].greaterThan) && (angle < angles[3].lessThan)) {
				// D selected
				if (send)
					listener.OnSelect(3);
				else {
					select = true;
					bmp_select = bmp_D;
				}
			} else if ((angle >= angles[2].greaterThan) && (angle < angles[2].lessThan)) {
				// C selected
				if (send)
					listener.OnSelect(2);
				else {
					select = true;
					bmp_select = bmp_C;
				}
			} else if ((angle >= angles[1].greaterThan) && (angle < angles[1].lessThan)) {
				// B selected
				if (send)
					listener.OnSelect(1);
				else {
					select = true;
					bmp_select = bmp_B;
				}
			} else if ((angle >= angles[0].greaterThan) || (angle <= angles[0].lessThan)) {
				// A selected
				if (send)
					listener.OnSelect(0);
				else {
					select = true;
					bmp_select = bmp_A;
				}
			} else if ((angle >= angles[4].greaterThan) && (angle < angles[4].lessThan)) {
				// sound/silent selected
				if (send)
					listener.OnSelect(4);
				else {
					select = true;
					bmp_select = bmp_sil;
				}
			} else if ((angle >= angles[5].greaterThan) && (angle < angles[5].lessThan)) {
				// emergency call selected
				if (send)
					listener.OnSelect(5);
				else {
					select = true;
					bmp_select = bmp_em;
				}
			} else if ((angle >= angles[6].greaterThan) && (angle < angles[6].lessThan)) {
				// quiz mode selected
				if (send)
					listener.OnSelect(6);
				else {
					select = true;
					bmp_select = bmp_quizMode;
				}
			} else if ((angle >= angles[7].greaterThan) && (angle < angles[7].lessThan)) {
				// settings selected
				if (send)
					listener.OnSelect(7);
				else {
					select = true;
					bmp_select = bmp_set;
				}
			} else {
				// nothing selected
				if (send)
					listener.OnSelect(-1);

			}
		}
	}*/

	/*private void returnHandleToCenter() {

		Handler handler = new Handler();
		int numFrames = 5;
		int frameTime = 20;
		final double intervalsX = (0 - touchX) / numFrames;
		final double intervalsY = (0 - touchY) / numFrames;

		for (int i = 0; i < numFrames; i++) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					touchX += intervalsX;
					touchY += intervalsY;
					invalidate();
				}
			}, i * frameTime);
		}
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				touchX = 0;
				touchY = 0;
				angle = 0;
				rCurrent = 0;
				invalidate();
			}
		}, numFrames * frameTime);
	}*/

	/*private void revealDisappearBackground(final boolean RorD) {
		Handler handler = new Handler();
		int numFrames = 10;
		int frameTime = 40;
		final int interval = 255 / numFrames;
		if (RorD)
			circlePaint.setAlpha(0);
		else
			circlePaint.setAlpha(255);

		for (int i = 0; i < numFrames; i++) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (RorD)
						circlePaint.setAlpha(circlePaint.getAlpha() + interval);
					else
						circlePaint.setAlpha(circlePaint.getAlpha() - interval);
					invalidate();
				}
			}, i * frameTime);
		}
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (RorD)
					circlePaint.setAlpha(255);
				else
					circlePaint.setAlpha(0);
				invalidate();
			}
		}, numFrames * frameTime);
	}*/
}