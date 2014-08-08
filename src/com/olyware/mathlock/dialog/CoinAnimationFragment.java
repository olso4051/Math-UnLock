package com.olyware.mathlock.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.olyware.mathlock.R;
import com.olyware.mathlock.views.CoinView;

public class CoinAnimationFragment extends Fragment {

	final private static String ARGS_COINS = "coins";
	final private static String ARGS_STARTING_WIDTH = "startingWidth";
	final private static String ARGS_STARTING_HEIGHT = "startingHeight";
	final private static String ARGS_ENDING_WIDTH = "endingWidth";
	final private static String ARGS_ENDING_HEIGHT = "endingHeight";
	final public static String TAG = "coin_animation_dialog";

	public static CoinAnimationFragment newInstance(Context ctx, int coins, int startingWidth, int startingHeight, int endingWidth,
			int endingHeight) {
		CoinAnimationFragment f = new CoinAnimationFragment();

		Bundle args = new Bundle();
		args.putInt(ARGS_COINS, coins);
		args.putInt(ARGS_STARTING_WIDTH, startingWidth);
		args.putInt(ARGS_STARTING_HEIGHT, startingHeight);
		args.putInt(ARGS_ENDING_WIDTH, endingWidth);
		args.putInt(ARGS_ENDING_HEIGHT, endingHeight);

		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_coins, container, false);

		Bundle args = getArguments();

		CoinView coins = (CoinView) v.findViewById(R.id.fragment_coins_coins);
		coins.setStartingCenter(args.getInt(ARGS_STARTING_WIDTH), args.getInt(ARGS_STARTING_HEIGHT));
		coins.setEndingCenter(args.getInt(ARGS_ENDING_WIDTH), args.getInt(ARGS_ENDING_HEIGHT));
		coins.setCoinAmount(args.getInt(ARGS_COINS));
		coins.setOnAnimationDoneListener(new CoinView.AnimationDoneListener() {
			@Override
			public void OnDone() {
				if (getActivity() != null)
					getActivity().getSupportFragmentManager().beginTransaction().remove(CoinAnimationFragment.this).commit();
			}
		});

		return v;
	}
}
