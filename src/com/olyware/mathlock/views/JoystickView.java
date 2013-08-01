package com.olyware.mathlock.views;

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
import com.olyware.mathlock.ui.Typefaces;

public class JoystickView extends View {
	private final int NumAnswers = 5;
	private final long tapLength = 250;

	private Bitmap bmpS, bmpQ, bmpQs, bmpP, bmpStore, bmpI, bmpUnlock, bmpHand, bmpArrow;
	private Bitmap[] bmpBack = new Bitmap[3];
	private RectF RectForAnswers[] = new RectF[NumAnswers + 1];
	private RectF dstRectForSet, dstRectForOpt, RectForUnlock, RectForUnlockPulse;
	private Rect dstRectForS, dstRectForQ, dstRectForP, dstRectForE, dstRectForI;
	private Rect srcRectForBack, srcRectForUnlock, srcRectForBig, srcRectForSmall;
	private Rect bounds[] = new Rect[NumAnswers];
	private Matrix rotateHand, rotateArrow;

	private TextPaint circleTextPaint[] = new TextPaint[NumAnswers];
	private TextPaint answerTextPaint[] = new TextPaint[NumAnswers];
	private TextPaint optionPaintWhite, textPaintWhite, textPaintBlack;
	private Path optionPath;
	private Paint circlePaint[] = new Paint[NumAnswers];
	private Paint settingsPaint, optPaint, unlockPaint;
	private int textSizeSP, textSizePix, answerSizeSP, answerHintSizeSP;
	private float answerSizePix, answerHintSizePix;
	private final int answerSizeSPDefault = 40, answerHintSizeSPDefault = 30;
	private Typeface font;

	private double touchX, touchY;
	private double startX, startY;
	private float optionX, optionY;
	private int Width, Height, dstHeight, pad = 10, outlineWidth = pad / 2, strokeWidth;
	private double X[] = new double[NumAnswers];		// a=0,b=1,c=2,d=3
	private double Y[] = new double[NumAnswers];
	private final float degreeStepInitial = 1;
	private float degrees = 0, radians = 0, degreeStep = degreeStepInitial;
	private int diffX1, diffY1, diffX, diffY;
	private int spacing, rUnlock, rUnlockChange, rBig, rSmall, swipeLengthOption, swipeLength1;
	private int TextHeight;
	private long tapTimer;

	private int type = 0;
	// direction answers are going (0=up-right, 1=up-left, 2=down-left, 3=down-right)
	private int state = 0;

	private int correctLoc, correctGuess, wrongGuess;
	private String answers[] = { "N/A", "N/A", "N/A", "N/A", "?" };
	private String answerTitles[];

	private StaticLayout layout[] = new StaticLayout[NumAnswers];
	private StaticLayout layoutAnswers[] = new StaticLayout[NumAnswers];
	private EquationLayout layoutE[] = new EquationLayout[NumAnswers];
	private boolean equation[] = new boolean[NumAnswers];

	private JoystickSelectListener listener;
	private JoystickTouchListener listenerTouch;

	private boolean quizMode, quickUnlock;
	private boolean selectAnswers[] = new boolean[NumAnswers];
	private boolean selectOptions[] = new boolean[5];
	private boolean selectUnlock;
	private boolean options = false, selectSideBar = false, showHint = false;
	private boolean problem = true, wrong = false, paused = false;
	private boolean measured = false;
	private int selectLeft[] = new int[5];
	private int selectRight[] = new int[5];

	private Resources res;
	private Runnable revealText, finishText, startAnimate, finishAnimate, pulseLock, spin;
	private Runnable finishAnswer[] = new Runnable[NumAnswers];
	private Handler answerHandler, textHandler, animateHandler;
	private final int textFrames = 10, textFrameTime = 50, answerFrames = 5, answerFrameTime = 10, startFrames = 30, startFrameTime = 50,
			pulseFrames = 25, pulseFrameTime = 40, spinFrameTime = 25;

	private int pulseFrame = 0;
	private double[] sin = new double[pulseFrames];
	private Context ctx;

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
		textSizePix = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, getResources().getDisplayMetrics());
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
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, getResources().getDisplayMetrics());
		Typefaces typefaces = Typefaces.getInstance(ctx);
		font = typefaces.robotoLight;

		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i] = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			answerTextPaint[i].setTextAlign(Paint.Align.CENTER);
			answerTextPaint[i].setColor(Color.WHITE);
			answerTextPaint[i].setTextSize(answerSizePix);
			answerTextPaint[i].setTypeface(font);
			bounds[i] = new Rect();
			equation[i] = false;
			if (answers[i].charAt(0) == '$')
				if (answers[i].length() > 1)
					if (answers[i].charAt(1) != '$')
						equation[i] = true;

			layoutE[i] = new EquationLayout(answers[i], Width, Height, answerTextPaint[i], answerSizeSPDefault);
			layout[i] = new StaticLayout(answers[i], answerTextPaint[i], Width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
		}

		bmpS = BitmapFactory.decodeResource(getResources(), R.drawable.select_s2);
		bmpQ = BitmapFactory.decodeResource(getResources(), R.drawable.select_q2);
		bmpQs = BitmapFactory.decodeResource(getResources(), R.drawable.select_q2s);
		bmpP = BitmapFactory.decodeResource(getResources(), R.drawable.select_p2);
		bmpStore = BitmapFactory.decodeResource(getResources(), R.drawable.select_store2);
		bmpI = BitmapFactory.decodeResource(getResources(), R.drawable.select_i2);
		bmpUnlock = BitmapFactory.decodeResource(getResources(), R.drawable.unlock);
		bmpHand = BitmapFactory.decodeResource(getResources(), R.drawable.swipe_hand);
		bmpArrow = BitmapFactory.decodeResource(getResources(), R.drawable.swipe_arrow);
		bmpBack[0] = BitmapFactory.decodeResource(getResources(), R.drawable.gradient_background_blue);
		bmpBack[1] = BitmapFactory.decodeResource(getResources(), R.drawable.gradient_background_green);
		bmpBack[2] = BitmapFactory.decodeResource(getResources(), R.drawable.gradient_background_red);

		touchX = 0;
		touchY = 0;

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
			circlePaint[i].setAlpha(0);
			circleTextPaint[i] = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			circleTextPaint[i].setTextAlign(Paint.Align.CENTER);
			circleTextPaint[i].setColor(Color.WHITE);
			circleTextPaint[i].setTextSize(rUnlock * 1.5f);
			circleTextPaint[i].setAlpha(0);
			layoutAnswers[i] = new StaticLayout(answerTitles[i], circleTextPaint[i], rUnlock * 2, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0,
					false);
		}
		RectForAnswers[5] = new RectF();
		selectUnlock = false;

		for (int i = 0; i < pulseFrames; i++) {
			sin[i] = Math.sin(Math.PI * 2 * i / pulseFrames);
		}
		listener = new JoystickSelectListener() {
			@Override
			public void OnSelect(int s) {
			}
		};
		tapTimer = 0;
		correctLoc = 0;
		quickUnlock = false;
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

	public void setTypeface(Typeface font) {
		this.font = font;
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
		// settingsPaint.setAlpha(255);
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
		if (type == 1) {
			degreeStep = degreeStepInitial * Math.max(streak, 0);
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
		// setLayouts();
	}

	public void setIncorrectGuess(int location) {
		this.wrongGuess = location;
		// setLayouts();
	}

	public void resetGuess() {
		this.correctGuess = -1;
		this.wrongGuess = -1;
	}

	public void setAnswers(String answers[], int correctLoc) {
		this.answers = new String[] { answers[0], answers[1], answers[2], answers[3], "?" };
		this.correctLoc = correctLoc;
		this.quickUnlock = false;
		if (measured) {
			animateHandler.removeCallbacks(pulseLock);
			animateHandler.removeCallbacks(spin);
			switch (type) {
			case 1:
				animateHandler.postDelayed(spin, spinFrameTime);
			case 0:
				resetAnswerSize();
				setDimensions();
				animateHandler.postDelayed(pulseLock, pulseFrameTime);
				break;
			case 2:
				break;
			}
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
		case 1:
			animateHandler.postDelayed(spin, spinFrameTime);
		case 0:
			measured = true;
			setDimensions();
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

		if ((type == 0) || (type == 1)) {
			RectForAnswers[0].set(outlineWidth, outlineWidth, Width / 2 - outlineWidth, (TextHeight - textSizePix) / 2 - outlineWidth
					- rUnlock);
			RectForAnswers[1].set(Width / 2 + outlineWidth, outlineWidth, Width - outlineWidth, (TextHeight - textSizePix) / 2
					- outlineWidth - rUnlock);
			RectForAnswers[2].set(outlineWidth, (TextHeight - textSizePix) / 2 + outlineWidth + rUnlock, Width / 2 - outlineWidth,
					(TextHeight - textSizePix) - outlineWidth);
			RectForAnswers[3].set(Width / 2 + outlineWidth, (TextHeight - textSizePix) / 2 + outlineWidth + rUnlock, Width - outlineWidth,
					(TextHeight - textSizePix) - outlineWidth);
			RectForAnswers[4].set(Width / 4 - rUnlock * 2, (TextHeight - textSizePix) / 2 - rUnlock * 2, Width / 4 + rUnlock * 2,
					(TextHeight - textSizePix) / 2 + rUnlock * 2);
			RectForAnswers[5].set(Width * 3 / 4 - rUnlock * 2, (TextHeight - textSizePix) / 2 - rUnlock * 2, Width * 3 / 4 + rUnlock * 2,
					(TextHeight - textSizePix) / 2 + rUnlock * 2);
		}
		switch (type) {
		case 1:
			canvas.rotate(-degrees, Width / 2, (TextHeight - textSizePix) / 2);	// intentional fall through
		case 0:
			for (int i = 0; i <= NumAnswers; i++) {
				if (!quickUnlock || (i == correctLoc)) {
					if (selectAnswers[Math.min(i, NumAnswers - 1)])
						canvas.drawBitmap(bmpBack[0], srcRectForBack, RectForAnswers[i], unlockPaint);
					else if (i == correctGuess)
						canvas.drawBitmap(bmpBack[1], srcRectForBack, RectForAnswers[i], unlockPaint);
					else if (i == wrongGuess)
						canvas.drawBitmap(bmpBack[2], srcRectForBack, RectForAnswers[i], unlockPaint);
				}
			}
			for (int i = 0; i <= NumAnswers; i++) {
				if (!quickUnlock || (i == correctLoc)) {
					canvas.save();
					if (equation[Math.min(i, NumAnswers - 1)]) {
						// position the text then draw the layout
						canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2,
								(RectForAnswers[i].top + RectForAnswers[i].bottom) / 2);
						layoutE[Math.min(i, NumAnswers - 1)].draw(canvas);
					} else {
						// position the text then draw the layout
						canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2,
								(RectForAnswers[i].top + RectForAnswers[i].bottom) / 2 - layout[Math.min(i, NumAnswers - 1)].getHeight()
										/ 2);
						layout[Math.min(i, NumAnswers - 1)].draw(canvas);
					}
					canvas.restore();
				}
			}
			if (type == 1)
				canvas.rotate(degrees, Width / 2, (TextHeight - textSizePix) / 2);
			canvas.drawRoundRect(RectForUnlockPulse, (RectForUnlockPulse.right - RectForUnlockPulse.left) / 2f,
					(RectForUnlockPulse.bottom - RectForUnlockPulse.top) / 2f, unlockPaint);
			canvas.drawBitmap(bmpUnlock, srcRectForUnlock, RectForUnlock, unlockPaint);
			break;
		case 2:
			for (int i = 0; i < X.length; i++) {
				if (!quickUnlock || (i == correctLoc)) {
					RectForAnswers[i].set((int) X[i] - rUnlock + strokeWidth / 2, (int) Y[i] - rUnlock + strokeWidth / 2, (int) X[i]
							+ rUnlock - strokeWidth / 2, (int) Y[i] + rUnlock - strokeWidth / 2);

					canvas.drawRoundRect(RectForAnswers[i], rUnlock, rUnlock, circlePaint[i]);
					canvas.save();
					canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2,
							(RectForAnswers[i].top + RectForAnswers[i].bottom) / 2 - layoutAnswers[i].getHeight() / 2); // position the text
					layoutAnswers[i].draw(canvas);
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
		// canvas.drawRect(0, 0, Width, Height, unlockPaint);
		canvas.drawRoundRect(dstRectForSet, textSizePix, textSizePix, settingsPaint);
		if ((selectOptions[0]) || (selectOptions[1]) || (selectOptions[2]) || (selectOptions[3]) || (selectOptions[4])) {
			canvas.drawRect(dstRectForOpt, optPaint);
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
		canvas.save();
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
						listener.OnSelect(-1);		// send a vibrate signal
				} else if (startY >= TextHeight - textSizePix * 2) {	// SettingsBar selected
					animateHandler.removeCallbacks(startAnimate);
					animateHandler.removeCallbacks(finishAnimate);
					// settingsPaint.setAlpha(255);
					selectSideBar = true;
					invalidate();
				} else if (!problem) {				// if there is no problem set
					if (listener != null)
						listener.OnSelect(0);		// select A on any touch event
				} else {							// touch was in the main control window
					switch (type) {
					case 0:
						checkSelection(false, true);
						break;
					case 1:
						checkSelection(false, true);
						break;
					case 2:
						if ((tapTimer + tapLength) > System.currentTimeMillis()) {
							quickUnlock = true;
							listener.OnSelect(11);		// send a quickUnlock mode activated signal back to mainActivity
						}
						setAnswerLocations();
						revealAnswers();
						if (listener != null)
							listener.OnSelect(-1);		// send a vibrate signal
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
				int centerY = rUnlock * 2;
				if (startX <= Width / 2) {
					centerX = Width - (int) (rUnlock * (Math.sqrt(3) + 1));
					X[4] = centerX + (int) (rUnlock * Math.sqrt(3));
					X[0] = centerX - (int) (rUnlock * Math.sqrt(3));
				} else {
					centerX = (int) (rUnlock * (Math.sqrt(3) + 1));
					X[4] = centerX - (int) (rUnlock * Math.sqrt(3));
					X[0] = centerX + (int) (rUnlock * Math.sqrt(3));
				}
				Y[0] = centerY;
				X[1] = centerX;
				Y[1] = centerY - rUnlock;
				X[2] = X[4];
				Y[2] = centerY;
				X[3] = centerX;
				Y[3] = centerY + rUnlock;
				Y[4] = centerY + rUnlock * 2;
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
			diffX = (int) (rUnlock * Math.sqrt(3));
			diffY = -rUnlock;
			break;
		case 1:		// up-left
			diffX = (int) (-rUnlock * Math.sqrt(3));
			diffY = -rUnlock;
			break;
		case 2:		// down-left
			diffX = (int) (-rUnlock * Math.sqrt(3));
			diffY = rUnlock;
			break;
		case 3:		// down-right
			diffX = (int) (rUnlock * Math.sqrt(3));
			diffY = rUnlock;
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
			if (Y[loc] - rUnlock < 0) {				// above top boundary
				checks += 1;
				state = ((state - 1) % 4 + 4) % 4;	// state-> 3=down-right
			}
			if (X[loc] + rUnlock > Width) {			// right of right boundary
				checks += 1;
				state = (state + checks) % 4;		// state-> 1=up-left or 2=down-left
			}
			break;
		case 1:		// up-left
			if (Y[loc] - rUnlock < 0) {		// above top boundary
				checks += 1;
				state = (state + 1) % 4;
			}
			if (X[loc] - rUnlock < 0) {		// left of left boundary
				checks += 1;
				state = ((state - checks) % 4 + 4) % 4;
			}
			break;
		case 2:		// down-left
			if (Y[loc] + rUnlock > (Height - rBig * 2)) {		// below bottom boundary
				checks += 1;
				state = ((state - 1) % 4 + 4) % 4;
			}
			if (X[loc] - rUnlock < 0) {		// left of left boundary
				checks += 1;
				state = (state + checks) % 4;
			}
			break;
		case 3:		// down-right
			if (Y[loc] + rUnlock > (Height - rBig * 2)) {		// below bottom boundary
				checks += 1;
				state = (state + 1) % 4;
			}
			if (X[loc] + rUnlock > Width) {		// right of right boundary
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
			answerHandler.removeCallbacks(finishAnswer[ans]);
		}

		for (int ans = 0; ans < X.length; ans++) {
			circlePaint[ans].setAlpha(0);
			circleTextPaint[ans].setAlpha(0);
		}

		for (int ans = 0; ans < X.length; ans++) {
			answerHandler.postDelayed(finishAnswer[ans], answerFrames * answerFrameTime * (ans + 1));
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
		selectUnlock = false;

		if (options)
			showStartAnimation(0, 3000);
		else if (selectSideBar)
			showStartAnimation(1, 0);
		else {
			int currentRadius = (int) ((RectForUnlockPulse.right - RectForUnlockPulse.left) / 2);
			RectForUnlockPulse.set(Width / 2 - currentRadius, (TextHeight - textSizePix) / 2 - currentRadius, Width / 2 + currentRadius,
					(TextHeight - textSizePix) / 2 + currentRadius);
			RectForUnlock.set(Width / 2 - rUnlock, (TextHeight - textSizePix) / 2 - rUnlock, Width / 2 + rUnlock,
					(TextHeight - textSizePix) / 2 + rUnlock);
		}
		selectSideBar = false;

		animateHandler.removeCallbacks(pulseLock);
		for (int ans = 0; ans < X.length; ans++) {
			answerHandler.removeCallbacks(finishAnswer[ans]);
			X[ans] = -rUnlock;
			Y[ans] = -rUnlock;
			circlePaint[ans].setAlpha(0);
			circleTextPaint[ans].setAlpha(0);
		}
		switch (type) {
		case 0:
		case 1:
			// circlePaint[0].setAlpha(255);
			animateHandler.postDelayed(pulseLock, pulseFrameTime);
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
				listener.OnSelect(s);
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

				if (selectUnlock) {
					diffx = touchX - startX;
					diffy = touchY - startY;
					double newX = touchX, newY = touchY;
					if (type == 1) {
						double oldX = touchX - Width / 2;
						double oldY = (TextHeight - textSizePix) / 2 - touchY;
						newX = (oldX * Math.cos(-radians) - oldY * Math.sin(-radians)) + Width / 2;
						newY = (TextHeight - textSizePix) / 2 - (oldX * Math.sin(-radians) + oldY * Math.cos(-radians));
					}
					RectForUnlockPulse.set((int) touchX - rUnlock + strokeWidth / 2, (int) touchY - rUnlock + strokeWidth / 2, (int) touchX
							+ rUnlock - strokeWidth / 2, (int) touchY + rUnlock - strokeWidth / 2);
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

						if (select >= 0)
							if (send && (!quickUnlock || select == correctLoc))
								listener.OnSelect(select);
							else {
								circlePaint[select].setColor(Color.BLUE);
								circleTextPaint[select].setColor(Color.BLUE);
								selectAnswers[select] = true;
							}
					}
				} else {
					touchX = startX;
					touchY = startY;
					diffx = startX - (RectForUnlock.right + RectForUnlock.left) / 2;
					diffy = startY - (RectForUnlock.top + RectForUnlock.bottom) / 2;
					if (Math.sqrt(diffx * diffx + diffy * diffy) < rUnlock * 1.5) {
						if ((tapTimer + tapLength) > System.currentTimeMillis()) {
							quickUnlock = true;
							listener.OnSelect(11);		// send a quickUnlock mode activated signal back to mainActivity
						}
						startX = (RectForUnlock.right + RectForUnlock.left) / 2;
						startY = (RectForUnlock.top + RectForUnlock.bottom) / 2;
						RectForUnlockPulse.set((int) touchX - rUnlock + strokeWidth / 2, (int) touchY - rUnlock + strokeWidth / 2,
								(int) touchX + rUnlock - strokeWidth / 2, (int) touchY + rUnlock - strokeWidth / 2);
						RectForUnlock.set((int) touchX - rUnlock, (int) touchY - rUnlock, (int) touchX + rUnlock, (int) touchY + rUnlock);

						selectUnlock = true;
						animateHandler.removeCallbacks(pulseLock);
						if (listener != null)
							listener.OnSelect(-1);		// send a vibrate signal
					} else if (firstTouch)
						listener.OnSelect(10);		// send a missed the lock signal
				}
				break;
			case 2:
				for (int i = 0; i < X.length; i++) {
					diffx = touchX - X[i];
					diffy = touchY - Y[i];
					if (Math.sqrt(diffx * diffx + diffy * diffy) < rUnlock)
						if (send) {
							circlePaint[i].setColor(Color.WHITE);
							circleTextPaint[i].setColor(Color.WHITE);
							selectAnswers[i] = false;
							listener.OnSelect(i);
						} else {
							circlePaint[i].setColor(Color.BLUE);
							circleTextPaint[i].setColor(Color.BLUE);
							selectAnswers[i] = true;
						}
					else {
						circlePaint[i].setColor(Color.WHITE);
						circleTextPaint[i].setColor(Color.WHITE);
						selectAnswers[i] = false;
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
		// final int answerInterval = 255 / answerFrames;
		final int textInterval = 255 / textFrames;
		// final int startInterval = -255 / startFrames;
		final int slideInterval = (dstHeight - pad) / startFrames;
		// final int pulseMaxChange = rUnlockChange;

		startAnimate = new Runnable() {
			@Override
			public void run() {
				options = false;
				// settingsPaint.setAlpha(settingsPaint.getAlpha() + startInterval);
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
				// settingsPaint.setAlpha(0);
				setSidePaths(Height - pad);
				options = false;
				invalidate();
			}
		};

		for (int i = 0; i < X.length; i++) {
			final int a = i;
			finishAnswer[a] = new Runnable() {
				@Override
				public void run() {
					circlePaint[a].setAlpha(255);
					circleTextPaint[a].setAlpha(255);
					invalidate();
				}
			};
		}
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
				// circlePaint[0].setAlpha(255);
				if (!selectUnlock) {
					int size = (int) ((RectForUnlockPulse.right - RectForUnlockPulse.left) / 2);
					pulseFrame = (int) (System.currentTimeMillis() % (pulseFrameTime * pulseFrames) / pulseFrameTime);
					int change = (int) (rUnlockChange * sin[pulseFrame] + rUnlock - size);
					RectForUnlockPulse.set(RectForUnlockPulse.left - change + strokeWidth / 2, RectForUnlockPulse.top - change
							+ strokeWidth / 2, RectForUnlockPulse.right + change - strokeWidth / 2, RectForUnlockPulse.bottom + change
							- strokeWidth / 2);
				}
				invalidate();
				animateHandler.postDelayed(pulseLock, pulseFrameTime);
			}
		};
		spin = new Runnable() {
			@Override
			public void run() {
				degrees = (degrees + degreeStep) % 360;
				radians = (float) (degrees * Math.PI / 180);
				checkSelection(false, false);
				invalidate();
				animateHandler.postDelayed(spin, spinFrameTime);
			}
		};
	}

	private void setSidePaths(int side) {
		TextHeight = side;
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
			RectForUnlockPulse.set(Width / 2 - currentRadius, (TextHeight - textSizePix) / 2 - currentRadius, Width / 2 + currentRadius,
					(TextHeight - textSizePix) / 2 + currentRadius);
			RectForUnlock.set(Width / 2 - rUnlock, (TextHeight - textSizePix) / 2 - rUnlock, Width / 2 + rUnlock,
					(TextHeight - textSizePix) / 2 + rUnlock);
		}

	}

	private void setHintDimensions() {
		setHintLayouts();
		float maxH = 0;
		maxH = Math.max(bounds[NumAnswers - 1].height(), layout[NumAnswers - 1].getHeight());
		maxH = Math.max(maxH, answerHintSizePix);
		if ((maxH > (rUnlock * 2 - outlineWidth * 2 - pad * 2)) && (Height > 0)) {
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
				if ((maxH > ((TextHeight - textSizePix) / 2 - outlineWidth * 3 - pad * 3 - rUnlock)) && (Height > 0)) {
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
		layout[NumAnswers - 1] = new StaticLayout(answers[NumAnswers - 1], answerTextPaint[NumAnswers - 1], Width / 2 - outlineWidth * 2
				- pad * 2 - (rUnlock + rUnlockChange) * 2, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
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
				int W = Width / 2 - outlineWidth * 2 - pad * 2;
				int H = (Height - rBig * 2 - pad - textSizePix) / 2 - outlineWidth * 3 - pad * 3 - rUnlock;
				if (!layoutE[i].isComputed(answers[i], W, H)) {
					layoutE[i] = new EquationLayout(answers[i], W, H, answerTextPaint[i], answerSizeSP);
					if (layoutE[i].getTextSizePix() < answerSizePix)
						changeAnswerSize(layoutE[i].getTextSizeSP(), layoutE[i].getTextSizePix());
				}
			} else
				layout[i] = new StaticLayout(answers[i], answerTextPaint[i], Width / 2 - outlineWidth * 2 - pad * 2,
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

	private void changeAnswerSize(int SP, float Pix) {
		answerSizeSP = SP;
		answerSizePix = Pix;
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
		RectForUnlockPulse.set(Width / 2 - rUnlock + strokeWidth / 2, (TextHeight - textSizePix) / 2 - rUnlock + strokeWidth / 2, Width / 2
				+ rUnlock - strokeWidth / 2, (TextHeight - textSizePix) / 2 + rUnlock - strokeWidth / 2);
	}

	private void setArc(int startDeg, int totalDeg) {
		optionPath.reset();
		// optionPath.moveTo(optionX - swipeLengthOption, optionY);
		optionPath.arcTo(new RectF(optionX - swipeLengthOption, optionY - swipeLengthOption, optionX + swipeLengthOption, optionY
				+ swipeLengthOption), startDeg, totalDeg);
	}
}