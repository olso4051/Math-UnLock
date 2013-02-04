package com.olyware.mathlock.database;

import java.util.List;

import android.database.Cursor;

import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.LanguageQuestion;
import com.olyware.mathlock.model.VocabQuestion;
import com.olyware.mathlock.utils.EZ;

public class DatabaseModelFactory {

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
