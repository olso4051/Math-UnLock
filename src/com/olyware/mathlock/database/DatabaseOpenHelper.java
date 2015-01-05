package com.olyware.mathlock.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.R;
import com.olyware.mathlock.database.contracts.BaseContract;
import com.olyware.mathlock.database.contracts.CustomQuestionContract;
import com.olyware.mathlock.database.contracts.EngineerQuestionContract;
import com.olyware.mathlock.database.contracts.HiqTriviaQuestionContract;
import com.olyware.mathlock.database.contracts.LanguageQuestionContract;
import com.olyware.mathlock.database.contracts.MathQuestionContract;
import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.database.contracts.StatisticContract;
import com.olyware.mathlock.database.contracts.VocabQuestionContract;
import com.olyware.mathlock.utils.Loggy;

/**
 * This creates/opens the actual SQLite database file.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "mathunlock.db";
	private static final int DATABASE_VERSION = 9;
	private static String DATABASE_PATH, DATABASE_FULL_PATH, DATABASE_OLD_FULL_PATH;
	private static int DATABASE_OLD_VERSION;

	private Context context;
	private static DatabaseOpenHelper instance = null;

	private DatabaseOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		DATABASE_FULL_PATH = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
		DATABASE_PATH = DATABASE_FULL_PATH.substring(0, DATABASE_FULL_PATH.indexOf(DATABASE_NAME));
		DATABASE_OLD_FULL_PATH = DATABASE_PATH + "old_" + DATABASE_NAME;

		int databaseState = dbState();
		if (databaseState == 0)
			copyDatabase();
		else if (databaseState == 2)
			upgradeDatabase();

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE TABLE IF NOT EXISTS " + VocabQuestionContract.TEMP_TABLE_NAME + " ( ");
		sqlBuilder.append(QuestionContract.QUESTION_TEXT + " STRING, ");
		sqlBuilder.append(QuestionContract.ANSWER_CORRECT + " STRING,");
		sqlBuilder.append(QuestionContract.DIFFICULTY + " INTEGER,");
		sqlBuilder.append(QuestionContract.PRIORITY + " INTEGER,");
		sqlBuilder.append(QuestionContract.TIME_STEP + " INTEGER,");
		sqlBuilder.append(QuestionContract.TIME_STEPS + " INTEGER ");
		sqlBuilder.append(");");
		SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_FULL_PATH, null, SQLiteDatabase.OPEN_READWRITE);
		db.execSQL(sqlBuilder.toString());

		String[] LANGUAGES = MainActivity.getContext().getResources().getStringArray(R.array.language_with_db);
		String[] LANGUAGE_PRIORITIES = MainActivity.getContext().getResources().getStringArray(R.array.language_priorities_with_db);
		sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE TABLE IF NOT EXISTS " + LanguageQuestionContract.TEMP_TABLE_NAME + " ( ");
		sqlBuilder.append(LANGUAGES[0] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[0] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[1] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[1] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[2] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[2] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[3] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[3] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[4] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[4] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[5] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[5] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[6] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[6] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[7] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[7] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[8] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[8] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[9] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[9] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[10] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[10] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[11] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[11] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[12] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[12] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[13] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[13] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[14] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[14] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[15] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[15] + " INTEGER, ");
		sqlBuilder.append(LANGUAGES[16] + " STRING, ");
		sqlBuilder.append(LANGUAGE_PRIORITIES[16] + " INTEGER, ");
		sqlBuilder.append(QuestionContract.DIFFICULTY + " INTEGER,");
		sqlBuilder.append(QuestionContract.TIME_STEP + " INTEGER,");
		sqlBuilder.append(QuestionContract.TIME_STEPS + " INTEGER ");
		sqlBuilder.append(");");
		db.execSQL(sqlBuilder.toString());
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Do nothing

	}

	private void copyDatabase() {

		try {
			InputStream is = context.getAssets().open(DATABASE_NAME);
			File dest = new File(DATABASE_FULL_PATH);
			FileUtils.copyInputStreamToFile(is, dest);
		} catch (IOException e) {
			Loggy.e("Unable to populate database", e);
			return;
		}
	}

	private void upgradeDatabase() {
		File oldDest = new File(DATABASE_OLD_FULL_PATH);
		File dest = new File(DATABASE_FULL_PATH);
		try {
			InputStream is = context.getAssets().open(DATABASE_NAME);
			FileUtils.copyFile(dest, oldDest);
			try {
				FileUtils.copyInputStreamToFile(is, dest);
			} catch (IOException e) {
				Loggy.e("Unable to populate database", e);
			}
			getWritableDatabase().close();
		} catch (IOException e) {
			Loggy.e("Unable to populate database", e);
			return;
		}
		SQLiteDatabase oldDB = SQLiteDatabase.openDatabase(DATABASE_OLD_FULL_PATH, null, SQLiteDatabase.OPEN_READWRITE);
		SQLiteDatabase newDB = SQLiteDatabase.openDatabase(DATABASE_FULL_PATH, null, SQLiteDatabase.OPEN_READWRITE);
		// insert stats into new database
		Cursor cursor = oldDB.rawQuery("SELECT * FROM " + StatisticContract.TABLE_NAME, null);
		cursor.moveToFirst();
		CursorHelper cursorHelper = new CursorHelper(cursor);
		ContentValues values = new ContentValues();
		while (!cursor.isAfterLast()) {
			cursorHelper.setCursor(cursor);
			values.put(StatisticContract.PACKAGE, cursorHelper.getString(StatisticContract.PACKAGE));
			values.put(StatisticContract.CORRECT, cursorHelper.getString(StatisticContract.CORRECT));
			values.put(StatisticContract.DIFFICULTY, cursorHelper.getInteger(StatisticContract.DIFFICULTY));
			values.put(StatisticContract.TIME, cursorHelper.getLong(StatisticContract.TIME));
			if (DATABASE_OLD_VERSION > 1)
				values.put(StatisticContract.TIME, cursorHelper.getLong(StatisticContract.TIME2SOLVE));
			newDB.insert(StatisticContract.TABLE_NAME, null, values);
			cursor.moveToNext();
			values.clear();
		}

		// update priorities that have changed in the math table
		String where = MathQuestionContract.PRIORITY + " != " + QuestionContract.DEFAULT_PRIORITY;
		cursor = oldDB.query(MathQuestionContract.TABLE_NAME, MathQuestionContract.QUESTION_AND_PRIORITY, where, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			cursorHelper.setCursor(cursor);
			String question = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
			newDB.execSQL("UPDATE " + MathQuestionContract.TABLE_NAME + " SET " + MathQuestionContract.PRIORITY + "="
					+ cursorHelper.getInteger(QuestionContract.PRIORITY) + " WHERE " + QuestionContract.QUESTION_TEXT + "='"
					+ question.replaceAll("'", "''") + "'");
			cursor.moveToNext();
		}

		// update priorities that have changed in the vocab table
		where = VocabQuestionContract.PRIORITY + " != " + QuestionContract.DEFAULT_PRIORITY;
		cursor = oldDB.query(VocabQuestionContract.TABLE_NAME, VocabQuestionContract.QUESTION_AND_PRIORITY, where, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			cursorHelper.setCursor(cursor);
			String question = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
			newDB.execSQL("UPDATE " + VocabQuestionContract.TABLE_NAME + " SET " + VocabQuestionContract.PRIORITY + "="
					+ cursorHelper.getInteger(QuestionContract.PRIORITY) + " WHERE " + QuestionContract.QUESTION_TEXT + "='"
					+ question.replaceAll("'", "''") + "'");
			cursor.moveToNext();
		}

		// update priorities that have changed in the language table
		String[] priorities = LanguageQuestionContract.LANGUAGE_PRIORITIES;
		where = priorities[0] + " != " + QuestionContract.DEFAULT_PRIORITY;
		for (int a = 1; a < priorities.length; a++) {
			where = where + " OR " + priorities[a] + " != " + QuestionContract.DEFAULT_PRIORITY;
		}
		cursor = oldDB.query(LanguageQuestionContract.TABLE_NAME, LanguageQuestionContract.ID_AND_PRIORITY, where, null, null, null, null);
		cursor.moveToFirst();
		String set;
		while (!cursor.isAfterLast()) {
			cursorHelper.setCursor(cursor);
			set = priorities[0] + "=" + cursorHelper.getInteger(priorities[0]);
			for (int a = 1; a < priorities.length; a++) {
				set = set + ", " + priorities[a] + " = " + cursorHelper.getInteger(priorities[a]);
			}
			newDB.execSQL("UPDATE " + LanguageQuestionContract.TABLE_NAME + " SET " + set + " WHERE " + BaseContract._ID + "="
					+ cursorHelper.getLong(BaseContract._ID));
			cursor.moveToNext();
		}

		// update priorities that have changed in the engineering table
		where = EngineerQuestionContract.PRIORITY + " != " + QuestionContract.DEFAULT_PRIORITY;
		cursor = oldDB.query(EngineerQuestionContract.TABLE_NAME, EngineerQuestionContract.QUESTION_AND_PRIORITY, where, null, null, null,
				null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			cursorHelper.setCursor(cursor);
			String question = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
			newDB.execSQL("UPDATE " + EngineerQuestionContract.TABLE_NAME + " SET " + EngineerQuestionContract.PRIORITY + "="
					+ cursorHelper.getInteger(QuestionContract.PRIORITY) + " WHERE " + QuestionContract.QUESTION_TEXT + "='"
					+ question.replaceAll("'", "''") + "'");
			cursor.moveToNext();
		}

		// update priorities that have changed in the trivia table
		where = HiqTriviaQuestionContract.PRIORITY + " != " + QuestionContract.DEFAULT_PRIORITY;
		String triviaTableNameOld;
		if (DATABASE_OLD_VERSION == 1)
			triviaTableNameOld = HiqTriviaQuestionContract.TABLE_NAME1;
		else
			triviaTableNameOld = HiqTriviaQuestionContract.TABLE_NAME2;
		cursor = oldDB.query(triviaTableNameOld, HiqTriviaQuestionContract.QUESTION_AND_PRIORITY, where, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			cursorHelper.setCursor(cursor);
			String question = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
			newDB.execSQL("UPDATE " + HiqTriviaQuestionContract.TABLE_NAME2 + " SET " + HiqTriviaQuestionContract.PRIORITY + "="
					+ cursorHelper.getInteger(QuestionContract.PRIORITY) + " WHERE " + QuestionContract.QUESTION_TEXT + "='"
					+ question.replaceAll("'", "''") + "'");
			cursor.moveToNext();
		}

		// add custom question into the new database (if it existed before)
		if (DATABASE_OLD_VERSION > 1) {
			where = QuestionContract.DIFFICULTY + " >= " + 0;
			cursor = oldDB.query(CustomQuestionContract.TABLE_NAME, CustomQuestionContract.ALL_COLUMNS, where, null, null, null, null);
			cursor.moveToFirst();
			values.clear();
			while (!cursor.isAfterLast()) {
				cursorHelper.setCursor(cursor);
				values.put(QuestionContract.QUESTION_TEXT, cursorHelper.getString(QuestionContract.QUESTION_TEXT));
				values.put(QuestionContract.ANSWER_CORRECT, cursorHelper.getString(QuestionContract.ANSWER_CORRECT));
				values.put(CustomQuestionContract.ANSWER_INCORRECT1, cursorHelper.getString(CustomQuestionContract.ANSWER_INCORRECT1));
				values.put(CustomQuestionContract.ANSWER_INCORRECT2, cursorHelper.getString(CustomQuestionContract.ANSWER_INCORRECT2));
				values.put(CustomQuestionContract.ANSWER_INCORRECT3, cursorHelper.getString(CustomQuestionContract.ANSWER_INCORRECT3));
				values.put(QuestionContract.DIFFICULTY, cursorHelper.getInteger(QuestionContract.DIFFICULTY));
				values.put(QuestionContract.PRIORITY, cursorHelper.getInteger(QuestionContract.PRIORITY));
				values.put(QuestionContract.TIME_STEP, cursorHelper.getInteger(QuestionContract.TIME_STEP));
				values.put(QuestionContract.TIME_STEPS, cursorHelper.getInteger(QuestionContract.TIME_STEPS));
				values.put(CustomQuestionContract.CATEGORY, cursorHelper.getInteger(CustomQuestionContract.CATEGORY));
				newDB.insert(CustomQuestionContract.TABLE_NAME, null, values);
				cursor.moveToNext();
				values.clear();
			}
		}
		cursor.close();
		cursorHelper.destroy();
		oldDB.close();
		newDB.close();
		context.deleteDatabase(DATABASE_OLD_FULL_PATH);
	}

	private int dbState() {
		// 0=doesn't exist, 1=exists, 2=needs upgrade
		try {
			SQLiteDatabase database = SQLiteDatabase.openDatabase(DATABASE_FULL_PATH, null, SQLiteDatabase.OPEN_READONLY);
			if (database != null) {
				DATABASE_OLD_VERSION = database.getVersion();
				if (database.needUpgrade(DATABASE_VERSION)) {
					database.close();
					return 2;
				} else {
					database.close();
					return 1;
				}
			}
			DATABASE_OLD_VERSION = 0;
			return 0;
		} catch (SQLiteException e) {
			DATABASE_OLD_VERSION = 0;
			return 0;
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// upgrade is handled in upgradeDatabase()
	}

	public static DatabaseOpenHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseOpenHelper(context.getApplicationContext());
		}
		return instance;
	}
}
