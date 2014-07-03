package com.olyware.mathlock.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.olyware.mathlock.R;
import com.olyware.mathlock.service.CustomContactData;
import com.olyware.mathlock.utils.ContactArrayAdapter;
import com.olyware.mathlock.utils.ContactHelper;

/**
 * Created by Kyle on 2/11/14.
 */
public class ChallengeDialog extends DialogFragment implements View.OnClickListener {

	private ListView lv;
	private ContactArrayAdapter adapter;
	private ArrayList<CustomContactData> contacts, allContacts;
	private EditText inputSearch;
	// private ProgressBar progress;
	private int lastLength = 0;

	public interface ChallengeDialogListener {
		void onInvitePressed();

		void onStartPressed();

		void onNextPressed();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int style = DialogFragment.STYLE_NORMAL;
		int theme = R.style.ChallengeTheme;
		setStyle(style, theme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_challenge, container, false);
		getDialog().setTitle(getString(R.string.fragment_challenge_title));

		// ((Button) v.findViewById(R.id.fragment_challenge_button_invite)).setOnClickListener(this);
		// ((Button) v.findViewById(R.id.fragment_challenge_button_start)).setOnClickListener(this);
		// ((Button) v.findViewById(R.id.fragment_challenge_button_next)).setOnClickListener(this);

		// search box
		inputSearch = (EditText) v.findViewById(R.id.challenge_search);
		inputSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				// When user changed the Text
				if (cs.length() > lastLength) {
					lastLength = cs.length();
					adapter.getFilter().filter(cs);
				} else {
					lastLength = cs.length();
					contacts.clear();
					contacts.addAll(allContacts);
					adapter.getFilter().filter(cs);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}
		});

		// progress bar
		// progress = (ProgressBar) v.findViewById(R.id.challenge_progress);

		// Contacts ListView
		lv = (ListView) v.findViewById(R.id.challenge_list_view);

		// Listview Data
		contacts = new ArrayList<CustomContactData>();
		// contacts.add(new CustomContactData("Kyle Olson", "olso4051@umn.edu", "(651)-895-9737"));
		// contacts.addAll(ContactHelper.getCustomContactData(getActivity()));
		allContacts = new ArrayList<CustomContactData>();
		allContacts.addAll(contacts);

		// Adding items to listview
		adapter = new ContactArrayAdapter(getActivity(), R.layout.list_friend_item, contacts);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				// final CustomContactData data = contacts.get(pos);
				// TODO start challenge or go to select pack dialog
			}
		});

		List<CustomContactData> contactsTemp = new ArrayList<CustomContactData>();
		contactsTemp.addAll(ContactHelper.getStoredContacts(getActivity()));
		if (contactsTemp.size() == 0)
			refreshContacts();
		else {
			contacts.clear();
			contacts.addAll(contactsTemp);
			allContacts.clear();
			allContacts.addAll(contactsTemp);
			adapter.notifyDataSetChanged();
		}

		return v;
	}

	@Override
	public void onClick(View view) {
		/*if (view.getId() == R.id.fragment_challenge_button_invite) {
			ChallengeDialogListener activity = (ChallengeDialogListener) getActivity();
			activity.onInvitePressed();
			this.dismiss();
		} else if (view.getId() == R.id.fragment_challenge_button_start) {
			ChallengeDialogListener activity = (ChallengeDialogListener) getActivity();
			activity.onStartPressed();
			this.dismiss();
		} else if (view.getId() == R.id.fragment_challenge_button_next) {
			ChallengeDialogListener activity = (ChallengeDialogListener) getActivity();
			activity.onNextPressed();
			this.dismiss();
		}*/
	}

	private void refreshContacts() {
		// progress.setVisibility(View.VISIBLE);
		contacts.clear();
		allContacts.clear();
		ContactHelper.getCustomContactDataAsync(getActivity(), new ContactHelper.contactDataListener() {
			@Override
			public void onNewContactFound(int replaceID, CustomContactData contactData) {
				if (contacts != null && allContacts != null && adapter != null) {
					if (replaceID < 0) {
						contacts.add(contactData);
						allContacts.add(contactData);
					} else {
						contacts.get(replaceID).addEmails(contactData.getEmails());
						contacts.get(replaceID).addPhoneNumbers(contactData.getPhoneNumbers());
						allContacts.get(replaceID).addEmails(contactData.getEmails());
						allContacts.get(replaceID).addPhoneNumbers(contactData.getPhoneNumbers());
					}
					Collections.sort(contacts);
					Collections.sort(allContacts);
					adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onDoneFindingContacts() {
				// progress.setVisibility(View.INVISIBLE);
				ContactHelper.storeContacts(getActivity(), allContacts);
			}
		});
	}
}
