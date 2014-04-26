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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class LoginFragment extends Fragment implements View.OnClickListener, RegisterID.RegisterIdResponse {

	OnFinishedListener mCallback;

	// Container Activity must implement this interface
	public interface OnFinishedListener {
		public void onFinish();
	}

	private String mPrefUserInfo, mPrefUserUsername, mPrefUserPassword, mPrefUserUserID;
	private float transY = 0;
	private EditText username, password;
	private Button facebook, login, skip;
	private TextView disclaimer, loggingIn;
	private ProgressBar progressBar;

	private void saveSessionID(String userID) {
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
		sharedPreferences.edit().putString(mPrefUserUserID, userID).commit();
	}

	/**
	 * Saves user info and switches to the main activity
	 * 
	 * @param userInfo
	 *            the info with which to login
	 */
	void logIn() {
		String uName = username.getText().toString();
		String uPass = password.getText().toString();
		facebook.setEnabled(false);
		login.setEnabled(false);
		skip.setEnabled(false);

		SharedPreferences sharedPrefs = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
		sharedPrefs.edit().putString(mPrefUserUsername, uName).putString(mPrefUserPassword, uPass).commit();

		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		View focus = getActivity().getCurrentFocus();
		if (imm != null && focus != null) {
			imm.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		attemptLogin(uName, uPass, "regID", "userID", "userID");

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
		mPrefUserPassword = ctx.getString(R.string.pref_user_password);
		mPrefUserUserID = ctx.getString(R.string.pref_user_userid);

		// check if user is logged in
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
		if (!sharedPreferences.getString(mPrefUserUserID, "").equals("")) {
			startMainActivity();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_login, container, false);

		username = (EditText) view.findViewById(R.id.fragment_login_username);
		password = (EditText) view.findViewById(R.id.fragment_login_password);
		facebook = (Button) view.findViewById(R.id.fragment_login_button_login_facebook);
		facebook.setOnClickListener(this);
		login = (Button) view.findViewById(R.id.fragment_login_button_login);
		login.setOnClickListener(this);
		skip = (Button) view.findViewById(R.id.fragment_login_button_skip);
		skip.setOnClickListener(this);

		setAlpha(view);

		if (savedInstanceState == null) {
			SharedPreferences sharedPrefs = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
			username.setText(sharedPrefs.getString(mPrefUserUsername, ""));
			password.setText(sharedPrefs.getString(mPrefUserPassword, ""));
		}

		return view;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.fragment_login_button_login) {
			logIn();
		} else if (view.getId() == R.id.fragment_login_button_skip) {
			startMainActivity();
		} else if (view.getId() == R.id.fragment_login_button_skip) {
			logIn();
		}
	}

	private void attemptLogin(String username, String password, String regID, String userID, String referral) {
		new RegisterID(getActivity()) {
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
		}.execute(username, password, regID, userID, referral);
	}

	public void registrationResult(int result, String userID) {
		if (result == 0) {
			// success
			saveSessionID(userID);
			startMainActivity();
		} else if (result == 1) {
			// network error
			Toast.makeText(getActivity(), "network error", Toast.LENGTH_LONG).show();
			endAnimationProgress();
			// TODO popup box with button to call 624-walk
		} else if (result == 2) {
			// service error
			Toast.makeText(getActivity(), "service error", Toast.LENGTH_LONG).show();
			endAnimationProgress();
			// TODO same popup box
		} else if (result == 3) {
			// developer didn't login
			endAnimationProgress();
			// startMainActivity();
		}
	}

	private void setAlpha(View root) {
		ObjectAnimator animationProgressFadeOut = ObjectAnimator.ofFloat(progressBar, "alpha", 0);
		animationProgressFadeOut.setDuration(0);
		ObjectAnimator animationLoggingInFadeOut = ObjectAnimator.ofFloat(loggingIn, "alpha", 0);
		animationLoggingInFadeOut.setDuration(0);
		AnimatorSet animSet = new AnimatorSet();
		animSet.playTogether(animationProgressFadeOut, animationLoggingInFadeOut);
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

		float width = username.getWidth() * 2;
		if (transY == 0) {
			int[] locations = new int[2];
			username.getLocationOnScreen(locations);
			float userNameY = locations[1];
			progressBar.getLocationOnScreen(locations);
			float progressBarY = locations[1];
			transY = userNameY - progressBarY;
		}

		AnimatorSet centerProgress = new AnimatorSet();
		ObjectAnimator animationProgressCenterX = ObjectAnimator.ofFloat(progressBar, "translationX", +width);
		animationProgressCenterX.setDuration(0);
		ObjectAnimator animationProgressCenterY = ObjectAnimator.ofFloat(progressBar, "translationY", transY);
		animationProgressCenterY.setDuration(0);
		ObjectAnimator animationLoggingInCenterX = ObjectAnimator.ofFloat(loggingIn, "translationX", +width);
		animationLoggingInCenterX.setDuration(0);
		ObjectAnimator animationLoggingInCenterY = ObjectAnimator.ofFloat(loggingIn, "translationY", transY);
		animationLoggingInCenterY.setDuration(0);
		centerProgress.playTogether(animationProgressCenterX, animationProgressCenterY, animationLoggingInCenterX,
				animationLoggingInCenterY);

		AnimatorSet animMoveLeft = new AnimatorSet();
		ObjectAnimator animationFacebookX = ObjectAnimator.ofFloat(facebook, "translationX", -width);
		animationFacebookX.setDuration(250);
		ObjectAnimator animationNameX = ObjectAnimator.ofFloat(username, "translationX", -width);
		animationNameX.setDuration(250);
		ObjectAnimator animationPhoneX = ObjectAnimator.ofFloat(password, "translationX", -width);
		animationPhoneX.setDuration(250);
		ObjectAnimator animationDisclaimerX = ObjectAnimator.ofFloat(disclaimer, "translationX", -width);
		animationDisclaimerX.setDuration(250);
		ObjectAnimator animationButtonX = ObjectAnimator.ofFloat(login, "translationX", -width);
		animationButtonX.setDuration(250);
		ObjectAnimator animationSkipX = ObjectAnimator.ofFloat(skip, "translationX", -width);
		animationSkipX.setDuration(250);
		ObjectAnimator animationProgressX = ObjectAnimator.ofFloat(progressBar, "translationX", 0);
		animationProgressX.setDuration(250);
		ObjectAnimator animationLoggingInX = ObjectAnimator.ofFloat(loggingIn, "translationX", 0);
		animationLoggingInX.setDuration(250);
		ObjectAnimator animationFacebookFadeOut = ObjectAnimator.ofFloat(facebook, "alpha", 0);
		animationFacebookFadeOut.setDuration(250);
		ObjectAnimator animationNameFadeOut = ObjectAnimator.ofFloat(username, "alpha", 0);
		animationNameFadeOut.setDuration(250);
		ObjectAnimator animationPhoneFadeOut = ObjectAnimator.ofFloat(password, "alpha", 0);
		animationPhoneFadeOut.setDuration(250);
		ObjectAnimator animationDisclaimerFadeOut = ObjectAnimator.ofFloat(disclaimer, "alpha", 0);
		animationDisclaimerFadeOut.setDuration(250);
		ObjectAnimator animationButtonFadeOut = ObjectAnimator.ofFloat(login, "alpha", 0);
		animationButtonFadeOut.setDuration(250);
		ObjectAnimator animationSkipFadeOut = ObjectAnimator.ofFloat(skip, "alpha", 0);
		animationSkipFadeOut.setDuration(250);
		ObjectAnimator animationProgressFadeIn = ObjectAnimator.ofFloat(progressBar, "alpha", 1);
		animationProgressFadeIn.setDuration(250);
		ObjectAnimator animationLoggingInFadeIn = ObjectAnimator.ofFloat(loggingIn, "alpha", 1);
		animationLoggingInFadeIn.setDuration(250);
		animMoveLeft.playTogether(animationFacebookX, animationNameX, animationPhoneX, animationDisclaimerX, animationButtonX,
				animationSkipX, animationProgressX, animationLoggingInX, animationFacebookFadeOut, animationNameFadeOut,
				animationPhoneFadeOut, animationDisclaimerFadeOut, animationButtonFadeOut, animationSkipFadeOut, animationProgressFadeIn,
				animationLoggingInFadeIn);

		animSet.play(centerProgress).before(animMoveLeft);

		return animSet;
	}

	private AnimatorSet setupEndAnimation() {
		AnimatorSet animMoveRight = new AnimatorSet();
		float width = username.getWidth() * 2;

		ObjectAnimator animationFacebookX = ObjectAnimator.ofFloat(facebook, "translationX", 0);
		animationFacebookX.setDuration(250);
		ObjectAnimator animationNameX = ObjectAnimator.ofFloat(username, "translationX", 0);
		animationNameX.setDuration(250);
		ObjectAnimator animationPhoneX = ObjectAnimator.ofFloat(password, "translationX", 0);
		animationPhoneX.setDuration(250);
		ObjectAnimator animationDisclaimerX = ObjectAnimator.ofFloat(disclaimer, "translationX", 0);
		animationDisclaimerX.setDuration(250);
		ObjectAnimator animationButtonX = ObjectAnimator.ofFloat(login, "translationX", 0);
		animationButtonX.setDuration(250);
		ObjectAnimator animationSkipX = ObjectAnimator.ofFloat(skip, "translationX", 0);
		animationSkipX.setDuration(250);
		ObjectAnimator animationProgressX = ObjectAnimator.ofFloat(progressBar, "translationX", width);
		animationProgressX.setDuration(250);
		ObjectAnimator animationLoggingInX = ObjectAnimator.ofFloat(loggingIn, "translationX", width);
		animationLoggingInX.setDuration(250);
		ObjectAnimator animationFacebookFadeIn = ObjectAnimator.ofFloat(facebook, "alpha", 1);
		animationFacebookFadeIn.setDuration(250);
		ObjectAnimator animationNameFadeIn = ObjectAnimator.ofFloat(username, "alpha", 1);
		animationNameFadeIn.setDuration(250);
		ObjectAnimator animationPassFadeIn = ObjectAnimator.ofFloat(password, "alpha", 1);
		animationPassFadeIn.setDuration(250);
		ObjectAnimator animationDisclaimerFadeIn = ObjectAnimator.ofFloat(disclaimer, "alpha", 1);
		animationDisclaimerFadeIn.setDuration(250);
		ObjectAnimator animationButtonFadeIn = ObjectAnimator.ofFloat(login, "alpha", 1);
		animationButtonFadeIn.setDuration(250);
		ObjectAnimator animationSkipFadeIn = ObjectAnimator.ofFloat(skip, "alpha", 1);
		animationSkipFadeIn.setDuration(250);
		ObjectAnimator animationProgressFadeOut = ObjectAnimator.ofFloat(progressBar, "alpha", 0);
		animationProgressFadeOut.setDuration(250);
		ObjectAnimator animationLoggingInFadeOut = ObjectAnimator.ofFloat(loggingIn, "alpha", 0);
		animationLoggingInFadeOut.setDuration(250);
		animMoveRight.playTogether(animationFacebookX, animationNameX, animationPhoneX, animationDisclaimerX, animationButtonX,
				animationSkipX, animationProgressX, animationLoggingInX, animationFacebookFadeIn, animationNameFadeIn, animationPassFadeIn,
				animationDisclaimerFadeIn, animationButtonFadeIn, animationSkipFadeIn, animationProgressFadeOut, animationLoggingInFadeOut);

		return animMoveRight;
	}

	private void startMainActivity() {
		Intent i = new Intent(getActivity(), MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		mCallback.onFinish();
	}
}
