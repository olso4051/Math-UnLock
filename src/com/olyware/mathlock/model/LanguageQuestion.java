package com.olyware.mathlock.model;

public class LanguageQuestion extends Question {

	public LanguageQuestion(long id, String text, String correctAnswer, Difficulty difficulty, int priority, int timeStep, int timeSteps) {
		super(id, text, correctAnswer, difficulty, priority, timeStep, timeSteps);
	}

	@Override
	public String toString() {
		return "LanguageQuestion [fromLanguage=" + text + ",toLanguage=" + correctAnswer + "]";
	}
}