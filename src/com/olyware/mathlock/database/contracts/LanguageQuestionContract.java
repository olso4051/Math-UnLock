package com.olyware.mathlock.database.contracts;

import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.R;

public class LanguageQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_language_question";
	public static final String[] ALL_COLUMNS = new String[LanguageQuestionContract.LANGUAGES.length + 2];

	static {
		ALL_COLUMNS[0] = BaseContract._ID;
		ALL_COLUMNS[1] = QuestionContract.DIFFICULTY;
		for (int i = 2; i < LanguageQuestionContract.LANGUAGES.length + 2; i++)
			ALL_COLUMNS[i] = LanguageQuestionContract.LANGUAGES[i];
	}

	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String LANGUAGES[] = MainActivity.getContext().getResources().getStringArray(R.array.language_values_not_localized);

	// Prevent instantiation of this class
	private LanguageQuestionContract() {
	}
}