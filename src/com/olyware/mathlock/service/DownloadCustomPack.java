package com.olyware.mathlock.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.olyware.mathlock.R;
import com.olyware.mathlock.ShowCustomEditActivity;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.database.contracts.CustomQuestionContract;
import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.model.Difficulty;

public class DownloadCustomPack extends AsyncTask<String, Integer, Integer> {
	private ArrayList<CustomPackData> customPackDataList = new ArrayList<CustomPackData>();
	private String baseURL;
	private Context ctx;

	public DownloadCustomPack(Context ctx) {
		this.ctx = ctx;
		baseURL = ctx.getString(R.string.service_base_url);
	}

	public ArrayList<CustomPackData> getCustomPackList() {
		return customPackDataList;
	}

	@Override
	protected Integer doInBackground(String... s) {
		DatabaseManager dbManager = new DatabaseManager(ctx.getApplicationContext());
		String userID = "", ID = "", name = "";
		if (s.length == 3) {
			if (s[0].length() > 0)
				userID = s[0];
			else
				return 1;
			if (s[1].length() > 0)
				ID = s[1];
			else
				return 1;
			if (s[2].length() > 0)
				name = s[2];
			else
				return 1;
		}

		InputStream input = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(baseURL + "pack/" + userID + "/" + ID);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return 1;
			}
			ArrayList<String[]> tempQuestions = new ArrayList<String[]>();
			input = connection.getInputStream();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
			String line = buffer.readLine();
			String[] lineEntries = line.split(",");
			if (lineEntries[0].equals(QuestionContract.QUESTION_TEXT) && lineEntries[1].equals(QuestionContract.ANSWER_CORRECT)
					&& lineEntries[2].equals(CustomQuestionContract.ANSWER_INCORRECT1)
					&& lineEntries[3].equals(CustomQuestionContract.ANSWER_INCORRECT2)
					&& lineEntries[4].equals(CustomQuestionContract.ANSWER_INCORRECT3)
					&& lineEntries[5].equals(QuestionContract.DIFFICULTY)) {
				line = buffer.readLine();
				while (line != null) {
					lineEntries = line.split(",");
					if (lineEntries.length == 6)
						if (Difficulty.isDifficulty(lineEntries[5]))
							if (testQuestion(lineEntries[0], lineEntries[1], lineEntries[2], lineEntries[3], lineEntries[4], name)) {
								tempQuestions.add(new String[] { lineEntries[0], lineEntries[1], lineEntries[2], lineEntries[3],
										lineEntries[4], name, lineEntries[5] });
							}
					line = buffer.readLine();
				}
			}
			buffer.close();
			for (int i = 0; i < tempQuestions.size(); i++) {
				dbManager.addCustomQuestion(tempQuestions.get(i));
			}
		} catch (Exception e) {
			return 1;
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException ignored) {
				// nothing
			}
			if (connection != null) {
				connection.disconnect();
			}
			if (dbManager != null) {
				if (!dbManager.isDestroyed())
					dbManager.destroy();
			}
		}
		return 0;
	}

	@Override
	protected void onPostExecute(Integer result) {
		// override in calling class
		// result == 0 success
	}

	private boolean testQuestion(String q, String a, String w1, String w2, String w3, String c) {
		return testQuestion(new String[] { q, a, w1, w2, w3, c });
	}

	private boolean testQuestion(String[] question) {
		boolean tooLongShort[] = new boolean[question.length];// { false, false, false, false, false, false };
		boolean same = false;
		List<String> q = new ArrayList<String>();
		for (int i = 0; i < question.length; i++) {
			if ((question[i].length() > ShowCustomEditActivity.MAX_LENGTH) || (question[i].length() == 0))
				tooLongShort[i] = true;
			else
				tooLongShort[i] = false;
			if ((i > 0) && (i < 5))
				if (q.contains(question[i]))
					same = true;
			q.add(question[i]);
		}
		if (tooLongShort[0] || tooLongShort[1] || tooLongShort[2] || tooLongShort[3] || tooLongShort[4] || tooLongShort[5]) {
			return false;
		} else if (same) {
			return false;
		}
		return true;
	}
}
