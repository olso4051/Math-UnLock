package com.olyware.mathlock.utils;

import android.util.Log;

public class Loggy {
	private static String tag = "test";
	private static boolean logging = true;

	public static void i(String msg) {
		if (logging)
			Log.i(tag, msg);
	}

	public static void i(String msg, Throwable exception) {
		if (logging)
			Log.i(tag, msg, exception);
	}

	public static void v(String msg) {
		if (logging)
			Log.v(tag, msg);
	}

	public static void v(String msg, Throwable exception) {
		if (logging)
			Log.v(tag, msg, exception);
	}

	public static void d(String msg) {
		if (logging)
			Log.d(tag, msg);
	}

	public static void d(String tag, String msg) {
		if (logging)
			Log.d(tag, msg);
	}

	public static void d(String msg, Throwable exception) {
		if (logging)
			Log.d(tag, msg, exception);
	}

	public static void w(String msg) {
		if (logging)
			Log.w(tag, msg);
	}

	public static void w(String tag, String msg) {
		if (logging)
			Log.w(tag, msg);
	}

	public static void w(String msg, Throwable exception) {
		if (logging)
			Log.w(tag, msg, exception);
	}

	public static void e(String msg) {
		if (logging)
			Log.e(tag, msg);
	}

	public static void e(String tag, String msg) {
		if (logging)
			Log.e(tag, msg);
	}

	public static void e(String msg, Throwable exception) {
		if (logging)
			Log.e(tag, msg, exception);
	}
}
