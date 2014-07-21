package com.olyware.mathlock.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.olyware.mathlock.R;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.utils.MoneyHelper;

/**
 * Created by Kyle on 2/11/14.
 */
public class ChallengeNewDialog extends DialogFragment {
	final public static String TAG = "fragment_challenge_new";
	final private static String USERNAME = "username", DIFFICULTY = "difficulty", BET = "bet", QUESTIONS = "questions";
	private TextView userNameText, difficultyText, betText, questionsText;
	private OnAcceptOrDeclineListener listener;

	public interface OnAcceptOrDeclineListener {
		void onClick(boolean accepted);
	}

	public void setChallengeDialogListener(OnAcceptOrDeclineListener listener) {
		this.listener = listener;
	}

	public static ChallengeNewDialog newInstance(Context ctx, String userName, int bet, int diffMin, int diffMax, int questions) {
		ChallengeNewDialog f = new ChallengeNewDialog();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putString(USERNAME, userName);
		String diff = Difficulty.fromValueToString(diffMin);
		if (diffMin != diffMax)
			diff += " - " + Difficulty.fromValueToString(diffMax);
		args.putString(DIFFICULTY, diff);
		args.putInt(BET, MoneyHelper.getModifiedBet(ctx, bet));
		args.putInt(QUESTIONS, questions);

		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int style = DialogFragment.STYLE_NO_TITLE;
		int theme = R.style.ChallengeTheme;
		setStyle(style, theme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_challenge_new, container, false);

		Bundle args = getArguments();

		userNameText = (TextView) v.findViewById(R.id.challenge_new_username);
		userNameText.setText(args.getString(USERNAME));

		difficultyText = (TextView) v.findViewById(R.id.challenge_new_difficulty);
		difficultyText.setText(args.getString(DIFFICULTY));

		betText = (TextView) v.findViewById(R.id.challenge_new_bet);
		betText.setText(String.valueOf(args.getInt(BET)));

		questionsText = (TextView) v.findViewById(R.id.challenge_new_questions);
		questionsText.setText(String.valueOf(args.getInt(QUESTIONS)));

		Button acceptButton = (Button) v.findViewById(R.id.challenge_new_button_accept);
		acceptButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				listener.onClick(true);
			}
		});
		Button declineButton = (Button) v.findViewById(R.id.challenge_new_button_decline);
		declineButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				listener.onClick(false);
			}
		});

		return v;
	}

}
