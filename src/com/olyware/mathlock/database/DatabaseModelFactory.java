package com.olyware.mathlock.database;

import java.util.List;
import java.util.Random;

import android.database.Cursor;

import com.olyware.mathlock.database.contracts.MathQuestionContract;
import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.database.contracts.StatisticContract;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.LanguageQuestion;
import com.olyware.mathlock.model.MathQuestion;
import com.olyware.mathlock.model.MathQuestion.ParseMode;
import com.olyware.mathlock.model.VocabQuestion;
import com.olyware.mathlock.utils.EZ;

public class DatabaseModelFactory {

	public static List<Integer> buildStats(Cursor cursor) {
		List<Integer> stats = EZ.list();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			CursorHelper cursorHelper = new CursorHelper(cursor);
			if (Boolean.parseBoolean(cursorHelper.getString(StatisticContract.CORRECT)))
				stats.add(100);
			else
				stats.add(0);
			cursor.moveToNext();
		}
		return stats;
	}

	public static MathQuestion buildMathQuestion(Cursor cursor, int weighSum) {
		Random rand = new Random();
		int selection = rand.nextInt(weighSum) + 1;
		int cumulativeWeight = 0;
		cursor.moveToFirst();
		while (!cursor.isLast()) {
			CursorHelper cursorHelper = new CursorHelper(cursor);
			cumulativeWeight += cursorHelper.getInteger(MathQuestionContract.PRIORITY);
			if (cumulativeWeight >= selection) {
				break;
			}
			cursor.moveToNext();
		}
		CursorHelper cursorHelper = new CursorHelper(cursor);
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
		int priority = cursorHelper.getInteger(MathQuestionContract.PRIORITY);
		return new MathQuestion(questionText, questionImage, correctAnswer, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3,
				difficulty, parseMode, range, precision, priority);
	}

	public static List<VocabQuestion> buildVocabQuestions(Cursor cursor) {
		List<VocabQuestion> questions = EZ.list();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			CursorHelper cursorHelper = new CursorHelper(cursor);
			String correctAnswer = cursorHelper.getString(QuestionContract.ANSWER_CORRECT);
			Difficulty difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
			String questionText = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
			// PartOfSpeech partOfSpeech = PartOfSpeech.fromValue(cursorHelper.getString(VocabQuestionContract.PART_OF_SPEECH));
			VocabQuestion question = new VocabQuestion(questionText, correctAnswer, difficulty, null);
			questions.add(question);

			cursor.moveToNext();
		}
		return questions;
	}

	public static VocabQuestion buildVocabQuestion(Cursor cursor) {
		cursor.moveToFirst();
		CursorHelper cursorHelper = new CursorHelper(cursor);
		String correctAnswer = cursorHelper.getString(QuestionContract.ANSWER_CORRECT);
		Difficulty difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
		String questionText = cursorHelper.getString(QuestionContract.QUESTION_TEXT);
		return new VocabQuestion(questionText, correctAnswer, difficulty, null);
	}

	public static List<LanguageQuestion> buildLanguageQuestions(Cursor cursor, String fromLanguage, String toLanguage) {
		List<LanguageQuestion> questions = EZ.list();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			CursorHelper cursorHelper = new CursorHelper(cursor);
			String correctAnswer = cursorHelper.getString(toLanguage);
			Difficulty difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
			String questionText = cursorHelper.getString(fromLanguage);
			LanguageQuestion question = new LanguageQuestion(questionText, correctAnswer, difficulty);
			questions.add(question);

			cursor.moveToNext();
		}
		return questions;
	}
}
