package com.olyware.mathlock.database.contracts;

public class LanguageQuestionContract extends QuestionContract {

	public static final String TABLE_NAME = "t_language_question";
	public static final String[] ALL_COLUMNS;

	static {
		ALL_COLUMNS = new String[] { BaseContract._ID, QuestionContract.DIFFICULTY, LanguageQuestionContract.ENGLISH,
				LanguageQuestionContract.SPANISH, LanguageQuestionContract.PORTUGUESE, LanguageQuestionContract.RUSSIAN,
				LanguageQuestionContract.GERMAN, LanguageQuestionContract.CZECH, LanguageQuestionContract.DANISH,
				LanguageQuestionContract.DUTCH };
	}

	// COLUMN DEFS
	/**
	 * Type: TEXT
	 */
	public static final String LANGUAGES[] = { "english", "spanish", "portuguese", "russian", "german", "czech", "danish", "dutch" };
	public static final String ENGLISH = "english";
	public static final String SPANISH = "spanish";
	public static final String PORTUGUESE = "portuguese";
	public static final String RUSSIAN = "russian";
	public static final String GERMAN = "german";
	public static final String CZECH = "czech";
	public static final String DANISH = "danish";
	public static final String DUTCH = "dutch";

	// Prevent instantiation of this class
	private LanguageQuestionContract() {
	}
}