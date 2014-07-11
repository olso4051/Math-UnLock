package com.olyware.mathlock.database.contracts;

public final class ChallengeQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_challenge_question";
	public static final String[] ALL_COLUMNS;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, ChallengeQuestionContract.CHALLENGE_ID, QuestionContract.QUESTION_TEXT,
				QuestionContract.ANSWER_CORRECT, ChallengeQuestionContract.ANSWER_INCORRECT1, ChallengeQuestionContract.ANSWER_INCORRECT2,
				ChallengeQuestionContract.ANSWER_INCORRECT3, ChallengeQuestionContract.USER_NAME };
	}

	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String CHALLENGE_ID = "challenge_id";
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
	public static final String USER_NAME = "user_name";

	// Prevent instantiation of this class
	private ChallengeQuestionContract() {
	}
}
