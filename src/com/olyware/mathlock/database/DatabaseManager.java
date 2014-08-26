package com.olyware.mathlock.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.olyware.mathlock.MainActivity;
import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.ChallengeData;
import com.olyware.mathlock.adapter.ContactHashes;
import com.olyware.mathlock.database.contracts.BaseContract;
import com.olyware.mathlock.database.contracts.ChallengeQuestionContract;
import com.olyware.mathlock.database.contracts.CustomQuestionContract;
import com.olyware.mathlock.database.contracts.EngineerQuestionContract;
import com.olyware.mathlock.database.contracts.HiqTriviaQuestionContract;
import com.olyware.mathlock.database.contracts.LanguageQuestionContract;
import com.olyware.mathlock.database.contracts.MathQuestionContract;
import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.database.contracts.StatisticContract;
import com.olyware.mathlock.database.contracts.VocabQuestionContract;
import com.olyware.mathlock.model.ChallengeQuestion;
import com.olyware.mathlock.model.CustomQuestion;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.EngineerQuestion;
import com.olyware.mathlock.model.GenericQuestion;
import com.olyware.mathlock.model.HiqTriviaQuestion;
import com.olyware.mathlock.model.LanguageQuestion;
import com.olyware.mathlock.model.MathQuestion;
import com.olyware.mathlock.model.Statistic;
import com.olyware.mathlock.model.VocabQuestion;
import com.olyware.mathlock.utils.ChallengeBuilder;
import com.olyware.mathlock.utils.PreferenceHelper;

public class DatabaseManager {

	DatabaseOpenHelper dbHelper;
	SQLiteDatabase db;
	Cursor cursor;

	public DatabaseManager(Context context) {
		dbHelper = DatabaseOpenHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
	}

	public void destroy() {
		if (db.isOpen()) {
			db.close();
			if (cursor != null)
				cursor.close();
			if (dbHelper != null)
				dbHelper.close();
		}
	}

	public boolean isDestroyed() {
		if (db.isOpen())
			return false;
		else
			return true;
	}

	public static String unescape(String description) {
		return description.replaceAll("\\\\n", "\\\n");
	}

	public double getPriority(int table, String fromLanguage, String toLanguage, Difficulty minDifficulty, Difficulty maxDifficulty,
			long notID) {
		if (db.isOpen()) {
			String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
					+ String.valueOf(minDifficulty.getValue());
			String priorities = QuestionContract.PRIORITY;
			// String notIDs = " AND " + BaseContract._ID + " != " + notID;
			String tableName, allColumns[];
			List<String> cats = getAllCustomCategories();
			boolean success;
			switch (table) {
			case 0:			// math question
				tableName = MathQuestionContract.TABLE_NAME;
				allColumns = MathQuestionContract.ALL_COLUMNS;
				success = true;
				break;
			case 1:			// vocabulary question
				tableName = VocabQuestionContract.TABLE_NAME;
				allColumns = VocabQuestionContract.ALL_COLUMNS;
				success = true;
				break;
			case 2:			// language question
				tableName = LanguageQuestionContract.TABLE_NAME;
				allColumns = LanguageQuestionContract.ALL_COLUMNS;
				priorities = fromLanguage + LanguageQuestionContract.PRIORITIES + " + " + toLanguage + LanguageQuestionContract.PRIORITIES;
				success = true;
				break;
			case 3:			// engineer question
				tableName = EngineerQuestionContract.TABLE_NAME;
				allColumns = EngineerQuestionContract.ALL_COLUMNS;
				success = true;
				break;
			case 4:			// HiqH Trivia question
				tableName = HiqTriviaQuestionContract.TABLE_NAME2;
				allColumns = HiqTriviaQuestionContract.ALL_COLUMNS;
				success = true;
				break;
			default:
				tableName = MathQuestionContract.TABLE_NAME;
				allColumns = MathQuestionContract.ALL_COLUMNS;
				success = false;
				break;
			}
			// Custom question
			if ((table > 4) && (table < 5 + cats.size())) {
				tableName = CustomQuestionContract.TABLE_NAME;
				allColumns = CustomQuestionContract.ALL_COLUMNS;
				where = where + " AND " + CustomQuestionContract.CATEGORY + " = '" + cats.get(table - 5).replaceAll("'", "''") + "'";
				success = true;
			}
			if (success) {
				Cursor cursor2 = db.query(tableName, allColumns, where, null, null, null, null);
				double count = cursor2.getCount();
				cursor2 = db.rawQuery("SELECT SUM(" + priorities + ") FROM " + tableName + " WHERE " + where, null);
				cursor2.moveToFirst();
				double sum = cursor2.getInt(0);
				cursor2.close();
				if (count > 0)
					return sum / count;
				else
					return 0;
			} else
				return 0;
		} else
			return 0;
	}

	public long addStat(Statistic stat) {
		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put(StatisticContract.PACKAGE, stat.getPack());
			values.put(StatisticContract.CORRECT, stat.getCorrect());
			values.put(QuestionContract.DIFFICULTY, stat.getDifficulty().getValue());
			values.put(StatisticContract.TIME, stat.getTime());
			values.put(StatisticContract.TIME2SOLVE, stat.getTime2Solve());
			return db.insert(StatisticContract.TABLE_NAME, null, values);
		} else
			return -1;
	}

	public List<Integer> getStatPercentArray(long oldestTime, String Pack, String difficulty) {
		if (db.isOpen()) {
			String where = StatisticContract.TIME + " >= " + String.valueOf(oldestTime);
			if (!Pack.equals(MainActivity.getContext().getResources().getString(R.string.all)))
				where = where + " AND " + StatisticContract.PACKAGE + " = '" + Pack + "'";
			if (!difficulty.equals(MainActivity.getContext().getResources().getString(R.string.all)))
				where = where + " AND " + StatisticContract.DIFFICULTY + " = "
						+ String.valueOf(Difficulty.fromValue(difficulty).getValue());
			cursor = db.query(StatisticContract.TABLE_NAME, StatisticContract.ALL_COLUMNS, where, null, null, null, null);
			return DatabaseModelFactory.buildStats(cursor);
		} else
			return null;
	}

	public MathQuestion getMathQuestion(Difficulty minDifficulty, Difficulty maxDifficulty, long notID) {
		if (db.isOpen()) {
			String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
					+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
			cursor = db.query(MathQuestionContract.TABLE_NAME, MathQuestionContract.ALL_COLUMNS, where, null, null, null, null);

			Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + MathQuestionContract.TABLE_NAME
					+ " WHERE " + where, null);
			cursor2.moveToFirst();
			int sum = cursor2.getInt(0);
			cursor2.close();
			return DatabaseModelFactory.buildMathQuestion(cursor, sum);
		} else
			return null;
	}

	public List<VocabQuestion> getVocabQuestions(Difficulty minDifficulty, Difficulty maxDifficulty, int number, long notID) {
		if (db.isOpen()) {
			// String order = "RANDOM()";// LIMIT " + number;
			String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
					+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
			cursor = db.query(VocabQuestionContract.TABLE_NAME, QuestionContract.ALL_COLUMNS, where, null, null, null, null);

			Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + VocabQuestionContract.TABLE_NAME
					+ " WHERE " + where, null);
			cursor2.moveToFirst();
			int sum = cursor2.getInt(0);
			cursor2.close();
			return DatabaseModelFactory.buildVocabQuestions(cursor, sum, number);
		} else
			return null;
	}

	public List<LanguageQuestion> getLanguageQuestions(Difficulty minDifficulty, Difficulty maxDifficulty, int number, String fromLanguage,
			String toLanguage, long notID) {
		if (db.isOpen()) {
			String fromLanguagePriority = fromLanguage + LanguageQuestionContract.PRIORITIES;
			String toLanguagePriority = toLanguage + LanguageQuestionContract.PRIORITIES;
			String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
					+ String.valueOf(minDifficulty.getValue()) + " AND " + fromLanguage + "!=" + toLanguage + " AND " + BaseContract._ID
					+ " != " + notID;
			String[] columns = { fromLanguage, toLanguage, fromLanguagePriority, toLanguagePriority, QuestionContract.DIFFICULTY,
					QuestionContract._ID, QuestionContract.TIME_STEP, QuestionContract.TIME_STEPS };
			cursor = db.query(LanguageQuestionContract.TABLE_NAME, columns, where, null, null, null, null);

			Cursor cursor2 = db.rawQuery("SELECT SUM(" + fromLanguagePriority + "+" + toLanguagePriority + ") FROM "
					+ LanguageQuestionContract.TABLE_NAME + " WHERE " + where, null);
			cursor2.moveToFirst();
			int sum = cursor2.getInt(0);
			cursor2.close();
			return DatabaseModelFactory.buildLanguageQuestions(cursor, fromLanguage, toLanguage, sum, number);
		} else
			return null;
	}

	public EngineerQuestion getEngineerQuestion(Difficulty minDifficulty, Difficulty maxDifficulty, long notID) {
		if (db.isOpen()) {
			String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
					+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
			cursor = db.query(EngineerQuestionContract.TABLE_NAME, EngineerQuestionContract.ALL_COLUMNS, where, null, null, null, null);

			Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + EngineerQuestionContract.TABLE_NAME
					+ " WHERE " + where, null);
			cursor2.moveToFirst();
			int sum = cursor2.getInt(0);
			cursor2.close();
			return DatabaseModelFactory.buildEngineerQuestion(cursor, sum);
		} else
			return null;
	}

	public HiqTriviaQuestion getHiqTriviaQuestion(Difficulty minDifficulty, Difficulty maxDifficulty, long notID) {
		if (db.isOpen()) {
			String where = "difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
					+ String.valueOf(minDifficulty.getValue()) + " AND " + BaseContract._ID + " != " + notID;
			cursor = db.query(HiqTriviaQuestionContract.TABLE_NAME2, HiqTriviaQuestionContract.ALL_COLUMNS, where, null, null, null, null);

			Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + HiqTriviaQuestionContract.TABLE_NAME2
					+ " WHERE " + where, null);
			cursor2.moveToFirst();
			int sum = cursor2.getInt(0);
			cursor2.close();
			return DatabaseModelFactory.buildHiqHTriviaQuestion(cursor, sum);
		} else
			return null;
	}

	public CustomQuestion getSwisherQuestion(int count) {
		return getCustomQuestion(PreferenceHelper.SWISHER_FILENAME, Difficulty.VERY_EASY, Difficulty.INSANE, -1, count);
	}

	public CustomQuestion getCustomQuestion(String category, Difficulty minDifficulty, Difficulty maxDifficulty, long notID) {
		return getCustomQuestion(category, minDifficulty, maxDifficulty, notID, -1);
	}

	public CustomQuestion getCustomQuestion(String category, Difficulty minDifficulty, Difficulty maxDifficulty, long notID, int count) {
		if (db.isOpen()) {
			String diff = "";
			if (!category.equals(PreferenceHelper.SWISHER_FILENAME))
				diff = " AND difficulty <= " + String.valueOf(maxDifficulty.getValue()) + " AND difficulty >= "
						+ String.valueOf(minDifficulty.getValue());
			String where = CustomQuestionContract.CATEGORY + " = '" + category.replaceAll("'", "''") + "'";
			String notIDs = " AND " + BaseContract._ID + " != " + notID;
			cursor = db.query(CustomQuestionContract.TABLE_NAME, CustomQuestionContract.ALL_COLUMNS, where + diff + notIDs, null, null,
					null, null);

			int sum = 0;
			if (cursor.getCount() > 0) {
				Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + CustomQuestionContract.TABLE_NAME
						+ " WHERE " + where + diff, null);
				cursor2.moveToFirst();
				sum = cursor2.getInt(0);
				cursor2.close();
			} else {
				cursor = db.query(CustomQuestionContract.TABLE_NAME, CustomQuestionContract.ALL_COLUMNS, where + diff, null, null, null,
						null);
				if (cursor.getCount() > 0) {
					Cursor cursor2 = db.rawQuery("SELECT SUM(" + QuestionContract.PRIORITY + ") FROM " + CustomQuestionContract.TABLE_NAME
							+ " WHERE " + where + diff, null);
					cursor2.moveToFirst();
					sum = cursor2.getInt(0);
					cursor2.close();
				} else {
					cursor = db.query(CustomQuestionContract.TABLE_NAME, CustomQuestionContract.ALL_COLUMNS, where, null, null, null, null);
					if (cursor.getCount() > 0) {
						where = "difficulty == " + "-2";
						cursor = db.query(CustomQuestionContract.TABLE_NAME, CustomQuestionContract.ALL_COLUMNS, where, null, null, null,
								null);
					} else {
						where = "difficulty == " + "-1";
						cursor = db.query(CustomQuestionContract.TABLE_NAME, CustomQuestionContract.ALL_COLUMNS, where, null, null, null,
								null);
					}
				}
			}
			return DatabaseModelFactory.buildCustomQuestion(cursor, sum, count);
		} else
			return null;
	}

	public List<CustomQuestion> getAllCustomQuestions() {
		if (db.isOpen()) {
			String where = "difficulty >= " + 0;
			String[] columns = CustomQuestionContract.ALL_COLUMNS;
			cursor = db.query(CustomQuestionContract.TABLE_NAME, columns, where, null, null, null, null);
			return DatabaseModelFactory.buildAllCustomQuestions(cursor);
		} else
			return null;
	}

	public List<String> getAllCustomCategories() {
		if (db.isOpen()) {
			String where = " WHERE difficulty >= " + 0;
			String sql = "SELECT distinct " + CustomQuestionContract.CATEGORY + " FROM " + CustomQuestionContract.TABLE_NAME;
			cursor = db.rawQuery(sql + where, null);
			return DatabaseModelFactory.buildAllCustomCategories(cursor);
		} else
			return null;
	}

	public int removeCustomQuestion(long ID) {
		if (db.isOpen()) {
			String where = BaseContract._ID + " = " + ID;
			return db.delete(CustomQuestionContract.TABLE_NAME, where, null);
		} else
			return 0;
	}

	public long addSwisherQuestion(String[] question) {
		return addCustomQuestion(new String[] { question[0], question[1], question[2], question[3], question[4], question[5] },
				Integer.parseInt(question[6]), 750, 10);
	}

	public long addCustomQuestion(String[] question) {
		return addCustomQuestion(new String[] { question[0], question[1], question[2], question[3], question[4], question[5] },
				Integer.parseInt(question[6]), 0, 0);
	}

	public long addCustomQuestion(String[] question, int difficulty, int timeStep, int timeSteps) {
		if (db.isOpen()) {
			String select = "Select count(1) FROM " + CustomQuestionContract.TABLE_NAME;
			String where = " WHERE " + QuestionContract.QUESTION_TEXT + " = '" + question[0].replaceAll("'", "''") + "'" + " AND "
					+ QuestionContract.ANSWER_CORRECT + " = '" + question[1].replaceAll("'", "''") + "'" + " AND "
					+ CustomQuestionContract.ANSWER_INCORRECT1 + " = '" + question[2].replaceAll("'", "''") + "'" + " AND "
					+ CustomQuestionContract.ANSWER_INCORRECT2 + " = '" + question[3].replaceAll("'", "''") + "'" + " AND "
					+ CustomQuestionContract.ANSWER_INCORRECT3 + " = '" + question[4].replaceAll("'", "''") + "'" + " AND "
					+ CustomQuestionContract.CATEGORY + " = '" + question[5].replaceAll("'", "''") + "'";
			cursor = db.rawQuery(select + where, null);
			cursor.moveToFirst();
			if (cursor.getInt(0) == 0) {
				ContentValues values = new ContentValues();
				values.put(QuestionContract.QUESTION_TEXT, question[0]);
				values.put(QuestionContract.ANSWER_CORRECT, question[1]);
				values.put(CustomQuestionContract.ANSWER_INCORRECT1, question[2]);
				values.put(CustomQuestionContract.ANSWER_INCORRECT2, question[3]);
				values.put(CustomQuestionContract.ANSWER_INCORRECT3, question[4]);
				values.put(QuestionContract.DIFFICULTY, difficulty);
				values.put(QuestionContract.PRIORITY, QuestionContract.DEFAULT_PRIORITY);
				values.put(QuestionContract.TIME_STEP, timeStep);
				values.put(QuestionContract.TIME_STEPS, timeSteps);
				values.put(CustomQuestionContract.CATEGORY, question[5]);
				return db.insert(CustomQuestionContract.TABLE_NAME, null, values);
			} else
				return -1;
		} else
			return -1;
	}

	public long updateCustomQuestion(long ID, String[] question, int difficulty) {
		if (db.isOpen()) {
			String where = BaseContract._ID + " = " + ID;
			ContentValues values = new ContentValues();
			values.put(QuestionContract.QUESTION_TEXT, question[0]);
			values.put(QuestionContract.ANSWER_CORRECT, question[1]);
			values.put(CustomQuestionContract.ANSWER_INCORRECT1, question[2]);
			values.put(CustomQuestionContract.ANSWER_INCORRECT2, question[3]);
			values.put(CustomQuestionContract.ANSWER_INCORRECT3, question[4]);
			values.put(QuestionContract.DIFFICULTY, difficulty);
			values.put(QuestionContract.PRIORITY, QuestionContract.DEFAULT_PRIORITY);
			values.put(QuestionContract.TIME_STEP, 0);
			values.put(QuestionContract.TIME_STEPS, 0);
			values.put(CustomQuestionContract.CATEGORY, question[5]);
			return db.update(CustomQuestionContract.TABLE_NAME, values, where, null);
		} else
			return 0;
	}

	public List<GenericQuestion> createChallengeQuestions(ChallengeBuilder builder) {
		if (db.isOpen()) {
			int questions = builder.getNumberOfQuestions();
			List<Integer> packIDs = builder.getSelectedQuestionPackIDs();
			// remove the id for all packs if it exists
			if (packIDs.get(0) == 0) {
				packIDs.remove(0);
			}
			int packs = packIDs.size();
			Random rand = new Random();
			List<GenericQuestion> challengeQuestions = new ArrayList<GenericQuestion>(questions);
			for (int i = 0; i < questions; i++) {
				int pack = packIDs.get(rand.nextInt(packs)) - 1;
				switch (pack) {
				case 0:	// Math
					challengeQuestions.add(getGenericQuestion(getMathQuestion(Difficulty.fromValue(builder.getDifficultyMin()),
							Difficulty.fromValue(builder.getDifficultyMax()), -1)));
					break;
				case 1: // Vocab
					challengeQuestions.add(getGenericQuestionFromVocab(getVocabQuestions(Difficulty.fromValue(builder.getDifficultyMin()),
							Difficulty.fromValue(builder.getDifficultyMax()), 4, -1)));
					break;
				case 2:	// Language
					Context ctx = MainActivity.getContext();
					SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
					String fromLanguage = sharedPrefs.getString("from_language", ctx.getString(R.string.language_from_default));
					String toLanguage = sharedPrefs.getString("to_language", ctx.getString(R.string.language_to_default));
					challengeQuestions.add(getGenericQuestionFromLanguage(
							getLanguageQuestions(Difficulty.fromValue(builder.getDifficultyMin()),
									Difficulty.fromValue(builder.getDifficultyMax()), 4, fromLanguage, toLanguage, -1), fromLanguage,
							toLanguage));
					break;
				case 3: // Engineer
					challengeQuestions.add(getGenericQuestion(getEngineerQuestion(Difficulty.fromValue(builder.getDifficultyMin()),
							Difficulty.fromValue(builder.getDifficultyMax()), -1)));
					break;
				case 4:	// Hiq Trivia
					challengeQuestions.add(getGenericQuestion(getHiqTriviaQuestion(Difficulty.fromValue(builder.getDifficultyMin()),
							Difficulty.fromValue(builder.getDifficultyMax()), -1)));
					break;
				default:
					// custom question
					challengeQuestions.add(getGenericQuestion(getCustomQuestion(this.getAllCustomCategories().get(pack - 5),
							Difficulty.fromValue(builder.getDifficultyMin()), Difficulty.fromValue(builder.getDifficultyMax()), -1)));
					break;
				}
			}
			return challengeQuestions;
		} else
			return null;
	}

	private GenericQuestion getGenericQuestion(MathQuestion question) {
		question.setVariables();
		String[] answers = question.getAnswers();
		String genericQuestion = question.getQuestionText();
		return new GenericQuestion("Math", genericQuestion, answers);
	}

	private GenericQuestion getGenericQuestionFromVocab(List<VocabQuestion> question) {
		String[] answers = new String[4];
		for (int i = 0; i < answers.length; i++) {
			answers[i] = question.get(i).getCorrectAnswer();
		}
		return new GenericQuestion("Vocab", question.get(0).getQuestionText(), answers);
	}

	private GenericQuestion getGenericQuestionFromLanguage(List<LanguageQuestion> question, String fromLanguage, String toLanguage) {
		String description = fromLanguage + " → " + toLanguage;
		String[] answers = new String[4];
		for (int i = 0; i < answers.length; i++) {
			answers[i] = question.get(i).getCorrectAnswer();
		}
		return new GenericQuestion(description, question.get(0).getQuestionText(), answers);
	}

	private GenericQuestion getGenericQuestion(EngineerQuestion question) {
		return new GenericQuestion("Engineer", question.getQuestionText(), question.getAnswers());
	}

	private GenericQuestion getGenericQuestion(HiqTriviaQuestion question) {
		return new GenericQuestion("Hiq Trivia", question.getQuestionText(), question.getAnswers());
	}

	private GenericQuestion getGenericQuestion(CustomQuestion question) {
		return new GenericQuestion(question.getCategory(), question.getQuestionText(), question.getAnswers());
	}

	public ChallengeQuestion getChallengeQuestion(String challengeID) {
		if (db.isOpen()) {
			String where = ChallengeQuestionContract.CHALLENGE_ID + " = '" + challengeID + "' AND " + ChallengeQuestionContract.SCORE
					+ " < 0";
			cursor = db.query(ChallengeQuestionContract.TABLE_NAME, ChallengeQuestionContract.ALL_COLUMNS, where, null, null, null, null);
			return DatabaseModelFactory.buildChallengeQuestion(cursor);
		} else
			return null;
	}

	public int getChallengeScore(long ID) {
		if (db.isOpen()) {
			String sql = "SELECT " + ChallengeQuestionContract.CHALLENGE_ID + " FROM " + ChallengeQuestionContract.TABLE_NAME + " WHERE "
					+ BaseContract._ID + " = " + ID;
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToFirst())
				return getChallengeScore(cursor.getString(0));
			else
				return -1;
		} else
			return -1;
	}

	public int getChallengeScore(String challengeID) {
		if (db.isOpen()) {
			String where = ChallengeQuestionContract.CHALLENGE_ID + " = '" + challengeID + "'";
			cursor = db.rawQuery("SELECT SUM(" + ChallengeQuestionContract.SCORE + ") FROM " + ChallengeQuestionContract.TABLE_NAME
					+ " WHERE " + where, null);
			cursor.moveToFirst();
			return cursor.getInt(0);
		} else
			return -1;
	}

	public Map<String, ChallengeData> getChallengeIDs() {
		if (db.isOpen()) {
			cursor = db.query(ChallengeQuestionContract.TABLE_NAME, ChallengeQuestionContract.IDS_AND_SCORE, null, null, null, null, null);
			return DatabaseModelFactory.buildChallengeIDs(cursor);
		} else
			return new HashMap<String, ChallengeData>();
	}

	public Map<String, Integer> getChallengeDescriptions(String challengeID) {
		if (db.isOpen()) {
			String where = ChallengeQuestionContract.CHALLENGE_ID + " = '" + challengeID + "'";
			cursor = db.query(ChallengeQuestionContract.TABLE_NAME, ChallengeQuestionContract.ALL_COLUMNS, where, null, null, null, null);
			return DatabaseModelFactory.buildChallengeDescriptions(cursor);
		} else
			return new HashMap<String, Integer>();
	}

	public ChallengeData getRandomChallengeData(List<ContactHashes> hashes) {
		if (db.isOpen()) {
			String where = "";
			boolean first = true;
			for (ContactHashes hash : hashes) {
				if (first) {
					first = false;
					where = ChallengeQuestionContract.USER_ID + " != '" + hash.getHiqUserHash() + "'";
				} else {
					where = where + " AND " + ChallengeQuestionContract.USER_ID + " != '" + hash.getHiqUserHash() + "'";
				}
			}
			cursor = db.query(ChallengeQuestionContract.TABLE_NAME, ChallengeQuestionContract.IDS_AND_SCORE, where, null, null, null, null);
			Map<String, ChallengeData> challengeIDs = DatabaseModelFactory.buildChallengeIDs(cursor);
			if (challengeIDs.size() > 0) {
				for (Map.Entry<String, ChallengeData> entry : challengeIDs.entrySet()) {
					return entry.getValue();
				}
				return new ChallengeData("", "", 0);
			} else
				return new ChallengeData("", "", 0);
		} else
			return new ChallengeData("", "", 0);
	}

	public int removeChallengeQuestions(long ID) {
		if (db.isOpen()) {
			String sql = "SELECT " + ChallengeQuestionContract.CHALLENGE_ID + " FROM " + ChallengeQuestionContract.TABLE_NAME + " WHERE "
					+ BaseContract._ID + " = " + ID;
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToFirst())
				return removeChallengeQuestions(cursor.getString(0));
			else
				return -1;
		} else
			return -1;
	}

	public int removeChallengeQuestions(String challengeID) {
		if (db.isOpen()) {
			String where = ChallengeQuestionContract.CHALLENGE_ID + " = '" + challengeID + "'";
			return db.delete(ChallengeQuestionContract.TABLE_NAME, where, null);
		} else
			return 0;
	}

	public long addChallengeQuestions(String challengeID, String userID, List<GenericQuestion> questions, String userName) {
		if (db.isOpen()) {
			long row = -1;
			for (GenericQuestion question : questions) {
				ContentValues values = new ContentValues();
				values.put(ChallengeQuestionContract.CHALLENGE_ID, challengeID);
				values.put(ChallengeQuestionContract.USER_ID, userID);
				values.put(QuestionContract.QUESTION_TEXT, question.getQuestion());
				values.put(QuestionContract.ANSWER_CORRECT, question.getAnswers()[0]);
				values.put(ChallengeQuestionContract.ANSWER_INCORRECT1, question.getAnswers()[1]);
				values.put(ChallengeQuestionContract.ANSWER_INCORRECT2, question.getAnswers()[2]);
				values.put(ChallengeQuestionContract.ANSWER_INCORRECT3, question.getAnswers()[3]);
				values.put(ChallengeQuestionContract.USER_NAME, userName);
				values.put(ChallengeQuestionContract.CHALLENGE_DESCRIPTION, question.getDescription());
				values.put(ChallengeQuestionContract.SCORE, -1);
				row = db.insert(ChallengeQuestionContract.TABLE_NAME, null, values);
			}
			return row;
		} else
			return -1;
	}

	public long addChallengeQuestion(String challengeID, String userID, String description, String question, String[] answers,
			String userName) {
		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put(ChallengeQuestionContract.CHALLENGE_ID, challengeID);
			values.put(ChallengeQuestionContract.USER_ID, userID);
			values.put(QuestionContract.QUESTION_TEXT, question);
			values.put(QuestionContract.ANSWER_CORRECT, answers[0]);
			values.put(ChallengeQuestionContract.ANSWER_INCORRECT1, answers[1]);
			values.put(ChallengeQuestionContract.ANSWER_INCORRECT2, answers[2]);
			values.put(ChallengeQuestionContract.ANSWER_INCORRECT3, answers[3]);
			values.put(ChallengeQuestionContract.USER_NAME, userName);
			values.put(ChallengeQuestionContract.CHALLENGE_DESCRIPTION, description);
			values.put(ChallengeQuestionContract.SCORE, -1);
			return db.insert(ChallengeQuestionContract.TABLE_NAME, null, values);
		} else
			return -1;
	}

	public boolean addChallengeScore(long ID, int score) {
		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put(ChallengeQuestionContract.SCORE, score);
			String where = BaseContract._ID + "=" + ID + " AND " + ChallengeQuestionContract.SCORE + " < 0";
			db.update(ChallengeQuestionContract.TABLE_NAME, values, where, null);
			return isChallengeDone(ID);
		}
		return false;
	}

	public String getChallengeID(long ID) {
		if (db.isOpen()) {
			String sql = "SELECT " + ChallengeQuestionContract.CHALLENGE_ID + " FROM " + ChallengeQuestionContract.TABLE_NAME + " WHERE "
					+ BaseContract._ID + " = " + ID;
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToFirst())
				return cursor.getString(0);
			else
				return "";
		} else
			return "";
	}

	public boolean isChallengeDone(long ID) {
		if (db.isOpen()) {
			String sql = "SELECT " + ChallengeQuestionContract.CHALLENGE_ID + " FROM " + ChallengeQuestionContract.TABLE_NAME + " WHERE "
					+ BaseContract._ID + " = " + ID;
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToFirst())
				return isChallengeDone(cursor.getString(0));
			else
				return false;
		} else
			return false;
	}

	public boolean isChallengeDone(String challengeID) {
		if (getQuestionsToAnswer(challengeID) == 0)
			return true;
		else
			return false;
	}

	public int getQuestionsToAnswer(String challengeID) {
		if (db.isOpen()) {
			String where = ChallengeQuestionContract.CHALLENGE_ID + " = '" + challengeID + "'";
			cursor = db.query(ChallengeQuestionContract.TABLE_NAME, ChallengeQuestionContract.IDS_AND_SCORE, where, null, null, null, null);
			Map<String, ChallengeData> map = DatabaseModelFactory.buildChallengeIDs(cursor);
			return map.get(challengeID).getNumberOfQuestions();
		} else
			return -1;
	}

	public boolean isSwisherPackAdded() {
		if (db.isOpen()) {
			String where = CustomQuestionContract.CATEGORY + " = '" + PreferenceHelper.SWISHER_FILENAME + "'";
			cursor = db.query(CustomQuestionContract.TABLE_NAME, CustomQuestionContract.ALL_COLUMNS, where, null, null, null, null);
			return cursor.moveToFirst();
		} else
			return false;
	}

	public long getTotalDifficulty() {
		if (db.isOpen()) {
			String sql = "SELECT SUM(" + StatisticContract.DIFFICULTY + ") FROM " + StatisticContract.TABLE_NAME;
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToFirst())
				return cursor.getLong(0);
			else
				return 0;
		} else
			return 0;
	}

	public boolean increasePriority(String tableName, String fromLanguage, String toLanguage, long ID) {
		if (db.isOpen()) {
			if (!(tableName == null)) {
				// String tableNames[] = MainActivity.getContext().getResources().getStringArray(R.array.table_names);
				String fromLanguagePriority = fromLanguage + LanguageQuestionContract.PRIORITIES;
				String toLanguagePriority = toLanguage + LanguageQuestionContract.PRIORITIES;
				int priority[] = getPriority(tableName, fromLanguagePriority, toLanguagePriority, ID);
				priority[0] = Math.max(QuestionContract.DEFAULT_PRIORITY, priority[0] * 2);
				priority[1] = Math.max(QuestionContract.DEFAULT_PRIORITY, priority[1] * 2);
				String where = " WHERE " + BaseContract._ID + "=" + ID;
				String sql = "UPDATE " + tableName + " SET ";
				String priorityUpdate;
				if (tableName.equals(MainActivity.getContext().getResources().getString(R.string.language_table)))
					priorityUpdate = fromLanguagePriority + "=" + priority[0] + "," + toLanguagePriority + "=" + priority[1];
				else
					priorityUpdate = QuestionContract.PRIORITY + "=" + priority[0];
				sql = sql + priorityUpdate + where;
				db.execSQL(sql);
				return true;
			}
		}
		return false;
	}

	public boolean decreasePriority(String tableName, String fromLanguage, String toLanguage, long ID) {
		if (db.isOpen()) {
			if (!(tableName == null)) {
				// String tableNames[] = MainActivity.getContext().getResources().getStringArray(R.array.table_names);
				String fromLanguagePriority = fromLanguage + LanguageQuestionContract.PRIORITIES;
				String toLanguagePriority = toLanguage + LanguageQuestionContract.PRIORITIES;
				int priority[] = getPriority(tableName, fromLanguagePriority, toLanguagePriority, ID);
				priority[0] = Math.min(QuestionContract.DEFAULT_PRIORITY, priority[0] / 4);
				priority[1] = Math.min(QuestionContract.DEFAULT_PRIORITY, priority[1] / 4);
				priority[0] = Math.max(1, priority[0]);
				priority[1] = Math.max(1, priority[1]);
				String where = " WHERE " + BaseContract._ID + "=" + ID;
				String sql = "UPDATE " + tableName + " SET ";
				String priorityUpdate;
				if (tableName.equals(MainActivity.getContext().getResources().getString(R.string.language_table)))
					priorityUpdate = fromLanguagePriority + "=" + priority[0] + "," + toLanguagePriority + "=" + priority[1];
				else
					priorityUpdate = QuestionContract.PRIORITY + "=" + priority[0];
				sql = sql + priorityUpdate + where;
				db.execSQL(sql);
				return true;
			}
		}
		return false;
	}

	private int[] getPriority(String tableName, String priority1, String priority2, long ID) {
		// String tableNames[] = MainActivity.getContext().getResources().getStringArray(R.array.table_names);
		int priority[] = { 0, 0 };
		if (tableName.equals(MainActivity.getContext().getResources().getString(R.string.language_table))) {
			cursor = db.rawQuery("SELECT " + priority1 + " FROM " + tableName + " WHERE " + QuestionContract._ID + "=" + ID, null);
			cursor.moveToFirst();
			priority[0] = cursor.getInt(0);
			cursor = db.rawQuery("SELECT " + priority2 + " FROM " + tableName + " WHERE " + QuestionContract._ID + "=" + ID, null);
			cursor.moveToFirst();
			priority[1] = cursor.getInt(0);
		} else {
			cursor = db.rawQuery(
					"SELECT " + QuestionContract.PRIORITY + " FROM " + tableName + " WHERE " + QuestionContract._ID + "=" + ID, null);
			cursor.moveToFirst();
			priority[0] = cursor.getInt(0);
		}
		return priority;
	}
}
