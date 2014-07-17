package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

import com.olyware.mathlock.utils.ContactHelper;

public class CustomContactData implements Comparable<CustomContactData> {
	private String name, description, hiqUserID, hiqUserName, facebookUserID;
	private List<String> emails, phoneNumbers;
	private boolean isFriend, isContact;
	private int contact, section;

	public CustomContactData() {
		this.name = "";
		this.emails = new ArrayList<String>();
		this.phoneNumbers = new ArrayList<String>();
		isFriend = false;
		isContact = false;
		contact = -1;
		section = -1;
		hiqUserID = "";
		hiqUserName = "";
		facebookUserID = "";
	}

	public CustomContactData(int contact, String userID, String hiqUserName) {
		isContact = false;
		this.contact = contact;
		this.hiqUserID = userID;
		this.hiqUserName = hiqUserName;
	}

	public CustomContactData(String name, String email, String phoneNumber) {
		this.name = name;
		this.emails = new ArrayList<String>(1);
		this.emails.add(email);
		this.phoneNumbers = new ArrayList<String>(1);
		this.phoneNumbers.add(ContactHelper.getPhoneNumberFromString(phoneNumber));
		isFriend = false;
		isContact = true;
		contact = -1;
		section = -1;
		hiqUserID = "";
		hiqUserName = "";
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
		section = -1;
		hiqUserID = "";
		hiqUserName = "";
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
		section = -1;
		hiqUserID = "";
		hiqUserName = "";
		facebookUserID = "";
	}

	public CustomContactData(String facebookName, String facebookID) {
		this.name = facebookName;
		this.emails = new ArrayList<String>();
		this.phoneNumbers = new ArrayList<String>();
		isFriend = true;
		isContact = true;
		contact = -1;
		section = -1;
		hiqUserID = "";
		hiqUserName = "";
		facebookUserID = facebookID;
	}

	public CustomContactData(String title, String description, int section) {
		this.emails = new ArrayList<String>();
		this.phoneNumbers = new ArrayList<String>();
		this.isFriend = false;
		this.isContact = false;
		this.section = section;
		name = title;
		this.description = description;
		contact = -1;
		hiqUserID = "";
		hiqUserName = "";
		facebookUserID = "";
	}

	public String getName() {
		return hiqUserName.equals("") ? name : hiqUserName;
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

	public boolean hasHiqUserID() {
		return !hiqUserID.equals("");
	}

	public String getFacebookHash() {
		return facebookUserID;
	}

	public boolean isFriend() {
		return (isFriend && !hiqUserID.equals(""));
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

	public String getScore() {
		return "0";
	}

	public List<String> getPhoneHashs() {
		return ContactHelper.getPhoneHashes(phoneNumbers);
	}

	public String getJSON() {
		String json = "{";
		json += "\"" + ContactHelper.CONTACT_IS_FRIEND + "\":\"" + isFriend() + "\",";
		json += "\"" + ContactHelper.CONTACT_USER_ID + "\":\"" + hiqUserID + "\",";
		json += "\"" + ContactHelper.CONTACT_NAME + "\":\"" + name + "\",";
		json += "\"" + ContactHelper.CONTACT_PHONE + "\":[";
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
		json += "],\"" + ContactHelper.CONTACT_EMAIL + "\":[";
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

	public void setHiqUserName(String hiqUserName) {
		this.hiqUserName = hiqUserName;
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

	public void addEmail(String email) {
		emails.add(email);
	}

	public void addEmails(List<String> emails) {
		this.emails.addAll(emails);
	}

	@Override
	public String toString() {
		String s = "name:" + name + ",email:[";
		if (emails != null) {
			for (String email : emails)
				s += email + ",";
		}
		s += "],phone:[";
		if (phoneNumbers != null) {
			for (String phone : phoneNumbers)
				s += phone + ",";
		}
		s += "]";
		return s;
	}

	@Override
	public int compareTo(CustomContactData data) {
		// CustomContactData data = (CustomContactData) arg0;
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
			if (data.isFriend())
				return 1;
			else
				return -1;
		} else if (!data.isContact && data.section == 0) {
			return 1;
		} else if (!data.isContact && data.section == 1) {
			if (isFriend())
				return -1;
			else
				return 1;
		}
		int compareName = name.compareToIgnoreCase(data.name);
		if (isFriend() == data.isFriend()) {
			// if both are friends or not friends then compare by name
			if (compareName == 0) {
				// if both have same name ignoring case then return tie
				return 0;
			} else {
				// sort A-Z
				return compareName / Math.abs(compareName);
			}
		} else if (isFriend() && !data.isFriend()) {
			// put friends at top of list
			return -1;
		} else {
			// put non friends at bottom of list
			return 1;
		}
	}
}
