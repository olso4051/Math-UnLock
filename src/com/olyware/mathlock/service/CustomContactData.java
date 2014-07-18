package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

import com.olyware.mathlock.utils.ContactHelper;

public class CustomContactData implements Comparable<CustomContactData> {
	final private static String FacebookContact = "Facebook";
	final private static String PhoneContact = "Phonebook";
	private String name, description, hiqUserID, hiqUserName, facebookUserID, facebookName;
	private List<String> emails, phoneNumbers;
	private boolean isFriend, isContact;
	private int section, contact;
	private List<Integer> contacts;

	public CustomContactData() {
		this.name = "";
		this.emails = new ArrayList<String>();
		this.phoneNumbers = new ArrayList<String>();
		this.contacts = new ArrayList<Integer>();
		isFriend = false;
		isContact = false;
		section = -1;
		contact = -1;
		hiqUserID = "";
		hiqUserName = "";
		facebookUserID = "";
		facebookName = "";
		this.description = "";
	}

	public CustomContactData(int contact, String userID, String hiqUserName) {
		isContact = false;
		this.contacts = new ArrayList<Integer>();
		this.contact = contact;
		this.hiqUserID = userID;
		this.hiqUserName = hiqUserName;
		this.name = "";
		this.emails = new ArrayList<String>();
		this.phoneNumbers = new ArrayList<String>();
		isFriend = false;
		section = -1;
		facebookUserID = "";
		facebookName = "";
		this.description = "";
	}

	public CustomContactData(List<Integer> contacts, String userID, String hiqUserName) {
		isContact = false;
		this.contacts = new ArrayList<Integer>();
		this.contacts.addAll(contacts);
		this.hiqUserID = userID;
		this.hiqUserName = hiqUserName;
		this.name = "";
		this.emails = new ArrayList<String>();
		this.phoneNumbers = new ArrayList<String>();
		isFriend = false;
		section = -1;
		contact = -1;
		facebookUserID = "";
		facebookName = "";
		this.description = "";
	}

	public CustomContactData(String name, String email, String phoneNumber) {
		this.name = name;
		this.emails = new ArrayList<String>(1);
		this.emails.add(email);
		this.phoneNumbers = new ArrayList<String>(1);
		this.phoneNumbers.add(ContactHelper.getPhoneNumberFromString(phoneNumber));
		isFriend = false;
		isContact = true;
		contacts = new ArrayList<Integer>();
		section = -1;
		contact = -1;
		hiqUserID = "";
		hiqUserName = "";
		facebookUserID = "";
		facebookName = "";
		this.description = "";
	}

	public CustomContactData(String hiqUserID, String hiqUserName, String facebookUserID, String facebookName, String name,
			List<String> emails, List<String> phoneNumbers, boolean isFriend) {
		this.name = name;
		this.emails = new ArrayList<String>(emails.size());
		this.emails.addAll(emails);
		this.phoneNumbers = new ArrayList<String>(phoneNumbers.size());
		this.phoneNumbers.addAll(ContactHelper.getPhoneNumbersFromStrings(phoneNumbers));
		this.isFriend = isFriend;
		this.hiqUserID = hiqUserID;
		this.hiqUserName = hiqUserName;
		this.facebookUserID = facebookUserID;
		this.facebookName = facebookName;
		isContact = true;
		contacts = new ArrayList<Integer>();
		section = -1;
		contact = -1;
		this.description = "";
	}

	public CustomContactData(String name, List<String> emails, List<String> phoneNumbers) {
		this.name = name;
		this.emails = new ArrayList<String>(emails.size());
		this.emails.addAll(emails);
		this.phoneNumbers = new ArrayList<String>(phoneNumbers.size());
		this.phoneNumbers.addAll(ContactHelper.getPhoneNumbersFromStrings(phoneNumbers));
		isFriend = false;
		isContact = true;
		contacts = new ArrayList<Integer>();
		section = -1;
		contact = -1;
		hiqUserID = "";
		hiqUserName = "";
		facebookUserID = "";
		facebookName = "";
		this.description = "";
	}

	public CustomContactData(String facebookName, String facebookID) {
		this.name = "";
		this.emails = new ArrayList<String>();
		this.phoneNumbers = new ArrayList<String>();
		isFriend = true;
		isContact = true;
		contacts = new ArrayList<Integer>();
		section = -1;
		contact = -1;
		hiqUserID = "";
		hiqUserName = "";
		facebookUserID = facebookID;
		this.facebookName = facebookName;
		this.description = "";
	}

	public CustomContactData(String title, String description, int section) {
		this.emails = new ArrayList<String>();
		this.phoneNumbers = new ArrayList<String>();
		this.isFriend = false;
		this.isContact = false;
		this.section = section;
		name = title;
		this.description = description;
		contacts = new ArrayList<Integer>();
		hiqUserID = "";
		hiqUserName = "";
		facebookUserID = "";
		facebookName = "";
		contact = -1;
	}

	public void mergeWith(CustomContactData data) {
		if (name.equals(""))
			name = data.getName();
		if (hiqUserID.equals(""))
			hiqUserID = data.getHiqUserID();
		if (hiqUserName.equals(""))
			hiqUserName = data.getHiqUserName();
		if (facebookUserID.equals(""))
			facebookUserID = data.getFacebookHash();
		if (facebookName.equals(""))
			facebookName = data.getFacebookName();
		phoneNumbers.addAll(data.getPhoneNumbers());
		emails.addAll(data.getEmails());
	}

	public String getName() {
		return name;
	}

	public String getHiqUserName() {
		return hiqUserName;
	}

	public String getFacebookName() {
		return facebookName;
	}

	public String getDisplayName() {
		return facebookName.equals("") ? (hiqUserName.equals("") ? name : hiqUserName) : facebookName;
	}

	public String getDisplayDescription() {
		String display = getDisplayContact();
		String desc = "";
		if (hiqUserName.equals("")) {
			return PhoneContact + " - " + display;
		}
		if (!facebookName.equals("")) {
			desc = FacebookContact;
		}
		if (!display.equals("")) {
			if (desc.equals(""))
				desc = PhoneContact;
			else
				desc += ", " + PhoneContact;
		}
		return desc;
	}

	public String getDisplayContact() {
		String display = getPhoneNumber();
		if (display.equals("")) {
			display = getEmail();
		}
		return display;
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

	public String getPhoneNumber() {
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

	public List<Integer> getContacts() {
		return contacts;
	}

	public int getSection() {
		return section;
	}

	public String getScore() {
		return "";
	}

	public List<String> getPhoneHashs() {
		return ContactHelper.getPhoneHashes(phoneNumbers);
	}

	public String getJSON() {
		String json = "{";
		json += "\"" + ContactHelper.CONTACT_IS_FRIEND + "\":\"" + isFriend + "\",";
		json += "\"" + ContactHelper.CONTACT_HIQ_USER_ID + "\":\"" + hiqUserID + "\",";
		json += "\"" + ContactHelper.CONTACT_HIQ_NAME + "\":\"" + hiqUserName + "\",";
		json += "\"" + ContactHelper.CONTACT_FACEBOOK_USER_ID + "\":\"" + facebookUserID + "\",";
		json += "\"" + ContactHelper.CONTACT_FACEBOOK_NAME + "\":\"" + facebookName + "\",";
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
		int compareName = getDisplayName().compareToIgnoreCase(data.getDisplayName());
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
