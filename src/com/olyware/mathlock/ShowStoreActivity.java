package com.olyware.mathlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ShowStoreActivity extends Activity {
	final private int CostAll = 10000;
	final private int CostSmall = 1000;
	final private int CostLarge = 5000;
	private TextView moneyText;
	private Button buttonCoins1, buttonCoins2, buttonCoins3;
	private Button buyAll;
	private Button buyMath, buyVocab, buyLanguage, buyACT, buySAT, buyGRE, buyToddler, buyEngineer;
	private String unlockPackageKeys[] = { "unlock_all", "unlock_math", "unlock_vocab", "unlock_language", "unlock_act", "unlock_sat",
			"unlock_gre", "unlock_toddler", "unlock_engineer" };
	private String PackageKeys[] = { "enable_math", "enable_vocab", "enable_language", "enable_act", "enable_sat", "enable_gre",
			"enable_toddler", "enable_engineer" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_store);

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
		buyAll = (Button) findViewById(R.id.unlock_all);
		buyAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(0, CostAll);
			}
		});
		buyMath = (Button) findViewById(R.id.unlock_math);
		buyMath.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(1, CostSmall);
			}
		});
		buyVocab = (Button) findViewById(R.id.unlock_vocab);
		buyVocab.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(2, CostSmall);
			}
		});
		buyLanguage = (Button) findViewById(R.id.unlock_language);
		buyLanguage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(3, CostSmall);
			}
		});
		buyACT = (Button) findViewById(R.id.unlock_act);
		buyACT.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(4, CostSmall);
			}
		});
		buySAT = (Button) findViewById(R.id.unlock_sat);
		buySAT.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(5, CostSmall);
			}
		});
		buyGRE = (Button) findViewById(R.id.unlock_gre);
		buyGRE.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(6, CostLarge);
			}
		});
		buyToddler = (Button) findViewById(R.id.unlock_toddler);
		buyToddler.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(7, CostSmall);
			}
		});
		buyEngineer = (Button) findViewById(R.id.unlock_engineer);
		buyEngineer.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(8, CostSmall);
			}
		});
		setCost();
	}

	@Override
	public void onAttachedToWindow() {
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	private void updateMoney(int amount) {
		SharedPreferences sharedPrefsMoney = getSharedPreferences("Packages", 0);
		SharedPreferences.Editor editor = sharedPrefsMoney.edit();
		editor.putInt("paid_money", sharedPrefsMoney.getInt("paid_money", 0) + amount);
		editor.commit();
		moneyText.setText(sharedPrefsMoney.getInt("paid_money", 0) + sharedPrefsMoney.getInt("money", 0) + " ");
	}

	private void buyProduct(final int product, final int amount) {
		SharedPreferences sharedPrefsMoney = getSharedPreferences("Packages", 0);
		int tempMoney = sharedPrefsMoney.getInt("money", 0);
		int tempPMoney = sharedPrefsMoney.getInt("paid_money", 0);
		// check if they have enough coins
		if (tempMoney + tempPMoney >= amount) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.purchase_package_message).setCancelable(false)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
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
			builder.setMessage(R.string.not_enough_coins).setCancelable(false)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	private void purchase(int product, int amount) {
		SharedPreferences sharedPrefsMoney = getSharedPreferences("Packages", 0);
		SharedPreferences.Editor editorMoney = sharedPrefsMoney.edit();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editorPrefs = sharedPrefs.edit();
		int tempMoney = sharedPrefsMoney.getInt("money", 0);
		int tempPMoney = sharedPrefsMoney.getInt("paid_money", 0);

		tempMoney -= amount;
		if (tempMoney < 0) {
			tempPMoney += tempMoney;
			tempMoney = 0;
		}
		editorMoney.putInt("paid_money", tempPMoney);
		editorMoney.putInt("money", tempMoney);
		editorMoney.putBoolean(unlockPackageKeys[product], true);
		editorMoney.commit();
		if (product == 0)
			for (int i = 0; i < PackageKeys.length; i++)
				editorPrefs.putBoolean(PackageKeys[i], true);
		else
			editorPrefs.putBoolean(PackageKeys[product - 1], true);
		editorPrefs.commit();
		setCost();
	}

	private void setCost() {
		SharedPreferences sharedPrefsMoney = getSharedPreferences("Packages", 0);

		moneyText.setText(sharedPrefsMoney.getInt("paid_money", 0) + sharedPrefsMoney.getInt("money", 0) + " ");
		if (sharedPrefsMoney.getBoolean("unlock_all", false)) {
			((TextView) findViewById(R.id.all_cost)).setText(getString(R.string.purchased));
			buyAll.setEnabled(false);
			buyMath.setEnabled(false);
			buyVocab.setEnabled(false);
			buyLanguage.setEnabled(false);
			buyACT.setEnabled(false);
			buySAT.setEnabled(false);
			buyGRE.setEnabled(false);
			buyToddler.setEnabled(false);
			buyEngineer.setEnabled(false);
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
		if (sharedPrefsMoney.getBoolean("unlock_act", false)) {
			((TextView) findViewById(R.id.act_cost)).setText(getString(R.string.purchased));
			buyACT.setEnabled(false);
		} else
			((TextView) findViewById(R.id.act_cost)).setText(CostSmall + " ");
		if (sharedPrefsMoney.getBoolean("unlock_sat", false)) {
			((TextView) findViewById(R.id.sat_cost)).setText(getString(R.string.purchased));
			buySAT.setEnabled(false);
		} else
			((TextView) findViewById(R.id.sat_cost)).setText(CostSmall + " ");
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

	}
}
