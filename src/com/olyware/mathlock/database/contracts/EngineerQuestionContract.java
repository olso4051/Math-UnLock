package com.olyware.mathlock.database.contracts;

public final class EngineerQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_engineer_question";
	public static final String[] ALL_COLUMNS;
	public static final String[] QUESTION_AND_PRIORITY;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, EngineerQuestionContract.VARIABLES,
				QuestionContract.DIFFICULTY, QuestionContract.PRIORITY, QuestionContract.TIME_STEP, QuestionContract.TIME_STEPS };
	}
	static {
		QUESTION_AND_PRIORITY = new String[] { QuestionContract.QUESTION_TEXT, EngineerQuestionContract.PRIORITY };
	}

	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String VARIABLES = "variables";

	// Prevent instantiation of this class
	private EngineerQuestionContract() {
	}
}