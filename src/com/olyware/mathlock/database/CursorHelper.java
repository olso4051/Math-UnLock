package com.olyware.mathlock.database;

import android.database.Cursor;

public class CursorHelper {

	Cursor cursor;

	public CursorHelper(Cursor cursor) {
		this.cursor = cursor;
	}

	public String getString(String columnName) {
		return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
	}

	public long getLong(String columnName) {
		return cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
	}

	public int getInteger(String columnName) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
	}
}