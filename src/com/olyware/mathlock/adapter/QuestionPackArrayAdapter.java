package com.olyware.mathlock.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.olyware.mathlock.R;
import com.olyware.mathlock.service.CustomPackData;
import com.olyware.mathlock.ui.Typefaces;

public class QuestionPackArrayAdapter extends ArrayAdapter<CustomPackData> {

	private Context ctx;
	private int layoutResourceId;
	private ArrayList<CustomPackData> data, fitems;
	private Filter filter;
	private Typefaces fonts;

	public QuestionPackArrayAdapter(Context context, int layoutResourceId, ArrayList<CustomPackData> data) {
		super(context, layoutResourceId, data);
		ctx = context;
		this.layoutResourceId = layoutResourceId;
		this.data = data;
		this.fitems = data;
		filter = new QuestionPackFilter();
		fonts = Typefaces.getInstance(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;
		CustomPackDataHolder holder = null;
		int color = (position % 2 == 0) ? ctx.getResources().getColor(R.color.light_light_blue) : ctx.getResources()
				.getColor(R.color.white);

		if (row == null) {
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new CustomPackDataHolder();
			holder.layout = (LinearLayout) row.findViewById(R.id.pack_background);
			holder.layout.setBackgroundColor(color);
			holder.txtName = (TextView) row.findViewById(R.id.pack_name);
			holder.txtName.setTypeface(fonts.robotoLight);
			holder.txtDownloads = (TextView) row.findViewById(R.id.pack_downloads);
			holder.txtDownloads.setTypeface(fonts.robotoLight);

			row.setTag(holder);
		} else {
			holder = (CustomPackDataHolder) row.getTag();
		}

		CustomPackData customPackData = data.get(position);
		holder.layout.setBackgroundColor(color);
		holder.txtName.setText(customPackData.getName());
		holder.txtDownloads.setText(customPackData.getDownloads());

		return row;
	}

	static class CustomPackDataHolder {
		public LinearLayout layout;
		public TextView txtName;
		public TextView txtDownloads;
	}

	@Override
	public Filter getFilter() {

		if (filter == null) {
			filter = new QuestionPackFilter();
		}
		return filter;
	}

	private class QuestionPackFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			String prefix = constraint.toString().toLowerCase(Locale.ENGLISH);

			if (prefix == null || prefix.length() == 0) {
				ArrayList<CustomPackData> list = new ArrayList<CustomPackData>(data);
				results.values = list;
				results.count = list.size();
			} else {
				ArrayList<CustomPackData> list = new ArrayList<CustomPackData>(data);
				ArrayList<CustomPackData> newList = new ArrayList<CustomPackData>();
				int count = list.size();

				for (int i = 0; i < count; i++) {
					final CustomPackData customPack = list.get(i);
					final String valueName = customPack.getName().toLowerCase(Locale.ENGLISH);
					final List<String> valueTags = new ArrayList<String>();
					valueTags.addAll(customPack.getTags());

					if (valueName.contains(prefix)) {
						newList.add(customPack);
					} else {
						for (String tag : valueTags) {
							if (tag.contains(prefix)) {
								newList.add(customPack);
							}
						}
					}
				}
				results.values = newList;
				results.count = newList.size();
			}
			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			fitems = (ArrayList<CustomPackData>) results.values;
			notifyDataSetChanged();
			clear();
			int count = fitems.size();
			for (int i = 0; i < count; i++) {
				add(fitems.get(i));
				notifyDataSetInvalidated();
			}
		}
	}
}
