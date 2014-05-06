package com.olyware.mathlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;

import com.olyware.mathlock.service.ConfirmID;
import com.olyware.mathlock.utils.GCMHelper;

public class LoginActivity extends ActionBarActivity implements LoginFragment.OnFinishedListener, GCMHelper.GCMResponse,
		ConfirmID.ConfirmIdResponse {

	final static public String RECEIVE_USERID = "com.olyware.mathlock.RECEIVE_USERID";
	final private Context ctx = this;

	public void onFinish() {
		finish();
	}

	private BroadcastReceiver userIDReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(RECEIVE_USERID)) {
				String userID = intent.getStringExtra("user_id");
				if (userID != null) {
					new ConfirmID(ctx).execute(userID);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// check if user is logged in
		SharedPreferences sharedPrefsUserInfo = getSharedPreferences(getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		if (sharedPrefsUserInfo.getBoolean(getString(R.string.pref_user_logged_in), false)) {
			startMainActivity();
		}

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new LoginFragment()).commit();
		}

		GCMHelper.registerGCM(this, getApplicationContext());

		IntentFilter intentFilter = new IntentFilter(RECEIVE_USERID);
		LocalBroadcastManager.getInstance(this).registerReceiver(userIDReceiver, intentFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		GCMHelper.checkPlayServices(this);
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(userIDReceiver);
		super.onDestroy();
	}

	public void GCMResult(boolean result) {
		LoginFragment loginFrag = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.container);
		if (loginFrag != null) {
			loginFrag.GCMRegistrationDone(result);
		}
	}

	public void confirmIDResult(int result) {
		LoginFragment loginFrag = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.container);
		if (loginFrag != null)
			loginFrag.GCMConfirmDone(result);
	}

	/*public void registrationResult(int result, String userID) {
		Log.d("GAtest", "upload result = " + result);
		Log.d("GAtest", "userID = " + userID);

		SharedPreferences sharedPrefsUserInfo = getSharedPreferences(getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		SharedPreferences.Editor editPrefsUserInfo = sharedPrefsUserInfo.edit();
		if ((result == 0) && (userID != null)) {
			editPrefsUserInfo.putBoolean(getString(R.string.pref_user_reg_uploaded), true);
			editPrefsUserInfo.putString(getString(R.string.pref_user_userid), userID);
		} else if (result == 1) {
			editPrefsUserInfo.putBoolean(getString(R.string.pref_user_reg_uploaded), false);
		}
		editPrefsUserInfo.commit();

		LoginFragment loginFrag = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.container);
		if (loginFrag != null)
			loginFrag.GCMRegistrationDone(true);
	}*/

	private void startMainActivity() {
		Intent i = new Intent(this, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		finish();
	}
}
