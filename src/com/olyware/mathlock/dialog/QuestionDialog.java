package com.olyware.mathlock.dialog;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.QuestionSelectAdapter;
import com.olyware.mathlock.adapter.QuestionSelectData;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.utils.ChallengeBuilder;
import com.olyware.mathlock.utils.PreferenceHelper;
import com.olyware.mathlock.utils.Toaster;
import com.olyware.mathlock.views.RangeSeekBar;
import com.olyware.mathlock.views.RangeSeekBar.OnRangeSeekBarChangeListener;

/**
 * Created by Kyle on 2/11/14.
 */
public class QuestionDialog extends DialogFragment {
	final public static String TAG = "fragment_question_select";
	final private static int MAX_QUESTIONS = 5, MIN_QUESTIONS = 1, MIN_BET = 0, MIN_PROGRESS = 100;
	private TextView betText, questionsText, difficultyMinText, difficultyMaxText;
	private Button challenge, cancel;
	private SeekBar seekBet, seekQuestions;
	private RangeSeekBar seekDifficulty;
	private ListView lv;
	private String difficultyMin, difficultyMax;;
	private int betMax, betValue, betProgressMax, questionsValue, questionsProgressMax, difficultyMinValue, difficultyMaxValue;
	private float betPercent, questionsPercent;
	private QuestionSelectAdapter adapter;
	private List<String> questionPackNames;
	private List<Integer> questionPackIDs;
	private boolean[] questionPacksChecked;
	private ArrayList<QuestionSelectData> questionPacks;
	private ChallengeBuilder builder;
	private QuestionDialogListener listener;

	public interface QuestionDialogListener {
		void onChallenge(ChallengeBuilder builder);
	}

	public void setQuestionDialogListener(QuestionDialogListener listener) {
		this.listener = listener;
	}

	public static QuestionDialog newInstance(Context ctx, ArrayList<String> questionPacks, ArrayList<Integer> questionPackIDs, long maxBet,
			int maxHeight) {
		QuestionDialog f = new QuestionDialog();

		Bundle settings = PreferenceHelper.getChallengeSettings(ctx);
		boolean[] checked = PreferenceHelper.getChallengePacksChecked(ctx, questionPacks);

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putFloat(PreferenceHelper.CHALLENGE_PREFS_BET_DEFAULT, settings.getFloat(PreferenceHelper.CHALLENGE_PREFS_BET_DEFAULT));
		args.putInt(PreferenceHelper.CHALLENGE_PREFS_QUESTION_NUMBER, settings.getInt(PreferenceHelper.CHALLENGE_PREFS_QUESTION_NUMBER));
		args.putInt(PreferenceHelper.CHALLENGE_PREFS_DIFFICULTY_MIN, settings.getInt(PreferenceHelper.CHALLENGE_PREFS_DIFFICULTY_MIN));
		args.putInt(PreferenceHelper.CHALLENGE_PREFS_DIFFICULTY_MAX, settings.getInt(PreferenceHelper.CHALLENGE_PREFS_DIFFICULTY_MAX));
		args.putStringArrayList("question_packs", questionPacks);
		args.putIntegerArrayList("question_pack_ids", questionPackIDs);
		args.putBooleanArray("questions_is_checked", checked);
		if (maxBet >= Integer.MAX_VALUE)
			args.putInt("max_bet", Integer.MAX_VALUE);
		else
			args.putInt("max_bet", (int) maxBet);
		args.putInt("height", maxHeight);

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
		View v = inflater.inflate(R.layout.fragment_question_select, container, false);
		View topView = inflater.inflate(R.layout.fragment_question_select_top, container, false);

		Bundle args = getArguments();

		betMax = args.getInt("max_bet");
		betPercent = args.getFloat(PreferenceHelper.CHALLENGE_PREFS_BET_DEFAULT);
		betValue = (int) (betMax * betPercent);
		betProgressMax = Math.max(betMax + MIN_BET, MIN_PROGRESS + MIN_BET);
		questionsValue = args.getInt(PreferenceHelper.CHALLENGE_PREFS_QUESTION_NUMBER);
		questionsProgressMax = Math.max(MIN_PROGRESS + MIN_QUESTIONS, MAX_QUESTIONS - MIN_QUESTIONS);
		questionsPercent = ((float) (questionsValue - MIN_QUESTIONS)) / ((float) (MAX_QUESTIONS - MIN_BET));
		difficultyMinValue = args.getInt(PreferenceHelper.CHALLENGE_PREFS_DIFFICULTY_MIN);
		difficultyMaxValue = args.getInt(PreferenceHelper.CHALLENGE_PREFS_DIFFICULTY_MAX);
		difficultyMin = Difficulty.fromValueToString(difficultyMinValue);
		difficultyMax = Difficulty.fromValueToString(difficultyMaxValue);

		// Bet Slider
		betText = (TextView) topView.findViewById(R.id.question_select_bet);
		betText.setText(String.valueOf(betValue));
		seekBet = (SeekBar) topView.findViewById(R.id.question_select_seekbar_bet);
		seekBet.setMax(betProgressMax);
		seekBet.setProgress((int) ((betProgressMax - MIN_BET) * betPercent));
		seekBet.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				betPercent = ((float) (progress - MIN_BET)) / ((float) (betProgressMax - MIN_BET));
				betValue = (int) (betPercent * (betMax - MIN_BET)) + MIN_BET;
				betText.setText(String.valueOf(betValue));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// do nothing
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// do nothing
			}
		});

		// Number of Questions Slider
		questionsText = (TextView) topView.findViewById(R.id.question_select_questions);
		questionsText.setText(String.valueOf(questionsValue));
		seekQuestions = (SeekBar) topView.findViewById(R.id.question_select_seekbar_questions);
		seekQuestions.setMax(questionsProgressMax);
		seekQuestions.setProgress((int) ((questionsProgressMax - MIN_QUESTIONS) * questionsPercent));
		seekQuestions.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float percent = ((float) (progress - MIN_QUESTIONS))
						/ ((float) (Math.max(MIN_PROGRESS + MIN_QUESTIONS, MAX_QUESTIONS - MIN_QUESTIONS) - MIN_QUESTIONS));
				questionsValue = (int) (percent * (MAX_QUESTIONS - MIN_QUESTIONS)) + MIN_QUESTIONS;
				questionsText.setText(String.valueOf(questionsValue));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// do nothing
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// do nothing
			}
		});

		// Difficulty slider
		difficultyMinText = (TextView) topView.findViewById(R.id.question_select_difficulty_min);
		difficultyMinText.setText(difficultyMin);
		difficultyMaxText = (TextView) topView.findViewById(R.id.question_select_difficulty_max);
		difficultyMaxText.setText(difficultyMax);
		seekDifficulty = (RangeSeekBar) topView.findViewById(R.id.question_select_seekbar_difficulty);
		seekDifficulty.setNotifyWhileDragging(true);
		seekDifficulty.setSelectedMinValue(difficultyMinValue);
		seekDifficulty.setSelectedMaxValue(difficultyMaxValue);
		seekDifficulty.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar bar, int minValue, int maxValue) {
				difficultyMinValue = minValue;
				difficultyMaxValue = maxValue;
				difficultyMin = Difficulty.fromValueToString(difficultyMinValue);
				difficultyMax = Difficulty.fromValueToString(difficultyMaxValue);
				difficultyMinText.setText(difficultyMin);
				difficultyMaxText.setText(difficultyMax);
			}
		});

		// Question Packs ListView
		lv = (ListView) v.findViewById(R.id.question_select_list_view);
		lv.addHeaderView(topView);

		// ListView Data
		questionPackNames = new ArrayList<String>();
		questionPackNames.addAll(args.getStringArrayList("question_packs"));
		questionPackIDs = new ArrayList<Integer>();
		questionPackIDs.addAll(args.getIntegerArrayList("question_pack_ids"));
		questionPacksChecked = args.getBooleanArray("questions_is_checked");
		questionPacks = new ArrayList<QuestionSelectData>(questionPackNames.size());
		for (int i = 0; i < questionPackNames.size(); i++) {
			questionPacks.add(new QuestionSelectData(questionPackNames.get(i), questionPacksChecked[i], questionPackIDs.get(i)));
		}

		// Adding items to ListView
		adapter = new QuestionSelectAdapter(getActivity(), R.layout.list_question_pack_item, questionPacks);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				boolean checked = !questionPacks.get(pos - lv.getHeaderViewsCount()).isChecked();
				questionPacks.get(pos - lv.getHeaderViewsCount()).setChecked(checked);
				if (questionPacks.get(pos - lv.getHeaderViewsCount()).getID() == 0) {
					if (questionPacks.size() > 1) {
						for (int i = 0; i < questionPacks.size(); i++) {
							questionPacks.get(i).setChecked(checked);
						}
					}
				} else if (questionPacks.get(0).getID() == 0) {
					questionPacks.get(0).setChecked(false);
				}
				adapter.notifyDataSetChanged();
			}
		});
		// setListViewHeightBasedOnChildren(lv, 6);

		challenge = (Button) v.findViewById(R.id.question_select_button_challenge);
		challenge.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				List<QuestionSelectData> selectedQuestionPacks = new ArrayList<QuestionSelectData>(questionPackNames.size());
				for (QuestionSelectData questionPack : questionPacks) {
					if (questionPack.isChecked()) {
						selectedQuestionPacks.add(questionPack);
					}
				}
				if (selectedQuestionPacks.size() > 0) {
					PreferenceHelper.storeChallengePacks(getActivity(), questionPacks);
					PreferenceHelper.storeChallengeSettings(getActivity(), betPercent, questionsValue, difficultyMinValue,
							difficultyMaxValue);
					builder.setQuestionSettings(betValue, questionsValue, difficultyMinValue, difficultyMaxValue);
					builder.setSelectedQuestionPacks(selectedQuestionPacks);
					listener.onChallenge(builder);
				} else {
					Toaster.toastChallengeNoPacksSelected(getActivity());
				}
			}

		});
		cancel = (Button) v.findViewById(R.id.question_select_button_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}

		});
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		Bundle args = getArguments();
		int maxHeight = args.getInt("height");
		if (maxHeight > 0) {
			Window window = getDialog().getWindow();
			int width = window.getAttributes().width;
			int height = (int) (maxHeight * .80d);
			window.setLayout(width, height);
		}
	}

	public void setBuilder(ChallengeBuilder builder) {
		this.builder = builder;
	}

	public static void setListViewHeightBasedOnChildren(ListView listView, int minViews) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
		for (int i = 0; i < listAdapter.getCount(); i++) {// Math.min(minViews, listAdapter.getCount()); i++) {
			/*int lastDivision = 1;
			if (i == minViews - 1)
				lastDivision = 3;*/
			View listItem = listAdapter.getView(i, null, listView);
			if (listItem instanceof ViewGroup) {
				listItem.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			}
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();// / lastDivision;
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}
}
