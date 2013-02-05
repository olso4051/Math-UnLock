package com.olyware.mathlock.database.contracts;

public final class MathQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_math_question";
	public static final String[] ALL_COLUMNS;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, MathQuestionContract.QUESTION_IMAGE,
				QuestionContract.ANSWER_CORRECT, MathQuestionContract.ANSWER_INCORRECT1, MathQuestionContract.ANSWER_INCORRECT2,
				MathQuestionContract.ANSWER_INCORRECT3, QuestionContract.DIFFICULTY, MathQuestionContract.PARSE_MODE };
	}

	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String QUESTION_IMAGE = "question_image";
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
	/**
	 * Type: TEXT
	 */
	public static final String PARSE_MODE = "parse_mode";

	// Prevent instantiation of this class
	private MathQuestionContract() {
	}
}
