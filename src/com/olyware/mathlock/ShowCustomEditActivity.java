package com.olyware.mathlock;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
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

	private final int MAX_LENGTH = 50;
	private int layoutID;
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

	@Override
	public void onBackPressed() {
		if (layoutID == R.layout.activity_custom_edit2)
			super.onBackPressed();
		else
			resetContentView(R.layout.activity_custom_edit2, null, -1);
	}

	private void resetContentView(int layoutResId, String addButtonText, int position) {
		final int positionFinal = position;
		layoutID = layoutResId;
		final String addButtonTextFinal = addButtonText;
		if (clock != null)
			clock.destroy();
		if (questionData != null)
			questionData.clear();
		if (adapter != null)
			adapter.clear();
		if (questions != null)
			questions.clear();

		setContentView(layoutResId);

		layout = (LinearLayout) findViewById(R.id.layout);
		EZ.setFont((ViewGroup) layout, fonts.robotoLight);

		questionData = EZ.list();
		questionData.addAll(dbManager.getAllCustomQuestions());

		clock = new Clock(this, (TextView) findViewById(R.id.clock), (TextView) findViewById(R.id.money));
		back = (ImageButton) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (layoutID == R.layout.activity_custom_edit2)
					finish();
				else
					resetContentView(R.layout.activity_custom_edit2, null, -1);
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
			for (int i = 0; i < inputs.length; i++) {
				final int a = i;
				inputs[a].setOnFocusChangeListener(new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (inputs[a].getText().toString().length() > MAX_LENGTH)
							inputs[a].setTextColor(Color.RED);
						else
							inputs[a].setTextColor(Color.WHITE);
					}
				});
			}
			difficulty = (Spinner) findViewById(R.id.spinner_difficulty);
			difficulty.setAdapter(adapterDifficulties);
			if ((positionFinal >= 0) && (positionFinal < questionData.size())) {
				inputs[0].setText(questionData.get(positionFinal).getQuestionText());
				String[] answers = questionData.get(positionFinal).getAnswers();
				for (int i = 0; i < answers.length; i++) {
					inputs[i + 1].setText(answers[i]);
				}
				difficulty.setSelection(questionData.get(positionFinal).getDifficulty().getValue());
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
						updateCustomQuestion(positionFinal, inputs[0].getText().toString(), inputs[1].getText().toString(), inputs[2]
								.getText().toString(), inputs[3].getText().toString(), inputs[4].getText().toString(), difficulty
								.getSelectedItemPosition());
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
		final String[] question = new String[] { q, a, w1, w2, w3 };
		if (testQuestion(question)) {
			final int difficulty = d;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.add_question);
			builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dbManager.addCustomQuestion(question, difficulty);
					resetContentView(R.layout.activity_custom_edit2, null, -1);
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

	private void updateCustomQuestion(int position, String q, String a, String w1, String w2, String w3, int d) {
		final int posFinal = position;
		if ((position >= 0) && (position < questionData.size())) {
			final String[] question = new String[] { q, a, w1, w2, w3 };
			if (testQuestion(question)) {
				final int difficulty = d;
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.update_question);
				builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dbManager.updateCustomQuestion(questionData.get(posFinal).getID(), question, difficulty);
						resetContentView(R.layout.activity_custom_edit2, null, -1);
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
	}

	private boolean testQuestion(String[] question) {
		boolean tooLongShort[] = new boolean[] { false, false, false, false, false };
		boolean same = false;
		List<String> q = new ArrayList<String>();
		for (int i = 0; i < question.length; i++) {
			if ((question[i].length() > MAX_LENGTH) || (question[i].length() == 0))
				tooLongShort[i] = true;
			if (i > 0)
				if (q.contains(question[i]))
					same = true;
			q.add(question[i]);
		}
		if (tooLongShort[0] || tooLongShort[1] || tooLongShort[2] || tooLongShort[3] || tooLongShort[4]) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.input_error);
			builder.setMessage(getString(R.string.max_length) + " " + MAX_LENGTH + " " + getString(R.string.max_length2));
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// Do nothing
				}
			});
			builder.create().show();
			return false;
		} else if (same) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.input_error);
			builder.setMessage(getString(R.string.same_entries));
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// Do nothing
				}
			});
			builder.create().show();
			return false;
		}
		return true;
	}
}
