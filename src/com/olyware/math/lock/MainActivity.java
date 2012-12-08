package com.olyware.math.lock;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {
	private TextView clock;
	private RadioGroup radioGroup1;
	private RadioButton radioPhone;
	private RadioButton radioSilent;
	private RadioButton radioSettings;
	private TextView problem;
	private RadioGroup radioGroup2;
	private RadioButton radioAnswer1;
	private RadioButton radioAnswer2;
	private RadioButton radioAnswer3;
	private RadioButton radioAnswer4;
	private Button buttonUnlock;
	private Button buttonSure;
	private AudioManager am;

	private double pi = Math.PI;

	private SharedPreferences sharedPrefs;

	private Handler mHandler;

	static {
		IntentFilter s_intentFilter = new IntentFilter();
		s_intentFilter.addAction(Intent.ACTION_TIME_TICK);
		s_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		s_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
	}

	private final BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				Date curDateTime = new Date(System.currentTimeMillis());
				SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d"); // hh:mm
																					// a
																					// yyyy
																					// G);
				clock.setText(formatter.format(curDateTime));
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		IntentFilter s_intentFilter = new IntentFilter();
		s_intentFilter.addAction(Intent.ACTION_TIME_TICK);
		s_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		s_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);

		mHandler = new Handler();
		setContentView(R.layout.activity_main);

		clock = (TextView) findViewById(R.id.clock);
		problem = (TextView) findViewById(R.id.problem);
		radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
		radioGroup2 = (RadioGroup) findViewById(R.id.radioGroup2);
		radioPhone = (RadioButton) findViewById(R.id.radioPhone);
		radioSilent = (RadioButton) findViewById(R.id.radioSilent);
		radioSettings = (RadioButton) findViewById(R.id.radioSettings);
		radioAnswer1 = (RadioButton) findViewById(R.id.radioAnswer1);
		radioAnswer2 = (RadioButton) findViewById(R.id.radioAnswer2);
		radioAnswer3 = (RadioButton) findViewById(R.id.radioAnswer3);
		radioAnswer4 = (RadioButton) findViewById(R.id.radioAnswer4);
		buttonUnlock = (Button) findViewById(R.id.buttonUnlock);
		buttonUnlock.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buttonUnlockClick();
			}
		});
		buttonSure = (Button) findViewById(R.id.buttonSure);
		buttonSure.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buttonSureClick();
			}
		});
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		switch (am.getRingerMode()) {
		case AudioManager.RINGER_MODE_SILENT:
			radioSilent.setText(getString(R.string.sound));
			break;
		case AudioManager.RINGER_MODE_VIBRATE:
			radioSilent.setText(getString(R.string.sound));
			break;
		case AudioManager.RINGER_MODE_NORMAL:
			radioSilent.setText(getString(R.string.silent));
			break;
		}
		Date curDateTime = new Date(System.currentTimeMillis());
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d"); // hh:mm
																			// a
																			// yyyy
																			// G);
		clock.setText(formatter.format(curDateTime));

		registerReceiver(m_timeChangedReceiver, s_intentFilter);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onRadioButtonClicked2(View view) {
		if (!buttonUnlock.isEnabled()) {
			buttonUnlock.setEnabled(true);
			mHandler.removeCallbacksAndMessages(null);
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					buttonUnlock.setEnabled(false);
					buttonSure.setEnabled(false);
					radioGroup2.clearCheck();
				}
			}, 3000); // disable unlock button after 3s
		}
		buttonSure.setEnabled(false);
		radioGroup1.clearCheck();
	}

	public void onRadioButtonClicked1(View view) {
		if (!buttonSure.isEnabled() || buttonUnlock.isEnabled()) {
			buttonSure.setEnabled(true);
			mHandler.removeCallbacksAndMessages(null);
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					buttonSure.setEnabled(false);
					radioGroup1.clearCheck();
				}
			}, 3000); // disable sure button after 3s
		}
		buttonUnlock.setEnabled(false);
		radioGroup2.clearCheck();
	}

	private void buttonUnlockClick() {
		buttonSure.setEnabled(true);
	}

	private void buttonSureClick() {
		if (!buttonUnlock.isEnabled()) {
			switch (radioGroup1.getCheckedRadioButtonId()) {
			case R.id.radioPhone:
				Intent i = new Intent(Intent.ACTION_DIAL, null);
				startActivity(i);
				break;
			case R.id.radioSilent:
				if (radioSilent.getText().toString().equals(getString(R.string.silent))) {
					am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
					radioSilent.setText(getString(R.string.sound));
				} else {
					am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					radioSilent.setText(getString(R.string.silent));
				}
				break;
			case R.id.radioSettings:
				startActivity(new Intent(this, ShowSettingsActivity.class));
				break;
			default:
				break;
			}
			buttonSure.setEnabled(false);
			radioGroup1.clearCheck();
		} else {
			// TODO check if answer is correct
			buttonUnlock.setEnabled(false);
			buttonSure.setEnabled(false);
			radioGroup2.clearCheck();
		}
	}

}
