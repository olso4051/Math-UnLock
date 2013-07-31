package com.olyware.mathlock.database.contracts;

public final class HiqHTriviaQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_hiqh_trivia_question";
	public static final String[] ALL_COLUMNS;
	public static final String[] QUESTION_AND_PRIORITY;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, QuestionContract.ANSWER_CORRECT,
				HiqHTriviaQuestionContract.ANSWER_INCORRECT1, HiqHTriviaQuestionContract.ANSWER_INCORRECT2,
				HiqHTriviaQuestionContract.ANSWER_INCORRECT3, QuestionContract.DIFFICULTY, QuestionContract.PRIORITY };
	}
	static {
		QUESTION_AND_PRIORITY = new String[] { QuestionContract.QUESTION_TEXT, HiqHTriviaQuestionContract.PRIORITY };
	}

	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String VARIABLES = "variables";
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
	private HiqHTriviaQuestionContract() {
	}
}