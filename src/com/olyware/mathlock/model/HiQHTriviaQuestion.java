package com.olyware.mathlock.model;

public class HiQHTriviaQuestion extends Question {

	private String incorrectAnswer1, incorrectAnswer2, incorrectAnswer3;

	public HiQHTriviaQuestion(int id, String text, String correctAnswer, String incorrectAnswer1, String incorrectAnswer2,
			String incorrectAnswer3, Difficulty difficulty, int priority) {
		super(id, text, correctAnswer, difficulty, priority);// not the right correct answer at this point
		this.incorrectAnswer1 = incorrectAnswer1;
		this.incorrectAnswer2 = incorrectAnswer2;
		this.incorrectAnswer3 = incorrectAnswer3;
	}

	public String[] getAnswers() {
		return new String[] { correctAnswer, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3 };
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		HiQHTriviaQuestion other = (HiQHTriviaQuestion) obj;
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
		return true;
	}

	@Override
	public String toString() {
		return "HiQHTriviaQuestion [incorrect1=" + incorrectAnswer1 + "|incorrect2=" + incorrectAnswer2 + "|incorrect3=" + incorrectAnswer3
				+ "]";
	}
}