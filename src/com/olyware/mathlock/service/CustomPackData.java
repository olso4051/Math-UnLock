package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

public class CustomPackData {
	private String filename, name, ID, userID, downloads;
	private List<String> tags;

	public CustomPackData(String filename, String name, String ID, String userID, String downloads, List<String> tags) {
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

	public String getDownloads() {
		if (downloads == null)
			return "0";
		else if (downloads.equals(""))
			return "0";
		else
			return downloads;
	}

	public List<String> getTags() {
		if (tags == null)
			return new ArrayList<String>(0);
		else
			return tags;
	}
}
