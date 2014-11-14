package com.olyware.mathlock.dialog;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.olyware.mathlock.R;

public class PreferenceListDialog extends Dialog {

	public PreferenceListDialog(Context context) {
		super(context);
	}

	public void show(int layout, String title, List<String> l, String sel, OnItemClickListener itemClickListener) {

		if (TextUtils.isEmpty(title)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		} else {
			setTitle(title);
		}
		getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		setContentView(layout);

		ListView listView = (ListView) findViewById(R.id.listView_prefs);
		listView.setAdapter(new ListArrayAdapter(l, sel));
		listView.setOnItemClickListener(itemClickListener);

		show();

	}

	class ListArrayAdapter extends BaseAdapter {
		List<String> list;
		String selected;

		public ListArrayAdapter(List<String> l, String sel) {
			list = l;
			selected = sel;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View view = getLayoutInflater().inflate(R.layout.item_preferencelist_item, null);
			TextView textView = (TextView) view.findViewById(R.id.txtListItem);
			if (list.get(arg0).equals(selected)) {
				textView.setBackgroundColor(getContext().getResources().getColor(R.color.graph_text_color_blue));
				textView.setText(list.get(arg0));
				textView.setTextColor(getContext().getResources().getColor(R.color.white));
			} else {
				textView.setBackgroundColor(getContext().getResources().getColor(android.R.color.white));
				textView.setText(list.get(arg0));
				textView.setTextColor(getContext().getResources().getColor(R.color.graph_text_color_blue));
			}
			return view;
		}

	}
}
