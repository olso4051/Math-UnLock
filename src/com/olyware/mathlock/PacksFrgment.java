package com.olyware.mathlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.olyware.mathlock.adapter.PackItemAdapter;
import com.olyware.mathlock.model.PackItem;
import com.olyware.mathlock.utils.IabHelper;
import com.olyware.mathlock.utils.IabResult;
import com.olyware.mathlock.utils.Inventory;
import com.olyware.mathlock.utils.MoneyHelper;
import com.olyware.mathlock.utils.PreferenceHelper;
import com.olyware.mathlock.utils.Purchase;

public class PacksFrgment extends Fragment implements OnItemClickListener {

	private ListView packList;
	private ArrayList<PackItem> packItems = new ArrayList<PackItem>();
	// final private static String[] SKU = { "allpack", "math", "vocab1", "language1", "engineer", "hiqentrepack", "expansion" };
	// final private static String[] SKU = { "testpackall", "testmath", "testvocab", "testlanguage", "testengineer", "testhiqtravia" };

	// Production
	final private static String[] SKU = { "allpacksforlife", "mathpack", "englishvocabulary", "languages", "engineering", "trivia" };
	final public static String PURCHASE_KEY = "jF8foS2vFiNit8vn#ksl9aTkuK)_uVWe5OKn2Lo:";

	private int[] Cost;
	private String[] unlockPackageKeys, unlockSubPackageKeys, PackageKeys, packageInfo;
	private ArrayList<String> packageTitle = new ArrayList<String>();
	private ArrayList<String> packageSummury = new ArrayList<String>();

	public IabHelper mHelper;
	private IabHelper.QueryInventoryFinishedListener mQueryFinishedListener;
	private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener;
	private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener;
	private int[] unlockCost;
	private ArrayList<Integer> colors = new ArrayList<Integer>();

	private SharedPreferences sharedPrefsMoney;
	private SharedPreferences.Editor editorPrefsMoney;
	private PackItemAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_packs, null);
		packList = (ListView) view.findViewById(R.id.listViewPacks);

		Cost = MainActivity.getCost();
		sharedPrefsMoney = getActivity().getSharedPreferences("Packages", 0);
		initListner();
		initIabHelper();

		ArrayList<String> pack = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.enable_package_keys)));
		pack.add(0, "unlock_all");
		PackageKeys = new String[pack.size()];
		pack.toArray(PackageKeys);
		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		unlockSubPackageKeys = getResources().getStringArray(R.array.unlock_sub_package_keys);
		unlockCost = getResources().getIntArray(R.array.unlock_cost);
		packageInfo = getResources().getStringArray(R.array.package_info);

		ImageView crossImage = (ImageView) view.findViewById(R.id.crossImage);
		crossImage.setOnClickListener((MainActivity) getActivity());

		colors.add(getResources().getColor(R.color.list1));
		colors.add(getResources().getColor(R.color.list2));
		colors.add(getResources().getColor(R.color.list3));
		colors.add(getResources().getColor(R.color.list4));
		colors.add(getResources().getColor(R.color.list5));

		packageTitle.add(getString(R.string.unlock_all));
		packageTitle.add(getString(R.string.unlock_math));
		packageTitle.add("English Vocabulary");
		packageTitle.add("Languages");
		packageTitle.add(getString(R.string.unlock_engineer));
		packageTitle.add(getString(R.string.unlock_hiqh_trivia));
		packageTitle.add(getString(R.string.unlock_hiq_expansion));

		packageSummury.add(getString(R.string.desc_all));
		packageSummury.add(getString(R.string.desc_Math));
		packageSummury.add(getString(R.string.desc_vocab));
		packageSummury.add(getString(R.string.desc_language));
		packageSummury.add(getString(R.string.desc_engineering));
		packageSummury.add(getString(R.string.desc_hiq_trivia));
		packageSummury.add("Expansion");

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		MyApplication.getGaTracker().send(new HitBuilders.AppViewBuilder().build());
		GoogleAnalytics.getInstance(getActivity()).reportActivityStart(getActivity());
	}

	@Override
	public void onStop() {
		GoogleAnalytics.getInstance(getActivity()).reportActivityStop(getActivity());
		super.onStop();
	}

	private void initListner() {
		mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
			public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
				if (result.isFailure()) {
					// handle error
				} else if (mHelper != null) {
					// update UI
					// iabFinishedSetup = true;
					// costSmall.setText(inventory.getSkuDetails(SKU[0]).getPrice());
					// costLarge.setText(inventory.getSkuDetails(SKU[1]).getPrice());
					// costAll.setText(inventory.getSkuDetails(SKU[2]).getPrice());
					// Drawable drawable_selected = getResources().getDrawable(R.drawable.oval_selected);
					// Drawable drawable_unselected = getResources().getDrawable(R.drawable.oval_unselected);

					// check for non-consumed purchases
					if (inventory.hasPurchase(SKU[0])) {
						// mHelper.consumeAsync(inventory.getPurchase(SKU[0]), mConsumeFinishedListener);
						PreferenceHelper.unlockSubscription(getActivity(), 0);
						for (int i = 0; i < unlockPackageKeys.length; i++)
							PreferenceHelper.unlockSubscription(getActivity(), i);
					} else {
						// PreferenceHelper.unlockSubscription(getActivity(), 0);
						// for (int i = 0; i < unlockPackageKeys.length; i++)
						// PreferenceHelper.unlockSubscription(getActivity(), i);
						PreferenceHelper.lockSubscription(getActivity(), 0);
						//
						if (inventory.hasPurchase(SKU[1])) {
							// mHelper.consumeAsync(inventory.getPurchase(SKU[0]), mConsumeFinishedListener);
							PreferenceHelper.unlockSubscription(getActivity(), 1);
						} else {
							PreferenceHelper.lockSubscription(getActivity(), 1);
						}
						if (inventory.hasPurchase(SKU[2]) || inventory.hasPurchase("language1")) {// OLD Subscription
							// mHelper.consumeAsync(inventory.getPurchase(SKU[0]), mConsumeFinishedListener);
							PreferenceHelper.unlockSubscription(getActivity(), 2);
						} else {
							PreferenceHelper.lockSubscription(getActivity(), 2);
						}
						if (inventory.hasPurchase(SKU[3]) || inventory.hasPurchase("vocab1")) { // OLD Subscription
							// mHelper.consumeAsync(inventory.getPurchase(SKU[0]), mConsumeFinishedListener);
							PreferenceHelper.unlockSubscription(getActivity(), 3);
						} else {
							PreferenceHelper.lockSubscription(getActivity(), 3);
						}
						if (inventory.hasPurchase(SKU[4])) {
							// mHelper.consumeAsync(inventory.getPurchase(SKU[0]), mConsumeFinishedListener);
							PreferenceHelper.unlockSubscription(getActivity(), 4);
						} else {
							PreferenceHelper.lockSubscription(getActivity(), 4);
						}
						if (inventory.hasPurchase(SKU[5])) {
							// mHelper.consumeAsync(inventory.getPurchase(SKU[0]), mConsumeFinishedListener);
							PreferenceHelper.unlockSubscription(getActivity(), 5);
						} else {
							PreferenceHelper.lockSubscription(getActivity(), 5);
						}
					}

					// if (inventory.hasPurchase(SKU[6])) {
					// // mHelper.consumeAsync(inventory.getPurchase(SKU[0]), mConsumeFinishedListener);
					// PreferenceHelper.unlockSubscription(getActivity(), 6);
					// } else {
					// PreferenceHelper.lockSubscription(getActivity(), 6);
					// }

					packItems.clear();
					int[] subs = { 0, 1, 2, 3, 4, 5 };
					for (int i = 0; i < subs.length; i++) {
						PackItem item = new PackItem();
						item.setTitle(packageTitle.get(subs[i]));
						item.setSummury(packageSummury.get(subs[i]));
						if (!sharedPrefsMoney.getBoolean(unlockPackageKeys[subs[i]], false)
								&& !sharedPrefsMoney.getBoolean(unlockSubPackageKeys[subs[i]], false)) {
							item.setTextToShow(inventory.getSkuDetails(SKU[subs[i]]).getPrice() + getString(R.string.per_month));
							item.setPurchased(false);
							item.setEnabled(false);

							// cost[subs[i]].setText(inventory.getSkuDetails(SKU[SKUsubs[i]]).getPrice() + getString(R.string.per_month));
							// cost[subs[i]].setCompoundDrawablesWithIntrinsicBounds(null, drawable_unselected, null, null);
						} else {
							boolean isOn = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(PackageKeys[i], false);

							item.setTextToShow(isOn ? "ON" : "OFF");
							item.setPurchased(true);
							item.setEnabled(isOn);
							// cost[subs[i]].setText("ON");
							// cost[subs[i]].setCompoundDrawablesWithIntrinsicBounds(null, drawable_selected, null, null);
						}
						packItems.add(item);
					}

					adapter = new PackItemAdapter(getLayoutInflater(getArguments()), packItems, colors);
					packList.setAdapter(adapter);
					packList.setOnItemClickListener(PacksFrgment.this);

				}
			}
		};
	}

	public void refereshlist() {
		ArrayList<String> additionalSkuList = new ArrayList<String>(Arrays.asList(SKU));
		additionalSkuList.add("language1");
		additionalSkuList.add("vocab1");
		if (mHelper != null)
			mHelper.flagEndAsync();
		mHelper.queryInventoryAsync(true, additionalSkuList, mQueryFinishedListener);
	}

	private void initIabHelper() {
		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvFriusQ7xzxd5eXOnodv5f/XFohXXDHyguNboQC5kPBbwF+Dje/LwdnNN4tzFYN/SbelMPu4sGFdKh6sA4f13wmzIvVOynG3WUqRzut53mAq7/2ljNjwTO0enfYh6F54lnHrp2FpZsLpbzSMnC95dd07k4YbDs5e4AbqtgHIRCLPOsTnmsihOQO8kf1cR0G/b+B37sqaLEnMAKFDcSICup5LMHLOimQMQ3K9eFjBsyU8fiIe+JqnXOdQfknshxZ33tFu+hO3JXs7wxOs/n2uaIm14e95FlC4T/RXC/duAi8LWt3NOFXgJIqAwztncGJHi3u787wEQkiDKNBO8AkSkwIDAQAB";
		mHelper = new IabHelper(getActivity(), base64EncodedPublicKey);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// handle error
					// buttonCoins1.setEnabled(false);
					// buttonCoins2.setEnabled(false);
					// buttonCoins3.setEnabled(false);
					// buy[2].setEnabled(false);
					// buy[3].setEnabled(false);
				} else {
					// in app billing is set up. check for non-consumed purchases and enable the buttons
					// buttonCoins1.setEnabled(true);
					// buttonCoins2.setEnabled(true);
					// buttonCoins3.setEnabled(true);
					// for (int i = 2; i <= 3; i++) {
					// if (!sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false)
					// && !sharedPrefsMoney.getBoolean(unlockSubPackageKeys[i], false)) {
					// buy[i].setEnabled(true);
					// }
					// }
					refereshlist();

				}
			}
		});

		// this listener checks the google play server for prices and consumable products purchased but not yet
		// provisioned to the user

		// this listener checks if a product has been purchased then tries to consume it
		mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
			public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
				if (result.isFailure()) {
					// handle error
					return;
				} else if (purchase.getSku().equals(SKU[0])) {
					sendEvent("store", "unlocked_pack", "all_packages", (long) 50.0);
					// mHelper.consumeAsync(purchase, null);
					MoneyHelper.BoughtSomething(getActivity());
					sendTransaction(purchase.getOrderId(), 0.99 * .7);
					sendItem(purchase.getOrderId(), "All Packs", purchase.getSku(), "subscriptions", 50.00);
					// mHelper.consumeAsync(purchase, mConsumeFinishedListener);
					PreferenceHelper.unlockSubscription(getActivity(), 0);
					for (int i = 0; i < unlockPackageKeys.length; i++)
						PreferenceHelper.unlockSubscription(getActivity(), i);
				}
				if (purchase.getSku().equals(SKU[1])) {
					MoneyHelper.BoughtSomething(getActivity());
					sendTransaction(purchase.getOrderId(), 1.99 * .7);
					sendItem(purchase.getOrderId(), "math", purchase.getSku(), "subscriptions", 1.00);
					sendEvent("store", "unlocked_pack", "math", (long) 1.0);
					// mHelper.consumeAsync(purchase, mConsumeFinishedListener);
					PreferenceHelper.unlockSubscription(getActivity(), 1);
				}
				if (purchase.getSku().equals(SKU[2])) {
					MoneyHelper.BoughtSomething(getActivity());
					sendTransaction(purchase.getOrderId(), 2.99 * .7);
					sendItem(purchase.getOrderId(), "Vocab", purchase.getSku(), "subscriptions", 1.00);
					sendEvent("store", "unlocked_pack", "Vocab", (long) 1.0);
					// mHelper.consumeAsync(purchase, mConsumeFinishedListener);
					PreferenceHelper.unlockSubscription(getActivity(), 2);
				}
				if (purchase.getSku().equals(SKU[3])) {
					MoneyHelper.BoughtSomething(getActivity());
					sendTransaction(purchase.getOrderId(), 1.00 * .7);
					sendItem(purchase.getOrderId(), "Language Subscription", purchase.getSku(), "subscriptions", 3.00);
					sendEvent("store", "unlocked_pack", "Language Subscription ", (long) 3.0);
					PreferenceHelper.unlockSubscription(getActivity(), 3);
				}
				if (purchase.getSku().equals(SKU[4])) {
					MoneyHelper.BoughtSomething(getActivity());
					sendTransaction(purchase.getOrderId(), 1.00 * .7);
					sendItem(purchase.getOrderId(), "Engineeer Subscription", purchase.getSku(), "subscriptions", 1.00);
					sendEvent("store", "unlocked_pack", "Engineeer Subscription", (long) 1.0);
					PreferenceHelper.unlockSubscription(getActivity(), 4);
				}
				if (purchase.getSku().equals(SKU[5])) {
					MoneyHelper.BoughtSomething(getActivity());
					sendTransaction(purchase.getOrderId(), 3.00 * .7);
					sendItem(purchase.getOrderId(), "Hiq Travia Subscription", purchase.getSku(), "subscriptions", 1.00);
					sendEvent("store", "unlocked_pack", "Hiq Travia Subscription", (long) 1.0);
					PreferenceHelper.unlockSubscription(getActivity(), 5);
				}

				// PackItem item = packItems.get(Arrays.asList(SKU).indexOf(purchase.getSku()));
				// int i = Arrays.asList(SKU).indexOf(purchase.getSku());
				// boolean isOn = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(PackageKeys[i], false);
				//
				// item.setTextToShow(isOn ? "ON" : "OFF");
				// item.setPurchased(true);
				// item.setEnabled(isOn);
				// if (adapter != null)
				// adapter.notifyDataSetChanged();
				refereshlist();

				// if (purchase.getSku().equals(SKU[6])) {
				// MoneyHelper.BoughtSomething(getActivity());
				// sendTransaction(purchase.getOrderId(), 3.00 * .7);
				// sendItem(purchase.getOrderId(), "Expansion Subscription", purchase.getSku(), "subscriptions", 3.00);
				// PreferenceHelper.unlockSubscription(getActivity(), 6);
				// }
			}
		};

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (packItems.get(arg2).isPurchased()) {

			if (arg2 != 0) { // not all packs
				packItems.get(arg2).setEnabled(!packItems.get(arg2).isEnabled());
				packItems.get(arg2).setTextToShow(packItems.get(arg2).isEnabled() ? "ON" : "OFF");
				PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
						.putBoolean(PackageKeys[arg2], packItems.get(arg2).isEnabled()).commit();
				// if all pack disabled expect the first one
				boolean onepackenabled = false;
				boolean onepackdisabled = false;
				int count = 0;
				for (int i = 1; i < packItems.size(); i++) {
					if (packItems.get(i).isEnabled()) {
						onepackenabled = true;
						count++;
					} else {
						onepackdisabled = true;
					}

				}

				if (sharedPrefsMoney.getBoolean(unlockSubPackageKeys[0], false) && onepackdisabled) {
					packItems.get(0).setEnabled(false);
					packItems.get(0).setTextToShow(packItems.get(0).isEnabled() ? "ON" : "OFF");
					PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putBoolean(PackageKeys[0], packItems.get(0).isEnabled()).commit();
				}
				// disable the first one too
				if (!onepackenabled && sharedPrefsMoney.getBoolean(unlockSubPackageKeys[0], false)) {
					packItems.get(0).setEnabled(false);
					packItems.get(0).setTextToShow(packItems.get(0).isEnabled() ? "ON" : "OFF");
					PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putBoolean(PackageKeys[0], packItems.get(0).isEnabled()).commit();
				}

				if (onepackenabled && count == packItems.size() - 1 && sharedPrefsMoney.getBoolean(unlockSubPackageKeys[0], false)) {
					packItems.get(0).setEnabled(true);
					packItems.get(0).setTextToShow(packItems.get(0).isEnabled() ? "ON" : "OFF");
					PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putBoolean(PackageKeys[0], packItems.get(0).isEnabled()).commit();
				}
			} else {
				// enable/disable depend on first one
				packItems.get(0).setEnabled(!packItems.get(0).isEnabled());
				packItems.get(0).setTextToShow(packItems.get(0).isEnabled() ? "ON" : "OFF");
				PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
						.putBoolean(PackageKeys[0], packItems.get(0).isEnabled()).commit();
				for (int i = 1; i < packItems.size(); i++) {
					packItems.get(i).setEnabled(packItems.get(0).isEnabled());
					packItems.get(i).setTextToShow(packItems.get(0).isEnabled() ? "ON" : "OFF");
					PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
							.putBoolean(PackageKeys[i], packItems.get(0).isEnabled()).commit();
				}
			}
			adapter.notifyDataSetChanged();

		} else {
			if (mHelper != null)
				mHelper.flagEndAsync();
			// mHelper.launchPurchaseFlow(getActivity(), SKU[arg2], 1, mPurchaseFinishedListener, PURCHASE_KEY);
			mHelper.launchSubscriptionPurchaseFlow(getActivity(), SKU[arg2], 1, mPurchaseFinishedListener, PURCHASE_KEY);
		}
	}

	private void sendEvent(String category, String action, String label, Long value) {
		// MyApplication.getGaTracker().send(MapBuilder.createEvent(category, action, label, value).build());
		MyApplication.getGaTracker().send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());

	}

	private void sendTransaction(String transactionID, Double revenue) {
		// MyApplication.getGaTracker().send(MapBuilder.createTransaction(transactionID, "In-App Store", revenue, 0.0d, 0.0d,
		// "USD").build());
		sendDataToTrackers(new HitBuilders.TransactionBuilder().setTransactionId(transactionID).setAffiliation("In-App Store")
				.setRevenue(revenue).setTax(0.0d).setShipping(0.0d).setCurrencyCode("USD").build());

	}

	private void sendItem(String transactionID, String name, String SKU, String category, Double price) {
		// MyApplication.getGaTracker().send(MapBuilder.createItem(transactionID, name, SKU, category, price, 1L, "USD").build());
		sendDataToTrackers(new HitBuilders.ItemBuilder().setTransactionId(transactionID).setName(name).setSku(SKU).setCategory(category)
				.setPrice(price).setQuantity(1L).setCurrencyCode("USD").build());

	}

	// Sends the ecommerce data.
	private void sendDataToTrackers(Map<String, String> params) {
		Tracker appTracker = MyApplication.getGaTracker();
		appTracker.send(params);
	}

}
