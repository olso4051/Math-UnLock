package com.olyware.mathlock.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
	private Bitmap bmpSet, bmpS, bmpQ, bmpQs, bmpP, bmpStore, bmpI;
	private Bitmap bmpAnswers[] = new Bitmap[NumAnswers];
	private Bitmap bmpAnswersSelected[] = new Bitmap[NumAnswers];
	private Rect RectForAnswers[] = new Rect[NumAnswers];
	private Rect dstRectForSet, dstRectForS, dstRectForQ, dstRectForP, dstRectForE, dstRectForI;
	private Rect srcRectForAns, srcRectForBig, srcRectForSmall, srcRectForSet;

	private Paint circlePaint[] = new Paint[NumAnswers];
	private Paint textPaint, sidePaint, optionPaint;

	private int textSizeSP, textSizePix, answerSizeSP;
	private float answerSizePix;
	private final int answerSizeSPDefault = 30;
	private double touchX, touchY;
	private double startX, startY;
	private float optionX, optionY, optionR;
	private int Width, Height, dstHeight, pad = 10, outlineWidth = pad / 2;
	private double X[] = new double[NumAnswers];		// a=0,b=1,c=2,d=3
	private double Y[] = new double[NumAnswers];
	private int type = 0;
	private int diffX1, diffY1, diffX, diffY;

	private int correctAnswer, wrongAnswer;
	final private int maxAnswers = 4;
	private String answers[] = { "N/A", "N/A", "N/A", "N/A" };
	private TextPaint answerPaint[] = new TextPaint[4];
	private StaticLayout layout[] = new StaticLayout[4];
	private Rect bounds[] = new Rect[4];

	// direction answers are going (0=up-right, 1=up-left, 2=down-left, 3=down-right)
	private int state = 0;

	private int spacing, rAns, rBig, rSmall, swipeLength;
	private int TextHeight;

	private JoystickSelectListener listener;
	private JoystickTouchListener listenerTouch;

	private boolean quizMode;
	private boolean LtrueRfalse;
	private boolean selectAnswers[] = new boolean[NumAnswers];
	private boolean selectOptions[] = new boolean[5];
	private boolean options = true, selectSideBar = false;
	private boolean problem = true, wrong = false, paused = false;
	private boolean measured = false;
	private int selectLeft[] = new int[5];
	private int selectRight[] = new int[5];

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

		answerSizeSP = answerSizeSPDefault;
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, getResources().getDisplayMetrics());
		for (int i = 0; i < answers.length; i++) {
			answerPaint[i] = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			answerPaint[i].setTextAlign(Paint.Align.CENTER);
			answerPaint[i].setColor(Color.WHITE);
			answerPaint[i].setTextSize(answerSizePix);
			bounds[i] = new Rect();
		}

		for (int i = 0; i < bmpAnswers.length; i++) {
			int id = getResources().getIdentifier("answer" + i, "drawable", ctx.getPackageName());
			bmpAnswers[i] = BitmapFactory.decodeResource(getResources(), id);
		}
		for (int i = 0; i < bmpAnswersSelected.length; i++) {
			int id = getResources().getIdentifier("answer_selected" + i, "drawable", ctx.getPackageName());
			bmpAnswersSelected[i] = BitmapFactory.decodeResource(getResources(), id);
		}
		bmpSet = BitmapFactory.decodeResource(getResources(), R.drawable.settings_background);
		bmpS = BitmapFactory.decodeResource(getResources(), R.drawable.select_s2);
		bmpQ = BitmapFactory.decodeResource(getResources(), R.drawable.select_q2);
		bmpQs = BitmapFactory.decodeResource(getResources(), R.drawable.select_q2s);
		bmpP = BitmapFactory.decodeResource(getResources(), R.drawable.select_p2);
		bmpStore = BitmapFactory.decodeResource(getResources(), R.drawable.select_store2);
		bmpI = BitmapFactory.decodeResource(getResources(), R.drawable.select_i2);

		touchX = 0;
		touchY = 0;

		rAns = Math.max(bmpAnswers[0].getWidth(), bmpAnswers[0].getHeight()) / 2;
		rBig = Math.max(bmpQ.getWidth(), bmpQ.getHeight()) / 2;
		swipeLength = rBig * 4;
		rSmall = Math.max(bmpS.getWidth(), bmpS.getHeight()) / 2;

		setDiffXY();

		srcRectForAns = new Rect(0, 0, bmpAnswers[0].getWidth(), bmpAnswers[0].getHeight());
		srcRectForBig = new Rect(0, 0, bmpQ.getWidth(), bmpQ.getHeight());
		srcRectForSmall = new Rect(0, 0, bmpS.getWidth(), bmpS.getHeight());
		srcRectForSet = new Rect(0, 0, bmpSet.getWidth(), bmpSet.getHeight());

		for (int i = 0; i < RectForAnswers.length; i++) {
			RectForAnswers[i] = new Rect();
		}
		dstRectForSet = new Rect();
		dstRectForS = new Rect();
		dstRectForQ = new Rect();
		dstRectForP = new Rect();
		dstRectForE = new Rect();
		dstRectForI = new Rect();

		// initRunnables();

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
		this.correctAnswer = maxAnswers;
		this.wrongAnswer = maxAnswers;
	}

	public void setAnswers(String answers[]) {
		switch (type) {
		case 0:
			break;
		case 1:
			this.answers = answers;
			if (measured) {
				resetTextSize();
				setDimensions();
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
			break;
		case 1:
			measured = true;
			setDimensions();
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

		dstHeight = Width * bmpSet.getHeight() / bmpSet.getWidth();

		if (options)
			setSidePaths(Height - rBig * 2 - pad);// - dstHeight + textSizePix - pad);
		else
			setSidePaths(Height - pad);

		setMeasuredDimension(Width, Height);
		initRunnables();
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
		canvas.drawBitmap(bmpI, srcRectForSmall, dstRectForI, sidePaint);

		switch (type) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			for (int i = 0; i < RectForAnswers.length; i++) {
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
			} else if (actionType == MotionEvent.ACTION_UP) {
				checkSelection(true);
				returnToDefault();
				flashText(false);
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

	@SuppressLint("NewApi")
	private void initRunnables() {
		final int answerInterval = 255 / answerFrames;
		final int textInterval = 255 / textFrames;
		// final int startInterval = -255 / startFrames;
		final int radiusInterval = (swipeLength - rSmall) / circleFrames;
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
		dstRectForI.set(selectLeft[0], temp - rBig - rSmall, selectRight[0], temp - rBig + rSmall);
	}

	private void setDimensions() {
		setLayouts();
		float maxH[] = { 0, 0, 0, 0 };
		for (int i = 0; i < answers.length; i++) {
			maxH[i] = Math.max(bounds[i].height(), answerSizePix);
			maxH[i] = Math.max(layout[i].getHeight(), maxH[i]);
			if ((maxH[i] > ((TextHeight - textSizePix) / 2 - outlineWidth * 4 - pad * 4)) && (Height > 0)) {
				decreaseTextSize();
				setDimensions();
				return;
			}
		}
		for (int i = 0; i < answers.length; i++) {
			answerPaint[i].setTextSize(answerSizePix);
		}
		invalidate();
	}

	private void setLayouts() {
		for (int i = 0; i < answers.length; i++) {
			answerPaint[i].getTextBounds(answers[i], 0, answers[i].length(), bounds[i]);
			layout[i] = new StaticLayout(answers[i], answerPaint[i], Width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
		}
		invalidate();
	}

	private void decreaseTextSize() {
		answerSizeSP = Math.max(answerSizeSP - 1, 1);
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, getResources().getDisplayMetrics());
		for (int i = 0; i < answers.length; i++) {
			answerPaint[i].setTextSize(answerSizePix);
		}
	}

	private void resetTextSize() {
		answerSizeSP = answerSizeSPDefault;
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, getResources().getDisplayMetrics());
		for (int i = 0; i < answers.length; i++) {
			answerPaint[i].setTextSize(answerSizePix);
		}
	}
}