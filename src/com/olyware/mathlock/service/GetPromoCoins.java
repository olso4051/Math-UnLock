package com.olyware.mathlock.service;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.JSONHelper;
import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.MoneyHelper;

public class GetPromoCoins extends AsyncTask<Void, Integer, Integer> {
	final private static String ENDPOINT = "coin/";
	final private static String COIN_RESPONSE = "coins";
	final private static int COIN_MAX = 10000;
	private String baseURL, coinHash;
	private int coins = 0;
	private Context ctx;

	public GetPromoCoins(Context ctx, String coinHash) {
		baseURL = ctx.getString(R.string.service_base_url);
		this.coinHash = coinHash;
		this.ctx = ctx;
	}

	public int getCoins() {
		if (coins < 0)
			return 0;
		else if (coins < COIN_MAX)
			return coins;
		else
			return COIN_MAX;
	}

	@Override
	protected Integer doInBackground(Void... v) {
		// PUT to API with user_id
		HttpClient httpclient = HttpClientBuilder.create().build();
		Loggy.d("get promo coins from " + baseURL + ENDPOINT + coinHash);
		HttpPost httppost = new HttpPost(baseURL + ENDPOINT + coinHash);
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			HttpResponse response = httpclient.execute(httppost);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			Loggy.d("fullResult = " + fullResult);
			jsonResponse = new JSONObject(fullResult);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			coins = JSONHelper.getIntFromJSON(jsonResponse, COIN_RESPONSE);
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		if (result == 0 && getCoins() > 0 && getCoins() <= COIN_MAX) {
			SharedPreferences sharedPrefsMoney = ctx.getSharedPreferences(ctx.getString(R.string.pref_money), Context.MODE_PRIVATE);
			SharedPreferences.Editor editorPrefsMoney = sharedPrefsMoney.edit();
			Loggy.d("increase coins");
			MoneyHelper.increasePendingMoney(ctx, getCoins());
			editorPrefsMoney.putBoolean(coinHash, true).commit();
		}
	}
}
