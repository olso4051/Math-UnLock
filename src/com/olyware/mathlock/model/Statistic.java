package com.olyware.mathlock.model;

public class Statistic {

	String pack, correct;
	Difficulty difficulty;
	long time, time2Solve;

	public Statistic(String pack, String correct, Difficulty difficulty, long time, long time2Solve) {
		super();
		this.pack = pack;
		this.correct = correct;
		this.difficulty = difficulty;
		this.time = time;
		this.time2Solve = time2Solve;
	}

	public void setPack(String pack) {
		this.pack = pack;
	}

	public void setCorrect(String correct) {
		this.correct = correct;
	}

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setTime2Solve(long time2Solve) {
		this.time2Solve = time2Solve;
	}

	public String getPack() {
		return pack;
	}

	public String getCorrect() {
		return correct;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public long getTime() {
		return time;
	}

	public long getTime2Solve() {
		return time2Solve;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((correct == null) ? 0 : correct.hashCode());
		result = prime * result + ((difficulty == null) ? 0 : difficulty.hashCode());
		result = prime * result + ((pack == null) ? 0 : pack.hashCode());
		result = prime * result + (int) (time ^ (time >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Statistic other = (Statistic) obj;
		if (correct == null) {
			if (other.correct != null)
				return false;
		} else if (!correct.equals(other.correct))
			return false;
		if (difficulty != other.difficulty)
			return false;
		if (pack == null) {
			if (other.pack != null)
				return false;
		} else if (!pack.equals(other.pack))
			return false;
		if (time != other.time)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Statistic [pack=" + pack + ",correct=" + correct + ",difficulty=" + difficulty + ",time=" + time + "]";
	}

}