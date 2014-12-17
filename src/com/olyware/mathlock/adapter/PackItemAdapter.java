package com.olyware.mathlock.adapter;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.olyware.mathlock.R;
import com.olyware.mathlock.model.PackItem;
import com.olyware.mathlock.ui.Typefaces;

public class PackItemAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	ArrayList<PackItem> packItems;
	ArrayList<Integer> colorList = new ArrayList<Integer>();

	public PackItemAdapter(LayoutInflater infl, ArrayList<PackItem> list, ArrayList<Integer> color) {
		inflater = infl;
		packItems = (ArrayList<PackItem>) list.clone();
		colorList = color;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return packItems.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return packItems.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {

		View view = inflater.inflate(R.layout.item_question_pack, null);
		TextView text = (TextView) view.findViewById(R.id.txtTitle);
		text.setText(packItems.get(arg0).getTitle());

		text.setTypeface(Typefaces.getInstance(text.getContext()).avenirnext);

		text = (TextView) view.findViewById(R.id.txtSummary);
		text.setText(packItems.get(arg0).getSummury());

		text.setTypeface(Typefaces.getInstance(text.getContext()).avenirnext);

		text = (TextView) view.findViewById(R.id.txtOnOff);
		text.setText(packItems.get(arg0).getTextToShow());

		text.setTypeface(Typefaces.getInstance(text.getContext()).avenirnext);

		ImageView imageView = (ImageView) view.findViewById(R.id.selected);
		if (packItems.get(arg0).isPurchased() && packItems.get(arg0).isEnabled())
			imageView.setImageResource(R.drawable.oval_selected);
		else
			imageView.setImageResource(R.drawable.oval_unselected);

		view.setTag(arg0);
		view.setBackgroundColor(colorList.get(arg0 % 5));

		return view;
	}
}
