package com.olyware.mathlock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class AnswerView extends View {

	private AnswerReadyListener listener;
	private int Width, Height;
	private int layout;
	private int textLabelSizeSP, textAnswerSizeSP;
	private float padVert, padHorz;
	private float textLabelSizePix, textAnswerSizePix;
	private String answers[] = { "N/A", "N/A", "N/A", "N/A" };
	final private String labels[] = { "A:", "B:", "C:", "D:" };

	private Paint TextLabelPaintL, TextLabelPaintR, TextAnswerPaintL, TextAnswerPaintR, TextAnswerPaintC;
	private TextPaint test;
	private float answerBoundsWidth[] = new float[4];
	private float labelBoundsWidth[] = new float[4];
	private float totalWidth;
	private float centersX[] = new float[4], centersY[] = new float[4];
	private Rect answerBounds[] = new Rect[4], labelBounds[] = new Rect[4];
	private StaticLayout layouts[] = new StaticLayout[4];
	private boolean measured = false;

	// =========================================
	// Constructors
	// =========================================

	public AnswerView(Context context) {
		super(context);
		initGraphView();
	}

	public AnswerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGraphView();
	}

	public AnswerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGraphView();
	}

	// =========================================
	// Initialization
	// =========================================

	private void initGraphView() {
		textLabelSizeSP = 30; // text size in scaled pixels
		textLabelSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textLabelSizeSP, getResources().getDisplayMetrics());
		TextLabelPaintL = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextLabelPaintL.setColor(Color.WHITE);
		TextLabelPaintL.setTextAlign(Paint.Align.LEFT);
		TextLabelPaintL.setTextSize(textLabelSizePix);
		TextLabelPaintR = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextLabelPaintR.setColor(Color.WHITE);
		TextLabelPaintR.setTextAlign(Paint.Align.RIGHT);
		TextLabelPaintR.setTextSize(textLabelSizePix);

		textAnswerSizeSP = 20;
		textAnswerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textAnswerSizeSP, getResources().getDisplayMetrics());
		TextAnswerPaintL = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextAnswerPaintL.setColor(Color.WHITE);
		TextAnswerPaintL.setTextAlign(Paint.Align.LEFT);
		TextAnswerPaintL.setTextSize(textAnswerSizePix);
		TextAnswerPaintR = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextAnswerPaintR.setColor(Color.WHITE);
		TextAnswerPaintR.setTextAlign(Paint.Align.RIGHT);
		TextAnswerPaintR.setTextSize(textAnswerSizePix);
		TextAnswerPaintC = new Paint(Paint.ANTI_ALIAS_FLAG);
		TextAnswerPaintC.setColor(Color.WHITE);
		TextAnswerPaintC.setTextAlign(Paint.Align.CENTER);
		TextAnswerPaintC.setTextSize(textAnswerSizePix);
		test = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
		test.setColor(Color.WHITE);
		test.setTextAlign(Paint.Align.LEFT);
		test.setTextSize(textAnswerSizePix);

		padVert = 5;
		padHorz = 0;
		for (int i = 0; i < answerBounds.length; i++) {
			answerBounds[i] = new Rect();
			labelBounds[i] = new Rect();
		}
		layout = 1;
	}

	// =========================================
	// Public Methods
	// =========================================

	public void setReadyListener(AnswerReadyListener listener) {
		this.listener = listener;
	}

	public void setAnswers(String answers[]) {
		this.answers = answers;
		if (measured)
			setDimensions();
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Here we make sure that we have a perfect circle
		Width = measure(widthMeasureSpec);
		Height = measure(heightMeasureSpec);

		setDimensions();
		// setMeasuredDimension(Width, h);
		measured = true;
		listener.Ready();
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

		if (layout == 4) {
			canvas.save();
			canvas.translate(labelBoundsWidth[0], centersY[0] - textAnswerSizePix); // position the text
			for (int i = 0; i < answers.length; i++) {
				layouts[i].draw(canvas);
				if (i < answers.length - 1)
					canvas.translate(labelBoundsWidth[i + 1] - labelBoundsWidth[i], layouts[i].getHeight() + padVert);
			}
			canvas.restore();
		} else {
			canvas.drawText(answers[0], centersX[0], centersY[0], TextAnswerPaintL);
			canvas.drawText(answers[1], centersX[1], centersY[1], TextAnswerPaintL);
			canvas.drawText(answers[2], centersX[2], centersY[2], TextAnswerPaintL);
			canvas.drawText(answers[3], centersX[3], centersY[3], TextAnswerPaintL);
		}
		canvas.drawText(labels[0], centersX[0], centersY[0], TextLabelPaintR);
		canvas.drawText(labels[1], centersX[1], centersY[1], TextLabelPaintR);
		canvas.drawText(labels[2], centersX[2], centersY[2], TextLabelPaintR);
		canvas.drawText(labels[3], centersX[3], centersY[3], TextLabelPaintR);
		canvas.save();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	// =========================================
	// Private Methods
	// =========================================

	private void setDimensions() {
		float maxW = getMaxWidth();

		if (maxW < Width / 4) {
			Height = (int) textLabelSizePix;
			setMeasuredDimension(Width, Height);// labelBounds[0].height());
			layout = 1;
			centersX[0] = labelBoundsWidth[0];
			centersX[1] = centersX[0] + answerBoundsWidth[0] + padHorz + labelBoundsWidth[0];
			centersX[2] = centersX[1] + answerBoundsWidth[1] + padHorz + labelBoundsWidth[1];
			centersX[3] = centersX[2] + answerBoundsWidth[2] + padHorz + labelBoundsWidth[2];
			centersY[0] = textLabelSizePix;
			centersY[1] = textLabelSizePix;
			centersY[2] = textLabelSizePix;
			centersY[3] = textLabelSizePix;
		} else if (maxW < Width / 2) {
			Height = (int) (textLabelSizePix * 2 + padVert);
			setMeasuredDimension(Width, Height);
			centersX[0] = labelBounds[0].width();
			centersX[1] = Width / 2 + labelBoundsWidth[1];
			centersX[2] = labelBounds[2].width();
			centersX[3] = Width / 2 + labelBoundsWidth[3];
			centersY[0] = textLabelSizePix;
			centersY[1] = textLabelSizePix;
			centersY[2] = textLabelSizePix * 2 + padVert;
			centersY[3] = textLabelSizePix * 2 + padVert;
			layout = 2;
		} else {
			int totalHeight = 0;
			centersY[0] = textLabelSizePix;
			for (int i = 0; i < layouts.length; i++) {
				totalHeight += layouts[i].getHeight();
				centersX[i] = labelBoundsWidth[i];
				if (i > 0)
					centersY[i] = layouts[i - 1].getHeight() + centersY[i - 1] + padVert;
			}
			Height = totalHeight + (int) (padVert * 3);
			setMeasuredDimension(Width, Height);
			layout = 4;
		}
		invalidate();
	}

	private float getMaxWidth() {
		float max = 0;
		float temp = 0;
		totalWidth = 0;

		for (int i = 0; i < answers.length; i++) {
			answerBoundsWidth[i] = TextAnswerPaintL.measureText(answers[i]);
			labelBoundsWidth[i] = TextLabelPaintL.measureText(labels[i]);
			TextAnswerPaintL.getTextBounds(answers[i], 0, answers[i].length(), answerBounds[i]);
			TextLabelPaintL.getTextBounds(labels[i], 0, labels[i].length(), labelBounds[i]);
			layouts[i] = new StaticLayout(answers[i], test, Width - (int) Math.abs(labelBoundsWidth[i]), Layout.Alignment.ALIGN_NORMAL,
					1.3f, 0, false);
			temp = Math.abs(answerBoundsWidth[i]) + Math.abs(labelBoundsWidth[i]);
			totalWidth += temp;
			if (temp > max)
				max = temp;
		}
		padHorz = (Width - totalWidth) / 3;
		return max;
	}
}
