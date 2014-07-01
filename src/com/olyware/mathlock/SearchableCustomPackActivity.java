package com.olyware.mathlock;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.olyware.mathlock.service.CustomPackData;
import com.olyware.mathlock.service.DownloadCustomPack;
import com.olyware.mathlock.service.GetCustomPacks;
import com.olyware.mathlock.utils.QuestionPackArrayAdapter;

public class SearchableCustomPackActivity extends Activity {
	private ListView lv;
	private QuestionPackArrayAdapter adapter;
	private ArrayList<CustomPackData> products, allProducts;
	private ArrayList<String> downloadedPackIDs;
	private EditText inputSearch;
	private ProgressDialog progressDialog;
	private int lastLength = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom_pack);

		progressDialog = ProgressDialog.show(this, "", "Retreiving Available Question Packs", true);

		// search box
		inputSearch = (EditText) findViewById(R.id.custom_pack_search);
		inputSearch.setEnabled(false);
		inputSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				// When user changed the Text
				if (cs.length() > lastLength) {
					lastLength = cs.length();
					adapter.getFilter().filter(cs);
				} else {
					lastLength = cs.length();
					products.clear();
					products.addAll(allProducts);
					adapter.getFilter().filter(cs);
				}
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

		// Question Pack ListView
		lv = (ListView) findViewById(R.id.list_view);
		lv.setEnabled(false);

		// Listview Data
		products = new ArrayList<CustomPackData>();
		products.add(new CustomPackData("null", "Finding Available Packs", "-1", "-1", "0", new ArrayList<String>(0)));
		allProducts = new ArrayList<CustomPackData>();
		allProducts.addAll(products);
		downloadedPackIDs = new ArrayList<String>();
		downloadedPackIDs.addAll(getInstalledCustomPacks());

		// Adding items to listview
		adapter = new QuestionPackArrayAdapter(this, R.layout.list_custom_pack_item, products);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				final CustomPackData data = products.get(pos);

				AlertDialog.Builder builder = new AlertDialog.Builder(SearchableCustomPackActivity.this);
				builder.setTitle("").setCancelable(false);
				builder.setMessage(getString(R.string.search_custom_packs_download_message) + data.getName());
				builder.setPositiveButton(R.string.search_custom_packs_download_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						lv.setEnabled(false);
						progressDialog = ProgressDialog.show(SearchableCustomPackActivity.this, "", "Downloading " + data.getName(), true);
						new DownloadCustomPack(SearchableCustomPackActivity.this) {
							@Override
							protected void onPostExecute(Integer result) {
								lv.setEnabled(true);
								if (progressDialog != null) {
									progressDialog.dismiss();
									progressDialog = null;
								}
								if (result == 0) {
									addInstalledCustomPackID(data.getID());
								}
							}
						}.execute(data.getID(), data.getUserID(), data.getName());
						dialog.cancel();
					}
				});
				builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				builder.create().show();
			}
		});

		new GetCustomPacks(this) {
			@Override
			protected void onPostExecute(Integer result) {
				inputSearch.setEnabled(true);
				lv.setEnabled(true);
				products.clear();
				ArrayList<CustomPackData> GlobalProducts = new ArrayList<CustomPackData>(getCustomPackList());
				for (int i = 0; i < GlobalProducts.size(); i++) {
					if (!downloadedPackIDs.contains(GlobalProducts.get(i).getID()))
						products.add(GlobalProducts.get(i));
				}
				allProducts.clear();
				allProducts.addAll(products);
				adapter.notifyDataSetChanged();
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
			}
		}.execute();
	}

	@Override
	protected void onStop() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		super.onStop();
	}

	private ArrayList<String> getInstalledCustomPacks() {
		ArrayList<String> downloaded = new ArrayList<String>();
		SharedPreferences sharedPrefs = getSharedPreferences("downloads", Context.MODE_PRIVATE);
		String list = sharedPrefs.getString("list_of_downloaded", null);
		if (list != null) {
			String[] test = list.split(",");
			for (int i = 0; i < test.length; i++) {
				downloaded.add(test[i]);
			}
		}
		return downloaded;
	}

	private void addInstalledCustomPackID(String ID) {
		downloadedPackIDs.add(ID);
		SharedPreferences sharedPrefs = getSharedPreferences("downloads", Context.MODE_PRIVATE);
		String list = "";
		list = downloadedPackIDs.get(0);
		for (int i = 1; i < downloadedPackIDs.size(); i++) {
			list += "," + downloadedPackIDs.get(i);
		}
		sharedPrefs.edit().putString("list_of_downloaded", list).commit();
	}
}
