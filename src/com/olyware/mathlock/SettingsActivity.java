package com.olyware.mathlock;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.olyware.mathlock.dialog.PreferenceListDialog;
import com.olyware.mathlock.service.ScreenService;
import com.olyware.mathlock.utils.PreferenceHelper;

public class SettingsActivity extends Activity implements OnClickListener {

	private SharedPreferences preferences;
	private RelativeLayout pnlLogin;
	private RelativeLayout pnlRestartTutorial;
	private RelativeLayout pnlInfo;

	private RelativeLayout pnlMinimumDifficulty;
	private RelativeLayout pnlMaxDifficulty;
	private RelativeLayout pnlMaxAttempts;

	private TextView MinimumDifficultyValue;
	private TextView MaxDifficultyValue;
	private TextView MaxAttemptsyValue;
	private TextView txtlogin;

	private ArrayList<String> keys;
	private ArrayList<String> values;

	private ArrayList<String> maxkeys;
	private ArrayList<String> maxvalues;

	private String KEY_MINDIFFICULTY = "difficulty_min";
	private String KEY_MAXDIFFICULTY = "difficulty_max";
	private String KEY_MAXATTEMPTS = "max_tries";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		preferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
		initView();

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

	private void initView() {

		pnlLogin = (RelativeLayout) findViewById(R.id.pnlLogin);
		pnlRestartTutorial = (RelativeLayout) findViewById(R.id.pnlRestartTutorial);
		pnlInfo = (RelativeLayout) findViewById(R.id.pnlInfo);

		pnlMinimumDifficulty = (RelativeLayout) findViewById(R.id.pnlMinimumDifficulty);
		pnlMaxDifficulty = (RelativeLayout) findViewById(R.id.pnlMaxDifficulty);
		pnlMaxAttempts = (RelativeLayout) findViewById(R.id.pnlMaxAttempts);
		pnlMaxAttempts.setVisibility(View.INVISIBLE);

		MinimumDifficultyValue = (TextView) findViewById(R.id.MinimumDifficultyValue);
		MaxDifficultyValue = (TextView) findViewById(R.id.MaxDifficultyValue);
		MaxAttemptsyValue = (TextView) findViewById(R.id.MaxAttemptsyValue);
		txtlogin = (TextView) findViewById(R.id.txtlogin);
		if (!getSharedPreferences(getString(R.string.pref_user_info), Context.MODE_PRIVATE).getBoolean(
				getString(R.string.pref_user_skipped), false)) {
			txtlogin.setText("Logout");
		} else {
			txtlogin.setText("Login");
		}

		String[] strings = getResources().getStringArray(R.array.difficulty_entries);
		keys = new ArrayList<String>(Arrays.asList(strings));
		strings = getResources().getStringArray(R.array.difficulty_values);
		values = new ArrayList<String>(Arrays.asList(strings));

		strings = getResources().getStringArray(R.array.max_tries_entries);
		maxkeys = new ArrayList<String>(Arrays.asList(strings));
		strings = getResources().getStringArray(R.array.max_tries_values);
		maxvalues = new ArrayList<String>(Arrays.asList(strings));

		loadPreference();

		pnlLogin.setOnClickListener(this);
		pnlRestartTutorial.setOnClickListener(this);
		pnlInfo.setOnClickListener(this);

		pnlMinimumDifficulty.setOnClickListener(this);
		pnlMaxDifficulty.setOnClickListener(this);
		pnlMaxAttempts.setOnClickListener(this);

		findViewById(R.id.AdvanceSettings).setOnClickListener(this);
		findViewById(R.id.crossImage).setOnClickListener(this);

	}

	private void loadPreference() {

		MinimumDifficultyValue.setText(keys.get(Integer.parseInt(preferences.getString(KEY_MINDIFFICULTY, "0"))));
		MaxDifficultyValue.setText(keys.get(Integer.parseInt(preferences.getString(KEY_MAXDIFFICULTY, "1"))));
		MaxAttemptsyValue.setText(maxkeys.get(Integer.parseInt(preferences.getString(KEY_MAXATTEMPTS, "1")) - 1));

	}

	@Override
	public void onClick(View view) {
		Intent mainIntent;
		switch (view.getId()) {
		case R.id.pnlLogin:
			final Intent sIntent = new Intent(this, ScreenService.class);
			PreferenceHelper.logout(this);
			stopService(sIntent);
			Intent broadcastIntent = new Intent(getString(R.string.logout_receiver_filter));
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
			Intent loginIntent = new Intent(this, MainActivity.class);
			loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			loginIntent.putExtra("facebook_logout", true);
			startActivity(loginIntent);
			finish();
			break;
		case R.id.pnlRestartTutorial:
			PreferenceHelper.resetTutorial(this);
			mainIntent = new Intent(this, MainActivity.class);
			mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PreferenceHelper.setShareCompeteShown(getApplicationContext(), false);
			startActivity(mainIntent);
			finish();
			break;
		case R.id.pnlInfo:
			mainIntent = new Intent(this, MainActivity.class);
			mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mainIntent.putExtra("info", true);
			startActivity(mainIntent);
			finish();

			break;
		case R.id.pnlMinimumDifficulty:

			final PreferenceListDialog listDialog = new PreferenceListDialog(SettingsActivity.this);

			listDialog.show(R.layout.dailog_preferencelist, null, keys,
					keys.get(Integer.parseInt(preferences.getString(KEY_MINDIFFICULTY, "0"))), new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
							listDialog.dismiss();
							preferences.edit().putString(KEY_MINDIFFICULTY, values.get(arg2)).commit();
							loadPreference();
						}
					});
			break;
		case R.id.pnlMaxDifficulty:
			final PreferenceListDialog listDialog1 = new PreferenceListDialog(SettingsActivity.this);

			listDialog1.show(R.layout.dailog_preferencelist, null, keys,
					keys.get(Integer.parseInt(preferences.getString(KEY_MAXDIFFICULTY, "1"))), new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
							listDialog1.dismiss();
							preferences.edit().putString(KEY_MAXDIFFICULTY, values.get(arg2)).commit();
							loadPreference();
						}
					});
			break;
		case R.id.pnlMaxAttempts:
			final PreferenceListDialog listDialog3 = new PreferenceListDialog(SettingsActivity.this);

			listDialog3.show(R.layout.dailog_preferencelist, null, maxkeys,
					maxkeys.get(Integer.parseInt(preferences.getString(KEY_MAXATTEMPTS, "1")) - 1), new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
							listDialog3.dismiss();
							preferences.edit().putString(KEY_MAXATTEMPTS, maxvalues.get(arg2)).commit();
							loadPreference();
						}
					});
			break;

		case R.id.AdvanceSettings:
			startActivity(new Intent(this, AdvanceSettingActivity.class));
			break;

		case R.id.crossImage:
			finish();
			break;

		default:
			break;
		}

	}

}
