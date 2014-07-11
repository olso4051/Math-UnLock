package com.olyware.mathlock.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.olyware.mathlock.R;
import com.olyware.mathlock.ui.Typefaces;

public class QuestionSelectAdapter extends ArrayAdapter<QuestionSelectData> {

	private Context ctx;
	private int layoutResourceId;
	private List<QuestionSelectData> data;
	private Typefaces fonts;

	public QuestionSelectAdapter(Context context, int layoutResourceId, List<QuestionSelectData> data) {
		super(context, layoutResourceId, data);
		ctx = context;
		this.layoutResourceId = layoutResourceId;
		this.data = data;
		if (data.size() > 1) {
			if (data.get(0).isChecked()) {
				for (int i = 0; i < data.size(); i++) {
					data.get(i).setChecked(true);
				}
			}
		}
		fonts = Typefaces.getInstance(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;
		CustomPackDataHolder holder = null;
		int backgroundResourceID = (position % 2 == 0) ? (R.drawable.lv_dark) : (R.drawable.lv_light);

		if (row == null) {
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new CustomPackDataHolder();
			holder.layout = (LinearLayout) row.findViewById(R.id.question_pack_background);
			holder.layout.setBackgroundResource(backgroundResourceID);
			holder.txtName = (TextView) row.findViewById(R.id.question_pack_name);
			holder.txtName.setTypeface(fonts.robotoLight);
			holder.check = (CheckBox) row.findViewById(R.id.question_pack_checkbox);
			holder.check.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					QuestionSelectData questionPack = (QuestionSelectData) cb.getTag();
					questionPack.setChecked(cb.isChecked());
					if (questionPack.getID() == 0) {
						if (data.size() > 1) {
							if (questionPack.isChecked()) {
								for (int i = 0; i < data.size(); i++) {
									data.get(i).setChecked(true);
								}
								notifyDataSetChanged();
							}
						}
					}
				}
			});

			row.setTag(holder);
		} else {
			holder = (CustomPackDataHolder) row.getTag();
		}

		QuestionSelectData questionPack = data.get(position);
		holder.layout.setBackgroundResource(backgroundResourceID);
		holder.txtName.setText(questionPack.getName());
		holder.check.setChecked(questionPack.isChecked());
		holder.check.setTag(questionPack);

		return row;
	}

	static class CustomPackDataHolder {
		public LinearLayout layout;
		public TextView txtName;
		public CheckBox check;
	}
}
