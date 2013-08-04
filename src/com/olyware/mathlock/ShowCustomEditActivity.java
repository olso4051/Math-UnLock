package com.olyware.mathlock;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.model.CustomQuestion;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.ui.Typefaces;
import com.olyware.mathlock.utils.Clock;
import com.olyware.mathlock.utils.CustomArrayAdapter;
import com.olyware.mathlock.utils.EZ;

public class ShowCustomEditActivity extends Activity {

	private LinearLayout layout;
	private Typefaces fonts;
	private Clock clock;
	private ImageButton back;
	private EditText[] inputs = new EditText[5];
	private String[] difficulties = new String[Difficulty.getSize()];
	ArrayAdapter<String> adapterDifficulties;
	private Spinner difficulty;
	private Button done, cancel;
	private DatabaseManager dbManager;
	private ListView list;
	CustomArrayAdapter<String> adapter;
	private List<CustomQuestion> questionData;
	private ArrayList<String> questions;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fonts = Typefaces.getInstance(this);
		dbManager = new DatabaseManager(getApplicationContext());

		for (int i = 0; i < difficulties.length; i++) {
			difficulties[i] = Difficulty.fromValueString(i);
		}
		adapterDifficulties = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, difficulties) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView) v).setTypeface(fonts.robotoLight);
				return v;
			}

			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View v = super.getDropDownView(position, convertView, parent);
				((TextView) v).setTypeface(fonts.robotoLight);
				return v;
			}
		};
		adapterDifficulties.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		resetContentView(R.layout.activity_custom_edit2, null, -1);
	}

	@Override
	public void onAttachedToWindow() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	@Override
	protected void onDestroy() {
		clock.destroy();
		super.onDestroy();
	}

	private void resetContentView(int layoutResId, String addButtonText, int position) {
		final String addButtonTextFinal = addButtonText;
		if (clock != null)
			clock.destroy();
		if (questionData != null)
			questionData.clear();
		if (adapter != null)
			adapter.clear();

		setContentView(layoutResId);

		layout = (LinearLayout) findViewById(R.id.layout);
		EZ.setFont((ViewGroup) layout, fonts.robotoLight);

		questionData = EZ.list();
		questionData.addAll(dbManager.getAllCustomQuestions());

		clock = new Clock(this, (TextView) findViewById(R.id.clock), (TextView) findViewById(R.id.money));
		back = (ImageButton) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		if (layoutResId == R.layout.activity_custom_edit2) {
			for (int i = 0; i < inputs.length; i++) {
				inputs[i] = null;
			}
			difficulty = null;
			done = null;
			cancel = null;
			list = (ListView) findViewById(R.id.list1);

			questions = new ArrayList<String>();
			questions.add(getString(R.string.add_new));
			for (int i = 0; i < questionData.size(); i++)
				questions.add(questionData.get(i).getQuestionText());
			adapter = new CustomArrayAdapter<String>(this, R.layout.list_text_item, R.id.text, questions);
			list.setAdapter(adapter);
			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
					final int posFinal = pos;
					Log.d("test", "item selected = " + pos);
					if (pos == 0)
						resetContentView(R.layout.activity_custom_edit, getString(R.string.add), posFinal - 1);
					else {
						final Dialog d = new Dialog(ShowCustomEditActivity.this);
						View v = getLayoutInflater().inflate(R.layout.copy_edit_delete_cancel, null);
						d.setContentView(v);
						d.setTitle(R.string.question);
						((TextView) d.findViewById(R.id.message)).setText(questions.get(pos));
						((Button) d.findViewById(R.id.copy)).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								resetContentView(R.layout.activity_custom_edit, getString(R.string.add), posFinal - 1);
								d.dismiss();
							}
						});
						((Button) d.findViewById(R.id.edit)).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								resetContentView(R.layout.activity_custom_edit, getString(R.string.update), posFinal - 1);
								d.dismiss();
							}
						});
						((Button) d.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								deleteCustomQuestion(posFinal - 1);
								d.dismiss();
							}
						});
						((Button) d.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								d.dismiss();
							}
						});
						d.show();
					}
				}
			});
		} else if (layoutResId == R.layout.activity_custom_edit) {
			list = null;
			questions = null;
			inputs[0] = (EditText) findViewById(R.id.custom_question_edit_text);
			inputs[1] = (EditText) findViewById(R.id.custom_answer_edit_text);
			inputs[2] = (EditText) findViewById(R.id.custom_wrong1_edit_text);
			inputs[3] = (EditText) findViewById(R.id.custom_wrong2_edit_text);
			inputs[4] = (EditText) findViewById(R.id.custom_wrong3_edit_text);
			difficulty = (Spinner) findViewById(R.id.spinner_difficulty);
			difficulty.setAdapter(adapterDifficulties);
			if ((position >= 0) && (position < questionData.size())) {
				inputs[0].setText(questionData.get(position).getQuestionText());
				String[] answers = questionData.get(position).getAnswers();
				for (int i = 0; i < answers.length; i++) {
					inputs[i + 1].setText(answers[i]);
				}
				difficulty.setSelection(questionData.get(position).getDifficulty().getValue());
			} else
				difficulty.setSelection(0);
			done = (Button) findViewById(R.id.done);
			done.setText(addButtonTextFinal);
			done.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (addButtonTextFinal.equals(getString(R.string.add)))
						addCustomQuestion(inputs[0].getText().toString(), inputs[1].getText().toString(), inputs[2].getText().toString(),
								inputs[3].getText().toString(), inputs[4].getText().toString(), difficulty.getSelectedItemPosition());
					else if (addButtonTextFinal.equals(getString(R.string.update)))
						updateCustomQuestion(inputs[0].getText().toString(), inputs[1].getText().toString(),
								inputs[2].getText().toString(), inputs[3].getText().toString(), inputs[4].getText().toString(),
								difficulty.getSelectedItemPosition());
					resetContentView(R.layout.activity_custom_edit2, null, -1);
				}
			});
			cancel = (Button) findViewById(R.id.cancel);
			cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					resetContentView(R.layout.activity_custom_edit2, null, -1);
				}
			});
		}
	}

	private void deleteCustomQuestion(int position) {
		final int posFinal = position;
		if ((position >= 0) && (position < questionData.size())) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.delete_question);
			builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dbManager.removeCustomQuestion(questionData.get(posFinal).getID());
					questionData.remove(posFinal);
					questions.remove(posFinal + 1);
					adapter.notifyDataSetChanged();
				}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// Do nothing
				}
			});
			builder.create().show();
		}
	}

	private void addCustomQuestion(String q, String a, String w1, String w2, String w3, int d) {

	}

	private void updateCustomQuestion(String q, String a, String w1, String w2, String w3, int d) {

	}
}
