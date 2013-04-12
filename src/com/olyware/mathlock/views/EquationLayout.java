package com.olyware.mathlock.views;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.TypedValue;

import com.olyware.mathlock.MainActivity;

public class EquationLayout {
	private final int extraPadding = 20;
	private int textSizeSPDefault = 40;
	private int maxWidth, maxHeight;
	private int textSizeSP;
	private float textSizePix, textSizePixDefault;
	private String equationText;
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
		private Att att;
		private int bracketGroup;
		private boolean Shown;

		public Attributes(Att att, boolean Shown, int bracketGroup) {
			this.Shown = Shown;
			this.bracketGroup = bracketGroup;
			this.att = att;
		}

		public void setAtt(Att att) {
			this.att = att;
		}

		public Att getAtt() {
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
		Normal, Subscript, Superscript, Numerator, Denominator, Bracket, BracketSub, BracketSuper, SquareRoot, Abs, Limit;
	}

	private class TextAttributes {
		private int X, Y, padHorz = 10;
		private float SizePix, Width, Height;
		private float HeightA, HeightB, HeightAg, Top, Bottom;
		private String text;
		private TextPaint paint;
		private Rect bounds;

		public TextAttributes(String text, int X, int Y, float SizePix) {
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

		public float getLeft() {
			return X - Width / 2;
		}

		public float getRight() {
			return X + Width / 2;
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

		public String getText() {
			return text;
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
			setTopBottom();
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
			paint.getTextBounds(text, 0, text.length(), bounds);
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
		private int Width, Height, Start, End, X, dX, Y, Bottom, Parent;
		private Att att, modifier;
		private float SizePix;
		private Path path;
		private List<Integer> children = new ArrayList<Integer>();

		public BracketGroup() {
			this.Width = 0;
			this.Height = 0;
			this.Start = -1;
			this.End = 0;
			this.X = 0;
			this.dX = 0;
			this.Y = 0;
			this.Bottom = 0;
			this.Parent = 0;
			this.att = Att.Normal;
			this.modifier = Att.Normal;
			this.SizePix = textSizePixDefault;
			this.path = new Path();
		}

		public void resetSize() {
			this.Width = 0;
			this.Height = 0;
			this.X = 0;
			this.dX = 0;
			this.Y = 0;
			this.Bottom = 0;
			this.SizePix = textSizePixDefault;
		}

		public void addChildren(int child) {
			if (!children.contains(child))
				children.add(child);
		}

		public void setSize(int Bottom, int Top) {
			this.Bottom = Bottom;
			this.Height = Bottom - Top;
		}

		public void setWidth(int Width) {
			this.Width = Width;
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

		public void setdX(int dX) {
			this.dX = dX;
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

		public void setModifier(Att att) {
			this.modifier = att;
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

		public int getdX() {
			return dX;
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

		public Att getModifier() {
			return modifier;
		}

		public float getSizePix() {
			return SizePix;
		}

		public Path getPath() {
			return path;
		}
	}

	public EquationLayout(String equation, int maxWidth, int maxHeight, TextPaint textPaint) {
		this.equationText = equation;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.font = textPaint.getTypeface();
		this.color = textPaint.getColor();

		testPaintWhite = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		testPaintWhite.setColor(color);
		testPaintWhite.setStyle(Paint.Style.STROKE);
		testPaintWhite.setStrokeWidth(3);

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

	public EquationLayout(String equation, int maxWidth, int maxHeight, Typeface font, int color) {
		this.equationText = equation;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.font = font;
		this.color = color;

		testPaintWhite = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		testPaintWhite.setColor(color);
		testPaintWhite.setStyle(Paint.Style.STROKE);
		testPaintWhite.setStrokeWidth(3);

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

	public void setTypeface(Typeface font) {
		this.font = font;
		setSize();
	}

	public Typeface getTypeface() {
		return font;
	}

	public int getHeight() {
		return bracketGroups.get(0).getHeight();
	}

	public void setBounds(int maxWidth, int maxHeight) {
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		setSize();
	}

	/** Draws the current layout to the supplied canvas **/
	public void draw(Canvas c) {
		c.save();
		c.translate(-maxWidth / 2, -maxHeight / 2);
		for (int i = 0; i < equationText.length(); i++) {
			if (attributes.get(i).getShown()) {
				if ((equationText.charAt(i) == '/') && ((equationText.charAt(i - 1) != '/') && (equationText.charAt(i + 1) != '/'))) {
					c.drawLine(textAttributes.get(i).getLeft(), textAttributes.get(i).getY(), textAttributes.get(i).getRight(),
							textAttributes.get(i).getY(), testPaintWhite);
				} else {
					c.drawText(textAttributes.get(i).getText(), textAttributes.get(i).getX(), textAttributes.get(i).getY(), textAttributes
							.get(i).getTextPaint());
				}
			}
		}
		// c.drawRect(0, 0, maxWidth, maxHeight, testPaintBlue);
		for (int i = 0; i < bracketGroups.size(); i++) {
			/*c.drawRect(bracketGroups.get(i).getLeft(), bracketGroups.get(i).getTop(), bracketGroups.get(i).getRight(), bracketGroups.get(i)
					.getBottom(), testPaintBlue);*/
			if (bracketGroups.get(i).getModifier().equals(Att.SquareRoot)) {
				c.drawPath(bracketGroups.get(i).getPath(), testPaintWhite);
			} else if (bracketGroups.get(i).getModifier().equals(Att.Abs)) {
				c.drawLine(bracketGroups.get(i).getLeft(), bracketGroups.get(i).getTop(), bracketGroups.get(i).getLeft(), bracketGroups
						.get(i).getBottom(), testPaintWhite);
				c.drawLine(bracketGroups.get(i).getRight(), bracketGroups.get(i).getTop(), bracketGroups.get(i).getRight(), bracketGroups
						.get(i).getBottom(), testPaintWhite);
			}
		}
		c.restore();
	}

	public void setColor(int color) {
		this.color = color;
		testPaintWhite.setColor(color);
		for (int i = 0; i < textAttributes.size(); i++) {
			textAttributes.get(i).getTextPaint().setColor(color);
		}
	}

	public void setDefaultSize(int size) {
		textSizeSPDefault = size;
		textSizePixDefault = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSPDefault, MainActivity.getContext()
				.getResources().getDisplayMetrics());
		textSizeSP = textSizeSPDefault;
		textSizePix = textSizePixDefault;
		setSize();
	}

	private void parseEquation() {
		attributes.clear();
		opened.clear();
		closed.clear();
		bracketGroups.clear();

		// find keywords and replace with corresponding characters
		findKeywords();

		// find the brackets in the equation and set attributes for each character
		findBrackets(equationText);

		// set attributes for each bracket
		for (int a = 0; a < closed.size(); a++) {
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();

			Att att = Att.Normal;
			char charAfter, charBefore;
			if ((locO > 0) && (locC < equationText.length() - 1)) {
				charAfter = equationText.charAt(locC + 1);
				charBefore = equationText.charAt(locO - 1);
				if (charBefore == '_')
					att = Att.Subscript;
				else if (charBefore == '^')
					att = Att.Superscript;
				else if ((charBefore == '/') && (equationText.charAt(locO - 2) != '/'))
					att = Att.Denominator;
				else if ((charAfter == '/') && (equationText.charAt(locC + 2) != '/'))
					att = Att.Numerator;
				else if (charBefore == ',')
					if (bracketGroups.get(attributes.get(locO - 2).getBracketGroup()).getAtt().equals(Att.BracketSub))
						att = Att.BracketSuper;
					else
						att = Att.BracketSub;
			}
			bracketGroups.get(closed.get(a).getBracketSet()).setAtt(att);
		}
	}

	private void findKeywords() {
		equationText = equationText.replaceAll("Phi", "ϕ");
		equationText = equationText.replaceAll("Theta", "θ");
		equationText = equationText.replaceAll("Pi", "π");
		equationText = equationText.replaceAll("Integrate", "∫");
		equationText = equationText.replaceAll("Sum", "∑");
		equationText = equationText.replaceAll("Product", "∏");
		equationText = equationText.replaceAll("Sqrt", "√");
		equationText = equationText.replaceAll("Abs", "|");
		equationText = equationText.replaceAll("Limit", "≐");
		equationText = equationText.replaceAll("->", "→");
		equationText = equationText.replaceAll("<-", "←");
		equationText = equationText.replaceAll("<->", "↔");
	}

	private void findBrackets(String s) {
		boolean subBracket = false;
		char currentChar;
		int set = 0;
		List<Integer> bSet = new ArrayList<Integer>();
		bSet.add(set);
		bracketGroups.add(new BracketGroup());
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
					bracketGroups.add(new BracketGroup());
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
			} else if ((currentChar == '∫') || (currentChar == '∑') || (currentChar == '∏') || (currentChar == '√') || (currentChar == '|')
					|| (currentChar == '≐')) {
				attributes.get(i).setAtt(Att.Bracket);
				bracketGroups.add(new BracketGroup());
				if ((currentChar == '√') || (currentChar == '|') || (currentChar == '≐')) {
					switch (currentChar) {
					case '√':
						attributes.get(i).setShown(false);
						bracketGroups.get(bracketGroups.size() - 1).setModifier(Att.SquareRoot);
						break;
					case '|':
						attributes.get(i).setShown(false);
						bracketGroups.get(bracketGroups.size() - 1).setModifier(Att.Abs);
						break;
					case '≐':
						bracketGroups.get(bracketGroups.size() - 1).setModifier(Att.Limit);
						break;
					}
				}
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
				bracketGroups.add(new BracketGroup());
				currentSet = set;
			} else if ((currentChar == '(') || (currentChar == '[') || (currentChar == '{')) {
				set++;
				attributes.get(i).setAtt(Att.Bracket);
				attributes.get(i).setShown(false);
				opened.add(new Bracket(i, set));
				bSet.add(set);
				bracketGroups.add(new BracketGroup());
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

		bracketGroups.get(0).resetSize();
		bracketGroups.get(0).setSizePix(textSizePix);
		bracketGroups.get(0).setY((int) ((maxHeight + textSizePix) / 2));
		for (int i = 1; i < bracketGroups.size(); i++) {
			bracketGroups.get(i).resetSize();
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
				bracketGroups.get(i).setSizePix(parentSizePix);
				bracketGroups.get(i).setY(parentY - (int) (parentSizePix * 3 / 4));
			} else if (att.equals(Att.Denominator)) {
				bracketGroups.get(i).setSizePix(parentSizePix);
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
		for (int i = 0; i < equationText.length(); i++) {
			if (attributes.get(i).getShown()) {
				String character = String.valueOf(equationText.charAt(i));
				if (equationText.charAt(i) == '≐')
					character = "lim";
				float currentSizePix = bracketGroups.get(attributes.get(i).getBracketGroup()).getSizePix();
				textAttributes.add(new TextAttributes(character, maxWidth / 2, bracketGroups.get(attributes.get(i).getBracketGroup())
						.getY(), currentSizePix));
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

				if ((equationText.charAt(i) == '/') && ((equationText.charAt(i - 1) != '/') && (equationText.charAt(i + 1) != '/'))) {
					textAttributes.get(i).setY(textAttributes.get(i).getY() - (int) (currentSizePix / 4));
					textAttributes.get(i).getTextPaint().setStrokeWidth(3);
				}
				textAttributes.get(i).setWidthAndHeight();
			} else {
				textAttributes.add(new TextAttributes(String.valueOf(equationText.charAt(i)), maxWidth / 2, bracketGroups.get(
						attributes.get(i).getBracketGroup()).getY(), 0));
				textAttributes.get(i).setWidth(0);
			}
		}

		// set size of brackets
		for (int a = 0; a < closed.size(); a++) {
			BracketGroup self = bracketGroups.get(closed.get(a).getBracketSet());
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();
			int top = maxHeight, bottom = 0;
			for (int b = locO; b <= locC; b++) {
				BracketGroup innerSelf = bracketGroups.get(attributes.get(b).getBracketGroup());
				if ((!innerSelf.getAtt().equals(Att.BracketSub) && !innerSelf.getAtt().equals(Att.BracketSuper))
						|| (self.getAtt().equals(Att.BracketSuper) || self.getAtt().equals(Att.BracketSub))) {
					if (attributes.get(b).getShown()
							&& ((attributes.get(b).getBracketGroup() != attributes.get(locO).getBracketGroup()) || (!attributes.get(b)
									.getAtt().equals(Att.Bracket)))) {
						top = Math.min(top, (int) textAttributes.get(b).getTop());
						bottom = Math.max(bottom, (int) textAttributes.get(b).getBottom());
					}
				}
			}
			List<Integer> children = self.getChildren();
			for (int i = 0; i < children.size(); i++) {
				BracketGroup child = bracketGroups.get(children.get(i));
				top = Math.min(top, child.getTop());
				bottom = Math.max(bottom, child.getBottom());
			}

			if (equationText.charAt(locO) == '\\') {
				textAttributes.get(locO + 1).setSizePix(Math.max(textAttributes.get(locO + 1).getSizePix(), bottom - top));
				int dY = (int) (top + bottom - textAttributes.get(locO + 1).getTop() - textAttributes.get(locO + 1).getBottom()) / 2;
				textAttributes.get(locO + 1).setY(textAttributes.get(locO + 1).getY() + dY);
			} else if (attributes.get(locO).getAtt().equals(Att.Bracket)
					&& (attributes.get(locO).getShown() && equationText.charAt(locO) != '≐')) {
				textAttributes.get(locO).setSizePix(Math.max(textAttributes.get(locO).getSizePix(), bottom - top));
				int dY = (int) (top + bottom - textAttributes.get(locO).getTop() - textAttributes.get(locO).getBottom()) / 2;
				textAttributes.get(locO).setY(textAttributes.get(locO).getY() + dY);
				bottom = (int) textAttributes.get(locO).getBottom();
				top = bottom - (int) textAttributes.get(locO).getHeight();
			}
			if (attributes.get(locC).getAtt().equals(Att.Bracket) && (attributes.get(locC).getShown())) {
				textAttributes.get(locC).setSizePix(Math.max(textAttributes.get(locC).getSizePix(), bottom - top));
				int dY = (int) (top + bottom - textAttributes.get(locC).getTop() - textAttributes.get(locC).getBottom()) / 2;
				textAttributes.get(locC).setY(textAttributes.get(locC).getY() + dY);
				bottom = (int) textAttributes.get(locC).getBottom();
				top = bottom - (int) textAttributes.get(locC).getHeight();
			}
			if (self.getModifier().equals(Att.SquareRoot) || self.getModifier().equals(Att.Abs)) {
				top -= extraPadding / 4;
				if (self.getModifier().equals(Att.Abs))
					bottom += extraPadding / 4;
			}

			self.setSize(bottom, top);

			testGroupPosition(self);
		}

		// set Size of brackets with sub brackets
		for (int a = 0; a < closed.size(); a++) {
			BracketGroup self = bracketGroups.get(closed.get(a).getBracketSet());
			BracketGroup parent = bracketGroups.get(self.getParent());
			Att att = self.getAtt();
			if (att.equals(Att.BracketSub)) {
				if (parent.getModifier().equals(Att.Limit))
					moveGroup(self, 0, (int) (parent.getY() - self.getTop() + extraPadding / 5));
				else
					moveGroup(self, 0, (int) (parent.getBottom() - self.getTop()));
				parent.setSize(self.getBottom(), parent.getTop());
				testGroupPosition(parent);
			} else if (att.equals(Att.BracketSuper)) {
				moveGroup(self, 0, (int) (parent.getTop() - self.getBottom()));
				parent.setSize(parent.getBottom(), self.getTop());
				testGroupPosition(parent);
			}
		}

		// move super/sub script characters
		for (int i = 3; i < equationText.length(); i++) {
			if (attributes.get(i - 2).getAtt().equals(Att.Bracket)) {
				BracketGroup parent = bracketGroups.get(attributes.get(i - 2).getBracketGroup());
				TextAttributes self = textAttributes.get(i);
				Att att = attributes.get(i).getAtt();
				if (att.equals(Att.Subscript)) {
					self.setY(self.getY() + (int) (parent.getBottom() - self.getBottom()));
				} else if (att.equals(Att.Superscript)) {
					self.setY(self.getY() + (int) (parent.getTop() - self.getTop()));
				}
			}
		}

		// move super/sub script groups
		for (int a = 0; a < closed.size(); a++) {
			BracketGroup self = bracketGroups.get(closed.get(a).getBracketSet());
			int loc = Math.max(0, self.getStart() - 2);
			if (attributes.get(loc).getAtt().equals(Att.Bracket)) {
				BracketGroup parent = bracketGroups.get(attributes.get(loc).getBracketGroup());
				Att att = self.getAtt();
				if (att.equals(Att.Subscript)) {
					moveGroup(self, 0, (int) (parent.getBottom() - self.getBottom()));
				} else if (att.equals(Att.Superscript)) {
					moveGroup(self, 0, (int) (parent.getTop() - self.getTop()));
				}
			}
		}

		// set width of groups
		setGroupWidths();

		// set X position of every character, or group relative to it's parent group (X=0 is left side of parent)
		setXposition();

		// make sure each group fits all of it's children within it's bounds
		boolean changed = false;
		for (int a = 0; a < closed.size(); a++) {
			BracketGroup self = bracketGroups.get(closed.get(a).getBracketSet());
			List<Integer> children = self.getChildren();
			for (int i = 0; i < children.size(); i++) {
				BracketGroup child = bracketGroups.get(children.get(i));
				if (child.getLeft() < self.getLeft()) {
					int X = self.getX() - (self.getLeft() - child.getLeft()) / 2;
					int dX = (self.getLeft() - child.getLeft());
					self.setWidth(self.getRight() - child.getLeft());
					self.setX(X);
					self.setdX(dX);
					changed = true;
				}
				if (child.getRight() > self.getRight()) {
					int X = self.getX() + (child.getRight() - self.getRight()) / 2;
					self.setWidth(child.getRight() - self.getLeft());
					self.setX(X);
					changed = true;
				}
				if (child.getTop() < self.getTop()) {
					self.setSize(self.getBottom(), child.getTop());
					testGroupPosition(self);
					changed = true;
				}
				if (child.getBottom() > self.getBottom()) {
					self.setSize(child.getBottom(), self.getTop());
					testGroupPosition(self);
					changed = true;
				}
			}
		}
		if (changed) {
			setGroupWidths();
			setXposition();
		}

		// check equation fits within bounds
		if ((maxWidth > 0) && (maxHeight > 0)) {
			if ((bracketGroups.get(0).getWidth() > maxWidth) || (bracketGroups.get(0).getHeight() > maxHeight)) {
				textSizeSP -= 5;
				textSizePix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSP, MainActivity.getContext().getResources()
						.getDisplayMetrics());
				setSize();
				return;
			}
		}

		// center equation vertically
		moveGroup(bracketGroups.get(0), 0, (maxHeight - bracketGroups.get(0).getHeight()) / 2 - (int) bracketGroups.get(0).getTop());

		// store paths for square roots
		for (int i = 0; i < bracketGroups.size(); i++) {
			if (bracketGroups.get(i).getModifier().equals(Att.SquareRoot)) {
				bracketGroups.get(i).getPath()
						.moveTo(bracketGroups.get(i).getLeft(), bracketGroups.get(i).getBottom() - bracketGroups.get(i).getSizePix() / 2);
				bracketGroups.get(i).getPath().lineTo(bracketGroups.get(i).getLeft() + extraPadding / 3, bracketGroups.get(i).getBottom());
				bracketGroups.get(i).getPath().lineTo(bracketGroups.get(i).getLeft() + extraPadding * 2 / 3, bracketGroups.get(i).getTop());
				bracketGroups.get(i).getPath().lineTo(bracketGroups.get(i).getRight(), bracketGroups.get(i).getTop());
				bracketGroups.get(i).getPath().lineTo(bracketGroups.get(i).getRight(), bracketGroups.get(i).getTop() + extraPadding);
			}
		}

		return;
	}

	private void setGroupWidths() {
		for (int a = 0; a < closed.size(); a++) {
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();
			int Width = 0;
			for (int b = locO; b <= locC; b++) {
				if (attributes.get(b).getShown()) {
					if ((equationText.charAt(b) == '/') && (!((equationText.charAt(b - 1) == '/') || (equationText.charAt(b + 1) == '/')))) {
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
				} else if (attributes.get(b).getBracketGroup() != closed.get(a).getBracketSet()) {
					Width += bracketGroups.get(attributes.get(b).getBracketGroup()).getWidth();
					b = bracketGroups.get(attributes.get(b).getBracketGroup()).getEnd();
				}
			}
			if (bracketGroups.get(closed.get(a).getBracketSet()).getModifier().equals(Att.SquareRoot)
					|| bracketGroups.get(closed.get(a).getBracketSet()).getModifier().equals(Att.Abs))
				Width += extraPadding;
			if (Width > bracketGroups.get(closed.get(a).getBracketSet()).getWidth())
				bracketGroups.get(closed.get(a).getBracketSet()).setWidth(Width);
		}
	}

	private void setXposition() {
		for (int a = 0; a < closed.size(); a++) {
			int locC = closed.get(a).getLocation();
			int locO = opened.get(closed.get(a).getBracketSet()).getLocation();
			int X = 0;
			if (bracketGroups.get(closed.get(a).getBracketSet()).getModifier().equals(Att.SquareRoot))
				X += extraPadding * 3 / 4;
			else if (bracketGroups.get(closed.get(a).getBracketSet()).getModifier().equals(Att.Abs))
				X += extraPadding / 2;
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
					if ((equationText.charAt(b) == '/') && (!((equationText.charAt(b - 1) == '/') || (equationText.charAt(b + 1) == '/')))) {
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
			int parentdX = bracketGroups.get(bracketGroups.get(i).getParent()).getdX();
			int parentWidth = bracketGroups.get(bracketGroups.get(i).getParent()).getWidth();
			int currentX = bracketGroups.get(i).getX();
			bracketGroups.get(i).setX(parentX - parentWidth / 2 + currentX + parentdX);
		}

		// set each characters final X position
		for (int i = 0; i < equationText.length(); i++) {
			int parentX = bracketGroups.get(attributes.get(i).getBracketGroup()).getX();
			int parentdX = bracketGroups.get(attributes.get(i).getBracketGroup()).getdX();
			int parentWidth = bracketGroups.get(attributes.get(i).getBracketGroup()).getWidth();
			int currentX = textAttributes.get(i).getX();
			textAttributes.get(i).setX(parentX - parentWidth / 2 + currentX + parentdX);
		}
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
				if (!((equationText.charAt(i) == '/') && ((equationText.charAt(i - 1) != '/') && (equationText.charAt(i + 1) != '/'))))
					textAttributes.get(i).setWidthAndHeight();
			}
		}
	}
}
