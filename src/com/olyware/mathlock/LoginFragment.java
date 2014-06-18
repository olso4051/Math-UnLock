package com.olyware.mathlock;

import java.util.Arrays;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.olyware.mathlock.service.RegisterID;
import com.olyware.mathlock.utils.GCMHelper;

public class LoginFragment extends Fragment implements View.OnClickListener {

	OnFinishedListener mCallback;

	// Container Activity must implement this interface
	public interface OnFinishedListener {
		public void restart();
	}

	public static String getUserName(Context ctx) {
		SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		return sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_facebook_name), "");
	}

	public static String getUserID(Context ctx) {
		SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		return sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_userid), "");
	}

	private String mPrefUserInfo, mPrefUserUsername, mPrefUserUserID, mPrefUserReferrer, mPrefUserLoggedIn, mPrefUserSkipped,
			mPrefUserFacebookName, mPrefUserFacebookBirth, mPrefUserFacebookGender, mPrefUserFacebookLocation, mPrefUserFacebookEmail;
	private float transY = 0;
	private EditText username;
	private Button login, skip;
	private LinearLayout inputs, progress;
	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception, false);
		}
	};
	private Session.StatusCallback loginCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception, true);
		}
	};

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

		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);

		mPrefUserInfo = ctx.getString(R.string.pref_user_info);
		mPrefUserUsername = ctx.getString(R.string.pref_user_username);
		mPrefUserUserID = ctx.getString(R.string.pref_user_userid);
		mPrefUserReferrer = ctx.getString(R.string.pref_user_referrer);
		mPrefUserLoggedIn = ctx.getString(R.string.pref_user_logged_in);
		mPrefUserSkipped = ctx.getString(R.string.pref_user_skipped);
		mPrefUserFacebookName = ctx.getString(R.string.pref_user_facebook_name);
		mPrefUserFacebookBirth = ctx.getString(R.string.pref_user_facebook_birth);
		mPrefUserFacebookGender = ctx.getString(R.string.pref_user_facebook_gender);
		mPrefUserFacebookLocation = ctx.getString(R.string.pref_user_facebook_location);
		mPrefUserFacebookEmail = ctx.getString(R.string.pref_user_facebook_email);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_login, container, false);

		inputs = (LinearLayout) view.findViewById(R.id.fragment_login_inputs);
		progress = (LinearLayout) view.findViewById(R.id.fragment_login_progress_layout);
		username = (EditText) view.findViewById(R.id.fragment_login_username);
		login = (Button) view.findViewById(R.id.fragment_login_button_login);
		login.setOnClickListener(this);
		skip = (Button) view.findViewById(R.id.fragment_login_button_skip);
		skip.setOnClickListener(this);

		LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setOnClickListener(this);
		authButton.setFragment(this);

		if (getArguments().getBoolean("facebook_logout")) {
			Log.d("test", "facebook_logout");
			Session session = Session.getActiveSession();
			if (!session.isClosed()) {
				session.closeAndClearTokenInformation();
			}
		}

		setAlpha();

		if (savedInstanceState == null) {
			SharedPreferences sharedPrefsUserInfo = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
			if (!GCMHelper.getRegistrationId(getActivity().getApplicationContext()).equals("")) {
				login.setEnabled(true);
			}
			username.setText(sharedPrefsUserInfo.getString(mPrefUserUsername, ""));
		}

		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		// For scenarios where the main activity is launched and user session is not null, the session state change notification may not be
		// triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null, false);
		}
		uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View view) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		// if (sharedPrefs.getInt("layout_status_bar_height", -1) < 0) {
		if (true) {
			Display display = getActivity().getWindowManager().getDefaultDisplay();
			int sizeY;
			if (android.os.Build.VERSION.SDK_INT < 13) {
				sizeY = display.getHeight();
			} else {
				Point size = new Point();
				display.getSize(size);
				sizeY = size.y;
			}
			View content = getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT);
			int statusBarHeight = sizeY - content.getHeight();
			Log.d("test", "sizeY = " + sizeY + " | content.getHeight() = " + content.getHeight() + " | statusBarHeight = "
					+ statusBarHeight);
			sharedPrefs.edit().putInt("layout_status_bar_height", statusBarHeight).commit();
		}
		SharedPreferences sharedPrefsUserInfo = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
		SharedPreferences.Editor editUserInfo = sharedPrefsUserInfo.edit();
		if (view.getId() == R.id.fragment_login_button_login) {
			editUserInfo.putBoolean(mPrefUserSkipped, false).commit();
			logIn();
		} else if (view.getId() == R.id.fragment_login_button_skip) {
			editUserInfo.putBoolean(mPrefUserSkipped, true).commit();
			startMainActivity();
		} else if (view.getId() == R.id.authButton) {
			editUserInfo.putBoolean(mPrefUserSkipped, false).commit();
			Session session = Session.getActiveSession();
			if (!session.isOpened() && !session.isClosed()) {
				Log.d("test", "openForRead");
				session.openForRead(new Session.OpenRequest(this).setPermissions(
						Arrays.asList("email", "public_profile", "user_friends"/*, "user_birthday", "user_location"*/)).setCallback(
						loginCallback));
			} else if (session.isOpened()) {
				session.closeAndClearTokenInformation();
			} else {
				Log.d("test", "openActiveSession");
				Session.openActiveSession(getActivity(), this, true, loginCallback);
			}
		}
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception, boolean fromFacebookButton) {
		Log.d("test", "onSessionStateChange + fromFacebookButton = " + fromFacebookButton);
		if (state.isOpened()) {
			Log.d("test", "Logged in... " + fromFacebookButton);
			if (fromFacebookButton) {
				// Request user data and show the results
				Request.newMeRequest(session, new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (user != null) {
							String gender = "", birthday = "", location = "", email = "";
							if (user.getProperty("gender") != null)
								gender = user.getProperty("gender").toString();
							if (user.getBirthday() != null)
								birthday = user.getBirthday();
							if (user.getLocation() != null)
								location = user.getLocation().getProperty("name").toString();
							if (user.getProperty("email") != null)
								email = user.getProperty("email").toString();
							SharedPreferences sharedPrefsUserInfo = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
							sharedPrefsUserInfo.edit().putString(mPrefUserFacebookName, user.getName())
									.putString(mPrefUserFacebookBirth, birthday).putString(mPrefUserFacebookGender, gender)
									.putString(mPrefUserFacebookLocation, location).putString(mPrefUserFacebookEmail, email)
									.putBoolean(mPrefUserSkipped, false).commit();
							Log.d("GAtest", user.toString());
							logIn();
						} else {
							Toast.makeText(getActivity(), "network error", Toast.LENGTH_LONG).show();
						}
					}
				}).executeAsync();
			}
		} else if (state.isClosed()) {
			Log.d("test", "Logged out... " + fromFacebookButton);
		}
	}

	void logIn() {
		login.setEnabled(false);
		skip.setEnabled(false);

		SharedPreferences sharedPrefsUserInfo = getActivity().getSharedPreferences(mPrefUserInfo, Context.MODE_PRIVATE);
		String regID = GCMHelper.getRegistrationId(getActivity().getApplicationContext());
		String userID = sharedPrefsUserInfo.getString(mPrefUserUserID, "");
		String referrer = sharedPrefsUserInfo.getString(mPrefUserReferrer, "");
		String birth = sharedPrefsUserInfo.getString(mPrefUserFacebookBirth, "");
		String gender = sharedPrefsUserInfo.getString(mPrefUserFacebookGender, "");
		String location = sharedPrefsUserInfo.getString(mPrefUserFacebookLocation, "");
		String email = sharedPrefsUserInfo.getString(mPrefUserFacebookEmail, "");
		String uName = sharedPrefsUserInfo.getString(mPrefUserFacebookName, "");
		if (uName.equals(""))
			uName = username.getText().toString();
		sharedPrefsUserInfo.edit().putString(mPrefUserUsername, uName).commit();

		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		View focus = getActivity().getCurrentFocus();
		if (imm != null && focus != null) {
			imm.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		attemptLogin(uName, regID, userID, referrer, birth, gender, location, email);

		startAnimationProgress();
	}

	private void attemptLogin(String username, String regID, String userID, String referral, String birth, String gender, String location,
			String email) {
		Log.d("test", "attemptLoging username=" + username + "|regID.equals(\"\")=" + regID.equals("") + "|userID=" + userID + "|referral="
				+ referral + "|birth=" + birth + "|gender=" + gender + "|location=" + location + "|email=" + email);
		new RegisterID(getActivity()) {
			@Override
			protected void onPostExecute(Integer result) {
				Log.d("test", "result=" + result);
				Log.d("test", "success=" + getSuccess());
				Log.d("test", "error=" + getError());
				if (result == 0) {
					// success
					startMainActivity();
				} else if (result == 1) {
					// network error
					Toast.makeText(getActivity(), "network error", Toast.LENGTH_LONG).show();
					endAnimationProgress();
				}
			}
		}.execute(username, regID, userID, referral, birth, gender, location, email);
	}

	public void GCMRegistrationDone(boolean result) {
		if (login != null)
			login.setEnabled(true);
	}

	// FOR REFERENCE
	/*private String buildUserInfoDisplay(GraphUser user) {
	    StringBuilder userInfo = new StringBuilder("");

	    // Example: typed access (name)
	    // - no special permissions required
	    userInfo.append(String.format("Name: %s\n\n", 
	        user.getName()));

	    // Example: typed access (birthday)
	    // - requires user_birthday permission
	    userInfo.append(String.format("Birthday: %s\n\n", 
	        user.getBirthday()));

	    // Example: partially typed access, to location field,
	    // name key (location)
	    // - requires user_location permission
	    userInfo.append(String.format("Location: %s\n\n", 
	        user.getLocation().getProperty("name")));

	    // Example: access via property name (locale)
	    // - no special permissions required
	    userInfo.append(String.format("Locale: %s\n\n", 
	        user.getProperty("locale")));

	    // Example: access via key for array (languages) 
	    // - requires user_likes permission
	    JSONArray languages = (JSONArray)user.getProperty("languages");
	    if (languages.length() > 0) {
	        ArrayList<String> languageNames = new ArrayList<String> ();
	        for (int i=0; i < languages.length(); i++) {
	            JSONObject language = languages.optJSONObject(i);
	            // Add the language name to a list. Use JSON
	            // methods to get access to the name field. 
	            languageNames.add(language.optString("name"));
	        }           
	        userInfo.append(String.format("Languages: %s\n\n", 
	        languageNames.toString()));
	    }

	    return userInfo.toString();
	}*/

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
		login.setEnabled(true);
		skip.setEnabled(true);
		AnimatorSet animSet = setupEndAnimation();
		animSet.start();
	}

	private AnimatorSet setupStartAnimation() {
		AnimatorSet animSet = new AnimatorSet();

		float width = inputs.getWidth() * 2;
		if (transY == 0) {
			int[] locations = new int[2];
			inputs.getLocationOnScreen(locations);
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
		float width = inputs.getWidth() * 2;

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

		mCallback.restart();
	}
}
