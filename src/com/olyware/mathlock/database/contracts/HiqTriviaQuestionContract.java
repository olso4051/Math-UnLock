package com.olyware.mathlock.database.contracts;

public final class HiqTriviaQuestionContract extends QuestionContract {

	public static final String TABLE_NAME1 = "t_hiqh_trivia_question";
	public static final String TABLE_NAME2 = "t_hiq_trivia_question";
	public static final String[] ALL_COLUMNS;
	public static final String[] QUESTION_AND_PRIORITY;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, QuestionContract.ANSWER_CORRECT,
				HiqTriviaQuestionContract.ANSWER_INCORRECT1, HiqTriviaQuestionContract.ANSWER_INCORRECT2,
				HiqTriviaQuestionContract.ANSWER_INCORRECT3, QuestionContract.DIFFICULTY, QuestionContract.PRIORITY,
				QuestionContract.TIME_STEP, QuestionContract.TIME_STEPS };
	}
	static {
		QUESTION_AND_PRIORITY = new String[] { QuestionContract.QUESTION_TEXT, HiqTriviaQuestionContract.PRIORITY };
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
	private HiqTriviaQuestionContract() {
	}
}