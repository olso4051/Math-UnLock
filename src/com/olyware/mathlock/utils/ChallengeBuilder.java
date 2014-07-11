package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.List;

import com.olyware.mathlock.service.SendChallenge;

public class ChallengeBuilder {
	private SendChallenge.FriendType friendType;
	private String userHash;
	private List<String> selectedQuestionPacks = new ArrayList<String>();
	private List<String> questions = new ArrayList<String>();
	private List<String[]> answers = new ArrayList<String[]>();

	public ChallengeBuilder(SendChallenge.FriendType friendType, String userHash) {
		this.friendType = friendType;
		this.userHash = userHash;
	}

	public void setFriendType(SendChallenge.FriendType friendType) {
		this.friendType = friendType;
	}

	public void setUserHash(String userHash) {
		this.userHash = userHash;
	}

	public void setSelectedQuestionPacks(List<String> selectedQuestionPacks) {
		this.selectedQuestionPacks.clear();
		this.selectedQuestionPacks.addAll(selectedQuestionPacks);
	}

	public void setQuestions(List<String> questions) {
		this.questions.clear();
		this.questions.addAll(questions);
	}

	public void setAnswers(List<String[]> answers) {
		this.answers.clear();
		this.answers.addAll(answers);
	}

	public SendChallenge.FriendType getFriendType() {
		return friendType;
	}

	public String getUserHash() {
		return userHash;
	}

	public List<String> getSelectedQuestionPacks() {
		return selectedQuestionPacks;
	}

	public List<String> getQuestions() {
		return questions;
	}

	public List<String[]> getAnswers() {
		return answers;
	}

}
