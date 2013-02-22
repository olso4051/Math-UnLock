package com.olyware.mathlock;

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
import com.olyware.mathlock.utils.MoneyHelper;

public class ShowStoreActivity extends Activity {
	final private int CostAll = 10000;
	final private int CostSmall = 1000;
	final private int CostLarge = 5000;
	private TextView moneyText, packsTitle, extrasTitle, testPrepTitle;
	private Button buttonCoins1, buttonCoins2, buttonCoins3;
	private Button[] buy;
	private TextView[] cost;
	private Button custom;
	private String[] unlockPackageKeys, unlockAllKeys, PackageKeys, packageInfo, EggKeys;
	private int[] unlockCost, EggMaxValues;

	private SharedPreferences sharedPrefsMoney;
	private SharedPreferences.Editor editorPrefsMoney;
	private Coins Money = new Coins(0, 0);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_store);

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

		buttonCoins1 = (Button) findViewById(R.id.extra_coins1);
		buttonCoins1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO make user pay for more coins from google play
				updateMoney(CostSmall);
			}
		});
		buttonCoins2 = (Button) findViewById(R.id.extra_coins2);
		buttonCoins2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO make user pay for more coins from google play
				updateMoney(CostLarge);
			}
		});
		buttonCoins3 = (Button) findViewById(R.id.extra_coins3);
		buttonCoins3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO make user pay for more coins from google play
				updateMoney(CostAll);
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
			Log.d("test", "i = " + i);
			Log.d("test", "idButton = " + idButton + "|idText = " + idText);
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
				buyProduct(String.valueOf(custom.getText()), 10, 0, i);
				// TODO start build question pack activity
			}
		});

		testPrepTitle = ((TextView) findViewById(R.id.test_prep));
		testPrepTitle.setPaintFlags(testPrepTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
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
		if ((product >= unlockPackageKeys.length) && (!isPackageUnlocked())) {
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
		} else if (Money.getMoney() + Money.getMoneyPaid() >= amount) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(title);
			builder.setMessage(packageInfo[product] + "\n\n" + getString(R.string.purchase_package_message)).setCancelable(false);
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
