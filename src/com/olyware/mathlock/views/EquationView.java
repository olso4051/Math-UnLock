package com.olyware.mathlock.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

public class EquationView extends AutoResizeTextView {

	private EquationLayout layout;
	private int color = Color.WHITE;

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
		// TODO
	}

	// =========================================
	// Override Functions
	// =========================================

	@Override
	public void setText(CharSequence text, BufferType type) {
		boolean equation = false;
		if (text.charAt(0) == '$')
			if (text.length() > 1)
				if (text.charAt(1) != '$')
					equation = true;
				else
					text = text.subSequence(1, text.length());

		if (equation) {
			super.setText("", type);
			layout = new EquationLayout(String.valueOf(text), getTextAreaWidth(), getTextAreaHeight(), getTypeface(), color);
		} else {
			layout = null;
			super.setText(text, type);
		}
		invalidate();
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
			layout.draw(canvas);
			canvas.save();
		}
	}

	@Override
	public void setTextColor(int color) {
		this.color = color;
		super.setTextColor(color);
		if (layout != null)
			layout.setColor(color);
	}

}
