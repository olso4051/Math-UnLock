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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.olyware.mathlock.R;
import com.olyware.mathlock.service.CustomContactData;
import com.olyware.mathlock.ui.Typefaces;

public class ContactArrayAdapter extends ArrayAdapter<CustomContactData> {

	private Context ctx;
	private int contactLayoutResourceId, sectionLayoutResourceId;
	private ArrayList<CustomContactData> data, fitems;
	private Filter filter;
	private Typefaces fonts;

	public ContactArrayAdapter(Context context, int contactLayoutResourceId, int sectionLayoutResourceId, ArrayList<CustomContactData> data) {
		super(context, 0, data);
		ctx = context;
		this.contactLayoutResourceId = contactLayoutResourceId;
		this.sectionLayoutResourceId = sectionLayoutResourceId;
		this.data = data;
		this.fitems = data;
		filter = new ContactDataFilter();
		fonts = Typefaces.getInstance(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;
		CustomContactDataHolder contactHolder = null;
		CustomSectionDataHolder sectionHolder = null;
		int backgroundResourceID = (position % 2 == 0) ? (R.drawable.lv_dark) : (R.drawable.lv_light);
		CustomContactData customContactData = data.get(position);
		boolean isContact = customContactData.isContact();

		LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
		if (row == null) {
			if (isContact) {
				row = inflater.inflate(contactLayoutResourceId, parent, false);
				contactHolder = new CustomContactDataHolder();
				contactHolder.layout = (LinearLayout) row.findViewById(R.id.contact_background);
				contactHolder.layout.setBackgroundResource(backgroundResourceID);
				contactHolder.txtName = (TextView) row.findViewById(R.id.contact_name);
				contactHolder.txtName.setTypeface(fonts.robotoLight);
				contactHolder.txtName.setTextColor(ctx.getResources().getColorStateList(customContactData.getTextColorID()));
				contactHolder.txtSub = (TextView) row.findViewById(R.id.contact_phone);
				contactHolder.txtSub.setTypeface(fonts.robotoLight);
				contactHolder.txtSub.setTextColor(ctx.getResources().getColorStateList(customContactData.getTextColorID()));
				contactHolder.txtState = (TextView) row.findViewById(R.id.contact_state_text);
				contactHolder.txtState.setTypeface(fonts.robotoLight);
				contactHolder.imgState = (ImageView) row.findViewById(R.id.contact_state_image);
				row.setTag(contactHolder);
			} else {
				row = inflater.inflate(sectionLayoutResourceId, parent, false);
				sectionHolder = new CustomSectionDataHolder();
				sectionHolder.title = (TextView) row.findViewById(R.id.section_title);
				sectionHolder.title.setTypeface(fonts.robotoLight);
				sectionHolder.description = (TextView) row.findViewById(R.id.section_description);
				sectionHolder.description.setTypeface(fonts.robotoLight);
				row.setTag(sectionHolder);
			}
		} else {
			if (isContact) {
				if (row.getTag() instanceof CustomContactDataHolder)
					contactHolder = (CustomContactDataHolder) row.getTag();
				else {
					row = inflater.inflate(contactLayoutResourceId, parent, false);
					contactHolder = new CustomContactDataHolder();
					contactHolder.layout = (LinearLayout) row.findViewById(R.id.contact_background);
					contactHolder.layout.setBackgroundResource(backgroundResourceID);
					contactHolder.txtName = (TextView) row.findViewById(R.id.contact_name);
					contactHolder.txtName.setTypeface(fonts.robotoLight);
					contactHolder.txtName.setTextColor(ctx.getResources().getColorStateList(customContactData.getTextColorID()));
					contactHolder.txtSub = (TextView) row.findViewById(R.id.contact_phone);
					contactHolder.txtSub.setTypeface(fonts.robotoLight);
					contactHolder.txtSub.setTextColor(ctx.getResources().getColorStateList(customContactData.getTextColorID()));
					contactHolder.txtState = (TextView) row.findViewById(R.id.contact_state_text);
					contactHolder.txtState.setTypeface(fonts.robotoLight);
					contactHolder.imgState = (ImageView) row.findViewById(R.id.contact_state_image);
					row.setTag(contactHolder);
				}
			} else {
				if (row.getTag() instanceof CustomSectionDataHolder)
					sectionHolder = (CustomSectionDataHolder) row.getTag();
				else {
					row = inflater.inflate(sectionLayoutResourceId, parent, false);
					sectionHolder = new CustomSectionDataHolder();
					sectionHolder.title = (TextView) row.findViewById(R.id.section_title);
					sectionHolder.title.setTypeface(fonts.robotoLight);
					sectionHolder.description = (TextView) row.findViewById(R.id.section_description);
					sectionHolder.description.setTypeface(fonts.robotoLight);
					row.setTag(sectionHolder);
				}
			}
		}

		if (isContact) {
			contactHolder.layout.setBackgroundResource(backgroundResourceID);
			contactHolder.txtName.setText(customContactData.getDisplayName());
			contactHolder.txtSub.setText(customContactData.getDisplayDescription());

			contactHolder.txtState.setText(customContactData.getStateDisplayString());
			contactHolder.txtState.setTextColor(ctx.getResources().getColorStateList(customContactData.getTextColorID()));
			// contactHolder.txtState.setTextColor(ctx.getResources().getColor(customContactData.getStateTextColorID()));
			// contactHolder.txtState.setBackgroundResource(customContactData.getStateTextBackgroundResID());

			contactHolder.imgState.setImageResource(customContactData.getImageResID());
		} else {
			sectionHolder.title.setText(customContactData.getDisplayName());
			sectionHolder.description.setText(customContactData.getDescription());
		}

		return row;
	}

	static class CustomContactDataHolder {
		public LinearLayout layout;
		public TextView txtName, txtSub, txtState;
		public ImageView imgState;
	}

	static class CustomSectionDataHolder {
		public TextView title, description;
	}

	@Override
	public Filter getFilter() {

		if (filter == null) {
			filter = new ContactDataFilter();
		}
		return filter;
	}

	private class ContactDataFilter extends Filter {
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
					CustomContactData customContact = list.get(i);
					String valueName = customContact.getDisplayName().toLowerCase(Locale.ENGLISH);
					int valueSection = customContact.getSection();
					if (valueSection >= 0) {
						newList.add(customContact);
					} else if (valueName.contains(prefix)) {
						newList.add(customContact);
					} else {
						List<String> valueEmails = new ArrayList<String>();
						valueEmails.addAll(customContact.getEmails());
						boolean done = false;
						for (String tag : valueEmails) {
							if (tag.contains(prefix)) {
								done = true;
								newList.add(customContact);
								break;
							}
						}
						if (!done) {
							List<String> valuePhones = new ArrayList<String>();
							valuePhones.addAll(customContact.getPhoneNumbers());
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
