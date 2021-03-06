package com.olyware.mathlock.database;

import java.util.List;

import android.provider.BaseColumns;

import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.database.contracts.VocabQuestionContract;
import com.olyware.mathlock.utils.EZ;

public class SchemaBuilder {

	/**
	 * @return a list of CREATE TABLE statments to be executed
	 */
	public static List<String> buildCreateTableSql() {
		List<String> statements = EZ.list();

		// t_user
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE TABLE " + VocabQuestionContract.TABLE_NAME + " (");
		sqlBuilder.append(BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sqlBuilder.append(QuestionContract.QUESTION_TEXT + " STRING, ");
		sqlBuilder.append(QuestionContract.ANSWER_CORRECT + " STRING,");
		sqlBuilder.append(QuestionContract.DIFFICULTY + " INTEGER,");
		// sqlBuilder.append(VocabQuestionContract.PART_OF_SPEECH + " STRING");
		sqlBuilder.append(");\n");
		statements.add(sqlBuilder.toString());

		return statements;
	}
}
