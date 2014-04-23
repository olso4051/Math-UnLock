package com.olyware.mathlock.database;

import android.database.Cursor;

public class CursorHelper {

	Cursor cursor;

	public CursorHelper(Cursor cursor) {
		this.cursor = cursor;
	}

	public void setCursor(Cursor cursor) {
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

	public boolean getBoolean(String columnName) {
		String s = getString(columnName);
		if (Boolean.parseBoolean(s) || s.equals("1"))
			return true;
		else
			return false;
	}

	public void destroy() {
		cursor.close();
	}
}