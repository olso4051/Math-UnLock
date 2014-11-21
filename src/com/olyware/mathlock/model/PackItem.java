package com.olyware.mathlock.model;

public class PackItem {

	private String textToShow;
	private String key_preference;
	private String title;
	private String Summury;
	private boolean isPurchased;

	public String getTextToShow() {
		return textToShow;
	}

	public void setTextToShow(String textToShow) {
		this.textToShow = textToShow;
	}

	public String getKey_preference() {
		return key_preference;
	}

	public void setKey_preference(String key_preference) {
		this.key_preference = key_preference;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummury() {
		return Summury;
	}

	public void setSummury(String summury) {
		Summury = summury;
	}

	public boolean isPurchased() {
		return isPurchased;
	}

	public void setPurchased(boolean isPurchased) {
		this.isPurchased = isPurchased;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	private boolean isEnabled;

}
