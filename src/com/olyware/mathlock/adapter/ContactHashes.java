package com.olyware.mathlock.adapter;

public class ContactHashes {

	private String phoneHash, facebookHash, hiqUserHash, hiqUserName;

	public ContactHashes(String phoneHash, String facebookHash, String hiqUserHash, String hiqUserName) {
		this.phoneHash = phoneHash;
		this.facebookHash = facebookHash;
		this.hiqUserHash = hiqUserHash;
		this.hiqUserName = hiqUserName;
	}

	public String getPhoneHash() {
		return phoneHash;
	}

	public String getFacebookHash() {
		return facebookHash;
	}

	public String getHiqUserHash() {
		return hiqUserHash;
	}

	public String getHiqUserName() {
		return hiqUserName;
	}
}
