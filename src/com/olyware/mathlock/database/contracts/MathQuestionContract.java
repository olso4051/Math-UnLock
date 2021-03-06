package com.olyware.mathlock.database.contracts;

public final class MathQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_math_question";
	public static final String[] ALL_COLUMNS;
	public static final String[] QUESTION_AND_PRIORITY;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, MathQuestionContract.QUESTION_IMAGE,
				QuestionContract.ANSWER_CORRECT, MathQuestionContract.ANSWER_INCORRECT1, MathQuestionContract.ANSWER_INCORRECT2,
				MathQuestionContract.ANSWER_INCORRECT3, QuestionContract.DIFFICULTY, MathQuestionContract.PARSE_MODE,
				MathQuestionContract.RANGE, MathQuestionContract.PRECISION, MathQuestionContract.PRIORITY, QuestionContract.TIME_STEP,
				QuestionContract.TIME_STEPS };
	}
	static {
		QUESTION_AND_PRIORITY = new String[] { QuestionContract.QUESTION_TEXT, MathQuestionContract.PRIORITY };
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
	/**
	 * Type: TEXT
	 */
	public static final String RANGE = "range";
	/**
	 * Type: TEXT
	 */
	public static final String PRECISION = "precision";
	/**
	 * Type: TEXT
	 */
	public static final String PRIORITY = "priority";

	// Prevent instantiation of this class
	private MathQuestionContract() {
	}
}
