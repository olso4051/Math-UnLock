package com.olyware.mathlock;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

import com.olyware.mathlock.service.CustomPackData;
import com.olyware.mathlock.service.GetCustomPacks;
import com.olyware.mathlock.utils.CustomArrayAdapter;

public class SearchableCustomPackActivity extends Activity {
	private ListView lv;
	CustomArrayAdapter<String> adapter;
	List<String> products;
	EditText inputSearch;
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom_pack);

		progressDialog = ProgressDialog.show(this, "", "Starting Facebook", true);
		new GetCustomPacks(this) {
			@Override
			protected void onPostExecute(Integer result) {
				ArrayList<CustomPackData> customPackData = getCustomPackList();
				products.clear();
				for (int i = 0; i < customPackData.size(); i++) {
					products.add(customPackData.get(i).getName());
				}
				adapter = new CustomArrayAdapter<String>(SearchableCustomPackActivity.this, R.layout.list_custom_pack_item, R.id.pack_name,
						products);
				adapter.notifyDataSetChanged();
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
			}
		}.execute();

		// Listview Data
		products = new ArrayList<String>();
		products.clear();
		products.add("Finding Available Packs");

		lv = (ListView) findViewById(R.id.list_view);
		inputSearch = (EditText) findViewById(R.id.custom_pack_search);

		// Adding items to listview
		adapter = new CustomArrayAdapter<String>(this, R.layout.list_custom_pack_item, R.id.pack_name, products);
		lv.setAdapter(adapter);

		/**
		 * Enabling Search Filter
		 * */
		inputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				// When user changed the Text
				adapter.getFilter().filter(cs);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	protected void onStop() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		super.onStop();
	}
}
