package com.olyware.mathlock.database;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.R;
import com.olyware.mathlock.database.contracts.LanguageQuestionContract;
import com.olyware.mathlock.database.contracts.MathQuestionContract;
import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.database.contracts.StatisticContract;
import com.olyware.mathlock.database.contracts.VocabQuestionContract;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.LanguageQuestion;
import com.olyware.mathlock.model.MathQuestion;
import com.olyware.mathlock.model.Statistic;
import com.olyware.mathlock.model.VocabQuestion;

public class DatabaseManager {

	DatabaseOpenHelper dbHelper;
	SQLiteDatabase db;

	public DatabaseManager(Context context) {
		dbHelper = DatabaseOpenHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
	}

	public long addStat(Statistic stat) {
		ContentValues values = new ContentValues();
		values.put(StatisticContract.PACKAGE, stat.getPack());
		values.put(StatisticContract.CORRECT, stat.getCorrect());
		values.put(QuestionContract.DIFFICULTY, stat.getDifficulty().getValue());
		values.put(StatisticContract.TIME, String.valueOf(stat.getTime()));
		return db.insert(StatisticContract.TABLE_NAME, null, values);
	}

	public List<Integer> getStatPercentArray(long oldestTime, String Pack, String difficulty) {
		String where = StatisticContract.TIME + " >= " + String.valueOf(oldestTime);
		if (!Pack.equals(MainActivity.getContext().getResources().getString(R.string.all)))
			where = where + " AND " + StatisticContract.PACKAGE + " = " + Pack;
		if (!difficulty.equals(MainActivity.getContext().getResources().getString(R.string.all)))
			where = where + " AND " + StatisticContract.DIFFICULTY + " = " + String.valueOf(Difficulty.fromValue(difficulty).getValue());
		Cursor cursor = db.query(StatisticContract.TABLE_NAME, StatisticContract.ALL_COLUMNS, where, null, null, null, null);
		return DatabaseModelFactory.buildStats(cursor);
	}

	public MathQuestion getMathQuestion(Difficulty minDifficulty, Difficulty maxDifficulty) {
		String order = "RANDOM() LIMIT 1";
		String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
				+ String.valueOf(minDifficulty.getValue());
		Cursor cursor = db.query(MathQuestionContract.TABLE_NAME, MathQuestionContract.ALL_COLUMNS, where, null, null, null, order);
		return DatabaseModelFactory.buildMathQuestion(cursor);
	}

	public long addVocabQuestion(VocabQuestion question) {
		ContentValues values = new ContentValues();
		values.put(QuestionContract.ANSWER_CORRECT, question.getCorrectAnswer());
		values.put(QuestionContract.DIFFICULTY, question.getDifficulty().getValue());
		values.put(QuestionContract.QUESTION_TEXT, question.getQuestionText());
		// values.put(VocabQuestionContract.PART_OF_SPEECH, question.getText());
		return db.insert(VocabQuestionContract.TABLE_NAME, null, values);
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

	public List<VocabQuestion> getVocabQuestions(Difficulty minDifficulty, Difficulty maxDifficulty, int number) {
		String order = "RANDOM() LIMIT " + number;
		String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
				+ String.valueOf(minDifficulty.getValue());
		Cursor cursor = db.query(VocabQuestionContract.TABLE_NAME, QuestionContract.ALL_COLUMNS, where, null, null, null, order);
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
