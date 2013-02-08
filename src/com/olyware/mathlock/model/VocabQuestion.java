package com.olyware.mathlock.model;

public class VocabQuestion extends Question {

	public enum PartOfSpeech {
		NOUN("(n.)"), VERB("(v.)"), ADVERB("(adv.)"), ADJECTIVE("(adj.)");

		String value;

		private PartOfSpeech(String value) {
			this.value = value;
		}

		public static PartOfSpeech fromValue(String value) {
			if ("(n.)".equals(value)) {
				return NOUN;
			} else if ("(v.)".equals(value)) {
				return VERB;
			} else if ("(adv.)".equals(value)) {
				return ADVERB;
			} else if ("(adj.)".equals(value)) {
				return ADJECTIVE;
			} else {
				return null;
			}
		}
	}

	PartOfSpeech partOfSpeech;

	public VocabQuestion(String text, String correctAnswer, Difficulty difficulty, PartOfSpeech partOfSpeech, int priority) {
		super(text, correctAnswer, difficulty, priority);
		this.partOfSpeech = partOfSpeech;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((partOfSpeech == null) ? 0 : partOfSpeech.hashCode());
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
		VocabQuestion other = (VocabQuestion) obj;
		if (partOfSpeech != other.partOfSpeech)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VocabQuestion [partOfSpeech=" + partOfSpeech + "]";
	}
}