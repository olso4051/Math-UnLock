package com.olyware.mathlock;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.olyware.mathlock.dialog.PreferenceListDialog;
import com.olyware.mathlock.ui.Typefaces;
import com.olyware.mathlock.utils.EZ;

public class AdvanceSettingActivity extends Activity implements OnClickListener {

	private SharedPreferences preferences;

	private RelativeLayout pnlUseAsLockScreen;
	private ArrayList<String> keys = new ArrayList<String>();
	private ArrayList<String> values = new ArrayList<String>();
	private RelativeLayout pnlLockScreenTimeout;
	private RelativeLayout pnlautoadjustdifficulty;
	private RelativeLayout pnlvibrate;
	private RelativeLayout pnlanalytics;
	private RelativeLayout pnlPush;

	private TextView lockscreenValue;
	private TextView timeoutValue;
	private TextView autoadjustValue;
	private TextView vibrateValue;
	private TextView analyticsValue;
	private TextView pushValue;

	private static String KEY_LOCKSCREEN = "lockscreen";
	private static String KEY_TIMEOUT = "lockscreen2";
	private static String KEY_AUTOADJUST = "algorithm";
	private static String KEY_VIBRATE = "vibration";
	private static String KEY_ANALYTICS = "analytics_tracking";
	private static String KEY_PUSH = "push_notifications";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advancesettings);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		initview();
	}

	@Override
	protected void onStart() {
		super.onStart();
		MyApplication.getGaTracker().send(new HitBuilders.AppViewBuilder().build());
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}

	@Override
	protected void onStop() {
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
		super.onStop();

	}

	private void initview() {

		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
		Typefaces typefaces = Typefaces.getInstance(this);
		EZ.setFont((ViewGroup) layout, typefaces.avenirnext);

		pnlUseAsLockScreen = (RelativeLayout) findViewById(R.id.pnlUseAsLockScreen);
		pnlLockScreenTimeout = (RelativeLayout) findViewById(R.id.pnlLockScreenTimeout);
		pnlautoadjustdifficulty = (RelativeLayout) findViewById(R.id.pnlautoadjustdifficulty);
		pnlvibrate = (RelativeLayout) findViewById(R.id.pnlvibrate);
		pnlanalytics = (RelativeLayout) findViewById(R.id.pnlanalytics);
		pnlPush = (RelativeLayout) findViewById(R.id.pnlPush);

		lockscreenValue = (TextView) findViewById(R.id.lockscreenValue);
		timeoutValue = (TextView) findViewById(R.id.timeoutValue);
		autoadjustValue = (TextView) findViewById(R.id.autoadjustValue);
		vibrateValue = (TextView) findViewById(R.id.vibrateValue);
		analyticsValue = (TextView) findViewById(R.id.analyticsValue);
		pushValue = (TextView) findViewById(R.id.pushValue);

		String[] strings = getResources().getStringArray(R.array.lockscreen2_entries);
		keys = new ArrayList<String>(Arrays.asList(strings));
		strings = getResources().getStringArray(R.array.lockscreen2_values);
		values = new ArrayList<String>(Arrays.asList(strings));

		loadDataFromPreference();

		pnlUseAsLockScreen.setOnClickListener(this);
		pnlLockScreenTimeout.setOnClickListener(this);
		pnlautoadjustdifficulty.setOnClickListener(this);
		pnlvibrate.setOnClickListener(this);
		pnlanalytics.setOnClickListener(this);
		pnlPush.setOnClickListener(this);
		findViewById(R.id.crossImage).setOnClickListener(this);

	}

	private void loadDataFromPreference() {
		lockscreenValue.setText(preferences.getBoolean(KEY_LOCKSCREEN, true) ? "Yes" : "No");
		pnlLockScreenTimeout.setEnabled(preferences.getBoolean(KEY_LOCKSCREEN, true));
		timeoutValue.setText(keys.get(Integer.parseInt(preferences.getString(KEY_TIMEOUT, "0"))));
		autoadjustValue.setText(preferences.getBoolean(KEY_AUTOADJUST, true) ? "Yes" : "No");
		vibrateValue.setText(preferences.getBoolean(KEY_VIBRATE, true) ? "Yes" : "No");
		analyticsValue.setText(preferences.getBoolean(KEY_ANALYTICS, true) ? "Yes" : "No");
		pushValue.setText(preferences.getBoolean(KEY_PUSH, true) ? "Yes" : "No");
	}

	@Override
	public void onClick(View arg0) {

		switch (arg0.getId()) {
		case R.id.pnlUseAsLockScreen:
			toggleandLoadPreference(KEY_LOCKSCREEN);
			break;
		case R.id.pnlLockScreenTimeout:
			final PreferenceListDialog listDialog = new PreferenceListDialog(AdvanceSettingActivity.this);

			listDialog.show(R.layout.dailog_preferencelist, null, keys,
					keys.get(Integer.parseInt(preferences.getString(KEY_TIMEOUT, "0"))), new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
							listDialog.dismiss();
							preferences.edit().putString(KEY_TIMEOUT, values.get(arg2)).commit();
							loadDataFromPreference();
						}
					});
			break;
		case R.id.pnlautoadjustdifficulty:
			toggleandLoadPreference(KEY_AUTOADJUST);
			break;
		case R.id.pnlvibrate:
			toggleandLoadPreference(KEY_VIBRATE);
			break;
		case R.id.pnlanalytics:
			toggleandLoadPreference(KEY_ANALYTICS);
			break;
		case R.id.pnlPush:
			toggleandLoadPreference(KEY_PUSH);
			break;

		case R.id.crossImage:
			finish();
			break;

		default:
			break;
		}

	}

	private void toggleandLoadPreference(String key) {
		preferences.edit().putBoolean(key, !preferences.getBoolean(key, false)).commit();
		loadDataFromPreference();
	}
}
