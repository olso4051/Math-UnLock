package com.olyware.mathlock.model;

public class GenericQuestion {

	private String hash, question, description;
	private String[] answers, urls;

	public GenericQuestion(String description, String question, String[] answers) {
		this.description = description;
		this.hash = "";
		this.question = question;
		this.answers = answers;
		this.urls = new String[answers.length];
		for (int i = 0; i < urls.length; i++) {
			urls[i] = "";
		}
	}

	public GenericQuestion(String description, String hash, String question, String[] answers, String[] urls) {
		this.description = description;
		this.hash = hash;
		this.question = question;
		this.answers = answers;
		this.urls = urls;
	}

	public String getDescription() {
		return description;
	}

	public String getHash() {
		return hash;
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
