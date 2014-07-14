package com.olyware.mathlock.model;

public class ChallengeQuestion {

	final public static int MAX_SCORE = 10000;
	private String challengeID, description, question, userName;
	private String[] answers = new String[4];
	private long id;

	public ChallengeQuestion(long id, String challengeID, String description, String question, String[] answers, String userName) {
		this.id = id;
		this.challengeID = challengeID;
		this.description = description;
		this.question = question;
		this.answers = answers;
		this.userName = userName;
	}

	public long getID() {
		return id;
	}

	public String getChallengeID() {
		return challengeID;
	}

	public String getDescription() {
		return description;
	}

	public String getQuestionText() {
		return question;
	}

	public String[] getAnswers() {
		return answers;
	}

	public String getUserName() {
		return userName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((answers == null) ? 0 : answers.hashCode());
		result = prime * result + ((question == null) ? 0 : question.hashCode());
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
		ChallengeQuestion other = (ChallengeQuestion) obj;
		if (answers == null) {
			if (other.answers != null)
				return false;
		} else if (!answers.equals(other.answers))
			return false;
		if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Question [text=" + question + ",Answer=" + answers.toString() + "]";
	}

}
