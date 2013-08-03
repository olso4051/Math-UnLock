package com.olyware.mathlock.database;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.R;
import com.olyware.mathlock.database.contracts.BaseContract;
import com.olyware.mathlock.database.contracts.CustomQuestionContract;
import com.olyware.mathlock.database.contracts.EngineerQuestionContract;
import com.olyware.mathlock.database.contracts.HiqHTriviaQuestionContract;
import com.olyware.mathlock.database.contracts.LanguageQuestionContract;
import com.olyware.mathlock.database.contracts.MathQuestionContract;
import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.database.contracts.StatisticContract;
import com.olyware.mathlock.database.contracts.VocabQuestionContract;
import com.olyware.mathlock.model.CustomQuestion;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.EngineerQuestion;
import com.olyware.mathlock.model.HiqHTriviaQuestion;
import com.olyware.mathlock.model.LanguageQuestion;
import com.olyware.mathlock.model.MathQuestion;
import com.olyware.mathlock.model.Statistic;
import com.olyware.mathlock.model.VocabQuestion;

public class DatabaseManager {

	DatabaseOpenHelper dbHelper;
	SQLiteDatabase db;
	Cursor cursor;

	public DatabaseManager(Context context) {
		dbHelper = DatabaseOpenHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
	}

	public void destroy() {
		db.close();
		cursor.close();
		dbHelper.close();
	}

	public long addStat(Statistic stat) {
		ContentValues values = new ContentValues();
		values.put(StatisticContract.PACKAGE, stat.getPack());
		values.put(StatisticContract.CORRECT, stat.getCorrect());
		values.put(QuestionContract.DIFFICULTY, stat.getDifficulty().getValue());
		values.put(StatisticContract.TIME, stat.getTime());
		return db.insert(StatisticContract.TABLE_NAME, null, values);
	}

	public List<Integer> getStatPercentArray(long oldestTime, String Pack, String difficulty) {
		String where = StatisticContract.TIME + " >= " + String.valueOf(oldestTime);
		if (!Pack.equals(MainActivity.getContext().getResources().getString(R.string.all)))
			where = where + " AND " + StatisticContract.PACKAGE + " = '" + Pack + "'";
		if (!difficulty.equals(MainActivity.getContext().getResources().getString(R.string.all)))
			where = where + " AND " + StatisticContract.DIFFICULTY + " = " + String.valueOf(Difficulty.fromValue(difficulty).getValue());
		cursor = db.query(StatisticContract.TABLE_NAME, StatisticContract.ALL_COLUMNS, where, null, null, null, null);
		return DatabaseModelFactory.buildStats(cursor);
	}

	public MathQuestion getMathQuestion(Difficulty minDifficulty, Difficulty maxDifficulty, int notID) {
		String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
				+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
		cursor = db.query(MathQuestionContract.TABLE_NAME, MathQuestionContract.ALL_COLUMNS, where, null, null, null, null);

		Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + MathQuestionContract.TABLE_NAME + " WHERE "
				+ where, null);
		cursor2.moveToFirst();
		int sum = cursor2.getInt(0);
		cursor2.close();
		return DatabaseModelFactory.buildMathQuestion(cursor, sum);
	}

	public List<VocabQuestion> getVocabQuestions(Difficulty minDifficulty, Difficulty maxDifficulty, int number, int notID) {
		// String order = "RANDOM()";// LIMIT " + number;
		String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
				+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
		cursor = db.query(VocabQuestionContract.TABLE_NAME, QuestionContract.ALL_COLUMNS, where, null, null, null, null);

		Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + VocabQuestionContract.TABLE_NAME + " WHERE "
				+ where, null);
		cursor2.moveToFirst();
		int sum = cursor2.getInt(0);
		cursor2.close();
		return DatabaseModelFactory.buildVocabQuestions(cursor, sum, number);
	}

	public List<LanguageQuestion> getLanguageQuestions(Difficulty minDifficulty, Difficulty maxDifficulty, int number, String fromLanguage,
			String toLanguage, int notID) {
		String fromLanguagePriority = fromLanguage + LanguageQuestionContract.PRIORITIES;
		String toLanguagePriority = toLanguage + LanguageQuestionContract.PRIORITIES;
		String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
				+ String.valueOf(minDifficulty.getValue()) + " AND " + fromLanguage + "!=" + toLanguage + " AND " + BaseContract._ID
				+ " != " + notID;
		String[] columns = { fromLanguage, toLanguage, fromLanguagePriority, toLanguagePriority, QuestionContract.DIFFICULTY,
				QuestionContract._ID };
		cursor = db.query(LanguageQuestionContract.TABLE_NAME, columns, where, null, null, null, null);

		Cursor cursor2 = db.rawQuery("SELECT SUM(" + fromLanguagePriority + "+" + toLanguagePriority + ") FROM "
				+ LanguageQuestionContract.TABLE_NAME + " WHERE " + where, null);
		cursor2.moveToFirst();
		int sum = cursor2.getInt(0);
		cursor2.close();
		return DatabaseModelFactory.buildLanguageQuestions(cursor, fromLanguage, toLanguage, sum, number);
	}

	public EngineerQuestion getEngineerQuestion(Difficulty minDifficulty, Difficulty maxDifficulty, int notID) {
		String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
				+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
		cursor = db.query(EngineerQuestionContract.TABLE_NAME, EngineerQuestionContract.ALL_COLUMNS, where, null, null, null, null);

		Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + EngineerQuestionContract.TABLE_NAME
				+ " WHERE " + where, null);
		cursor2.moveToFirst();
		int sum = cursor2.getInt(0);
		cursor2.close();
		return DatabaseModelFactory.buildEngineerQuestion(cursor, sum);
	}

	public HiqHTriviaQuestion getHiqHTriviaQuestion(Difficulty minDifficulty, Difficulty maxDifficulty, int notID) {
		String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
				+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
		cursor = db.query(HiqHTriviaQuestionContract.TABLE_NAME, HiqHTriviaQuestionContract.ALL_COLUMNS, where, null, null, null, null);

		Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + HiqHTriviaQuestionContract.TABLE_NAME
				+ " WHERE " + where, null);
		cursor2.moveToFirst();
		int sum = cursor2.getInt(0);
		cursor2.close();
		return DatabaseModelFactory.buildHiqHTriviaQuestion(cursor, sum);
	}

	public CustomQuestion getCustomQuestion(Difficulty minDifficulty, Difficulty maxDifficulty, int notID) {
		String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
				+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
		cursor = db.query(CustomQuestionContract.TABLE_NAME, CustomQuestionContract.ALL_COLUMNS, where, null, null, null, null);

		int sum = 0;
		if (cursor.getCount() > 0) {
			Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + CustomQuestionContract.TABLE_NAME
					+ " WHERE " + where, null);
			cursor2.moveToFirst();
			sum = cursor2.getInt(0);
			cursor2.close();
		} else {
			where = "difficulty == " + "-1";
			cursor = db.query(CustomQuestionContract.TABLE_NAME, CustomQuestionContract.ALL_COLUMNS, where, null, null, null, null);
		}
		return DatabaseModelFactory.buildCustomQuestion(cursor, sum);
	}

	public void increasePriority(String tableName, String fromLanguage, String toLanguage, int ID) {
		if (!(tableName == null)) {
			// String tableNames[] = MainActivity.getContext().getResources().getStringArray(R.array.table_names);
			String fromLanguagePriority = fromLanguage + LanguageQuestionContract.PRIORITIES;
			String toLanguagePriority = toLanguage + LanguageQuestionContract.PRIORITIES;
			int priority[] = getPriority(tableName, fromLanguagePriority, toLanguagePriority, ID);
			priority[0] = Math.max(QuestionContract.DEFAULT_PRIORITY, priority[0] * 2);
			priority[1] = Math.max(QuestionContract.DEFAULT_PRIORITY, priority[1] * 2);
			String where = " WHERE " + BaseContract._ID + "=" + ID;
			String sql = "UPDATE " + tableName + " SET ";
			String priorityUpdate;
			if (tableName.equals(MainActivity.getContext().getResources().getString(R.string.language_table)))
				priorityUpdate = fromLanguagePriority + "=" + priority[0] + "," + toLanguagePriority + "=" + priority[1];
			else
				priorityUpdate = QuestionContract.PRIORITY + "=" + priority[0];
			sql = sql + priorityUpdate + where;
			db.execSQL(sql);
		}
	}

	public void decreasePriority(String tableName, String fromLanguage, String toLanguage, int ID) {
		if (!(tableName == null)) {
			// String tableNames[] = MainActivity.getContext().getResources().getStringArray(R.array.table_names);
			String fromLanguagePriority = fromLanguage + LanguageQuestionContract.PRIORITIES;
			String toLanguagePriority = toLanguage + LanguageQuestionContract.PRIORITIES;
			int priority[] = getPriority(tableName, fromLanguagePriority, toLanguagePriority, ID);
			priority[0] = Math.min(QuestionContract.DEFAULT_PRIORITY, priority[0] / 4);
			priority[1] = Math.min(QuestionContract.DEFAULT_PRIORITY, priority[1] / 4);
			priority[0] = Math.max(1, priority[0]);
			priority[1] = Math.max(1, priority[1]);
			String where = " WHERE " + BaseContract._ID + "=" + ID;
			String sql = "UPDATE " + tableName + " SET ";
			String priorityUpdate;
			if (tableName.equals(MainActivity.getContext().getResources().getString(R.string.language_table)))
				priorityUpdate = fromLanguagePriority + "=" + priority[0] + "," + toLanguagePriority + "=" + priority[1];
			else
				priorityUpdate = QuestionContract.PRIORITY + "=" + priority[0];
			sql = sql + priorityUpdate + where;
			db.execSQL(sql);
		}
	}

	private int[] getPriority(String tableName, String priority1, String priority2, int ID) {
		// String tableNames[] = MainActivity.getContext().getResources().getStringArray(R.array.table_names);
		int priority[] = { 0, 0 };
		if (tableName.equals(MainActivity.getContext().getResources().getString(R.string.language_table))) {
			cursor = db.rawQuery("SELECT " + priority1 + " FROM " + tableName + " WHERE " + QuestionContract._ID + "=" + ID, null);
			cursor.moveToFirst();
			priority[0] = cursor.getInt(0);
			cursor = db.rawQuery("SELECT " + priority2 + " FROM " + tableName + " WHERE " + QuestionContract._ID + "=" + ID, null);
			cursor.moveToFirst();
			priority[1] = cursor.getInt(0);
		} else {
			cursor = db.rawQuery(
					"SELECT " + QuestionContract.PRIORITY + " FROM " + tableName + " WHERE " + QuestionContract._ID + "=" + ID, null);
			cursor.moveToFirst();
			priority[0] = cursor.getInt(0);
		}
		return priority;
	}
}
