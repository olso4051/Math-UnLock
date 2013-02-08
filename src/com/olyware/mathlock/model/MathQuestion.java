package com.olyware.mathlock.model;

import java.util.Random;

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
	String image, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3, range;
	private char questionVariables[] = { 'A', 'B', 'C', 'D' };
	public double questionVariableValues[] = new double[questionVariables.length];
	public int variablePrecision[] = new int[questionVariables.length];
	int answerPrecision;
	private Random rand = new Random();

	public MathQuestion(String text, String image, String correctAnswer, String incorrectAnswer1, String incorrectAnswer2,
			String incorrectAnswer3, Difficulty difficulty, ParseMode parseMode, String range, int answerPrecision, int priority) {
		super(text, correctAnswer, difficulty, priority);
		this.image = image;
		this.incorrectAnswer1 = incorrectAnswer1;
		this.incorrectAnswer2 = incorrectAnswer2;
		this.incorrectAnswer3 = incorrectAnswer3;
		this.parseMode = parseMode;
		this.range = range;
		this.answerPrecision = answerPrecision;
	}

	public void setVariables() {
		variablePrecision = getVariablePrecisionFromQuestion();
		if (range.equals("default")) {
			for (int i = 0; i < questionVariables.length; i++) {
				questionVariableValues[i] = rand.nextInt(9) + 1;	// default random number 1-9 precision=0
			}
		} else {
			questionVariableValues = getValuesFromRangeAndPrecision();
		}
	}

	public double[] getVariables() {
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
				postParse[i] = getStringPrecisionNumber(math.evaluate(preParse[i]), answerPrecision);
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
			if (indices[i] != -1) {
				postParse = postParse.replaceAll(String.valueOf(questionVariables[i]) + variablePrecision[i],
						getStringPrecisionNumber(questionVariableValues[i], variablePrecision[i]));
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
				subEq = getStringPrecisionNumber(math.evaluate(subEq), answerPrecision);
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

	private double[] getValuesFromRangeAndPrecision() {
		int index = 0, first = 0, count = 0, needs = 0;
		double min = 0, max;
		char next;
		String subEq;
		MathEval math = new MathEval();
		double Values[] = new double[questionVariableValues.length];
		while (index < range.length()) {
			next = range.charAt(index);
			if ((next == '-') && (first != index) && (needs == 0)) {
				subEq = range.substring(first, index);
				min = Double.parseDouble(subEq);
				first = index + 1;
			} else if (next == ',') {
				subEq = range.substring(first, index);
				max = Double.parseDouble(subEq);
				if (variablePrecision[count] >= 0)
					Values[count] = getDoublePrecisionNumber(rand.nextDouble() * (max - min) + min, variablePrecision[count]);
				else
					Values[count] = getDoublePrecisionNumber(rand.nextDouble() * (max - min) + min, answerPrecision);
				math.setVariable(String.valueOf(questionVariables[count]), Values[count]);
				count++;
				first = index + 1;
			} else if ((next == ')') && (needs == 1)) {
				subEq = range.substring(first, index + 1);
				if (index + 1 == range.length()) { 				// max
					max = math.evaluate(subEq);
					if (variablePrecision[count] >= 0)
						Values[count] = getDoublePrecisionNumber(rand.nextDouble() * (max - min) + min, variablePrecision[count]);
					else
						Values[count] = getDoublePrecisionNumber(rand.nextDouble() * (max - min) + min, answerPrecision);
					math.setVariable(String.valueOf(questionVariables[count]), Values[count]);
					count++;
				} else if (range.charAt(index + 1) == '-') {	// min
					min = math.evaluate(subEq);
				} else {										// max
					max = math.evaluate(subEq);
					if (variablePrecision[count] >= 0)
						Values[count] = getDoublePrecisionNumber(rand.nextDouble() * (max - min) + min, variablePrecision[count]);
					else
						Values[count] = getDoublePrecisionNumber(rand.nextDouble() * (max - min) + min, answerPrecision);
					math.setVariable(String.valueOf(questionVariables[count]), Values[count]);
					count++;
				}
				index += 1;
				first = index + 1;
				needs -= 1;
			} else if ((next == ')') && (needs > 1)) {
				needs -= 1;
			} else if ((next == '(') && (needs == 0)) {
				needs += 1;
				first = index;
			} else if (next == '(')
				needs += 1;
			index += 1;
		}
		return Values;
	}

	private int[] getVariablePrecisionFromQuestion() {
		String preParse = super.getQuestionText();
		int index;
		int p[] = new int[questionVariables.length];
		for (int i = 0; i < questionVariables.length; i++) {
			index = preParse.indexOf(questionVariables[i]);
			if (index != -1)
				p[i] = Integer.parseInt(preParse.substring(index + 1, index + 2));
			else
				p[i] = -1;
		}
		return p;
	}

	private double getDoublePrecisionNumber(double num, int decimalPrecision) {
		if (decimalPrecision == -1)
			return num;
		else if (decimalPrecision == 0)
			return Math.round(num);
		else {
			double factor = Math.pow(10, decimalPrecision);
			return Math.round(num * factor) / factor;
		}
	}

	private String getStringPrecisionNumber(double num, int decimalPrecision) {
		if (decimalPrecision == -1)
			return String.valueOf(num);
		else if (decimalPrecision == 0)
			return String.valueOf(Math.round(num));
		else {
			double factor = Math.pow(10, decimalPrecision);
			return String.valueOf(Math.round(num * factor) / factor);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + answerPrecision;
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + ((incorrectAnswer1 == null) ? 0 : incorrectAnswer1.hashCode());
		result = prime * result + ((incorrectAnswer2 == null) ? 0 : incorrectAnswer2.hashCode());
		result = prime * result + ((incorrectAnswer3 == null) ? 0 : incorrectAnswer3.hashCode());
		result = prime * result + ((parseMode == null) ? 0 : parseMode.hashCode());
		result = prime * result + ((range == null) ? 0 : range.hashCode());
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
		if (answerPrecision != other.answerPrecision)
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (incorrectAnswer1 == null) {
			if (other.incorrectAnswer1 != null)
				return false;
		} else if (!incorrectAnswer1.equals(other.incorrectAnswer1))
			return false;
		if (incorrectAnswer2 == null) {
			if (other.incorrectAnswer2 != null)
				return false;
		} else if (!incorrectAnswer2.equals(other.incorrectAnswer2))
			return false;
		if (incorrectAnswer3 == null) {
			if (other.incorrectAnswer3 != null)
				return false;
		} else if (!incorrectAnswer3.equals(other.incorrectAnswer3))
			return false;
		if (parseMode != other.parseMode)
			return false;
		if (range == null) {
			if (other.range != null)
				return false;
		} else if (!range.equals(other.range))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MathQuestion [image=" + image + "|incorrect1=" + incorrectAnswer1 + "|incorrect2=" + incorrectAnswer2 + "|incorrect3="
				+ incorrectAnswer3 + "|parseMode=" + parseMode + "]";
	}
}