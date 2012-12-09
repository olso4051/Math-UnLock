package com.olyware.mathlock.database.contracts;

public final class QuestionContract implements BaseContract {

	public static final String TABLE_NAME = "t_question";
	public static final String[] ALL_COLUMNS;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, QuestionContract.QUESTION_IMAGE,
				QuestionContract.ANSWER_CORRECT, QuestionContract.ANSWER_WRONG, QuestionContract.DIFFICULTY };
	}

	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String QUESTION_TEXT = "question_text";

	/**
	 * Type: TEXT
	 */
	public static final String QUESTION_IMAGE = "question_image";

	/**
	 * Type: TEXT
	 */
	public static final String ANSWER_CORRECT = "answer_correct";

	/**
	 * Type: TEXT
	 */
	public static final String ANSWER_WRONG = "answer_wrong";

	/**
	 * 1 - Easy 2 - Medium 3 - Hard Type : INTEGER
	 */
	public static final String DIFFICULTY = "difficulty";

	// Prevent instantiation of this class
	private QuestionContract() {
	}
}
