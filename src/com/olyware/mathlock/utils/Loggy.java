package com.olyware.mathlock.utils;

import android.util.Log;

public class Loggy {
	String tag;

	public Loggy(Class<?> clazz) {
		this.tag = clazz.getName();
	}

	public void i(String msg) {
		Log.i(tag, msg);
	}

	public void i(String msg, Throwable exception) {
		Log.i(tag, msg, exception);
	}

	public void v(String msg) {
		Log.v(tag, msg);
	}

	public void v(String msg, Throwable exception) {
		Log.v(tag, msg, exception);
	}

	public void d(String msg) {
		Log.d(tag, msg);
	}

	public void d(String msg, Throwable exception) {
		Log.d(tag, msg, exception);
	}

	public void w(String msg) {
		Log.w(tag, msg);
	}

	public void w(String msg, Throwable exception) {
		Log.w(tag, msg, exception);
	}

	public void e(String msg) {
		Log.e(tag, msg);
	}

	public void e(String msg, Throwable exception) {
		Log.e(tag, msg, exception);
	}
}
