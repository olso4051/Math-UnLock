package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.List;

import com.olyware.mathlock.adapter.QuestionSelectData;

public class ChallengeBuilder {
	private String userHash, userName;
	private int bet, numQuestions, difficultyMin, difficultyMax;
	private List<Integer> selectedQuestionPackIDs = new ArrayList<Integer>();
	private List<String> questions = new ArrayList<String>();
	private List<String[]> answers = new ArrayList<String[]>();

	public ChallengeBuilder(String userName, String userHash) {
		this.userName = userName;
		this.userHash = userHash;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUserHash(String userHash) {
		this.userHash = userHash;
	}

	public void setQuestionSettings(int bet, int numQuestions, int difficultyMin, int difficultyMax) {
		this.bet = bet;
		this.numQuestions = numQuestions;
		this.difficultyMin = difficultyMin;
		this.difficultyMax = difficultyMax;
	}

	public void setSelectedQuestionPacks(List<QuestionSelectData> selectedQuestionPacks) {
		this.selectedQuestionPackIDs.clear();
		for (QuestionSelectData pack : selectedQuestionPacks) {
			this.selectedQuestionPackIDs.add(pack.getID());
		}
	}

	public void setSelectedQuestionPackIDs(List<Integer> selectedQuestionPackIDs) {
		this.selectedQuestionPackIDs.clear();
		this.selectedQuestionPackIDs.addAll(selectedQuestionPackIDs);
	}

	public void setQuestions(List<String> questions) {
		this.questions.clear();
		this.questions.addAll(questions);
	}

	public void setAnswers(List<String[]> answers) {
		this.answers.clear();
		this.answers.addAll(answers);
	}

	public String getUserName() {
		return userName;
	}

	public String getUserHash() {
		return userHash;
	}

	public int getBet() {
		return bet;
	}

	public int getNumberOfQuestions() {
		return numQuestions;
	}

	public int getDifficultyMin() {
		return difficultyMin;
	}

	public int getDifficultyMax() {
		return difficultyMax;
	}

	public List<Integer> getSelectedQuestionPackIDs() {
		return selectedQuestionPackIDs;
	}

	public List<String> getQuestions() {
		return questions;
	}

	public List<String[]> getAnswers() {
		return answers;
	}

}
