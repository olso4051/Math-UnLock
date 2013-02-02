package com.olyware.mathlock.model;

public class LanguageQuestion extends Question {

	public LanguageQuestion(String text, String correctAnswer, Difficulty difficulty) {
		super(text, correctAnswer, difficulty);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LocationQuestion [Question=" + text + ",Answer=" + correctAnswer + ",difficulty=" + difficulty + "]";
	}
}