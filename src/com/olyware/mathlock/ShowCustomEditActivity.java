package com.olyware.mathlock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.olyware.mathlock.adapter.CustomArrayAdapter;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.database.contracts.CustomQuestionContract;
import com.olyware.mathlock.database.contracts.QuestionContract;
import com.olyware.mathlock.model.CustomQuestion;
import com.olyware.mathlock.model.Difficulty;
import com.olyware.mathlock.ui.Typefaces;
import com.olyware.mathlock.utils.CSVReader;
import com.olyware.mathlock.utils.Clock;
import com.olyware.mathlock.utils.Coins;
import com.olyware.mathlock.utils.EZ;
import com.olyware.mathlock.utils.EggHelper;
import com.olyware.mathlock.utils.FileDialog;
import com.olyware.mathlock.utils.MoneyHelper;

public class ShowCustomEditActivity extends FragmentActivity {

	private final String FTYPE = ".csv";
	final private static String SCREEN_LABEL = "Custom Screen";
	public final static int MAX_LENGTH = 50;
	private File sdFilePath;
	private FileDialog fileDialog;
	private int layoutID;
	private LinearLayout layout;
	private Typefaces fonts;
	private Clock clock;
	private ImageButton back;
	private TextView moneyText;
	private EditText[] inputs = new EditText[6];
	private String cat;
	private String[] difficulties = new String[Difficulty.getSize()];
	private String[] EggKeys;
	private int[] EggMaxValues;
	ArrayAdapter<String> adapterDifficulties, adapterCategories;
	private Spinner difficulty, category;
	private Button done, cancel;
	private DatabaseManager dbManager;
	private ListView list;
	CustomArrayAdapter<String> adapter;
	private List<CustomQuestion> questionData;
	private List<Integer> ids;
	private ArrayList<String> questions, categories;
	private Coins Money = new Coins(0, 0);

	private class OpenDatabase extends AsyncTask<Void, Integer, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			publishProgress(0);
			dbManager = new DatabaseManager(getApplicationContext());
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void v) {
			resetContentView(R.layout.activity_custom_edit2, null, -1);
			super.onPostExecute(v);
		}
	}

	private class addCustomPack extends AsyncTask<Void, Integer, Integer> {
		private ArrayList<String[]> newQuestions = new ArrayList<String[]>();

		public addCustomPack(ArrayList<String[]> newQuestions) {
			this.newQuestions.addAll(newQuestions);
		}

		@Override
		protected Integer doInBackground(Void... v) {
			int count = 0;
			for (int i = 0; i < newQuestions.size(); i++) {
				long id = dbManager.addCustomQuestion(newQuestions.get(i));
				cat = id >= 0 ? newQuestions.get(i)[5] : cat;
				count += id >= 0 ? 1 : 0;
			}
			if (count > 0) {
				categories.clear();
				categories.add(getString(R.string.category_all));
				questionData = EZ.list(dbManager.getAllCustomQuestions());
				for (int i = 0; i < questionData.size(); i++) {
					if (!categories.contains(questionData.get(i).getCategory()))
						categories.add(questionData.get(i).getCategory());
				}
				questions.clear();
				ids.clear();
				questions.add(getString(R.string.howto_import_csv));
				questions.add(getString(R.string.add_new_pack));
				questions.add(getString(R.string.add_new));
				for (int i = 0; i < questionData.size(); i++) {
					if (questionData.get(i).getCategory().equals(cat) || cat.equals(getString(R.string.category_all))) {
						ids.add(i);
						questions.add(questionData.get(i).getQuestionText());
					}
				}
				return count;
			} else
				return -1;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result > 0) {
				sendEvent("custom_question", "add_pack", cat, (long) result);
				Money.increaseMoney(EggHelper.unlockEgg(ShowCustomEditActivity.this, moneyText, null, EggKeys[16], EggMaxValues[16]));
				SharedPreferences sharedPrefsMoney = getSharedPreferences("Packages", 0);
				SharedPreferences.Editor editorPrefsMoney = sharedPrefsMoney.edit();
				Money.decreaseMoneyAndPaidWithDebt(result);
				editorPrefsMoney.putInt("paid_money", Money.getMoneyPaid());
				editorPrefsMoney.putInt("money", Money.getMoney());
				editorPrefsMoney.commit();
				MoneyHelper.setMoney(ShowCustomEditActivity.this, moneyText, null, Money.getMoney(), Money.getMoneyPaid(), 0);
				adapter.notifyDataSetChanged();
				adapterCategories.notifyDataSetChanged();
				Toast.makeText(ShowCustomEditActivity.this, "Uploaded " + result + " question(s)", Toast.LENGTH_LONG).show();
			} else
				Toast.makeText(ShowCustomEditActivity.this, "Uploaded 0 question(s)", Toast.LENGTH_LONG).show();
			category.setEnabled(true);
			list.setEnabled(true);
			super.onPostExecute(result);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new OpenDatabase().execute();

		EggKeys = getResources().getStringArray(R.array.egg_keys);
		EggMaxValues = getResources().getIntArray(R.array.egg_max_values);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		sdFilePath = new File(sharedPrefs.getString("hiq_path", Environment.getExternalStorageDirectory() + ""));// + "//yourdir//");

		fonts = Typefaces.getInstance(this);

		for (int i = 0; i < difficulties.length; i++) {
			difficulties[i] = Difficulty.fromValueToString(i);
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

		ids = EZ.list();

		fileDialog = new FileDialog(this, sdFilePath, FTYPE);
		fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
			public void fileSelected(final File file) {
				final String fileName = FilenameUtils.removeExtension(file.getName());
				String filePath = FilenameUtils.removeExtension(file.getParent());
				ArrayList<String[]> tempQuestions = new ArrayList<String[]>();
				int lines = -1;
				try {
					FileReader fileR = new FileReader(file);
					CSVReader csvReader = new CSVReader(fileR);
					String[] lineEntries = csvReader.readNext();
					if (lineEntries[0].equals(QuestionContract.QUESTION_TEXT) && lineEntries[1].equals(QuestionContract.ANSWER_CORRECT)
							&& lineEntries[2].equals(CustomQuestionContract.ANSWER_INCORRECT1)
							&& lineEntries[3].equals(CustomQuestionContract.ANSWER_INCORRECT2)
							&& lineEntries[4].equals(CustomQuestionContract.ANSWER_INCORRECT3)
							&& lineEntries[5].equals(QuestionContract.DIFFICULTY)) {
						lines++;
						lineEntries = csvReader.readNext();
						while (lineEntries != null) {
							if (lineEntries.length == 6) {
								if (Difficulty.isDifficulty(lineEntries[5])) {
									if (testQuestion(lineEntries[0], lineEntries[1], lineEntries[2], lineEntries[3], lineEntries[4],
											fileName)) {
										tempQuestions.add(new String[] { lineEntries[0], lineEntries[1], lineEntries[2], lineEntries[3],
												lineEntries[4], fileName, lineEntries[5] });
										lines += 1;
									}
								}
							}
							lineEntries = csvReader.readNext();
						}
					} else {
						lines = 0;
					}
					csvReader.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				final ArrayList<String[]> tempQuestionsFinal = tempQuestions;
				AlertDialog.Builder builder = new AlertDialog.Builder(ShowCustomEditActivity.this);
				builder.setTitle(FilenameUtils.removeExtension(file.getName())).setCancelable(false);
				if (Money.getMoney() + Money.getMoneyPaid() >= lines) {
					builder.setMessage(getString(R.string.purchase_package_custom_message) + " " + lines + " " + getString(R.string.coins));
					builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							questions.clear();
							questions.add(getString(R.string.db_loading));
							adapter.notifyDataSetChanged();
							category.setEnabled(false);
							list.setEnabled(false);
							new addCustomPack(tempQuestionsFinal).execute();
						}
					});
					builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
				} else {
					builder.setMessage(getString(R.string.not_enough_coins));
					builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
				}
				builder.create().show();
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ShowCustomEditActivity.this);
				SharedPreferences.Editor editorPrefs = sharedPrefs.edit();
				editorPrefs.putString("hiq_path", filePath).commit();
			}
		});

		resetContentView(R.layout.activity_custom_edit2, null, -1);

		MyApplication.getGaTracker().set(Fields.SCREEN_NAME, SCREEN_LABEL);
	}

	@Override
	protected void onStart() {
		super.onStart();
		MyApplication.getGaTracker().send(MapBuilder.createAppView().build());
	}

	@Override
	protected void onResume() {
		super.onResume();
		initMoney();
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
		final String addButtonTextFinal = addButtonText;
		final boolean dbOpened = (dbManager != null);
		layoutID = layoutResId;
		if (clock != null)
			clock.destroy();
		if (questionData != null)
			questionData.clear();
		if (adapter != null)
			adapter.clear();
		if (questions != null)
			questions.clear();
		if (categories != null)
			categories.clear();
		if (adapterCategories != null)
			adapterCategories.clear();
		ids.clear();

		setContentView(layoutResId);

		layout = (LinearLayout) findViewById(R.id.layout);
		EZ.setFont((ViewGroup) layout, fonts.robotoLight);

		categories = new ArrayList<String>();
		if (layoutResId == R.layout.activity_custom_edit2)
			categories.add(getString(R.string.category_all));

		if (dbOpened) {
			questionData = EZ.list(dbManager.getAllCustomQuestions());
			for (int i = 0; i < questionData.size(); i++) {
				if (!categories.contains(questionData.get(i).getCategory()))
					categories.add(questionData.get(i).getCategory());
			}
		} else {
			questionData = EZ.list();
		}
		if (layoutResId == R.layout.activity_custom_edit)
			categories.add(getString(R.string.add_new_category));
		cat = getString(R.string.category_all);

		adapterCategories = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
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
		adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		clock = new Clock(this, (TextView) findViewById(R.id.clock), (TextView) findViewById(R.id.money));
		moneyText = (TextView) findViewById(R.id.money);

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

			category = (Spinner) findViewById(R.id.spinner_category);
			category.setAdapter(adapterCategories);
			category.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					cat = category.getItemAtPosition(pos).toString();
					questions.clear();
					ids.clear();
					if (dbOpened) {
						questions.add(getString(R.string.howto_import_csv));
						questions.add(getString(R.string.add_new_pack));
						questions.add(getString(R.string.add_new));
						for (int i = 0; i < questionData.size(); i++) {
							if (questionData.get(i).getCategory().equals(cat) || cat.equals(getString(R.string.category_all))) {
								ids.add(i);
								questions.add(questionData.get(i).getQuestionText());
							}
						}
					} else
						questions.add(getString(R.string.db_loading));
					adapter = new CustomArrayAdapter<String>(ShowCustomEditActivity.this, R.layout.list_text_item, R.id.text, questions);
					list.setAdapter(adapter);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

			questions = new ArrayList<String>();
			if (dbOpened) {
				questions.add(getString(R.string.howto_import_csv));
				questions.add(getString(R.string.add_new_pack));
				questions.add(getString(R.string.add_new));
				for (int i = 0; i < questionData.size(); i++) {
					ids.add(i);
					questions.add(questionData.get(i).getQuestionText());
				}
			} else
				questions.add(getString(R.string.db_loading));
			adapter = new CustomArrayAdapter<String>(this, R.layout.list_text_item, R.id.text, questions);
			list.setAdapter(adapter);
			if (dbOpened) {
				list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
						if (pos == 0)
							showHowTo();
						else if (pos == 1)
							fileDialog.showDialog();
						else if (pos == 2)
							resetContentView(R.layout.activity_custom_edit, getString(R.string.add), -1);
						else {
							final int posFinal = ids.get(pos - 3);
							final Dialog d = new Dialog(ShowCustomEditActivity.this);
							View v = getLayoutInflater().inflate(R.layout.copy_edit_delete_cancel, null);
							d.setContentView(v);
							d.setTitle(R.string.question);
							((TextView) d.findViewById(R.id.message)).setText(questions.get(pos));
							((Button) d.findViewById(R.id.copy)).setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									resetContentView(R.layout.activity_custom_edit, getString(R.string.add), posFinal);
									d.dismiss();
								}
							});
							((Button) d.findViewById(R.id.edit)).setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									resetContentView(R.layout.activity_custom_edit, getString(R.string.update), posFinal);
									d.dismiss();
								}
							});
							((Button) d.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									deleteCustomQuestion(posFinal);
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
			}
		} else if (layoutResId == R.layout.activity_custom_edit) {
			list = null;
			questions = null;
			inputs[0] = (EditText) findViewById(R.id.custom_question_edit_text);
			inputs[1] = (EditText) findViewById(R.id.custom_answer_edit_text);
			inputs[2] = (EditText) findViewById(R.id.custom_wrong1_edit_text);
			inputs[3] = (EditText) findViewById(R.id.custom_wrong2_edit_text);
			inputs[4] = (EditText) findViewById(R.id.custom_wrong3_edit_text);
			inputs[5] = (EditText) findViewById(R.id.category_edit_text);
			inputs[5].setEnabled(false);
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
			category = (Spinner) findViewById(R.id.spinner_category);
			category.setAdapter(adapterCategories);
			category.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if (pos == categories.size() - 1) {
						inputs[5].setEnabled(true);
						inputs[5].setText("");
					} else {
						inputs[5].setEnabled(false);
						inputs[5].setText(category.getSelectedItem().toString());
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
			if ((positionFinal >= 0) && (positionFinal < questionData.size())) {
				inputs[0].setText(questionData.get(positionFinal).getQuestionText());
				String[] answers = questionData.get(positionFinal).getAnswers();
				for (int i = 0; i < answers.length; i++) {
					inputs[i + 1].setText(answers[i]);
				}
				difficulty.setSelection(questionData.get(positionFinal).getDifficulty().getValue());
				category.setSelection(categories.indexOf(questionData.get(positionFinal).getCategory()));
			} else {
				difficulty.setSelection(0);
				category.setSelection(adapterCategories.getCount() - 1);
				inputs[5].setEnabled(true);
			}

			done = (Button) findViewById(R.id.done);
			done.setText(addButtonTextFinal);
			done.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (addButtonTextFinal.equals(getString(R.string.add)))
						addCustomQuestion(inputs[0].getText().toString(), inputs[1].getText().toString(), inputs[2].getText().toString(),
								inputs[3].getText().toString(), inputs[4].getText().toString(), difficulty.getSelectedItemPosition(),
								inputs[5].getText().toString());
					else if (addButtonTextFinal.equals(getString(R.string.update)))
						updateCustomQuestion(positionFinal, inputs[0].getText().toString(), inputs[1].getText().toString(), inputs[2]
								.getText().toString(), inputs[3].getText().toString(), inputs[4].getText().toString(), difficulty
								.getSelectedItemPosition(), inputs[5].getText().toString());
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
					sendEvent("custom_question", "delete_question", questionData.get(posFinal).getQuestionText(),
							(long) questionData.get(posFinal).getDifficulty().getValue());
					dbManager.removeCustomQuestion(questionData.get(posFinal).getID());
					questionData.remove(posFinal);
					questions.remove(posFinal + 3);
					categories.clear();
					categories.add(getString(R.string.category_all));
					for (int i = 0; i < questionData.size(); i++) {
						if (!categories.contains(questionData.get(i).getCategory()))
							categories.add(questionData.get(i).getCategory());
					}
					adapter.notifyDataSetChanged();
					adapterCategories.notifyDataSetChanged();
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

	private void addCustomQuestion(String q, String a, String w1, String w2, String w3, int d, String c) {
		final String[] question = new String[] { q, a, w1, w2, w3, c };
		if (testQuestion(question, true)) {
			final int difficulty = d;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.add_question);
			builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					sendEvent("custom_question", "add_question", question[0], (long) difficulty);
					Money.increaseMoney(EggHelper.unlockEgg(ShowCustomEditActivity.this, moneyText, null, EggKeys[15], EggMaxValues[15]));
					dbManager.addCustomQuestion(question, difficulty, 0, 0);
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

	private void updateCustomQuestion(int position, String q, String a, String w1, String w2, String w3, int d, String c) {
		final int posFinal = position;
		if ((position >= 0) && (position < questionData.size())) {
			final String[] question = new String[] { q, a, w1, w2, w3, c };
			if (testQuestion(question, true)) {
				final int difficulty = d;
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.update_question);
				builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						sendEvent("custom_question", "update_question", question[0], (long) difficulty);
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

	private void showHowTo() {
		String header = QuestionContract.QUESTION_TEXT + "," + QuestionContract.ANSWER_CORRECT + ","
				+ CustomQuestionContract.ANSWER_INCORRECT1 + "," + CustomQuestionContract.ANSWER_INCORRECT2 + ","
				+ CustomQuestionContract.ANSWER_INCORRECT3 + "," + QuestionContract.DIFFICULTY + "\n\n";
		String limit = " " + MAX_LENGTH + " ";
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.howto_import_csv);
		builder.setMessage(getString(R.string.howto_import_csv1) + header + getString(R.string.howto_import_csv2) + limit
				+ getString(R.string.howto_import_csv3));
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Do nothing
			}
		});
		builder.setNegativeButton(R.string.download, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://learnwithhiq.com/downloads.php"));
				startActivity(intent);
			}
		});
		builder.create().show();
	}

	private boolean testQuestion(String q, String a, String w1, String w2, String w3, String c) {
		return testQuestion(new String[] { q, a, w1, w2, w3, c }, false);
	}

	private boolean testQuestion(String[] question, boolean displayDialogs) {
		boolean tooLongShort[] = new boolean[question.length];// { false, false, false, false, false, false };
		boolean same = false;
		List<String> q = new ArrayList<String>();
		for (int i = 0; i < question.length; i++) {
			if ((i > 0 && question[i].length() > MAX_LENGTH) || (i == 0 && question[i].length() > MAX_LENGTH * 3)
					|| (question[i].length() == 0))
				tooLongShort[i] = true;
			else
				tooLongShort[i] = false;
			if ((i > 0) && (i < 5))
				if (q.contains(question[i]))
					same = true;
			q.add(question[i]);
		}
		if (tooLongShort[0] || tooLongShort[1] || tooLongShort[2] || tooLongShort[3] || tooLongShort[4] || tooLongShort[5]) {
			if (displayDialogs) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.input_error);
				builder.setMessage(getString(R.string.max_length) + " " + MAX_LENGTH + " " + getString(R.string.max_length2));
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Do nothing
					}
				});
				builder.create().show();
			}
			return false;
		} else if (same) {
			if (displayDialogs) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.input_error);
				builder.setMessage(getString(R.string.same_entries));
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Do nothing
					}
				});
				builder.create().show();
			}
			return false;
		}
		return true;
	}

	private void initMoney() {
		SharedPreferences sharedPrefsMoney = getSharedPreferences("Packages", 0);
		Money.setMoneyPaid(sharedPrefsMoney.getInt("paid_money", 0));
		Money.setMoney(sharedPrefsMoney.getInt("money", 0));
	}

	private void sendEvent(String category, String action, String label, Long value) {
		MyApplication.getGaTracker().send(MapBuilder.createEvent(category, action, label, value).build());
	}
}
