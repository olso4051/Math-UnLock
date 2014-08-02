package com.olyware.mathlock.database.contracts;

public final class SwisherTriviaQuestionContract extends QuestionContract {

	public static final String TABLE_NAME1 = "t_swisher_trivia";
	public static final String TABLE_NAME2 = "t_swisher_trivia";
	public static final String[] ALL_COLUMNS;
	public static final String[] QUESTION_AND_PRIORITY;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, QuestionContract.ANSWER_CORRECT,
				SwisherTriviaQuestionContract.ANSWER_INCORRECT1, SwisherTriviaQuestionContract.ANSWER_INCORRECT2,
				SwisherTriviaQuestionContract.ANSWER_INCORRECT3, QuestionContract.DIFFICULTY, QuestionContract.PRIORITY,
				QuestionContract.TIME_STEP, QuestionContract.TIME_STEPS };
	}
	static {
		QUESTION_AND_PRIORITY = new String[] { QuestionContract.QUESTION_TEXT, SwisherTriviaQuestionContract.PRIORITY };
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
	private SwisherTriviaQuestionContract() {
	}
}