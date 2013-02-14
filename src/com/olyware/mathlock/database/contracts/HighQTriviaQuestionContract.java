package com.olyware.mathlock.database.contracts;

public final class HighQTriviaQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_highq_trivia_question";
	public static final String[] ALL_COLUMNS;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, QuestionContract.ANSWER_CORRECT,
				HighQTriviaQuestionContract.ANSWER_INCORRECT1, HighQTriviaQuestionContract.ANSWER_INCORRECT2,
				HighQTriviaQuestionContract.ANSWER_INCORRECT3, QuestionContract.DIFFICULTY, QuestionContract.PRIORITY };
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
	private HighQTriviaQuestionContract() {
	}
}