package com.olyware.mathlock;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GraphView extends View {
	private final String TAG = "GraphView";
	private GraphViewListener listener;

	// =========================================
	// Constructors
	// =========================================

	public GraphView(Context context) {
		super(context);
		initGraphView();
	}

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGraphView();
	}

	public GraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGraphView();
	}

	// =========================================
	// Initialization
	// =========================================

	private void initGraphView() {

	}

	// =========================================
	// Public Methods
	// =========================================

	public void setGraphViewListener(GraphViewListener listener) {
		this.listener = listener;
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Here we make sure that we have a perfect circle
		int W = measure(widthMeasureSpec);
		int H = measure(heightMeasureSpec);

		setMeasuredDimension(W, H);
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
		int W = getMeasuredWidth();
		int H = getMeasuredHeight();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

}
