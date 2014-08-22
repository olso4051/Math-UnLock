package com.olyware.mathlock.model;

public class GenericQuestion {

	private String question, description;
	private String[] answers, urls;

	public GenericQuestion(String description, String question, String[] answers) {
		this.description = description;
		this.question = question;
		this.answers = answers;
		this.urls = new String[answers.length];
		for (int i = 0; i < urls.length; i++) {
			urls[i] = "";
		}
	}

	public GenericQuestion(String description, String question, String[] answers, String[] urls) {
		this.description = description;
		this.question = question;
		this.answers = answers;
		this.urls = urls;
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

	public String[] getURLs() {
		return urls;
	}
}
