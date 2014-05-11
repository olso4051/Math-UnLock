package com.olyware.mathlock.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class SaveHelper {

	public static boolean SaveBitmapPrivate(Context context, Bitmap b, String picName) {
		FileOutputStream fos;
		try {
			fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
			b.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();
			return true;
		} catch (FileNotFoundException e) {
			Log.d("GAtest", "file not found");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			Log.d("GAtest", "io exception");
			e.printStackTrace();
			return false;
		}
	}

	public static Bitmap loadBitmap(Context context, String picName) {
		Bitmap b = null;
		FileInputStream fis;
		try {
			fis = context.openFileInput(picName);
			b = BitmapFactory.decodeStream(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			Log.d("GAtest", "file not found");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("GAtest", "io exception");
			e.printStackTrace();
		}
		return b;
	}

	public static boolean SaveTextFilePublic(String txt) {
		if (isExternalStorageWritable()) {
			try {
				File file = getStorageDir("HiqGCMRegistration.txt");
				FileOutputStream stream = new FileOutputStream(file);
				stream.write(txt.getBytes());
				stream.close();
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
