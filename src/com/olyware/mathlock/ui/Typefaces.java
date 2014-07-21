package com.olyware.mathlock.ui;

import android.content.Context;
import android.graphics.Typeface;

import com.olyware.mathlock.utils.Loggy;

public class Typefaces {
	protected static Typefaces instance;
	public Typeface robotoLight;
	public Typeface robotoMedium;
	public Typeface robotoCondensed_Regular;

	protected Typefaces(Context context) {
		try {
			robotoLight = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.otf");
			robotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");
			robotoCondensed_Regular = Typeface.createFromAsset(context.getAssets(), "RobotoCondensed-Regular.ttf");
		} catch (Exception e) {
			Loggy.d("Unable to load typefaces", e);
		}
	}

	public static Typefaces getInstance(Context context) {
		if (instance == null) {
			instance = new Typefaces(context);
		}
		return instance;
	}
}
