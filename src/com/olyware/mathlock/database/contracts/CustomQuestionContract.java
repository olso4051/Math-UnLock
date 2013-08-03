package com.olyware.mathlock.database.contracts;

public final class CustomQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_custom_question";
	public static final String[] ALL_COLUMNS;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, QuestionContract.ANSWER_CORRECT,
				CustomQuestionContract.ANSWER_INCORRECT1, CustomQuestionContract.ANSWER_INCORRECT2,
				CustomQuestionContract.ANSWER_INCORRECT3, QuestionContract.DIFFICULTY, CustomQuestionContract.PRIORITY };
	}

	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String ANSWER_INCORRECT1 = "answer_incorrect1";
	/**
	 * Type: TEXT
	 */
	public static final String ANSWER_INCORRECT2 = "answer_incorrect2";
	/**
	 * Type: TEXT
	 */
	public static final String ANSWER_INCORRECT3 = "answer_incorrect3";

	// Prevent instantiation of this class
	private CustomQuestionContract() {
	}
}
