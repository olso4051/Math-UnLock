package com.olyware.mathlock.views;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.TypedValue;

import com.olyware.mathlock.MainActivity;

public class EquationLayout {
	private final int textSizeSPDefault = 30;
	private int maxWidth, maxHeight;
	private float textSizePix, textSizePixDefault;
	private String equationTextFull, equationText;
	private BracketGroup[] bracketGroups;
	private List<Integer> bracketGroup = new ArrayList<Integer>();
	private List<Attributes> attributes = new ArrayList<Attributes>();
	private List<Bracket> opened = new ArrayList<Bracket>();
	private List<Bracket> closed = new ArrayList<Bracket>();
	private List<TextAttributes> textAttributes = new ArrayList<TextAttributes>();

	private class Bracket {
		private int Location;
		private int BracketSet;

		private Bracket(int Location, int BracketSet) {
			this.Location = Location;
			this.BracketSet = BracketSet;
		}

		public int getLocation() {
			return Location;
		}

		public int getBracketSet() {
			return BracketSet;
		}
	}

	private class Attributes {
		private List<Att> type = new ArrayList<Att>();

		public Attributes(Att attribute) {
			type.add(attribute);
		}

		public void addAttribute(Att attribute) {
			type.add(attribute);
		}

		public List<Att> getAttributes() {
			return type;
		}
	}

	private enum Att {
		Normal(0), Subscript(1), Superscript(2), Numerator(3), Denominator(4), SquareRoot(5), Bracket(6);
		private int value;

		private Att(int value) {
			this.value = value;
		}

		private static Att fromValue(int value) {
			Att att = null;
			switch (value) {
			case 0:
				att = Normal;
				break;
			case 1:
				att = Subscript;
				break;
			case 2:
				att = Superscript;
				break;
			case 3:
				att = Numerator;
				break;
			case 4:
				att = Denominator;
				break;
			case 5:
				att = SquareRoot;
				break;
			case 6:
				att = Bracket;
				break;
			}
			return att;
		}

		public int getValue() {
			return value;
		}
	}

	private class TextAttributes {
		private int X, Y;
		private float SizePix, Width;
		private TextPaint paint;

		public TextAttributes(int X, int Y, float SizePix) {
			this.X = X;
			this.Y = Y;
			this.SizePix = SizePix;
			paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			paint.setTextSize(SizePix);
		}

		public int getX() {
			return X;
		}

		public int getY() {
			return Y;
		}

		public float getSizePix() {
			return SizePix;
		}

		public float getWidth() {
			return Width;
		}

		public TextPaint getTextPaint() {
			return paint;
		}

		public void setX(int X) {
			this.X = X;
		}

		public void setY(int Y) {
			this.Y = Y;
		}

		public void setSizePix(float SizePix) {
			this.SizePix = SizePix;
			paint.setTextSize(SizePix);
		}

		public void setWidth(float Width) {
			this.Width = Width;
		}
	}

	public class BracketGroup {
		private int Width, Height;

		public BracketGroup(int Width, int Height) {
			this.Width = Width;
			this.Height = Height;
		}

		public void setWidth(int Width) {
			this.Width = Width;
		}

		public void setHeight(int Height) {
			this.Height = Height;
		}

		public int getWidth() {
			return Width;
		}

		public int getHeight() {
			return Height;
		}
	}

	public EquationLayout(String equation, int maxWidth, int maxHeight) {
		this.equationTextFull = equation;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		textSizePixDefault = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSPDefault, MainActivity.getContext()
				.getResources().getDisplayMetrics());
		textSizePix = textSizePixDefault;
		parseEquation();
		setSize();
	}

	/** Draws the current layout to the supplied canvas **/
	public void draw(Canvas c) {
		// TODO draw the equation
	}

	private void parseEquation() {
		attributes.clear();
		equationText = "";
		char currentChar;

		// find the brackets in the equation
		findBrackets(equationTextFull);

		// set attributes for each character
		for (int a = 0; a < closed.size() + 1; a++) {
			int locC = 0, locO = 0;
			if (a == closed.size()) {
				locC = equationTextFull.length() - 2;
				locO = 1;
			} else {
				locC = closed.get(a).getLocation();
				locO = opened.get(closed.get(a).getBracketSet()).getLocation();
			}
			char charAfter = equationTextFull.charAt(locC + 1);
			char charBefore = equationTextFull.charAt(locO - 1);
			Att att = Att.Normal;
			if (charBefore == '\\')
				charBefore = equationTextFull.charAt(locO - 2);
			if (charBefore == '_')
				att = Att.Subscript;
			else if (charBefore == '^')
				att = Att.Superscript;
			else if (charBefore == '/')
				att = Att.Denominator;
			else if (charBefore == '√')
				att = Att.SquareRoot;
			else if (charAfter == '/')
				att = Att.Numerator;
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
							attributes.get(b).addAttribute(Att.Numerator);
						else
							attributes.get(b).addAttribute(Att.Normal);
					} else {
						charBefore = equationTextFull.charAt(b - 1);
						if ((currentChar == '(') || (currentChar == '\\')) {
							ignore = true;
							needs = 1;
						} else if (charBefore == '_')
							attributes.get(b).addAttribute(Att.Subscript);
						else if (charBefore == '^')
							attributes.get(b).addAttribute(Att.Superscript);
						else if (charBefore == '/')
							attributes.get(b).addAttribute(Att.Denominator);
						else if (charBefore == '√')
							attributes.get(b).addAttribute(Att.SquareRoot);
						else if (charAfter == '/')
							attributes.get(b).addAttribute(Att.Numerator);
						else
							attributes.get(b).addAttribute(Att.Normal);
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
		char previousChar = '$';
		for (int i = equationTextFull.length() - 1; i >= 0; i--) {
			currentChar = equationTextFull.charAt(i);
			if (currentChar == '\\') {
				equationText = previousChar + equationText;
			} else if (!((currentChar == '$') || (currentChar == '_') || (currentChar == '^') || (currentChar == '(')
					|| (currentChar == '[') || (currentChar == '{') || (currentChar == ')') || (currentChar == ']') || (currentChar == '}')))
				equationText = currentChar + equationText;
			else {
				attributes.remove(i);
				bracketGroup.remove(i);
			}
			previousChar = currentChar;
		}

		// find only the displayed brackets
		findBracketsOnly(equationText);
	}

	private void findBrackets(String s) {
		opened.clear();
		closed.clear();
		char currentChar;
		int set = 0;
		List<Integer> bSet = new ArrayList<Integer>();
		bSet.add(set);
		int currentSet = 0;
		for (int i = 0; i < s.length(); i++) {
			currentChar = s.charAt(i);
			attributes.add(new Attributes(Att.Normal));
			if (currentChar == '\\') {
				attributes.get(i).addAttribute(Att.Bracket);
				i++;
				currentChar = s.charAt(i);
				attributes.add(new Attributes(Att.Normal));
				if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
					set++;
					attributes.get(i).addAttribute(Att.Bracket);
					opened.add(new Bracket(i, set));
					bSet.add(set);
					// set++;
					currentSet = set;
					bracketGroup.add(currentSet);
					bracketGroup.add(currentSet);
				} else if ((currentChar == ')') || (currentChar == ']') || (currentChar == '}')) {
					attributes.get(i).addAttribute(Att.Bracket);
					closed.add(new Bracket(i, bSet.get(bSet.size() - 1)));
					bracketGroup.add(bSet.get(bSet.size() - 1));
					bracketGroup.add(bSet.get(bSet.size() - 1));
					bSet.remove(bSet.size() - 1);
					currentSet = bSet.get(bSet.size() - 1);
				}
			} else if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{') || (currentChar == '∫') || (currentChar == '∑')
					|| (currentChar == '∏')) {
				set++;
				attributes.get(i).addAttribute(Att.Bracket);
				opened.add(new Bracket(i, set));
				bSet.add(set);
				// set++;
				currentSet = set;
				bracketGroup.add(currentSet);
			} else if ((currentChar == ')') || (currentChar == ']') || (currentChar == '}')) {
				attributes.get(i).addAttribute(Att.Bracket);
				closed.add(new Bracket(i, bSet.get(bSet.size() - 1)));
				bracketGroup.add(bSet.get(bSet.size() - 1));
				bSet.remove(bSet.size() - 1);
				currentSet = bSet.get(bSet.size() - 1);
			} else
				bracketGroup.add(currentSet);
		}
		bracketGroups = new BracketGroup[set + 1];
	}

	private void findBracketsOnly(String s) {
		opened.clear();
		closed.clear();
		char currentChar;
		int set = 0;
		List<Integer> bSet = new ArrayList<Integer>();
		for (int i = 0; i < s.length(); i++) {
			currentChar = s.charAt(i);
			if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
				opened.add(new Bracket(i, set));
				bSet.add(set);
				set++;
			} else if ((currentChar == ')') || (currentChar == ']') || (currentChar == '}')) {
				closed.add(new Bracket(i, bSet.get(bSet.size() - 1)));
				bSet.remove(bSet.size() - 1);
			}
		}
	}

	private void setSize() {
		textAttributes.clear();

		// set text size to default, set vertical locations, and widths of characters
		for (int i = 0; i < equationText.length(); i++) {
			textAttributes.add(new TextAttributes(maxWidth / 2, maxHeight / 2, textSizePixDefault));
			for (int a = 0; a < attributes.get(i).getAttributes().size(); a++) {
				Att att = attributes.get(i).getAttributes().get(a);
				if (att.equals(Att.Subscript)) {
					textAttributes.get(i).setSizePix(textAttributes.get(i).getSizePix() / 2);
					textAttributes.get(i).setY(textAttributes.get(i).getY() + (int) (textSizePix / 2));
				} else if (att.equals(Att.Superscript)) {
					textAttributes.get(i).setSizePix(textAttributes.get(i).getSizePix() / 2);
					textAttributes.get(i).setY(textAttributes.get(i).getY() - (int) (textSizePix / 2));
				} else if (att.equals(Att.Numerator)) {
					textAttributes.get(i).setY(textAttributes.get(i).getY() - (int) (textSizePix / 2));
				} else if (att.equals(Att.Denominator)) {
					textAttributes.get(i).setY(textAttributes.get(i).getY() + (int) (textSizePix / 2));
				} else if (att.equals(Att.SquareRoot)) {
					;
				} else if (att.equals(Att.Bracket)) {
					;
				}
			}
			textAttributes.get(i).setWidth(textAttributes.get(i).getTextPaint().measureText(String.valueOf(equationText.charAt(i))));
		}

		// set size of brackets
		for (int a = 0; a < closed.size(); a++) {
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();

			int top = maxHeight / 2, bottom = maxHeight / 2;
			for (int b = locO; b < locC; b++) {
				int topTemp = (int) (textAttributes.get(b).getY() - textAttributes.get(b).getSizePix());
				int bottomTemp = (int) (textAttributes.get(b).getY() + textAttributes.get(b).getSizePix());
				if (topTemp < top)
					top = topTemp;
				if (bottomTemp > bottom)
					bottom = bottomTemp;
			}
			textAttributes.get(locO).setSizePix(bottom - top);
			textAttributes.get(locC).setSizePix(bottom - top);
		}

	}
}
