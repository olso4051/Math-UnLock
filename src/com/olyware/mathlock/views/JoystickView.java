package com.olyware.mathlock.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.olyware.mathlock.R;

public class JoystickView extends View {
	public static int IN_OUT_DURATION = 250;
	private final int pad = 5, NumAnswers = 4, NumOptions = 5, answerSizeSPDefault = 30, startFrames = 30, startFrameTime = 50,
			frameTimeReveal = 10;
	private final long tapLength = 250;

	private Bitmap bmpSelectBar, bmpS, bmpQ, bmpQs, bmpP, bmpStore, bmpFriend, bmpUnlock, bmpHand, bmpArrow;
	private Bitmap[] bmpBack = new Bitmap[3];
	private RectF[] RectForAnswers = new RectF[NumAnswers];
	private Rect[] bounds = new Rect[NumAnswers];
	private RectF dstRectForOpt, RectForUnlock, RectForUnlockPulse;
	private Rect dstRectForS, dstRectForQ, dstRectForP, dstRectForE, dstRectForI, srcRectForBack, srcRectForUnlock, srcRectForBig,
			srcRectForSmall;
	private Matrix rotateHand, rotateArrow;

	private TextPaint[] answerTextPaint = new TextPaint[NumAnswers];
	private TextPaint optionPaintWhite;
	private Path optionPath, dstPathForSet;
	private Paint settingsPaint, settingsPaintOutline, optPaint, unlockPaint;

	private int barY, barHeight, centerOffset, textSizeSP, textSizePix, answerSizeSP, Width, Height, dstHeight, spacing, rUnlock,
			radiusOfSettingsIcons, rApps, swipeLengthOption, swipeLength1, correctLoc, shareLoc, correctGuess, wrongGuess, selectAppDrag,
			appCenterVert, appCenterHorz, answerAlpha = 0;
	private long tapTimer, lastTimeRevealOrHide = 0, startTimeRevealOrHide = 0;
	private float answerSizePix, optionX, optionY, appDragX = 0, appDragY = 0, strokeWidth;
	private double touchX, touchY, startX, startY, appAngle;
	private boolean quizMode, quickUnlock, selectUnlock, options = false, selectSideBar = false, showHint = false, problem = true,
			paused = false, measured = false, isFirstApp = false;
	private String shareOldAnswer;

	private int[] selectLeft = new int[5], selectRight = new int[5];
	private double[] X = new double[NumAnswers], Y = new double[NumAnswers];
	private boolean[] selectAnswers = new boolean[NumAnswers], selectOptions = new boolean[NumOptions];
	private String[] answers = { " ", " ", " ", " ", "?" };

	private StaticLayout[] layout = new StaticLayout[NumAnswers];
	private EquationLayout[] layoutE = new EquationLayout[NumAnswers];
	private boolean[] equation = new boolean[NumAnswers];

	private JoystickSelectListener listener;

	private Runnable startAnimate, finishAnimate, revealAnswers, hideAnswers;
	private Handler animateHandler;

	private List<Drawable> d;
	private List<App> apps;
	private Drawable test, drawAdd, drawTrash, drawBackBlue, drawBackRed;
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
		optPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		optPaint.setStyle(Paint.Style.FILL);

		optionPath = new Path();
		dstPathForSet = new Path();
		options = false;

		answerSizeSP = answerSizeSPDefault;
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, res.getDisplayMetrics());

		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i] = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			answerTextPaint[i].setTextAlign(Paint.Align.CENTER);
			answerTextPaint[i].setColor(Color.WHITE);
			answerTextPaint[i].setTextSize(answerSizePix);
			answerTextPaint[i].setAlpha(answerAlpha);
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
		bmpUnlock = BitmapFactory.decodeResource(res, R.drawable.unlock);
		// bmpHand = BitmapFactory.decodeResource(res, R.drawable.swipe_hand);
		// bmpArrow = BitmapFactory.decodeResource(res, R.drawable.swipe_arrow);
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
		radiusOfSettingsIcons = Math.max(bmpQ.getWidth(), bmpQ.getHeight()) / 2;
		rUnlock = radiusOfSettingsIcons;
		swipeLengthOption = radiusOfSettingsIcons * 4;
		swipeLength1 = rUnlock * 2;

		srcRectForBack = new Rect(0, 0, bmpBack[0].getWidth(), bmpBack[0].getHeight());
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

		rotateHand = new Matrix();
		rotateArrow = new Matrix();

		strokeWidth = rUnlock / 10;
		unlockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		unlockPaint.setColor(Color.WHITE);
		unlockPaint.setStyle(Paint.Style.STROKE);
		unlockPaint.setStrokeWidth(strokeWidth);
		settingsPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
		settingsPaintOutline.setColor(Color.WHITE);
		settingsPaintOutline.setStyle(Paint.Style.STROKE);
		settingsPaintOutline.setStrokeWidth(rUnlock / 20);
		for (int i = 0; i < NumAnswers; i++) {
			selectAnswers[i] = false;
			RectForAnswers[i] = new RectF();
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

	public boolean getQuickUnlock() {
		return quickUnlock;
	}

	public int getHeightToLock(int padding) {
		int bottom = (barY - barHeight) / 2 + rUnlock + padding;
		return (Height < bottom) ? Height : bottom;
	}

	public void setOnJostickSelectedListener(JoystickSelectListener listener) {
		this.listener = listener;
	}

	public void addApp(Drawable icon) {
		if (d.size() == apps.size()) {
			isFirstApp = true;
		}
		icon.setBounds(-icon.getIntrinsicWidth() / 2, -icon.getIntrinsicHeight() / 2, icon.getIntrinsicWidth() / 2,
				icon.getIntrinsicHeight() / 2);
		d.add(d.size() - 1, icon);
		invalidate();
	}

	public void removeApp(int loc) {
		d.remove(loc);
		if (isFirstApp) {
			isFirstApp = false;
		}
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
	}

	public boolean setQuizMode(boolean quizMode) {
		this.quizMode = quizMode;
		return this.quizMode;
	}

	public void showHint(int hint) {
		showHint = true;
		if (bmpHand == null)
			bmpHand = BitmapFactory.decodeResource(res, R.drawable.swipe_hand);
		if (bmpArrow == null)
			bmpArrow = BitmapFactory.decodeResource(res, R.drawable.swipe_arrow);
		boolean mirror = false;
		int centerVert = 0, centerHorz = 0, handRotation = 0, arrowRotation = 0;
		switch (hint) {
		case 0:
			centerVert = (int) ((RectForUnlock.bottom + RectForUnlock.top) / 2);
			centerHorz = (int) ((RectForUnlock.left + RectForUnlock.right) / 2);
			arrowRotation = 225;
			break;
		case 1:
			setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
			centerVert = (int) ((dstRectForQ.bottom + dstRectForQ.top) / 2);
			centerHorz = (int) ((dstRectForQ.left + dstRectForQ.right) / 2);
			handRotation = 270;
			break;
		case 2:
			setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
			centerVert = (int) ((dstRectForS.bottom + dstRectForS.top) / 2);
			centerHorz = (int) ((dstRectForS.left + dstRectForS.right) / 2);
			handRotation = 270;
			break;
		case 3:
			setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
			centerVert = (int) ((dstRectForP.bottom + dstRectForP.top) / 2);
			centerHorz = (int) ((dstRectForP.left + dstRectForP.right) / 2);
			handRotation = 270;
			break;
		case 4:
			mirror = true;
			setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
			centerVert = (int) ((dstRectForE.bottom + dstRectForE.top) / 2);
			centerHorz = (int) ((dstRectForE.left + dstRectForE.right) / 2);
			handRotation = 90;
			break;
		case 5:
			mirror = true;
			setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
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
			setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
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
	}

	public void setAnswers(String answers[], int correctLoc) {
		setAnswers(answers, correctLoc, -1);
	}

	public void setAnswers(String answers[], int correctLoc, int shareLoc) {
		this.answers = new String[] { answers[0], answers[1], answers[2], answers[3], res.getString(R.string.unknown) };
		this.correctLoc = correctLoc;
		this.shareLoc = shareLoc;
		this.quickUnlock = false;
		centerOffset = 0;
		if (measured) {
			resetAnswerSize();
			setDimensions();
			int centerX = Width / 2;
			int bottom = barY - barHeight;
			int centerY = bottom / 2;
			RectForUnlockPulse.set(centerX - rUnlock, centerY - rUnlock, centerX + rUnlock, centerY + rUnlock);
			RectForUnlock.set(centerX - rUnlock, centerY - rUnlock, centerX + rUnlock, centerY + rUnlock);
			RectForAnswers[0].set(0, 0, Width / 2, centerY - rUnlock);
			RectForAnswers[1].set(Width / 2, 0, Width, centerY - rUnlock);
			RectForAnswers[2].set(0, centerY + rUnlock, Width / 2, bottom);
			RectForAnswers[3].set(Width / 2, centerY + rUnlock, Width, bottom);
		}
	}

	public void askToShare() {
		if (shareLoc < 0) {
			Random rand = new Random();
			int loc = rand.nextInt(3);
			if (loc >= correctLoc) {
				loc++;
			}
			shareLoc = loc;
			shareOldAnswer = answers[loc];
			answers[loc] = res.getString(R.string.ask_to_share0);
			setAnswers(answers, correctLoc, shareLoc);
		}
	}

	public void resetAskToShare() {
		if (shareLoc >= 0) {
			answers[shareLoc] = shareOldAnswer;
			setAnswers(answers, correctLoc);
		}
	}

	public void moveCorrect(int loc) {
		shareLoc = -1;
		if (correctLoc != loc) {
			String temp = answers[loc];
			answers[loc] = answers[correctLoc];
			answers[correctLoc] = temp;
			correctLoc = loc;
			setDimensions();
		}
	}

	public void startAnimations() {
		animateHandler.removeCallbacksAndMessages(null);
		animateHandler.post(finishAnimate);
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

		if (options)
			setSidePaths(Height - radiusOfSettingsIcons * 2 - pad * 3);
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
		for (int i = 0; i < NumAnswers; i++) {
			if (!quickUnlock || (i == correctLoc)) {
				canvas.save();
				// Draw the background for selected answers
				unlockPaint.setAlpha(answerAlpha);
				if (selectAnswers[i])
					canvas.drawBitmap(bmpBack[0], srcRectForBack, RectForAnswers[i], unlockPaint);
				else if (i == correctGuess)
					canvas.drawBitmap(bmpBack[1], srcRectForBack, RectForAnswers[i], unlockPaint);
				else if (i == wrongGuess)
					canvas.drawBitmap(bmpBack[2], srcRectForBack, RectForAnswers[i], unlockPaint);

				// position the text then draw the layout
				if (equation[i]) {
					canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2,
							(RectForAnswers[i].top + RectForAnswers[i].bottom) / 2);
					layoutE[i].setAlpha(answerAlpha);
					layoutE[i].draw(canvas);
				} else {
					canvas.translate((RectForAnswers[i].left + RectForAnswers[i].right) / 2,
							(RectForAnswers[i].top + RectForAnswers[i].bottom) / 2 - layout[i].getHeight() / 2);
					layout[i].draw(canvas);
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
					d.get(i).setAlpha(answerAlpha);
					d.get(i).draw(canvas);
					canvas.restore();
				}
			}
			if (selectAppDrag >= 0) {
				canvas.save();
				canvas.translate(apps.get(selectAppDrag).getX() + appDragX, apps.get(selectAppDrag).getY() + appDragY);
				d.get(selectAppDrag + start).setAlpha(answerAlpha);
				d.get(selectAppDrag + start).draw(canvas);
				canvas.restore();
			}
		}
		unlockPaint.setAlpha(255);
		canvas.drawRoundRect(RectForUnlockPulse, (RectForUnlockPulse.right - RectForUnlockPulse.left) / 2f,
				(RectForUnlockPulse.bottom - RectForUnlockPulse.top) / 2f, unlockPaint);
		canvas.drawBitmap(bmpUnlock, srcRectForUnlock, RectForUnlock, unlockPaint);

		// Draw the option bar text hints
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

		// Draw the settings bar background
		canvas.drawPath(dstPathForSet, settingsPaint);
		canvas.save();
		canvas.translate(Width / 2, barY - barHeight / 2 + pad);
		test.draw(canvas);
		canvas.restore();

		// Draw the settings icons
		canvas.drawBitmap(bmpS, srcRectForSmall, dstRectForS, optPaint);
		if (quizMode)
			canvas.drawBitmap(bmpQs, srcRectForBig, dstRectForQ, optPaint);
		else
			canvas.drawBitmap(bmpQ, srcRectForBig, dstRectForQ, optPaint);
		canvas.drawBitmap(bmpP, srcRectForBig, dstRectForP, optPaint);
		canvas.drawBitmap(bmpStore, srcRectForBig, dstRectForE, optPaint);
		canvas.drawBitmap(bmpFriend, srcRectForBig, dstRectForI, optPaint);

		// Draw hints
		if (showHint) {
			canvas.drawBitmap(bmpArrow, rotateArrow, optPaint);
			canvas.drawBitmap(bmpHand, rotateHand, optPaint);
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
					touchX = startX;
					touchY = startY;
					if ((startX >= selectLeft[0]) && (startX <= selectRight[0])) {			// info selected
						selectOptions[0] = true;
						optionX = selectLeft[0] + radiusOfSettingsIcons;
						setArc(180, 150);
					} else if ((startX >= selectLeft[1]) && (startX <= selectRight[1])) {	// store selected
						selectOptions[1] = true;
						optionX = selectLeft[1] + radiusOfSettingsIcons;
						setArc(180, 180);
					} else if ((startX >= selectLeft[2]) && (startX <= selectRight[2])) {	// progress selected
						selectOptions[2] = true;
						optionX = selectLeft[2] + radiusOfSettingsIcons;
						setArc(180, 180);
					} else if ((startX >= selectLeft[3]) && (startX <= selectRight[3])) {	// quiz mode selected
						selectOptions[3] = true;
						optionX = selectLeft[3] + radiusOfSettingsIcons;
						setArc(180, 180);
					} else if ((startX >= selectLeft[4]) && (startX <= selectRight[4])) {	// settings selected
						selectOptions[4] = true;
						optionX = selectLeft[4] + radiusOfSettingsIcons;
						setArc(210, 150);
					} else
						return true;
					if (listener != null)
						listener.OnSelect(JoystickSelect.Vibrate, true, -1);				// send a vibrate signal
				} else if (startY >= barY - barHeight * 2) {	// SettingsBar selected
					animateHandler.removeCallbacks(startAnimate);
					animateHandler.removeCallbacks(finishAnimate);
					selectSideBar = true;
					invalidate();
				} else if (!problem) {										// if there is no problem set
					if (listener != null)
						listener.OnSelect(JoystickSelect.A, true, -1);		// select A on any touch event
				} else {													// touch was in the main control window
					checkSelection(false, true);
				}
			} else if (actionType == MotionEvent.ACTION_MOVE) {
				touchX = event.getX();
				touchY = event.getY();
				checkSelection(false, false);
				invalidate();
			} else if (actionType == MotionEvent.ACTION_UP) {
				Log.d("test", "action_up");
				tapTimer = System.currentTimeMillis();
				checkSelection(true, false);
				returnToDefault();
			}

		} else if (actionType == MotionEvent.ACTION_UP) {
			if (listener != null && paused)
				listener.OnSelect(JoystickSelect.Touch, false, -1);
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
		int maxApps = (int) Math.floor((3 * Math.PI / 2) / appAngle);
		if (oldMaxApps < maxApps) {
			apps.clear();
			for (int i = 0; i < maxApps; i++) {
				double angle = Math.PI - appAngle / 2 - appAngle * i;
				apps.add(new App(angle, (float) (appCenterHorz + Math.cos(angle) * rAppsX), (float) (appCenterVert - Math.sin(angle)
						* rAppsY), rApps));
			}
		} else {
			for (int i = 0; i < apps.size(); i++) {
				double angle = Math.PI - appAngle / 2 - appAngle * i;
				apps.get(i).setAll(angle, (float) (appCenterHorz + Math.cos(angle) * rAppsX),
						(float) (appCenterVert - Math.sin(angle) * rAppsY), rApps);
				if (!sel) {
					apps.get(i).setSelect(false);
					apps.get(i).setSelectDrag(false);
				}
			}
		}
	}

	private void returnToDefault() {
		touchX = 0;
		touchY = 0;
		startX = -rUnlock;
		startY = -rUnlock;
		for (int ans = 0; ans < X.length; ans++) {
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

		Log.d("test", "returnToDefault() - options = " + options);
		Log.d("test", "returnToDefault() - selectSideBar = " + selectSideBar);
		if (options)
			showStartAnimation(0, 3000);
		else if (selectSideBar)
			showStartAnimation(1, 0);
		else {
			int currentRadius = (int) ((RectForUnlockPulse.right - RectForUnlockPulse.left) / 2);
			RectForUnlockPulse.set(Width / 2 - currentRadius, (barY - barHeight + centerOffset) / 2 - currentRadius, Width / 2
					+ currentRadius, (barY - barHeight + centerOffset) / 2 + currentRadius);
			RectForUnlock.set(Width / 2 - rUnlock, (barY - barHeight + centerOffset) / 2 - rUnlock, Width / 2 + rUnlock,
					(barY - barHeight + centerOffset) / 2 + rUnlock);
		}
		selectSideBar = false;

		for (int ans = 0; ans < X.length; ans++) {
			X[ans] = -rApps;
			Y[ans] = -rApps;
		}

		if (listener != null)
			listener.OnSelect(JoystickSelect.ReturnToDefault, false, -1);
		startTimeRevealOrHide = 0;
		animateHandler.removeCallbacks(hideAnswers);
		// animateHandler.removeCallbacks(revealAnswers);
		animateHandler.postDelayed(hideAnswers, tapLength);
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

					if (newX < RectForAnswers[0].right) {
						if (newY < RectForAnswers[0].bottom)
							select = 0;
						else if (newY > RectForAnswers[2].top)
							select = 2;
					} else if (newX > RectForAnswers[1].left) {
						if (newY < RectForAnswers[1].bottom)
							select = 1;
						else if (newY > RectForAnswers[3].top)
							select = 3;
					}

					if (quickUnlock) {
						if (select == correctLoc) {
							selectAnswers[select] = true;
							if (send)
								listener.OnSelect(JoystickSelect.fromValue(select), true, -1);
						} else {
							double angle = Math.atan2(appCenterVert - newY, newX - appCenterHorz);
							if (angle < 0)// because atan2 returns from -pi to pi, we want 0 to 2pi
								angle += 2 * Math.PI;
							int selection = -1;
							for (int i = 0; i < apps.size(); i++) {
								double angleDiff = Math.abs(apps.get(i).getAngle() - angle);
								if (angleDiff < appAngle / 2) {
									selection = i;
									break;
								}
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
								listener.OnSelect(JoystickSelect.Share, true, 0);
							} else
								listener.OnSelect(JoystickSelect.fromValue(select), true, 0);
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
						quickUnlock = true;
						centerOffset = drawBackBlue.getIntrinsicHeight() / 3;
						listener.OnSelect(JoystickSelect.QuickUnlock, false, -1);		// send a quickUnlock mode activated signal back to
						// mainActivity
					}
					startX = (RectForUnlock.right + RectForUnlock.left) / 2;
					startY = (RectForUnlock.top + RectForUnlock.bottom) / 2;
					RectForUnlockPulse.set((int) touchX - rUnlock, (int) touchY - rUnlock, (int) touchX + rUnlock, (int) touchY + rUnlock);
					RectForUnlock.set((int) touchX - rUnlock, (int) touchY - rUnlock, (int) touchX + rUnlock, (int) touchY + rUnlock);

					selectUnlock = true;
					if (listener != null)
						listener.OnSelect(JoystickSelect.SelectLock, true, -1);		// send a vibrate signal
					startTimeRevealOrHide = System.currentTimeMillis();
					animateHandler.removeCallbacks(hideAnswers);
					animateHandler.removeCallbacks(revealAnswers);
					animateHandler.post(revealAnswers);
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
				options = false;
				if (barY + slideInterval > Height - pad) {
					setSidePaths(Height - pad);
				} else {
					setSidePaths(barY + slideInterval);
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
				lastTimeRevealOrHide = System.currentTimeMillis();
				float percent = (lastTimeRevealOrHide - startTimeRevealOrHide) / (float) IN_OUT_DURATION;
				if (percent <= 1) {
					answerAlpha = Math.max(answerAlpha, (int) (percent * 255));
					setAlpha(answerAlpha);
					invalidate();
					animateHandler.postDelayed(revealAnswers, frameTimeReveal);
				} else {
					answerAlpha = 255;
					setAlpha(answerAlpha);
					invalidate();
				}
			}
		};
		hideAnswers = new Runnable() {
			@Override
			public void run() {
				if (startTimeRevealOrHide == 0)
					startTimeRevealOrHide = System.currentTimeMillis();
				lastTimeRevealOrHide = System.currentTimeMillis();
				float percent = (lastTimeRevealOrHide - startTimeRevealOrHide) / (float) IN_OUT_DURATION;
				if (percent <= 1) {
					answerAlpha = Math.min(answerAlpha, (int) ((1 - percent) * 255));
					setAlpha(answerAlpha);
					invalidate();
					animateHandler.postDelayed(hideAnswers, frameTimeReveal);
				} else {
					answerAlpha = 0;
					setAlpha(answerAlpha);
					invalidate();
				}
			}
		};
	}

	private void setSidePaths(int side) {
		barY = side;
		int middle = (barY - barHeight + centerOffset) / 2;
		int bottom = barY - barHeight;
		RectForAnswers[0].set(0, 0, Width / 2, middle - rUnlock);
		RectForAnswers[1].set(Width / 2, 0, Width, middle - rUnlock);
		RectForAnswers[2].set(0, middle + rUnlock, Width / 2, bottom);
		RectForAnswers[3].set(Width / 2, middle + rUnlock, Width, bottom);
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
		// dstRectForSet.set(spacing / 2, barY - barHeight, Width - spacing / 2, Height + barHeight);
		// settingsPaint.setShader(new LinearGradient(0, TextHeight - textSizePix, 0, TextHeight - textSizePix + dstHeight,
		// Color.WHITE,Color.BLACK, TileMode.MIRROR));
		optPaint.setShader(new LinearGradient(0, barY - barHeight, 0, Math.max(optionY - swipeLengthOption * 3, 0), Color.BLACK,
				Color.TRANSPARENT, TileMode.MIRROR));
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
			RectForUnlockPulse.set(Width / 2 - currentRadius, middle - currentRadius, Width / 2 + currentRadius, middle + currentRadius);
			RectForUnlock.set(Width / 2 - rUnlock, middle - rUnlock, Width / 2 + rUnlock, middle + rUnlock);
		}
		setAppCenters(selectAppDrag >= 0);
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
			} else
				layout[i] = new StaticLayout(answers[i], answerTextPaint[i], Width / 2 - pad * 2, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0,
						false);
		}
		invalidate();
	}

	private void setAlpha(int alpha) {
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setAlpha(alpha);
			if (!equation[i]) {
				layout[i] = new StaticLayout(answers[i], answerTextPaint[i], Width / 2 - pad * 2, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0,
						false);
			}
		}
	}

	private boolean isLayoutSplittingWords(String string, StaticLayout layout) {
		for (int line = 0; line < layout.getLineCount() - 1; line++) {
			if (string.charAt(layout.getLineEnd(line) - 1) != ' ')
				return true;
		}
		return false;
	}

	private boolean decreaseAnswerSize() {
		if (answerSizeSP == 1)
			return false;
		answerSizeSP = Math.max(answerSizeSP - 1, 1);
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, res.getDisplayMetrics());
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
		return true;
	}

	private void changeAnswerSize(int SP, float Pix) {
		answerSizeSP = SP;
		answerSizePix = Pix;
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
	}

	private void resetAnswerSize() {
		answerSizeSP = answerSizeSPDefault;
		answerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, answerSizeSP, res.getDisplayMetrics());
		for (int i = 0; i < NumAnswers; i++) {
			answerTextPaint[i].setTextSize(answerSizePix);
		}
	}

	private void setArc(int startDeg, int totalDeg) {
		optionPath.reset();
		optionPath.arcTo(new RectF(optionX - swipeLengthOption, optionY - swipeLengthOption, optionX + swipeLengthOption, optionY
				+ swipeLengthOption), startDeg, totalDeg);
	}
}