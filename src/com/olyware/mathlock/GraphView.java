package com.olyware.mathlock;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GraphView extends View {
	private final String TAG = "GraphView";
	private GraphViewListener listener;
	private ArrayList<Integer> percent = new ArrayList<Integer>();
	private int Width, Height;
	private int movingAverage;

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

	public void setMovingAverage(int movingAverage) {
		this.movingAverage = movingAverage;
		setMovingAveragePercent();
	}

	public void setArray(int arrayInPercent[]) {
		// clear all emlements in percentage array
		percent.clear();

		// set all elements in percentage array
		for (int i = 0; i < arrayInPercent.length; i++)
			percent.add(arrayInPercent[i]);

		// check to see if the graph will fit all the data nicely on the screen
		// if not increase moving average size
		while (Width / percent.size() < 10) {
			movingAverage = movingAverage * 2;
			setMovingAveragePercent();
		}
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Here we make sure that we have a perfect circle
		Width = measure(widthMeasureSpec);
		Height = measure(heightMeasureSpec);

		setMeasuredDimension(Width, Height);
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

	// =========================================
	// Private Methods
	// =========================================

	private void setMovingAveragePercent() {

	}
}
