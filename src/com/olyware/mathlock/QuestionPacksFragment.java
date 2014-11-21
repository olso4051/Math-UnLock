package com.olyware.mathlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.olyware.mathlock.database.DatabaseManager;

public class QuestionPacksFragment extends Fragment {
	private DatabaseManager dbManager;
	private String[] unlockSubPackageKeys;
	private String[] pacakageKeys;

	private class OpenDatabase extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			publishProgress(0);
			dbManager = new DatabaseManager(getActivity().getApplicationContext());
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void v) {
			addCustomPreferences();
			super.onPostExecute(null);
		}
	}

	ListView listView;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_packs, null);
		unlockSubPackageKeys = getResources().getStringArray(R.array.unlock_sub_package_keys);
		pacakageKeys = getResources().getStringArray(R.array.enable_package_keys);
		listView = (ListView) view.findViewById(R.id.listViewPacks);
		listView.setDivider(null);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		new OpenDatabase().execute();
	}

	public void addCustomPreferences() {
		if (dbManager != null) {

			List<String> categories = dbManager.getAllCustomCategories();
			String[] customPacks = getResources().getStringArray(R.array.custom_packs);
			List<String> customCats = Arrays.asList(customPacks);
			SharedPreferences sharedPrefsMoney = getActivity().getSharedPreferences("Packages", 0);
			ArrayList<ListEntity> entities = new ArrayList<QuestionPacksFragment.ListEntity>();
			for (String cat : categories) {
				ListEntity entity = new ListEntity();
				// pref.setLayoutResource(R.layout.preference_layout);
				entity.setKey(getString(R.string.custom_enable) + cat);
				entity.setTitle(getString(R.string.enable_custom_summary) + " " + cat);
				// entity.setSummary();
				entity.setDefaultValue(false);
				if (!customCats.contains(cat) || sharedPrefsMoney.getBoolean(unlockSubPackageKeys[6], false)) {
					entity.setEnabled(true);
				} else {
					entity.setEnabled(false);
				}
				entities.add(entity);
			}
			listView.setAdapter(new PackageListAdaper(entities));

		}

	}

	class PackageListAdaper extends BaseAdapter {
		private List<ListEntity> entities;
		private List<Integer> colors = new ArrayList<Integer>();

		public PackageListAdaper(List<ListEntity> e) {
			entities = e;
			colors.clear();
			colors.add(getResources().getColor(R.color.list1));
			colors.add(getResources().getColor(R.color.list2));
			colors.add(getResources().getColor(R.color.list3));
			colors.add(getResources().getColor(R.color.list4));
			colors.add(getResources().getColor(R.color.list5));

		}

		@Override
		public int getCount() {
			return entities.size();
		}

		@Override
		public Object getItem(int arg0) {
			return entities.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View view = getLayoutInflater(getArguments()).inflate(R.layout.item_question_pack, null);

			TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
			txtTitle.setText(entities.get(arg0).getTitle());

			txtTitle = (TextView) view.findViewById(R.id.txtSummary);
			if (TextUtils.isEmpty(entities.get(arg0).getSummary())) {
				txtTitle.setVisibility(View.GONE);
			} else {
				txtTitle.setVisibility(View.VISIBLE);
				txtTitle.setText(entities.get(arg0).getSummary());
			}

			ImageView imageView = (ImageView) view.findViewById(R.id.selected);
			txtTitle = (TextView) view.findViewById(R.id.txtOnOff);

			if (entities.get(arg0).getEnabled()) {
				imageView.setBackgroundResource(R.drawable.oval_selected);
				txtTitle.setText("ON");
			} else {
				imageView.setBackgroundResource(R.drawable.oval_unselected);
				txtTitle.setText("OFF");
			}

			view.setBackgroundColor(colors.get(arg0 % 5));

			return view;
		}
	}

	class ListEntity {
		private String title;
		private String key;
		private String summury;
		private boolean defaultvalue;
		private boolean isEnabled;

		public boolean getEnabled() {
			return isEnabled;
		}

		public void setEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getSummary() {
			return summury;
		}

		public void setSummary(String summury) {
			this.summury = summury;
		}

		public boolean getDefaultValue() {
			return defaultvalue;
		}

		public void setDefaultValue(boolean defaultvalue) {
			this.defaultvalue = defaultvalue;
		}

		public ListEntity() {
		}
	}

}
