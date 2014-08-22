package com.olyware.mathlock.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.olyware.mathlock.R;

public class AutoResizeTextView extends TextView {

	private int Width, Height;
	private int textSizeSP;
	private float textSizePix, textSizePixDefault;

	private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);;
	private StaticLayout layout;
	private Rect bounds;
	private boolean measured = false;

	private Context ctx;

	// =========================================
	// Constructors
	// =========================================

	public AutoResizeTextView(Context context) {
		super(context);
		ctx = context;
		initView();
	}

	public AutoResizeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Custom, 0, 0);
		try {
			textSizePixDefault = a.getDimension(R.styleable.Custom_textSizeDefault, 0);
			textSizeSP = (int) PixelHelper.pixelToSP(context, textSizePixDefault);
		} finally {
			a.recycle();
		}
		initView();
	}

	public AutoResizeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ctx = context;
		initView();
	}

	// =========================================
	// Initialization
	// =========================================

	private void initView() {
		textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		setTextColor(Color.WHITE);
		textPaint.setTextAlign(Paint.Align.LEFT);
		resetTextSize();

		bounds = new Rect();
	}

	// =========================================
	// Public Functions
	// =========================================
	public int getTextAreaWidth() {
		return Width;
	}

	public int getTextAreaHeight() {
		return Height;
	}

	public int getTextSizeSP() {
		return textSizeSP;
	}

	// =========================================
	// Override Functions
	// =========================================

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
		if (measured) {
			resetTextSize();
			setDimensions();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Width = (right - left) - getCompoundPaddingLeft() - getCompoundPaddingRight();
		Height = (bottom - top) - getCompoundPaddingBottom() - getCompoundPaddingTop();
		measured = true;
		setDimensions();
		super.onLayout(changed, left, top, right, bottom);
	}

	// =========================================
	// Private Methods
	// =========================================

	private void setDimensions() {
		setLayouts();
		float maxH = Math.max(bounds.height(), textSizePix);
		maxH = Math.max(layout.getHeight(), maxH);
		if ((maxH > Height - textSizePix) && (Height > 0)) {
			if (decreaseTextSize()) {
				setDimensions();
				return;
			}
		}
		setTextSize(textSizeSP);
		invalidate();
	}

	private void setLayouts() {
		textPaint.getTextBounds(String.valueOf(getText()), 0, getText().length(), bounds);
		layout = new StaticLayout(getText(), textPaint, Width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
		invalidate();
	}

	private boolean decreaseTextSize() {
		if (textSizeSP == 1)
			return false;
		textSizeSP = Math.max(textSizeSP - 1, 1);
		textSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, getResources().getDisplayMetrics());
		textPaint.setTextSize(textSizePix);
		return true;
	}

	private void resetTextSize() {

		textSizePix = textSizePixDefault;
		float scaledDensity = ctx.getResources().getDisplayMetrics().scaledDensity;
		textSizeSP = (int) (textSizePix / scaledDensity);
		textPaint.setTextSize(textSizePix);
	}
}
