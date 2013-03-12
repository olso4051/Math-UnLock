package com.olyware.mathlock.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;

public class EquationView extends AutoResizeTextView {

	private EquationLayout layout;

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
		Log.d("test", "text = " + text);
		if (text.charAt(0) == '$')
			if (text.length() > 1)
				if (text.charAt(1) != '$')
					equation = true;
				else
					text = text.subSequence(1, text.length());

		if (equation) {
			super.setText("", type);
			layout = new EquationLayout(String.valueOf(text), getTextAreaWidth(), getTextAreaHeight(), getTypeface());
		} else {
			layout = null;
			super.setText(text, type);
		}
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (layout == null)
			super.onDraw(canvas);
		else {
			layout.draw(canvas);
		}
	}
}
