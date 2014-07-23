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
import com.olyware.mathlock.service.CustomContactData;
import com.olyware.mathlock.utils.MoneyHelper;

/**
 * Created by Kyle on 2/11/14.
 */
public class ChallengeNewDialog extends DialogFragment {
	final public static String TAG = "fragment_challenge_new";
	final private static String USERNAME = "username";
	final private static String DIFFICULTY = "difficulty";
	final private static String BET = "bet";
	final private static String QUESTIONS = "questions";
	final private static String POSITIVE = "positive_text";
	final private static String NEGATIVE = "negative_text";
	final private static String NAME_TAG = "name_tag";
	private TextView userNameText, difficultyText, betText, questionsText;
	private OnAcceptOrDeclineListener listener;

	public interface OnAcceptOrDeclineListener {
		void onClick(boolean accepted);
	}

	public void setChallengeDialogListener(OnAcceptOrDeclineListener listener) {
		this.listener = listener;
	}

	public static ChallengeNewDialog newInstance(Context ctx, String userName, int bet, int diffMin, int diffMax, int questions,
			CustomContactData.ChallengeState state) {
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
		String pos = ctx.getString(R.string.ok);
		String neg = ctx.getString(R.string.cancel);
		String nameTag = ctx.getString(R.string.fragment_challenge_new_from);
		if (state == CustomContactData.ChallengeState.Sent) {
			pos = ctx.getString(R.string.fragment_challenge_new_wait);
			neg = ctx.getString(R.string.fragment_challenge_new_cancel);
			nameTag = ctx.getString(R.string.fragment_challenge_new_to);
		} else if (state == CustomContactData.ChallengeState.New) {
			pos = ctx.getString(R.string.fragment_challenge_new_accept);
			neg = ctx.getString(R.string.fragment_challenge_new_decline);
		}
		args.putString(POSITIVE, pos);
		args.putString(NEGATIVE, neg);
		args.putString(NAME_TAG, nameTag);

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

		TextView nameTag = (TextView) v.findViewById(R.id.challenge_new_name_tag);
		nameTag.setText(args.getString(NAME_TAG));

		userNameText = (TextView) v.findViewById(R.id.challenge_new_username);
		userNameText.setText(args.getString(USERNAME));

		difficultyText = (TextView) v.findViewById(R.id.challenge_new_difficulty);
		difficultyText.setText(args.getString(DIFFICULTY));

		betText = (TextView) v.findViewById(R.id.challenge_new_bet);
		betText.setText(String.valueOf(args.getInt(BET)));

		questionsText = (TextView) v.findViewById(R.id.challenge_new_questions);
		questionsText.setText(String.valueOf(args.getInt(QUESTIONS)));

		Button posiviteButton = (Button) v.findViewById(R.id.challenge_new_button_accept);
		posiviteButton.setText(args.getString(POSITIVE));
		posiviteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				listener.onClick(true);
			}
		});
		Button negativeButton = (Button) v.findViewById(R.id.challenge_new_button_decline);
		negativeButton.setText(args.getString(NEGATIVE));
		negativeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				listener.onClick(false);
			}
		});

		return v;
	}

}
