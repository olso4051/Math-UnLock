package com.olyware.mathlock.database;

import java.util.List;
import java.util.Random;

import android.database.Cursor;

import com.olyware.mathlock.database.contracts.BaseContract;
import com.olyware.mathlock.database.contracts.ChallengeQuestionContract;
import com.olyware.mathlock.database.contracts.CustomQuestionContract;
import com.olyware.mathlock.database.contracts.EngineerQuestionContract;
import com.olyware.mathlock.database.contracts.HiqTriviaQuestionContract;
import com.olyware.mathlock.database.contracts.LanguageQuestionContract;
import com.olyware.mathlock.database.contracts.MathQuestionContract;
import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.database.contracts.StatisticContract;
import com.olyware.mathlock.model.ChallengeQuestion;
import com.olyware.mathlock.model.CustomQuestion;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.EngineerQuestion;
import com.olyware.mathlock.model.HiqTriviaQuestion;
import com.olyware.mathlock.model.LanguageQuestion;
import com.olyware.mathlock.model.MathQuestion;
import com.olyware.mathlock.model.MathQuestion.ParseMode;
import com.olyware.mathlock.model.VocabQuestion;
import com.olyware.mathlock.utils.EZ;

public class DatabaseModelFactory {

	public static List<Integer> buildStats(Cursor cursor) {
		List<Integer> stats = EZ.list();
		cursor.moveToFirst();
		CursorHelper cursorHelper = new CursorHelper(cursor);
		while (!cursor.isAfterLast()) {
			cursorHelper.setCursor(cursor);
			if (Boolean.parseBoolean(cursorHelper.getString(StatisticContract.CORRECT)))
				stats.add(100);
			else
				stats.add(0);
			cursor.moveToNext();
		}
		cursor.close();
		cursorHelper.destroy();
		return stats;
	}

	public static MathQuestion buildMathQuestion(Cursor cursor, int weightSum) {
		Random rand = new Random();
		int selection = rand.nextInt(weightSum) + 1;
		int cumulativeWeight = 0;
		cursor.moveToFirst();
		CursorHelper cursorHelper = new CursorHelper(cursor);
		while (!cursor.isLast()) {
			cursorHelper.setCursor(cursor);
			cumulativeWeight += cursorHelper.getInteger(QuestionContract.PRIORITY);
			if (cumulativeWeight >= selection) {
				break;
			}
			cursor.moveToNext();
		}
		cursorHelper.setCursor(cursor);
		long id = cursorHelper.getLong(QuestionContract._ID);
		String questionText = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
		String questionImage = cursorHelper.getString(MathQuestionContract.QUESTION_IMAGE);
		String correctAnswer = cursorHelper.getString(QuestionContract.ANSWER_CORRECT);
		String incorrectAnswer1 = cursorHelper.getString(MathQuestionContract.ANSWER_INCORRECT1);
		String incorrectAnswer2 = cursorHelper.getString(MathQuestionContract.ANSWER_INCORRECT2);
		String incorrectAnswer3 = cursorHelper.getString(MathQuestionContract.ANSWER_INCORRECT3);
		Difficulty difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
		ParseMode parseMode = ParseMode.fromValue(cursorHelper.getInteger(MathQuestionContract.PARSE_MODE));
		String range = cursorHelper.getString(MathQuestionContract.RANGE);
		int precision = cursorHelper.getInteger(MathQuestionContract.PRECISION);
		int priority = cursorHelper.getInteger(QuestionContract.PRIORITY);
		int timeStep = cursorHelper.getInteger(QuestionContract.TIME_STEP);
		int timeSteps = cursorHelper.getInteger(QuestionContract.TIME_STEPS);
		cursor.close();
		cursorHelper.destroy();
		return new MathQuestion(id, questionText, questionImage, correctAnswer, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3,
				difficulty, parseMode, range, precision, priority, timeStep, timeSteps);
	}

	public static List<VocabQuestion> buildVocabQuestions(Cursor cursor, int weightSum, int numOfWrongs) {
		Random rand = new Random();
		int selection = rand.nextInt(weightSum) + 1;
		int cumulativeWeight = 0;
		List<VocabQuestion> questions = EZ.list();
		cursor.moveToFirst();
		CursorHelper cursorHelper = new CursorHelper(cursor);
		// get weighted random vocab question
		while (!cursor.isLast()) {
			cursorHelper.setCursor(cursor);
			cumulativeWeight += cursorHelper.getInteger(QuestionContract.PRIORITY);
			if (cumulativeWeight >= selection) {
				break;
			}
			cursor.moveToNext();
		}
		cursorHelper.setCursor(cursor);
		long id = cursorHelper.getLong(QuestionContract._ID);
		String correctAnswer = cursorHelper.getString(QuestionContract.ANSWER_CORRECT);
		Difficulty difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
		String questionText = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
		int priority = cursorHelper.getInteger(QuestionContract.PRIORITY);
		int timeStep = cursorHelper.getInteger(QuestionContract.TIME_STEP);
		int timeSteps = cursorHelper.getInteger(QuestionContract.TIME_STEPS);
		// PartOfSpeech partOfSpeech = PartOfSpeech.fromValue(cursorHelper.getString(VocabQuestionContract.PART_OF_SPEECH));
		VocabQuestion question = new VocabQuestion(id, questionText, correctAnswer, difficulty, null, priority, timeStep, timeSteps);
		questions.add(question);
		// get three more random question for the wrong answers
		for (int i = 0; i < numOfWrongs; i++) {
			while (true) {
				cursor.moveToPosition(rand.nextInt(cursor.getCount()));
				cursorHelper.setCursor(cursor);
				id = cursorHelper.getLong(QuestionContract._ID);
				correctAnswer = cursorHelper.getString(QuestionContract.ANSWER_CORRECT);
				difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
				questionText = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
				priority = cursorHelper.getInteger(QuestionContract.PRIORITY);
				timeStep = cursorHelper.getInteger(QuestionContract.TIME_STEP);
				timeSteps = cursorHelper.getInteger(QuestionContract.TIME_STEPS);
				// PartOfSpeech partOfSpeech = PartOfSpeech.fromValue(cursorHelper.getString(VocabQuestionContract.PART_OF_SPEECH));
				question = new VocabQuestion(id, questionText, correctAnswer, difficulty, null, priority, timeStep, timeSteps);
				if (!questions.contains(question)) {
					questions.add(question);	// add question then done for this iteration
					break;
				}
			}
		}
		cursor.close();
		cursorHelper.destroy();
		return questions;
	}

	public static List<LanguageQuestion> buildLanguageQuestions(Cursor cursor, String fromLanguage, String toLanguage, int weightSum,
			int numOfWrongs) {
		String fromLanguagePriority = fromLanguage + LanguageQuestionContract.PRIORITIES;
		String toLanguagePriority = toLanguage + LanguageQuestionContract.PRIORITIES;
		List<LanguageQuestion> questions = EZ.list();
		List<String> answers = EZ.list();

		// get weighted random language question, skip null questions and answers
		Random rand = new Random();
		String correctAnswer = null, questionText = null;
		CursorHelper cursorHelper = new CursorHelper(cursor);
		do {
			int selection = rand.nextInt(weightSum) + 1;
			int cumulativeWeight = 0;
			cursor.moveToFirst();
			while (!cursor.isLast()) {
				cursorHelper.setCursor(cursor);
				cumulativeWeight += cursorHelper.getInteger(fromLanguagePriority) + cursorHelper.getInteger(toLanguagePriority);
				if (cumulativeWeight >= selection) {
					break;
				}
				cursor.moveToNext();
			}
			cursorHelper.setCursor(cursor);
			correctAnswer = cursorHelper.getString(toLanguage);
			questionText = cursorHelper.getString(fromLanguage);
		} while ((correctAnswer == null) || (questionText == null));

		long id = cursorHelper.getLong(QuestionContract._ID);
		Difficulty difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
		int priority = cursorHelper.getInteger(fromLanguagePriority) + cursorHelper.getInteger(toLanguagePriority);
		int timeStep = cursorHelper.getInteger(QuestionContract.TIME_STEP);
		int timeSteps = cursorHelper.getInteger(QuestionContract.TIME_STEPS);
		LanguageQuestion question = new LanguageQuestion(id, questionText, correctAnswer, difficulty, priority, timeStep, timeSteps);
		answers.add(correctAnswer);
		questions.add(question);

		// get three more random question for the wrong answers, don't duplicate answers
		for (int i = 0; i < numOfWrongs; i++) {
			while (true) {
				cursor.moveToPosition(rand.nextInt(cursor.getCount()));
				cursorHelper.setCursor(cursor);
				id = cursorHelper.getLong(QuestionContract._ID);
				correctAnswer = cursorHelper.getString(toLanguage);
				difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
				questionText = cursorHelper.getString(fromLanguage);
				priority = cursorHelper.getInteger(fromLanguagePriority) + cursorHelper.getInteger(toLanguagePriority);
				timeStep = cursorHelper.getInteger(QuestionContract.TIME_STEP);
				timeSteps = cursorHelper.getInteger(QuestionContract.TIME_STEPS);
				question = new LanguageQuestion(id, questionText, correctAnswer, difficulty, priority, timeStep, timeSteps);
				if (!questions.contains(question) && !answers.contains(correctAnswer) && correctAnswer != null) {
					answers.add(correctAnswer);
					questions.add(question);	// add question then done for this iteration
					break;
				}
			}
		}
		cursor.close();
		cursorHelper.destroy();
		return questions;
	}

	public static EngineerQuestion buildEngineerQuestion(Cursor cursor, int weightSum) {
		Random rand = new Random();
		int selection = rand.nextInt(weightSum) + 1;
		int cumulativeWeight = 0;
		cursor.moveToFirst();
		CursorHelper cursorHelper = new CursorHelper(cursor);
		while (!cursor.isLast()) {
			cursorHelper.setCursor(cursor);
			cumulativeWeight += cursorHelper.getInteger(QuestionContract.PRIORITY);
			if (cumulativeWeight >= selection) {
				break;
			}
			cursor.moveToNext();
		}
		cursorHelper.setCursor(cursor);
		long id = cursorHelper.getLong(QuestionContract._ID);
		String questionText = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
		String variables = cursorHelper.getString(EngineerQuestionContract.VARIABLES);
		Difficulty difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
		int priority = cursorHelper.getInteger(QuestionContract.PRIORITY);
		int timeStep = cursorHelper.getInteger(QuestionContract.TIME_STEP);
		int timeSteps = cursorHelper.getInteger(QuestionContract.TIME_STEPS);
		cursor.close();
		cursorHelper.destroy();
		return new EngineerQuestion(id, questionText, variables, difficulty, priority, timeStep, timeSteps);
	}

	public static HiqTriviaQuestion buildHiqHTriviaQuestion(Cursor cursor, int weightSum) {
		Random rand = new Random();
		int selection = rand.nextInt(weightSum) + 1;
		int cumulativeWeight = 0;
		cursor.moveToFirst();
		CursorHelper cursorHelper = new CursorHelper(cursor);
		while (!cursor.isLast()) {
			cursorHelper.setCursor(cursor);
			cumulativeWeight += cursorHelper.getInteger(QuestionContract.PRIORITY);
			if (cumulativeWeight >= selection) {
				break;
			}
			cursor.moveToNext();
		}
		cursorHelper.setCursor(cursor);
		long id = cursorHelper.getLong(QuestionContract._ID);
		String questionText = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
		String correctAnswer = cursorHelper.getString(QuestionContract.ANSWER_CORRECT);
		String incorrectAnswer1 = cursorHelper.getString(HiqTriviaQuestionContract.ANSWER_INCORRECT1);
		String incorrectAnswer2 = cursorHelper.getString(HiqTriviaQuestionContract.ANSWER_INCORRECT2);
		String incorrectAnswer3 = cursorHelper.getString(HiqTriviaQuestionContract.ANSWER_INCORRECT3);
		Difficulty difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
		int priority = cursorHelper.getInteger(QuestionContract.PRIORITY);
		int timeStep = cursorHelper.getInteger(QuestionContract.TIME_STEP);
		int timeSteps = cursorHelper.getInteger(QuestionContract.TIME_STEPS);
		cursor.close();
		cursorHelper.destroy();
		return new HiqTriviaQuestion(id, questionText, correctAnswer, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3, difficulty,
				priority, timeStep, timeSteps);
	}

	public static CustomQuestion buildCustomQuestion(Cursor cursor, int weightSum) {
		Random rand = new Random();
		int selection = 0;
		if (weightSum > 0)
			selection = rand.nextInt(weightSum) + 1;
		int cumulativeWeight = 0;
		cursor.moveToFirst();
		CursorHelper cursorHelper = new CursorHelper(cursor);
		while (!cursor.isLast()) {
			cursorHelper.setCursor(cursor);
			cumulativeWeight += cursorHelper.getInteger(QuestionContract.PRIORITY);
			if (cumulativeWeight >= selection) {
				break;
			}
			cursor.moveToNext();
		}
		cursorHelper.setCursor(cursor);
		long id = cursorHelper.getLong(QuestionContract._ID);
		String questionText = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
		String correctAnswer = cursorHelper.getString(QuestionContract.ANSWER_CORRECT);
		String incorrectAnswer1 = cursorHelper.getString(CustomQuestionContract.ANSWER_INCORRECT1);
		String incorrectAnswer2 = cursorHelper.getString(CustomQuestionContract.ANSWER_INCORRECT2);
		String incorrectAnswer3 = cursorHelper.getString(CustomQuestionContract.ANSWER_INCORRECT3);
		Difficulty difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
		int priority = cursorHelper.getInteger(QuestionContract.PRIORITY);
		int timeStep = cursorHelper.getInteger(QuestionContract.TIME_STEP);
		int timeSteps = cursorHelper.getInteger(QuestionContract.TIME_STEPS);
		String category = cursorHelper.getString(CustomQuestionContract.CATEGORY);
		cursor.close();
		cursorHelper.destroy();
		return new CustomQuestion(id, questionText, correctAnswer, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3, difficulty,
				priority, timeStep, timeSteps, category);
	}

	public static List<CustomQuestion> buildAllCustomQuestions(Cursor cursor) {
		List<CustomQuestion> questions = EZ.list();
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			CursorHelper cursorHelper = new CursorHelper(cursor);
			long id;
			int priority, timeStep, timeSteps;
			String text, answer, wrong1, wrong2, wrong3, category;
			while (!cursor.isAfterLast()) {
				cursorHelper.setCursor(cursor);
				id = cursorHelper.getLong(QuestionContract._ID);
				text = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
				answer = cursorHelper.getString(QuestionContract.ANSWER_CORRECT);
				wrong1 = cursorHelper.getString(CustomQuestionContract.ANSWER_INCORRECT1);
				wrong2 = cursorHelper.getString(CustomQuestionContract.ANSWER_INCORRECT2);
				wrong3 = cursorHelper.getString(CustomQuestionContract.ANSWER_INCORRECT3);
				Difficulty difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
				priority = cursorHelper.getInteger(QuestionContract.PRIORITY);
				timeStep = cursorHelper.getInteger(QuestionContract.TIME_STEP);
				timeSteps = cursorHelper.getInteger(QuestionContract.TIME_STEPS);
				category = cursorHelper.getString(CustomQuestionContract.CATEGORY);
				questions.add(new CustomQuestion(id, text, answer, wrong1, wrong2, wrong3, difficulty, priority, timeStep, timeSteps,
						category));
				cursor.moveToNext();
			}
			cursor.close();
			cursorHelper.destroy();
		}
		return questions;
	}

	public static List<String> buildAllCustomCategories(Cursor cursor) {
		List<String> categories = EZ.list();
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			CursorHelper cursorHelper = new CursorHelper(cursor);
			while (!cursor.isAfterLast()) {
				cursorHelper.setCursor(cursor);
				categories.add(cursorHelper.getString(CustomQuestionContract.CATEGORY));
				cursor.moveToNext();
			}
			cursor.close();
			cursorHelper.destroy();
		}
		return categories;
	}

	public static ChallengeQuestion buildChallengeQuestion(Cursor cursor) {
		cursor.moveToFirst();
		CursorHelper cursorHelper = new CursorHelper(cursor);
		cursorHelper.setCursor(cursor);
		long id = cursorHelper.getLong(BaseContract._ID);
		String challengeID = cursorHelper.getString(ChallengeQuestionContract.CHALLENGE_ID);
		String questionText = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
		String correctAnswer = cursorHelper.getString(QuestionContract.ANSWER_CORRECT);
		String incorrectAnswer1 = cursorHelper.getString(ChallengeQuestionContract.ANSWER_INCORRECT1);
		String incorrectAnswer2 = cursorHelper.getString(ChallengeQuestionContract.ANSWER_INCORRECT2);
		String incorrectAnswer3 = cursorHelper.getString(ChallengeQuestionContract.ANSWER_INCORRECT3);
		String userName = cursorHelper.getString(ChallengeQuestionContract.USER_NAME);
		String[] answers = new String[] { correctAnswer, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3 };
		cursor.close();
		cursorHelper.destroy();
		return new ChallengeQuestion(id, challengeID, questionText, answers, userName);
	}
}
