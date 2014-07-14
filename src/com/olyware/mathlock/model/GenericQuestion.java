package com.olyware.mathlock.model;

public class GenericQuestion {

	private String question, description;
	private String[] answers;

	public GenericQuestion(String description, String question, String[] answers) {
		this.description = description;
		this.question = question;
		this.answers = answers;
	}

	public String getDescription() {
		return description;
	}

	public String getQuestion() {
		return question;
	}

	public String[] getAnswers() {
		return answers;
	}
}
