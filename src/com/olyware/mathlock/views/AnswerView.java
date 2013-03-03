package com.olyware.mathlock.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class AnswerView extends View {

	private AnswerReadyListener listener;
	private int Width, Height;
	private int maxHeight = 0;
	private int layout;
	private final int textConstLabelSizeSP = 30, textConstAnswerSizeSP = 25;
	private int type = 0;
	private int textLabelSizeSP, textAnswerSizeSP;
	private int correctAnswer, wrongAnswer;
	final private int maxAnswers = 4;
	private float padVert, padHorz;
	private float textLabelSizePix, textAnswerSizePix;
	private float answerBoundsWidth[] = new float[maxAnswers];
	private float labelBoundsWidth[] = new float[maxAnswers];
	private float totalWidth, maxW;
	private float Widths[] = new float[maxAnswers];
	private float centersX[] = new float[maxAnswers], centersY[] = new float[maxAnswers];
	private String answers[] = { "N/A", "N/A", "N/A", "N/A" };
	final private String labels[] = { "A) ", "B) ", "C) ", "D) ", "E) " };

	private TextPaint TextLabelPaintR, TextAnswerPaintL, correctAnswerPaint, correctLabelPaint, wrongAnswerPaint, wrongLabelPaint;

	private Rect answerBounds[] = new Rect[maxAnswers], labelBounds[] = new Rect[maxAnswers];
	private StaticLayout layouts[] = new StaticLayout[maxAnswers];
	private boolean measured = false;

	// =========================================
	// Constructors
	// =========================================

	public AnswerView(Context context) {
		super(context);
		initAnswerView();
	}

	public AnswerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAnswerView();
	}

	public AnswerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initAnswerView();
	}

	// =========================================
	// Initialization
	// =========================================

	private void initAnswerView() {
		textLabelSizeSP = textConstLabelSizeSP; // text size in scaled pixels
		textLabelSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textLabelSizeSP, getResources().getDisplayMetrics());
		TextLabelPaintR = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		TextLabelPaintR.setColor(Color.WHITE);
		TextLabelPaintR.setTextAlign(Paint.Align.RIGHT);
		TextLabelPaintR.setTextSize(textLabelSizePix);
		correctLabelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		correctLabelPaint.setColor(Color.GREEN);
		correctLabelPaint.setTextAlign(Paint.Align.RIGHT);
		correctLabelPaint.setTextSize(textLabelSizePix);
		wrongLabelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		wrongLabelPaint.setColor(Color.RED);
		wrongLabelPaint.setTextAlign(Paint.Align.RIGHT);
		wrongLabelPaint.setTextSize(textLabelSizePix);

		textAnswerSizeSP = textConstAnswerSizeSP;
		textAnswerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textAnswerSizeSP, getResources().getDisplayMetrics());
		TextAnswerPaintL = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		TextAnswerPaintL.setColor(Color.WHITE);
		TextAnswerPaintL.setTextAlign(Paint.Align.LEFT);
		TextAnswerPaintL.setTextSize(textAnswerSizePix);
		correctAnswerPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		correctAnswerPaint.setColor(Color.GREEN);
		correctAnswerPaint.setTextAlign(Paint.Align.LEFT);
		correctAnswerPaint.setTextSize(textAnswerSizePix);
		wrongAnswerPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		wrongAnswerPaint.setColor(Color.RED);
		wrongAnswerPaint.setTextAlign(Paint.Align.LEFT);
		wrongAnswerPaint.setTextSize(textAnswerSizePix);

		padVert = 5;
		padHorz = 0;
		for (int i = 0; i < answerBounds.length; i++) {
			answerBounds[i] = new Rect();
			labelBounds[i] = new Rect();
		}
		layout = 1;
		listener = new AnswerReadyListener() {
			@Override
			public void Ready() {
			}
		};

		correctAnswer = maxAnswers;
		wrongAnswer = maxAnswers;
		maxHeight = 0;
		String temp[] = { "test fine I'll try a couple more", "test", "test", "test" };
		setAnswers(temp);
	}

	// =========================================
	// Public Methods
	// =========================================

	public void setReadyListener(AnswerReadyListener listener) {
		this.listener = listener;
	}

	public void setTypeface(Typeface font) {
		TextLabelPaintR.setTypeface(font);
		TextAnswerPaintL.setTypeface(font);
		correctAnswerPaint.setTypeface(font);
		correctLabelPaint.setTypeface(font);
		wrongAnswerPaint.setTypeface(font);
		wrongLabelPaint.setTypeface(font);
	}

	public void setColor(int color) {
		TextLabelPaintR.setColor(color);
		TextAnswerPaintL.setColor(color);
		TextAnswerPaintL.setColor(color);
	}

	public void setUnlockType(int type) {
		this.type = type;
		switch (type) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			resetTextSize();
			break;
		}
		if (measured)
			setDimensions(false);
	}

	public void setCorrectAnswer(int location) {
		this.correctAnswer = location;
		setLayouts();
	}

	public void setIncorrectGuess(int location) {
		this.wrongAnswer = location;
		setLayouts();
	}

	public void resetGuess() {
		this.correctAnswer = maxAnswers;
		this.wrongAnswer = maxAnswers;
	}

	public void setAnswers(String answers[]) {
		this.answers = answers;
		resetTextSize();
		if (measured)
			setDimensions(false);
	}

	public String[] getAnswers() {
		return answers;
	}

	public void setParentHeight(int h) {
		this.maxHeight = h / 4;
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Here we get the width and height
		Width = measure(widthMeasureSpec);
		Height = measure(heightMeasureSpec);

		setDimensions(true);
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
		switch (type) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			if (layout == 4) {
				for (int i = 0; i < answers.length; i++) {
					canvas.save();
					canvas.translate(centersX[i], centersY[i] - textAnswerSizePix); // position the text
					layouts[i].draw(canvas);
					canvas.restore();
				}
			} else {
				for (int i = 0; i < answers.length; i++) {
					if (i == correctAnswer)
						canvas.drawText(answers[i], centersX[i], centersY[i], correctAnswerPaint);
					else if (i == wrongAnswer)
						canvas.drawText(answers[i], centersX[i], centersY[i], wrongAnswerPaint);
					else
						canvas.drawText(answers[i], centersX[i], centersY[i], TextAnswerPaintL);
				}
			}
			for (int i = 0; i < answers.length; i++) {
				if (i == correctAnswer)
					canvas.drawText(labels[i], centersX[i], centersY[i], correctLabelPaint);
				else if (i == wrongAnswer)
					canvas.drawText(labels[i], centersX[i], centersY[i], wrongLabelPaint);
				else
					canvas.drawText(labels[i], centersX[i], centersY[i], TextLabelPaintR);
			}
			break;
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

	private void setDimensions(boolean fromOnMeasure) {
		switch (type) {
		case 0:
		case 1:
			Height = 0;
			Height = MeasureSpec.makeMeasureSpec(Height, MeasureSpec.UNSPECIFIED);
			setMeasuredDimension(Width, Height);
			setLayoutParams(new LinearLayout.LayoutParams(Width, Height));
			break;
		case 2:
			setLayouts();
			float maxH = textLabelSizePix;
			for (int i = 0; i < answers.length; i++)
				maxH = Math.max(labelBounds[i].height(), maxH);

			float minH = Math.min(maxH, textLabelSizePix);
			for (int i = 0; i < answers.length; i++)
				minH = Math.min(labelBounds[i].height(), minH);

			if (maxW < Width / answers.length) {
				Height = (int) maxH;
				padHorz = (Width - totalWidth) / (answers.length + 1);
				layout = 1;
				Height = MeasureSpec.makeMeasureSpec(Height, MeasureSpec.UNSPECIFIED);
				if ((Height > maxHeight) && (maxHeight > 0) && !fromOnMeasure) {
					decreaseTextSize();
					setDimensions(false);
					return;
				}
				setMeasuredDimension(Width, Height);
				setLayoutParams(new LinearLayout.LayoutParams(Width, Height));
				centersX[0] = labelBoundsWidth[0] + padHorz;
				centersX[1] = centersX[0] + answerBoundsWidth[0] + padHorz + labelBoundsWidth[0];
				centersX[2] = centersX[1] + answerBoundsWidth[1] + padHorz + labelBoundsWidth[1];
				centersX[3] = centersX[2] + answerBoundsWidth[2] + padHorz + labelBoundsWidth[2];
				// centersX[4] = centersX[3] + answerBoundsWidth[3] + padHorz + labelBoundsWidth[3];
				centersY[0] = minH;
				centersY[1] = minH;
				centersY[2] = minH;
				centersY[3] = minH;
				// centersY[4] = minH;
			} else if ((answers.length == 5) && (maxW < Width / 3)) {
				Height = (int) (maxH * 2 + padVert);
				totalWidth = 3 * maxW;
				padHorz = (Width - totalWidth) / 4;
				layout = 2;
				Height = MeasureSpec.makeMeasureSpec(Height, MeasureSpec.UNSPECIFIED);
				if ((Height > maxHeight) && (maxHeight > 0) && !fromOnMeasure) {
					decreaseTextSize();
					setDimensions(false);
					return;
				}
				setMeasuredDimension(Width, Height);
				setLayoutParams(new LinearLayout.LayoutParams(Width, Height));
				centersX[0] = labelBounds[0].width() + padHorz;
				centersX[1] = centersX[0] + maxW + padHorz;
				centersX[2] = centersX[1] + maxW + padHorz;
				centersX[3] = centersX[0];
				// centersX[4] = centersX[1];
				centersY[0] = minH;
				centersY[1] = minH;
				centersY[2] = minH;
				centersY[3] = minH * 2 + padVert;
				// centersY[4] = minH * 2 + padVert;
			} else if (maxW < Width / 2) {
				int lines = Math.round((float) answers.length / 2);
				Height = (int) (maxH * lines + padVert * (lines - 1));
				totalWidth = 2 * maxW;
				padHorz = (Width - totalWidth) / 3;
				layout = 2;
				Height = MeasureSpec.makeMeasureSpec(Height, MeasureSpec.UNSPECIFIED);
				if ((Height > maxHeight) && (maxHeight > 0) && !fromOnMeasure) {
					decreaseTextSize();
					setDimensions(false);
					return;
				}
				setMeasuredDimension(Width, Height);
				setLayoutParams(new LinearLayout.LayoutParams(Width, Height));
				centersX[0] = labelBoundsWidth[0] + padHorz;
				centersX[1] = centersX[0] + maxW + padHorz;
				centersX[2] = centersX[0];
				centersX[3] = centersX[1];
				// centersX[4] = centersX[0];
				centersY[0] = minH;
				centersY[1] = minH;
				centersY[2] = minH * 2 + padVert;
				centersY[3] = minH * 2 + padVert;
				// centersY[4] = minH * 3 + padVert;

			} else {
				float maxWlabel = 0;
				centersY[0] = minH;
				for (int i = 0; i < answers.length; i++) {
					if (labelBoundsWidth[i] > maxWlabel)
						maxWlabel = labelBoundsWidth[i];
					if (i > 0) {
						centersY[i] = centersY[i - 1] - answerBounds[i - 1].height() + layouts[i - 1].getHeight() + padVert + minH;
					}
				}
				for (int i = 0; i < answers.length; i++) {
					centersX[i] = maxWlabel;
				}
				Height = (int) (centersY[answers.length - 1] - textAnswerSizePix + padVert * (answers.length - 1))
						+ layouts[answers.length - 1].getHeight();
				layout = 4;
				Height = MeasureSpec.makeMeasureSpec(Height, MeasureSpec.UNSPECIFIED);
				if ((Height > maxHeight) && (maxHeight > 0) && !fromOnMeasure) {
					decreaseTextSize();
					setDimensions(false);
					return;
				}
				setMeasuredDimension(Width, Height);
				setLayoutParams(new LinearLayout.LayoutParams(Width, Height));
			}
			invalidate();
			break;
		}
	}

	private void setLayouts() {
		switch (type) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			maxW = 0;
			totalWidth = 0;
			for (int i = 0; i < answers.length; i++) {
				answerBoundsWidth[i] = TextAnswerPaintL.measureText(answers[i]);
				labelBoundsWidth[i] = TextLabelPaintR.measureText(labels[i]);
				TextAnswerPaintL.getTextBounds(answers[i], 0, answers[i].length(), answerBounds[i]);
				TextLabelPaintR.getTextBounds(labels[i], 0, labels[i].length(), labelBounds[i]);
				if (i == correctAnswer)
					layouts[i] = new StaticLayout(answers[i], correctAnswerPaint, Width - (int) Math.abs(labelBoundsWidth[i]),
							Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
				else if (i == wrongAnswer)
					layouts[i] = new StaticLayout(answers[i], wrongAnswerPaint, Width - (int) Math.abs(labelBoundsWidth[i]),
							Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
				else
					layouts[i] = new StaticLayout(answers[i], TextAnswerPaintL, Width - (int) Math.abs(labelBoundsWidth[i]),
							Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
				Widths[i] = Math.abs(answerBoundsWidth[i]) + Math.abs(labelBoundsWidth[i]);
				totalWidth += Widths[i];
				if (Widths[i] > maxW)
					maxW = Widths[i];
			}
			invalidate();
			break;
		}
	}

	private void decreaseTextSize() {
		textLabelSizeSP -= 5;
		textLabelSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textLabelSizeSP, getResources().getDisplayMetrics());
		TextLabelPaintR.setTextSize(textLabelSizePix);
		correctLabelPaint.setTextSize(textLabelSizePix);
		wrongLabelPaint.setTextSize(textLabelSizePix);

		textAnswerSizeSP -= 5;
		textAnswerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textAnswerSizeSP, getResources().getDisplayMetrics());
		TextAnswerPaintL.setTextSize(textAnswerSizePix);
		correctAnswerPaint.setTextSize(textAnswerSizePix);
		wrongAnswerPaint.setTextSize(textAnswerSizePix);
		TextAnswerPaintL.setTextSize(textAnswerSizePix);

	}

	private void resetTextSize() {
		textLabelSizeSP = textConstLabelSizeSP;
		textLabelSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textLabelSizeSP, getResources().getDisplayMetrics());
		TextLabelPaintR.setTextSize(textLabelSizePix);
		correctLabelPaint.setTextSize(textLabelSizePix);
		wrongLabelPaint.setTextSize(textLabelSizePix);

		textAnswerSizeSP = textConstAnswerSizeSP;
		textAnswerSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textAnswerSizeSP, getResources().getDisplayMetrics());
		TextAnswerPaintL.setTextSize(textAnswerSizePix);
		correctAnswerPaint.setTextSize(textAnswerSizePix);
		wrongAnswerPaint.setTextSize(textAnswerSizePix);
		TextAnswerPaintL.setTextSize(textAnswerSizePix);
	}
}
