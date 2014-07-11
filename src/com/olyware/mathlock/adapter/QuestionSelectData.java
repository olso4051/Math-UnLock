package com.olyware.mathlock.adapter;

public class QuestionSelectData {
	private String name;
	private boolean checked;
	private int ID;

	public QuestionSelectData(String name, boolean checked, int ID) {
		this.name = name;
		this.checked = checked;
		this.ID = ID;
	}

	public String getName() {
		return name;
	}

	public boolean isChecked() {
		return checked;
	}

	public int getID() {
		return ID;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void setID(int ID) {
		this.ID = ID;
	}
}
