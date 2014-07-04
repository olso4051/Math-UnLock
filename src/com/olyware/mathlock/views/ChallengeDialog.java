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
	private int lastLength = 0, numFriends = 0;

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
		allContacts = new ArrayList<CustomContactData>();
		contacts.add(new CustomContactData("Friends", "0/0", 0));
		contacts.add(new CustomContactData("Friends to invite", "0/0", 1));

		// Adding items to listview
		adapter = new ContactArrayAdapter(getActivity(), R.layout.list_friend_item, R.layout.list_section_item, contacts);
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
			refreshContacts();
			/*contacts.clear();
			contacts.add(new CustomContactData("Friends", "0/0", 0));
			contacts.add(new CustomContactData("Friends to invite", "0/0", 1));
			contacts.addAll(contactsTemp);
			Collections.sort(contacts);
			allContacts.clear();
			allContacts.addAll(contactsTemp);
			adapter.notifyDataSetChanged();/**/
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
		contacts.add(new CustomContactData("Friends", "0/0", 0));
		contacts.add(new CustomContactData("Friends to invite", "0/0", 1));
		allContacts.clear();
		numFriends = 0;
		ContactHelper.getCustomContactDataAsync(getActivity(), new ContactHelper.contactDataListener() {
			@Override
			public void onNewContactFound(int replaceID, CustomContactData contactData) {
				if (contacts != null && allContacts != null && adapter != null) {
					if (replaceID < 0) {
						if (contactData.isFriend())
							numFriends++;
						contacts.add(contactData);
						allContacts.add(contactData);
						contacts.get(0).setDescription(numFriends + "/" + allContacts.size());
						contacts.get(1 + numFriends).setDescription((allContacts.size() - numFriends) + "/" + allContacts.size());
					} else {
						int replaceAddition = 1 + ((replaceID > numFriends) ? 1 : 0);
						contacts.get(replaceID + replaceAddition).addEmails(contactData.getEmails());
						contacts.get(replaceID + replaceAddition).addPhoneNumbers(contactData.getPhoneNumbers());
						allContacts.get(replaceID).addEmails(contactData.getEmails());
						allContacts.get(replaceID).addPhoneNumbers(contactData.getPhoneNumbers());
					}
					Collections.sort(contacts);
					Collections.sort(allContacts);
					adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onFriendContactFound(int id, String userID) {
				numFriends++;
				contacts.get(0).setDescription(numFriends + "/" + allContacts.size());
				contacts.get(numFriends).setDescription((allContacts.size() - numFriends) + "/" + allContacts.size());
				contacts.get(id + 2).setIsFriend(true);
				contacts.get(id + 2).setUserID(userID);
				Collections.sort(contacts);
				allContacts.get(id).setIsFriend(true);
				allContacts.get(id).setUserID(userID);
				Collections.sort(allContacts);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onDoneFindingContacts(List<String> userPhoneHashes, List<String> userIDHashes) {
				// progress.setVisibility(View.INVISIBLE);
				/*List<ArrayList<Integer>> matchingIndex = ContactHelper.findContact(ContactHelper.FindType.PHONEHASH, allContacts,
						userPhoneHashes);
				for (int i = 0; i < userPhoneHashes.size(); i++) {
					for (int j = 0; j < matchingIndex.get(i).size(); j++) {
						contacts.get(matchingIndex.get(i).get(j)).setIsFriend(true);
						contacts.get(matchingIndex.get(i).get(j)).setUserID(userIDHashes.get(i));
					}
				}
				Collections.sort(contacts);
				allContacts.clear();
				allContacts.addAll(contacts);*/
				ContactHelper.storeContacts(getActivity(), allContacts);
			}
		});
	}
}
