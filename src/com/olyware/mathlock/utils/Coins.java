package com.olyware.mathlock.utils;

public class Coins {
	private int money;
	private int moneyPaid;

	public Coins(int money, int moneyPaid) {
		this.money = money;
		this.moneyPaid = moneyPaid;
	}

	public int getMoney() {
		return money;
	}

	public int getMoneyPaid() {
		return moneyPaid;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public void setMoneyPaid(int moneyPaid) {
		this.moneyPaid = moneyPaid;
	}

	public int increaseMoney(int amount) {
		this.money += amount;
		return amount;
	}

	public int increaseMoneyPaid(int amount) {
		this.moneyPaid += amount;
		return amount;
	}

	public int decreaseMoneyNoDebt(int amount) {
		int initMoney = this.money;
		this.money -= amount;
		if (this.money < 0)
			this.money = 0;
		return initMoney - this.money;
	}

	public void decreaseMoneyAndPaidWithDebt(int amount) {
		this.money -= amount;
		if (this.money < 0) {
			this.moneyPaid += this.money;
			this.money = 0;
		}
	}
}