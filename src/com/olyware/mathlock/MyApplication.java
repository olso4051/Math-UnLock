package com.olyware.mathlock;

import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", // will not be used
mailTo = "olso4051@umn.edu", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text)
public class MyApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		// The following line triggers the initialization of ACRA
		// ACRA.init(this);
	}
}