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

import com.olyware.mathlock.database.contracts.StatisticContract;
import com.olyware.mathlock.utils.Loggy;

/**
 * This creates/opens the actual SQLite database file.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

	private final Loggy log = new Loggy(this.getClass());

	private static final String DATABASE_NAME = "mathunlock.db";
	private static final int DATABASE_VERSION = 1;
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
		Cursor cursor = oldDB.rawQuery("SELECT * FROM " + StatisticContract.TABLE_NAME, null);
		cursor.moveToFirst();
		Log.d("test", "stats=" + cursor.getCount());
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
