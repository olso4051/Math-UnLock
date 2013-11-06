package com.olyware.mathlock.database.contracts;

public abstract class QuestionContract implements BaseContract {

	public static final String[] ALL_COLUMNS;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, QuestionContract.ANSWER_CORRECT,
				QuestionContract.DIFFICULTY, QuestionContract.PRIORITY, QuestionContract.TIME_STEP, QuestionContract.TIME_STEPS };
	}

	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String QUESTION_TEXT = "question_text";

	/**
	 * Type: TEXT
	 */
	public static final String ANSWER_CORRECT = "answer_correct";

	/**
	 * 0 - Very Easy 1 - Easy 2 - Medium 3 - Hard 4 - Very Hard 5 - Insane Type : INTEGER
	 */
	public static final String DIFFICULTY = "difficulty";
	/**
	 * Type : TEXT
	 */
	public static final String PRIORITY = "priority";
	/**
	 * Type : INTEGER default = 100
	 */
	public static final int DEFAULT_PRIORITY = 100;
	/**
	 * time in milliseconds Type : INTEGER
	 */
	public static final String TIME_STEP = "time_step";
	/**
	 * max score for question Type : INTEGER
	 */
	public static final String TIME_STEPS = "time_steps";
}
