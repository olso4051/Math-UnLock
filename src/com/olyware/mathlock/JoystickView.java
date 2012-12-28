package com.olyware.mathlock;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {

	// =========================================
	// Private Members
	// =========================================

	private final String TAG = "JoystickView";
	// private Bitmap bmp, bmp_handle;
	// private Bitmap bmp_silent, bmp_sound, bmp_quizMode, bmp_set, bmp_em, bmp_sil, bmp_A, bmp_B, bmp_C, bmp_D;
	// private Bitmap bmp_select;
	private Bitmap bmpA, bmpB, bmpC, bmpD, bmpS, bmpQ, bmpE, bmpSil, bmpSnd;
	private Bitmap bmpAs, bmpBs, bmpCs, bmpDs, bmpSs, bmpQs, bmpEs, bmpSils, bmpSnds;
	// private Rect dstRectForRender, dstRectForHandle;
	// private Rect srcRectForRender, srcRectForHandle;
	private Rect dstRectForA, dstRectForB, dstRectForC, dstRectForD;
	private Rect dstRectForS, dstRectForQ, dstRectForE, dstRectForSnd;
	private Rect srcRectForBig, srcRectForSmall;
	private Paint circlePaint[] = new Paint[4];
	private Paint handlePaint, textPaint;
	private int textSizeDP, textSizePix;
	private double touchX, touchY;// , angle; // angle is in radians
	private double startX, startY;// , aX, aY, bX, bY, cX, cY, dX, dY;
	private double X[] = new double[4];		// a=0,b=1,c=2,d=3
	private double Y[] = new double[4];
	private int type = 0;
	private int diffX1, diffY1, diffX, diffY;
	private int state = 0;					// direction answers are going (up-left, up-right, ect..)
	// private int handleRadius;
	// private double thetaMax, rMax, fX, fY; // for maximum area of a rectangle in an ellipse
	// private double rX, rY, rCurrent;
	private int spacing, rBig, rSmall;
	private JoystickSelectListener listener;

	private boolean settingsMode;
	private boolean quizMode;
	private boolean emergencyMode;
	private boolean silentMode;
	private boolean selectAnswers[] = new boolean[4];
	private boolean selectOptions[] = new boolean[4];
	private int selectLeft[] = new int[4];
	private int selectRight[] = new int[4];

	private double pi = Math.PI;

	private AngleSelect angles[] = new AngleSelect[8];

	private Resources res;
	private Runnable revealText, finishText;
	private Runnable revealAnswer[] = new Runnable[4];
	private Runnable finishAnswer[] = new Runnable[4];
	private Handler answerHandler, textHandler;
	private final int textFrames = 10, textFrameTime = 50, answerFrames = 5, answerFrameTime = 20;

	// =========================================
	// Constructors
	// =========================================

	public JoystickView(Context context) {
		super(context);
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initJoystickView();
	}

	// =========================================
	// Initialization
	// =========================================

	private void initJoystickView() {
		setFocusable(true);
		// int W = getMeasuredWidth();
		// int H = getMeasuredHeight();
		res = getResources();

		textSizeDP = 20; // 5dp
		textSizePix = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSizeDP, getResources().getDisplayMetrics());

		textHandler = new Handler();
		answerHandler = new Handler();
		initRunnables();

		for (int ans = 0; ans < 4; ans++) {
			selectAnswers[ans] = false;
			selectOptions[ans] = false;
			circlePaint[ans] = new Paint(Paint.ANTI_ALIAS_FLAG);
			circlePaint[ans].setAlpha(0);
		}
		// circlePaint.setColor(Color.GRAY);
		// circlePaint.setStrokeWidth(2);
		// circlePaint.setStyle(Paint.Style.STROKE);

		handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		handlePaint.setAlpha(255);
		// handlePaint.setColor(Color.DKGRAY);
		// handlePaint.setStrokeWidth(1);
		// handlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(textSizePix);

		bmpA = BitmapFactory.decodeResource(getResources(), R.drawable.select_a2);
		bmpB = BitmapFactory.decodeResource(getResources(), R.drawable.select_b2);
		bmpC = BitmapFactory.decodeResource(getResources(), R.drawable.select_c2);
		bmpD = BitmapFactory.decodeResource(getResources(), R.drawable.select_d2);
		bmpAs = BitmapFactory.decodeResource(getResources(), R.drawable.select_a2s);
		bmpBs = BitmapFactory.decodeResource(getResources(), R.drawable.select_b2s);
		bmpCs = BitmapFactory.decodeResource(getResources(), R.drawable.select_c2s);
		bmpDs = BitmapFactory.decodeResource(getResources(), R.drawable.select_d2s);
		bmpS = BitmapFactory.decodeResource(getResources(), R.drawable.select_s2);
		bmpQ = BitmapFactory.decodeResource(getResources(), R.drawable.select_q2);
		bmpQs = BitmapFactory.decodeResource(getResources(), R.drawable.select_q2s);
		bmpE = BitmapFactory.decodeResource(getResources(), R.drawable.select_e2);
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
		rBig = Math.max(bmpA.getWidth(), bmpA.getHeight()) / 2;
		rSmall = Math.max(bmpS.getWidth(), bmpS.getHeight()) / 2;

		setDiffXY();

		/*srcRectForRender = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
		dstRectForRender = new Rect();
		srcRectForHandle = new Rect(0, 0, bmp_handle.getWidth(), bmp_handle.getHeight());
		dstRectForHandle = new Rect();*/
		srcRectForBig = new Rect(0, 0, bmpA.getWidth(), bmpA.getHeight());
		srcRectForSmall = new Rect(0, 0, bmpS.getWidth(), bmpS.getHeight());
		dstRectForA = new Rect();
		dstRectForB = new Rect();
		dstRectForC = new Rect();
		dstRectForD = new Rect();
		dstRectForS = new Rect();
		dstRectForQ = new Rect();
		dstRectForE = new Rect();
		dstRectForSnd = new Rect();

		for (int i = 0; i < angles.length; i++)
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
		angles[7].greaterThan = -pi * 5 / 6;
	}

	// =========================================
	// Public Methods
	// =========================================

	public void setOnJostickSelectedListener(JoystickSelectListener listener) {
		this.listener = listener;
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
		setDiffXY();
	}

	public void setUnlockType(int type) {
		this.type = type;
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Here we make sure that we have a perfect circle
		int W = measure(widthMeasureSpec);
		int H = measure(heightMeasureSpec);
		if (H < W / 2)
			H = W;
		spacing = (W - rBig * 4 - rSmall * 4) / 5;
		selectLeft[3] = W / 2 - rBig * 2 - rSmall * 2 - spacing * 3 / 2;
		selectRight[3] = W / 2 - rBig * 2 - spacing * 3 / 2;
		selectLeft[2] = W / 2 - rBig * 2 - spacing / 2;
		selectRight[2] = W / 2 - spacing / 2;
		selectLeft[1] = W / 2 + spacing / 2;
		selectRight[1] = W / 2 + rBig * 2 + spacing / 2;
		selectLeft[0] = W / 2 + rBig * 2 + spacing * 3 / 2;
		selectRight[0] = W / 2 + rBig * 2 + rSmall * 2 + spacing * 3 / 2;

		dstRectForS.set(selectLeft[3], H - rBig - rSmall, selectRight[3], H - rBig + rSmall);
		dstRectForQ.set(selectLeft[2], H - rBig * 2, selectRight[2], H);
		dstRectForE.set(selectLeft[1], H - rBig * 2, selectRight[1], H);
		dstRectForSnd.set(selectLeft[0], H - rBig - rSmall, selectRight[0], H - rBig + rSmall);

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

		setMeasuredDimension(W, H);
	}

	private int measure(int measureSpec) {
		int result = 0;
		// Decode the measurement specifications.
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.UNSPECIFIED) {
			// Return a default size of 200 if no bounds are specified.
			result = 200;
		} else {
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int W = getMeasuredWidth();
		int H = getMeasuredHeight();

		canvas.drawBitmap(bmpS, srcRectForSmall, dstRectForS, handlePaint);
		if (quizMode)
			canvas.drawBitmap(bmpQs, srcRectForBig, dstRectForQ, handlePaint);
		else
			canvas.drawBitmap(bmpQ, srcRectForBig, dstRectForQ, handlePaint);
		canvas.drawBitmap(bmpE, srcRectForBig, dstRectForE, handlePaint);
		// canvas.drawBitmap(bmpSnd, srcRectForSmall, dstRectForSnd, handlePaint);

		dstRectForA.set((int) X[0] - rBig, (int) Y[0] - rBig, (int) X[0] + rBig, (int) Y[0] + rBig);
		dstRectForB.set((int) X[1] - rBig, (int) Y[1] - rBig, (int) X[1] + rBig, (int) Y[1] + rBig);
		dstRectForC.set((int) X[2] - rBig, (int) Y[2] - rBig, (int) X[2] + rBig, (int) Y[2] + rBig);
		dstRectForD.set((int) X[3] - rBig, (int) Y[3] - rBig, (int) X[3] + rBig, (int) Y[3] + rBig);
		if (selectAnswers[0])
			canvas.drawBitmap(bmpAs, srcRectForBig, dstRectForA, circlePaint[0]);
		else
			canvas.drawBitmap(bmpA, srcRectForBig, dstRectForA, circlePaint[0]);
		if (selectAnswers[1])
			canvas.drawBitmap(bmpBs, srcRectForBig, dstRectForB, circlePaint[1]);
		else
			canvas.drawBitmap(bmpB, srcRectForBig, dstRectForB, circlePaint[1]);
		if (selectAnswers[2])
			canvas.drawBitmap(bmpCs, srcRectForBig, dstRectForC, circlePaint[2]);
		else
			canvas.drawBitmap(bmpC, srcRectForBig, dstRectForC, circlePaint[2]);
		if (selectAnswers[3])
			canvas.drawBitmap(bmpDs, srcRectForBig, dstRectForD, circlePaint[3]);
		else
			canvas.drawBitmap(bmpD, srcRectForBig, dstRectForD, circlePaint[3]);

		/*int hx = (int) touchX + px / 2 - handleRadius;
		int hy = py / 2 - (int) touchY - handleRadius;

		// Draw the background
		dstRectForRender.set(0, 0, px, py);
		dstRectForHandle.set(hx, hy, hx + 2 * handleRadius, hy + 2 * handleRadius);

		canvas.drawBitmap(bmp, srcRectForRender, dstRectForRender, circlePaint);
		if (select) {
			canvas.drawBitmap(bmp_select, srcRectForRender, dstRectForRender, circlePaint);
		}*/
		if (silentMode) {
			canvas.drawBitmap(bmpSil, srcRectForSmall, dstRectForSnd, handlePaint);
		} else {
			canvas.drawBitmap(bmpSnd, srcRectForSmall, dstRectForSnd, handlePaint);
		}
		/*if (quizMode) {
			canvas.drawBitmap(bmp_quizMode, srcRectForRender, dstRectForRender, circlePaint);
		}

		// Draw the handle
		canvas.drawBitmap(bmp_handle, srcRectForHandle, dstRectForHandle, handlePaint);
		// canvas.drawCircle((int) touchX + px, py - (int) touchY, handleRadius, handlePaint);
		*/
		if ((selectOptions[0]) || (selectOptions[1]) || (selectOptions[2]) || (selectOptions[3]))
			canvas.drawText(res.getString(R.string.swipe_option), W / 2, H - rBig * 2 - 20, textPaint);
		else
			canvas.drawText(res.getString(R.string.swipe_screen), W / 2, H - rBig * 2 - 20, textPaint);
		canvas.save();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int actionType = event.getAction();
		// int W = getMeasuredWidth();
		int H = getMeasuredHeight();

		if (actionType == MotionEvent.ACTION_DOWN) {
			startX = event.getX();
			startY = event.getY();
			if (startY < (H - rBig * 3)) {			// select answers
				setAnswerLocations();
				if (listener != null)
					listener.OnSelect(-1);		// send a vibrate signal
				revealAnswers();
			} else {							// select options
				touchX = startX;
				touchY = startY;
				if ((startX >= selectLeft[0]) && (startX <= selectRight[0])) {
					selectOptions[0] = true;
					flashText();
				} else if ((startX >= selectLeft[1]) && (startX <= selectRight[1])) {
					selectOptions[1] = true;
					flashText();
				} else if ((startX >= selectLeft[2]) && (startX <= selectRight[2])) {
					selectOptions[2] = true;
					flashText();
				} else if ((startX >= selectLeft[3]) && (startX <= selectRight[3])) {
					selectOptions[3] = true;
					flashText();
				}
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
			checkSelection(true);
			returnToDefault();
			flashText();
			/*if ((Math.sqrt(touchX * touchX + touchY * touchY) > rCurrent * .6) && (rCurrent > 0))
				checkSelection(angle, true);
			revealDisappearBackground(false);
			returnHandleToCenter();*/
		}
		return true;
	}

	private void setAnswerLocations() {
		int maxDiff = 2;
		int diff = 0;
		int attempts = 0;
		int startingState = state;

		while ((maxDiff >= 2) && (attempts < 4)) {
			maxDiff = 0;
			state = (startingState + attempts) % 4;
			attempts += 1;
			setDiffXY();

			X[0] = startX + diffX1;
			Y[0] = startY + diffY1;
			checkDiff(0);

			X[1] = X[0] + diffX;
			Y[1] = Y[0] + diffY;
			diff = checkDiff(1);
			if (diff > maxDiff)
				maxDiff = diff;

			X[2] = X[1] + diffX;
			Y[2] = Y[1] + diffY;
			diff = checkDiff(2);
			if (diff > maxDiff)
				maxDiff = diff;

			X[3] = X[2] + diffX;
			Y[3] = Y[2] + diffY;
			diff = checkDiff(3);
			if (diff > maxDiff)
				maxDiff = diff;
		}
		state = startingState;
	}

	private void setDiffXY() {
		switch (state) {
		case 0:		// up-right
			diffX = (int) (rBig * Math.sqrt(3));
			diffY = -rBig;
			break;
		case 1:		// up-left
			diffX = (int) (-rBig * Math.sqrt(3));
			diffY = -rBig;
			break;
		case 2:		// down-left
			diffX = (int) (-rBig * Math.sqrt(3));
			diffY = rBig;
			break;
		case 3:		// down-right
			diffX = (int) (rBig * Math.sqrt(3));
			diffY = rBig;
			break;
		}
		diffX1 = diffX * 2;
		diffY1 = diffY * 2;
	}

	private int checkDiff(int loc) {
		int W = getMeasuredWidth();
		int H = getMeasuredHeight();
		int checks = 0;
		boolean first = (loc == 0);
		switch (state) {
		case 0:		// up-right
			if (Y[loc] - rBig < 0) {		// above top boundary
				checks += 1;
				state = ((state - 1) % 4 + 4) % 4;
				setDiffXY();
				if (first) {
					X[loc] = startX + diffX1;
					Y[loc] = startY + diffY1;
				} else {
					X[loc] = X[loc - 1] + diffX;
					Y[loc] = Y[loc - 1] + diffY;
				}
			}
			if (X[loc] + rBig > W) {		// right of right boundary
				checks += 1;
				state = (state + 1) % 4;
				setDiffXY();
				if (first) {
					X[loc] = startX + diffX1;
					Y[loc] = startY + diffY1;
				} else {
					X[loc] = X[loc - 1] + diffX;
					Y[loc] = Y[loc - 1] + diffY;
				}
			}
			break;
		case 1:		// up-left
			if (Y[loc] - rBig < 0) {		// above top boundary
				checks += 1;
				state = (state + 1) % 4;
				setDiffXY();
				if (first) {
					X[loc] = startX + diffX1;
					Y[loc] = startY + diffY1;
				} else {
					X[loc] = X[loc - 1] + diffX;
					Y[loc] = Y[loc - 1] + diffY;
				}
			}
			if (X[loc] - rBig < 0) {		// left of left boundary
				checks += 1;
				state = ((state - 1) % 4 + 4) % 4;
				setDiffXY();
				if (first) {
					X[loc] = startX + diffX1;
					Y[loc] = startY + diffY1;
				} else {
					X[loc] = X[loc - 1] + diffX;
					Y[loc] = Y[loc - 1] + diffY;
				}
			}
			break;
		case 2:		// down-left
			if (Y[loc] + rBig > H) {		// below bottom boundary
				checks += 1;
				state = ((state - 1) % 4 + 4) % 4;
				setDiffXY();
				if (first) {
					X[loc] = startX + diffX1;
					Y[loc] = startY + diffY1;
				} else {
					X[loc] = X[loc - 1] + diffX;
					Y[loc] = Y[loc - 1] + diffY;
				}
			}
			if (X[loc] - rBig < 0) {		// left of left boundary
				checks += 1;
				state = (state + 1) % 4;
				setDiffXY();
				if (first) {
					X[loc] = startX + diffX1;
					Y[loc] = startY + diffY1;
				} else {
					X[loc] = X[loc - 1] + diffX;
					Y[loc] = Y[loc - 1] + diffY;
				}
			}
			break;
		case 3:		// down-right
			if (Y[loc] + rBig > H) {		// below bottom boundary
				checks += 1;
				state = (state + 1) % 4;
				setDiffXY();
				if (first) {
					X[loc] = startX + diffX1;
					Y[loc] = startY + diffY1;
				} else {
					X[loc] = X[loc - 1] + diffX;
					Y[loc] = Y[loc - 1] + diffY;
				}
			}
			if (X[loc] + rBig > W) {		// right of right boundary
				checks += 1;
				state = ((state - 1) % 4 + 4) % 4;
				setDiffXY();
				if (first) {
					X[loc] = startX + diffX1;
					Y[loc] = startY + diffY1;
				} else {
					X[loc] = X[loc - 1] + diffX;
					Y[loc] = Y[loc - 1] + diffY;
				}
			}
			break;
		}
		return checks;
	}

	private void revealAnswers() {
		for (int ans = 0; ans < 4; ans++) {
			answerHandler.removeCallbacks(revealAnswer[ans]);
			answerHandler.removeCallbacks(finishAnswer[ans]);
		}

		for (int ans = 0; ans < 4; ans++)
			circlePaint[ans].setAlpha(0);

		for (int ans = 0; ans < 4; ans++) {
			for (int i = 0; i < answerFrames; i++) {
				answerHandler.postDelayed(revealAnswer[ans], i * answerFrameTime + ans * answerFrames * answerFrameTime);
			}
			answerHandler.postDelayed(finishAnswer[ans], answerFrames * answerFrameTime * (ans + 1));
		}
	}

	private void returnToDefault() {
		int H = getMeasuredHeight();
		touchX = 0;
		touchY = 0;
		startX = -rBig;
		startY = -rBig;
		for (int ans = 0; ans < 4; ans++) {
			X[ans] = -rBig;
			Y[ans] = -rBig;
			selectAnswers[ans] = false;
			selectOptions[ans] = false;
			circlePaint[ans].setAlpha(0);
		}
		dstRectForS.set(selectLeft[3], H - rBig - rSmall, selectRight[3], H - rBig + rSmall);
		dstRectForQ.set(selectLeft[2], H - rBig * 2, selectRight[2], H);
		dstRectForE.set(selectLeft[1], H - rBig * 2, selectRight[1], H);
		dstRectForSnd.set(selectLeft[0], H - rBig - rSmall, selectRight[0], H - rBig + rSmall);
		invalidate();
	}

	private void checkSelection(boolean send) {
		double diffx, diffy;
		int s = -1, swipeLength = rBig * 4;
		if ((selectOptions[0]) || (selectOptions[1]) || (selectOptions[2]) || (selectOptions[3])) {
			if (selectOptions[0]) {
				s = 4;
				dstRectForSnd.set((int) touchX - rSmall, (int) touchY - rSmall, (int) touchX + rSmall, (int) touchY + rSmall);
			} else if (selectOptions[1]) {
				s = 5;
				dstRectForE.set((int) touchX - rSmall, (int) touchY - rSmall, (int) touchX + rSmall, (int) touchY + rSmall);
			} else if (selectOptions[2]) {
				s = 6;
				dstRectForQ.set((int) touchX - rSmall, (int) touchY - rSmall, (int) touchX + rSmall, (int) touchY + rSmall);
			} else if (selectOptions[3]) {
				s = 7;
				dstRectForS.set((int) touchX - rSmall, (int) touchY - rSmall, (int) touchX + rSmall, (int) touchY + rSmall);
			}
			diffx = touchX - startX;
			diffy = touchY - startY;
			if (Math.sqrt(diffx * diffx + diffy * diffy) > swipeLength) {
				selectOptions[s - 4] = false;
				listener.OnSelect(s);
			}
		} else
			for (int i = 0; i < 4; i++) {
				diffx = touchX - X[i];
				diffy = touchY - Y[i];
				if (Math.sqrt(diffx * diffx + diffy * diffy) < rBig)
					if (send) {
						selectAnswers[i] = false;
						listener.OnSelect(i);
					} else
						selectAnswers[i] = true;
				else
					selectAnswers[i] = false;
			}
	}

	private void flashText() {
		textHandler.removeCallbacks(revealText);
		textHandler.removeCallbacks(finishText);

		textPaint.setAlpha(0);

		for (int i = 0; i < textFrames; i++) {
			textHandler.postDelayed(revealText, i * textFrameTime);
		}
		textHandler.postDelayed(finishText, textFrames * textFrameTime);
	}

	private void initRunnables() {
		final int answerInterval = 255 / answerFrames;
		final int textInterval = 255 / textFrames;

		for (int i = 0; i < 4; i++) {
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