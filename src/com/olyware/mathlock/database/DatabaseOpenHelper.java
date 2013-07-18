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
import android.util.Log;

import com.olyware.mathlock.database.contracts.BaseContract;
import com.olyware.mathlock.database.contracts.EngineerQuestionContract;
import com.olyware.mathlock.database.contracts.HiQHTriviaQuestionContract;
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

	private final Loggy log = new Loggy(this.getClass());

	private static final String DATABASE_NAME = "mathunlock.db";
	private static final int DATABASE_VERSION = 4;
	private static String DATABASE_PATH, DATABASE_FULL_PATH, DATABASE_OLD_FULL_PATH;

	private Context context;
	private static DatabaseOpenHelper instance = null;

	private DatabaseOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		DATABASE_FULL_PATH = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
		DATABASE_PATH = DATABASE_FULL_PATH.substring(0, DATABASE_FULL_PATH.indexOf(DATABASE_NAME));
		DATABASE_OLD_FULL_PATH = DATABASE_PATH + "old_" + DATABASE_NAME;
		int databaseState = dbState();
		Log.d("test", "state=" + databaseState);
		if (databaseState == 0)
			copyDatabase();
		else if (databaseState == 2)
			upgradeDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Do nothing
	}

	private void copyDatabase() {
		Log.d("test", "copying database");
		try {
			InputStream is = context.getAssets().open(DATABASE_NAME);
			File dest = new File(DATABASE_FULL_PATH);
			FileUtils.copyInputStreamToFile(is, dest);
		} catch (IOException e) {
			log.e("Unable to populate database", e);
			return;
		}
	}

	private void upgradeDatabase() {
		Log.d("test", "upgrading database");
		File oldDest = new File(DATABASE_OLD_FULL_PATH);
		File dest = new File(DATABASE_FULL_PATH);
		try {
			InputStream is = context.getAssets().open(DATABASE_NAME);
			FileUtils.copyFile(dest, oldDest);
			try {
				FileUtils.copyInputStreamToFile(is, dest);
			} catch (IOException e) {
				log.e("Unable to populate database", e);
			}
			getWritableDatabase().close();
		} catch (IOException e) {
			log.e("Unable to populate database", e);
			return;
		}
		SQLiteDatabase oldDB = SQLiteDatabase.openDatabase(DATABASE_OLD_FULL_PATH, null, SQLiteDatabase.OPEN_READWRITE);
		SQLiteDatabase newDB = SQLiteDatabase.openDatabase(DATABASE_FULL_PATH, null, SQLiteDatabase.OPEN_READWRITE);
		// insert stats into new database
		Cursor cursor = oldDB.rawQuery("SELECT * FROM " + StatisticContract.TABLE_NAME, null);
		cursor.moveToFirst();
		ContentValues values = new ContentValues();
		while (!cursor.isAfterLast()) {
			CursorHelper cursorHelper = new CursorHelper(cursor);
			values.put(StatisticContract.PACKAGE, cursorHelper.getString(StatisticContract.PACKAGE));
			values.put(StatisticContract.CORRECT, cursorHelper.getString(StatisticContract.CORRECT));
			values.put(StatisticContract.DIFFICULTY, cursorHelper.getInteger(StatisticContract.DIFFICULTY));
			values.put(StatisticContract.TIME, cursorHelper.getLong(StatisticContract.TIME));
			newDB.insert(StatisticContract.TABLE_NAME, null, values);
			cursor.moveToNext();
			values.clear();
		}
		// update priorities that have changed in the math table
		String where = MathQuestionContract.PRIORITY + " != " + QuestionContract.DEFAULT_PRIORITY;
		cursor = oldDB.query(MathQuestionContract.TABLE_NAME, MathQuestionContract.ID_AND_PRIORITY, where, null, null, null, null);
		cursor.moveToFirst();
		Log.d("test", "math entries = " + cursor.getCount());
		while (!cursor.isAfterLast()) {
			CursorHelper cursorHelper = new CursorHelper(cursor);
			newDB.execSQL("UPDATE " + MathQuestionContract.TABLE_NAME + " SET " + MathQuestionContract.PRIORITY + "="
					+ cursorHelper.getInteger(MathQuestionContract.PRIORITY) + " WHERE " + BaseContract._ID + "="
					+ cursorHelper.getInteger(MathQuestionContract._ID));
			cursor.moveToNext();
		}
		// update priorities that have changed in the vocab table
		where = VocabQuestionContract.PRIORITY + " != " + QuestionContract.DEFAULT_PRIORITY;
		cursor = oldDB.query(VocabQuestionContract.TABLE_NAME, VocabQuestionContract.ID_AND_PRIORITY, where, null, null, null, null);
		cursor.moveToFirst();
		Log.d("test", "vocab entries = " + cursor.getCount());
		while (!cursor.isAfterLast()) {
			CursorHelper cursorHelper = new CursorHelper(cursor);
			newDB.execSQL("UPDATE " + VocabQuestionContract.TABLE_NAME + " SET " + VocabQuestionContract.PRIORITY + "="
					+ cursorHelper.getInteger(VocabQuestionContract.PRIORITY) + " WHERE " + BaseContract._ID + "="
					+ cursorHelper.getInteger(VocabQuestionContract._ID));
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
		Log.d("test", "Language entries = " + cursor.getCount());
		String set;
		while (!cursor.isAfterLast()) {
			CursorHelper cursorHelper = new CursorHelper(cursor);
			set = priorities[0] + "=" + cursorHelper.getInteger(priorities[0]);
			for (int a = 1; a < priorities.length; a++) {
				set = set + ", " + priorities[a] + " = " + cursorHelper.getInteger(priorities[a]);
			}
			newDB.execSQL("UPDATE " + LanguageQuestionContract.TABLE_NAME + " SET " + set + " WHERE " + BaseContract._ID + "="
					+ cursorHelper.getInteger(LanguageQuestionContract._ID));
			cursor.moveToNext();
		}
		// update priorities that have changed in the engineering table
		where = EngineerQuestionContract.PRIORITY + " != " + QuestionContract.DEFAULT_PRIORITY;
		cursor = oldDB.query(EngineerQuestionContract.TABLE_NAME, EngineerQuestionContract.ID_AND_PRIORITY, where, null, null, null, null);
		cursor.moveToFirst();
		Log.d("test", "Engineer entries = " + cursor.getCount());
		while (!cursor.isAfterLast()) {
			CursorHelper cursorHelper = new CursorHelper(cursor);
			newDB.execSQL("UPDATE " + EngineerQuestionContract.TABLE_NAME + " SET " + EngineerQuestionContract.PRIORITY + "="
					+ cursorHelper.getInteger(EngineerQuestionContract.PRIORITY) + " WHERE " + BaseContract._ID + "="
					+ cursorHelper.getInteger(EngineerQuestionContract._ID));
			cursor.moveToNext();
		}
		// update priorities that have changed in the engineering table
		where = HiQHTriviaQuestionContract.PRIORITY + " != " + QuestionContract.DEFAULT_PRIORITY;
		cursor = oldDB.query(HiQHTriviaQuestionContract.TABLE_NAME, HiQHTriviaQuestionContract.ID_AND_PRIORITY, where, null, null, null,
				null);
		cursor.moveToFirst();
		Log.d("test", "HiQHTrivia entries = " + cursor.getCount());
		while (!cursor.isAfterLast()) {
			CursorHelper cursorHelper = new CursorHelper(cursor);
			newDB.execSQL("UPDATE " + HiQHTriviaQuestionContract.TABLE_NAME + " SET " + HiQHTriviaQuestionContract.PRIORITY + "="
					+ cursorHelper.getInteger(HiQHTriviaQuestionContract.PRIORITY) + " WHERE " + BaseContract._ID + "="
					+ cursorHelper.getInteger(HiQHTriviaQuestionContract._ID));
			cursor.moveToNext();
		}
		context.deleteDatabase(DATABASE_OLD_FULL_PATH);
	}

	private int dbState() {
		// 0=doesn't exist, 1=exists, 2=needs upgrade
		try {
			SQLiteDatabase database = SQLiteDatabase.openDatabase(DATABASE_FULL_PATH, null, SQLiteDatabase.OPEN_READONLY);
			if (database != null) {
				if (database.needUpgrade(DATABASE_VERSION)) {
					Log.d("test", "database needs update");
					database.close();
					return 2;
				} else {
					Log.d("test", "database exists");
					database.close();
					return 1;
				}
			}
			return 0;
		} catch (SQLiteException e) {
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
