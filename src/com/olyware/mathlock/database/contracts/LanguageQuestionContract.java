package com.olyware.mathlock.database.contracts;

import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.R;

public final class LanguageQuestionContract extends QuestionContract {
	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String LANGUAGES[] = MainActivity.getContext().getResources().getStringArray(R.array.language_values_not_localized);
	public static final String LANGUAGE_PRIORITIES[] = MainActivity.getContext().getResources()
			.getStringArray(R.array.language_priorities_not_localized);
	public static final String PRIORITIES = "_priority";
	public static final String TABLE_NAME = "t_language_question";
	public static final String[] ALL_COLUMNS = { BaseContract._ID, QuestionContract.DIFFICULTY, LANGUAGES[0], LANGUAGE_PRIORITIES[0],
			LANGUAGES[1], LANGUAGE_PRIORITIES[1], LANGUAGES[2], LANGUAGE_PRIORITIES[2], LANGUAGES[3], LANGUAGE_PRIORITIES[4], LANGUAGES[4],
			LANGUAGE_PRIORITIES[4], LANGUAGES[5], LANGUAGE_PRIORITIES[5] };

	// Prevent instantiation of this class
	private LanguageQuestionContract() {
	}
}