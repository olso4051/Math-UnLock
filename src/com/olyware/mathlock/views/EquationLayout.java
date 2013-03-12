package com.olyware.mathlock.views;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;

import com.olyware.mathlock.MainActivity;

public class EquationLayout {
	private final int textSizeSPDefault = 30;
	private int maxWidth, maxHeight;
	private int textSizeSP;
	private float textSizePix, textSizePixDefault;
	private String equationTextFull;// , equationText;
	private Typeface font;
	private int color;
	private List<BracketGroup> bracketGroups = new ArrayList<BracketGroup>();
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
		private int bracketGroup;
		private boolean Shown;

		public Attributes(Att attribute, boolean Shown, int bracketGroup) {
			this.Shown = Shown;
			this.bracketGroup = bracketGroup;
			type.add(attribute);
		}

		public void addAttribute(Att attribute) {
			type.add(attribute);
		}

		public List<Att> getAttributes() {
			return type;
		}

		public boolean getShown() {
			return Shown;
		}

		public int getBracketGroup() {
			return bracketGroup;
		}

		public void setShown(boolean Shown) {
			this.Shown = Shown;
		}

		public void setBracketGroup(int bracketGroup) {
			this.bracketGroup = bracketGroup;
		}
	}

	private enum Att {
		Normal(0), Subscript(1), Superscript(2), Numerator(3), Denominator(4), Bracket(5);
		private int value;

		private Att(int value) {
			this.value = value;
		}
	}

	private class TextAttributes {
		private int X, Y;
		private float SizePix, Width, Height;
		private char text;
		private TextPaint paint;
		private Rect bounds;

		public TextAttributes(char text, int X, int Y, float SizePix) {
			this.X = X;
			this.Y = Y;
			this.SizePix = SizePix;
			this.text = text;
			paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			paint.setTypeface(font);
			paint.setColor(color);
			paint.setTextSize(SizePix);
			bounds = new Rect();
			paint.getTextBounds(String.valueOf(text), 0, 1, bounds);
			Width = bounds.width();
			Height = bounds.height();
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

		public float getHeight() {
			return Height;
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
			paint.getTextBounds(String.valueOf(text), 0, 1, bounds);
			Width = bounds.width();
			Height = bounds.height();
		}

		public void setWidth(float Width) {
			this.Width = Width;
		}

		public void setWidth(char c) {
			Width = paint.measureText(String.valueOf(c));
		}
	}

	private class BracketGroup {
		private int Width, Height, Start, End, X, Parent;

		public BracketGroup() {
			this.Width = 0;
			this.Height = 0;
			this.Start = -1;
			this.End = 0;
			this.X = 0;
			this.Parent = 0;
		}

		public BracketGroup(int Width, int Height, int Start, int End, int X, int Parent) {
			this.Width = Width;
			this.Height = Height;
			this.Start = Start;
			this.End = End;
			this.X = X;
			this.Parent = Parent;
		}

		public void setWidth(int Width) {
			this.Width = Width;
		}

		public void setHeight(int Height) {
			this.Height = Height;
		}

		public void setStart(int Start) {
			this.Start = Start;
		}

		public void setEnd(int End) {
			this.End = End;
		}

		public void setX(int X) {
			this.X = X;
		}

		public void setParent(int parent) {
			this.Parent = parent;
		}

		public int getWidth() {
			return Width;
		}

		public int getHeight() {
			return Height;
		}

		public int getStart() {
			return Start;
		}

		public int getEnd() {
			return End;
		}

		public int getX() {
			return X;
		}

		public int getParent() {
			return Parent;
		}
	}

	public EquationLayout(String equation, int maxWidth, int maxHeight, Typeface font, int color) {
		this.equationTextFull = equation;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.font = font;
		this.color = color;
		textSizePixDefault = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSPDefault, MainActivity.getContext()
				.getResources().getDisplayMetrics());
		textSizeSP = textSizeSPDefault;
		textSizePix = textSizePixDefault;
		parseEquation();
		setSize();
	}

	public void setBounds(int maxWidth, int maxHeight) {
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		setSize();
	}

	/** Draws the current layout to the supplied canvas **/
	public void draw(Canvas c) {

		for (int i = 0; i < equationTextFull.length(); i++) {
			if (attributes.get(i).getShown()) {
				c.drawText(equationTextFull.substring(i, i + 1), textAttributes.get(i).getX(), textAttributes.get(i).getY(), textAttributes
						.get(i).getTextPaint());
			}
		}
		c.save();
	}

	private void parseEquation() {
		attributes.clear();
		// equationText = "";
		char currentChar;

		// find the brackets in the equation
		findBrackets(equationTextFull);

		// set attributes for each character
		for (int a = 0; a < closed.size(); a++) {
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();
			bracketGroups.get(closed.get(a).getBracketSet()).setEnd(locC);
			Log.d("test", "bracketGroup = " + closed.get(a).getBracketSet());
			Log.d("test", "start = " + locO);
			bracketGroups.get(closed.get(a).getBracketSet()).setStart(locO);
			Att att = Att.Normal;
			char charAfter, charBefore;
			if ((locO > 0) && (locC < equationTextFull.length() - 1)) {
				charAfter = equationTextFull.charAt(locC + 1);
				charBefore = equationTextFull.charAt(locO - 1);
				if (charBefore == '_')
					att = Att.Subscript;
				else if (charBefore == '^')
					att = Att.Superscript;
				else if (charBefore == '/')
					att = Att.Denominator;
				else if (charAfter == '/')
					att = Att.Numerator;
			}
			boolean ignore = false;
			int needs = 0;
			int start = locO + 1, end = locC - 1;
			if (equationTextFull.charAt(locO) == '\\')
				start++;
			if (equationTextFull.charAt(locC) == '\\')
				end--;
			for (int b = locO; b < locC; b++) {
				attributes.get(b).addAttribute(att);
				if ((b >= start) && (b <= end)) {
					charAfter = equationTextFull.charAt(b + 1);
					currentChar = equationTextFull.charAt(b);
					if (!ignore) {
						if (b == start) {
							if (currentChar == '(') {
								ignore = true;
								needs = 1;
							} else if (currentChar == '\\') {
								ignore = true;
								needs = 1;
								b++;
								attributes.get(b).addAttribute(att);
							} else if (charAfter == '/')
								attributes.get(b).addAttribute(Att.Numerator);
							else
								attributes.get(b).addAttribute(Att.Normal);
						} else {
							charBefore = equationTextFull.charAt(b - 1);
							if (currentChar == '(') {
								ignore = true;
								needs = 1;
							} else if (currentChar == '\\') {
								ignore = true;
								needs = 1;
								b++;
								attributes.get(b).addAttribute(att);
							} else if (charBefore == '_')
								attributes.get(b).addAttribute(Att.Subscript);
							else if (charBefore == '^')
								attributes.get(b).addAttribute(Att.Superscript);
							else if (charBefore == '/')
								attributes.get(b).addAttribute(Att.Denominator);
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
		}

		/*for (int i = 0; i < equationTextFull.length(); i++) {
			if (bracketGroups.get(attributes.get(i).getBracketGroup()).getStart() < 0)
				bracketGroups.get(attributes.get(i).getBracketGroup()).setStart(i);
			else
				bracketGroups.get(attributes.get(i).getBracketGroup()).setEnd(i);
		}*/

		int currentGroup = 0;
		for (int i = 0; i < bracketGroups.size(); i++) {
			for (int a = bracketGroups.get(i).getStart(); a <= bracketGroups.get(i).getEnd(); a++) {
				currentGroup = attributes.get(a).getBracketGroup();
				if (currentGroup != i)
					bracketGroups.get(currentGroup).setParent(i);
			}
		}
	}

	private void findBrackets(String s) {
		opened.clear();
		closed.clear();
		char currentChar;
		int set = 0;
		List<Integer> bSet = new ArrayList<Integer>();
		bSet.add(set);
		opened.add(new Bracket(0, 0));
		int currentSet = 0;
		for (int i = 0; i < s.length(); i++) {
			currentChar = s.charAt(i);
			attributes.add(new Attributes(Att.Normal, true, 0));
			if (currentChar == '\\') {
				attributes.get(i).addAttribute(Att.Bracket);
				attributes.get(i).setShown(false);
				i++;
				currentChar = s.charAt(i);
				attributes.add(new Attributes(Att.Normal, true, 0));
				if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
					set++;
					attributes.get(i).addAttribute(Att.Bracket);
					opened.add(new Bracket(i - 1, set));
					bSet.add(set);
					currentSet = set;
					attributes.get(i - 1).setBracketGroup(currentSet);
					attributes.get(i).setBracketGroup(currentSet);
				} else if ((currentChar == ')') || (currentChar == ']') || (currentChar == '}')) {
					attributes.get(i).addAttribute(Att.Bracket);
					closed.add(new Bracket(i, bSet.get(bSet.size() - 1)));
					attributes.get(i - 1).setBracketGroup(bSet.get(bSet.size() - 1));
					attributes.get(i).setBracketGroup(bSet.get(bSet.size() - 1));
					bSet.remove(bSet.size() - 1);
					currentSet = bSet.get(bSet.size() - 1);
				}
			} else if ((currentChar == '∫') || (currentChar == '∑') || (currentChar == '∏') || (currentChar == '√')) {
				attributes.get(i).addAttribute(Att.Bracket);
				i++;
				currentChar = s.charAt(i);
				attributes.add(new Attributes(Att.Normal, false, 0));
				if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
					set++;
					attributes.get(i).addAttribute(Att.Bracket);
					opened.add(new Bracket(i - 1, set));
					bSet.add(set);
					currentSet = set;
					attributes.get(i - 1).setBracketGroup(currentSet);
					attributes.get(i).setBracketGroup(currentSet);
				}
			} else if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
				set++;
				attributes.get(i).addAttribute(Att.Bracket);
				attributes.get(i).setShown(false);
				opened.add(new Bracket(i, set));
				bSet.add(set);
				currentSet = set;
				attributes.get(i).setBracketGroup(currentSet);
			} else if ((currentChar == ')') || (currentChar == ']') || (currentChar == '}')) {
				attributes.get(i).addAttribute(Att.Bracket);
				attributes.get(i).setShown(false);
				closed.add(new Bracket(i, bSet.get(bSet.size() - 1)));
				attributes.get(i).setBracketGroup(bSet.get(bSet.size() - 1));
				bSet.remove(bSet.size() - 1);
				currentSet = bSet.get(bSet.size() - 1);
			} else if ((currentChar == '$') || (currentChar == '_') || (currentChar == '^')) {
				attributes.get(i).setBracketGroup(currentSet);
				attributes.get(i).setShown(false);
			} else {
				attributes.get(i).setBracketGroup(currentSet);
			}
		}
		closed.add(new Bracket(s.length() - 1, 0));
		for (int i = 0; i < set + 1; i++) {
			bracketGroups.add(new BracketGroup());
		}
	}

	private void setSize() {
		textAttributes.clear();

		// set text size, vertical locations, and widths of characters
		for (int i = 0; i < equationTextFull.length(); i++) {
			if (attributes.get(i).getShown()) {
				textAttributes.add(new TextAttributes(equationTextFull.charAt(i), maxWidth / 2, maxHeight / 2, textSizePix));
				int currentSizePix = (int) textSizePix;
				for (int a = attributes.get(i).getAttributes().size() - 1; a >= 0; a--) {
					Att att = attributes.get(i).getAttributes().get(a);
					if (att.equals(Att.Subscript)) {
						currentSizePix = currentSizePix / 2;
						textAttributes.get(i).setSizePix(currentSizePix);
						textAttributes.get(i).setY(textAttributes.get(i).getY() + currentSizePix);
					} else if (att.equals(Att.Superscript)) {
						currentSizePix = currentSizePix / 2;
						textAttributes.get(i).setSizePix(currentSizePix);
						textAttributes.get(i).setY(textAttributes.get(i).getY() - currentSizePix);
					} else if (att.equals(Att.Numerator)) {
						textAttributes.get(i).setY(textAttributes.get(i).getY() - currentSizePix / 2);
					} else if (att.equals(Att.Denominator)) {
						textAttributes.get(i).setY(textAttributes.get(i).getY() + currentSizePix / 2);
					}
				}
				textAttributes.get(i).setWidth(equationTextFull.charAt(i));
			} else {
				textAttributes.add(new TextAttributes(equationTextFull.charAt(i), maxWidth / 2, maxHeight / 2, 0));
				textAttributes.get(i).setWidth(0);
			}
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
			bracketGroups.get(closed.get(a).getBracketSet()).setHeight(bottom - top);
			textAttributes.get(locO).setSizePix(bottom - top);
			textAttributes.get(locC).setSizePix(bottom - top);
		}

		// set width of groups
		for (int a = 0; a < closed.size(); a++) {
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();
			int Width = 0;
			for (int b = locO; b < locC; b++) {
				if (attributes.get(b).getShown()) {
					if (equationTextFull.charAt(b) == '/') {
						int widthBefore = 0, widthAfter = 0;
						if (attributes.get(b - 1).getBracketGroup() != attributes.get(b).getBracketGroup())
							widthBefore = bracketGroups.get(attributes.get(b - 1).getBracketGroup()).getWidth();
						else
							widthBefore = (int) textAttributes.get(b - 1).getWidth();
						if (attributes.get(b + 1).getBracketGroup() != attributes.get(b).getBracketGroup()) {
							widthAfter = bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getWidth();
							b = bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getEnd();
						} else
							widthAfter = (int) textAttributes.get(b + 1).getWidth();
						if (widthBefore < widthAfter)
							Width += (widthAfter - widthBefore);
					} else if (attributes.get(b).getBracketGroup() != closed.get(a).getBracketSet()) {
						Width += bracketGroups.get(attributes.get(b).getBracketGroup()).getWidth();
						b = bracketGroups.get(attributes.get(b).getBracketGroup()).getEnd();
					}
					Width += textAttributes.get(b).getWidth();
				}
			}
			bracketGroups.get(closed.get(a).getBracketSet()).setWidth(Width);
		}

		// check equation fits within bounds
		if ((maxWidth > 0) && (maxHeight > 0))
			if ((bracketGroups.get(0).getWidth() > maxWidth) || (bracketGroups.get(0).getHeight() > maxHeight)) {
				textSizeSP -= 5;
				textSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, MainActivity.getContext().getResources()
						.getDisplayMetrics());
				setSize();
				return;
			}

		// set X position of every character, or group relative to it's parent group (X=0 is left side of parent)
		for (int a = 0; a < closed.size(); a++) {
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();
			int X = 0;
			for (int b = locO + 1; b < locC; b++) {
				if (attributes.get(locO).getBracketGroup() != attributes.get(b).getBracketGroup()) {
					X += bracketGroups.get(attributes.get(b).getBracketGroup()).getWidth() / 2;
					bracketGroups.get(attributes.get(b).getBracketGroup()).setX(X);
					X += bracketGroups.get(attributes.get(b).getBracketGroup()).getWidth() / 2;
					b = bracketGroups.get(attributes.get(b).getBracketGroup()).getEnd();
				} else if (attributes.get(b).getShown()) {
					if (equationTextFull.charAt(b) == '/') {
						int widthBefore = 0, widthAfter = 0;
						boolean bracketBefore = (attributes.get(b - 1).getBracketGroup() != attributes.get(b).getBracketGroup());
						boolean bracketAfter = (attributes.get(b + 1).getBracketGroup() != attributes.get(b).getBracketGroup());
						int newB = 0;
						if (bracketBefore)
							widthBefore = bracketGroups.get(attributes.get(b - 1).getBracketGroup()).getWidth();
						else
							widthBefore = (int) textAttributes.get(b - 1).getWidth();
						if (bracketAfter) {
							widthAfter = bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getWidth();
							newB = bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getEnd();
						} else {
							widthAfter = (int) textAttributes.get(b + 1).getWidth();
							newB = b + 1;
						}

						if (widthBefore > widthAfter) {
							if (bracketAfter) {
								if (bracketBefore) {
									X -= bracketGroups.get(attributes.get(b - 1).getBracketGroup()).getWidth() / 2;
									bracketGroups.get(attributes.get(b + 1).getBracketGroup()).setX(X);
									X += bracketGroups.get(attributes.get(b - 1).getBracketGroup()).getWidth() / 2;
								} else {
									X -= (int) (textAttributes.get(b - 1).getWidth() / 2);
									bracketGroups.get(attributes.get(b + 1).getBracketGroup()).setX(X);
									X += (int) (textAttributes.get(b - 1).getWidth() / 2);
								}
							} else {
								if (bracketBefore) {
									X -= bracketGroups.get(attributes.get(b - 1).getBracketGroup()).getWidth() / 2;
									textAttributes.get(b + 1).setX(X);
									X += bracketGroups.get(attributes.get(b - 1).getBracketGroup()).getWidth() / 2;
								} else {
									X -= (int) (textAttributes.get(b - 1).getWidth() / 2);
									textAttributes.get(b + 1).setX(X);
									X += (int) (textAttributes.get(b - 1).getWidth() / 2);
								}
							}
						} else {
							if (bracketBefore) {
								X -= bracketGroups.get(attributes.get(b - 1).getBracketGroup()).getWidth();
								if (bracketAfter) {
									X += bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getWidth() / 2;
									bracketGroups.get(attributes.get(b - 1).getBracketGroup()).setX(X);
									bracketGroups.get(attributes.get(b + 1).getBracketGroup()).setX(X);
									X += bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getWidth() / 2;
								} else {
									X += (int) (textAttributes.get(b + 1).getWidth() / 2);
									bracketGroups.get(attributes.get(b - 1).getBracketGroup()).setX(X);
									textAttributes.get(b + 1).setX(X);
									X += (int) (textAttributes.get(b + 1).getWidth() / 2);
								}
							} else {
								X -= (int) textAttributes.get(b - 1).getWidth();
								if (bracketAfter) {
									X += bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getWidth() / 2;
									textAttributes.get(b - 1).setX(X);
									bracketGroups.get(attributes.get(b + 1).getBracketGroup()).setX(X);
									X += bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getWidth() / 2;
								} else {
									X += (int) (textAttributes.get(b + 1).getWidth() / 2);
									textAttributes.get(b - 1).setX(X);
									textAttributes.get(b + 1).setX(X);
									X += (int) (textAttributes.get(b + 1).getWidth() / 2);
								}
							}
						}
						b = newB;
					} else {
						X += (int) (textAttributes.get(b).getWidth() / 2);
						textAttributes.get(b).setX(X);
						X += (int) (textAttributes.get(b).getWidth() / 2);
					}
				}
			}
		}
		bracketGroups.get(0).setX(maxWidth / 2);

		// set each groups final X position
		// group 0 and 1 will already be set so we skip them
		for (int i = 2; i < bracketGroups.size(); i++) {
			int parentX = bracketGroups.get(bracketGroups.get(i).getParent()).getX();
			int parentWidth = bracketGroups.get(bracketGroups.get(i).getParent()).getWidth();
			int currentX = bracketGroups.get(i).getX();
			bracketGroups.get(i).setX(parentX - parentWidth / 2 + currentX);
		}

		// set each characters final X position
		for (int i = 0; i < equationTextFull.length(); i++) {
			int parentX = bracketGroups.get(attributes.get(i).getBracketGroup()).getX();
			int parentWidth = bracketGroups.get(attributes.get(i).getBracketGroup()).getWidth();
			int currentX = textAttributes.get(i).getX();
			textAttributes.get(i).setX(parentX - parentWidth / 2 + currentX);
		}

		return;
	}
}
