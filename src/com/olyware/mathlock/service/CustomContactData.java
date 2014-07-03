package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.List;

import com.olyware.mathlock.utils.EncryptionHelper;

public class CustomContactData implements Comparable<CustomContactData> {
	private String name;
	private List<String> emails, phoneNumbers;

	public CustomContactData(String name, String email, String phoneNumber) {
		this.name = name;
		this.emails = new ArrayList<String>(1);
		this.emails.add(email);
		this.phoneNumbers = new ArrayList<String>(1);
		this.phoneNumbers.add(phoneNumber.replaceAll("[^\\d]", ""));
	}

	public CustomContactData(String name, List<String> emails, List<String> phoneNumbers) {
		this.name = name;
		this.emails = new ArrayList<String>(emails.size());
		this.emails.addAll(emails);
		this.phoneNumbers = new ArrayList<String>(phoneNumbers.size());
		for (int i = 0; i < phoneNumbers.size(); i++) {
			this.phoneNumbers.add(phoneNumbers.get(i).replaceAll("[^\\d]", ""));
		}
	}

	public String getName() {
		return name;
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

	public List<String> getPhoneHashs() {
		List<String> phoneNumberEncrypted = new ArrayList<String>(phoneNumbers.size());
		if (phoneNumbers.size() > 0) {
			for (int i = 0; i < phoneNumbers.size(); i++) {
				phoneNumberEncrypted.add(new EncryptionHelper().encryptForURL(phoneNumbers.get(i)));
			}
		}
		return phoneNumberEncrypted;
	}

	public String getJSON() {
		String json = "{";
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

	public void addPhoneNumbers(List<String> phoneNumbers) {
		this.phoneNumbers.addAll(phoneNumbers);
	}

	public void addEmails(List<String> emails) {
		this.emails.addAll(emails);
	}

	@Override
	public int compareTo(CustomContactData arg0) {
		CustomContactData data = (CustomContactData) arg0;
		int compare = name.compareToIgnoreCase(data.name);
		if (compare == 0)
			return 0;
		else
			return compare / Math.abs(compare);
	}
}
