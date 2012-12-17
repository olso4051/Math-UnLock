package com.olyware.mathlock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
	private Bitmap bmp, bmp_quizMode, bmp_silent, bmp_sound;
	private Rect dstRectForRender;
	private Rect srcRectForRender;
	private Paint circlePaint;
	private Paint handlePaint;
	private double touchX, touchY, angle = 2 * Math.PI;	// angle is in radians
	private int innerPadding;
	private int handleRadius;
	private int handleInnerBoundaries;
	private JoystickMovedListener listener;
	private int sensitivity;

	private double pi = Math.PI;
	private double pi4 = pi / 4;
	private double pi2 = pi / 2;
	private double pi34 = pi * 3 / 4;

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
		circlePaint.setColor(Color.GRAY);
		circlePaint.setStrokeWidth(2);
		circlePaint.setStyle(Paint.Style.STROKE);

		handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		handlePaint.setColor(Color.DKGRAY);
		handlePaint.setStrokeWidth(1);
		handlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		innerPadding = 0;
		sensitivity = 10;

		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_background);
		bmp_quizMode = BitmapFactory.decodeResource(getResources(), R.drawable.quizmode_background);
		bmp_silent = BitmapFactory.decodeResource(getResources(), R.drawable.silent_background);
		bmp_sound = BitmapFactory.decodeResource(getResources(), R.drawable.sound_background);
		if (bmp == null)
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		srcRectForRender = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
		dstRectForRender = new Rect();
	}

	// =========================================
	// Public Methods
	// =========================================

	public void setOnJostickMovedListener(JoystickMovedListener listener) {
		this.listener = listener;
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Here we make sure that we have a perfect circle
		int measuredWidth = measure(widthMeasureSpec);
		int measuredHeight = measure(heightMeasureSpec);
		int d = Math.min(measuredWidth, measuredHeight);

		handleRadius = (int) (d * 0.05);
		handleInnerBoundaries = handleRadius;

		setMeasuredDimension(d, d);
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
		int px = getMeasuredWidth() / 2;
		int py = getMeasuredHeight() / 2;
		int radius = Math.min(px, py);

		// Draw the background

		// canvas.drawColor(Color.BLACK);
		dstRectForRender.set(innerPadding, innerPadding, 2 * px - innerPadding, 2 * py - innerPadding);
		canvas.drawBitmap(bmp_quizMode, srcRectForRender, dstRectForRender, null);
		canvas.drawBitmap(bmp, srcRectForRender, dstRectForRender, null);
		canvas.drawBitmap(bmp_silent, srcRectForRender, dstRectForRender, null);
		// canvas.drawBitmap(bmp,innerPadding, innerPadding, null);
		// canvas.drawCircle(px, py, radius - innerPadding, circlePaint);

		// Draw the handle
		canvas.drawCircle((int) touchX + px, (int) touchY + py, handleRadius, handlePaint);

		canvas.save();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int actionType = event.getAction();
		if (actionType == MotionEvent.ACTION_MOVE) {
			int px = getMeasuredWidth() / 2;
			int py = getMeasuredHeight() / 2;
			int radius = (int) (Math.min(px, py) * 0.8 - handleInnerBoundaries);

			touchX = (event.getX() - px);
			// touchX = Math.max(Math.min(touchX, radius), -radius);

			touchY = (event.getY() - py);
			// touchY = Math.max(Math.min(touchY, radius), -radius);

			// set to radius if on edge
			Log.d(TAG, "X:" + touchX + "|Y:" + touchY + "|radius:" + radius);
			if ((Math.abs(touchX * 1.5) > radius) || (Math.abs(touchY * 1.5) > radius)) {
				if (Math.sqrt(touchX * touchX + touchY * touchY) > radius) {
					Log.d(TAG, "X:" + touchX + "|Y:" + touchY + "|angle:" + angle);
					angle = Math.atan2(-touchY, touchX);
					touchX = radius * Math.cos(angle);
					touchY = -radius * Math.sin(angle);
					Log.d(TAG, "X:" + touchX + "|Y:" + touchY + "|angle:" + angle);
				}
			}

			// Coordinates
			// Log.d(TAG, "X:" + touchX + "|Y:" + touchY);

			// Pressure
			if (listener != null) {
				listener.OnMoved((int) (touchX / radius * sensitivity), (int) (touchY / radius * sensitivity));
			}

			invalidate();
		} else if (actionType == MotionEvent.ACTION_UP) {
			if ((angle >= 0) && (angle < pi4)) {
				// D selected
				Log.d(TAG, "D selected");
			} else if ((angle >= pi4) && (angle < pi2)) {
				// C selected
				Log.d(TAG, "C selected");
			} else if ((angle >= pi2) && (angle < pi34)) {
				// B selected
				Log.d(TAG, "B selected");
			} else if ((angle >= pi34) && (angle <= pi)) {
				// A selected
				Log.d(TAG, "A selected");
			} else if ((angle < 0) && (angle >= -pi4)) {
				// sound/silent selected
				Log.d(TAG, "sound or silent");
			} else if ((angle < -pi4) && (angle >= -pi2)) {
				// emergency call selected
				Log.d(TAG, "emergency");
			} else if ((angle < -pi2) && (angle >= -pi34)) {
				// quiz mode selected
				Log.d(TAG, "quiz mode");
			} else if ((angle < -pi34) && (angle >= -pi)) {
				// settings selected
				Log.d(TAG, "settings");
			} else {
				// nothing selected
				Log.d(TAG, "nothing");
			}
			returnHandleToCenter();
			Log.d(TAG, "X:" + touchX + "|Y:" + touchY);
		}
		return true;
	}

	private void returnHandleToCenter() {

		Handler handler = new Handler();
		int numberOfFrames = 10;
		final double intervalsX = (0 - touchX) / numberOfFrames;
		final double intervalsY = (0 - touchY) / numberOfFrames;

		for (int i = 0; i < numberOfFrames; i++) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					touchX += intervalsX;
					touchY += intervalsY;
					invalidate();
				}
			}, i * 20);
		}
		angle = 2 * Math.PI;		// atan2(y,x) gives results in [-pi,pi] 2pi is outside this but equal to zero

		if (listener != null) {
			listener.OnReleased();
		}
	}
}