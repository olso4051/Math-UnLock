package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

public class CustomPackData implements Comparable<CustomPackData> {
	private String filename, name, ID, userID;
	private int downloads;
	private List<String> tags;

	public CustomPackData(String filename, String name, String ID, String userID, int downloads, List<String> tags) {
		this.filename = filename;
		// this.name = name;
		int end = filename.indexOf('.');
		if (end > 0)
			this.name = filename.substring(0, end);
		else
			this.name = filename;
		this.ID = ID;
		this.userID = userID;
		this.downloads = downloads;
		this.tags = new ArrayList<String>(tags.size());
		this.tags.addAll(tags);
	}

	public String getFilename() {
		return filename;
	}

	public String getName() {
		return name;
	}

	public String getID() {
		return ID;
	}

	public String getUserID() {
		return userID;
	}

	public int getDownloads() {
		return downloads;
	}

	public List<String> getTags() {
		if (tags == null)
			return new ArrayList<String>(0);
		else
			return tags;
	}

	@Override
	public int compareTo(CustomPackData data) {
		int compareName = getName().compareToIgnoreCase(data.getName());
		if (compareName == 0) {
			// if both have same name ignoring case then return tie
			return 0;
		} else {
			// sort A-Z
			return compareName / Math.abs(compareName);
		}

	}
}
