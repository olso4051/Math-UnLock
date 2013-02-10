package com.olyware.mathlock.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class ProblemTextView extends TextView {

	private int Width, Height;
	private final int textConstSizeSP = 30;
	private int textSizeSP;
	private float textSizePix;
	private String currentText;

	private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);;
	private StaticLayout layout;
	private Rect bounds;
	private boolean measured = false;

	// =========================================
	// Constructors
	// =========================================

	public ProblemTextView(Context context) {
		super(context);
		initAnswerView();
	}

	public ProblemTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAnswerView();
	}

	public ProblemTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initAnswerView();
	}

	// =========================================
	// Initialization
	// =========================================

	private void initAnswerView() {
		textSizeSP = textConstSizeSP; // text size in scaled pixels
		textSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, getResources().getDisplayMetrics());
		textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextAlign(Paint.Align.LEFT);
		textPaint.setTextSize(textSizePix);

		bounds = new Rect();
	}

	// =========================================
	// Public Methods
	// =========================================

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
		currentText = String.valueOf(text);
		if (measured) {
			resetTextSize();
			setDimensions();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Width = (right - left) - getCompoundPaddingLeft() - getCompoundPaddingRight();
		Height = (bottom - top) - getCompoundPaddingBottom() - getCompoundPaddingTop();
		measured = true;
		setDimensions();
		super.onLayout(changed, left, top, right, bottom);
	}

	// =========================================
	// Private Methods
	// =========================================

	private void setDimensions() {
		setLayouts();
		float maxH = Math.max(bounds.height(), textSizePix);
		maxH = Math.max(layout.getHeight(), maxH);

		if ((maxH > Height) && (Height > 0)) {
			decreaseTextSize();
			setDimensions();
			return;
		}
		setTextSize(textSizeSP);
		invalidate();
	}

	private void setLayouts() {
		textPaint.getTextBounds(currentText, 0, currentText.length(), bounds);
		layout = new StaticLayout(currentText, textPaint, Width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
		invalidate();
	}

	private void decreaseTextSize() {
		textSizeSP -= 1;
		textSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, getResources().getDisplayMetrics());
		textPaint.setTextSize(textSizePix);
	}

	private void resetTextSize() {
		textSizeSP = textConstSizeSP;
		textSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, getResources().getDisplayMetrics());
		textPaint.setTextSize(textSizePix);
	}
}
