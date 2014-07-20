package com.olyware.mathlock.views;

import android.content.Context;

public class PixelHelper {

	public static float pixelToSP(Context ctx, float pixel) {
		return pixel / ctx.getResources().getDisplayMetrics().scaledDensity;
	}
}
