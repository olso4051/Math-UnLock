package com.olyware.mathlock.views;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;

public class EquationLayout {
	private String equation;
	private int maxWidth, maxHeight;
	private String equationTextFull, equationText;
	private List<Attributes> attributes = new ArrayList<Attributes>();

	private class Bracket {
		private int Location;
		private int BracketSet;
		private boolean Shown;

		private Bracket(int Location, int BracketSet, boolean Shown) {
			this.Location = Location;
			this.BracketSet = BracketSet;
			this.Shown = Shown;
		}

		public int getLocation() {
			return Location;
		}

		public int getBracketSet() {
			return BracketSet;
		}

		public boolean getShown() {
			return Shown;
		}
	}

	private class Attributes {
		private List<Integer> type = new ArrayList<Integer>();

		public Attributes(int attribute) {
			type.add(attribute);
		}

		public void addAttribute(int attribute) {
			type.add(attribute);
		}

		public List<Integer> getAttributes() {
			return type;
		}
	}

	public EquationLayout(String equation, int maxWidth, int maxHeight) {
		this.equation = equation;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		parseEquation();
	}

	public void draw(Canvas c) {
		// TODO draw the equation
	}

	private void parseEquation() {
		boolean notEquation = true;
		attributes.clear();
		equationText = "";

		// find the brackets in the equation
		char currentChar;
		int set = 0;
		List<Bracket> opened = new ArrayList<Bracket>();
		List<Bracket> closed = new ArrayList<Bracket>();
		List<Integer> bSet = new ArrayList<Integer>();
		for (int i = 0; i < equationTextFull.length(); i++) {
			currentChar = equationTextFull.charAt(i);
			attributes.add(new Attributes(0));
			if (currentChar == '\\') {
				i++;
				currentChar = equationTextFull.charAt(i);
				attributes.add(new Attributes(0));
				if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
					attributes.get(i).addAttribute(6);
					opened.add(new Bracket(i, set, true));
					bSet.add(set);
					set++;
				} else if ((currentChar == ')') || (currentChar == ']') || (currentChar == '}')) {
					attributes.get(i).addAttribute(6);
					closed.add(new Bracket(i, bSet.get(bSet.size() - 1), true));
					bSet.remove(bSet.size() - 1);
				}
			} else if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
				attributes.get(i).addAttribute(6);
				opened.add(new Bracket(i, set, false));
				bSet.add(set);
				set++;
			} else if ((currentChar == ')') || (currentChar == ']') || (currentChar == '}')) {
				attributes.get(i).addAttribute(6);
				closed.add(new Bracket(i, bSet.get(bSet.size() - 1), false));
				bSet.remove(bSet.size() - 1);
			}
		}

		// set attributes for each character
		for (int a = 0; a < closed.size(); a++) {
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();
			char charAfter = equationTextFull.charAt(locC + 1);
			char charBefore = equationTextFull.charAt(locO - 1);
			int att = 0;
			if (charBefore == '\\')
				charBefore = equationTextFull.charAt(locO - 2);
			if (charBefore == '_')
				att = 1;
			else if (charBefore == '^')
				att = 2;
			else if (charBefore == '/')
				att = 4;
			else if (charBefore == '√')
				att = 5;
			else if (charAfter == '/')
				att = 3;
			int start = locO + 1;
			int end = locC - 1;
			if (equationTextFull.charAt(end) == '\\')
				end--;
			boolean ignore = false;
			int needs = 0;
			for (int b = start; b < end; b++) {
				attributes.get(b).addAttribute(att);
				charAfter = equationTextFull.charAt(b + 1);
				currentChar = equationTextFull.charAt(b);
				if (!ignore) {
					if (b == start) {
						if ((currentChar == '(') || (currentChar == '\\')) {
							ignore = true;
							needs = 1;
						} else if (charAfter == '/')
							attributes.get(b).addAttribute(3);
						else
							attributes.get(b).addAttribute(0);
					} else {
						charBefore = equationTextFull.charAt(b - 1);
						if ((currentChar == '(') || (currentChar == '\\')) {
							ignore = true;
							needs = 1;
						} else if (charBefore == '_')
							attributes.get(b).addAttribute(1);
						else if (charBefore == '^')
							attributes.get(b).addAttribute(2);
						else if (charBefore == '/')
							attributes.get(b).addAttribute(4);
						else if (charBefore == '√')
							attributes.get(b).addAttribute(5);
						else if (charAfter == '/')
							attributes.get(b).addAttribute(3);
						else
							attributes.get(b).addAttribute(0);
					}
				} else {
					if (currentChar == '(')
						needs++;
					else if (currentChar == ')') {
						needs--;
						if (needs == 0)
							ignore = false;
					}
				}
			}
		}

		// remove extra brackets, _ , ^ , \ , and $ that wont get displayed
		boolean keepNext = false;
		for (int i = 0; i < equationTextFull.length(); i++) {
			currentChar = equationTextFull.charAt(i);
			if (currentChar == '\\')
				keepNext = true;
			else if (keepNext) {
				keepNext = false;
				equationText = equationText + currentChar;
			} else if ((currentChar == '$') || (currentChar == '_') || (currentChar == '^') || (currentChar == '(') || (currentChar == '[')
					|| (currentChar == '{') || (currentChar == ')') || (currentChar == ']') || (currentChar == '}'))
				;
			else
				equationText = equationText + currentChar;
		}
	}
}
