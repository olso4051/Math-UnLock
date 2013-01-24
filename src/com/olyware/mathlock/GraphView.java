package com.olyware.mathlock;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class GraphView extends View {
	private final String TAG = "GraphView";
	private GraphViewListener listener;
	private ArrayList<Integer> percent = new ArrayList<Integer>();
	private ArrayList<Integer> percentAve = new ArrayList<Integer>();
	private int Width, Height;
	private int movingAverage;
	private long StatsValues[] = new long[8];
	private String Stats[] = { "Total Correct Answers", "Total Incorrect Answers", "(+/-) Coins", "Best Correct Streak", "Current Streak",
			"Total Time Spent Studying", "Fastest Time to Answer", "Average Time to Answer" };
	private int pad;
	private int textLabelSizeSP, textLabelSizePix, textStatsSizeSP, textStatsSizePix;
	private Paint TextLabelPaint, TextStatsPaintL, TextStatsPaintR, TextStatsPaintC, GraphPaint, LinePaint;
	private Rect textBounds;
	private Path Xlabel;

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
		textLabelSizeSP = 40; // text size in scaled pixels
		textLabelSizePix = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textLabelSizeSP, getResources().getDisplayMetrics());
		TextLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextLabelPaint.setColor(Color.WHITE);
		TextLabelPaint.setTextAlign(Paint.Align.CENTER);
		TextLabelPaint.setTextSize(textLabelSizePix);

		textStatsSizeSP = 20;
		textStatsSizePix = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textStatsSizeSP, getResources().getDisplayMetrics());
		TextStatsPaintL = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextStatsPaintL.setColor(Color.WHITE);
		TextStatsPaintL.setTextAlign(Paint.Align.LEFT);
		TextStatsPaintL.setTextSize(textStatsSizePix);
		TextStatsPaintR = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextStatsPaintR.setColor(Color.WHITE);
		TextStatsPaintR.setTextAlign(Paint.Align.RIGHT);
		TextStatsPaintR.setTextSize(textStatsSizePix);
		TextStatsPaintC = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextStatsPaintC.setColor(Color.WHITE);
		TextStatsPaintC.setTextAlign(Paint.Align.CENTER);
		TextStatsPaintC.setTextSize(textStatsSizePix);

		movingAverage = 1;
		setStats(0, 0, 0, 0, 0, 0, 0, 0);
		pad = 4;
		textBounds = new Rect();
		Xlabel = new Path();

		GraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		GraphPaint.setColor(Color.WHITE);
		GraphPaint.setStyle(Paint.Style.STROKE);
		GraphPaint.setStrokeWidth(pad);

		LinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		LinePaint.setColor(Color.CYAN);
		LinePaint.setStyle(Paint.Style.STROKE);
		LinePaint.setStrokeWidth(pad);
	}

	// =========================================
	// Public Methods
	// =========================================

	public void setGraphViewListener(GraphViewListener listener) {
		this.listener = listener;
	}

	public void setMovingAverage(int movingAverage) {
		this.movingAverage = movingAverage;
		if (!percent.isEmpty())
			setMovingAveragePercent();
	}

	public void setArray(int arrayInPercent[]) {
		// clear all elements in percentage array
		percent.clear();
		percent.ensureCapacity(arrayInPercent.length);

		// set all elements in percentage array
		for (int i = 0; i < arrayInPercent.length; i++)
			percent.add(arrayInPercent[i]);

		setMovingAveragePercent();
		// check to see if the graph will fit all the data nicely on the screen
		// if not increase moving average size
		// while (Width / percent.size() < 10) {
		// movingAverage = movingAverage * 2;
		// setMovingAveragePercent();
		// }
	}

	@SuppressLint("NewApi")
	public void setStats(long correct, long wrong, long coins, long totalTime, long streakBest, long streakCurrent, long answerTimeFast,
			long answerTimeAve) {
		StatsValues[0] = correct;
		StatsValues[1] = wrong;
		StatsValues[2] = coins;
		StatsValues[3] = streakBest;
		StatsValues[4] = streakCurrent;
		StatsValues[5] = totalTime;
		StatsValues[6] = answerTimeFast;
		StatsValues[7] = answerTimeAve;
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Here we make sure that we have a perfect circle
		Width = measure(widthMeasureSpec);
		Height = measure(heightMeasureSpec);
		setStats(StatsValues[0], StatsValues[1], StatsValues[2], StatsValues[3], StatsValues[4], StatsValues[5], StatsValues[6],
				StatsValues[7]);
		Xlabel.rewind();
		Xlabel.moveTo(textStatsSizePix, Width / 2);
		Xlabel.lineTo(textStatsSizePix, 0);
		Height = Width / 2 + textLabelSizePix + textStatsSizePix * (Stats.length + 2) + pad * (Stats.length + 2);
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
		int padNum = Stats.length + 1;
		int StatsNum = Stats.length;
		String s = "100";
		TextStatsPaintR.getTextBounds(s, 0, s.length(), textBounds);
		int left = textStatsSizePix + pad + textBounds.width();
		int bottom = Width / 2;

		canvas.drawText("100", left, textStatsSizePix, TextStatsPaintR);
		canvas.drawText("50", left, (bottom + textStatsSizePix) / 2, TextStatsPaintR);
		canvas.drawText("0", left, bottom, TextStatsPaintR);
		canvas.drawTextOnPath("Percent", Xlabel, 0, 0, TextStatsPaintC);
		canvas.drawText("Time", (Width + left) / 2, bottom + textStatsSizePix * 2 + pad, TextStatsPaintC);
		canvas.drawText("Today", Width - pad, bottom + textStatsSizePix, TextStatsPaintR);
		canvas.drawLine(left + pad, 0, left + pad, bottom - pad / 2, GraphPaint);
		canvas.drawLine(Width - pad, 0, Width - pad, bottom - pad / 2, GraphPaint);
		// TODO draw percentage path
		canvas.drawLine(left + pad, bottom - pad, Width - pad, bottom - pad, GraphPaint);
		canvas.drawLine(left + pad, pad / 2, Width - pad, pad / 2, GraphPaint);

		canvas.drawText("All Time Stats", Width / 2, Height - textStatsSizePix * StatsNum - pad * padNum, TextLabelPaint);
		for (int i = 0; i < StatsNum; i++) {
			canvas.drawText(Stats[i], 0, Height - textStatsSizePix * (StatsNum - i - 1) - pad * (padNum - i), TextStatsPaintL);
			canvas.drawText(StatsValues[i] + " ", Width, Height - textStatsSizePix * (StatsNum - i - 1) - pad * (padNum - i),
					TextStatsPaintR);
		}
		canvas.save();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	// =========================================
	// Private Methods
	// =========================================

	private void setMovingAveragePercent() {
		Object temp[] = percent.toArray();
		int tempPercent[] = new int[temp.length];
		percentAve.clear();
		percentAve.ensureCapacity(tempPercent.length - movingAverage + 1);

		for (int i = 0; i < temp.length; i++)
			tempPercent[i] = ((Integer) temp[i]).intValue();

		for (int i = 0; i < tempPercent.length - movingAverage + 1; i++) {
			int sum = 0;
			for (int a = 0; a < movingAverage; a++) {
				sum += tempPercent[i + a];
			}
			percentAve.add(sum / movingAverage);
		}
		// TODO set percentage path
	}
}
