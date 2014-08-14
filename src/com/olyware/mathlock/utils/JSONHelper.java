package com.olyware.mathlock.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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

	public static String getStringFromJSON2(JSONObject json, String key1, String key2) {
		try {
			JSONObject jsonKey1 = json.getJSONObject(key1);
			return getStringFromJSON(jsonKey1, key2);
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

	public static List<String> getStringListFromJSONArray(JSONArray array) {
		List<String> list = new ArrayList<String>();
		try {
			for (int i = 0; i < array.length(); i++) {
				list.add(URLDecoder.decode(array.getString(i), "utf-8"));
			}
		} catch (JSONException e) {
			return new ArrayList<String>();
		} catch (UnsupportedEncodingException e) {
			return new ArrayList<String>();
		}
		return list;
	}

	public static String encodeJSON(String s) {
		try {
			return URLEncoder.encode(s, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}

}
