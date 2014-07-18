package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {
	public static String getStringFromJSON(JSONObject json, String key) {
		try {
			String s = json.getString(key);
			if (s.toLowerCase(Locale.ENGLISH).equals("null"))
				return "";
			else
				return s;
		} catch (JSONException e) {
			return "";
		}
	}

	public static List<String> getStringListFromJSON(JSONObject json, String key) {
		try {
			JSONArray array = json.getJSONArray(key);
			List<String> result = new ArrayList<String>(array.length());
			for (int i = 0; i < array.length(); i++) {
				result.add(array.getString(i));
			}
			return result;
		} catch (JSONException e) {
			return new ArrayList<String>();
		}
	}

	public static List<String[]> getStringArrayListFromJSON(JSONObject json, String key) {
		try {
			JSONArray array = json.getJSONArray(key);
			List<String[]> result = new ArrayList<String[]>(array.length());
			for (int i = 0; i < array.length(); i++) {
				JSONArray innerArray = array.getJSONArray(i);
				String[] results = new String[innerArray.length()];
				for (int j = 0; j < innerArray.length(); j++) {
					results[j] = innerArray.getString(j);
				}
				result.add(results);
			}
			return result;
		} catch (JSONException e) {
			return new ArrayList<String[]>();
		}
	}

	public static int getIntFromJSON(JSONObject json, String key) {
		try {
			int value = Integer.parseInt(json.getString(key));
			return value;
		} catch (JSONException e) {
			return 0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
