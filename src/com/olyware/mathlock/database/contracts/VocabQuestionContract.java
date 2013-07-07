package com.olyware.mathlock.database.contracts;

public final class VocabQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_vocab_question";
	public static final String[] ALL_COLUMNS;
	public static final String[] ID_AND_PRIORITY;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, QuestionContract.ANSWER_CORRECT,
				QuestionContract.DIFFICULTY, VocabQuestionContract.PART_OF_SPEECH, VocabQuestionContract.PRIORITY };
	}
	static {
		ID_AND_PRIORITY = new String[] { BaseContract._ID, VocabQuestionContract.PRIORITY };
	}
	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String PART_OF_SPEECH = "part_of_speech";

	// Prevent instantiation of this class
	private VocabQuestionContract() {
	}
}
