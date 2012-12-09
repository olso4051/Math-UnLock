package com.olyware.mathlock.database;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.model.Question;

public class DatabaseManager {

	DatabaseOpenHelper dbHelper;
	SQLiteDatabase db;

	public DatabaseManager(Context context) {
		dbHelper = DatabaseOpenHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
	}

	public void addQuestion(Question question) {
		ContentValues values = new ContentValues();
		values.put(QuestionContract.ANSWER_CORRECT, question.getCorrectAnswer());
		String wrongAnswersStr = StringUtils.join(question.getWrongAnswers(), ",");
		values.put(QuestionContract.ANSWER_WRONG, wrongAnswersStr);
		values.put(QuestionContract.DIFFICULTY, question.getDifficulty().getValue());
		values.put(QuestionContract.QUESTION_IMAGE, question.getImage().getAbsolutePath());
		values.put(QuestionContract.QUESTION_TEXT, question.getText());
		db.insert(QuestionContract.TABLE_NAME, null, values);
	}

	public List<Question> getAllQuestions() {
		Cursor cursor = db.query(QuestionContract.TABLE_NAME, QuestionContract.ALL_COLUMNS, null, null, null, null, null);
		return DatabaseModelFactory.buildQuestions(cursor);
	}
}
