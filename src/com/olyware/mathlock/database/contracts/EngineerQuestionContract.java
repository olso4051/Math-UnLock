package com.olyware.mathlock.database.contracts;

public final class EngineerQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_engineer_question";
	public static final String[] ALL_COLUMNS;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, EngineerQuestionContract.VARIABLES,
				QuestionContract.DIFFICULTY, EngineerQuestionContract.PARSE_MODE, QuestionContract.PRIORITY };
	}

	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String VARIABLES = "variables";
	/**
	 * Type: TEXT
	 */
	public static final String PARSE_MODE = "parse_mode";

	// Prevent instantiation of this class
	private EngineerQuestionContract() {
	}
}