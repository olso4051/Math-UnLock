package com.olyware.mathlock.database;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.database.contracts.VocabQuestionContract;
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
		values.put(QuestionContract.QUESTION_TEXT, question.getText());
		values.put(VocabQuestionContract.PART_OF_SPEECH, question.getText());
		db.insert(VocabQuestionContract.TABLE_NAME, null, values);
	}

	public List<VocabQuestion> getVocabQuestions() {
		Cursor cursor = db.query(VocabQuestionContract.TABLE_NAME, QuestionContract.ALL_COLUMNS, null, null, null, null, null);
		return DatabaseModelFactory.buildVocabQuestions(cursor);
	}
}
