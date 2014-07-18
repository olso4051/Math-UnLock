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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((facebookHash == null) ? 0 : facebookHash.hashCode());
		result = prime * result + ((hiqUserHash == null) ? 0 : hiqUserHash.hashCode());
		result = prime * result + ((hiqUserName == null) ? 0 : hiqUserName.hashCode());
		result = prime * result + ((phoneHash == null) ? 0 : phoneHash.hashCode());
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
		ContactHashes other = (ContactHashes) obj;
		if (facebookHash == null) {
			if (other.facebookHash != null)
				return false;
		} else if (!facebookHash.equals(other.facebookHash))
			return false;
		if (hiqUserHash == null) {
			if (other.hiqUserHash != null)
				return false;
		} else if (!hiqUserHash.equals(other.hiqUserHash))
			return false;
		if (hiqUserName == null) {
			if (other.hiqUserName != null)
				return false;
		} else if (!hiqUserName.equals(other.hiqUserName))
			return false;
		if (phoneHash == null) {
			if (other.phoneHash != null)
				return false;
		} else if (!phoneHash.equals(other.phoneHash))
			return false;
		return true;
	}

}
