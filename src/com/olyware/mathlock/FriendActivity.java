package com.olyware.mathlock;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.ui.Typefaces;
import com.olyware.mathlock.utils.Clock;
import com.olyware.mathlock.utils.Coins;
import com.olyware.mathlock.utils.EZ;
import com.olyware.mathlock.utils.EggHelper;

public class FriendActivity extends Activity {
	final private static String SCREEN_LABEL = "Friend Screen";
	private LinearLayout layout;
	private Typefaces typefaces;
	private Coins Money = new Coins(0, 0);
	private SharedPreferences sharedPrefsMoney;
	private ImageButton back;
	private Clock clock;
	private TextView coins;
	private String[] EggKeys;
	private int[] EggMaxValues;
	private EditText username;

	private DatabaseManager dbManager;

	private class OpenDatabase extends AsyncTask<Void, Integer, Integer> {
		@Override
		protected Integer doInBackground(Void... voids) {
			dbManager = new DatabaseManager(getApplicationContext());
			return 0;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend);

		layout = (LinearLayout) findViewById(R.id.layout);
		typefaces = Typefaces.getInstance(this);
		EZ.setFont((ViewGroup) layout, typefaces.robotoLight);

		EggKeys = getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = getResources().getIntArray(R.array.egg_max_values);

		new OpenDatabase().execute();

		back = (ImageButton) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		coins = (TextView) findViewById(R.id.money);
		clock = new Clock(this, (TextView) findViewById(R.id.clock), coins);

		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		Money = new Coins(sharedPrefsMoney.getInt("money", 0), sharedPrefsMoney.getInt("paid_money", 0));

		username = (EditText) findViewById(R.id.edittext_add_friend);

		MyApplication.getGaTracker().set(Fields.SCREEN_NAME, SCREEN_LABEL);
	}

	@Override
	protected void onStart() {
		super.onStart();
		MyApplication.getGaTracker().send(MapBuilder.createAppView().build());
	}

	@Override
	protected void onDestroy() {
		clock.destroy();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		sharedPrefsMoney = getSharedPreferences("Packages", 0);
		Money.setMoneyPaid(sharedPrefsMoney.getInt("paid_money", 0));
		Money.setMoney(sharedPrefsMoney.getInt("money", 0));
		coins.setText(String.valueOf(Money.getMoney() + Money.getMoneyPaid()));
		Money.increaseMoney(EggHelper.unlockEgg(this, coins, EggKeys[6], EggMaxValues[6]));
	}

	@Override
	protected void onPause() {
		SharedPreferences.Editor editor = sharedPrefsMoney.edit();
		editor.putInt("money", Money.getMoney());
		editor.putInt("paid_money", Money.getMoneyPaid());
		editor.commit();
		super.onPause();
	}

	@Override
	public void onAttachedToWindow() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}
}
