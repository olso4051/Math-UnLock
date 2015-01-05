package com.olyware.mathlock.database.contracts;

public final class VocabQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_vocab_question";
	public static final String TEMP_TABLE_NAME = "t_vocab_question_temp";
	public static final String[] ALL_COLUMNS;
	public static final String[] QUESTION_AND_PRIORITY;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.QUESTION_TEXT, QuestionContract.ANSWER_CORRECT,
				QuestionContract.DIFFICULTY, VocabQuestionContract.PRIORITY, QuestionContract.TIME_STEP, QuestionContract.TIME_STEPS };
	}
	static {
		QUESTION_AND_PRIORITY = new String[] { QuestionContract.QUESTION_TEXT, VocabQuestionContract.PRIORITY };
	}

	// Prevent instantiation of this class
	private VocabQuestionContract() {
	}
}
