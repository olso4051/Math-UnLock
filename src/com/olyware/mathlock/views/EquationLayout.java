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
		private boolean Shown;

		private Bracket(int Location, boolean Shown) {
			this.Location = Location;
			this.Shown = Shown;
		}

		public int getLocation() {
			return Location;
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
		char currentChar;
		List<Bracket> opened = new ArrayList<Bracket>();
		List<Bracket> closed = new ArrayList<Bracket>();

		// find the brackets in the equation
		for (int i = 0; i < equationTextFull.length(); i++) {
			currentChar = equationTextFull.charAt(i);
			if (currentChar == '\\') {
				attributes.add(new Attributes(0));
				i++;
				currentChar = equationTextFull.charAt(i);
				if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
					attributes.add(new Attributes(6));
					opened.add(new Bracket(i, true));
				} else if ((currentChar == ')') || (currentChar == ']') || (currentChar == '}')) {
					attributes.add(new Attributes(6));
					closed.add(new Bracket(i, true));
				}
			} else if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
				attributes.add(new Attributes(6));
				opened.add(new Bracket(i, false));
			} else if ((currentChar == ')') || (currentChar == ']') || (currentChar == '}')) {
				attributes.add(new Attributes(6));
				closed.add(new Bracket(i, false));
			} else
				attributes.add(new Attributes(0));
		}

		for (int i = 0; i < equationTextFull.length(); i++) {
			currentChar = equationTextFull.charAt(i);
			if (currentChar == '$')
				notEquation = !notEquation;
			else if (!notEquation) {
				if (currentChar == '\\') {
					i++;
					currentChar = equationTextFull.charAt(i);
					if ((currentChar == '{') || (currentChar == '(') || (currentChar == '[') || (currentChar == '∫')
							|| (currentChar == '∑') || (currentChar == '∏')) {
						equationText = equationText + currentChar;
						// attributes.add("5");
					}
				} else if (currentChar == '_') {

				} else if (currentChar == '^') {

				} else if (currentChar == '/') {

				} else if (currentChar == '(') {

				} else if (currentChar == ')') {

				}

			} else {
				equationText = equationText + currentChar;
				// attributes.add("0");
			}
		}
	}
}
