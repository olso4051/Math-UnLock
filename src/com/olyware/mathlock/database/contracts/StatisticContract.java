package com.olyware.mathlock.database.contracts;

public final class StatisticContract extends QuestionContract {

	public static final String TABLE_NAME = "t_statistic";
	public static final String[] ALL_COLUMNS;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, StatisticContract.PACKAGE, StatisticContract.CORRECT, QuestionContract.DIFFICULTY,
				StatisticContract.TIME };
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
	 * 1 - Easy 2 - Medium 3 - Hard Type : INTEGER
	 */
	public static final String TIME = "time";
}