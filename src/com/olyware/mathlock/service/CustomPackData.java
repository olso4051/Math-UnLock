package com.olyware.mathlock.service;

public class CustomPackData {
	private String filename, name, ID, userID;

	public CustomPackData(String filename, String ID, String userID) {
		this.filename = filename;
		int end = filename.indexOf('.');
		if (end > 0)
			name = filename.substring(0, end);
		else
			name = filename;
		this.ID = ID;
		this.userID = userID;
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
}
