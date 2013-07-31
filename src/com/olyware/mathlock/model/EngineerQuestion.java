package com.olyware.mathlock.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EngineerQuestion extends Question {

	private String variables;
	private String incorrectAnswer1, incorrectAnswer2, incorrectAnswer3;
	private Random rand = new Random();

	public EngineerQuestion(int id, String text, String variables, Difficulty difficulty, int priority) {
		super(id, text, variables.substring(0, 1), difficulty, priority);// not the right correct answer at this point
		this.variables = variables;
		// Log.d("test", "question id=" + id + "question=" + text);
		setVariables();
	}

	public String[] getAnswers() {
		return new String[] { correctAnswer, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3 };
	}

	private void setVariables() {
		// String preParse = super.getQuestionText();
		String[] postParse = new String[5];
		postParse = setQuestionAndAnswer(text);
		this.text = postParse[0];
		this.correctAnswer = postParse[1];
		this.incorrectAnswer1 = postParse[2];
		this.incorrectAnswer2 = postParse[3];
		this.incorrectAnswer3 = postParse[4];
	}

	private String[] setQuestionAndAnswer(String equation) {
		String subEq = "", subEqFinal = "";
		int indexs = equation.indexOf('|');
		int indexf = equation.indexOf('|', indexs + 1);
		subEq = equation.substring(indexs + 1, indexf);
		subEqFinal = equation.substring(Math.min(indexf + 1, equation.length()), equation.length());
		equation = equation.substring(0, indexs);

		List<String> wrong = getWrongAnswers();
		if (wrong.size() > 3)
			wrong = shuffleStringList(wrong);

		equation = equation + "?" + subEqFinal;
		subEq = "$" + subEq + "$";
		wrong.set(0, "$" + wrong.get(0) + "$");
		wrong.set(1, "$" + wrong.get(1) + "$");
		wrong.set(2, "$" + wrong.get(2) + "$");

		return new String[] { equation, subEq, wrong.get(0), wrong.get(1), wrong.get(2) };
	}

	private List<String> getWrongAnswers() {
		int index = 0;
		int indexStart = index;
		char nextC;
		boolean variable = false;
		List<String> wrongAnswers = new ArrayList<String>();
		while (index < variables.length()) {
			nextC = variables.charAt(index);
			if ((nextC == ';') && (variable)) {
				wrongAnswers.add(variables.substring(indexStart, index));
				variable = false;
			} else if (!variable) {
				variable = true;
				indexStart = index;
			}
			if (index + 1 == variables.length()) {
				wrongAnswers.add(variables.substring(indexStart, index + 1));
			}
			index += 1;
		}
		return wrongAnswers;// .toArray(new String[wrongAnswers.size()]);
	}

	private List<String> shuffleStringList(List<String> ar) {
		for (int i = ar.size() - 1; i >= 0; i--) {
			int index = rand.nextInt(i + 1);
			// Simple swap
			String a = ar.get(index);
			ar.set(index, ar.get(i));
			ar.set(i, a);
		}
		return ar;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((variables == null) ? 0 : variables.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EngineerQuestion other = (EngineerQuestion) obj;
		if (variables == null) {
			if (other.variables != null)
				return false;
		} else if (!variables.equals(other.variables))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EngineerQuestion [variables=" + variables + "]";
	}
}