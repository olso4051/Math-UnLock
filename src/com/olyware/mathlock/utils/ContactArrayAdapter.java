package com.olyware.mathlock.utils;

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
import com.olyware.mathlock.service.CustomContactData;
import com.olyware.mathlock.ui.Typefaces;

public class ContactArrayAdapter extends ArrayAdapter<CustomContactData> {

	private Context ctx;
	private int layoutResourceId;
	private ArrayList<CustomContactData> data, fitems;
	private Filter filter;
	private Typefaces fonts;

	public ContactArrayAdapter(Context context, int layoutResourceId, ArrayList<CustomContactData> data) {
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
		CustomContactDataHolder holder = null;
		int color = (position % 2 == 0) ? ctx.getResources().getColor(R.color.light_light_blue) : ctx.getResources()
				.getColor(R.color.white);

		if (row == null) {
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new CustomContactDataHolder();
			holder.layout = (LinearLayout) row.findViewById(R.id.contact_background);
			holder.layout.setBackgroundColor(color);
			holder.txtName = (TextView) row.findViewById(R.id.contact_name);
			holder.txtName.setTypeface(fonts.robotoLight);
			holder.txtSub = (TextView) row.findViewById(R.id.contact_phone);
			holder.txtSub.setTypeface(fonts.robotoLight);

			row.setTag(holder);
		} else {
			holder = (CustomContactDataHolder) row.getTag();
		}

		CustomContactData customContactData = data.get(position);
		holder.layout.setBackgroundColor(color);
		holder.txtName.setText(customContactData.getName());
		holder.txtSub.setText(customContactData.getPhone());

		return row;
	}

	static class CustomContactDataHolder {
		public LinearLayout layout;
		public TextView txtName, txtSub;
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
				ArrayList<CustomContactData> list = new ArrayList<CustomContactData>(data);
				results.values = list;
				results.count = list.size();
			} else {
				ArrayList<CustomContactData> list = new ArrayList<CustomContactData>(data);
				ArrayList<CustomContactData> newList = new ArrayList<CustomContactData>();
				int count = list.size();

				for (int i = 0; i < count; i++) {
					final CustomContactData customContact = list.get(i);
					final String valueName = customContact.getName().toLowerCase(Locale.ENGLISH);
					final List<String> valueEmails = new ArrayList<String>();
					valueEmails.addAll(customContact.getEmails());
					final List<String> valuePhones = new ArrayList<String>();
					valuePhones.addAll(customContact.getEmails());
					boolean done = false;
					if (valueName.contains(prefix)) {
						newList.add(customContact);
					} else {
						for (String tag : valueEmails) {
							if (tag.contains(prefix)) {
								done = true;
								newList.add(customContact);
								break;
							}
						}
						if (!done) {
							for (String tag : valuePhones) {
								if (tag.contains(prefix)) {
									done = true;
									newList.add(customContact);
									break;
								}
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
			fitems = (ArrayList<CustomContactData>) results.values;
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
