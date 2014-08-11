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
	final private static int ayDefaultDP = 10000;
	final private static int vxDefaultDP = 500;
	final private static int vyDefaultDP = 700;
	private int Width, Height, startingCenterX = -1, startingCenterY = -1, endingCenterX = -1, endingCenterY = -1, straightLength = -1,
			coins, coinsMissed, drawCount = 0, ayDefault, vxDefault, vyDefault;
	private List<Drawable> coinDrawables;
	private List<Integer> currentCenterX, currentCenterY, plusOrMinus, velocityY, velocityX;
	private List<Long> arcRadius, arcCenterX, arcCenterY;
	private List<Boolean> gotOrMissed;
	private long lastTimePercent;
	private List<Long> startTimePercent;
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

		ayDefault = (int) PixelHelper.dpToPixel(ctx, ayDefaultDP);
		vxDefault = (int) PixelHelper.dpToPixel(ctx, vxDefaultDP);
		vyDefault = (int) PixelHelper.dpToPixel(ctx, vyDefaultDP);

		coins = 0;
		coinsMissed = 0;
		arcRadius = new ArrayList<Long>();
		arcCenterX = new ArrayList<Long>();
		arcCenterY = new ArrayList<Long>();
		plusOrMinus = new ArrayList<Integer>();
		currentCenterX = new ArrayList<Integer>();
		currentCenterY = new ArrayList<Integer>();
		velocityX = new ArrayList<Integer>();
		velocityY = new ArrayList<Integer>();
		startTimePercent = new ArrayList<Long>();
		gotOrMissed = new ArrayList<Boolean>();
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

	public void setCoinAmount(int coinAmountGot, int coinAmountMissed) {
		if (coinAmountGot <= 0)
			coins = 0;
		else if (coinAmountGot < 10)
			coins = (int) Math.floor((coinAmountGot + 1) / 1d) - 1;
		else if (coinAmountGot < 100)
			coins = (int) Math.floor((coinAmountGot + 10) / 10d) + 8;
		else if (coinAmountGot < 1000)
			coins = (int) Math.floor((coinAmountGot + 100) / 100d) + 17;
		else if (coinAmountGot < 10000)
			coins = (int) Math.floor((coinAmountGot + 1000) / 1000d) + 26;
		else
			coins = 29;

		if (coinAmountMissed <= 0)
			coinsMissed = 0;
		else if (coinAmountMissed < 10)
			coinsMissed = (int) Math.floor((coinAmountMissed + 1) / 1d) - 1;
		else if (coinAmountMissed < 100)
			coinsMissed = (int) Math.floor((coinAmountMissed + 10) / 10d) + 8;
		else if (coinAmountMissed < 1000)
			coinsMissed = (int) Math.floor((coinAmountMissed + 100) / 100d) + 17;
		else if (coinAmountMissed < 10000)
			coinsMissed = (int) Math.floor((coinAmountMissed + 1000) / 1000d) + 26;
		else
			coinsMissed = 29;
		// following equations are the same as above
		// int x = (int) Math.floor(Math.log10(coinAmount));
		// int y = (int) Math.pow(10, x);
		// int z = 9 * x - 1;
		// coins = (int) Math.floor((coinAmount + y) / y) + z;
		Loggy.d("coinAmount = " + coinAmountGot + " |coins = " + coins + "coinAmountMissed = " + coinAmountMissed + " |coinsMissed = "
				+ coinsMissed);
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
			int cX = currentCenterX.get(i);
			int cY = currentCenterY.get(i);
			if (cY > endingCenterY && cY < Height && cX > 0 && cX < Width) {
				int coin = (i + (int) Math.floor(drawCount / 2)) % 3;
				canvas.save();
				canvas.translate(cX, cY);
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
					float partialTime = (lastTimePercent - startTimePercent.get(i)) / 1000f;
					float percent = ((int) (lastTimePercent - startTimePercent.get(i)))
							/ (float) (MoneyHelper.updateMoneyTime / UpdateMoneyTimeFraction);

					if (i >= currentCenterX.size()) {
						int got = 0, missed = 0;
						for (boolean val : gotOrMissed) {
							if (val)
								got++;
							else
								missed++;
						}
						boolean coinsOrMissed = true;
						if (got < coins && missed < coinsMissed) {
							if (rand != null)
								coinsOrMissed = rand.nextBoolean();
						} else if (got < coins) {
							coinsOrMissed = true;
						} else if (missed < coinsMissed) {
							coinsOrMissed = false;
						} else {
							coinsOrMissed = true;
						}
						gotOrMissed.add(coinsOrMissed);
						if (coinsOrMissed) {
							int h = 0;
							int pOrM = 1;
							if (rand != null) {
								h = rand.nextInt(straightLength / 10) + 10;
								pOrM = rand.nextInt(2) * 2 - 1;
							}
							if (h > 0) {
								int hx = (startingCenterX + endingCenterX) / 2;
								int hy = (startingCenterY + endingCenterY) / 2;
								long rad = (straightLength * straightLength + 4 * h * h) / (8 * h);
								long aCenterX = (long) (hx + pOrM * Math.sqrt(rad * rad - Math.pow(straightLength / 2, 2))
										* (startingCenterY - endingCenterY) / straightLength);
								long aCenterY = (long) (hy + pOrM * Math.sqrt(rad * rad - Math.pow(straightLength / 2, 2))
										* (endingCenterX - startingCenterX) / straightLength);
								int centerY = (int) (startingCenterY + (endingCenterY - startingCenterY) * percent);
								int centerX = 0;
								if (aCenterX == 0 || aCenterY == 0) {
									// if rad*rad overflowed this will be true
									arcRadius.add(0l);
									arcCenterX.add(0l);
									arcCenterY.add(0l);
									plusOrMinus.add(1);
									centerX = (int) (startingCenterX + (endingCenterX - startingCenterX) * percent);

								} else {
									arcRadius.add(rad);
									arcCenterX.add(aCenterX);
									arcCenterY.add(aCenterY);
									plusOrMinus.add(pOrM);
									centerX = (int) (aCenterX + pOrM
											* Math.sqrt(-aCenterY * aCenterY + 2 * aCenterY * centerY + rad * rad - centerY * centerY));
								}
								velocityX.add(0);
								velocityY.add(0);
								currentCenterY.add(centerY);
								currentCenterX.add(centerX);
							} else {
								velocityX.add(0);
								velocityY.add(0);
								arcRadius.add(0l);
								arcCenterX.add(0l);
								arcCenterY.add(0l);
								plusOrMinus.add(1);
								currentCenterX.add((int) (startingCenterX + (endingCenterX - startingCenterX) * percent));
								currentCenterY.add((int) (startingCenterY + (endingCenterY - startingCenterY) * percent));
							}
						} else {
							int vx = vxDefault;
							int vy = -vyDefault;
							if (rand != null) {
								vx = rand.nextInt(vxDefault * 2 + 1) - vxDefault;
								vy = rand.nextInt(vyDefault + 1) - vyDefault * 3;
							}
							Loggy.d("vx = " + vx + " |vy = " + vy);
							velocityX.add(vx);
							velocityY.add(vy);
							arcRadius.add(0l);
							arcCenterX.add(0l);
							arcCenterY.add(0l);
							plusOrMinus.add(1);
							currentCenterX.add((int) (startingCenterX + vx * partialTime));
							currentCenterY.add((int) (startingCenterY + vy * partialTime + ayDefault * partialTime * partialTime / 2));
						}
					} else {
						if (!gotOrMissed.get(i)) {
							int vx = velocityX.get(i);
							int vy = velocityY.get(i);
							currentCenterX.set(i, (int) (startingCenterX + vx * partialTime));
							currentCenterY.set(i, (int) (startingCenterY + vy * partialTime + ayDefault * partialTime * partialTime / 2));
						} else if (arcRadius.get(i) > 0) {
							long rad = arcRadius.get(i);
							long aCenterX = arcCenterX.get(i);
							long aCenterY = arcCenterY.get(i);
							int pOrM = plusOrMinus.get(i);
							int centerY = (int) (startingCenterY + (endingCenterY - startingCenterY) * percent);
							int centerX = -100;
							if (centerY > 0) {
								centerX = (int) (aCenterX - pOrM
										* Math.sqrt(-aCenterY * aCenterY + 2 * aCenterY * centerY + rad * rad - centerY * centerY));
								if (centerX == 0) {
									centerX = (int) (startingCenterX + (endingCenterX - startingCenterX) * percent);
								}
							}
							currentCenterY.set(i, centerY);
							currentCenterX.set(i, centerX);
						} else {
							currentCenterX.set(i, (int) (startingCenterX + (endingCenterX - startingCenterX) * percent));
							currentCenterY.set(i, (int) (startingCenterY + (endingCenterY - startingCenterY) * percent));
						}
					}
				}
				// if (percent >= 0 && percent <= 1) {
				if (totalTime > 0 && totalTime < MoneyHelper.updateMoneyTime) {
					invalidate();
					int coinsSent = startTimePercent.size();
					if (coinsSent < (coins + coinsMissed) && (coins + coinsMissed) > 1) {
						long maxDelay = MoneyHelper.updateMoneyTime - MoneyHelper.updateMoneyTime / UpdateMoneyTimeFraction;
						long delayStep = maxDelay / (coins + coinsMissed - 1);
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
		arcRadius.clear();
		arcCenterX.clear();
		arcCenterY.clear();
		plusOrMinus.clear();
		currentCenterY.clear();
		currentCenterX.clear();
		velocityX.clear();
		velocityY.clear();
		gotOrMissed.clear();
		startTimePercent.add(System.currentTimeMillis());
		animateHandler.post(startAnimate);
	}

	private void setStraightLength() {
		if (startingCenterX >= 0 && startingCenterY >= 0 && endingCenterX >= 0 && endingCenterY >= 0)
			straightLength = (int) Math.sqrt(Math.pow(startingCenterX - endingCenterX, 2) + Math.pow(startingCenterY - endingCenterY, 2));
	}
}