package com.olyware.mathlock.views;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.olyware.mathlock.R;

/**
 * Created by Kyle on 2/11/14.
 */
public class ChallengeDialog extends DialogFragment implements View.OnClickListener {

	public interface ChallengeDialogListener {
		void onInvitePressed();

		void onStartPressed();

		void onNextPressed();

		void onChallengeDialogDestroyed();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int style = DialogFragment.STYLE_NORMAL;
		int theme = R.style.ChallengeTheme;
		setStyle(style, theme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_challenge, container, false);
		getDialog().setCanceledOnTouchOutside(true);
		getDialog().setTitle(getString(R.string.fragment_challenge_title));

		((Button) v.findViewById(R.id.fragment_challenge_button_invite)).setOnClickListener(this);
		((Button) v.findViewById(R.id.fragment_challenge_button_start)).setOnClickListener(this);
		((Button) v.findViewById(R.id.fragment_challenge_button_next)).setOnClickListener(this);

		return v;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.fragment_challenge_button_invite) {
			ChallengeDialogListener activity = (ChallengeDialogListener) getActivity();
			activity.onInvitePressed();
			this.dismiss();
		} else if (view.getId() == R.id.fragment_challenge_button_start) {
			ChallengeDialogListener activity = (ChallengeDialogListener) getActivity();
			activity.onStartPressed();
			this.dismiss();
		} else if (view.getId() == R.id.fragment_challenge_button_next) {
			ChallengeDialogListener activity = (ChallengeDialogListener) getActivity();
			activity.onNextPressed();
			this.dismiss();
		}
	}

	@Override
	public void onDestroyView() {
		ChallengeDialogListener activity = (ChallengeDialogListener) getActivity();
		activity.onChallengeDialogDestroyed();
		super.onDestroyView();
	}
}
