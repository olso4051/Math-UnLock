package com.olyware.mathlock;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.olyware.mathlock.utils.Coins;
import com.olyware.mathlock.utils.EggHelper;
import com.olyware.mathlock.utils.IabHelper;
import com.olyware.mathlock.utils.IabResult;
import com.olyware.mathlock.utils.Inventory;
import com.olyware.mathlock.utils.MoneyHelper;
import com.olyware.mathlock.utils.PreferenceHelper;
import com.olyware.mathlock.utils.Purchase;

public class ShowStoreActivity extends FragmentActivity {
	final private static String SCREEN_LABEL = "Store Screen";
	final private static String PURCHASE_KEY = "jF8foS2vFiNit8vn#ksl9aTkuK)_uVWe5OKn2Lo:";
	private int[] Cost;
	private String[] SKU;
	private ImageButton back;
	private TextView moneyText, packsTitle, costSmall, costLarge, costAll;
	private Button buttonCoins1, buttonCoins2, buttonCoins3;
	private Button[] buy;
	private TextView[] cost;
	// private Button custom;
	private String[] unlockPackageKeys, unlockSubPackageKeys, PackageKeys, packageInfo, EggKeys;
	private int[] unlockCost, EggMaxValues;
	private boolean iabFinishedSetup;
	private Drawable price;

	private SharedPreferences sharedPrefsMoney;
	private SharedPreferences.Editor editorPrefsMoney;
	private Coins Money = new Coins(0, 0);

	private IabHelper mHelper;
	private IabHelper.QueryInventoryFinishedListener mQueryFinishedListener;
	private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener;
	private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_store);

		MyApplication.getGaTracker().set(Fields.SCREEN_NAME, SCREEN_LABEL);

		Cost = MainActivity.getCost();
		SKU = MainActivity.getSKU();
		moneyText = (TextView) findViewById(R.id.money);
		EggKeys = getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = getResources().getIntArray(R.array.egg_max_values);
		initMoney();
		costSmall = (TextView) findViewById(R.id.cost_small);
		costLarge = (TextView) findViewById(R.id.cost_large);
		costAll = (TextView) findViewById(R.id.cost_all);
		iabFinishedSetup = false;

		PackageKeys = getResources().getStringArray(R.array.enable_package_keys);
		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		unlockSubPackageKeys = getResources().getStringArray(R.array.unlock_sub_package_keys);
		unlockCost = getResources().getIntArray(R.array.unlock_cost);
		packageInfo = getResources().getStringArray(R.array.package_info);

		buttonCoins1 = (Button) findViewById(R.id.extra_coins1);
		buttonCoins1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				purchaseCoins(ShowStoreActivity.this, SKU[0], Cost[0] + 1, mPurchaseFinishedListener, PURCHASE_KEY);
			}
		});
		buttonCoins2 = (Button) findViewById(R.id.extra_coins2);
		buttonCoins2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				purchaseCoins(ShowStoreActivity.this, SKU[1], Cost[1] + 1, mPurchaseFinishedListener, PURCHASE_KEY);
			}
		});
		buttonCoins3 = (Button) findViewById(R.id.extra_coins3);
		buttonCoins3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				purchaseCoins(ShowStoreActivity.this, SKU[2], Cost[2] + 1, mPurchaseFinishedListener, PURCHASE_KEY);
			}
		});

		buy = new Button[unlockCost.length];
		cost = new TextView[unlockCost.length];

		packsTitle = ((TextView) findViewById(R.id.packs));
		packsTitle.setPaintFlags(packsTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

		for (int a = 0; a < buy.length; a++) {
			final int loc = a;
			int idButton = getResources().getIdentifier(unlockPackageKeys[a], "id", getPackageName());
			int idText = getResources().getIdentifier(unlockPackageKeys[a].substring(7) + "_cost", "id", getPackageName());
			cost[a] = (TextView) findViewById(idText);
			buy[a] = (Button) findViewById(idButton);
			buy[a].setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					buyProduct(String.valueOf(buy[loc].getText()), loc, unlockCost[loc], null);
				}
			});
		}

		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvFriusQ7xzxd5eXOnodv5f/XFohXXDHyguNboQC5kPBbwF+Dje/LwdnNN4tzFYN/SbelMPu4sGFdKh6sA4f13wmzIvVOynG3WUqRzut53mAq7/2ljNjwTO0enfYh6F54lnHrp2FpZsLpbzSMnC95dd07k4YbDs5e4AbqtgHIRCLPOsTnmsihOQO8kf1cR0G/b+B37sqaLEnMAKFDcSICup5LMHLOimQMQ3K9eFjBsyU8fiIe+JqnXOdQfknshxZ33tFu+hO3JXs7wxOs/n2uaIm14e95FlC4T/RXC/duAi8LWt3NOFXgJIqAwztncGJHi3u787wEQkiDKNBO8AkSkwIDAQAB";
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// handle error
					buttonCoins1.setEnabled(false);
					buttonCoins2.setEnabled(false);
					buttonCoins3.setEnabled(false);
					buy[2].setEnabled(false);
					buy[3].setEnabled(false);
				} else {
					// in app billing is set up. check for non-consumed purchases and enable the buttons
					buttonCoins1.setEnabled(true);
					buttonCoins2.setEnabled(true);
					buttonCoins3.setEnabled(true);
					for (int i = 2; i <= 3; i++) {
						if (!sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false)
								&& !sharedPrefsMoney.getBoolean(unlockSubPackageKeys[i], false)) {
							buy[i].setEnabled(true);
						}
					}
					List<String> additionalSkuList = new ArrayList<String>();
					additionalSkuList.add(SKU[0]);
					additionalSkuList.add(SKU[1]);
					additionalSkuList.add(SKU[2]);
					additionalSkuList.add(SKU[3]);
					additionalSkuList.add(SKU[4]);
					additionalSkuList.add(SKU[5]);
					mHelper.queryInventoryAsync(true, additionalSkuList, mQueryFinishedListener);
				}
			}
		});
		// this listener checks the google play server for prices and consumable products purchased but not yet
		// provisioned to the user
		mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
			public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
				if (result.isFailure()) {
					// handle error
				} else if (costSmall != null && costLarge != null && costAll != null && mHelper != null) {
					// update UI
					iabFinishedSetup = true;
					costSmall.setText(inventory.getSkuDetails(SKU[0]).getPrice());
					costLarge.setText(inventory.getSkuDetails(SKU[1]).getPrice());
					costAll.setText(inventory.getSkuDetails(SKU[2]).getPrice());
					int[] subs = { 2, 3, 6 };
					int[] SKUsubs = { 3, 4, 5 };
					for (int i = 0; i < subs.length; i++) {
						if (!sharedPrefsMoney.getBoolean(unlockPackageKeys[subs[i]], false)
								&& !sharedPrefsMoney.getBoolean(unlockSubPackageKeys[subs[i]], false)) {
							cost[subs[i]].setText(inventory.getSkuDetails(SKU[SKUsubs[i]]).getPrice() + getString(R.string.per_month));
						} else {
							cost[subs[i]].setText(getString(R.string.purchased));
						}
					}
					// check for non-consumed purchases
					if (inventory.hasPurchase(SKU[0])) {
						mHelper.consumeAsync(inventory.getPurchase(SKU[0]), mConsumeFinishedListener);
					}
					if (inventory.hasPurchase(SKU[1])) {
						mHelper.consumeAsync(inventory.getPurchase(SKU[1]), mConsumeFinishedListener);
					}
					if (inventory.hasPurchase(SKU[2])) {
						mHelper.consumeAsync(inventory.getPurchase(SKU[2]), mConsumeFinishedListener);
					}
					if (inventory.hasPurchase(SKU[3])) {
						PreferenceHelper.unlockSubscription(ShowStoreActivity.this, 2);
					} else {
						PreferenceHelper.lockSubscription(ShowStoreActivity.this, 2);
					}
					if (inventory.hasPurchase(SKU[4])) {
						PreferenceHelper.unlockSubscription(ShowStoreActivity.this, 3);
					} else {
						PreferenceHelper.lockSubscription(ShowStoreActivity.this, 3);
					}
					if (inventory.hasPurchase(SKU[5])) {
						PreferenceHelper.unlockSubscription(ShowStoreActivity.this, 6);
					} else {
						PreferenceHelper.lockSubscription(ShowStoreActivity.this, 6);
					}
				}
			}
		};
		// this listener checks if a product has been purchased then tries to consume it
		mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
			public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
				if (result.isFailure()) {
					// handle error
					return;
				} else if (purchase.getSku().equals(SKU[0])) {
					MoneyHelper.BoughtSomething(ShowStoreActivity.this);
					sendTransaction(purchase.getOrderId(), 0.99 * .7);
					sendItem(purchase.getOrderId(), getString(R.string.extra_coins1), purchase.getSku(), "coins", 0.99);
					mHelper.consumeAsync(purchase, mConsumeFinishedListener);
				} else if (purchase.getSku().equals(SKU[1])) {
					MoneyHelper.BoughtSomething(ShowStoreActivity.this);
					sendTransaction(purchase.getOrderId(), 1.99 * .7);
					sendItem(purchase.getOrderId(), getString(R.string.extra_coins2), purchase.getSku(), "coins", 1.99);
					mHelper.consumeAsync(purchase, mConsumeFinishedListener);
				} else if (purchase.getSku().equals(SKU[2])) {
					MoneyHelper.BoughtSomething(ShowStoreActivity.this);
					sendTransaction(purchase.getOrderId(), 2.99 * .7);
					sendItem(purchase.getOrderId(), getString(R.string.extra_coins3), purchase.getSku(), "coins", 2.99);
					mHelper.consumeAsync(purchase, mConsumeFinishedListener);
				} else if (purchase.getSku().equals(SKU[3])) {
					MoneyHelper.BoughtSomething(ShowStoreActivity.this);
					sendTransaction(purchase.getOrderId(), 1.00 * .7);
					sendItem(purchase.getOrderId(), "Vocab Subscription", purchase.getSku(), "subscriptions", 1.00);
					PreferenceHelper.unlockSubscription(ShowStoreActivity.this, 2);
				} else if (purchase.getSku().equals(SKU[4])) {
					MoneyHelper.BoughtSomething(ShowStoreActivity.this);
					sendTransaction(purchase.getOrderId(), 1.00 * .7);
					sendItem(purchase.getOrderId(), "Language Subscription", purchase.getSku(), "subscriptions", 1.00);
					PreferenceHelper.unlockSubscription(ShowStoreActivity.this, 3);
				} else if (purchase.getSku().equals(SKU[5])) {
					MoneyHelper.BoughtSomething(ShowStoreActivity.this);
					sendTransaction(purchase.getOrderId(), 3.00 * .7);
					sendItem(purchase.getOrderId(), "Expansion Subscription", purchase.getSku(), "subscriptions", 3.00);
					PreferenceHelper.unlockSubscription(ShowStoreActivity.this, 6);
				}
			}
		};
		// this listener checks for products that have been consumed and provisions them to the user
		mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
			public void onConsumeFinished(Purchase purchase, IabResult result) {
				if (result.isSuccess()) {
					if (purchase.getSku().equals(SKU[0])) {
						updateMoney(Cost[0]);
						Money.increaseMoney(EggHelper.unlockEgg(ShowStoreActivity.this, moneyText, null, EggKeys[10], EggMaxValues[10]));
					} else if (purchase.getSku().equals(SKU[1])) {
						updateMoney(Cost[1]);
						Money.increaseMoney(EggHelper.unlockEgg(ShowStoreActivity.this, moneyText, null, EggKeys[11], EggMaxValues[11]));
					} else if (purchase.getSku().equals(SKU[2])) {
						updateMoney(Cost[2]);
						Money.increaseMoney(EggHelper.unlockEgg(ShowStoreActivity.this, moneyText, null, EggKeys[12], EggMaxValues[12]));
					}
				} else {
					// handle error
				}
			}
		};

		back = (ImageButton) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		Button customPacks = (Button) findViewById(R.id.unlock_custom);
		customPacks.setEnabled(false);
		/*customPacks.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(ShowStoreActivity.this, SearchableCustomPackActivity.class));
			}
		});*/
		TextView customPacksCost = (TextView) findViewById(R.id.custom_cost);
		customPacksCost.setText("Soon");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			super.onActivityResult(requestCode, resultCode, data);
		} else {
			// handled by IabHelper
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		MyApplication.getGaTracker().send(MapBuilder.createAppView().build());
	}

	@Override
	protected void onResume() {
		super.onResume();
		setCost();
		Money.increaseMoney(EggHelper.unlockEgg(this, moneyText, null, EggKeys[5], EggMaxValues[5]));
	}

	@Override
	public void onAttachedToWindow() {
		// don't show when locked to bring up screen lock mechanism
		// dismiss keyguard though so if there is no security then the activity is shown
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	@Override
	protected void onDestroy() {
		if (mHelper != null)
			mHelper.dispose();
		mHelper = null;
		super.onDestroy();
	}

	private void initMoney() {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		Money.setMoneyPaid(sharedPrefsMoney.getInt("paid_money", 0));
		Money.setMoney(sharedPrefsMoney.getInt("money", 0));
		moneyText.setText(String.valueOf(Money.getMoney() + Money.getMoneyPaid()));
	}

	private void updateMoney(int amount) {
		Money.increaseMoneyPaid(amount);
		MoneyHelper.setMoney(this, moneyText, null, Money.getMoney(), Money.getMoneyPaid(), 0);
	}

	private void alertIabNotSetup() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.info_iab_not_setup_title)).setCancelable(false);
		builder.setMessage(getString(R.string.info_iab_not_setup_message));
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	private void purchaseCoins(Activity act, String SKU, int id, IabHelper.OnIabPurchaseFinishedListener listener, String key) {
		if (!iabFinishedSetup) {
			alertIabNotSetup();
		} else {
			mHelper.launchPurchaseFlow(act, SKU, id, listener, key);
		}
	}

	private void buyProduct(String title, final int product, final int amount, final Intent i) {
		// if language or vocab launch subscription purchase flow
		if (product == 2 || product == 3 || product == 6) {
			if (!iabFinishedSetup) {
				alertIabNotSetup();
			} else {
				int SKUsub = product + 1;
				if (product == 6)
					SKUsub = 5;
				mHelper.launchSubscriptionPurchaseFlow(ShowStoreActivity.this, SKU[SKUsub], Cost[SKUsub] + 1, mPurchaseFinishedListener,
						PURCHASE_KEY);
			}
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(title).setCancelable(false);
			if (Money.getMoney() + Money.getMoneyPaid() >= amount) {
				builder.setMessage(packageInfo[product] + "\n\n" + getString(R.string.purchase_package_message));
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (i != null)
							startActivity(i);
						else
							purchase(product, amount);
					}
				});
				builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
			} else {
				builder.setMessage(packageInfo[product] + "\n\n" + getString(R.string.not_enough_coins));
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
			}
			builder.create().show();
		}
	}

	private void purchase(int product, int amount) {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editorPrefs = sharedPrefs.edit();
		Money.decreaseMoneyAndPaidWithDebt(amount);
		editorPrefsMoney.putInt("paid_money", Money.getMoneyPaid());
		editorPrefsMoney.putInt("money", Money.getMoney());
		editorPrefsMoney.putBoolean(unlockPackageKeys[product], true);		// unlocks the product
		editorPrefsMoney.commit();
		// enables all the question packs
		if (product == 0) {												// 0 is unlock_all
			sendEvent("store", "unlocked_pack", "all_packages", (long) amount);
			for (int i = 0; i < PackageKeys.length; i++)
				editorPrefs.putBoolean(PackageKeys[i], true);
		}
		// enables the question pack that was unlocked
		else if (product <= unlockPackageKeys.length - 2) {
			sendEvent("store", "unlocked_pack", PackageKeys[product - 1], (long) amount);
			editorPrefs.putBoolean(PackageKeys[product - 1], true);
		}
		editorPrefs.commit();
		setCost();
	}

	private void setCost() {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		MoneyHelper.setMoney(this, moneyText, null, Money.getMoney(), Money.getMoneyPaid(), 0);
		if (sharedPrefsMoney.getBoolean("unlock_all", false)) {
			cost[0].setText(getString(R.string.purchased));
			for (int i = 0; i < unlockPackageKeys.length; i++) {
				buy[i].setEnabled(false);
			}
		} else
			cost[0].setText(String.valueOf(unlockCost[0]));

		for (int i = 1; i < buy.length; i++) {
			if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false) || sharedPrefsMoney.getBoolean(unlockSubPackageKeys[i], false)) {
				cost[i].setText(getString(R.string.purchased));
				buy[i].setEnabled(false);
			} else if (i != 2 && i != 3 && i != 6) {
				cost[i].setText(String.valueOf(unlockCost[i]));
			}
		}

		price = getResources().getDrawable(R.drawable.coin1);
		price.setBounds(0, 0, price.getIntrinsicWidth(), price.getIntrinsicHeight());
		for (int a = 1; a < unlockPackageKeys.length; a++) {
			cost[a].setCompoundDrawables(null, null, price, null);
		}
	}

	private void sendEvent(String category, String action, String label, Long value) {
		MyApplication.getGaTracker().send(MapBuilder.createEvent(category, action, label, value).build());
	}

	private void sendTransaction(String transactionID, Double revenue) {
		MyApplication.getGaTracker().send(MapBuilder.createTransaction(transactionID, "In-App Store", revenue, 0.0d, 0.0d, "USD").build());
	}

	private void sendItem(String transactionID, String name, String SKU, String category, Double price) {
		MyApplication.getGaTracker().send(MapBuilder.createItem(transactionID, name, SKU, category, price, 1L, "USD").build());
	}
}
