package com.olyware.mathlock.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.olyware.mathlock.R;

public class JoystickView extends View {
	private final int NumAnswers = 5;
	private final int alpha = 75;
	private Bitmap /*bmpSet, */bmpS, bmpQ, bmpQs, bmpP, bmpStore, bmpI, bmpUnlock;
	private Bitmap bmpAnswers[] = new Bitmap[NumAnswers];
	private Bitmap bmpAnswersSelected[] = new Bitmap[NumAnswers];
	private RectF RectForAnswers[] = new RectF[NumAnswers + 1];
	private RectF dstRectForSet;
	private Rect dstRectForS, dstRectForQ, dstRectForP, dstRectForE, dstRectForI, RectForUnlock;
	private Rect srcRectForAns, srcRectForBig, srcRectForSmall;// , srcRectForSet;

	private Paint circlePaint[] = new Paint[NumAnswers];
	private Paint textPaint, sidePaint, settingsPaint, optionPaint;

	private int textSizeSP, textSizePix, answerSizeSP, answerHintSizeSP;
	private float answerSizePix, answerHintSizePix;
	private final int answerSizeSPDefault = 50, answerHintSizeSPDefault = 30;
	private double touchX, touchY;
	private double startX, startY;
	private float optionX, optionY, optionR;
	private int Width, Height, dstHeight, pad = 10, outlineWidth = pad / 2;
	private double X[] = new double[NumAnswers];		// a=0,b=1,c=2,d=3
	private double Y[] = new double[NumAnswers];
	private int type = 0;
	private int diffX1, diffY1, diffX, diffY;

	private int correctAnswer, wrongAnswer;
	private String answers[] = { "N/A", "N/A", "N/A", "N/A", "?" };
	private TextPaint answerTextPaint[] = new TextPaint[NumAnswers];
	private TextPaint answerSelectedTextPaint[] = new TextPaint[NumAnswers];
	private Paint answerPaint[] = new Paint[NumAnswers];
	private Paint answerSelectedPaint[] = new Paint[NumAnswers];
	private StaticLayout layout[] = new StaticLayout[NumAnswers];
	private Rect bounds[] = new Rect[NumAnswers];

	// direction answers are going (0=up-right, 1=up-left, 2=down-left, 3=down-right)
	private int state = 0;

	private int spacing, rAns, rBig, rSmall, swipeLengthOption, swipeLength1;
	private int TextHeight;

	private JoystickSelectListener listener;
	private JoystickTouchListener listenerTouch;

	private boolean quizMode;
	private boolean LtrueRfalse;
	private boolean selectAnswers[] = new boolean[NumAnswers];
	private boolean selectOptions[] = new boolean[5];
	private boolean selectUnlock;
	private boolean options = true, selectSideBar = false;
	private boolean problem = true, wrong = false, paused = false;
	private boolean measured = false;
	private int selectLeft[] = new int[5];
	private int selectRight[] = new int[5];

	private Resources res;
	private Runnable revealText, finishText, startAnimate, finishAnimate, revealCircle, finishCircle, pulseLock;
	private Runnable revealAnswer[] = new Runnable[NumAnswers];
	private Runnable finishAnswer[] = new Runnable[NumAnswers];
	private Handler answerHandler, textHandler, animateHandler;
	private final int textFrames = 10, textFrameTime = 50, answerFrames = 5, answerFrameTime = 10, startFrames = 30, startFrameTime = 50,
			circleFrames = 30, circleFrameTime = 20, pulseFrames = 20, pulseFrameTime = 50;

	private final Context ctx;

	// =========================================
	// Constructors
	// =========================================

	public JoystickView(Context context) {
		super(context);
		ctx = context;
		initView();
	}

	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;
		initView();
	}

	public JoystickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ctx = context;
		initView();
	}

	// =========================================
	// Initialization
	// =========================================

	private void initView() {
		setFocusable(true);
		Width = getMeasuredWidth();
		Height = getMeasuredHeight();

		res = getResources();

		textSizeSP = 20;
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
		selectUnlock = false;

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(textSizePix);

		settingsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		settingsPaint.setStyle(Paint.Style.FILL);

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

		answerSizeSP = answerSizeSPDefault;
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, getResources().getDisplayMetrics());
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i] = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			answerTextPaint[i].setTextAlign(Paint.Align.CENTER);
			answerTextPaint[i].setColor(Color.WHITE);
			answerTextPaint[i].setTextSize(answerSizePix);
			answerSelectedTextPaint[i] = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			answerSelectedTextPaint[i].setTextAlign(Paint.Align.CENTER);
			answerSelectedTextPaint[i].setColor(Color.BLUE);
			answerSelectedTextPaint[i].setTextSize(answerSizePix);
			answerPaint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
			answerPaint[i].setStyle(Paint.Style.FILL_AND_STROKE);
			answerPaint[i].setColor(Color.argb(alpha, 255, 255, 255));
			answerPaint[i].setStrokeWidth(outlineWidth);
			answerSelectedPaint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
			answerSelectedPaint[i].setStyle(Paint.Style.FILL_AND_STROKE);
			answerSelectedPaint[i].setColor(Color.argb(alpha, 0, 0, 255));
			answerSelectedPaint[i].setStrokeWidth(outlineWidth);
			bounds[i] = new Rect();
			layout[i] = new StaticLayout(answers[i], answerTextPaint[i], Width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
		}

		for (int i = 0; i < bmpAnswers.length; i++) {
			int id = getResources().getIdentifier("answer" + i, "drawable", ctx.getPackageName());
			bmpAnswers[i] = BitmapFactory.decodeResource(getResources(), id);
		}
		for (int i = 0; i < bmpAnswersSelected.length; i++) {
			int id = getResources().getIdentifier("answer_selected" + i, "drawable", ctx.getPackageName());
			bmpAnswersSelected[i] = BitmapFactory.decodeResource(getResources(), id);
		}
		// bmpSet = BitmapFactory.decodeResource(getResources(), R.drawable.settings_background);
		bmpS = BitmapFactory.decodeResource(getResources(), R.drawable.select_s2);
		bmpQ = BitmapFactory.decodeResource(getResources(), R.drawable.select_q2);
		bmpQs = BitmapFactory.decodeResource(getResources(), R.drawable.select_q2s);
		bmpP = BitmapFactory.decodeResource(getResources(), R.drawable.select_p2);
		bmpStore = BitmapFactory.decodeResource(getResources(), R.drawable.select_store2);
		bmpI = BitmapFactory.decodeResource(getResources(), R.drawable.select_i2);
		bmpUnlock = BitmapFactory.decodeResource(getResources(), R.drawable.unlock);

		touchX = 0;
		touchY = 0;

		rAns = Math.max(bmpAnswers[0].getWidth(), bmpAnswers[0].getHeight()) / 2;
		rBig = Math.max(bmpQ.getWidth(), bmpQ.getHeight()) / 2;
		swipeLengthOption = rBig * 4;
		swipeLength1 = rAns * 2;
		rSmall = Math.max(bmpS.getWidth(), bmpS.getHeight()) / 2;

		setDiffXY();

		srcRectForAns = new Rect(0, 0, bmpAnswers[0].getWidth(), bmpAnswers[0].getHeight());
		srcRectForBig = new Rect(0, 0, bmpQ.getWidth(), bmpQ.getHeight());
		srcRectForSmall = new Rect(0, 0, bmpS.getWidth(), bmpS.getHeight());

		for (int i = 0; i < RectForAnswers.length; i++) {
			RectForAnswers[i] = new RectF();
		}
		dstRectForSet = new RectF();
		dstRectForS = new Rect();
		dstRectForQ = new Rect();
		dstRectForP = new Rect();
		dstRectForE = new Rect();
		dstRectForI = new Rect();
		RectForUnlock = new Rect();

		listener = new JoystickSelectListener() {
			@Override
			public void OnSelect(int s) {
			}
		};
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

	public boolean setQuizMode(boolean quizMode) {
		this.quizMode = quizMode;
		return this.quizMode;
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

	public void setCorrectAnswer(int location) {
		this.correctAnswer = location;
		setLayouts();
	}

	public void setIncorrectGuess(int location) {
		this.wrongAnswer = location;
		setLayouts();
	}

	public void resetGuess() {
		this.correctAnswer = -1;
		this.wrongAnswer = -1;
	}

	public void setAnswers(String answers[]) {
		this.answers = new String[] { answers[0], answers[1], answers[2], answers[3], "?" };
		switch (type) {
		case 0:
		case 1:
			if (measured) {
				resetAnswerSize();
				setDimensions();
				animateHandler.removeCallbacks(pulseLock);
				animateHandler.postDelayed(pulseLock, pulseFrameTime);
			}
			break;
		case 2:
			break;
		}

	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		switch (type) {
		case 0:
		case 1:
			measured = true;
			setDimensions();
			animateHandler.removeCallbacks(pulseLock);
			animateHandler.postDelayed(pulseLock, pulseFrameTime);
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
			setSidePaths(Height - rBig * 2 - pad);// - dstHeight + textSizePix - pad);
		else
			setSidePaths(Height - pad);

		setMeasuredDimension(Width, Height);
		initRunnables();
		showStartAnimation(0, 3000);
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
		} else {
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Draw the option bar
		canvas.drawRoundRect(dstRectForSet, textSizePix, textSizePix, settingsPaint);
		canvas.drawCircle(optionX, optionY, optionR, optionPaint);
		canvas.drawText(res.getString(R.string.side_bar), Width / 2, TextHeight, sidePaint);
		canvas.drawBitmap(bmpS, srcRectForSmall, dstRectForS, sidePaint);
		if (quizMode)
			canvas.drawBitmap(bmpQs, srcRectForBig, dstRectForQ, sidePaint);
		else
			canvas.drawBitmap(bmpQ, srcRectForBig, dstRectForQ, sidePaint);
		canvas.drawBitmap(bmpP, srcRectForBig, dstRectForP, sidePaint);
		canvas.drawBitmap(bmpStore, srcRectForBig, dstRectForE, sidePaint);
		canvas.drawBitmap(bmpI, srcRectForSmall, dstRectForI, sidePaint);

		if ((type == 0) || (type == 1)) {
			RectForAnswers[0].set(outlineWidth, outlineWidth, Width / 2 - outlineWidth, (TextHeight - textSizePix) / 2 - outlineWidth
					- rAns);
			RectForAnswers[1].set(Width / 2 + outlineWidth, outlineWidth, Width - outlineWidth, (TextHeight - textSizePix) / 2
					- outlineWidth - rAns);
			RectForAnswers[2].set(outlineWidth, (TextHeight - textSizePix) / 2 + outlineWidth + rAns, Width / 2 - outlineWidth,
					(TextHeight - textSizePix) - outlineWidth);
			RectForAnswers[3].set(Width / 2 + outlineWidth, (TextHeight - textSizePix) / 2 + outlineWidth + rAns, Width - outlineWidth,
					(TextHeight - textSizePix) - outlineWidth);
			RectForAnswers[4].set(outlineWidth, (TextHeight - textSizePix) / 2 + outlineWidth - rAns, Width / 2 - outlineWidth - rAns,
					(TextHeight - textSizePix) / 2 - outlineWidth + rAns);
			RectForAnswers[5].set(Width / 2 + outlineWidth + rAns, (TextHeight - textSizePix) / 2 + outlineWidth - rAns, Width
					- outlineWidth, (TextHeight - textSizePix) / 2 - outlineWidth + rAns);
		}
		switch (type) {
		case 0:
			for (int i = 0; i <= NumAnswers; i++) {
				if (selectAnswers[Math.min(i, NumAnswers - 1)])
					canvas.drawRoundRect(RectForAnswers[i], 5, 5, answerSelectedPaint[Math.min(i, NumAnswers - 1)]);
				else
					canvas.drawRoundRect(RectForAnswers[i], 5, 5, answerPaint[Math.min(i, NumAnswers - 1)]);
			}
			for (int i = 0; i <= NumAnswers; i++) {
				canvas.save();
				canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2, (RectForAnswers[i].top + RectForAnswers[i].bottom)
						/ 2 - layout[Math.min(i, NumAnswers - 1)].getHeight() / 2); // position the text
				layout[Math.min(i, NumAnswers - 1)].draw(canvas);
				canvas.restore();
			}
			canvas.drawBitmap(bmpUnlock, srcRectForAns, RectForUnlock, sidePaint);
			break;
		case 1:
			for (int i = 0; i <= NumAnswers; i++) {
				if (selectAnswers[Math.min(i, NumAnswers - 1)])
					canvas.drawRoundRect(RectForAnswers[i], 5, 5, answerSelectedPaint[Math.min(i, NumAnswers - 1)]);
				else
					canvas.drawRoundRect(RectForAnswers[i], 5, 5, answerPaint[Math.min(i, NumAnswers - 1)]);
			}
			for (int i = 0; i <= NumAnswers; i++) {
				canvas.save();
				canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2, (RectForAnswers[i].top + RectForAnswers[i].bottom)
						/ 2 - layout[Math.min(i, NumAnswers - 1)].getHeight() / 2); // position the text
				layout[Math.min(i, NumAnswers - 1)].draw(canvas);
				canvas.restore();
			}
			canvas.drawBitmap(bmpUnlock, srcRectForAns, RectForUnlock, sidePaint);
			break;
		case 2:
			for (int i = 0; i < X.length; i++) {
				RectForAnswers[i].set((int) X[i] - rAns, (int) Y[i] - rAns, (int) X[i] + rAns, (int) Y[i] + rAns);
				if (selectAnswers[i])
					canvas.drawBitmap(bmpAnswersSelected[i], srcRectForAns, RectForAnswers[i], circlePaint[i]);
				else
					canvas.drawBitmap(bmpAnswers[i], srcRectForAns, RectForAnswers[i], circlePaint[i]);
			}
			if ((selectOptions[0]) || (selectOptions[1]) || (selectOptions[2]) || (selectOptions[3]) || (selectOptions[4]))
				canvas.drawText(res.getString(R.string.swipe_option), Width / 2, (Height - rBig * 2) / 2, textPaint);
			else if (problem)
				if (!wrong)
					canvas.drawText(res.getString(R.string.swipe_screen), Width / 2, (Height - rBig * 2) / 2, textPaint);
				else
					canvas.drawText(res.getString(R.string.swipe_new), Width / 2, (Height - rBig * 2) / 2, textPaint);
			else
				canvas.drawText(res.getString(R.string.swipe_exit), Width / 2, (Height - rBig * 2) / 2, textPaint);
			break;
		}
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
					} else if ((startX >= selectLeft[1]) && (startX <= selectRight[1])) {
						selectOptions[1] = true;
						optionX = selectLeft[1] + rBig;
					} else if ((startX >= selectLeft[2]) && (startX <= selectRight[2])) {
						selectOptions[2] = true;
						optionX = selectLeft[2] + rBig;
					} else if ((startX >= selectLeft[3]) && (startX <= selectRight[3])) {
						selectOptions[3] = true;
						optionX = selectLeft[3] + rBig;
					} else if ((startX >= selectLeft[4]) && (startX <= selectRight[4])) {
						selectOptions[4] = true;
						optionX = selectLeft[4] + rSmall;
					}
					flashText(true);
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
						checkSelection(false, true);
						break;
					case 1:
						checkSelection(false, true);
						break;
					case 2:
						setAnswerLocations();
						revealAnswers();
						if (listener != null)
							listener.OnSelect(-1);		// send a vibrate signal
						break;
					}
				}
				setDimensions();
			}
			if (actionType == MotionEvent.ACTION_MOVE) {
				touchX = event.getX();
				touchY = event.getY();
				checkSelection(false, false);
				invalidate();
			} else if (actionType == MotionEvent.ACTION_UP) {
				checkSelection(true, false);
				returnToDefault();
				flashText(false);
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
			if (Y[loc] + rAns > (Height - rBig * 2)) {		// below bottom boundary
				checks += 1;
				state = ((state - 1) % 4 + 4) % 4;
			}
			if (X[loc] - rAns < 0) {		// left of left boundary
				checks += 1;
				state = (state + checks) % 4;
			}
			break;
		case 3:		// down-right
			if (Y[loc] + rAns > (Height - rBig * 2)) {		// below bottom boundary
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
		touchX = 0;
		touchY = 0;
		startX = -rAns;
		startY = -rAns;
		for (int ans = 0; ans < X.length; ans++) {
			selectAnswers[ans] = false;
			selectOptions[ans] = false;
		}
		selectUnlock = false;

		if (options)
			showStartAnimation(0, 3000);
		else if (selectSideBar)
			showStartAnimation(1, 0);
		else
			RectForUnlock.set(Width / 2 - rAns, (TextHeight - textSizePix) / 2 - rAns, Width / 2 + rAns, (TextHeight - textSizePix) / 2
					+ rAns);
		selectSideBar = false;
		switch (type) {
		case 0:
		case 1:
			animateHandler.postDelayed(pulseLock, pulseFrameTime);
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
		setDimensions();
		// invalidate();
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
				listener.OnSelect(s);
			}
		} else
			switch (type) {
			case 0:
			case 1:
				for (int i = 0; i < NumAnswers; i++)
					selectAnswers[i] = false;

				if (selectUnlock) {
					diffx = touchX - startX;
					diffy = touchY - startY;
					RectForUnlock.set((int) touchX - rAns, (int) touchY - rAns, (int) touchX + rAns, (int) touchY + rAns);
					if (Math.sqrt(diffx * diffx + diffy * diffy) > swipeLength1) {
						int select = -1;
						if (touchX < RectForAnswers[0].right) {
							if (touchY < RectForAnswers[0].bottom)
								select = 0;
							else if (touchY > RectForAnswers[2].top)
								select = 2;
							else
								select = 4;
						} else if (touchX > RectForAnswers[1].left) {
							if (touchY < RectForAnswers[1].bottom)
								select = 1;
							else if (touchY > RectForAnswers[3].top)
								select = 3;
							else
								select = 4;
						}

						if (select >= 0)
							if (send)
								listener.OnSelect(select);
							else
								selectAnswers[select] = true;
					}
				} else {
					touchX = startX;
					touchY = startY;
					diffx = startX - (RectForUnlock.right + RectForUnlock.left) / 2;
					diffy = startY - (RectForUnlock.top + RectForUnlock.bottom) / 2;
					if (Math.sqrt(diffx * diffx + diffy * diffy) < rAns) {
						RectForUnlock.set((int) touchX - rAns, (int) touchY - rAns, (int) touchX + rAns, (int) touchY + rAns);
						selectUnlock = true;
						animateHandler.removeCallbacks(pulseLock);
						if (listener != null)
							listener.OnSelect(-1);		// send a vibrate signal
					}
				}
				break;
			case 2:
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
				break;
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

	@SuppressLint("NewApi")
	private void initRunnables() {
		final int answerInterval = 255 / answerFrames;
		final int textInterval = 255 / textFrames;
		// final int startInterval = -255 / startFrames;
		final int radiusInterval = (swipeLengthOption - rSmall) / circleFrames;
		final int slideInterval = (dstHeight - pad) / startFrames;
		final int pulseMaxChange = rAns / 5;

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
		pulseLock = new Runnable() {
			@Override
			public void run() {
				if (!selectUnlock) {
					int size = (RectForUnlock.right - RectForUnlock.left) / 2;
					int change = ((int) (pulseMaxChange * Math.sin(Math.PI * 2 * System.currentTimeMillis()
							/ (pulseFrames * pulseFrameTime))) + rAns)
							- size;
					RectForUnlock.set(RectForUnlock.left - change, RectForUnlock.top - change, RectForUnlock.right + change,
							RectForUnlock.bottom + change);
				}
				invalidate();
				animateHandler.postDelayed(pulseLock, pulseFrameTime);
			}
		};
	}

	private void setSidePaths(int side) {
		TextHeight = side;
		dstRectForSet.set(0, TextHeight - textSizePix, Width, TextHeight - textSizePix + dstHeight);
		settingsPaint.setShader(new LinearGradient(0, TextHeight - textSizePix, 0, TextHeight - textSizePix + dstHeight, Color.WHITE,
				Color.TRANSPARENT, TileMode.MIRROR));
		int temp = side + rBig * 2 + pad;
		dstRectForS.set(selectLeft[4], temp - rBig - rSmall, selectRight[4], temp - rBig + rSmall);
		dstRectForQ.set(selectLeft[3], temp - rBig * 2, selectRight[3], temp);
		dstRectForP.set(selectLeft[2], temp - rBig * 2, selectRight[2], temp);
		dstRectForE.set(selectLeft[1], temp - rBig * 2, selectRight[1], temp);
		dstRectForI.set(selectLeft[0], temp - rBig - rSmall, selectRight[0], temp - rBig + rSmall);
		if (!selectUnlock) {
			int currentRadius = (RectForUnlock.right - RectForUnlock.left) / 2;
			RectForUnlock.set(Width / 2 - currentRadius, (TextHeight - textSizePix) / 2 - currentRadius, Width / 2 + currentRadius,
					(TextHeight - textSizePix) / 2 + currentRadius);
		}

	}

	private void setDimensions() {
		setLayouts();
		float maxH[] = { 0, 0, 0, 0, 0 };
		for (int i = 0; i < NumAnswers; i++) {
			maxH[i] = Math.max(bounds[i].height(), layout[i].getHeight());
			if (i < NumAnswers - 1) {
				maxH[i] = Math.max(maxH[i], answerSizePix);
				if ((maxH[i] > ((TextHeight - textSizePix) / 2 - outlineWidth * 3 - pad * 3 - rAns)) && (Height > 0)) {
					decreaseAnswerSize();
					setDimensions();
					return;
				} else if (isLayoutSplittingWords(answers[i], layout[i])) {
					decreaseAnswerSize();
					setDimensions();
					return;
				}
			} else {
				maxH[i] = Math.max(maxH[i], answerHintSizePix);
				if ((maxH[i] > (rAns * 2 - outlineWidth * 2 - pad * 2)) && (Height > 0)) {
					decreaseHintSize();
					setDimensions();
					return;
				} else if (isLayoutSplittingWords(answers[i], layout[i])) {
					decreaseHintSize();
					setDimensions();
					return;
				}
			}
		}
		for (int i = 0; i < NumAnswers - 1; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
		answerTextPaint[NumAnswers - 1].setTextSize(answerHintSizePix);
		invalidate();
	}

	private void setLayouts() {
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

		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].getTextBounds(answers[i], 0, answers[i].length(), bounds[i]);
			if (i == correctAnswer) {
				// answerTextPaint[i].setColor(Color.GREEN);
				answerPaint[i].setColor(Color.argb(alpha, 0, 255, 0));
			} else if (i == wrongAnswer) {
				// answerTextPaint[i].setColor(Color.RED);
				answerPaint[i].setColor(Color.argb(alpha, 255, 0, 0));
			} else {
				answerTextPaint[i].setColor(Color.WHITE);
				answerPaint[i].setColor(Color.argb(alpha, 255, 255, 255));
			}
			if (i < NumAnswers - 1)
				layout[i] = new StaticLayout(answers[i], answerTextPaint[i], Width / 2 - outlineWidth * 2 - pad * 2,
						Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
			else
				layout[i] = new StaticLayout(answers[i], answerTextPaint[i], Width / 2 - outlineWidth * 2 - pad * 2 - rAns,
						Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
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
		answerSizeSP = Math.max(answerSizeSP - 2, 1);
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, getResources().getDisplayMetrics());
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
	}

	private void decreaseHintSize() {
		answerHintSizeSP = Math.max(answerHintSizeSP - 1, 1);
		answerHintSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerHintSizeSP, getResources().getDisplayMetrics());
		answerTextPaint[NumAnswers - 1].setTextSize(answerHintSizePix);
	}

	private void resetAnswerSize() {
		answerSizeSP = answerSizeSPDefault;
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, getResources().getDisplayMetrics());
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
	}

	private void resetHintSize() {
		answerHintSizeSP = answerHintSizeSPDefault;
		answerHintSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerHintSizeSP, getResources().getDisplayMetrics());
		answerTextPaint[NumAnswers - 1].setTextSize(answerHintSizePix);
	}

	private void removePulseAnimation() {
		animateHandler.removeCallbacks(pulseLock);
		RectForUnlock.set(Width / 2 - rAns, (TextHeight - textSizePix) / 2 - rAns, Width / 2 + rAns, (TextHeight - textSizePix) / 2 + rAns);
	}
}