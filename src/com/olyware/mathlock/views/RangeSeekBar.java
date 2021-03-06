package com.olyware.mathlock.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import com.olyware.mathlock.R;

public class RangeSeekBar extends ImageView {
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	/*private final Bitmap thumbImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal);
	private final Bitmap thumbPressedImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_pressed);
	private final float thumbWidth = thumbImage.getWidth();
	private final float thumbHalfWidth = 0.5f * thumbWidth;*/
	private Bitmap thumbImage, thumbPressedImage;
	private float thumbWidth, thumbHalfWidth;
	// private final float thumbHalfHeight = 0.5f * thumbImage.getHeight();
	// private final float lineHeight = 0.1f * thumbHalfHeight;
	// private final float padding = thumbHalfWidth;
	private final int absoluteMinValue = 0, absoluteMaxValue = 5;
	private final double absoluteMinValuePrim = 0d, absoluteMaxValuePrim = 5d;
	private double normalizedMinValue = 0d;
	private double normalizedMaxValue = 1d;
	private Thumb pressedThumb = null;
	private boolean notifyWhileDragging = false;
	private OnRangeSeekBarChangeListener listener;
	private Rect fullRect, rangeRect;
	private NinePatchDrawable progress, track;
	private int Width = 0, Height = 0, PaddingLeft = 0, PaddingTop = 0, PaddingRight = 0, PaddingBottom = 0;

	public static final int DEFAULT_COLOR = Color.argb(0xFF, 0x00, 0x5C, 0xB5);

	/**
	 * An invalid pointer id.
	 */
	public static final int INVALID_POINTER_ID = 255;

	// Localized constants from MotionEvent for compatibility
	// with API < 8 "Froyo".
	public static final int ACTION_POINTER_UP = 0x6, ACTION_POINTER_INDEX_MASK = 0x0000ff00, ACTION_POINTER_INDEX_SHIFT = 8;

	private float mDownMotionX;
	private int mActivePointerId = INVALID_POINTER_ID;

	/**
	 * On touch, this offset plus the scaled value from the position of the touch will form the progress value. Usually 0.
	 */
	float mTouchProgressOffset;

	private int mScaledTouchSlop;
	private boolean mIsDragging;

	// =========================================
	// Constructors
	// =========================================

	public RangeSeekBar(Context context) {
		super(context);
		initView(context);
	}

	public RangeSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public RangeSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private final void initView(Context ctx) {
		// make RangeSeekBar focusable. This solves focus handling issues in case EditText widgets are being used along with the
		// RangeSeekBar within ScollViews.
		setFocusable(true);
		setFocusableInTouchMode(true);
		mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		thumbImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal);
		thumbPressedImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_pressed);
		// Drawable test = getResources().getDrawable(R.drawable.seek_thumb_normal);
		// thumbWidth = test.getIntrinsicWidth();
		thumbWidth = thumbImage.getWidth();
		thumbHalfWidth = 0.5f * thumbWidth;
		fullRect = new Rect();
		rangeRect = new Rect();
		progress = (NinePatchDrawable) ctx.getResources().getDrawable(R.drawable.custom_progress_primary);
		track = (NinePatchDrawable) ctx.getResources().getDrawable(R.drawable.custom_progress_track);

	}

	public boolean isNotifyWhileDragging() {
		return notifyWhileDragging;
	}

	/**
	 * Should the widget notify the listener callback while the user is still dragging a thumb? Default is false.
	 * 
	 * @param flag
	 */
	public void setNotifyWhileDragging(boolean flag) {
		this.notifyWhileDragging = flag;
	}

	/**
	 * Returns the absolute minimum value of the range that has been set at construction time.
	 * 
	 * @return The absolute minimum value of the range.
	 */
	public int getAbsoluteMinValue() {
		return absoluteMinValue;
	}

	/**
	 * Returns the absolute maximum value of the range that has been set at construction time.
	 * 
	 * @return The absolute maximum value of the range.
	 */
	public int getAbsoluteMaxValue() {
		return absoluteMaxValue;
	}

	/**
	 * Returns the currently selected min value.
	 * 
	 * @return The currently selected min value.
	 */
	public int getSelectedMinValue() {
		return normalizedToValue(normalizedMinValue);
	}

	/**
	 * Sets the currently selected minimum value. The widget will be invalidated and redrawn.
	 * 
	 * @param value
	 *            The Number value to set the minimum value to. Will be clamped to given absolute minimum/maximum range.
	 */
	public void setSelectedMinValue(int value) {
		// in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
		if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
			setNormalizedMinValue(0d);
		} else {
			setNormalizedMinValue(valueToNormalized(value));
		}
	}

	/**
	 * Returns the currently selected max value.
	 * 
	 * @return The currently selected max value.
	 */
	public int getSelectedMaxValue() {
		return normalizedToValue(normalizedMaxValue);
	}

	/**
	 * Sets the currently selected maximum value. The widget will be invalidated and redrawn.
	 * 
	 * @param value
	 *            The Number value to set the maximum value to. Will be clamped to given absolute minimum/maximum range.
	 */
	public void setSelectedMaxValue(int value) {
		// in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
		if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
			setNormalizedMaxValue(1d);
		} else {
			setNormalizedMaxValue(valueToNormalized(value));
		}
	}

	/**
	 * Registers given listener callback to notify about changed selected values.
	 * 
	 * @param listener
	 *            The listener to notify about changed selected values.
	 */
	public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener listener) {
		this.listener = listener;
	}

	/**
	 * Handles thumb selection and movement. Notifies listener callback on certain events.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (!isEnabled())
			return false;

		int pointerIndex;

		final int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {

		case MotionEvent.ACTION_DOWN:
			// Remember where the motion event started
			mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
			pointerIndex = event.findPointerIndex(mActivePointerId);
			mDownMotionX = event.getX(pointerIndex);

			pressedThumb = evalPressedThumb(mDownMotionX);

			// Only handle thumb presses.
			if (pressedThumb == null)
				return super.onTouchEvent(event);

			setPressed(true);
			invalidate();
			onStartTrackingTouch();
			trackTouchEvent(event);
			attemptClaimDrag();

			break;
		case MotionEvent.ACTION_MOVE:
			if (pressedThumb != null) {

				if (mIsDragging) {
					trackTouchEvent(event);
				} else {
					// Scroll to follow the motion event
					pointerIndex = event.findPointerIndex(mActivePointerId);
					final float x = event.getX(pointerIndex);

					if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
						setPressed(true);
						invalidate();
						onStartTrackingTouch();
						trackTouchEvent(event);
						attemptClaimDrag();
					}
				}

				if (notifyWhileDragging && listener != null) {
					listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mIsDragging) {
				trackTouchEvent(event);
				onStopTrackingTouch();
				setPressed(false);
			} else {
				// Touch up when we never crossed the touch slop threshold
				// should be interpreted as a tap-seek to that location.
				onStartTrackingTouch();
				trackTouchEvent(event);
				onStopTrackingTouch();
			}

			pressedThumb = null;
			invalidate();
			if (listener != null) {
				listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN: {
			final int index = event.getPointerCount() - 1;
			// final int index = ev.getActionIndex();
			mDownMotionX = event.getX(index);
			mActivePointerId = event.getPointerId(index);
			invalidate();
			break;
		}
		case MotionEvent.ACTION_POINTER_UP:
			onSecondaryPointerUp(event);
			invalidate();
			break;
		case MotionEvent.ACTION_CANCEL:
			if (mIsDragging) {
				onStopTrackingTouch();
				setPressed(false);
			}
			invalidate(); // see above explanation
			break;
		}
		return true;
	}

	private final void onSecondaryPointerUp(MotionEvent ev) {
		final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;

		final int pointerId = ev.getPointerId(pointerIndex);
		if (pointerId == mActivePointerId) {
			// This was our active pointer going up. Choose
			// a new active pointer and adjust accordingly.
			// TODO: Make this decision more intelligent.
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mDownMotionX = ev.getX(newPointerIndex);
			mActivePointerId = ev.getPointerId(newPointerIndex);
		}
	}

	private final void trackTouchEvent(MotionEvent event) {
		final int pointerIndex = event.findPointerIndex(mActivePointerId);
		final float x = event.getX(pointerIndex);

		if (Thumb.MIN.equals(pressedThumb)) {
			setNormalizedMinValue(screenToNormalized(x));
		} else if (Thumb.MAX.equals(pressedThumb)) {
			setNormalizedMaxValue(screenToNormalized(x));
		}
	}

	/**
	 * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
	 */
	private void attemptClaimDrag() {
		if (getParent() != null) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}
	}

	/**
	 * This is called when the user has started touching this widget.
	 */
	void onStartTrackingTouch() {
		mIsDragging = true;
	}

	/**
	 * This is called when the user either releases his touch or the touch is canceled.
	 */
	void onStopTrackingTouch() {
		mIsDragging = false;
	}

	/**
	 * Ensures correct size of the widget.
	 */
	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		PaddingLeft = getPaddingLeft();
		PaddingTop = getPaddingTop();
		PaddingRight = getPaddingRight();
		PaddingBottom = getPaddingBottom();
		Width = 200;
		if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
			Width = MeasureSpec.getSize(widthMeasureSpec);
		}

		Height = thumbImage.getHeight() + PaddingTop + PaddingBottom;
		if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
			Height = Math.min(Height, MeasureSpec.getSize(heightMeasureSpec));
		}
		setMeasuredDimension(Width, Height);
		fullRect.set((int) thumbHalfWidth + PaddingLeft, PaddingTop, (int) (Width - thumbHalfWidth - PaddingRight), Height - PaddingBottom);
		rangeRect.set(normalizedToScreen(normalizedMinValue), PaddingTop, normalizedToScreen(normalizedMaxValue), Height - PaddingBottom);
	}

	/**
	 * Draws the widget on the given canvas.
	 */
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// draw seek bar background line
		// final RectF rect = new RectF(padding, 0.5f * (getHeight() - lineHeight), getWidth() - padding, 0.5f * (getHeight() +
		// lineHeight));
		// paint.setStyle(Style.FILL);
		// paint.setColor(Color.GRAY);
		// paint.setAntiAlias(true);
		// canvas.drawRect(fullRect, paint);
		track.setBounds(fullRect);
		track.draw(canvas);

		// draw seek bar active range line
		// rect.left = normalizedToScreen(normalizedMinValue);
		// rect.right = normalizedToScreen(normalizedMaxValue);

		// orange color
		// paint.setColor(DEFAULT_COLOR);
		// canvas.drawRect(rangeRect, paint);
		progress.setBounds(rangeRect);
		progress.draw(canvas);

		// draw minimum thumb
		drawThumb(normalizedToScreen(normalizedMinValue), Thumb.MIN.equals(pressedThumb), canvas);

		// draw maximum thumb
		drawThumb(normalizedToScreen(normalizedMaxValue), Thumb.MAX.equals(pressedThumb), canvas);
	}

	/**
	 * Overridden to save instance state when device orientation changes. This method is called automatically if you assign an id to the
	 * RangeSeekBar widget using the {@link #setId(int)} method. Other members of this class than the normalized min and max values don't
	 * need to be saved.
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		final Bundle bundle = new Bundle();
		bundle.putParcelable("SUPER", super.onSaveInstanceState());
		bundle.putDouble("MIN", normalizedMinValue);
		bundle.putDouble("MAX", normalizedMaxValue);
		return bundle;
	}

	/**
	 * Overridden to restore instance state when device orientation changes. This method is called automatically if you assign an id to the
	 * RangeSeekBar widget using the {@link #setId(int)} method.
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable parcel) {
		final Bundle bundle = (Bundle) parcel;
		super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
		normalizedMinValue = bundle.getDouble("MIN");
		normalizedMaxValue = bundle.getDouble("MAX");
		rangeRect.left = normalizedToScreen(normalizedMinValue);
		rangeRect.right = normalizedToScreen(normalizedMaxValue);
	}

	/**
	 * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
	 * 
	 * @param screenCoord
	 *            The x-coordinate in screen space where to draw the image.
	 * @param pressed
	 *            Is the thumb currently in "pressed" state?
	 * @param canvas
	 *            The canvas to draw upon.
	 */
	private void drawThumb(float screenCoord, boolean pressed, Canvas canvas) {
		canvas.drawBitmap(pressed ? thumbPressedImage : thumbImage, screenCoord - thumbHalfWidth, PaddingTop, paint);
	}

	/**
	 * Decides which (if any) thumb is touched by the given x-coordinate.
	 * 
	 * @param touchX
	 *            The x-coordinate of a touch event in screen space.
	 * @return The pressed thumb or null if none has been touched.
	 */
	private Thumb evalPressedThumb(float touchX) {
		Thumb result = null;
		boolean minThumbPressed = isInThumbRange(touchX, normalizedMinValue);
		boolean maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue);
		if (minThumbPressed && maxThumbPressed) {
			// if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the
			// thumbs in a corner, not being able to drag them apart anymore.
			result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
		} else if (minThumbPressed) {
			result = Thumb.MIN;
		} else if (maxThumbPressed) {
			result = Thumb.MAX;
		}
		return result;
	}

	/**
	 * Decides if given x-coordinate in screen space needs to be interpreted as "within" the normalized thumb x-coordinate.
	 * 
	 * @param touchX
	 *            The x-coordinate in screen space to check.
	 * @param normalizedThumbValue
	 *            The normalized x-coordinate of the thumb to check.
	 * @return true if x-coordinate is in thumb range, false otherwise.
	 */
	private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
		return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth;
	}

	/**
	 * Sets normalized min value to value so that 0 <= value <= normalized max value <= 1. The View will get invalidated when calling this
	 * method.
	 * 
	 * @param value
	 *            The new normalized min value to set.
	 */
	public void setNormalizedMinValue(double value) {
		normalizedMinValue = Math.max(0d, Math.min(1d, Math.min(value, normalizedMaxValue)));
		rangeRect.set(normalizedToScreen(normalizedMinValue), PaddingTop, normalizedToScreen(normalizedMaxValue), getHeight()
				- PaddingBottom);
		invalidate();
	}

	/**
	 * Sets normalized max value to value so that 0 <= normalized min value <= value <= 1. The View will get invalidated when calling this
	 * method.
	 * 
	 * @param value
	 *            The new normalized max value to set.
	 */
	public void setNormalizedMaxValue(double value) {
		normalizedMaxValue = Math.max(0d, Math.min(1d, Math.max(value, normalizedMinValue)));
		rangeRect.set(normalizedToScreen(normalizedMinValue), PaddingTop, normalizedToScreen(normalizedMaxValue), getHeight()
				- PaddingBottom);
		invalidate();
	}

	/**
	 * Converts a normalized value to a Number object in the value space between absolute minimum and maximum.
	 * 
	 * @param normalized
	 * @return
	 */
	private int normalizedToValue(double normalized) {
		return (int) (absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim));
	}

	/**
	 * Converts the given Number value to a normalized double.
	 * 
	 * @param value
	 *            The Number value to normalize.
	 * @return The normalized double.
	 */
	private double valueToNormalized(int value) {
		if (0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
			// prevent division by zero, simply return 0.
			return 0d;
		}
		return (value - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim);
	}

	/**
	 * Converts a normalized value into screen space.
	 * 
	 * @param normalizedCoord
	 *            The normalized value to convert.
	 * @return The converted value in screen space.
	 */
	private int normalizedToScreen(double normalizedCoord) {
		return (int) (thumbHalfWidth + PaddingLeft + normalizedCoord * (getWidth() - 2 * thumbHalfWidth - PaddingLeft - PaddingRight));
	}

	/**
	 * Converts screen space x-coordinates into normalized values.
	 * 
	 * @param screenCoord
	 *            The x-coordinate in screen space to convert.
	 * @return The normalized value.
	 */
	private double screenToNormalized(float screenCoord) {
		int width = getWidth();
		if (width <= 2 * thumbHalfWidth + PaddingLeft + PaddingRight) {
			// prevent division by zero, simply return 0.
			return 0d;
		} else {
			double result = (screenCoord - thumbHalfWidth - PaddingLeft) / (width - 2 * thumbHalfWidth - PaddingLeft - PaddingRight);
			return Math.min(1d, Math.max(0d, result));
		}
	}

	/**
	 * Callback listener interface to notify about changed range values.
	 * 
	 * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
	 * 
	 * @param <int> The Number type the RangeSeekBar has been declared with.
	 */
	public interface OnRangeSeekBarChangeListener {
		public void onRangeSeekBarValuesChanged(RangeSeekBar bar, int minValue, int maxValue);
	}

	/**
	 * Thumb constants (min and max).
	 */
	private static enum Thumb {
		MIN, MAX
	};
}
