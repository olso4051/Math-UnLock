package com.olyware.mathlock.model;

public enum Difficulty {

	VERY_EASY(0), EASY(1), MEDIUM(2), HARD(3), VERY_HARD(4), INSANE(5);

	private int value;

	private Difficulty(int value) {
		this.value = value;
	}

	public static Difficulty fromValue(int value) {
		Difficulty difficulty = null;

		switch (value) {
		case 0:
			difficulty = VERY_EASY;
			break;
		case 1:
			difficulty = EASY;
			break;
		case 2:
			difficulty = MEDIUM;
			break;
		case 3:
			difficulty = HARD;
			break;
		case 4:
			difficulty = VERY_HARD;
			break;
		case 5:
			difficulty = INSANE;
			break;
		}

		return difficulty;
	}

	public int getValue() {
		return value;
	}
}
