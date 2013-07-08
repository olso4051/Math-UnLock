package com.olyware.mathlock.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

public class EquationView extends AutoResizeTextView {

	private EquationLayout layout;
	private int color = Color.WHITE;
	private int offset = 0;

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
			layout = new EquationLayout(String.valueOf(text), getTextAreaWidth(), getTextAreaHeight() - offset * 2, getTypeface(), color);
			layout.setDefaultSize(30);
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
			layout.setBounds(getTextAreaWidth(), getTextAreaHeight());
			invalidate();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (layout == null)
			super.onDraw(canvas);
		else {
			canvas.save();
			canvas.translate(getTextAreaWidth() / 2, getTextAreaHeight() / 2 + offset);
			layout.draw(canvas);
			canvas.restore();
			canvas.save();
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

	public void setDefaultSize(int size) {
		layout.setDefaultSize(size);
		invalidate();
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
}
