package com.olyware.mathlock.model;

import java.util.Random;

import android.util.Log;

import com.olyware.mathlock.MathEval;

public class MathQuestion extends Question {

	public enum ParseMode {
		ALL(0), PARENTHESIS_ONLY(1), NOTHING(2);

		int value;

		private ParseMode(int value) {
			this.value = value;
		}

		public static ParseMode fromValue(int value) {
			switch (value) {
			case 0:
				return ALL;
			case 1:
				return PARENTHESIS_ONLY;
			case 2:
				return NOTHING;
			default:
				return null;
			}
		}
	}

	ParseMode parseMode;
	String image, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3;
	private char questionVariables[] = { 'A', 'B', 'C', 'D' };
	public int questionVariableValues[] = new int[questionVariables.length];
	private Random rand = new Random();

	public MathQuestion(String text, String image, String correctAnswer, String incorrectAnswer1, String incorrectAnswer2,
			String incorrectAnswer3, Difficulty difficulty, ParseMode parseMode) {
		super(text, correctAnswer, difficulty);
		this.image = image;
		this.incorrectAnswer1 = incorrectAnswer1;
		this.incorrectAnswer2 = incorrectAnswer2;
		this.incorrectAnswer3 = incorrectAnswer3;
		this.parseMode = parseMode;
	}

	public void setVariables() {
		for (int i = 0; i < questionVariables.length; i++) {
			questionVariableValues[i] = rand.nextInt(10) + 1;
		}
	}

	public int[] getVariables() {
		return questionVariableValues;
	}

	public String getImage() {
		return image;
	}

	public String[] getAnswers() {
		String preParse[] = new String[] { correctAnswer, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3 };
		String postParse[] = preParse;
		MathEval math = new MathEval();
		for (int i = 0; i < questionVariables.length; i++) {
			math.setVariable(String.valueOf(questionVariables[i]), questionVariableValues[i]);
		}
		for (int i = 0; i < preParse.length; i++) {
			switch (parseMode) {
			case ALL:
				postParse[i] = String.valueOf(math.evaluate(preParse[i]));
			case PARENTHESIS_ONLY:
				postParse[i] = removeParentheses(preParse[i]);
			case NOTHING:
				postParse[i] = preParse[i];
			}
		}
		return postParse;
	}

	@Override
	public String getQuestionText() {
		String preParse = super.getQuestionText();
		String postParse = preParse;
		int indices[] = new int[questionVariables.length];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = preParse.indexOf(questionVariables[i]);
			Log.d("question test", "Variable - " + questionVariables[i]);
			Log.d("question test", "index - " + indices[i]);
			if (indices[i] != -1) {
				Log.d("question test", "before " + postParse);
				postParse = postParse.replaceAll(String.valueOf(questionVariables[i]), String.valueOf(questionVariableValues[i]));
				Log.d("question test", "after " + postParse);
			}
		}
		return postParse;
	}

	public ParseMode getParseMode() {
		return parseMode;
	}

	private String removeParentheses(String equation) {
		char next;
		int needs = 1;
		int index = equation.indexOf('(');
		int first = index;
		String subEq;
		MathEval math = new MathEval();
		for (int i = 0; i < questionVariables.length; i++) {
			math.setVariable(String.valueOf(questionVariables[i]), questionVariableValues[i]);
		}
		while (index < equation.length() - 1) {
			next = equation.charAt(index + 1);
			if ((next == ')') && (needs == 1)) {
				subEq = equation.substring(first, index + 2);
				subEq = String.valueOf(Math.round(math.evaluate(subEq)));
				if (first > 0)
					equation = equation.substring(0, first) + subEq + equation.substring(index + 2);
				else
					equation = subEq + equation.substring(index + 2);
				index = -1;
				needs -= 1;
			} else if ((next == ')') && (needs > 1))
				needs -= 1;
			else if ((next == '(') && (needs == 0)) {
				needs += 1;
				first = index + 1;
			} else if (next == '(')
				needs += 1;
			index += 1;
		}
		return equation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((parseMode == null) ? 0 : parseMode.hashCode());
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + ((incorrectAnswer1 == null) ? 0 : incorrectAnswer1.hashCode());
		result = prime * result + ((incorrectAnswer2 == null) ? 0 : incorrectAnswer2.hashCode());
		result = prime * result + ((incorrectAnswer3 == null) ? 0 : incorrectAnswer3.hashCode());
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
		MathQuestion other = (MathQuestion) obj;
		if (parseMode != other.parseMode)
			return false;
		if (image != other.image)
			return false;
		if (incorrectAnswer1 != other.incorrectAnswer1)
			return false;
		if (incorrectAnswer2 != other.incorrectAnswer2)
			return false;
		if (incorrectAnswer3 != other.incorrectAnswer3)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MathQuestion [image=" + image + "|incorrect1=" + incorrectAnswer1 + "|incorrect2=" + incorrectAnswer2 + "|incorrect3="
				+ incorrectAnswer3 + "|parseMode=" + parseMode + "]";
	}
}