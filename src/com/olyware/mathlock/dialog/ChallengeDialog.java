package com.olyware.mathlock.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.ChallengeData;
import com.olyware.mathlock.adapter.ContactArrayAdapter;
import com.olyware.mathlock.adapter.ContactHashes;
import com.olyware.mathlock.database.DatabaseManager;
import com.olyware.mathlock.service.CustomContactData;
import com.olyware.mathlock.service.RefreshContacts;
import com.olyware.mathlock.utils.ChallengeBuilder;
import com.olyware.mathlock.utils.ContactHelper;
import com.olyware.mathlock.utils.ContactHelper.FindType;
import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.PreferenceHelper;

/**
 * Created by Kyle on 2/11/14.
 */
public class ChallengeDialog extends DialogFragment {

	final private static String MaxHeight = "max_height";
	final private static boolean RandomChallenge = false;
	final private static int RandomAddition = RandomChallenge ? 2 : 1;
	final public static String TAG = "fragment_challenge";
	private ListView lv;
	private SwipeRefreshLayout swipeLayout;
	private ContactArrayAdapter adapter;
	private ArrayList<CustomContactData> contacts, allContacts;
	private EditText inputSearch;
	private int lastLength = 0, numFriends = 0;
	private ChallengeDialogListener listener;
	private DatabaseManager dbManager;
	private RefreshContacts refreshContactsTask = null;

	public interface ChallengeDialogListener {
		void onActiveStateSelected();

		void onSentStateSelected(String challengeID, String hiqUserID, String userName, int bet, int diffMin, int diffMax, int questions,
				CustomContactData.ChallengeState state);

		void onNewStateSelected(String challengeID, String userName, int bet, int diffMin, int diffMax, int questions,
				CustomContactData.ChallengeState state);

		void onInactiveSelected(ChallengeBuilder builder);

		void onInviteSelected(String address);
	}

	public void setChallengeDialogListener(ChallengeDialogListener listener) {
		this.listener = listener;
	}

	public void setDatabaseManager(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public static ChallengeDialog newInstance(Context ctx, int maxHeight) {
		ChallengeDialog f = new ChallengeDialog();

		Bundle args = new Bundle();
		args.putInt(MaxHeight, maxHeight);

		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int style = DialogFragment.STYLE_NO_TITLE;
		int theme = R.style.ChallengeTheme;
		setStyle(style, theme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_challenge, container, false);

		// search box
		inputSearch = (EditText) v.findViewById(R.id.challenge_search);
		inputSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				// When user changed the Text
				if (refreshContactsTask != null) {
					refreshContactsTask.cancel(true);
					swipeLayout.setRefreshing(false);
				}
				if (cs.length() > lastLength) {
					lastLength = cs.length();
					adapter.getFilter().filter(cs);
				} else {
					lastLength = cs.length();
					contacts.clear();
					addSectionHeaders();
					contacts.addAll(allContacts);
					Collections.sort(contacts);
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

		// Contacts ListView
		lv = (ListView) v.findViewById(R.id.challenge_list_view);

		// Listview Data
		contacts = new ArrayList<CustomContactData>();
		allContacts = new ArrayList<CustomContactData>();
		addSectionHeaders();

		// Adding items to listview
		adapter = new ContactArrayAdapter(getActivity(), R.layout.list_friend_item, R.layout.list_section_item, contacts);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				CustomContactData selectedContact = contacts.get(pos);
				if (selectedContact.isContact()) {
					if (selectedContact.hasHiqUserID() || selectedContact.isRandom()) {
						String challengeID = selectedContact.getChallengeID();
						String displayName = selectedContact.getDisplayName();
						int bet = PreferenceHelper.getChallengeBet(getActivity(), challengeID);
						int diffMin = PreferenceHelper.getChallengeDifficultyMin(getActivity(), challengeID);
						int diffMax = PreferenceHelper.getChallengeDifficultyMax(getActivity(), challengeID);
						int questions = PreferenceHelper.getChallengeQuestions(getActivity(), challengeID);
						CustomContactData.ChallengeState state = selectedContact.getState();
						Loggy.d("selected contact state =" + state.getValue());
						if (state.equals(CustomContactData.ChallengeState.Active)) {
							listener.onActiveStateSelected();
						} else if (state.equals(CustomContactData.ChallengeState.New)) {
							listener.onNewStateSelected(challengeID, displayName, bet, diffMin, diffMax, questions, state);
						} else if (state.equals(CustomContactData.ChallengeState.Sent)) {
							String hiqUserID = selectedContact.getHiqUserID();
							listener.onSentStateSelected(challengeID, hiqUserID, displayName, bet, diffMin, diffMax, questions, state);
						} else if (state.equals(CustomContactData.ChallengeState.None)) {
							Loggy.d("selected: userName = " + selectedContact.getDisplayName() + " |userID = "
									+ selectedContact.getHiqUserID());
							listener.onInactiveSelected(new ChallengeBuilder(selectedContact.getDisplayName(), selectedContact
									.getHiqUserID()));
						}
					} else {
						String addresses = "";
						for (String address : selectedContact.getPhoneNumbers()) {
							addresses += address + ",";
						}
						listener.onInviteSelected(addresses);
					}
				}
			}
		});

		swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.challenge_swipe);
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshContacts(true);
			}
		});
		swipeLayout.setColorScheme(R.color.white, R.color.light_blue, R.color.blue_on_white, R.color.light_blue);

		List<CustomContactData> contactsTemp = new ArrayList<CustomContactData>();
		contactsTemp.addAll(ContactHelper.getStoredContacts(getActivity()));
		if (contactsTemp.size() == 0)
			refreshContacts(true);
		else {
			contacts.clear();
			addSectionHeaders();
			contacts.addAll(contactsTemp);
			Collections.sort(contacts);
			allContacts.clear();
			allContacts.addAll(contactsTemp);
			Collections.sort(allContacts);
			adapter.notifyDataSetChanged();
			refreshContacts(false);
		}

		return v;
	}

	@Override
	public void onStop() {
		Loggy.d("ChallengeDialog onStop");
		if (refreshContactsTask != null)
			refreshContactsTask.cancel(true);
		refreshContactsTask = null;
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		Bundle args = getArguments();
		int maxHeight = args.getInt(MaxHeight);
		if (maxHeight > 0) {
			Window window = getDialog().getWindow();
			int width = window.getAttributes().width;
			int height = (int) (maxHeight * .80d);
			window.setLayout(width, height);
		}
	}

	private void refreshContacts(boolean refreshPhonebook) {
		swipeLayout.setRefreshing(true);
		numFriends = ContactHelper.getNumberOfFriendsFromContacts(allContacts);
		Loggy.d("test", "numFriends = " + numFriends);
		refreshContactsTask = ContactHelper.getCustomContactDataAsync(getActivity(), allContacts, refreshPhonebook,
				new ContactHelper.contactDataListener() {
					@Override
					public void onNewContactFound(int replaceID, CustomContactData contactData) {

						if (contacts != null && allContacts != null && adapter != null) {
							if (replaceID < 0) {
								if (contactData.isFriend())
									numFriends++;
								contacts.add(contactData);
								allContacts.add(contactData);
							} else {
								int replaceAddition = 1 + ((replaceID > numFriends) ? RandomAddition : 0);
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
					public void onFriendContactFound(int id, String hiqUserID, String userName) {
						if (contacts != null && allContacts != null && adapter != null && getActivity() != null) {
							int replaceAddition = 1;
							if (!contacts.get(id + 1).isFriend()) {
								numFriends++;
								replaceAddition += RandomAddition;
							}
							String oldHiqUserID = contacts.get(id + replaceAddition).getHiqUserID();
							if (!oldHiqUserID.equals(hiqUserID)) {

							}
							CustomContactData.ChallengeState state = PreferenceHelper.getChallengeStateFromUserID(getActivity(), hiqUserID);
							String challengeID = PreferenceHelper.getChallengeIDFromHiqUserID(getActivity(), hiqUserID);
							contacts.get(id + replaceAddition).setIsFriend(true);
							contacts.get(id + replaceAddition).setHiqUserID(hiqUserID);
							contacts.get(id + replaceAddition).setHiqUserName(userName);
							contacts.get(id + replaceAddition).setState(state);
							contacts.get(id + replaceAddition).setChallengeID(challengeID);
							Collections.sort(contacts);
							allContacts.get(id).setIsFriend(true);
							allContacts.get(id).setHiqUserID(hiqUserID);
							allContacts.get(id).setHiqUserName(userName);
							allContacts.get(id).setState(state);
							allContacts.get(id).setChallengeID(challengeID);
							Collections.sort(allContacts);
							adapter.notifyDataSetChanged();
						}
					}

					@Override
					public void onDoneFindingContacts(List<ContactHashes> hashesList) {
						if (allContacts != null && getActivity() != null) {
							for (ContactHashes hashes : hashesList) {
								List<Integer> ids = ContactHelper.findContacts(FindType.PhoneAndFacebookHASH, 0, allContacts, hashes);
								if (ids.size() > 0) {
									int mergeID = ids.get(0);
									int mergeReplaceAddition = 1;
									if (!contacts.get(mergeID + 1).isFriend()) {
										mergeReplaceAddition += RandomAddition;
									}
									for (int i = 1; i < ids.size(); i++) {
										int replaceAddition = 1;
										int id = ids.get(i);
										if (!contacts.get(id + 1).isFriend()) {
											replaceAddition += RandomAddition;
										}
										contacts.get(mergeID + mergeReplaceAddition).mergeWith(contacts.get(id + replaceAddition));
										allContacts.get(mergeID).mergeWith(allContacts.get(id));
									}
									for (int i = 1; i < ids.size(); i++) {
										int replaceAddition = 1;
										int id = ids.get(i);
										if (!contacts.get(id + 1).isFriend()) {
											replaceAddition += RandomAddition;
										}
										contacts.remove(id + replaceAddition);
										allContacts.remove(id);
									}
								}
								adapter.notifyDataSetChanged();
							}
							if (dbManager != null) {
								if (!dbManager.isDestroyed()) {
									ChallengeData randomChallengeData = dbManager.getRandomChallengeID(hashesList);
									if (!randomChallengeData.getChallengeID().equals("")) {
										for (int id = 0; id < contacts.size(); id++) {
											if (contacts.get(id).isRandom()) {
												String challengeID = randomChallengeData.getChallengeID();
												contacts.get(id).setHiqUserID(randomChallengeData.getUserID());
												contacts.get(id).setHiqUserName(
														PreferenceHelper.getChallengeUserName(getActivity(), challengeID));
												contacts.get(id).setState(
														PreferenceHelper.getChallengeStateFromID(getActivity(), challengeID));
												contacts.get(id).setChallengeID(challengeID);
												adapter.notifyDataSetChanged();
												break;
											}
										}
									}
								}
							}
							Loggy.d("storing contacts");
							ContactHelper.storeContacts(getActivity(), allContacts);
							swipeLayout.setRefreshing(false);
						}
					}
				});
	}

	private void addSectionHeaders() {
		contacts.add(new CustomContactData(getString(R.string.fragment_challenge_friends), "", 0));
		if (RandomChallenge)
			contacts.add(new CustomContactData("Random"));
		contacts.add(new CustomContactData(getString(R.string.fragment_challenge_friends_invite), "", 1));
	}
}
