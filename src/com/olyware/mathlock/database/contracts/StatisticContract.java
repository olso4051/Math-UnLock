package com.olyware.mathlock.database.contracts;

public final class StatisticContract extends QuestionContract {

	public static final String TABLE_NAME = "t_statistic";
	public static final String[] ALL_COLUMNS;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, StatisticContract.PACKAGE, StatisticContract.CORRECT, QuestionContract.DIFFICULTY,
				StatisticContract.TIME, StatisticContract.TIME2SOLVE };
	}

	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String PACKAGE = "package";

	/**
	 * Type: TEXT
	 */
	public static final String CORRECT = "correct";

	/**
	 * Type : INTEGER
	 */
	public static final String TIME = "time";
	/**
	 * Type : INTEGER
	 */
	public static final String TIME2SOLVE = "time_2_solve";
}