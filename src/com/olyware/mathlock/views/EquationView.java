package com.olyware.mathlock.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

public class EquationView extends AutoResizeTextView {

	private EquationLayout layout;
	private boolean equation;
	private int color = Color.WHITE, alpha = 255, textSizeSPDefault = 30;

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
		alpha = 255;
	}

	// =========================================
	// Override Functions
	// =========================================

	@Override
	public void setText(CharSequence text, BufferType type) {
		equation = false;
		if (text.length() > 1)
			if (text.charAt(0) == '$')
				if (text.length() > 1)
					if (text.charAt(1) != '$')
						equation = true;
					else
						text = text.subSequence(1, text.length());
		if (equation) {
			layout = new EquationLayout(String.valueOf(text), getTextAreaWidth(), getTextAreaHeight(), getTypeface(), color, alpha,
					textSizeSPDefault);
		} else {
			layout = null;
		}
		super.setText(text, type);
		invalidate();
	}

	public String getReadableText() {
		if (equation) {
			return layout.getReadableText();
		} else
			return getText().toString();
	}

	public String getOriginalText() {
		if (equation)
			return layout.getOriginalText();
		else
			return getText().toString();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (layout != null) {
			layout.setBounds(getTextAreaWidth(), getTextAreaHeight());
			invalidate();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (layout == null)
			super.onDraw(canvas);
		else {
			canvas.translate(getTextAreaWidth() / 2, getTextAreaHeight() / 2);
			layout.setAlpha(alpha);
			layout.draw(canvas);
		}
	}

	@Override
	public void setTextColor(int color) {
		this.color = color;
		super.setTextColor(color);
		if (layout != null)
			layout.setColor(color);
		invalidate();
	}

	@Override
	public void setAlpha(float alpha) {
		this.alpha = (int) (alpha * 255);
		super.setAlpha(alpha);
		if (layout != null)
			layout.setAlpha(this.alpha);
		invalidate();
	}

}
