package com.olyware.mathlock;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.olyware.mathlock.utils.GCMHelper;

public class LoginActivity extends ActionBarActivity implements LoginFragment.OnFinishedListener, RegisterID.RegisterIdResponse {

	public void onFinish() {
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new LoginFragment()).commit();
		}
		GCMHelper.registerAndStoreGCM(this, getApplicationContext());
	}

	@Override
	protected void onResume() {
		super.onResume();
		GCMHelper.checkPlayServices(this);
	}

	public void registrationResult(int result, String userID) {
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
			loginFrag.GCMRegistrationDone();
	}
}
