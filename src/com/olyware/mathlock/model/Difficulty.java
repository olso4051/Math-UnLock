package com.olyware.mathlock.model;

public enum Difficulty {

	EASY(1), MEDIUM(2), HARD(3);

	private int value;

	private Difficulty(int value) {
		this.value = value;
	}

	public static Difficulty fromValue(int value) {
		Difficulty difficulty = null;

		switch (value) {
		case 1:
			difficulty = EASY;
			break;
		case 2:
			difficulty = MEDIUM;
			break;
		case 3:
			difficulty = HARD;
			break;
		}

		return difficulty;
	}

	public int getValue() {
		return value;
	}
}
