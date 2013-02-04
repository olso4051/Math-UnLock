package com.olyware.mathlock.database.contracts;

import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.R;

public final class LanguageQuestionContract extends QuestionContract {
	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String LANGUAGES[] = MainActivity.getContext().getResources().getStringArray(R.array.language_values_not_localized);
	public static final String TABLE_NAME = "t_language_question";
	public static final String[] ALL_COLUMNS = { BaseContract._ID, QuestionContract.DIFFICULTY, LANGUAGES[0], LANGUAGES[1], LANGUAGES[2],
			LANGUAGES[3], LANGUAGES[4], LANGUAGES[5] };

	// Prevent instantiation of this class
	private LanguageQuestionContract() {
	}
}