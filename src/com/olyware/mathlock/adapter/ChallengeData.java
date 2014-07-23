package com.olyware.mathlock.adapter;

public class ChallengeData {
	private String challengeID, userID;
	private int numOfQuestions;

	public ChallengeData(String challengeID, String userID, int numOfQuestions) {
		this.challengeID = challengeID;
		this.userID = userID;
		this.numOfQuestions = numOfQuestions;
	}

	public String getChallengeID() {
		return challengeID;
	}

	public String getUserID() {
		return userID;
	}

	public int getNumberOfQuestions() {
		return numOfQuestions;
	}

	public void setChallengeID(String challengeID) {
		this.challengeID = challengeID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public void setNumberOfQuestions(int numOfQuestions) {
		this.numOfQuestions = numOfQuestions;
	}
}
