package com.olyware.mathlock.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.MoneyHelper;

public class CoinView extends View {

	final private static int UpdateMoneyTimeFraction = 2;
	private int Width, Height, startingCenterX = -1, startingCenterY = -1, endingCenterX = -1, endingCenterY = -1, straightLength = -1,
			coins, drawCount = 0;
	private List<Drawable> coinDrawables;
	private List<Integer> currentCenterX, currentCenterY, arcRadius, arcCenterX, arcCenterY, plusOrMinus;
	private long lastTimePercent;
	private List<Long> startTimePercent;
	private float percent = 0;
	private TextPaint tempPaint;
	private Runnable startAnimate;
	private Handler animateHandler;
	private AnimationDoneListener listener;
	private Random rand;

	// =========================================
	// Constructors
	// =========================================

	public interface AnimationDoneListener {

		public void OnDone();

	}

	public CoinView(Context context) {
		super(context);
		initView(context);
	}

	public CoinView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Custom, 0, 0);

		try {
			// answerSizePix = a.getDimension(R.styleable.Custom_textSizeDefault, 0);
			// answerSizeSPDefault = (int) PixelHelper.pixelToSP(context, answerSizePix);
		} finally {
			a.recycle();
		}
		initView(context);
	}

	public CoinView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	// =========================================
	// Initialization
	// =========================================

	private void initView(Context ctx) {
		tempPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		tempPaint.setColor(ctx.getResources().getColor(R.color.grey_on_dark));
		tempPaint.setTextAlign(Paint.Align.CENTER);
		tempPaint.setTextSize(100);

		rand = new Random();
		coinDrawables = new ArrayList<Drawable>();
		Drawable drawCoin1 = ctx.getResources().getDrawable(R.drawable.coin1);
		drawCoin1.setBounds(-drawCoin1.getIntrinsicWidth() / 2, -drawCoin1.getIntrinsicHeight() / 2, drawCoin1.getIntrinsicWidth() / 2,
				drawCoin1.getIntrinsicHeight() / 2);
		Drawable drawCoin2 = ctx.getResources().getDrawable(R.drawable.coin2);
		drawCoin2.setBounds(-drawCoin2.getIntrinsicWidth() / 2, -drawCoin2.getIntrinsicHeight() / 2, drawCoin2.getIntrinsicWidth() / 2,
				drawCoin2.getIntrinsicHeight() / 2);
		Drawable drawCoin3 = ctx.getResources().getDrawable(R.drawable.coin3);
		drawCoin3.setBounds(-drawCoin3.getIntrinsicWidth() / 2, -drawCoin3.getIntrinsicHeight() / 2, drawCoin3.getIntrinsicWidth() / 2,
				drawCoin3.getIntrinsicHeight() / 2);
		coinDrawables.add(drawCoin1);
		coinDrawables.add(drawCoin2);
		coinDrawables.add(drawCoin3);

		coins = 0;
		arcRadius = new ArrayList<Integer>();
		arcCenterX = new ArrayList<Integer>();
		arcCenterY = new ArrayList<Integer>();
		plusOrMinus = new ArrayList<Integer>();
		currentCenterX = new ArrayList<Integer>();
		currentCenterY = new ArrayList<Integer>();
		startTimePercent = new ArrayList<Long>();
		animateHandler = new Handler();
		listener = new AnimationDoneListener() {
			@Override
			public void OnDone() {
			}
		};
	}

	// =========================================
	// Public Methods
	// =========================================

	public void setStartingCenter(int startingWidth, int startingHeight) {
		this.startingCenterX = startingWidth;
		this.startingCenterY = startingHeight;
		setStraightLength();
	}

	public void setEndingCenter(int endingWidth, int endingHeight) {
		this.endingCenterX = endingWidth;
		this.endingCenterY = endingHeight;
		setStraightLength();
	}

	public void setCoinAmount(int coinAmount) {
		if (coinAmount <= 0)
			coins = 0;
		else if (coinAmount < 10)
			coins = (int) Math.floor((coinAmount + 1) / 1d) - 1;
		else if (coinAmount < 100)
			coins = (int) Math.floor((coinAmount + 10) / 10d) + 8;
		else if (coinAmount < 1000)
			coins = (int) Math.floor((coinAmount + 100) / 100d) + 17;
		else if (coinAmount < 10000)
			coins = (int) Math.floor((coinAmount + 1000) / 1000d) + 26;
		else
			coins = 29;
		// following equations are the same as above
		// int x = (int) Math.floor(Math.log10(coinAmount));
		// int y = (int) Math.pow(10, x);
		// int z = 9 * x - 1;
		// coins = (int) Math.floor((coinAmount + y) / y) + z;
		Loggy.d("coinAmount = " + coinAmount + " |coins = " + coins);
	}

	public void setOnAnimationDoneListener(AnimationDoneListener listener) {
		this.listener = listener;
	}

	// =========================================
	// Drawing Functionality
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Width = measure(widthMeasureSpec);
		Height = measure(heightMeasureSpec);
		boolean setLength = false;
		if (startingCenterX < 0) {
			startingCenterX = Width / 2;
			setLength = true;
		}
		if (startingCenterY < 0) {
			startingCenterY = Height / 2;
			setLength = true;
		}
		if (endingCenterX < 0) {
			endingCenterX = Width / 2;
			setLength = true;
		}
		if (endingCenterY < 0) {
			endingCenterY = Height / 2;
			setLength = true;
		}
		if (setLength) {
			setStraightLength();
		}
		setMeasuredDimension(Width, Height);
		initRunnables();
		startAnimation();
	}

	private int measure(int measureSpec) {
		int result = 0;
		// Decode the measurement specifications.
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.UNSPECIFIED) {
			// Return a default size of 480 if no bounds are specified.
			result = 480;
		} else if (specMode == MeasureSpec.AT_MOST) {
			result = specSize;
		} else {
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawCount = (drawCount + 1) % (3 * 2);
		for (int i = 0; i < currentCenterX.size(); i++) {
			if (currentCenterY.get(i) > endingCenterY) {
				int coin = (i + (int) Math.floor(drawCount / 2)) % 3;
				canvas.save();
				canvas.translate(currentCenterX.get(i), currentCenterY.get(i));
				coinDrawables.get(coin).draw(canvas);
				canvas.restore();
			}
		}
	}

	// =========================================
	// Setup and Initialization Algorithms
	// =========================================

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	private void initRunnables() {
		startAnimate = new Runnable() {
			@Override
			public void run() {
				lastTimePercent = System.currentTimeMillis();
				long totalTime = 0;
				if (startTimePercent.size() > 0)
					totalTime = lastTimePercent - startTimePercent.get(0);
				for (int i = 0; i < startTimePercent.size(); i++) {
					percent = (lastTimePercent - startTimePercent.get(i)) / (float) (MoneyHelper.updateMoneyTime / UpdateMoneyTimeFraction);
					if (i >= currentCenterX.size()) {
						int h = 0;
						int pOrM = 1;
						if (rand != null) {
							h = rand.nextInt(straightLength / 10);
							pOrM = rand.nextInt(2) * 2 - 1;
						}
						if (h > 0) {
							int hx = (startingCenterX + endingCenterX) / 2;
							int hy = (startingCenterY + endingCenterY) / 2;
							int rad = (straightLength * straightLength + 4 * h * h) / (8 * h);
							int aCenterX = (int) (hx + pOrM * Math.sqrt(rad * rad - Math.pow(straightLength / 2, 2))
									* (startingCenterY - endingCenterY) / straightLength);
							int aCenterY = (int) (hy + pOrM * Math.sqrt(rad * rad - Math.pow(straightLength / 2, 2))
									* (endingCenterX - startingCenterX) / straightLength);
							int centerY = (int) (startingCenterY + (endingCenterY - startingCenterY) * percent);
							int centerX = (int) (aCenterX + pOrM
									* Math.sqrt(-aCenterY * aCenterY + 2 * aCenterY * centerY + rad * rad - centerY * centerY));
							arcRadius.add(rad);
							arcCenterX.add(aCenterX);
							arcCenterY.add(aCenterY);
							plusOrMinus.add(pOrM);
							currentCenterY.add(centerY);
							currentCenterX.add(centerX);
						} else {
							arcRadius.add(0);
							arcCenterX.add(0);
							arcCenterY.add(0);
							plusOrMinus.add(1);
							int centerX = (int) (startingCenterX + (endingCenterX - startingCenterX) * percent);
							int centerY = (int) (startingCenterY + (endingCenterY - startingCenterY) * percent);
							currentCenterX.add(centerX);
							currentCenterY.add(centerY);
						}
					} else {
						if (arcRadius.get(i) > 0) {
							int rad = arcRadius.get(i);
							int aCenterX = arcCenterX.get(i);
							int aCenterY = arcCenterY.get(i);
							int pOrM = plusOrMinus.get(i);
							int centerY = (int) (startingCenterY + (endingCenterY - startingCenterY) * percent);
							int centerX = (int) (aCenterX - pOrM
									* Math.sqrt(-aCenterY * aCenterY + 2 * aCenterY * centerY + rad * rad - centerY * centerY));
							currentCenterY.set(i, centerY);
							currentCenterX.set(i, centerX);
						} else {
							int centerX = (int) (startingCenterX + (endingCenterX - startingCenterX) * percent);
							int centerY = (int) (startingCenterY + (endingCenterY - startingCenterY) * percent);
							currentCenterX.add(centerX);
							currentCenterY.add(centerY);
						}
					}
				}
				// if (percent >= 0 && percent <= 1) {
				if (totalTime > 0 && totalTime < MoneyHelper.updateMoneyTime) {
					invalidate();
					int coinsSent = startTimePercent.size();
					if (coinsSent < coins && coins > 1) {
						long maxDelay = MoneyHelper.updateMoneyTime - MoneyHelper.updateMoneyTime / UpdateMoneyTimeFraction;
						long delayStep = maxDelay / (coins - 1);
						long nextDelay = delayStep * coinsSent;
						if (totalTime >= nextDelay) {
							startTimePercent.add(startTimePercent.get(0) + nextDelay);
						}
					}
					animateHandler.post(startAnimate);
				} else if (listener != null) {
					listener.OnDone();
				}
			}
		};
	}

	// =========================================
	// Public Methods
	// =========================================
	private void startAnimation() {
		startTimePercent.clear();
		startTimePercent.add(System.currentTimeMillis());
		animateHandler.post(startAnimate);
	}

	private void setStraightLength() {
		if (startingCenterX >= 0 && startingCenterY >= 0 && endingCenterX >= 0 && endingCenterY >= 0)
			straightLength = (int) Math.sqrt(Math.pow(startingCenterX - endingCenterX, 2) + Math.pow(startingCenterY - endingCenterY, 2));
	}
}