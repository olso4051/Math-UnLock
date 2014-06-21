package com.olyware.mathlock.utils;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.olyware.mathlock.ui.Typefaces;

public class CustomArrayAdapter<T> extends ArrayAdapter<T> {

	private int id;
	private Typefaces fonts;

	public CustomArrayAdapter(Context context, int resource, int textViewResourceId, List<T> list) {
		super(context, resource, textViewResourceId, list);
		id = textViewResourceId;
		fonts = Typefaces.getInstance(context);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		View v = super.getView(position, view, parent);
		TextView text = (TextView) v.findViewById(id);
		text.setTypeface(fonts.robotoLight);
		return v;
	}
}