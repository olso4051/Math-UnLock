package com.olyware.mathlock.model;

public class LanguageQuestion extends Question {

	public LanguageQuestion(int id, String text, String correctAnswer, Difficulty difficulty, int priority) {
		super(id, text, correctAnswer, difficulty, priority);
	}

	@Override
	public String toString() {
		return "LanguageQuestion [fromLanguage=" + text + ",toLanguage=" + correctAnswer + ",difficulty=" + difficulty + "]";
	}
}