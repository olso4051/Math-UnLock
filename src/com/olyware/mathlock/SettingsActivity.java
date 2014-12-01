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
import android.widget.Toast;

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

	private RelativeLayout pnlFromLanguage;
	private RelativeLayout pnlToLanguage;

	private TextView MinimumDifficultyValue;
	private TextView MaxDifficultyValue;
	private TextView MaxAttemptsyValue;
	private TextView txtlogin;
	private TextView tolanguage;
	private TextView fromLanguage;

	private ArrayList<String> keys;
	private ArrayList<String> values;

	private ArrayList<String> maxkeys;
	private ArrayList<String> maxvalues;

	private String KEY_MINDIFFICULTY = "difficulty_min";
	private String KEY_MAXDIFFICULTY = "difficulty_max";
	private String KEY_MAXATTEMPTS = "max_tries";
	private String KEY_FROMLANGUAGE = "from_language";
	private String KEY_TOLANGUAGE = "to_language";

	private boolean isLanguageEnabled = false;

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

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		isLanguageEnabled = sharedPrefs.getBoolean("enable_language", false);

		pnlLogin = (RelativeLayout) findViewById(R.id.pnlLogin);
		pnlRestartTutorial = (RelativeLayout) findViewById(R.id.pnlRestartTutorial);
		pnlInfo = (RelativeLayout) findViewById(R.id.pnlInfo);

		pnlFromLanguage = (RelativeLayout) findViewById(R.id.pnlFromlanguage);
		pnlToLanguage = (RelativeLayout) findViewById(R.id.pnlTolanguage);

		pnlMinimumDifficulty = (RelativeLayout) findViewById(R.id.pnlMinimumDifficulty);
		pnlMaxDifficulty = (RelativeLayout) findViewById(R.id.pnlMaxDifficulty);
		pnlMaxAttempts = (RelativeLayout) findViewById(R.id.pnlMaxAttempts);
		pnlMaxAttempts.setVisibility(View.INVISIBLE);

		MinimumDifficultyValue = (TextView) findViewById(R.id.MinimumDifficultyValue);
		MaxDifficultyValue = (TextView) findViewById(R.id.MaxDifficultyValue);
		MaxAttemptsyValue = (TextView) findViewById(R.id.MaxAttemptsyValue);
		txtlogin = (TextView) findViewById(R.id.txtlogin);

		tolanguage = (TextView) findViewById(R.id.tolanguage);
		fromLanguage = (TextView) findViewById(R.id.fromlanguage);

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

		pnlFromLanguage.setOnClickListener(this);
		pnlToLanguage.setOnClickListener(this);

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

		String selectedTo = preferences.getString(KEY_TOLANGUAGE, getString(R.string.language_to_default));
		selectedTo = String.valueOf(selectedTo.charAt(0)).toUpperCase() + selectedTo.substring(1, selectedTo.length());

		tolanguage.setText(selectedTo);

		selectedTo = preferences.getString(KEY_FROMLANGUAGE, getString(R.string.language_from_default));
		selectedTo = String.valueOf(selectedTo.charAt(0)).toUpperCase() + selectedTo.substring(1, selectedTo.length());

		fromLanguage.setText(selectedTo);

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

		case R.id.pnlFromlanguage:
			if (isLanguageEnabled) {
				final PreferenceListDialog listDialogFrom = new PreferenceListDialog(SettingsActivity.this);
				ArrayList<String> fromlanguage = new ArrayList<String>(Arrays.asList(getResources()
						.getStringArray(R.array.language_entries)));

				for (int i = 0; i < fromlanguage.size(); i++)
					if (preferences.getString(KEY_TOLANGUAGE, getString(R.string.language_to_default))
							.equalsIgnoreCase(fromlanguage.get(i))) {
						fromlanguage.remove(i);
					}

				String selectedFrom = preferences.getString(KEY_FROMLANGUAGE, getString(R.string.language_from_default));
				selectedFrom = String.valueOf(selectedFrom.charAt(0)).toUpperCase() + selectedFrom.substring(1, selectedFrom.length());
				listDialogFrom.show(R.layout.dailog_preferencelist, null, fromlanguage, selectedFrom, new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						listDialogFrom.dismiss();
						ArrayList<String> valu = new ArrayList<String>(Arrays.asList(getResources().getStringArray(
								R.array.language_values_not_localized)));
						valu.remove(preferences.getString(KEY_TOLANGUAGE, getString(R.string.language_to_default)));
						preferences.edit().putString(KEY_FROMLANGUAGE, valu.get(arg2)).commit();
						loadPreference();
					}
				});
			} else {
				Toast.makeText(SettingsActivity.this, "Please enable language pack to change the preference.", 1).show();
			}
			break;

		case R.id.pnlTolanguage:
			if (isLanguageEnabled) {
				final PreferenceListDialog listDialogTo = new PreferenceListDialog(SettingsActivity.this);
				ArrayList<String> ToLanguage = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.language_entries)));
				for (int i = 0; i < ToLanguage.size(); i++)
					if (preferences.getString(KEY_FROMLANGUAGE, getString(R.string.language_from_default)).equalsIgnoreCase(
							ToLanguage.get(i))) {
						ToLanguage.remove(i);
					}
				String selectedTo = preferences.getString(KEY_TOLANGUAGE, getString(R.string.language_to_default));
				selectedTo = String.valueOf(selectedTo.charAt(0)).toUpperCase() + selectedTo.substring(1, selectedTo.length());
				listDialogTo.show(R.layout.dailog_preferencelist, null, ToLanguage, selectedTo, new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						listDialogTo.dismiss();
						ArrayList<String> valu = new ArrayList<String>(Arrays.asList(getResources().getStringArray(
								R.array.language_values_not_localized)));
						valu.remove(preferences.getString(KEY_FROMLANGUAGE, getString(R.string.language_from_default)));
						preferences.edit().putString(KEY_TOLANGUAGE, valu.get(arg2)).commit();
						loadPreference();
					}
				});
			} else {
				Toast.makeText(SettingsActivity.this, "Please enable Language pack to change the preference.", 1).show();
			}
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
