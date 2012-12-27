package com.olyware.mathlock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {

	// =========================================
	// Private Members
	// =========================================

	private final String TAG = "JoystickView";
	private Bitmap bmp, bmp_handle;
	private Bitmap bmp_silent, bmp_sound, bmp_quizMode, bmp_set, bmp_em, bmp_sil, bmp_A, bmp_B, bmp_C, bmp_D;
	private Bitmap bmp_select;
	// private Path p[] = new Path[4];
	private Rect dstRectForRender, dstRectForHandle;
	private Rect srcRectForRender, srcRectForHandle;
	private Paint circlePaint;
	private Paint handlePaint;
	private double touchX, touchY, angle;	// angle is in radians
	private int handleRadius;
	private double thetaMax, rMax, fX, fY;	// for maximum area of a rectangle in an ellipse
	private double rX, rY, rCurrent;
	private JoystickSelectListener listener;

	private boolean settingsMode;
	private boolean quizMode;
	private boolean emergencyMode;
	private boolean silentMode;
	private boolean select = false;

	private double pi = Math.PI;

	private AngleSelect angles[] = new AngleSelect[8];

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

		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setAlpha(0);
		// circlePaint.setColor(Color.GRAY);
		// circlePaint.setStrokeWidth(2);
		// circlePaint.setStyle(Paint.Style.STROKE);

		handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		handlePaint.setAlpha(255);
		// handlePaint.setColor(Color.DKGRAY);
		// handlePaint.setStrokeWidth(1);
		// handlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_background2);
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
			bmp_select = BitmapFactory.decodeResource(getResources(), R.drawable.select_a);

		srcRectForRender = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
		dstRectForRender = new Rect();
		srcRectForHandle = new Rect(0, 0, bmp_handle.getWidth(), bmp_handle.getHeight());
		dstRectForHandle = new Rect();

		touchX = 0;
		touchY = 0;
		angle = 0;
		rCurrent = 0;

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

		/*p[0] = new Path();
		p[1] = new Path();
		p[2] = new Path();
		p[3] = new Path();*/
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

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Here we make sure that we have a perfect circle
		int W = measure(widthMeasureSpec);
		int H = measure(heightMeasureSpec);
		W = Math.min(W, bmp.getWidth()); // use the size of the bmp so we don't have to scale
		Log.d(TAG, H + "|" + W);

		if (H > W) {
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

		handleRadius = (int) (W * 0.1);

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
		int px = getMeasuredWidth();
		int py = getMeasuredHeight();
		int hx = (int) touchX + px / 2 - handleRadius;
		int hy = py / 2 - (int) touchY - handleRadius;

		// Draw the background
		dstRectForRender.set(0, 0, px, py);
		dstRectForHandle.set(hx, hy, hx + 2 * handleRadius, hy + 2 * handleRadius);

		canvas.drawBitmap(bmp, srcRectForRender, dstRectForRender, circlePaint);
		if (select) {
			canvas.drawBitmap(bmp_select, srcRectForRender, dstRectForRender, circlePaint);
		}
		if (silentMode) {
			canvas.drawBitmap(bmp_silent, srcRectForRender, dstRectForRender, circlePaint);
		} else {
			canvas.drawBitmap(bmp_sound, srcRectForRender, dstRectForRender, circlePaint);
		}
		if (quizMode) {
			canvas.drawBitmap(bmp_quizMode, srcRectForRender, dstRectForRender, circlePaint);
		}

		// Draw the handle
		canvas.drawBitmap(bmp_handle, srcRectForHandle, dstRectForHandle, handlePaint);
		// canvas.drawCircle((int) touchX + px, py - (int) touchY, handleRadius, handlePaint);

		canvas.save();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int actionType = event.getAction();
		if (actionType == MotionEvent.ACTION_MOVE) {
			int px = getMeasuredWidth() / 2;
			int py = getMeasuredHeight() / 2;

			double tempX = (event.getX() - px);
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
				// } else {
				// select = false;
				// }
				invalidate();
			}
		} else if (actionType == MotionEvent.ACTION_UP) {
			if ((Math.sqrt(touchX * touchX + touchY * touchY) > rCurrent * .6) && (rCurrent > 0))
				checkSelection(angle, true);
			revealDisappearBackground(false);
			returnHandleToCenter();
		}
		return true;
	}

	private void checkSelection(double ang, boolean send) {
		select = false;

		if ((listener != null) || (!send)) {
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
	}

	private void returnHandleToCenter() {

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

	}

	private void revealDisappearBackground(final boolean RorD) {
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
	}

	/*private void revealAnswer(final int a) {

		Handler handler = new Handler();
		final int numFrames = 5;
		int frameTime = 20;
		final int px = getMeasuredWidth();
		final int py = getMeasuredHeight();
		int IntervalX = px / (2 * (numFrames - 1));
		int IntervalY = py / (2 * (numFrames - 1));

		switch (a) {
		case 0:
			p[a].moveTo(0, 0);
			break;
		case 1:
			p[a].moveTo(px, 0);
			IntervalX = -IntervalX;
			break;
		case 2:
			p[a].moveTo(0, py);
			IntervalY = -IntervalY;
			break;
		case 3:
			p[a].moveTo(px, py);
			IntervalX = -IntervalX;
			IntervalY = -IntervalY;
			break;
		}

		for (int i = 0; i < numFrames; i++) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					switch (a) {
					case 0:
						p[a].lineTo(0, (int) (py * Frac));
						p[a].lineTo((int) (px * Frac / 2), (int) (py * Frac));
						p[a].lineTo((int) (px * Frac), (int) (py * Frac / 2));
						p[a].lineTo((int) (px * Frac), 0);
						p[a].close();
						break;
					case 1:
						p[a].lineTo(px, (int) (py * Frac));
						p[a].lineTo((int) (px * (1 - Frac / 2)), (int) (py * Frac));
						p[a].lineTo((int) (px * (1 - Frac)), (int) (py * Frac / 2));
						p[a].lineTo((int) (px * (1 - Frac)), 0);
						p[a].lineTo(px, 0);
						break;
					case 2:
						p[a].lineTo(0, (int) (py * (1 - Frac)));
						p[a].lineTo((int) (px * Frac / 2), (int) (py * (1 - Frac)));
						p[a].lineTo((int) (px * Frac), (int) (py * (1 - Frac / 2)));
						p[a].lineTo((int) (px * Frac), py);
						p[a].lineTo(0, py);
						break;
					case 3:
						p[a].lineTo(px, (int) (py * (1 - Frac)));
						p[a].lineTo((int) (px * (1 - Frac / 2)), (int) (py * (1 - Frac)));
						p[a].lineTo((int) (px * (1 - Frac)), (int) (py * (1 - Frac / 2)));
						p[a].lineTo((int) (px * (1 - Frac)), py);
						p[a].lineTo(px, py);
						break;
					}
					invalidate();
				}
			}, i * frameTime);
		}
	}*/
}