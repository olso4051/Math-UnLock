package com.olyware.mathlock.model;

public enum Difficulty {

	VERY_EASY(0), EASY(1), MEDIUM(2), HARD(3), VERY_HARD(4), INSANE(5);

	private int value;

	private Difficulty(int value) {
		this.value = value;
	}

	public static int getSize() {
		return 6;
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
		default:
			difficulty = VERY_EASY;
		}

		return difficulty;
	}

	public static Difficulty fromValue(String value) {
		Difficulty difficulty = null;
		if (value.equals("Very Easy") || value.equals("0"))
			difficulty = VERY_EASY;
		else if (value.equals("Easy") || value.equals("1"))
			difficulty = EASY;
		else if (value.equals("Medium") || value.equals("2"))
			difficulty = MEDIUM;
		else if (value.equals("Hard") || value.equals("3"))
			difficulty = HARD;
		else if (value.equals("Very Hard") || value.equals("4"))
			difficulty = VERY_HARD;
		else if (value.equals("Insane") || value.equals("5"))
			difficulty = INSANE;

		return difficulty;
	}

	public static String fromValueToString(int value) {
		String difficulty = null;
		switch (value) {
		case 0:
			difficulty = "Very Easy";
			break;
		case 1:
			difficulty = "Easy";
			break;
		case 2:
			difficulty = "Medium";
			break;
		case 3:
			difficulty = "Hard";
			break;
		case 4:
			difficulty = "Very Hard";
			break;
		case 5:
			difficulty = "Insane";
			break;
		}
		return difficulty;
	}

	public static boolean isDifficulty(String value) {
		if (value == null) {
			return false;
		}
		int length = value.length();
		if (length == 0) {
			return false;
		}
		if (value.charAt(0) < '0' || value.charAt(0) > '5') {
			return false;
		}
		return true;
	}

	public static boolean isDifficulty(int value) {
		if (value < 0 || value > 5) {
			return false;
		}
		return true;
	}

	public int getValue() {
		return value;
	}
}
