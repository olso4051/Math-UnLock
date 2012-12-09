package com.olyware.mathlock.database;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.database.Cursor;

import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.model.Question;
import com.olyware.mathlock.utils.EZ;

public class DatabaseModelFactory {

	public static List<Question> buildQuestions(Cursor cursor) {
		List<Question> questions = EZ.list();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			CursorHelper cursorHelper = new CursorHelper(cursor);
			String correctAnswer = cursorHelper.getString(QuestionContract.ANSWER_CORRECT);
			String wrongAnswerStr = cursorHelper.getString(QuestionContract.ANSWER_WRONG);
			Difficulty difficulty = Difficulty.fromValue(cursorHelper.getInteger(QuestionContract.DIFFICULTY));
			String questionImage = cursorHelper.getString(QuestionContract.QUESTION_IMAGE);
			String questionText = cursorHelper.getString(QuestionContract.QUESTION_TEXT);

			List<String> wrongAnswers = EZ.list(StringUtils.split(wrongAnswerStr, ","));

			Question question = new Question(questionText, new File(questionImage), correctAnswer, wrongAnswers, difficulty);
			questions.add(question);

			cursor.moveToNext();
		}
		return questions;
	}
}
