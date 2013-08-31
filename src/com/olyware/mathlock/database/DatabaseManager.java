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
		if (db.isOpen()) {
			db.close();
			cursor.close();
			dbHelper.close();
		}
	}

	public long addStat(Statistic stat) {
		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put(StatisticContract.PACKAGE, stat.getPack());
			values.put(StatisticContract.CORRECT, stat.getCorrect());
			values.put(QuestionContract.DIFFICULTY, stat.getDifficulty().getValue());
			values.put(StatisticContract.TIME, stat.getTime());
			return db.insert(StatisticContract.TABLE_NAME, null, values);
		} else
			return -1;
	}

	public List<Integer> getStatPercentArray(long oldestTime, String Pack, String difficulty) {
		if (db.isOpen()) {
			String where = StatisticContract.TIME + " >= " + String.valueOf(oldestTime);
			if (!Pack.equals(MainActivity.getContext().getResources().getString(R.string.all)))
				where = where + " AND " + StatisticContract.PACKAGE + " = '" + Pack + "'";
			if (!difficulty.equals(MainActivity.getContext().getResources().getString(R.string.all)))
				where = where + " AND " + StatisticContract.DIFFICULTY + " = "
						+ String.valueOf(Difficulty.fromValue(difficulty).getValue());
			cursor = db.query(StatisticContract.TABLE_NAME, StatisticContract.ALL_COLUMNS, where, null, null, null, null);
			return DatabaseModelFactory.buildStats(cursor);
		} else
			return null;
	}

	public MathQuestion getMathQuestion(Difficulty minDifficulty, Difficulty maxDifficulty, long notID) {
		if (db.isOpen()) {
			String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
					+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
			cursor = db.query(MathQuestionContract.TABLE_NAME, MathQuestionContract.ALL_COLUMNS, where, null, null, null, null);

			Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + MathQuestionContract.TABLE_NAME
					+ " WHERE " + where, null);
			cursor2.moveToFirst();
			int sum = cursor2.getInt(0);
			cursor2.close();
			return DatabaseModelFactory.buildMathQuestion(cursor, sum);
		} else
			return null;
	}

	public List<VocabQuestion> getVocabQuestions(Difficulty minDifficulty, Difficulty maxDifficulty, int number, long notID) {
		if (db.isOpen()) {
			// String order = "RANDOM()";// LIMIT " + number;
			String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
					+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
			cursor = db.query(VocabQuestionContract.TABLE_NAME, QuestionContract.ALL_COLUMNS, where, null, null, null, null);

			Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + VocabQuestionContract.TABLE_NAME
					+ " WHERE " + where, null);
			cursor2.moveToFirst();
			int sum = cursor2.getInt(0);
			cursor2.close();
			return DatabaseModelFactory.buildVocabQuestions(cursor, sum, number);
		} else
			return null;
	}

	public List<LanguageQuestion> getLanguageQuestions(Difficulty minDifficulty, Difficulty maxDifficulty, int number, String fromLanguage,
			String toLanguage, long notID) {
		if (db.isOpen()) {
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
		} else
			return null;
	}

	public EngineerQuestion getEngineerQuestion(Difficulty minDifficulty, Difficulty maxDifficulty, long notID) {
		if (db.isOpen()) {
			String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
					+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
			cursor = db.query(EngineerQuestionContract.TABLE_NAME, EngineerQuestionContract.ALL_COLUMNS, where, null, null, null, null);

			Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + EngineerQuestionContract.TABLE_NAME
					+ " WHERE " + where, null);
			cursor2.moveToFirst();
			int sum = cursor2.getInt(0);
			cursor2.close();
			return DatabaseModelFactory.buildEngineerQuestion(cursor, sum);
		} else
			return null;
	}

	public HiqHTriviaQuestion getHiqHTriviaQuestion(Difficulty minDifficulty, Difficulty maxDifficulty, long notID) {
		if (db.isOpen()) {
			String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
					+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
			cursor = db.query(HiqHTriviaQuestionContract.TABLE_NAME, HiqHTriviaQuestionContract.ALL_COLUMNS, where, null, null, null, null);

			Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + HiqHTriviaQuestionContract.TABLE_NAME
					+ " WHERE " + where, null);
			cursor2.moveToFirst();
			int sum = cursor2.getInt(0);
			cursor2.close();
			return DatabaseModelFactory.buildHiqHTriviaQuestion(cursor, sum);
		} else
			return null;
	}

	public CustomQuestion getCustomQuestion(Difficulty minDifficulty, Difficulty maxDifficulty, long notID) {
		if (db.isOpen()) {
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
		} else
			return null;
	}

	public List<CustomQuestion> getAllCustomQuestions() {
		if (db.isOpen()) {
			String where = "difficulty >= " + 0;
			String[] columns = CustomQuestionContract.ALL_COLUMNS;
			cursor = db.query(CustomQuestionContract.TABLE_NAME, columns, where, null, null, null, null);
			return DatabaseModelFactory.buildAllCustomQuestions(cursor);
		} else
			return null;
	}

	public int removeCustomQuestion(long ID) {
		if (db.isOpen()) {
			String where = BaseContract._ID + " = " + ID;
			return db.delete(CustomQuestionContract.TABLE_NAME, where, null);
		} else
			return 0;
	}

	public long addCustomQuestion(String[] question, int difficulty) {
		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put(QuestionContract.QUESTION_TEXT, question[0]);
			values.put(QuestionContract.ANSWER_CORRECT, question[1]);
			values.put(CustomQuestionContract.ANSWER_INCORRECT1, question[2]);
			values.put(CustomQuestionContract.ANSWER_INCORRECT2, question[3]);
			values.put(CustomQuestionContract.ANSWER_INCORRECT3, question[4]);
			values.put(QuestionContract.DIFFICULTY, difficulty);
			values.put(QuestionContract.PRIORITY, QuestionContract.DEFAULT_PRIORITY);
			return db.insert(CustomQuestionContract.TABLE_NAME, null, values);
		} else
			return -1;
	}

	public long updateCustomQuestion(long ID, String[] question, int difficulty) {
		if (db.isOpen()) {
			String where = BaseContract._ID + " = " + ID;
			ContentValues values = new ContentValues();
			values.put(QuestionContract.QUESTION_TEXT, question[0]);
			values.put(QuestionContract.ANSWER_CORRECT, question[1]);
			values.put(CustomQuestionContract.ANSWER_INCORRECT1, question[2]);
			values.put(CustomQuestionContract.ANSWER_INCORRECT2, question[3]);
			values.put(CustomQuestionContract.ANSWER_INCORRECT3, question[4]);
			values.put(QuestionContract.DIFFICULTY, difficulty);
			values.put(QuestionContract.PRIORITY, QuestionContract.DEFAULT_PRIORITY);
			return db.update(CustomQuestionContract.TABLE_NAME, values, where, null);
		} else
			return 0;
	}

	public boolean increasePriority(String tableName, String fromLanguage, String toLanguage, long ID) {
		if (db.isOpen()) {
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
				return true;
			}
		}
		return false;
	}

	public boolean decreasePriority(String tableName, String fromLanguage, String toLanguage, long ID) {
		if (db.isOpen()) {
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
				return true;
			}
		}
		return false;
	}

	private int[] getPriority(String tableName, String priority1, String priority2, long ID) {
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
