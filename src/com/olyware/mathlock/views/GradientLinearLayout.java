package com.olyware.mathlock.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class GradientLinearLayout extends LinearLayout {

	private Paint gradientPaint;

	// =========================================
	// Constructors
	// =========================================

	public GradientLinearLayout(Context context) {
		super(context);
		initView();
	}

	public GradientLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	// =========================================
	// Initialization
	// =========================================

	private void initView() {
		gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		gradientPaint.setStyle(Paint.Style.FILL);
	}

	// =========================================
	// Override Functions
	// =========================================

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		gradientPaint.setShader(new LinearGradient(0, 0, 0, Math.min(getWidth(), getHeight()), Color.BLACK, Color.TRANSPARENT,
				TileMode.CLAMP));
	}
}
