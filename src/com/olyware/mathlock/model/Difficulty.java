package com.olyware.mathlock.model;

public enum Difficulty {

	VERY_EASY(1), EASY(2), MEDIUM(3), HARD(4), VERY_HARD(5), INSANE(6);

	private int value;

	private Difficulty(int value) {
		this.value = value;
	}

	public static Difficulty fromValue(int value) {
		Difficulty difficulty = null;

		switch (value) {
		case 1:
			difficulty = VERY_EASY;
			break;
		case 2:
			difficulty = EASY;
			break;
		case 3:
			difficulty = MEDIUM;
			break;
		case 4:
			difficulty = HARD;
			break;
		case 5:
			difficulty = VERY_HARD;
			break;
		case 6:
			difficulty = INSANE;
			break;
		}

		return difficulty;
	}

	public int getValue() {
		return value;
	}
}
