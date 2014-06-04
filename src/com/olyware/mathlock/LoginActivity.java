package com.olyware.mathlock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.olyware.mathlock.utils.GCMHelper;

public class LoginActivity extends FragmentActivity implements LoginFragment.OnFinishedListener, GCMHelper.GCMResponse {

	// final static public String RECEIVE_USERID = "com.olyware.mathlock.RECEIVE_USERID";
	// final private Context ctx = this;
	private LoginFragment mainFragment;

	public void onFinish() {
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_login);

		// check if user is logged in
		SharedPreferences sharedPrefsUserInfo = getSharedPreferences(getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		if (sharedPrefsUserInfo.getBoolean(getString(R.string.pref_user_logged_in), false)) {
			startMainActivity();
		}

		if (savedInstanceState == null) {
			mainFragment = new LoginFragment();
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).commit();
		} else {
			// Or set the fragment from restored state info
			mainFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
		}

		GCMHelper.registerGCM(this, getApplicationContext());

		/*IntentFilter intentFilter = new IntentFilter(RECEIVE_USERID);
		LocalBroadcastManager.getInstance(this).registerReceiver(userIDReceiver, intentFilter);*/
	}

	@Override
	protected void onResume() {
		super.onResume();
		GCMHelper.checkPlayServices(this);
	}

	/*@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(userIDReceiver);
		super.onDestroy();
	}*/

	public void GCMResult(boolean result) {
		LoginFragment loginFrag = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.container);
		if (loginFrag != null) {
			loginFrag.GCMRegistrationDone(result);
		}
	}

	public void RegisterIDResult(int result) {
		// Do Nothing
	}

	/*public void confirmIDResult(int result) {
		LoginFragment loginFrag = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.container);
		if (loginFrag != null)
			loginFrag.GCMConfirmDone(result);
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
