package com.olyware.mathlock.database;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.olyware.mathlock.database.contracts.LanguageQuestionContract;
import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.database.contracts.VocabQuestionContract;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.LanguageQuestion;
import com.olyware.mathlock.model.VocabQuestion;

public class DatabaseManager {

	DatabaseOpenHelper dbHelper;
	SQLiteDatabase db;

	public DatabaseManager(Context context) {
		dbHelper = DatabaseOpenHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
	}

	public void addVocabQuestion(VocabQuestion question) {
		ContentValues values = new ContentValues();
		values.put(QuestionContract.ANSWER_CORRECT, question.getCorrectAnswer());
		values.put(QuestionContract.DIFFICULTY, question.getDifficulty().getValue());
		values.put(QuestionContract.QUESTION_TEXT, question.getQuestionText());
		// values.put(VocabQuestionContract.PART_OF_SPEECH, question.getText());
		db.insert(VocabQuestionContract.TABLE_NAME, null, values);
	}

	public List<VocabQuestion> getAllVocabQuestions() {
		Cursor cursor = db.query(VocabQuestionContract.TABLE_NAME, QuestionContract.ALL_COLUMNS, null, null, null, null, null);
		return DatabaseModelFactory.buildVocabQuestions(cursor);
	}

	public List<VocabQuestion> getVocabQuestions(Difficulty difficulty) {
		String where = "difficulty <= ?";
		String[] whereArgs = new String[] { String.valueOf(difficulty.getValue()) };
		Cursor cursor = db.query(VocabQuestionContract.TABLE_NAME, QuestionContract.ALL_COLUMNS, where, whereArgs, null, null, null);
		return DatabaseModelFactory.buildVocabQuestions(cursor);
	}

	public List<VocabQuestion> getVocabQuestions(Difficulty difficulty, int number) {
		String order = "RANDOM() LIMIT " + number;
		String where = "difficulty <= ?";
		String[] whereArgs = new String[] { String.valueOf(difficulty.getValue()) };
		Cursor cursor = db.query(VocabQuestionContract.TABLE_NAME, QuestionContract.ALL_COLUMNS, where, whereArgs, null, null, order);
		return DatabaseModelFactory.buildVocabQuestions(cursor);
	}

	public List<LanguageQuestion> getLanguageQuestions(Difficulty difficulty, String fromLanguage, String toLanguage) {
		String where = "difficulty <= ?";
		String[] whereArgs = new String[] { String.valueOf(difficulty.getValue()) };
		String[] columns = { fromLanguage, toLanguage, QuestionContract.DIFFICULTY };
		Cursor cursor = db.query(LanguageQuestionContract.TABLE_NAME, columns, where, whereArgs, null, null, null);
		return DatabaseModelFactory.buildLanguageQuestions(cursor, fromLanguage, toLanguage);
	}

	public List<LanguageQuestion> getLanguageQuestions(Difficulty difficulty, int number, String fromLanguage, String toLanguage) {
		String order = "RANDOM() LIMIT " + number;
		String where = "difficulty <= ?";
		String[] whereArgs = new String[] { String.valueOf(difficulty.getValue()) };
		String[] columns = { fromLanguage, toLanguage, QuestionContract.DIFFICULTY };
		Cursor cursor = db.query(LanguageQuestionContract.TABLE_NAME, columns, where, whereArgs, null, null, order);
		return DatabaseModelFactory.buildLanguageQuestions(cursor, fromLanguage, toLanguage);
	}
}
