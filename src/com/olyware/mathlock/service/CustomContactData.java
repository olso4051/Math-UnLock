package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

import com.olyware.mathlock.utils.ContactHelper;

public class CustomContactData implements Comparable<CustomContactData> {
	private String name, description, hiqUserID, facebookUserID;
	private List<String> emails, phoneNumbers;
	private boolean isFriend, isContact;
	private int contact, section;

	public CustomContactData(int contact, String userID) {
		isContact = false;
		this.contact = contact;
		this.hiqUserID = userID;
	}

	public CustomContactData(String name, String email, String phoneNumber) {
		this.name = name;
		this.emails = new ArrayList<String>(1);
		this.emails.add(email);
		this.phoneNumbers = new ArrayList<String>(1);
		this.phoneNumbers.add(phoneNumber.replaceAll("[^\\d]", ""));
		isFriend = false;
		isContact = true;
		contact = -1;
		hiqUserID = "";
		facebookUserID = "";
	}

	public CustomContactData(String name, List<String> emails, List<String> phoneNumbers, boolean isFriend) {
		this.name = name;
		this.emails = new ArrayList<String>(emails.size());
		this.emails.addAll(emails);
		this.phoneNumbers = new ArrayList<String>(phoneNumbers.size());
		for (int i = 0; i < phoneNumbers.size(); i++) {
			this.phoneNumbers.add(phoneNumbers.get(i).replaceAll("[^\\d]", ""));
		}
		this.isFriend = isFriend;
		isContact = true;
		contact = -1;
		hiqUserID = "";
		facebookUserID = "";
	}

	public CustomContactData(String name, List<String> emails, List<String> phoneNumbers) {
		this.name = name;
		this.emails = new ArrayList<String>(emails.size());
		this.emails.addAll(emails);
		this.phoneNumbers = new ArrayList<String>(phoneNumbers.size());
		for (int i = 0; i < phoneNumbers.size(); i++) {
			this.phoneNumbers.add(phoneNumbers.get(i).replaceAll("[^\\d]", ""));
		}
		isFriend = false;
		isContact = true;
		contact = -1;
		hiqUserID = "";
		facebookUserID = "";
	}

	public CustomContactData(String facebookName, String facebookID) {
		this.name = facebookName;
		this.emails = new ArrayList<String>();
		this.phoneNumbers = new ArrayList<String>();
		isFriend = true;
		isContact = true;
		contact = -1;
		hiqUserID = "";
		facebookUserID = facebookID;
	}

	public CustomContactData(String title, String description, int section) {
		this.isContact = false;
		this.section = section;
		name = title;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getEmail() {
		if (emails.size() > 0)
			return emails.get(0);
		else
			return "";
	}

	public List<String> getEmails() {
		return emails;
	}

	public String getPhone() {
		if (phoneNumbers.size() > 0)
			return phoneNumbers.get(0);
		else
			return "";
	}

	public List<String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public String getHiqUserID() {
		return hiqUserID;
	}

	public String getFacebookUserID() {
		return facebookUserID;
	}

	public boolean isFriend() {
		return isFriend;
	}

	public boolean isContact() {
		return isContact;
	}

	public int getContact() {
		return contact;
	}

	public int getSection() {
		return section;
	}

	public List<String> getPhoneHashs() {
		return ContactHelper.getPhoneHashes(phoneNumbers);
	}

	public String getJSON() {
		String json = "{";
		json += "\"is_friend\":\"" + isFriend + "\",";
		json += "\"name\":\"" + name + "\",";
		json += "\"phone\":[";
		boolean first = true;
		if (phoneNumbers.size() > 0) {
			json += "\"";
			for (int i = 0; i < phoneNumbers.size(); i++) {
				if (first) {
					json += phoneNumbers.get(i) + "\"";
					first = false;
				} else
					json += ",\"" + phoneNumbers.get(i) + "\"";
			}
		}
		json += "],\"email\":[";
		first = true;
		if (emails.size() > 0) {
			json += "\"";
			for (int i = 0; i < emails.size(); i++) {
				if (first) {
					json += emails.get(i) + "\"";
					first = false;
				} else
					json += ",\"" + emails.get(i) + "\"";
			}
		}
		json += "]}";
		return json;
	}

	public void setIsFriend(boolean isFriend) {
		this.isFriend = isFriend;
	}

	public void setUserID(String userID) {
		this.hiqUserID = userID;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void addPhoneNumbers(List<String> phoneNumbers) {
		this.phoneNumbers.addAll(phoneNumbers);
	}

	public void addEmails(List<String> emails) {
		this.emails.addAll(emails);
	}

	@Override
	public int compareTo(CustomContactData arg0) {
		CustomContactData data = (CustomContactData) arg0;
		if (!isContact && !data.isContact) {
			if (section == data.section)
				return 0;
			else if (section < data.section)
				return -1;
			else
				return 1;
		}
		if (!isContact && section == 0) {
			return -1;
		} else if (!isContact && section == 1) {
			if (data.isFriend)
				return 1;
			else
				return -1;
		} else if (!data.isContact && data.section == 0) {
			return 1;
		} else if (!data.isContact && data.section == 1) {
			if (isFriend)
				return -1;
			else
				return 1;
		}
		int compareName = name.compareToIgnoreCase(data.name);
		if (isFriend == data.isFriend) {
			// if both are friends or not friends then compare by name
			if (compareName == 0) {
				// if both have same name ignoring case then return tie
				return 0;
			} else {
				// sort A-Z
				return compareName / Math.abs(compareName);
			}
		} else if (isFriend && !data.isFriend) {
			// put friends at top of list
			return -1;
		} else {
			// put non friends at bottom of list
			return 1;
		}
	}
}
