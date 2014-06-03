package com.olyware.mathlock.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

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

	public static Bitmap loadBitmap(Context context, String picName, Bitmap compareBitmap) {
		Bitmap b = loadBitmap(context, picName);
		if (compare(b, compareBitmap))
			return compareBitmap;
		else
			return null;
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

	private static boolean compare(Bitmap b1, Bitmap b2) {
		if (b1.getWidth() == b2.getWidth() && b1.getHeight() == b2.getHeight()) {
			int[] pixels1 = new int[b1.getWidth() * b1.getHeight()];
			int[] pixels2 = new int[b2.getWidth() * b2.getHeight()];
			b1.getPixels(pixels1, 0, b1.getWidth(), 0, 0, b1.getWidth(), b1.getHeight());
			b2.getPixels(pixels2, 0, b2.getWidth(), 0, 0, b2.getWidth(), b2.getHeight());
			if (Arrays.equals(pixels1, pixels2)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
