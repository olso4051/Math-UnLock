package com.olyware.mathlock.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EngineerQuestion extends Question {

	public enum ParseModeE {
		SINGLE(0), ALL(1);

		int value;

		private ParseModeE(int value) {
			this.value = value;
		}

		public static ParseModeE fromValue(int value) {
			switch (value) {
			case 0:
				return SINGLE;
			case 1:
				return ALL;
			default:
				return null;
			}
		}
	}

	private ParseModeE parseMode;
	private String variables;
	private String incorrectAnswer1, incorrectAnswer2, incorrectAnswer3;
	private Random rand = new Random();

	public EngineerQuestion(int id, String text, String variables, Difficulty difficulty, ParseModeE parseMode, int priority) {
		super(id, text, variables.substring(0, 1), difficulty, priority);// not the right correct answer at this point
		this.variables = variables;
		this.parseMode = parseMode;
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
		// char next;
		// int needs = 1;
		// int index = equation.indexOf('(');
		// int first = index;
		String subEq = "", subEqFinal = "";
		int indexs = equation.indexOf('|');
		int indexf = equation.indexOf('|', indexs + 1);
		subEq = equation.substring(indexs + 1, indexf);
		subEqFinal = equation.substring(Math.min(indexf + 1, equation.length()), equation.length());
		equation = equation.substring(0, indexs);
		/*while (index < equation.length() - 1) {
			next = equation.charAt(index + 1);
			if ((next == ')') && (needs == 1)) {
				subEq = equation.substring(first + 1, index + 1);
				subEqFinal = equation.substring(Math.min(index + 2, equation.length()), equation.length());
				equation = equation.substring(0, first);
				break;
			} else if ((next == ')') && (needs > 1))
				needs -= 1;
			else if (next == '(')
				needs += 1;
			index += 1;
		}*/
		int variableLocs[][] = getVariableLocations(subEq);
		int rows = variableLocs.length;
		int loc = rand.nextInt(rows);
		String variable = subEq.substring(variableLocs[loc][0], variableLocs[loc][1]);
		if (variableLocs[loc][0] > 0) {
			subEq = subEq.substring(0, variableLocs[loc][0]) + "?" + subEq.substring(variableLocs[loc][1]);
		} else {
			subEq = "?" + subEq.substring(variableLocs[loc][1]);
		}
		List<String> wrong = getWrongAnswers(loc);

		while (wrong.size() < 3) {
			loc = (loc + 1) % rows;
			wrong.addAll(getWrongAnswers(loc));
		}
		if (wrong.size() > 3)
			wrong = shuffleStringList(wrong);
		equation = equation + subEq + subEqFinal;
		if (text.charAt(0) == '$')
			if (text.length() > 1)
				if (text.charAt(1) != '$') {
					variable = "$" + variable + "$";
					wrong.set(0, "$" + wrong.get(0) + "$");
					wrong.set(1, "$" + wrong.get(1) + "$");
					wrong.set(2, "$" + wrong.get(2) + "$");
				}
		return new String[] { equation, variable, wrong.get(0), wrong.get(1), wrong.get(2) };
	}

	private int[][] getVariableLocations(String s) {
		int index = 0;
		int indexStart = 0;
		int next;
		char nextC;
		boolean number = false;
		boolean variable = false;
		List<Integer> locs = new ArrayList<Integer>();
		List<Integer> locf = new ArrayList<Integer>();
		if (parseMode.equals(ParseModeE.SINGLE)) {
			while (index < s.length()) {
				next = s.codePointAt(index);
				nextC = s.charAt(index);
				if (number) {
					// check if next is not a number and still a variable
					if ((!(Character.isDigit(next) || (nextC == '.'))) && (variable)) {
						locs.add(indexStart);
						locf.add(index);
						number = false;
						variable = false;
						// if next is a number set variable to false
					} else if (Character.isDigit(next) || (nextC == '.')) {
						variable = false;
						// next is not a number or variable so reset number to false
					} else
						number = false;
					// if not a number then if next is math symbol and variable is true then we found a variable
				} else if ((nextC == '+') || (nextC == '-') || (nextC == '*') || (nextC == '/') || (nextC == '(') || (nextC == ')')
						|| (nextC == '^') || (nextC == '\\') || (nextC == '=')) {
					if (variable) {
						locs.add(indexStart);
						locf.add(index);
						number = false;
						variable = false;
						// if next is a number then we may have found a variable
					}
				} else if (Character.isDigit(next) || (nextC == '.')) {
					number = true;
					variable = true;
					indexStart = index;
					// if variable is false then we have found the start of a variable
				} else if (!variable) {
					variable = true;
					indexStart = index;
				}
				index += 1;
			}
			// if the last char was a variable then we found the last variable
			if (variable) {
				locs.add(indexStart);
				locf.add(index);
			}
		} else {
			locs.add(0);
			locf.add(s.length());
		}
		int[][] array = new int[locs.size()][2];
		for (int i = 0; i < locs.size(); i++) {
			array[i][0] = locs.get(i);
			array[i][1] = locf.get(i);
		}
		return array;
	}

	private List<String> getWrongAnswers(int loc) {
		int index = 0;
		for (int i = 0; i < loc; i++)
			index = variables.indexOf(';', index) + 1;
		int indexStart = index;
		char nextC;
		boolean variable = false;
		List<String> wrongAnswers = new ArrayList<String>();
		while (index < variables.length()) {
			nextC = variables.charAt(index);
			if ((nextC == ';') && (variable)) {
				wrongAnswers.add(variables.substring(indexStart, index));
				break;
			} else if (nextC == ';')
				break;
			else if ((nextC == ',') && (variable)) {
				wrongAnswers.add(variables.substring(indexStart, index));
				variable = false;
			} else if (!variable) {
				variable = true;
				indexStart = index;
			}
			index += 1;
		}
		return wrongAnswers;// .toArray(new String[wrongAnswers.size()]);
	}

	private List<String> shuffleStringList(List<String> ar) {
		Random rnd = new Random();
		for (int i = ar.size() - 1; i >= 0; i--) {
			int index = rnd.nextInt(i + 1);
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