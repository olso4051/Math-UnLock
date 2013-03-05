package com.olyware.mathlock.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class EquationView extends AutoResizeTextView {

	private EquationLayout layout;
	private Context ctx;

	// =========================================
	// Constructors
	// =========================================

	public EquationView(Context context) {
		super(context);
		ctx = context;
		initView();
	}

	public EquationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;
		initView();
	}

	public EquationView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ctx = context;
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
		if (text.charAt(0) != '$') {		// string starts with $ then it is a equation
			layout = null;
			super.setText(text, type);
		} else {
			super.setText("", type);
			layout = new EquationLayout(String.valueOf(text), getTextAreaWidth(), getTextAreaHeight());
		}
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
