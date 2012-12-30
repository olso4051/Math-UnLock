package com.olyware.mathlock;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class ShowProgressActivity extends Activity {
	// private Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// mHandler = new Handler();
		setContentView(R.layout.activity_progress);
	}

	@Override
	public void onAttachedToWindow() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}
}
