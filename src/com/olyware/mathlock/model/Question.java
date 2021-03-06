package com.olyware.mathlock.model;

public abstract class Question {

	String text, correctAnswer;
	private Difficulty difficulty;
	long id;
	int priority, timeStep, timeSteps;

	public Question(long id, String text, String correctAnswer, Difficulty difficulty, int priority, int timeStep, int timeSteps) {
		super();
		this.id = id;
		this.text = text;
		this.correctAnswer = correctAnswer;
		this.difficulty = difficulty;
		this.priority = priority;
		this.timeStep = timeStep;
		this.timeSteps = timeSteps;
	}

	public long getID() {
		return id;
	}

	public String getQuestionText() {
		return text;
	}

	public String getCorrectAnswer() {
		return correctAnswer;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public int getPriority() {
		return priority;
	}

	public int getTimeStep() {
		return timeStep;
	}

	public int getTimeSteps() {
		return timeSteps;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((correctAnswer == null) ? 0 : correctAnswer.hashCode());
		result = prime * result + ((difficulty == null) ? 0 : difficulty.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Question [text=" + text + ",Answer=" + correctAnswer + ",difficulty=" + difficulty + "]";
	}

}
