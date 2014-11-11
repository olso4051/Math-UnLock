package com.olyware.mathlock.views;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.olyware.mathlock.R;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.ui.Typefaces;

public class GraphView extends View {
	private ArrayList<Integer> percent = new ArrayList<Integer>();
	private ArrayList<Integer> percentAve = new ArrayList<Integer>();
	private int Width, Height;
	private int movingAverage;
	private int textLabelSizeSP, textStatsSizeSP;
	private float padVert, padHorz, padScrollPix;
	private float textLabelSizePix, textStatsSizePix;
	private float left, top, right, bottom;
	private Typeface font;

	private String Stats[] = { "Average Difficulty", "Correct Answers", "Incorrect Answers", "(+/-) Coins", "Best Streak",
			"Current Streak", "Total Study Time", "Fastest Time", "Average Time", "coins/hr (cph)", "Eggs Found" };
	private String StatsValues[] = new String[Stats.length];

	private Paint TextLabelPaint, TextStatsPaintL, TextStatsPaintR, TextStatsPaintC, GraphPaint, LinePaint;
	private Rect textBounds;
	private Path Xlabel;
	private Path percentPath;

	private Context ctx;

	// =========================================
	// Constructors
	// =========================================

	public GraphView(Context context) {
		super(context);
		initGraphView(context);
	}

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGraphView(context);
	}

	public GraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGraphView(context);
	}

	// =========================================
	// Initialization
	// =========================================

	private void initGraphView(Context ctx) {
		this.ctx = ctx;
		Typefaces typefaces = Typefaces.getInstance(this.ctx);
		font = typefaces.robotoLight;

		textLabelSizeSP = 40; // text size in scaled pixels
		textLabelSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textLabelSizeSP, getResources().getDisplayMetrics());
		TextLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextLabelPaint.setColor(Color.WHITE);
		TextLabelPaint.setTextAlign(Paint.Align.CENTER);
		TextLabelPaint.setTextSize(textLabelSizePix);
		TextLabelPaint.setTypeface(font);

		textStatsSizeSP = 20;
		textStatsSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textStatsSizeSP, getResources().getDisplayMetrics());
		TextStatsPaintL = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextStatsPaintL.setColor(Color.BLACK);
		TextStatsPaintL.setTextAlign(Paint.Align.LEFT);
		TextStatsPaintL.setTextSize(textStatsSizePix);
		TextStatsPaintL.setTypeface(font);
		TextStatsPaintR = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextStatsPaintR.setColor(getResources().getColor(R.color.graph_text_color_blue));
		TextStatsPaintR.setTextAlign(Paint.Align.RIGHT);
		TextStatsPaintR.setTextSize(textStatsSizePix);
		TextStatsPaintR.setTypeface(font);
		TextStatsPaintC = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextStatsPaintC.setColor(Color.BLACK);
		TextStatsPaintC.setTextAlign(Paint.Align.CENTER);
		TextStatsPaintC.setTextSize(textStatsSizePix);
		TextStatsPaintC.setTypeface(font);

		movingAverage = 20;
		setStats(0, 0, 0, 0, 0, 0, 0, 0, 0, "0 / 0");
		padVert = textStatsSizePix / 2;
		padHorz = 30;
		padScrollPix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padHorz, getResources().getDisplayMetrics());
		textBounds = new Rect();
		Xlabel = new Path();
		percentPath = new Path();

		GraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		GraphPaint.setColor(getResources().getColor(R.color.light_white_seperator));
		GraphPaint.setStyle(Paint.Style.STROKE);
		GraphPaint.setStrokeWidth(3);

		LinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		LinePaint.setColor(ctx.getResources().getColor(R.color.light_blue));
		LinePaint.setStyle(Paint.Style.STROKE);
		LinePaint.setStrokeWidth(5);
	}

	// =========================================
	// Public Methods
	// =========================================

	public void setTypeface(Typeface font) {
		this.font = font;
		TextLabelPaint.setTypeface(font);
		TextStatsPaintL.setTypeface(font);
		TextStatsPaintR.setTypeface(font);
		TextStatsPaintC.setTypeface(font);
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

	public void setStats(int difficultyAve, int correct, int wrong, int coins, long totalTime, int streakBest, int streakCurrent,
			long answerTimeFast, long answerTimeAve, String eggs) {
		StatsValues[0] = Difficulty.fromValueToString(difficultyAve);
		StatsValues[1] = correct + "";
		StatsValues[2] = wrong + "";
		StatsValues[3] = coins + "";
		StatsValues[4] = streakBest + "";
		StatsValues[5] = streakCurrent + "";
		StatsValues[6] = format(totalTime);
		StatsValues[7] = format(answerTimeFast);
		StatsValues[8] = format(answerTimeAve);
		if (totalTime != 0)
			StatsValues[9] = coins * 1000l * 60l * 60l / totalTime + "";
		else
			StatsValues[9] = 0 + "";
		StatsValues[10] = eggs;
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Here we make sure that we have a perfect circle
		Width = measure(widthMeasureSpec);
		Height = measure(heightMeasureSpec);

		// setStats(StatsValues[0], StatsValues[1], StatsValues[2], StatsValues[3], StatsValues[4], StatsValues[5], StatsValues[6],
		// StatsValues[7]);
		Xlabel.rewind();
		Xlabel.moveTo(textStatsSizePix, Width / 2);
		Xlabel.lineTo(textStatsSizePix, 0);
		String s = "100";
		TextStatsPaintR.getTextBounds(s, 0, s.length(), textBounds);
		left = textStatsSizePix + padHorz * 2 + textBounds.width();
		top = padVert;
		right = Width - left + textStatsSizePix;
		bottom = Width / 2;

		Height = (int) (Width / 2 + textLabelSizePix + textStatsSizePix * (Stats.length + 1) + padVert * (Stats.length + 2));
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

		canvas.drawText("100", padScrollPix, padVert + textStatsSizePix / 2, TextStatsPaintC);
		canvas.drawText("75", padScrollPix, (bottom - padVert) / 4 + padVert + textStatsSizePix / 2, TextStatsPaintC);
		canvas.drawText("50", padScrollPix, (bottom - padVert) / 2 + padVert + textStatsSizePix / 2, TextStatsPaintC);
		canvas.drawText("25", padScrollPix, (bottom - padVert) * 3 / 4 + padVert + textStatsSizePix / 2, TextStatsPaintC);
		canvas.drawText("0", padScrollPix, bottom + textStatsSizePix / 2, TextStatsPaintC);
		canvas.drawText("%", padScrollPix, bottom + textStatsSizePix * 2 + padVert, TextStatsPaintC);
		// canvas.drawTextOnPath("Percent", Xlabel, 0, 0, TextStatsPaintC);
		// canvas.drawText("Time", Width / 2, bottom + textStatsSizePix + padVert, TextStatsPaintC);

		// canvas.drawRect(left, top, right, bottom, GraphPaint);
		// canvas.drawLine(left + padVert, 0, left + padVert, bottom - padVert / 2, GraphPaint);
		// canvas.drawLine(Width - padVert, 0, Width - padVert, bottom - padVert / 2, GraphPaint);
		if (!percentPath.isEmpty())
			canvas.drawPath(percentPath, LinePaint);
		// canvas.drawLine(left + padVert, bottom - padVert, Width - padVert, bottom - padVert, GraphPaint);
		// canvas.drawLine(left + padVert, padVert / 2, Width - padVert, padVert / 2, GraphPaint);

		canvas.drawLine(0, Height - textStatsSizePix * StatsNum - padVert * padNum, Width, Height - textStatsSizePix * StatsNum - padVert
				* padNum, GraphPaint);
		// canvas.drawText("All Time Stats", Width / 2, Height - textStatsSizePix * StatsNum - padVert * padNum, TextLabelPaint);
		for (int i = 0; i < StatsNum; i++) {
			canvas.drawText(Stats[i], padScrollPix, Height - textStatsSizePix * (StatsNum - i - 1) - padVert * (padNum - i - 1),
					TextStatsPaintL);
			canvas.drawText(StatsValues[i], Width - padScrollPix, Height - textStatsSizePix * (StatsNum - i - 1) - padVert
					* (padNum - i - 1), TextStatsPaintR);
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

	private String format(long time) {
		long minute = 1000 * 60;	// ms in a minute
		long hour = minute * 60;	// ms in an hour
		long day = hour * 24;		// ms in a day
		long year = day * 365;		// ms in a year
		long sec = time % (minute);						// # of seconds in ms
		long min = (time - sec) % (hour);				// # of minutes in ms
		long hr = (time - min - sec) % (day);			// # of hrs in ms
		long days = (time - hr - min - sec) % (year);	// # of days in ms
		long years = (time - days - hr - min - sec);	// # of years in ms
		min = min / minute;
		hr = hr / hour;
		days = days / day;
		years = years / year;
		if (time < minute)
			return String.format(Locale.ENGLISH, "%.2fs", sec / 1000f);
		else if (time < hour)
			return String.format(Locale.ENGLISH, "%dm%.2fs", min, sec / 1000f);
		else if (time < day)
			return String.format(Locale.ENGLISH, "%dh%dm%.2fs", hr, min, sec / 1000f);
		else if (time < year)
			return String.format(Locale.ENGLISH, "%dd%dh%dm%.2fs", days, hr, min, sec / 1000f);
		else
			return String.format(Locale.ENGLISH, "%dy%dd%dh%dm%.2fs", years, days, hr, min, sec / 1000f);
	}

	private void setMovingAveragePercent() {
		Object temp[] = percent.toArray();
		int tempPercent[] = new int[temp.length];
		int size = tempPercent.length - movingAverage + 1;
		percentAve.clear();
		percentAve.ensureCapacity(size);
		percentPath.rewind();

		for (int i = 0; i < temp.length; i++)
			tempPercent[i] = ((Integer) temp[i]).intValue();

		if (movingAverage >= tempPercent.length) {
			int sum = 0;
			for (int i = 0; i < tempPercent.length; i++)
				sum += tempPercent[i];
			percentAve.add(sum / tempPercent.length);
			percentPath.moveTo(left, (1 - percentAve.get(0) / 100f) * (bottom - top) + top);
			percentPath.lineTo(right, (1 - percentAve.get(0) / 100f) * (bottom - top) + top);
		} else {
			for (int i = 0; i < size; i++) {
				int sum = 0;
				for (int a = 0; a < movingAverage; a++) {
					sum += tempPercent[i + a];
				}
				percentAve.add(sum / movingAverage);
				if (i == 0)
					percentPath.moveTo(left, (1 - percentAve.get(0) / 100f) * (bottom - top) + top);
				else
					percentPath.lineTo((right - left) * i / (size - 1) + left, (1 - percentAve.get(i) / 100f) * (bottom - top) + top);
			}
		}
		invalidate();
	}
}
