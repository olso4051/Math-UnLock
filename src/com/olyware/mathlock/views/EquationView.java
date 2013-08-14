package com.olyware.mathlock.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;

public class EquationView extends AutoResizeTextView {

	private EquationLayout layout;
	private TextPaint TopRightTextPaint;
	private boolean equation;
	private int color = Color.WHITE, TopRightTextSizeSP, TopRightTextSizePix;
	private float TopRightWidth;
	private String textTopRight;
	private Drawable dTopRight;

	// =========================================
	// Constructors
	// =========================================

	public EquationView(Context context) {
		super(context);
		initView();
	}

	public EquationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public EquationView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	// =========================================
	// Initialization
	// =========================================

	private void initView() {
		equation = false;
		color = Color.WHITE;
		textTopRight = "0";
		TopRightTextSizeSP = 25;
		TopRightTextSizePix = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, TopRightTextSizeSP, getResources()
				.getDisplayMetrics());
		TopRightTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		TopRightTextPaint.setTextAlign(Paint.Align.RIGHT);
		TopRightTextPaint.setColor(Color.WHITE);
		TopRightTextPaint.setTextSize(TopRightTextSizePix);
	}

	// =========================================
	// Override Functions
	// =========================================

	@Override
	public void setText(CharSequence text, BufferType type) {
		equation = false;
		if (text.charAt(0) == '$')
			if (text.length() > 1)
				if (text.charAt(1) != '$')
					equation = true;
				else
					text = text.subSequence(1, text.length());
		if (equation) {
			layout = new EquationLayout(String.valueOf(text), getTextAreaWidth(), getTextAreaHeight() - TopRightTextSizePix, getTypeface(),
					color, 40);
		} else {
			layout = null;
		}
		super.setText(text, type);
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (layout != null) {
			layout.setBounds(getTextAreaWidth(), getTextAreaHeight() - TopRightTextSizePix);
			invalidate();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (layout == null)
			super.onDraw(canvas);
		else {
			canvas.save();
			// canvas.drawRect(0, 0, getWidth(), getHeight(), getPaint());
			canvas.translate(getTextAreaWidth() / 2, getTextAreaHeight() / 2);
			layout.draw(canvas);
			canvas.restore();
			// canvas.save();
		}
		canvas.drawText(textTopRight, getWidth(), TopRightTextSizePix, TopRightTextPaint);
		canvas.save();
		canvas.translate(TopRightWidth, TopRightTextSizePix / 2);
		dTopRight.draw(canvas);
		canvas.restore();
		// canvas.save();
	}

	@Override
	public void setTextColor(int color) {
		this.color = color;
		super.setTextColor(color);
		if (layout != null)
			layout.setColor(color);
		invalidate();
	}

	public void setTopRightText(String text) {
		this.textTopRight = text;
		TopRightWidth = getWidth() - TopRightTextPaint.measureText(textTopRight) - dTopRight.getIntrinsicWidth() / 2;
		invalidate();
	}

	public void setTopRightDrawable(Drawable icon) {
		icon.setBounds(-icon.getIntrinsicWidth() / 2, -icon.getIntrinsicHeight() / 2, icon.getIntrinsicWidth() / 2,
				icon.getIntrinsicHeight() / 2);
		this.dTopRight = icon;
		TopRightWidth = getWidth() - TopRightTextPaint.measureText(textTopRight) - dTopRight.getIntrinsicWidth() / 2;
		invalidate();
	}

	public void setTopRightTypeface(Typeface font) {
		TopRightTextPaint.setTypeface(font);
	}
}
