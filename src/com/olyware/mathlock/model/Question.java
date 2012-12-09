package com.olyware.mathlock.model;

import java.io.File;
import java.util.List;

public class Question {

	String text;
	File image;
	String correctAnswer;
	List<String> wrongAnswers;
	Difficulty difficulty;

	public Question(String text, File image, String correctAnswer, List<String> wrongAnswers, Difficulty difficulty) {
		super();
		this.text = text;
		this.image = image;
		this.correctAnswer = correctAnswer;
		this.wrongAnswers = wrongAnswers;
		this.difficulty = difficulty;
	}

	public String getText() {
		return text;
	}

	public File getImage() {
		return image;
	}

	public String getCorrectAnswer() {
		return correctAnswer;
	}

	public List<String> getWrongAnswers() {
		return wrongAnswers;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((correctAnswer == null) ? 0 : correctAnswer.hashCode());
		result = prime * result + ((difficulty == null) ? 0 : difficulty.hashCode());
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((wrongAnswers == null) ? 0 : wrongAnswers.hashCode());
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
		Question other = (Question) obj;
		if (correctAnswer == null) {
			if (other.correctAnswer != null)
				return false;
		} else if (!correctAnswer.equals(other.correctAnswer))
			return false;
		if (difficulty != other.difficulty)
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (wrongAnswers == null) {
			if (other.wrongAnswers != null)
				return false;
		} else if (!wrongAnswers.equals(other.wrongAnswers))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Question [text=" + text + ", image=" + image + ", correctAnswer=" + correctAnswer + ", wrongAnswers=" + wrongAnswers
				+ ", difficulty=" + difficulty + "]";
	}
}
