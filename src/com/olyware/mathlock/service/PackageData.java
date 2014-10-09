package com.olyware.mathlock.service;

import org.json.JSONException;
import org.json.JSONObject;

import com.olyware.mathlock.utils.Loggy;

public class PackageData {

	private String pack;
	private long startTime;

	PackageData(String pack, long startTime) {
		this.pack = pack;
		this.startTime = startTime;
	}

	public String getPack() {
		return pack;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getTimeToOpen() {
		Loggy.d("getTimeToOpen = " + (System.currentTimeMillis() - startTime));
		return System.currentTimeMillis() - startTime;
	}

	public JSONObject getJSON() {
		JSONObject data = new JSONObject();
		try {
			data.put("pack", pack);
			data.put("startTime", startTime);
			return data;
		} catch (JSONException j) {
			return data;
		}
	}

	public static String getPackFromJSON(JSONObject json) {
		try {
			return json.getString("pack");
		} catch (JSONException e) {
			return "";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pack == null) ? 0 : pack.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PackageData other = (PackageData) obj;
		if (pack == null) {
			if (other.pack != null)
				return false;
		} else if (!pack.equals(other.pack))
			return false;
		return true;
	}

}
