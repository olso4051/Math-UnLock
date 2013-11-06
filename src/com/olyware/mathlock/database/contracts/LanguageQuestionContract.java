package com.olyware.mathlock.database.contracts;

import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.R;

public final class LanguageQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_language_question";
	public static final String[] LANGUAGES = MainActivity.getContext().getResources().getStringArray(R.array.language_values_not_localized);
	public static final String[] LANGUAGE_PRIORITIES = MainActivity.getContext().getResources()
			.getStringArray(R.array.language_priorities_not_localized);
	public static final String PRIORITIES = "_priority";
	public static final String[] ALL_COLUMNS;
	public static final String[] ID_AND_PRIORITY;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, LANGUAGES[0], LANGUAGE_PRIORITIES[0], LANGUAGES[1], LANGUAGE_PRIORITIES[1],
				LANGUAGES[2], LANGUAGE_PRIORITIES[2], LANGUAGES[3], LANGUAGE_PRIORITIES[3], LANGUAGES[4], LANGUAGE_PRIORITIES[4],
				LANGUAGES[5], LANGUAGE_PRIORITIES[5], LANGUAGES[6], LANGUAGE_PRIORITIES[6], LANGUAGES[7], LANGUAGE_PRIORITIES[7],
				LANGUAGES[8], LANGUAGE_PRIORITIES[8], LANGUAGES[9], LANGUAGE_PRIORITIES[9], LANGUAGES[10], LANGUAGE_PRIORITIES[10],
				LANGUAGES[11], LANGUAGE_PRIORITIES[11], LANGUAGES[12], LANGUAGE_PRIORITIES[12], LANGUAGES[13], LANGUAGE_PRIORITIES[13],
				LANGUAGES[14], LANGUAGE_PRIORITIES[14], LANGUAGES[15], LANGUAGE_PRIORITIES[15], LANGUAGES[16], LANGUAGE_PRIORITIES[16],
				QuestionContract.DIFFICULTY, QuestionContract.TIME_STEP, QuestionContract.TIME_STEPS };
	}
	static {
		ID_AND_PRIORITY = new String[] { BaseContract._ID, LANGUAGE_PRIORITIES[0], LANGUAGE_PRIORITIES[1], LANGUAGE_PRIORITIES[2],
				LANGUAGE_PRIORITIES[3], LANGUAGE_PRIORITIES[4], LANGUAGE_PRIORITIES[5], LANGUAGE_PRIORITIES[6], LANGUAGE_PRIORITIES[7],
				LANGUAGE_PRIORITIES[8], LANGUAGE_PRIORITIES[9], LANGUAGE_PRIORITIES[10], LANGUAGE_PRIORITIES[11], LANGUAGE_PRIORITIES[12],
				LANGUAGE_PRIORITIES[13], LANGUAGE_PRIORITIES[14], LANGUAGE_PRIORITIES[15], LANGUAGE_PRIORITIES[16] };
	}

	// Prevent instantiation of this class
	private LanguageQuestionContract() {
	}
}