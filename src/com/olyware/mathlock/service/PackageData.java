package com.olyware.mathlock.service;

public class PackageData {

	private String pack;
	private long startTime;

	PackageData(String pack, long startTime) {
		this.pack = pack;
		this.startTime = startTime;
	}

	public String getPack() {
		return pack;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getTime(long doneTime) {
		return doneTime - startTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pack == null) ? 0 : pack.hashCode());
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
		PackageData other = (PackageData) obj;
		if (pack == null) {
			if (other.pack != null)
				return false;
		} else if (!pack.equals(other.pack))
			return false;
		return true;
	}

}
