package com.olyware.mathlock.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.olyware.mathlock.utils.Loggy;

/**
 * This creates/opens the actual SQLite database file.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

	private final Loggy log = new Loggy(this.getClass());

	private static final String DATABASE_PATH = "/data/data/com.olyware.mathlock/databases/";
	private static final String DATABASE_NAME = "mathunlock.db";
	private static final String DATABASE_FULL_PATH = DATABASE_PATH + DATABASE_NAME;
	private static final int DATABASE_VERSION = 1;

	private Context context;
	private static DatabaseOpenHelper instance = null;

	private DatabaseOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		copyDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Do nothing
	}

	private void copyDatabase() {
		if (!dbExists()) {
			InputStream is;
			try {
				is = context.getAssets().open(DATABASE_NAME);
				File dest = new File(DATABASE_FULL_PATH);
				FileUtils.copyInputStreamToFile(is, dest);
			} catch (IOException e) {
				log.e("Unable to populate database", e);
				return;
			}
		}
	}

	private boolean dbExists() {
		try {
			SQLiteDatabase database = SQLiteDatabase.openDatabase(DATABASE_FULL_PATH, null, SQLiteDatabase.OPEN_READONLY);
			if (database != null) {
				database.close();
			}
			return database != null;
		} catch (SQLiteException e) {
			return false;
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Nothing to do here until our database schema changes
	}

	public static DatabaseOpenHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseOpenHelper(context.getApplicationContext());
		}
		return instance;
	}
}
