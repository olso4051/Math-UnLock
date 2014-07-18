package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {
	public static String getStringFromMessage(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException e) {
			return "";
		}
	}

	public static List<String> getStringListFromMessage(JSONObject json, String key) {
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

	public static List<String[]> getStringArrayListFromMessage(JSONObject json, String key) {
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

	public static int getIntFromMessage(JSONObject json, String key) {
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
