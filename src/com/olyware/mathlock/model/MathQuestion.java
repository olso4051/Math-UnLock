package com.olyware.mathlock.model;

import java.util.Random;

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
	private int questionVariableValues[] = new int[4];
	private char questionVariables[] = { 'A', 'B', 'C', 'D' };
	private Random rand=new Random();

	public MathQuestion(String text, String image, String correctAnswer, String incorrectAnswer1, String incorrectAnswer2,
			String incorrectAnswer3, Difficulty difficulty, ParseMode parseMode) {
		super(text, correctAnswer, difficulty);
		this.image = image;
		this.incorrectAnswer1 = incorrectAnswer1;
		this.incorrectAnswer2 = incorrectAnswer2;
		this.incorrectAnswer3 = incorrectAnswer3;
		this.parseMode = parseMode;
	}

	public String getImage() {
		return image;
	}

	public String[] getIncorrectAnswers() {
		// TODO parse wrong answer
		return new String[] { incorrectAnswer1, incorrectAnswer2, incorrectAnswer3 };
	}

	@Override
	public String getCorrectAnswer() {
		// TODO parse answer
		return correctAnswer;
	}

	@Override
	public String getQuestionText() {
		// TODO parse answer
		String preParse = super.getQuestionText();
		String postParse = preParse;
		int indices[] = new int[4];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = preParse.indexOf(questionVariables[i]);
			if (indices[i]!=-1){
				questionVariableValues[i]=rand.nextInt(10)+1;
				postParse.replace(String.valueOf(questionVariables[i]), String.valueOf(questionVariableValues[i]));
			}
		}
		return postParse;
	}

	public ParseMode getParseMode() {
		return parseMode;
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