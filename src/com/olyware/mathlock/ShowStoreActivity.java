package com.olyware.mathlock;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.olyware.mathlock.utils.Coins;
import com.olyware.mathlock.utils.EggHelper;
import com.olyware.mathlock.utils.IabHelper;
import com.olyware.mathlock.utils.IabResult;
import com.olyware.mathlock.utils.Inventory;
import com.olyware.mathlock.utils.MoneyHelper;
import com.olyware.mathlock.utils.Purchase;

public class ShowStoreActivity extends Activity {
	// final private int CostAll = 10000, CostSmall = 1000, CostLarge = 5000;
	// final private String SKUcoins1000 = "coins1000", SKUcoins5000 = "coins5000", SKUcoins10000 = "coins10000";
	private int[] Cost;
	private String[] SKU;
	private TextView moneyText, packsTitle, extrasTitle, testPrepTitle, costSmall, costLarge, costAll;
	private Button buttonCoins1, buttonCoins2, buttonCoins3;
	private Button[] buy;
	private TextView[] cost;
	private Button custom;
	private String[] unlockPackageKeys, unlockAllKeys, PackageKeys, packageInfo, EggKeys;
	private int[] unlockCost, EggMaxValues;

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

		Cost = MainActivity.getCost();
		SKU = MainActivity.getSKU();

		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvFriusQ7xzxd5eXOnodv5f/XFohXXDHyguNboQC5kPBbwF+Dje/LwdnNN4tzFYN/SbelMPu4sGFdKh6sA4f13wmzIvVOynG3WUqRzut53mAq7/2ljNjwTO0enfYh6F54lnHrp2FpZsLpbzSMnC95dd07k4YbDs5e4AbqtgHIRCLPOsTnmsihOQO8kf1cR0G/b+B37sqaLEnMAKFDcSICup5LMHLOimQMQ3K9eFjBsyU8fiIe+JqnXOdQfknshxZ33tFu+hO3JXs7wxOs/n2uaIm14e95FlC4T/RXC/duAi8LWt3NOFXgJIqAwztncGJHi3u787wEQkiDKNBO8AkSkwIDAQAB";
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// Log.d("test", "Problem setting up In-app Billing: " + result);
				} else {
					Log.d("test", "Hooray, IAB is fully set up!");
					List<String> additionalSkuList = new ArrayList<String>();
					additionalSkuList.add(SKU[0]);
					additionalSkuList.add(SKU[1]);
					additionalSkuList.add(SKU[2]);
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
				} else {
					Log.d("test", "Hooray, IAB finished a query");
					// update UI
					costSmall.setText(inventory.getSkuDetails(SKU[0]).getPrice());
					costLarge.setText(inventory.getSkuDetails(SKU[1]).getPrice());
					costAll.setText(inventory.getSkuDetails(SKU[2]).getPrice());
					// check for non-consumed purchases
					if (inventory.hasPurchase(SKU[0])) {
						mHelper.consumeAsync(inventory.getPurchase(SKU[0]), mConsumeFinishedListener);
					} else if (inventory.hasPurchase(SKU[1])) {
						mHelper.consumeAsync(inventory.getPurchase(SKU[1]), mConsumeFinishedListener);
					} else if (inventory.hasPurchase(SKU[2])) {
						mHelper.consumeAsync(inventory.getPurchase(SKU[2]), mConsumeFinishedListener);
					} else if (inventory.hasPurchase("android.test.purchased")) {
						mHelper.consumeAsync(inventory.getPurchase("android.test.purchased"), mConsumeFinishedListener);
					}
				}
			}
		};
		// this listener checks if a product has been purchased then tries to consume it
		mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
			public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
				if (result.isFailure()) {
					Log.d("purchasing", "Error purchasing: " + result);
					return;
				} else if (purchase.getSku().equals(SKU[0])) {
					mHelper.consumeAsync(purchase, mConsumeFinishedListener);
				} else if (purchase.getSku().equals(SKU[1])) {
					mHelper.consumeAsync(purchase, mConsumeFinishedListener);
				} else if (purchase.getSku().equals(SKU[2])) {
					mHelper.consumeAsync(purchase, mConsumeFinishedListener);
				} else if (purchase.getSku().equals("android.test.purchased")) {
					Log.d("purchasing", "made purchase");
					mHelper.consumeAsync(purchase, mConsumeFinishedListener);
				}
			}
		};
		// this listener checks for products that have been consumed and provisions them to the user
		mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
			public void onConsumeFinished(Purchase purchase, IabResult result) {
				if (result.isSuccess()) {
					if (purchase.getSku().equals(SKU[0])) {
						updateMoney(Cost[0]);
					} else if (purchase.getSku().equals(SKU[1])) {
						updateMoney(Cost[1]);
					} else if (purchase.getSku().equals(SKU[2])) {
						updateMoney(Cost[2]);
					} else if (purchase.getSku().equals("android.test.purchased")) {
						Log.d("purchasing", "Hooray, IAB consumed android.test.purchased!");
						updateMoney(Cost[0]);
					}
				} else {
					// handle error
				}
			}
		};

		PackageKeys = getResources().getStringArray(R.array.enable_package_keys);
		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		unlockAllKeys = ArrayUtils.addAll(unlockPackageKeys, getResources().getStringArray(R.array.unlock_extra_keys));
		unlockCost = getResources().getIntArray(R.array.unlock_cost);
		packageInfo = getResources().getStringArray(R.array.package_info);
		EggKeys = getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = getResources().getIntArray(R.array.egg_max_values);

		moneyText = (TextView) findViewById(R.id.money);
		buy = new Button[unlockCost.length];
		cost = new TextView[unlockCost.length];

		costSmall = (TextView) findViewById(R.id.cost_small);
		costLarge = (TextView) findViewById(R.id.cost_large);
		costAll = (TextView) findViewById(R.id.cost_all);
		buttonCoins1 = (Button) findViewById(R.id.extra_coins1);
		buttonCoins1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO make user pay for more coins from google play
				mHelper.launchPurchaseFlow(ShowStoreActivity.this, "android.test.purchased", 10001, mPurchaseFinishedListener,
						"jF8foS2vFiNit8vn#ksl9aTkuK)_uVWe5OKn2Lo:");
				// updateMoney(Cost[0]);
			}
		});
		buttonCoins2 = (Button) findViewById(R.id.extra_coins2);
		buttonCoins2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO make user pay for more coins from google play
				mHelper.launchPurchaseFlow(ShowStoreActivity.this, "android.test.canceled", 50001, mPurchaseFinishedListener,
						"jF8foS2vFiNit8vn#ksl9aTkuK)_uVWe5OKn2Lo:");
				// updateMoney(Cost[1]);
			}
		});
		buttonCoins3 = (Button) findViewById(R.id.extra_coins3);
		buttonCoins3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO make user pay for more coins from google play
				mHelper.launchPurchaseFlow(ShowStoreActivity.this, "android.test.refunded", 100001, mPurchaseFinishedListener,
						"jF8foS2vFiNit8vn#ksl9aTkuK)_uVWe5OKn2Lo:");
				// updateMoney(Cost[2]);
			}
		});

		packsTitle = ((TextView) findViewById(R.id.packs));
		packsTitle.setPaintFlags(packsTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

		for (int i = 0; i < buy.length; i++) {
			final int loc = i;
			int idButton = getResources().getIdentifier(unlockAllKeys[i], "id", getPackageName());
			int idText = getResources().getIdentifier(unlockAllKeys[i].substring(7) + "_cost", "id", getPackageName());
			cost[i] = (TextView) findViewById(idText);
			buy[i] = (Button) findViewById(idButton);
			buy[i].setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					buyProduct(String.valueOf(buy[loc].getText()), loc, unlockCost[loc], null);
				}
			});
		}

		extrasTitle = ((TextView) findViewById(R.id.extras));
		extrasTitle.setPaintFlags(extrasTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

		custom = (Button) findViewById(R.id.custom);
		final Intent i = new Intent(this, ShowProgressActivity.class);
		custom.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(String.valueOf(custom.getText()), packageInfo.length - 1, 0, i);
				// TODO start build question pack activity
			}
		});

		testPrepTitle = ((TextView) findViewById(R.id.test_prep));
		testPrepTitle.setPaintFlags(testPrepTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			super.onActivityResult(requestCode, resultCode, data);
		} else {
			Log.d("purchasing", "onActivityResult handled by IAB util.");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		initMoney();
		setCost();
		if (isPackageUnlocked())
			Money.increaseMoney(EggHelper.unlockEgg(this, moneyText, EggKeys[5], EggMaxValues[5]));
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mHelper != null)
			mHelper.dispose();
		mHelper = null;
	}

	@Override
	public void onAttachedToWindow() {
		// don't show when locked to bring up screen lock mechanism
		// dismiss keyguard though so if there is no security then the activity is shown
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	private void initMoney() {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		Money.setMoneyPaid(sharedPrefsMoney.getInt("paid_money", 0));
		Money.setMoney(sharedPrefsMoney.getInt("money", 0));
		moneyText.setText(String.valueOf(Money.getMoney() + Money.getMoneyPaid()));
	}

	private boolean isPackageUnlocked() {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		for (int i = 0; i < unlockPackageKeys.length; i++)
			if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false))
				return true;
		return false;
	}

	private void updateMoney(int amount) {
		Money.increaseMoneyPaid(amount);
		MoneyHelper.setMoney(this, moneyText, Money.getMoney(), Money.getMoneyPaid());
	}

	private void buyProduct(String title, final int product, final int amount, final Intent i) {
		final boolean firstPack = !isPackageUnlocked();
		if (((product >= unlockPackageKeys.length) || (product == 0)) && (firstPack)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(title);
			builder.setMessage(packageInfo[product] + "\n\n" + getString(R.string.get_pack_first)).setCancelable(false);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		} else if ((Money.getMoney() + Money.getMoneyPaid() >= amount) || (firstPack)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(title);
			builder.setMessage(packageInfo[product] + "\n\n" + getString(R.string.purchase_package_message)).setCancelable(false);
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					if (i != null)
						startActivity(i);
					else if (firstPack)
						purchase(product, 0);
					else
						purchase(product, amount);
				}
			});
			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(title);
			builder.setMessage(packageInfo[product] + "\n\n" + getString(R.string.not_enough_coins)).setCancelable(false);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
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
		editorPrefsMoney.putBoolean(unlockAllKeys[product], true);
		editorPrefsMoney.commit();
		if (product == 0)												// 0 is unlock_all
			for (int i = 0; i < PackageKeys.length; i++)
				editorPrefs.putBoolean(PackageKeys[i], true);
		else if (product <= unlockPackageKeys.length - 1)				// if false then product is an extra
			editorPrefs.putBoolean(PackageKeys[product - 1], true);
		/*else if (product <= 3)											// before test prep
			editorPrefs.putBoolean(PackageKeys[product - 1], true);
		else if (product == 4) {										// need to unlock act_sat math and vocab
			editorPrefs.putBoolean(PackageKeys[product - 1], true);
			editorPrefs.putBoolean(PackageKeys[product], true);
		} else if (product == 5) {										// need to unlock gre math and vocab
			editorPrefs.putBoolean(PackageKeys[product], true);
			editorPrefs.putBoolean(PackageKeys[product + 1], true);
		} else if (product <= unlockPackageKeys.length - 1)				// if false then product is an extra
			editorPrefs.putBoolean(PackageKeys[product + 1], true);*/
		editorPrefs.commit();
		setCost();
	}

	private void setCost() {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		MoneyHelper.setMoney(this, moneyText, Money.getMoney(), Money.getMoneyPaid());
		if (sharedPrefsMoney.getBoolean("unlock_all", false)) {
			cost[0].setText(getString(R.string.purchased));
			for (int i = 0; i < buy.length; i++) {
				buy[i].setEnabled(false);
			}
		} else
			cost[0].setText(String.valueOf(unlockCost[0]));

		for (int i = 1; i < buy.length; i++) {
			if (sharedPrefsMoney.getBoolean(unlockAllKeys[i], false)) {
				cost[i].setText(getString(R.string.purchased));
				buy[i].setEnabled(false);
			} else
				cost[i].setText(String.valueOf(unlockCost[i]));
		}
		((TextView) findViewById(R.id.custom_cost)).setText("FREE");
	}
}
