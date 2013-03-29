package com.olyware.mathlock.views;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.TypedValue;

import com.olyware.mathlock.MainActivity;
//import android.util.Log;

public class EquationLayout {
	private final int textSizeSPDefault = 30;
	private int maxWidth, maxHeight, diffY;
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

	private Paint testPaintWhite, testPaintBlue;

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
		// private List<Att> type = new ArrayList<Att>();
		private Att att;
		private int bracketGroup;
		private boolean Shown;

		public Attributes(Att att, boolean Shown, int bracketGroup) {
			this.Shown = Shown;
			this.bracketGroup = bracketGroup;
			// type.add(att);
			this.att = att;
		}

		public void setAtt(Att att) {
			// type.add(att);
			this.att = att;
		}

		public Att/*List<Att>*/getAtt() {
			// return type;
			return att;
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
		Normal(0), Subscript(1), Superscript(2), Numerator(3), Denominator(4), Bracket(5), BracketSub(6), BracketSuper(7);
		private int value;

		private Att(int value) {
			this.value = value;
		}
	}

	private class TextAttributes {
		private int X, Y, padHorz = 10;
		private float SizePix, Width, Height;
		private float HeightA, HeightB, HeightAg, Top, Bottom;
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
			paint.setTextAlign(Paint.Align.CENTER);
			bounds = new Rect();
			setWidthAndHeight();
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

		public float getTop() {
			return Top;
		}

		public float getBottom() {
			return Bottom;
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
			setWidthAndHeight();
		}

		public void setWidth(float Width) {
			this.Width = Width;
		}

		public void setWidthAndHeight() {
			setTopBottom();
			paint.getTextBounds(String.valueOf(text), 0, 1, bounds);
			Width = bounds.width() + padHorz;
			Height = bounds.height();
		}

		private void setTopBottom() {
			paint.getTextBounds("(", 0, 1, bounds);
			HeightB = bounds.height();
			paint.getTextBounds("A", 0, 1, bounds);
			HeightA = bounds.height();
			paint.getTextBounds("Ag", 0, 2, bounds);
			HeightAg = bounds.height();
			Top = Y - HeightB - HeightA + HeightAg;
			Bottom = Y - HeightA + HeightAg;
		}
	}

	private class BracketGroup {
		private int Width, Height, Start, End, X, Y, Bottom, Top, Parent;
		private Att att;
		private float SizePix;
		private List<Integer> children = new ArrayList<Integer>();

		public BracketGroup() {
			this.Width = 0;
			this.Height = 0;
			this.Start = -1;
			this.End = 0;
			this.X = 0;
			this.Y = 0;
			this.Bottom = 0;
			this.Top = 0;
			this.Parent = 0;
			this.att = Att.Normal;
			this.SizePix = textSizePixDefault;
		}

		public void addChildren(int child) {
			if (!children.contains(child))
				children.add(child);
		}

		public void setSize(int Bottom, int Top) {
			this.Bottom = Bottom;
			this.Top = Top;
			this.Height = Bottom - Top;
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

		public void setY(int Y) {
			this.Y = Y;
		}

		public void setParent(int parent) {
			this.Parent = parent;
		}

		public void setAtt(Att att) {
			this.att = att;
		}

		public void setSizePix(float SizePix) {
			this.SizePix = SizePix;
		}

		public List<Integer> getChildren() {
			return children;
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

		public int getY() {
			return Y;
		}

		public int getLeft() {
			return X - Width / 2;
		}

		public int getRight() {
			return X + Width / 2;
		}

		public int getTop() {
			return Bottom - Height;
		}

		public int getBottom() {
			return Bottom;
		}

		public int getParent() {
			return Parent;
		}

		public Att getAtt() {
			return att;
		}

		public float getSizePix() {
			return SizePix;
		}
	}

	public EquationLayout(String equation, int maxWidth, int maxHeight, Typeface font, int color) {
		this.equationTextFull = equation;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.font = font;
		this.color = color;

		testPaintWhite = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		testPaintWhite.setColor(color);
		testPaintWhite.setStyle(Paint.Style.STROKE);
		testPaintWhite.setStrokeWidth(2);

		testPaintBlue = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		testPaintBlue.setColor(Color.BLUE);
		testPaintBlue.setStyle(Paint.Style.STROKE);
		testPaintBlue.setStrokeWidth(2);

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
				if ((equationTextFull.charAt(i) == '/')
						&& (!((equationTextFull.charAt(i - 1) == '/') || (equationTextFull.charAt(i + 1) == '/')))) {
					c.drawLine(textAttributes.get(i).getX() - textAttributes.get(i).getWidth() / 2, textAttributes.get(i).getY() + diffY,
							textAttributes.get(i).getX() + textAttributes.get(i).getWidth() / 2, textAttributes.get(i).getY() + diffY,
							textAttributes.get(i).getTextPaint());
				} else {
					c.drawText(equationTextFull.substring(i, i + 1), textAttributes.get(i).getX(), textAttributes.get(i).getY() + diffY,
							textAttributes.get(i).getTextPaint());
					/*c.drawRect(textAttributes.get(i).getX() - textAttributes.get(i).getWidth() / 2, textAttributes.get(i).getY()
							- textAttributes.get(i).getHeight(), textAttributes.get(i).getX() + textAttributes.get(i).getWidth() / 2,
							textAttributes.get(i).getY(), testPaintWhite);*/
				}
			}
		}
		for (int i = 0; i < bracketGroups.size(); i++)
			c.drawRect(bracketGroups.get(i).getLeft(), bracketGroups.get(i).getTop() + diffY, bracketGroups.get(i).getRight(),
					bracketGroups.get(i).getBottom() + diffY, testPaintBlue);
		c.save();
	}

	private void parseEquation() {
		attributes.clear();
		// equationText = "";
		// char currentChar;

		// find the brackets in the equation and set attributes for each character
		findBrackets(equationTextFull);

		// set attributes for each bracket
		for (int a = 0; a < closed.size(); a++) {
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();

			Att att = Att.Normal;
			char charAfter, charBefore;
			if ((locO > 0) && (locC < equationTextFull.length() - 1)) {
				charAfter = equationTextFull.charAt(locC + 1);
				charBefore = equationTextFull.charAt(locO - 1);
				if (charBefore == '_')
					att = Att.Subscript;
				else if (charBefore == '^')
					att = Att.Superscript;
				else if ((charBefore == '/') && (equationTextFull.charAt(locO - 2) != '/'))
					att = Att.Denominator;
				else if ((charAfter == '/') && (equationTextFull.charAt(locC + 2) != '/'))
					att = Att.Numerator;
				else if (charBefore == ',')
					if (charAfter == ',')
						att = Att.BracketSub;
					else
						att = Att.BracketSuper;
			}
			bracketGroups.get(closed.get(a).getBracketSet()).setAtt(att);
		}
	}

	private void findBrackets(String s) {
		opened.clear();
		closed.clear();
		boolean subBracket = false;
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
				attributes.get(i).setAtt(Att.Bracket);
				attributes.get(i).setShown(false);
				i++;
				currentChar = s.charAt(i);
				attributes.add(new Attributes(Att.Normal, true, 0));
				if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
					set++;
					attributes.get(i).setAtt(Att.Bracket);
					opened.add(new Bracket(i - 1, set));
					bSet.add(set);
					currentSet = set;
					attributes.get(i - 1).setBracketGroup(currentSet);
					attributes.get(i).setBracketGroup(currentSet);
				} else if ((currentChar == ')') || (currentChar == ']') || (currentChar == '}')) {
					attributes.get(i).setAtt(Att.Bracket);
					closed.add(new Bracket(i, bSet.get(bSet.size() - 1)));
					attributes.get(i - 1).setBracketGroup(bSet.get(bSet.size() - 1));
					attributes.get(i).setBracketGroup(bSet.get(bSet.size() - 1));
					bSet.remove(bSet.size() - 1);
					currentSet = bSet.get(bSet.size() - 1);
				}
			} else if ((currentChar == '∫') || (currentChar == '∑') || (currentChar == '∏') || (currentChar == '√')) {
				attributes.get(i).setAtt(Att.Bracket);
				i++;
				currentChar = s.charAt(i);
				attributes.add(new Attributes(Att.Normal, false, 0));
				if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
					set++;
					attributes.get(i).setAtt(Att.Bracket);
					opened.add(new Bracket(i - 1, set));
					bSet.add(set);
					currentSet = set;
					attributes.get(i - 1).setBracketGroup(currentSet);
					attributes.get(i).setBracketGroup(currentSet);
				}
			} else if (currentChar == ',') {
				attributes.get(i).setAtt(Att.Bracket);
				attributes.get(i).setShown(false);
				if (subBracket) {
					closed.add(new Bracket(i - 1, bSet.get(bSet.size() - 1)));
					bSet.remove(bSet.size() - 1);
					currentSet = bSet.get(bSet.size() - 1);
				} else
					subBracket = true;
				attributes.get(i).setBracketGroup(currentSet);
				set++;
				opened.add(new Bracket(i + 1, set));
				bSet.add(set);
				currentSet = set;
			} else if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
				set++;
				attributes.get(i).setAtt(Att.Bracket);
				attributes.get(i).setShown(false);
				opened.add(new Bracket(i, set));
				bSet.add(set);
				currentSet = set;
				attributes.get(i).setBracketGroup(currentSet);
			} else if ((currentChar == ')') || (currentChar == ']') || (currentChar == '}')) {
				attributes.get(i).setAtt(Att.Bracket);
				attributes.get(i).setShown(false);
				if (subBracket) {
					closed.add(new Bracket(i - 1, bSet.get(bSet.size() - 1)));
					bSet.remove(bSet.size() - 1);
					currentSet = bSet.get(bSet.size() - 1);
					subBracket = false;
				}
				closed.add(new Bracket(i, bSet.get(bSet.size() - 1)));
				attributes.get(i).setBracketGroup(bSet.get(bSet.size() - 1));
				bSet.remove(bSet.size() - 1);
				currentSet = bSet.get(bSet.size() - 1);
			} else if ((currentChar == '$') || (currentChar == '_') || (currentChar == '^')) {
				attributes.get(i).setBracketGroup(currentSet);
				attributes.get(i).setShown(false);
			} else {
				attributes.get(i).setBracketGroup(currentSet);
				if ((i >= 1) && (i <= s.length() - 2)) {
					char charBefore = s.charAt(i - 1), charAfter = s.charAt(i + 1);
					if (charBefore == '_')
						attributes.get(i).setAtt(Att.Subscript);
					else if (charBefore == '^')
						attributes.get(i).setAtt(Att.Superscript);
					else if ((charBefore == '/') && (s.charAt(i - 2) != '/') && (s.charAt(i) != '/'))
						attributes.get(i).setAtt(Att.Denominator);
					else if ((charAfter == '/') && (s.charAt(i + 2) != '/') && (s.charAt(i) != '/'))
						attributes.get(i).setAtt(Att.Numerator);
					else if ((charAfter == '/') && (s.charAt(i) == '/'))
						attributes.get(i).setShown(false);
				}
			}
		}
		closed.add(new Bracket(s.length() - 1, 0));

		for (int i = 0; i < set + 1; i++) {
			bracketGroups.add(new BracketGroup());
		}

		for (int a = 0; a < closed.size(); a++) {
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();
			bracketGroups.get(closed.get(a).getBracketSet()).setEnd(locC);
			bracketGroups.get(closed.get(a).getBracketSet()).setStart(locO);
		}

		int currentGroup = 0;
		for (int i = 0; i < bracketGroups.size(); i++) {
			for (int a = bracketGroups.get(i).getStart(); a <= bracketGroups.get(i).getEnd(); a++) {
				currentGroup = attributes.get(a).getBracketGroup();
				if (currentGroup != i) {
					bracketGroups.get(currentGroup).setParent(i);
					bracketGroups.get(i).addChildren(currentGroup);
				}
			}
		}
		bracketGroups.get(0).setParent(0);
	}

	private void setSize() {
		textAttributes.clear();

		bracketGroups.get(0).setSizePix(textSizePix);
		bracketGroups.get(0).setY((int) ((maxHeight + textSizePix) / 2));
		for (int i = 1; i < bracketGroups.size(); i++) {
			Att att = bracketGroups.get(i).getAtt();
			float parentSizePix = bracketGroups.get(bracketGroups.get(i).getParent()).getSizePix();
			int parentY = bracketGroups.get(bracketGroups.get(i).getParent()).getY();
			if (att.equals(Att.Normal)) {
				bracketGroups.get(i).setSizePix(parentSizePix);
				bracketGroups.get(i).setY(parentY);
			} else if (att.equals(Att.Subscript)) {
				bracketGroups.get(i).setSizePix(parentSizePix / 2);
				bracketGroups.get(i).setY(parentY + (int) (parentSizePix / 4));
			} else if (att.equals(Att.Superscript)) {
				bracketGroups.get(i).setSizePix(parentSizePix / 2);
				bracketGroups.get(i).setY(parentY - (int) (parentSizePix * 3 / 4));
			} else if (att.equals(Att.Numerator)) {
				bracketGroups.get(i).setY(parentY - (int) (parentSizePix * 3 / 4));
			} else if (att.equals(Att.Denominator)) {
				bracketGroups.get(i).setY(parentY + (int) (parentSizePix * 3 / 4));
			} else if (att.equals(Att.BracketSuper)) {
				bracketGroups.get(i).setSizePix(parentSizePix / 2);
				bracketGroups.get(i).setY(parentY - (int) (parentSizePix * 3 / 4));
			} else if (att.equals(Att.BracketSub)) {
				bracketGroups.get(i).setSizePix(parentSizePix / 2);
				bracketGroups.get(i).setY(parentY + (int) (parentSizePix / 4));
			}
		}

		// set text size, vertical locations, and widths of characters relative to their parent bracket group
		for (int i = 0; i < equationTextFull.length(); i++) {
			if (attributes.get(i).getShown()) {
				float currentSizePix = bracketGroups.get(attributes.get(i).getBracketGroup()).getSizePix();
				textAttributes.add(new TextAttributes(equationTextFull.charAt(i), maxWidth / 2, bracketGroups.get(
						attributes.get(i).getBracketGroup()).getY(), currentSizePix));
				Att att = attributes.get(i).getAtt();
				if (att.equals(Att.Subscript)) {
					currentSizePix = currentSizePix / 2;
					textAttributes.get(i).setSizePix(currentSizePix);
					textAttributes.get(i).setY(textAttributes.get(i).getY() + (int) (currentSizePix / 2));
				} else if (att.equals(Att.Superscript)) {
					currentSizePix = currentSizePix / 2;
					textAttributes.get(i).setSizePix(currentSizePix);
					textAttributes.get(i).setY(textAttributes.get(i).getY() - (int) (currentSizePix * 3 / 2));
				} else if (att.equals(Att.Numerator)) {
					textAttributes.get(i).setY(textAttributes.get(i).getY() - (int) (currentSizePix * 3 / 4));
				} else if (att.equals(Att.Denominator)) {
					textAttributes.get(i).setY(textAttributes.get(i).getY() + (int) (currentSizePix * 3 / 4));
				}

				if ((equationTextFull.charAt(i) == '/')
						&& (!((equationTextFull.charAt(i - 1) == '/') || (equationTextFull.charAt(i + 1) == '/')))) {
					textAttributes.get(i).setY(textAttributes.get(i).getY() - (int) (currentSizePix / 4));
					textAttributes.get(i).getTextPaint().setStrokeWidth(3);
				}
				textAttributes.get(i).setWidthAndHeight();
			} else {
				textAttributes.add(new TextAttributes(equationTextFull.charAt(i), maxWidth / 2, bracketGroups.get(
						attributes.get(i).getBracketGroup()).getY(), 0));
				textAttributes.get(i).setWidth(0);
			}
		}

		// set size of brackets
		for (int a = 0; a < closed.size(); a++) {
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();
			int top = maxHeight, bottom = 0;
			for (int b = locO; b <= locC; b++) {
				if ((!bracketGroups.get(attributes.get(b).getBracketGroup()).getAtt().equals(Att.BracketSub) && !bracketGroups
						.get(attributes.get(b).getBracketGroup()).getAtt().equals(Att.BracketSuper))
						|| (bracketGroups.get(attributes.get(locO).getBracketGroup()).getAtt().equals(Att.BracketSuper) || bracketGroups
								.get(attributes.get(locO).getBracketGroup()).getAtt().equals(Att.BracketSub))) {
					if (attributes.get(b).getShown()
							&& ((attributes.get(b).getBracketGroup() != attributes.get(locO).getBracketGroup()) || (!attributes.get(b)
									.getAtt().equals(Att.Bracket)))) {
						int topTemp = (int) (textAttributes.get(b).getTop());
						int bottomTemp = (int) (textAttributes.get(b).getBottom());
						if (topTemp < top)
							top = topTemp;
						if (bottomTemp > bottom)
							bottom = bottomTemp;
					}
				}
			}

			if (equationTextFull.charAt(locO) == '\\')
				textAttributes.get(locO + 1).setSizePix(Math.max(textAttributes.get(locO + 1).getSizePix(), bottom - top));
			else if (attributes.get(locO).getAtt().equals(Att.Bracket) && (attributes.get(locO).getShown())) {
				textAttributes.get(locO).setSizePix(Math.max(textAttributes.get(locO).getSizePix(), bottom - top));
				bottom = (int) textAttributes.get(locO).getBottom();
				top = bottom - (int) textAttributes.get(locO).getHeight();
			}
			if (attributes.get(locC).getAtt().equals(Att.Bracket) && (attributes.get(locC).getShown())) {
				textAttributes.get(locC).setSizePix(Math.max(textAttributes.get(locC).getSizePix(), bottom - top));
				bottom = (int) textAttributes.get(locC).getBottom();
				top = bottom - (int) textAttributes.get(locC).getHeight();
			}
			bracketGroups.get(closed.get(a).getBracketSet()).setSize(bottom, top);

			testGroupPosition(bracketGroups.get(closed.get(a).getBracketSet()));
		}

		for (int a = 0; a < closed.size(); a++) {
			BracketGroup parent = bracketGroups.get(bracketGroups.get(closed.get(a).getBracketSet()).getParent());
			BracketGroup self = bracketGroups.get(closed.get(a).getBracketSet());
			Att att = self.getAtt();
			if (att.equals(Att.BracketSub)) {
				moveGroup(self, 0, (int) (parent.getBottom() - self.getTop()));
				parent.setSize(self.getBottom(), parent.getTop());
				testGroupPosition(parent);
			} else if (att.equals(Att.BracketSuper)) {
				moveGroup(self, 0, (int) (parent.getTop() - self.getBottom()));
				parent.setSize(parent.getBottom(), self.getTop());
				testGroupPosition(parent);
			}
		}

		// set width of groups
		for (int a = 0; a < closed.size(); a++) {
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();
			int Width = 0;
			for (int b = locO; b <= locC; b++) {
				if (attributes.get(b).getShown()) {
					if ((equationTextFull.charAt(b) == '/')
							&& (!((equationTextFull.charAt(b - 1) == '/') || (equationTextFull.charAt(b + 1) == '/')))) {
						int widthBefore = 0, widthAfter = 0;
						if (attributes.get(b - 1).getBracketGroup() != attributes.get(b).getBracketGroup())
							widthBefore = bracketGroups.get(attributes.get(b - 1).getBracketGroup()).getWidth();
						else
							widthBefore = (int) textAttributes.get(b - 1).getWidth();
						if (attributes.get(b + 1).getBracketGroup() != attributes.get(b).getBracketGroup()) {
							widthAfter = bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getWidth();
							b = bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getEnd();
						} else {
							widthAfter = (int) textAttributes.get(b + 1).getWidth();
							b++;
						}
						if (widthBefore < widthAfter)
							Width += (widthAfter - widthBefore);
					} else if (attributes.get(b).getBracketGroup() != closed.get(a).getBracketSet()) {
						if (bracketGroups.get(attributes.get(b).getBracketGroup()).getAtt().equals(Att.BracketSub)
								|| bracketGroups.get(attributes.get(b).getBracketGroup()).getAtt().equals(Att.BracketSuper)) {
							b = bracketGroups.get(attributes.get(b).getBracketGroup()).getEnd();
						} else {
							Width += bracketGroups.get(attributes.get(b).getBracketGroup()).getWidth();
							b = bracketGroups.get(attributes.get(b).getBracketGroup()).getEnd();
						}
					} else
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
			for (int b = locO; b <= locC; b++) {
				if (attributes.get(locO).getBracketGroup() != attributes.get(b).getBracketGroup()) {
					if (bracketGroups.get(attributes.get(b).getBracketGroup()).getAtt().equals(Att.BracketSub)
							|| bracketGroups.get(attributes.get(b).getBracketGroup()).getAtt().equals(Att.BracketSuper)) {
						BracketGroup parent = bracketGroups.get(bracketGroups.get(attributes.get(b).getBracketGroup()).getParent());
						bracketGroups.get(attributes.get(b).getBracketGroup()).setX(textAttributes.get(parent.getStart()).getX());
						b = bracketGroups.get(attributes.get(b).getBracketGroup()).getEnd();
					} else {
						X += bracketGroups.get(attributes.get(b).getBracketGroup()).getWidth() / 2;
						bracketGroups.get(attributes.get(b).getBracketGroup()).setX(X);
						X += bracketGroups.get(attributes.get(b).getBracketGroup()).getWidth() / 2;
						b = bracketGroups.get(attributes.get(b).getBracketGroup()).getEnd();
					}
				} else if (attributes.get(b).getShown()) {
					if ((equationTextFull.charAt(b) == '/')
							&& (!((equationTextFull.charAt(b - 1) == '/') || (equationTextFull.charAt(b + 1) == '/')))) {
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

						textAttributes.get(b).setWidth(Math.max(widthAfter, widthBefore));
						if (widthBefore > widthAfter) {
							if (bracketAfter) {
								if (bracketBefore) {
									X -= bracketGroups.get(attributes.get(b - 1).getBracketGroup()).getWidth() / 2;
									textAttributes.get(b).setX(X);
									bracketGroups.get(attributes.get(b + 1).getBracketGroup()).setX(X);
									X += bracketGroups.get(attributes.get(b - 1).getBracketGroup()).getWidth() / 2;
								} else {
									X -= (int) (textAttributes.get(b - 1).getWidth() / 2);
									textAttributes.get(b).setX(X);
									bracketGroups.get(attributes.get(b + 1).getBracketGroup()).setX(X);
									X += (int) (textAttributes.get(b - 1).getWidth() / 2);
								}
							} else {
								if (bracketBefore) {
									X -= bracketGroups.get(attributes.get(b - 1).getBracketGroup()).getWidth() / 2;
									textAttributes.get(b).setX(X);
									textAttributes.get(b + 1).setX(X);
									X += bracketGroups.get(attributes.get(b - 1).getBracketGroup()).getWidth() / 2;
								} else {
									X -= (int) (textAttributes.get(b - 1).getWidth() / 2);
									textAttributes.get(b).setX(X);
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
									textAttributes.get(b).setX(X);
									bracketGroups.get(attributes.get(b + 1).getBracketGroup()).setX(X);
									X += bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getWidth() / 2;
								} else {
									X += (int) (textAttributes.get(b + 1).getWidth() / 2);
									bracketGroups.get(attributes.get(b - 1).getBracketGroup()).setX(X);
									textAttributes.get(b).setX(X);
									textAttributes.get(b + 1).setX(X);
									X += (int) (textAttributes.get(b + 1).getWidth() / 2);
								}
							} else {
								X -= (int) textAttributes.get(b - 1).getWidth();
								if (bracketAfter) {
									X += bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getWidth() / 2;
									textAttributes.get(b - 1).setX(X);
									textAttributes.get(b).setX(X);
									bracketGroups.get(attributes.get(b + 1).getBracketGroup()).setX(X);
									X += bracketGroups.get(attributes.get(b + 1).getBracketGroup()).getWidth() / 2;
								} else {
									X += (int) (textAttributes.get(b + 1).getWidth() / 2);
									textAttributes.get(b - 1).setX(X);
									textAttributes.get(b).setX(X);
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
		// group 0 will already be set so we skip it
		for (int i = 1; i < bracketGroups.size(); i++) {
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

		// set difference in Y to center equation vertically
		diffY = (maxHeight - bracketGroups.get(0).getHeight()) / 2 - (int) bracketGroups.get(0).getTop();

		return;
	}

	private void testGroupPosition(BracketGroup group) {
		Att att = group.getAtt();
		BracketGroup parent = bracketGroups.get(group.getParent());
		int parentY = parent.getY();
		float parentSizePix = parent.getSizePix();
		if (att.equals(Att.Normal)) {
			moveGroup(group, 0, (int) (parentY - group.getY()));
		} else if (att.equals(Att.Subscript)) {
			moveGroup(group, 0, (int) (parentY - group.getTop()));
		} else if (att.equals(Att.Superscript)) {
			moveGroup(group, 0, (int) (parentY - parentSizePix * 3 / 4 - group.getBottom()));
		} else if (att.equals(Att.Numerator)) {
			moveGroup(group, 0, (int) (parentY - parentSizePix / 2 - group.getBottom()));
		} else if (att.equals(Att.Denominator)) {
			moveGroup(group, 0, (int) (parentY - group.getTop()));
		}
	}

	private void moveGroup(BracketGroup group, int diffX, int diffY) {
		group.setY(group.getY() + diffY);
		group.setX(group.getX() + diffX);
		group.setSize(group.getBottom() + diffY, group.getTop() + diffY);
		List<Integer> children = group.getChildren();
		for (int i = 0; i < children.size(); i++) {
			BracketGroup child = bracketGroups.get(children.get(i));
			child.setY(child.getY() + diffY);
			child.setX(child.getX() + diffX);
			child.setSize(child.getBottom() + diffY, child.getTop() + diffY);
		}
		for (int i = group.getStart(); i <= group.getEnd(); i++) {
			if (attributes.get(i).getShown()) {
				textAttributes.get(i).setY(textAttributes.get(i).getY() + diffY);
				textAttributes.get(i).setX(textAttributes.get(i).getX() + diffX);
				textAttributes.get(i).setWidthAndHeight();
			}
		}
	}
}
