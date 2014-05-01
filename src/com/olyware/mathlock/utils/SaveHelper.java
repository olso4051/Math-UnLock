package com.olyware.mathlock.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class SaveHelper {

	public static boolean SaveTextFile(String txt) {
		if (isExternalStorageWritable()) {
			try {
				File file = getStorageDir("HiqGCMRegistration.txt");
				FileOutputStream stream = new FileOutputStream(file);
				Log.d("GAtest", "FileOutputStream");
				stream.write(txt.getBytes());
				Log.d("GAtest", "stream.write()");
				stream.close();
				Log.d("GAtest", "stream.close()");
			} catch (IOException e) {
				Log.d("GAtest", "File did not save" + e.toString());
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	/* Checks if external storage is available for read and write */
	private static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	private static File getStorageDir(String fileName) {
		// Get the directory for the user's public pictures directory.
		File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		if (!file.mkdirs()) {
			Log.e("GAtest", "Directory not created");
		}
		File file2 = new File(file.getAbsolutePath() + "/" + fileName);
		return file2;
	}

}
