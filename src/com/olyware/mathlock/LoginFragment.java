package com.olyware.mathlock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.olyware.mathlock.service.RegisterID;
import com.olyware.mathlock.utils.GCMHelper;

public class LoginFragment extends Fragment implements View.OnClickListener, RegisterID.RegisterIdResponse {

	OnFinishedListener mCallback;

	// Container Activity must implement this interface
	public interface OnFinishedListener {
		public void onFinish();
	}

	private String mPrefUserInfo, mPrefUserUsername, mPrefUserUserID, mPrefUserReferrer, mPrefUserLoggedIn, mPrefUserSkipped;
	private float transY = 0;
	private EditText username;
	private Button facebook, login, skip;
	private LinearLayout inputs, progress;

	/**
	 * Saves user info and switches to the main activity
	 */
	void logIn() {
		facebook.setEnabled(false);
		login.setEnabled(false);
		skip.setEnabled(false);

		SharedPreferences sharedPrefsUserInfo = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
		String regID = GCMHelper.getRegistrationId(getActivity().getApplicationContext());
		String userID = sharedPrefsUserInfo.getString(mPrefUserUserID, "");
		String referrer = sharedPrefsUserInfo.getString(mPrefUserReferrer, "");
		String uName = username.getText().toString();
		sharedPrefsUserInfo.edit().putString(mPrefUserUsername, uName).commit();

		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		View focus = getActivity().getCurrentFocus();
		if (imm != null && focus != null) {
			imm.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		attemptLogin(uName, regID, userID, referrer);

		startAnimationProgress();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnFinishedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFinishedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context ctx = getActivity();

		mPrefUserInfo = ctx.getString(R.string.pref_user_info);
		mPrefUserUsername = ctx.getString(R.string.pref_user_username);
		mPrefUserUserID = ctx.getString(R.string.pref_user_userid);
		mPrefUserReferrer = ctx.getString(R.string.pref_user_referrer);
		mPrefUserLoggedIn = ctx.getString(R.string.pref_user_logged_in);
		mPrefUserSkipped = ctx.getString(R.string.pref_user_skipped);

		// check if user is logged in
		SharedPreferences sharedPrefsUserInfo = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
		if (sharedPrefsUserInfo.getBoolean(mPrefUserLoggedIn, false)) {
			startMainActivity();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_login, container, false);

		inputs = (LinearLayout) view.findViewById(R.id.fragment_login_inputs);
		progress = (LinearLayout) view.findViewById(R.id.fragment_login_progress_layout);
		username = (EditText) view.findViewById(R.id.fragment_login_username);
		facebook = (Button) view.findViewById(R.id.fragment_login_button_login_facebook);
		facebook.setOnClickListener(this);
		login = (Button) view.findViewById(R.id.fragment_login_button_login);
		login.setOnClickListener(this);
		skip = (Button) view.findViewById(R.id.fragment_login_button_skip);
		skip.setOnClickListener(this);

		setAlpha();

		if (savedInstanceState == null) {
			SharedPreferences sharedPrefsUserInfo = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
			if (!GCMHelper.getRegistrationId(getActivity().getApplicationContext()).equals("")) {
				facebook.setEnabled(true);
				login.setEnabled(true);
			}
			username.setText(sharedPrefsUserInfo.getString(mPrefUserUsername, ""));
		}

		return view;
	}

	@Override
	public void onClick(View view) {
		SharedPreferences sharedPrefsUserInfo = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
		SharedPreferences.Editor editUserInfo = sharedPrefsUserInfo.edit();
		if (view.getId() == R.id.fragment_login_button_login_facebook) {
			editUserInfo.putBoolean(mPrefUserSkipped, false).commit();
			logIn();
		} else if (view.getId() == R.id.fragment_login_button_login) {
			editUserInfo.putBoolean(mPrefUserSkipped, false).commit();
			logIn();
		} else if (view.getId() == R.id.fragment_login_button_skip) {
			editUserInfo.putBoolean(mPrefUserSkipped, true).commit();
			startMainActivity();
		}
	}

	private void attemptLogin(String username, String regID, String userID, String referral) {
		new RegisterID(this, getActivity()) {
			@Override
			protected Integer doInBackground(String... s) {
				return super.doInBackground(s);
				/* uncomment this section to test the progress bar and comment the line above*/
				/*try{
				    Thread.sleep(3000);
				}catch (InterruptedException e){
				    return 2;
				}
				return 3;*/
			}
		}.execute(username, regID, userID, referral);
	}

	public void registrationResult(int result) {
		if (result == 0) {
			// success
			startMainActivity();
		} else if (result == 1) {
			// network error
			Toast.makeText(getActivity(), "network error", Toast.LENGTH_LONG).show();
			endAnimationProgress();
		}
	}

	public void GCMRegistrationDone(boolean result) {
		facebook.setEnabled(true);
		login.setEnabled(true);
	}

	public void GCMConfirmDone(int result) {
		startMainActivity();
	}

	private void setAlpha() {
		ObjectAnimator animationProgressFadeOut = ObjectAnimator.ofFloat(progress, "alpha", 0);
		animationProgressFadeOut.setDuration(0);
		AnimatorSet animSet = new AnimatorSet();
		animSet.playTogether(animationProgressFadeOut);
		animSet.start();
	}

	private void startAnimationProgress() {
		AnimatorSet animSet = setupStartAnimation();
		animSet.start();
	}

	private void endAnimationProgress() {
		facebook.setEnabled(true);
		login.setEnabled(true);
		skip.setEnabled(true);
		AnimatorSet animSet = setupEndAnimation();
		animSet.start();
	}

	private AnimatorSet setupStartAnimation() {
		AnimatorSet animSet = new AnimatorSet();

		float width = facebook.getWidth() * 2;
		if (transY == 0) {
			int[] locations = new int[2];
			facebook.getLocationOnScreen(locations);
			float userNameY = locations[1];
			progress.getLocationOnScreen(locations);
			float progressBarY = locations[1];
			transY = userNameY - progressBarY;
		}

		AnimatorSet centerProgress = new AnimatorSet();
		ObjectAnimator animationProgressCenterX = ObjectAnimator.ofFloat(progress, "translationX", width);
		animationProgressCenterX.setDuration(0);
		ObjectAnimator animationProgressCenterY = ObjectAnimator.ofFloat(progress, "translationY", transY);
		animationProgressCenterY.setDuration(0);
		centerProgress.playTogether(animationProgressCenterX, animationProgressCenterY);

		AnimatorSet animMoveLeft = new AnimatorSet();
		ObjectAnimator animationInputsX = ObjectAnimator.ofFloat(inputs, "translationX", -width);
		animationInputsX.setDuration(250);
		ObjectAnimator animationProgressX = ObjectAnimator.ofFloat(progress, "translationX", 0);
		animationProgressX.setDuration(250);
		ObjectAnimator animationInputsFadeOut = ObjectAnimator.ofFloat(inputs, "alpha", 0);
		animationInputsFadeOut.setDuration(250);
		ObjectAnimator animationProgressFadeIn = ObjectAnimator.ofFloat(progress, "alpha", 1);
		animationProgressFadeIn.setDuration(250);
		animMoveLeft.playTogether(animationInputsX, animationProgressX, animationInputsFadeOut, animationProgressFadeIn);

		animSet.play(centerProgress).before(animMoveLeft);

		return animSet;
	}

	private AnimatorSet setupEndAnimation() {
		AnimatorSet animMoveRight = new AnimatorSet();
		float width = facebook.getWidth() * 2;

		ObjectAnimator animationFacebookX = ObjectAnimator.ofFloat(inputs, "translationX", 0);
		animationFacebookX.setDuration(250);
		ObjectAnimator animationProgressX = ObjectAnimator.ofFloat(progress, "translationX", width);
		animationProgressX.setDuration(250);
		ObjectAnimator animationFacebookFadeIn = ObjectAnimator.ofFloat(inputs, "alpha", 1);
		animationFacebookFadeIn.setDuration(250);
		ObjectAnimator animationProgressFadeOut = ObjectAnimator.ofFloat(progress, "alpha", 0);
		animationProgressFadeOut.setDuration(250);
		animMoveRight.playTogether(animationFacebookX, animationProgressX, animationFacebookFadeIn, animationProgressFadeOut);

		return animMoveRight;
	}

	private void startMainActivity() {
		SharedPreferences sharedPrefsUserInfo = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
		SharedPreferences.Editor editUserInfo = sharedPrefsUserInfo.edit();
		editUserInfo.putBoolean(mPrefUserLoggedIn, true).commit();
		Intent i = new Intent(getActivity(), MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		mCallback.onFinish();
	}
}
