package com.olyware.mathlock.database.contracts;

public abstract class QuestionContract implements BaseContract {

	public static final String[] ALL_COLUMNS;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, QuestionContract.ANSWER_CORRECT,
				QuestionContract.DIFFICULTY };
	}

	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String QUESTION_TEXT = "question_text";

	/**
	 * Type: TEXT
	 */
	public static final String ANSWER_CORRECT = "answer_correct";

	/**
	 * 1 - Easy 2 - Medium 3 - Hard Type : INTEGER
	 */
	public static final String DIFFICULTY = "difficulty";
}
