package com.olyware.mathlock;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.olyware.mathlock.ui.Typefaces;
import com.olyware.mathlock.utils.Clock;
import com.olyware.mathlock.utils.EZ;

public class ShowCustomEditActivity extends Activity {

	private LinearLayout layout;
	private Typefaces fonts;
	private Clock clock2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom_edit);

		layout = (LinearLayout) findViewById(R.id.layout);
		fonts = Typefaces.getInstance(this);
		EZ.setFont((ViewGroup) layout, fonts.robotoLight);

		clock2 = new Clock(this, (TextView) findViewById(R.id.clock), (TextView) findViewById(R.id.money));
	}

	@Override
	public void onAttachedToWindow() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	@Override
	protected void onDestroy() {
		clock2.destroy();
		super.onDestroy();
	}

}
