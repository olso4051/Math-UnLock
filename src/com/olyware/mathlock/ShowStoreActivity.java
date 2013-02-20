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

import com.olyware.mathlock.utils.EggHelper;
import com.olyware.mathlock.utils.MoneyHelper;

public class ShowStoreActivity extends Activity {
	final private int CostAll = 10000;
	final private int CostSmall = 1000;
	final private int CostLarge = 5000;
	private TextView moneyText;
	private Button buttonCoins1, buttonCoins2, buttonCoins3;
	private Button buyAll;
	private Button buyMath, buyVocab, buyLanguage, buyACT_SAT, buyGRE, buyToddler, buyEngineer, buyHighQTrivia;
	private String titles[];
	private String unlockPackageKeys[];
	private String PackageKeys[];

	private SharedPreferences sharedPrefsMoney;
	private SharedPreferences.Editor editorPrefsMoney;
	private int money, Pmoney;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_store);

		PackageKeys = getResources().getStringArray(R.array.enable_package_keys);
		unlockPackageKeys = getResources().getStringArray(R.array.unlock_package_keys);
		titles = new String[unlockPackageKeys.length];

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
		titles[0] = String.valueOf(buyAll.getText());
		buyAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(0, CostAll);
			}
		});
		buyMath = (Button) findViewById(R.id.unlock_math);
		titles[1] = String.valueOf(buyMath.getText());
		buyMath.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(1, CostSmall);
			}
		});
		buyVocab = (Button) findViewById(R.id.unlock_vocab);
		titles[2] = String.valueOf(buyVocab.getText());
		buyVocab.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(2, CostSmall);
			}
		});
		buyLanguage = (Button) findViewById(R.id.unlock_language);
		titles[3] = String.valueOf(buyLanguage.getText());
		buyLanguage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(3, CostSmall);
			}
		});
		buyACT_SAT = (Button) findViewById(R.id.unlock_act_sat);
		titles[4] = String.valueOf(buyACT_SAT.getText());
		buyACT_SAT.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(4, CostLarge);
			}
		});
		buyGRE = (Button) findViewById(R.id.unlock_gre);
		titles[5] = String.valueOf(buyGRE.getText());
		buyGRE.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(5, CostLarge);
			}
		});
		buyToddler = (Button) findViewById(R.id.unlock_toddler);
		titles[6] = String.valueOf(buyToddler.getText());
		buyToddler.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(6, CostSmall);
			}
		});
		buyEngineer = (Button) findViewById(R.id.unlock_engineer);
		titles[7] = String.valueOf(buyEngineer.getText());
		buyEngineer.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(7, CostSmall);
			}
		});
		buyHighQTrivia = (Button) findViewById(R.id.unlock_highq_trivia);
		titles[8] = String.valueOf(buyHighQTrivia.getText());
		buyHighQTrivia.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				buyProduct(8, CostSmall);
			}
		});
		initMoney();
		setCost();
		if (isPackageUnlocked())
			money += EggHelper.unlockEgg(this, moneyText, "store", 1000);
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
		money = sharedPrefsMoney.getInt("money", 0);
		Pmoney = sharedPrefsMoney.getInt("paid_money", 0);
		moneyText.setText(String.valueOf(money + Pmoney));
	}

	private boolean isPackageUnlocked() {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		for (int i = 0; i < unlockPackageKeys.length; i++)
			if (sharedPrefsMoney.getBoolean(unlockPackageKeys[i], false))
				return true;
		return false;
	}

	private void updateMoney(int amount) {
		Pmoney += amount;
		MoneyHelper.setMoney(this, moneyText, money, Pmoney);
	}

	private void buyProduct(final int product, final int amount) {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		int tempMoney = sharedPrefsMoney.getInt("money", 0);
		int tempPMoney = sharedPrefsMoney.getInt("paid_money", 0);
		int id = getResources().getIdentifier("package_info" + product, "string", getPackageName());
		String info = getResources().getString(id) + "\n\n";
		// check if they have enough coins
		if (tempMoney + tempPMoney >= amount) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(titles[product]);
			builder.setMessage(info + getString(R.string.purchase_package_message)).setCancelable(false)
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
			titles[0] = String.valueOf(buyAll.getText());
			builder.setMessage(info + getString(R.string.not_enough_coins)).setCancelable(false)
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
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		editorPrefsMoney = sharedPrefsMoney.edit();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editorPrefs = sharedPrefs.edit();
		int tempMoney = sharedPrefsMoney.getInt("money", 0);
		int tempPMoney = sharedPrefsMoney.getInt("paid_money", 0);

		tempMoney -= amount;
		if (tempMoney < 0) {
			tempPMoney += tempMoney;
			tempMoney = 0;
		}
		money = tempMoney;
		Pmoney = tempPMoney;
		editorPrefsMoney.putInt("paid_money", Pmoney);
		editorPrefsMoney.putInt("money", money);
		editorPrefsMoney.putBoolean(unlockPackageKeys[product], true);
		editorPrefsMoney.commit();
		if (product == 0)
			for (int i = 0; i < PackageKeys.length; i++)
				editorPrefs.putBoolean(PackageKeys[i], true);
		else if (product <= 3)
			editorPrefs.putBoolean(PackageKeys[product - 1], true);
		else if (product == 4) {
			editorPrefs.putBoolean(PackageKeys[product - 1], true);
			editorPrefs.putBoolean(PackageKeys[product], true);
		} else if (product == 5) {
			editorPrefs.putBoolean(PackageKeys[product], true);
			editorPrefs.putBoolean(PackageKeys[product + 1], true);
		} else
			editorPrefs.putBoolean(PackageKeys[product + 1], true);
		editorPrefs.commit();
		setCost();
	}

	private void setCost() {
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		MoneyHelper.setMoney(this, moneyText, money, Pmoney);
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
	}
}
