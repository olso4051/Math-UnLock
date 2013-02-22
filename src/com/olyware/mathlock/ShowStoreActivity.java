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
	private TextView moneyText, packsTitle, extrasTitle;
	private Button buttonCoins1, buttonCoins2, buttonCoins3;
	private Button buyAll;
	private Button buyMath, buyVocab, buyLanguage, buyACT_SAT, buyGRE, buyToddler, buyEngineer, buyHighQTrivia;
	private Button buyHarder, custom;
	private String[] unlockPackageKeys, unlockAllKeys, PackageKeys, packageInfo, EggKeys;
	private int[] EggMaxValues;

	private SharedPreferences sharedPrefsMoney;
	private SharedPreferences.Editor editorPrefsMoney;
	private Coins Money = new Coins(0, 0);

	// private int money, Pmoney;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_store);

		PackageKeys = getResources().getStringArray(R.array.enable_package_keys);
		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		unlockAllKeys = ArrayUtils.addAll(unlockPackageKeys, getResources().getStringArray(R.array.unlock_extra_keys));
		packageInfo = getResources().getStringArray(R.array.package_info);
		EggKeys = getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = getResources().getIntArray(R.array.egg_max_values);

		moneyText = (TextView) findViewById(R.id.money);

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

		buyAll = (Button) findViewById(R.id.unlock_all);
		buyAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(String.valueOf(buyAll.getText()), 0, CostAll, null);
			}
		});
		buyMath = (Button) findViewById(R.id.unlock_math);
		buyMath.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(String.valueOf(buyMath.getText()), 1, CostSmall, null);
			}
		});
		buyVocab = (Button) findViewById(R.id.unlock_vocab);
		buyVocab.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(String.valueOf(buyVocab.getText()), 2, CostSmall, null);
			}
		});
		buyLanguage = (Button) findViewById(R.id.unlock_language);
		buyLanguage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(String.valueOf(buyLanguage.getText()), 3, CostSmall, null);
			}
		});
		buyACT_SAT = (Button) findViewById(R.id.unlock_act_sat);
		buyACT_SAT.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(String.valueOf(buyACT_SAT.getText()), 4, CostLarge, null);
			}
		});
		buyGRE = (Button) findViewById(R.id.unlock_gre);
		buyGRE.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(String.valueOf(buyGRE.getText()), 5, CostLarge, null);
			}
		});
		buyToddler = (Button) findViewById(R.id.unlock_toddler);
		buyToddler.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(String.valueOf(buyToddler.getText()), 6, CostSmall, null);
			}
		});
		buyEngineer = (Button) findViewById(R.id.unlock_engineer);
		buyEngineer.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(String.valueOf(buyEngineer.getText()), 7, CostSmall, null);
			}
		});
		buyHighQTrivia = (Button) findViewById(R.id.unlock_highq_trivia);
		buyHighQTrivia.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(String.valueOf(buyHighQTrivia.getText()), 8, CostSmall, null);
			}
		});

		extrasTitle = ((TextView) findViewById(R.id.extras));
		extrasTitle.setPaintFlags(extrasTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		buyHarder = (Button) findViewById(R.id.harder);
		buyHarder.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(String.valueOf(buyHarder.getText()), 9, CostSmall, null);
			}
		});
		custom = (Button) findViewById(R.id.custom);
		final Intent i = new Intent(this, ShowProgressActivity.class);
		custom.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(String.valueOf(custom.getText()), 10, 0, i);
				// TODO start build question pack activity
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		initMoney();
		setCost();
		if (isPackageUnlocked())
			Money.increaseMoney(EggHelper.unlockEgg(this, moneyText, EggKeys[5], EggMaxValues[5]));
		// money += EggHelper.unlockEgg(this, moneyText, EggKeys[5], EggMaxValues[5]);
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
		// money = sharedPrefsMoney.getInt("money", 0);
		// Pmoney = sharedPrefsMoney.getInt("paid_money", 0);
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
		// Pmoney += amount;
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
		} else {
			sharedPrefsMoney = getSharedPreferences("Packages", 0);
			int tempMoney = sharedPrefsMoney.getInt("money", 0);
			int tempPMoney = sharedPrefsMoney.getInt("paid_money", 0);
			// check if they have enough coins
			if (tempMoney + tempPMoney >= amount) {
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
	}

	private void purchase(int product, int amount) {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editorPrefs = sharedPrefs.edit();
		Log.d("test", "amount = " + amount);
		Log.d("test", "money before = " + Money.getMoney());
		Money.decreaseMoneyAndPaidWithDebt(amount);
		Log.d("test", "money after = " + Money.getMoney());
		/*int tempMoney = sharedPrefsMoney.getInt("money", 0);
		int tempPMoney = sharedPrefsMoney.getInt("paid_money", 0);

		tempMoney -= amount;
		if (tempMoney < 0) {
			tempPMoney += tempMoney;
			tempMoney = 0;
		}
		money = tempMoney;
		Pmoney = tempPMoney;*/
		editorPrefsMoney.putInt("paid_money", Money.getMoneyPaid());
		editorPrefsMoney.putInt("money", Money.getMoney());
		// editorPrefsMoney.putInt("paid_money", Pmoney);
		// editorPrefsMoney.putInt("money", money);
		editorPrefsMoney.putBoolean(unlockAllKeys[product], true);
		editorPrefsMoney.commit();
		if (product == 0)												// 0 is unlock_all
			for (int i = 0; i < PackageKeys.length; i++)
				editorPrefs.putBoolean(PackageKeys[i], true);
		else if (product <= 3)											// before test prep
			editorPrefs.putBoolean(PackageKeys[product - 1], true);
		else if (product == 4) {										// need to unlock act_sat math and vocab
			editorPrefs.putBoolean(PackageKeys[product - 1], true);
			editorPrefs.putBoolean(PackageKeys[product], true);
		} else if (product == 5) {										// need to unlock gre math and vocab
			editorPrefs.putBoolean(PackageKeys[product], true);
			editorPrefs.putBoolean(PackageKeys[product + 1], true);
		} else if (product <= unlockPackageKeys.length - 1)				// if false then product is an extra
			editorPrefs.putBoolean(PackageKeys[product + 1], true);
		editorPrefs.commit();
		setCost();
	}

	private void setCost() {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		MoneyHelper.setMoney(this, moneyText, Money.getMoney(), Money.getMoneyPaid());
		if (sharedPrefsMoney.getBoolean("unlock_all", false)) {
			((TextView) findViewById(R.id.all_cost)).setText(getString(R.string.purchased));
			buyAll.setEnabled(false);
			buyMath.setEnabled(false);
			buyVocab.setEnabled(false);
			buyLanguage.setEnabled(false);
			buyACT_SAT.setEnabled(false);
			buyGRE.setEnabled(false);
			buyToddler.setEnabled(false);
			buyEngineer.setEnabled(false);
			buyHighQTrivia.setEnabled(false);
			buyHarder.setEnabled(false);
		} else
			((TextView) findViewById(R.id.all_cost)).setText(CostAll + " ");
		if (sharedPrefsMoney.getBoolean("unlock_math", false)) {
			((TextView) findViewById(R.id.math_cost)).setText(getString(R.string.purchased));
			buyMath.setEnabled(false);
		} else
			((TextView) findViewById(R.id.math_cost)).setText(CostSmall + " ");
		if (sharedPrefsMoney.getBoolean("unlock_vocab", false)) {
			((TextView) findViewById(R.id.vocab_cost)).setText(getString(R.string.purchased));
			buyVocab.setEnabled(false);
		} else
			((TextView) findViewById(R.id.vocab_cost)).setText(CostSmall + " ");
		if (sharedPrefsMoney.getBoolean("unlock_language", false)) {
			((TextView) findViewById(R.id.language_cost)).setText(getString(R.string.purchased));
			buyLanguage.setEnabled(false);
		} else
			((TextView) findViewById(R.id.language_cost)).setText(CostSmall + " ");
		if (sharedPrefsMoney.getBoolean("unlock_act_sat", false)) {
			((TextView) findViewById(R.id.act_sat_cost)).setText(getString(R.string.purchased));
			buyACT_SAT.setEnabled(false);
		} else
			((TextView) findViewById(R.id.act_sat_cost)).setText(CostLarge + " ");
		if (sharedPrefsMoney.getBoolean("unlock_gre", false)) {
			((TextView) findViewById(R.id.gre_cost)).setText(getString(R.string.purchased));
			buyGRE.setEnabled(false);
		} else
			((TextView) findViewById(R.id.gre_cost)).setText(CostLarge + " ");
		if (sharedPrefsMoney.getBoolean("unlock_toddler", false)) {
			((TextView) findViewById(R.id.toddler_cost)).setText(getString(R.string.purchased));
			buyToddler.setEnabled(false);
		} else
			((TextView) findViewById(R.id.toddler_cost)).setText(CostSmall + " ");
		if (sharedPrefsMoney.getBoolean("unlock_engineer", false)) {
			((TextView) findViewById(R.id.engineer_cost)).setText(getString(R.string.purchased));
			buyEngineer.setEnabled(false);
		} else
			((TextView) findViewById(R.id.engineer_cost)).setText(CostSmall + " ");
		if (sharedPrefsMoney.getBoolean("unlock_highq_trivia", false)) {
			((TextView) findViewById(R.id.highq_trivia_cost)).setText(getString(R.string.purchased));
			buyHighQTrivia.setEnabled(false);
		} else
			((TextView) findViewById(R.id.highq_trivia_cost)).setText(CostSmall + " ");
		if (sharedPrefsMoney.getBoolean("unlock_harder", false)) {
			((TextView) findViewById(R.id.harder_cost)).setText(getString(R.string.purchased));
			buyHarder.setEnabled(false);
		} else
			((TextView) findViewById(R.id.harder_cost)).setText(CostSmall + " ");
		((TextView) findViewById(R.id.custom_cost)).setText("FREE");
	}
}
