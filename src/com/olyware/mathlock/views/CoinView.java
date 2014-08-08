package com.olyware.mathlock.views;

import java.util.ArrayList;
import java.util.List;

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

	private int Width, Height, startingWidth = -1, startingHeight = -1, endingWidth = -1, endingHeight = -1, currentWidth, currentHeight,
			coins, drawCount = 0;
	private List<Drawable> coinDrawables;
	private List<Integer> currentCenterX, currentCenterY;
	private long lastTimePercent;
	private List<Long> startTimePercent;
	private float percent = 0;
	private TextPaint tempPaint;
	private Runnable startAnimate;
	private Handler animateHandler;
	private AnimationDoneListener listener;

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
		this.startingWidth = startingWidth;
		this.startingHeight = startingHeight;
	}

	public void setEndingCenter(int endingWidth, int endingHeight) {
		this.endingWidth = endingWidth;
		this.endingHeight = endingHeight;
	}

	public void setCoinAmount(int coinAmount) {
		if (coinAmount <= 0)
			coins = 0;
		else if (coinAmount < 100)
			coins = (int) Math.ceil((coinAmount + 1) / 10d) + 0;
		else if (coinAmount < 1000)
			coins = (int) Math.ceil((coinAmount + 1) / 100d) + 9;
		else if (coinAmount < 10000)
			coins = (int) Math.ceil((coinAmount + 1) / 1000d) + 18;
		else
			coins = 29;
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
		if (startingWidth < 0)
			startingWidth = Width / 2;
		if (startingHeight < 0)
			startingHeight = Height / 2;
		if (endingWidth < 0)
			endingWidth = Width / 2;
		if (endingHeight < 0)
			endingHeight = Height / 2;

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
			int coin = i % 3;
			canvas.save();
			canvas.translate(currentCenterX.get(i), currentCenterY.get(i));
			// d.get(i).setAlpha((wrongGuess < 0&&!quickUnlock) ? answerAlpha : 255);
			coinDrawables.get(coin).draw(canvas);
			canvas.restore();
			// canvas.drawText("coins", currentCenterX.get(i), currentCenterY.get(i), tempPaint);
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
				for (int i = 0; i < startTimePercent.size(); i++) {
					percent = (lastTimePercent - startTimePercent.get(i)) / (float) (MoneyHelper.updateMoneyTime / 3);
					if (i >= currentCenterX.size()) {
						currentCenterX.add((int) (startingWidth + (endingWidth - startingWidth) * percent));
						currentCenterY.add((int) (startingHeight + (endingHeight - startingHeight) * percent));
					} else {
						currentCenterX.set(i, (int) (startingWidth + (endingWidth - startingWidth) * percent));
						currentCenterY.set(i, (int) (startingHeight + (endingHeight - startingHeight) * percent));
					}
				}
				if (percent >= 0 && percent <= 1) {
					invalidate();
					if (startTimePercent.size() < coins)
						startTimePercent.add(lastTimePercent);
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
}